import java.util.*;

public class Scheduler {
    private static final float Wnz = 4F; // weight of new zone for calcs
    private static final float Wcz = 2F; // weight of current zone for calcs
    private static final float Wd = 0.1F; // weight of drone distance from a zone
    private static final float scoreThreshold = 0F; // the score needed in order to reschedule drone
    private static final Position BASE = new Position(0, 0);
    private final HashMap<Zone, ZoneTriageInfo> zonesOnFire;
    private final DroneActionsTable droneActionsTable;


    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler() {
        this.zonesOnFire = new HashMap<>();
        this.droneActionsTable = new DroneActionsTable();
    }

    public synchronized void putZoneOnFire(Zone zone) {
        zonesOnFire.put(zone, new ZoneTriageInfo(zone));
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
                freeDroneInfos.add(droneInfo);
            }
        }
        /* Free up drones for all zones but let one drone stay */

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
            scheduleDrone(droneInfo);
        }

    }

    public synchronized void processDroneInfo(DroneInfo droneInfo, MessagePasser messagePasser) {
        DroneTask newTask;
        switch (droneInfo.getStateID()) {
            case ARRIVED:
                newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RELEASE_AGENT, droneInfo.getZoneToService());
                droneActionsTable.addAction(droneInfo.droneID, newTask);
                break;
            case IDLE:
                Iterator<Map.Entry<Zone, ZoneTriageInfo>> zoneServicingEntriesIter =
                        zonesOnFire.entrySet().iterator();
                while (zoneServicingEntriesIter.hasNext()) {
                    Map.Entry<Zone, ZoneTriageInfo> zoneEntry = zoneServicingEntriesIter.next();
                    if (zoneEntry.getValue().getServicingDrones().containsKey(droneInfo.droneID)) {
                        // remove zoneOnFire
                        messagePasser.send(droneInfo.zoneToService, "localhost", 9000);
                        zoneServicingEntriesIter.remove();
                        break;
                    }
                }
                System.out.println("[" + Thread.currentThread().getName()
                        + "]: Fire Extinguished received from Drone#" + droneInfo.getDroneID()
                        + " Zone: " + droneInfo.getZoneToService());

                newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RECALL);
                droneActionsTable.addAction(droneInfo.droneID, newTask);
                scheduleDrone(droneInfo); // will override recall task if the drone can be rerouted
                break;
            case BASE:
                scheduleDrone(droneInfo); // may do nothing if no fires to respond to
                break;
            default:
                System.out.println("[" + Thread.currentThread().getName()
                        + "]: Scheduler unable to process DroneInfo: " + droneInfo);
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
            return false;
        }
        /* if there is a zone without a drone go there */

        Iterator<Map.Entry<Zone, ZoneTriageInfo>> servicesIterator =
                zonesOnFire.entrySet().iterator();
        while (servicesIterator.hasNext()) {
            Map.Entry<Zone, ZoneTriageInfo> zoneEntry = servicesIterator.next();
            if (zoneEntry.getValue().getSize() == 0) {

                // create new task to service this zone
                DroneTask newTask = new DroneTask(droneInfo.getDroneID()
                        , DroneTaskType.SERVICE_ZONE, zoneEntry.getKey());
                // add drone to servicing structure to keep track of response time
                zoneEntry.getValue().addDrone(droneInfo.droneID, droneInfo.position);
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
            boolean droneAdded = zonesOnFire.get(zoneEntry.getKey()).addDrone(droneInfo.droneID, droneInfo.position);
            if (droneAdded) {
                // create new task to service this zone
                DroneTask newTask = new DroneTask(droneInfo.getDroneID()
                        , DroneTaskType.SERVICE_ZONE, zoneEntry.getKey());
                // add task to actions table
                droneActionsTable.addAction(droneInfo.droneID, newTask);
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the zonesOnFire.
     *
     * @return zonesOnFire
     */
    public HashMap<Zone, ZoneTriageInfo> getZonesOnFire() {
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