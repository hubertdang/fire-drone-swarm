import java.util.*;

import static java.lang.Thread.sleep;

public class Scheduler {
    public static final int FEH_PORT = 7000;
    public static final int DRH_PORT = 7001;
    private static final float Wnz = 4F; // weight of new zone for calcs
    private static final float Wcz = 2F; // weight of current zone for calcs
    private static final float Wd = 0.1F; // weight of drone distance from a zone
    private static final float scoreThreshold = 0F; // the score needed in order to reschedule drone
    private static final Position BASE = new Position(0, 0);
    private volatile Map<Zone, ZoneTriageInfo> zonesOnFire;
    private final DroneActionsTable droneActionsTable;
    private HashMap<Integer, Float> hardcodeZoneNeededAgent;


    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler() {
        this.zonesOnFire = Collections.synchronizedMap(new HashMap<>());
        this.droneActionsTable = new DroneActionsTable();
        this.hardcodeZoneNeededAgent = new HashMap<>();
    }

    public synchronized void putZoneOnFire(Zone zone) {
        System.out.println("#ADDED FIRE @ ZONE" + zone.getId());
        zonesOnFire.put(zone, new ZoneTriageInfo(zone));
        hardcodeZoneNeededAgent.put(zone.getId(), zone.getRequiredAgentAmount());
    }

    public synchronized void scheduleDrones(ArrayList<DroneInfo> droneInfoList) {
        ArrayList<DroneInfo> freeDroneInfos = new ArrayList<>();
        ArrayList<Integer> busyDrones = new ArrayList<>();

        for (Map.Entry<Zone, ZoneTriageInfo> zoneEntry : zonesOnFire.entrySet()) {
            Set<Map.Entry<Integer, Map.Entry<Float, Float>>> servicingDrones =
                    zoneEntry.getValue().getServicingDrones().entrySet();
            for (Map.Entry<Integer, Map.Entry<Float, Float>> droneEntry : servicingDrones) {
                busyDrones.add(droneEntry.getKey()); // find all busy drone ids
            }
        }
        for (DroneInfo droneInfo : droneInfoList) {
            if (!busyDrones.contains(droneInfo.droneID)) {
                if (droneInfo.getStateID() == DroneStateID.FAULT) {
                    System.out.println("[" + Thread.currentThread().getName() + "]: "
                            + "Drone#" + droneInfo.droneID
                            + " | OFFLINE "
                            + " | FAULT = " + droneInfo.fault);
                } else if (droneInfo.agentTankAmount == 0F) {
                    System.out.println("[" + Thread.currentThread().getName() + "]: "
                            + "Drone#" + droneInfo.droneID
                            + " | EMPTY ");
                } else {
                    freeDroneInfos.add(droneInfo);
                }
            }
        }
        /* Free up drones for all zones but let one drone stay  */

        Iterator<Map.Entry<Zone, ZoneTriageInfo>> servicesIterator =
                zonesOnFire.entrySet().iterator();
        while (servicesIterator.hasNext()) {
            Map.Entry<Zone, ZoneTriageInfo> zoneEntry = servicesIterator.next();
            if (zoneEntry.getValue().getSize() > 1) {
                // remove all but 1 drone from zone
                Iterator<Map.Entry<Integer, Map.Entry<Float, Float>>> servicingDronesIterator =
                        zoneEntry.getValue().getServicingDrones().entrySet().iterator();
                servicingDronesIterator.next(); // skip first drone
                while (servicingDronesIterator.hasNext()) {
                    Map.Entry<Integer, Map.Entry<Float, Float>> freeDrone =
                            servicingDronesIterator.next();

                    DroneInfo freeDroneInfo = DroneInfo.droneInfoLookUp(
                            freeDrone.getKey(), droneInfoList);
                    droneActionsTable.removeAction(freeDrone.getKey());
                    freeDroneInfos.add(freeDroneInfo);
                    servicingDronesIterator.remove();
                }
            }
        }

        /* Run algorithm for all free drones */

        for (DroneInfo droneInfo : freeDroneInfos) {
            if (droneInfo != null) scheduleDrone(droneInfo);
        }

    }

