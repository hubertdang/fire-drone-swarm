import java.util.*;

/**
 * ServicingDronesInfo
 *
 * Stores dynamic context of response time for a specific zone. Including the expected arrival times
 * of drones, total agent flow rate, and methods to manipulate context.
 *
 * servicingDrones := LinkedHashMap<droneId, {arrivalTime, zoneFlowRate}>
 */

public class ServicingDronesInfo {
    private final Integer zoneId;
    private final Float originalRequiredAgent;
    private final Position zonePosition;
    private LinkedHashMap<Integer, Map.Entry<Float, Float>> servicingDrones;
    private float currentResponseTime;

    /**
     * Constructor for the ServicingDronesInfo class
     * @param zone the zone to be serviced
     */
    public ServicingDronesInfo(Zone zone) {
        this.zoneId = zone.getId();
        this.originalRequiredAgent = zone.getRequiredAgentAmount();
        this.zonePosition = zone.getPosition();
        servicingDrones = new LinkedHashMap<>();
        currentResponseTime = -1F;
    }

    /**
     * getCurrentResponseTime
     * @return the currentResponseTime for zone on fire
     */
    public float getCurrentResponseTime() { return currentResponseTime; }

    /**
     * getZoneId
     * @return the zoneId associated with service object
     */
    public int getZoneId() { return zoneId; }

    /**
     * getServicingDrones
     * @return servicingDrones LinkedHashMap structure
     */
    public LinkedHashMap<Integer, Map.Entry<Float, Float>> getServicingDrones() {
        return servicingDrones;
    }

    /**
     * addDrone determines if new drone would reduce zone response time, if so adds to
     * servicingDrones
     * @param droneId the id of drone
     * @param dronePosition the position of drone
     * @return true if the drone was added to servicingDrones, false otherwise
     */
    public boolean addDrone(Integer droneId, Position dronePosition) {
        float distance = dronePosition.distanceFrom(zonePosition);
        float arrivalTime = distance / Drone.TOP_SPEED;
        float zoneFlowRate = AgentTank.AGENT_DROP_RATE * (servicingDrones.size() + 1);
        float currentVolume = 0;
        float prevFlowRate = 0;
        float lastTime = 0;

        if ( servicingDrones.containsKey(droneId) ) { return false; }
        if (arrivalTime < currentResponseTime || currentResponseTime == -1 ) {
            // add drone scheduling details to servicingDrones
            Map.Entry<Float, Float> serviceEntry = new AbstractMap.SimpleEntry<>(arrivalTime, zoneFlowRate);
            servicingDrones.put(droneId, serviceEntry);
            sortServicingDrones();
            updateResponseTime();
            return true;
        }

        return false;
    }

    /**
     * removeDrone
     * Removes drone from servicing structure
     * @param droneId the ID of drone to remove
     * @return true if successful, false otherwise
     */
    public boolean removeDrone(int droneId) {
        Map.Entry<Float, Float> entry = servicingDrones.remove(droneId);
        if (entry != null) {
            updateResponseTime();
        }
        return (entry != null);
    }

    /**
     * sortServicingDrones
     * configures the servicingDronesStructure in order of ascending drone arrival times
     */
    private void sortServicingDrones() {
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
     */
    private void updateResponseTime() {
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
        currentResponseTime = lastTime + (originalRequiredAgent - currentVolume) / prevFlowRate;
    }

    /**
     * getSize
     * @return the number of entries in LinkedHashMap
     */
    public int getSize() { return servicingDrones.size(); }
}
