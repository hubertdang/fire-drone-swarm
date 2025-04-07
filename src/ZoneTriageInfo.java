import java.util.*;

/**
 * ZoneTriageInfo
 *
 * Stores dynamic context of response time for a specific zone. Including the expected arrival times
 * of drones, total agent flow rate, and methods to manipulate context.
 *
 * servicingDrones := LinkedHashMap<droneId, {arrivalTime, zoneFlowRate}>
 */

public class ZoneTriageInfo {
    private final Integer zoneId;
    private volatile Float requiredAgentAmount;
    private final Position zonePosition;
    private LinkedHashMap<Integer, Map.Entry<Float, Float>> servicingDrones;
    private float extinguishingTime;
    private List<Integer> servicingDroneIDs;

    /**
     * Constructor for the ZoneTriageInfo class
     * @param zone the zone to be serviced
     */
    public ZoneTriageInfo(Zone zone) {
        this.zoneId = zone.getId();
        this.requiredAgentAmount = zone.getRequiredAgentAmount();
        this.zonePosition = zone.getPosition();
        servicingDrones = new LinkedHashMap<>();
        extinguishingTime = -1F;
        servicingDroneIDs = Collections.synchronizedList(new ArrayList<Integer>());
    }

    public synchronized void updateRequiredAgentAmount(Float agentDropped) {
        System.out.println("#OLD REQUIRED AMOUNT"+ requiredAgentAmount);
        this.requiredAgentAmount -= agentDropped;
        System.out.println("#DECREASING BY"+ agentDropped);
        System.out.println("#NEW REQUIRED AMOUNT"+ requiredAgentAmount);
        if(requiredAgentAmount < 0) {
            requiredAgentAmount = 0F;
        }
    }

    public synchronized Float getRequiredAgentAmount() {
        return requiredAgentAmount;
    }

    /**
     * getExtinguishingTime
     * @return the current extinguishing time for zone on fire
     */
    public synchronized float getExtinguishingTime() { return extinguishingTime; }

    /**
     * getZoneId
     * @return the zoneId associated with service object
     */
    public synchronized int getZoneId() { return zoneId; }

    /**
     * getServicingDrones
     * @return servicingDrones LinkedHashMap structure
     */
    public synchronized LinkedHashMap<Integer, Map.Entry<Float, Float>> getServicingDrones() {
        return servicingDrones;
    }

    public synchronized List<Integer> getServicingDroneIDs() {
        return servicingDroneIDs;
    }

    /**
     * addDrone determines if new drone would reduce zone response time, if so adds to
     * servicingDrones
     * @param info the drone info
     * @return true if the drone was added to servicingDrones, false otherwise
     */
    public synchronized boolean addDrone(DroneInfo info) {
        float distance = info.getPosition().distanceFrom(zonePosition);
        // Proper accel and deccel should be used but is overly complicated
        float arrivalTime = (float) (distance / Drone.TOP_SPEED);
        float zoneFlowRate = AgentTank.AGENT_DROP_RATE * (servicingDrones.size() + 1);
        float currentVolume = 0;
        float prevFlowRate = 0;
        float lastTime = 0;
        if (info.zoneToService != null) requiredAgentAmount = info.getZoneToService().getRequiredAgentAmount();

        if ( servicingDrones.containsKey(info.droneID) ) { return false; }
        if (arrivalTime < extinguishingTime || extinguishingTime == -1 ) {
            // add drone scheduling details to servicingDrones
            Map.Entry<Float, Float> serviceEntry = new AbstractMap.SimpleEntry<>(arrivalTime, zoneFlowRate);
            servicingDrones.put(info.droneID, serviceEntry);
            sortServicingDrones();
            updateResponseTime();
            servicingDroneIDs.add(info.droneID);
            return true;
        }

        return false;
    }

    /**
     * removeDrone
     * Removes drone from servicing structure
     * @param info the drone to remove
     * @return true if successful, false otherwise
     */
    public synchronized boolean removeDrone(DroneInfo info) {
        Map.Entry<Float, Float> entry = servicingDrones.remove(info.droneID);
        if (entry != null) {
            System.out.println();
            if (info.zoneToService != null) {
                requiredAgentAmount = info.getZoneToService().getRequiredAgentAmount();
            }
            updateResponseTime();
            servicingDroneIDs.remove((Integer) info.droneID);
        }
        return (entry != null);
    }

    /**
     * sortServicingDrones
     * configures the servicingDronesStructure in order of ascending drone arrival times
     */
    private synchronized void sortServicingDrones() {
        servicingDrones = servicingDrones.entrySet()
                .stream()
                .sorted(Comparator.comparing
                        (entry -> entry.getValue().getKey())) // sort by arrival time
                .collect(LinkedHashMap::new,
                        (map, entry)
                                -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
    }

    /**
     * updateResponseTime
     * Updates the total response time for the drones servicing this zone
     *
     * total response time for a zone = arrival time of first drone +
     * (Agent needed / agent drop time * numdrones)
     * drones can arrive at different times
     * so sum of agent drop rates is scaled as they arrive
     *
     * Refer to Scheduler Algorithm Doc
     */
    private synchronized void updateResponseTime() {
        float currentVolume = 0;
        float prevFlowRate = 0;
        float lastTime = 0;

        // recalculate agent drop rates
        int order = 1;
        for (Map.Entry<Integer, Map.Entry<Float, Float>> e : servicingDrones.entrySet()) {
            float newFlowRate = AgentTank.AGENT_DROP_RATE * order++;
            e.setValue(new AbstractMap.SimpleEntry<>(e.getValue().getKey(), newFlowRate));
        }

        // Determine fire response time

        SequencedSet<Map.Entry<Integer, Map.Entry<Float, Float>>> entrySet =
                servicingDrones.sequencedEntrySet();

        /* servicingDrones := LinkedHashMap<droneId, {arrivalTime, zoneFlowRate}> */

        for (Map.Entry<Integer, Map.Entry<Float, Float>> serviceDroneEntry : entrySet ) {
            float entryArrivalTime = serviceDroneEntry.getValue().getKey();
            float entryFlowRate = serviceDroneEntry.getValue().getValue();

            // time interval where previous flow rate was active
            float timeInterval = entryArrivalTime - lastTime;

            // fire suppression contribution from other drones prior to new drone arrival
            currentVolume += (timeInterval * prevFlowRate);

            // update values for next iteration
            lastTime = entryArrivalTime;
            prevFlowRate = entryFlowRate;
        }
        // compute remaining time with full rate
        extinguishingTime = lastTime + (requiredAgentAmount - currentVolume) / prevFlowRate;
    }

    /**
     * getSize
     * @return the number of entries in LinkedHashMap
     */
    public synchronized int getSize() { return servicingDrones.size(); }
}