    public synchronized void processDroneInfo(DroneInfo droneInfo, MessagePasser messagePasser, ArrayList<DroneInfo> droneInfoList) {
        DroneTask newTask;
        switch (droneInfo.getStateID()) {
            case ARRIVED:
                newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RELEASE_AGENT, droneInfo.getZoneToService(), DRH_PORT);
                droneActionsTable.addAction(droneInfo.droneID, newTask);
                break;
            case EMPTY_TANK:
                if (zonesOnFire.get(droneInfo.zoneToService) != null) {
                    zonesOnFire.get(droneInfo.zoneToService).removeDrone(droneInfo);
                    // hard code updating agent needed
                    Float oldAgentNeeded = hardcodeZoneNeededAgent.get(droneInfo.zoneToService.getId());
                    Float newAgentNeeded = oldAgentNeeded - droneInfo.getReleasedAgentAmount();
                    if (newAgentNeeded <= 0) {
                        hardcodeZoneNeededAgent.put(droneInfo.zoneToService.getId(), 0F);
                        // remove zoneOnFire
                        ZoneTriageInfo triageInfo = zonesOnFire.remove(droneInfo.zoneToService);
                        if (triageInfo != null) {
                            Zone noFire = droneInfo.getZoneToService();
                            noFire.setSeverity(FireSeverity.NO_FIRE);
                            noFire.setRequiredAgentAmount(0);
                            messagePasser.send(noFire, "localhost", 9000);
                            System.out.println("[" + Thread.currentThread().getName()
                                    + "]: 🧯 Fire Extinguished received from Drone#" + droneInfo.getDroneID()
                                    + " Zone: " + droneInfo.getZoneToService());
                        }

                        // reschedule drone
                        newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RECALL, null, DRH_PORT);
                        if (!scheduleDrone(droneInfo)) {droneActionsTable.addAction(droneInfo.droneID, newTask);}

                    } else {
                        hardcodeZoneNeededAgent.put(droneInfo.zoneToService.getId(), newAgentNeeded);
                        newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RECALL, droneInfo.getZoneToService(), DRH_PORT);
                        droneActionsTable.addAction(droneInfo.droneID, newTask);
                    }
                } else {
                    newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RECALL, droneInfo.getZoneToService(), DRH_PORT);
                    droneActionsTable.addAction(droneInfo.droneID, newTask);
                }
                break;
            case IDLE:
                Iterator<Map.Entry<Zone, ZoneTriageInfo>> zoneServicingEntriesIter =
                        zonesOnFire.entrySet().iterator();
                while (zoneServicingEntriesIter.hasNext()) {
                    Map.Entry<Zone, ZoneTriageInfo> zoneEntry = zoneServicingEntriesIter.next();

                    if (zoneEntry.getValue().getServicingDrones().containsKey(droneInfo.droneID) && droneInfo.getZoneToService().getSeverity() == FireSeverity.NO_FIRE) {
                        // remove zoneOnFire
                        messagePasser.send(droneInfo.zoneToService, "localhost", 9000);
                        zoneServicingEntriesIter.remove();
                        System.out.println("#REMOVED ZONE: " + zoneEntry.getKey());

                        System.out.println("[" + Thread.currentThread().getName()
                                + "]: 🧯 Fire Extinguished received from Drone#" + droneInfo.getDroneID()
                                + " Zone: " + droneInfo.getZoneToService());

                        break;
                    }
                }
                try {
                    sleep(500);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RECALL, null, DRH_PORT);
                if (!scheduleDrone(droneInfo)) {droneActionsTable.addAction(droneInfo.droneID, newTask);}

                break;
            case BASE:
                System.out.println("#REMOVE: " + droneInfo);
                scheduleDrone(droneInfo); // may do nothing if no fires to respond to
                break;
            default:
                System.out.println("[" + Thread.currentThread().getName()
                        + "]: Unable to process DroneInfo: " + droneInfo);
        }
    }

    public synchronized void dispatchActions(MessagePasser messagePasser, int droneID) {
        droneActionsTable.dispatchActions(messagePasser, droneID);

    }



    /**
     * Assigns task to a drone using Worst Zone Response Time First Algorithm
     *
     * @param droneInfo the current info of a drone
     * @return true if drone was scheduled, false otherwise
     */
    private boolean scheduleDrone(DroneInfo droneInfo) {
        if (zonesOnFire.isEmpty()) {
            System.out.println("Zones NOT on fire ❌🔥: " + zonesOnFire);
            return false;
        }
        /* if there is a zone without a drone go there */
        System.out.println("Zones still on fire 🔥: " + zonesOnFire);
        Iterator<Map.Entry<Zone, ZoneTriageInfo>> servicesIterator =
                zonesOnFire.entrySet().iterator();
        while (servicesIterator.hasNext()) {
            Map.Entry<Zone, ZoneTriageInfo> zoneEntry = servicesIterator.next();
            if (zoneEntry.getValue().getSize() == 0) { //load balancing

                // create new task to service this zone
                DroneTask newTask = new DroneTask(droneInfo.getDroneID()
                        , DroneTaskType.SERVICE_ZONE, zoneEntry.getKey());
                // add drone to servicing structure to keep track of response time
                zoneEntry.getValue().addDrone(droneInfo);
                // add task to actions table
                droneActionsTable.addAction(droneInfo.droneID, newTask);
                return true;
            }
        }

        /* Sort zones from longest to shortest extinguish time */
        List<Map.Entry<Zone, ZoneTriageInfo>> sortedList = new ArrayList<>(zonesOnFire.entrySet());
        sortedList.sort((a, b)
                -> Float.compare(b.getValue().getExtinguishingTime(),
                a.getValue().getExtinguishingTime()));

        for (Map.Entry<Zone, ZoneTriageInfo> zoneEntry : sortedList) {
            // add drone to servicing structure to keep track of response time
            if(zoneEntry.getValue().getSize() < 3){
                boolean droneAdded = zonesOnFire.get(zoneEntry.getKey()).addDrone(droneInfo);
                if (droneAdded) {
                    // create new task to service this zone
                    DroneTask newTask = new DroneTask(droneInfo.getDroneID()
                            , DroneTaskType.SERVICE_ZONE, zoneEntry.getKey(), 7000);
                    // add task to actions table
                    droneActionsTable.addAction(droneInfo.droneID, newTask);
                    return true;
                }
                try {
                    sleep(500);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        return false;
    }


    /**
     * Returns the zonesOnFire.
     *
     * @return zonesOnFire
     */
    public Map<Zone, ZoneTriageInfo> getZonesOnFire() {
        return zonesOnFire;
    }

    /**
     * Returns the droneActionsTable
     *
     * @return droneActionsTable
     */
    public DroneActionsTable getDroneActionsTable() {
        return droneActionsTable;
    }
}