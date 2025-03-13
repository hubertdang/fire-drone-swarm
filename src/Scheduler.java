import java.util.*;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static final float Wnz = 4F; // weight of new zone for calcs
    private static final float Wcz = 2F; // weight of current zone for calcs
    private static final float Wd = 0.1F; // weight of drone distance from a zone
    private static final float scoreThreshold = 0F; // the score needed in order to reschedule drone
    private static final Position BASE = new Position(0, 0);
    private final HashMap<Zone, ZoneTriageInfo> zonesOnFire;
    private final DroneBuffer droneBuffer;
    private final FireIncidentBuffer fireBuffer;
    private final DroneActionsTable droneActionsTable;

    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler(DroneBuffer droneBuffer, FireIncidentBuffer fireBuffer) {

        this.zonesOnFire = new HashMap<>();
        this.droneBuffer = droneBuffer;
        this.fireBuffer = fireBuffer;
        this.droneActionsTable = new DroneActionsTable();
    }

    /**
     * Checks for fire incidents by checking for the relevant flags set in the fireIncidentSubsystem
     * Adds the zone to the mission queue based on priority (i.e. severity and agent amount needed)
     * Dispatches the drone to the zone
     * Waits for fire to be extinguished by polling the drone flags
     * Calls the drone back to the base
     */
    @Override
    public void run() {
        while (true) {
            /* check fireBuffer for new messages, update drone actions table */
            if (fireBuffer.newEvent()) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a new event.\n\t" + " adding to mission queue.");
                Zone zoneToService = fireBuffer.popEventMessage();
                zonesOnFire.put(zoneToService, new ZoneTriageInfo(zoneToService));
                scheduleAllDrones(); // scheduling algorithm updates drone actions table for all drones
            }

            /* check for drone messages */
            DroneInfo droneInfo = null;

            if (droneBuffer.hasDroneInfo() ) {

                Object droneInfoObj = droneBuffer.popDroneInfo();
                if (droneInfoObj instanceof DroneInfo) {
                    droneInfo = (DroneInfo) droneInfoObj;
                }
                else if (droneInfoObj instanceof ArrayList<?>) {
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler has no more tasks.");
                    break;
                }
                else if (droneInfoObj == null)
                    System.out.println("DroneInfo was null");
                else {
                    System.out.println(droneInfoObj.getClass().getName());
                    System.out.println(droneInfoObj);
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler has received a invalid message, " +
                            "cannot process DroneInfo");
                    break;
                }

                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a drone info from drone#" + droneInfo.droneID
                        + " | STATE = " + droneInfo.stateID
                        + " | POSITION = " + droneInfo.getPosition()
                        + " | TANK = " + String.format("%.2f L", droneInfo.getAgentTankAmount()));

                /* Update drone actions table */

                DroneTask newTask;
                switch (droneInfo.getStateID()) {
                    case ARRIVED:
                        newTask = new DroneTask(droneInfo.droneID, DroneTaskType.RELEASE_AGENT);
                        droneActionsTable.addAction(droneInfo.droneID, newTask);
                        break;
                    case IDLE:
                        /* remove drone from servicing zones - implies fire is extinguished */
                        Iterator<Map.Entry<Zone, ZoneTriageInfo>> zoneServicingEntriesIter =
                                zonesOnFire.entrySet().iterator();
                        while (zoneServicingEntriesIter.hasNext()) {
                            Map.Entry<Zone, ZoneTriageInfo> zoneEntry = zoneServicingEntriesIter.next();
                            if (zoneEntry.getValue().getServicingDrones().containsKey(droneInfo.droneID)) {
                                // Only 1 drone at the zone and its idle now, so we can safely remove the zoneOnFire
                                if (zoneEntry.getValue().getSize() == 1) {
                                    // remove zoneOnFire
                                    System.out.println("[" + Thread.currentThread().getName()
                                            + "]: Fire Extinguished received from all servicing drones for Zone: "
                                            + zoneEntry.getKey().getId());
                                    zoneServicingEntriesIter.remove();
                                    break;
                                } else {
                                    zoneEntry.getValue().removeDrone(droneInfo.droneID);
                                    break;
                                }
                            }
                        }

                        /* Assign Recall/Reschedule drone */
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

            /* Send messages to drones using drone actions table */
            droneActionsTable.dispatchActions(droneBuffer);


            /* remove Zone from on fire list if it has been serviced */
            if (droneInfo != null && droneInfo.getStateID() == DroneStateID.IDLE
                    && droneInfo.zoneToService.getSeverity() == FireSeverity.NO_FIRE){
                // Acknowledge fire extinguished to FireIncidentSubsystem
                fireBuffer.addAcknowledgementMessage(droneInfo.zoneToService);
                zonesOnFire.remove(droneInfo.zoneToService);
            }

            /* give other threads opportunity to access shared buffers */
            try {
                sleep(3000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * scheduleAllDrones to fight fires by using load balanced worst zone first algorithm
     * and updating the drone actions table based on results.
     */
    private void scheduleAllDrones() {
       /* Gather all drone data */

        DroneTask getAllInfo = new DroneTask(0, DroneTaskType.REQUEST_INFO, null);
        droneBuffer.addDroneTask(getAllInfo);
        ArrayList<DroneInfo> droneInfoList = null;
        Object droneInfoListObj;
        int tries = 0;
        do  {
            while (!droneBuffer.hasDroneInfo()) {
                try {
                    sleep(500); // remove when subsystems are decoupled and use udp
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            droneInfoListObj = droneBuffer.popDroneInfo();
            tries++;

        } while (!(droneInfoListObj instanceof ArrayList) && tries < 10);

        if (droneInfoListObj instanceof ArrayList) {
            droneInfoList = (ArrayList<DroneInfo>) droneInfoListObj;
        } else {
            System.out.println(droneInfoListObj.getClass().getName());
            System.out.println(droneInfoListObj);
            System.out.println("[" + Thread.currentThread().getName()
                    + "]: Scheduler has received a invalid message, " +
                    "cannot process DroneInfo List");
            return;
        }

        /* Add drones that are not busy to free DroneInfo list */

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
            if ( !busyDrones.contains(droneInfo.droneID) ) {
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

    /**
     * Assigns task to a drone using Worst Zone Response Time First Algorithm
     * @param droneInfo the current info of a drone
     * @return true if drone was scheduled, false otherwise
     */
    public boolean scheduleDrone(DroneInfo droneInfo) {
        if (zonesOnFire.isEmpty()) { return false; }
        /* if there is a zone without a drone go there */

        Iterator<Map.Entry<Zone, ZoneTriageInfo>> servicesIterator =
                zonesOnFire.entrySet().iterator();
        while (servicesIterator.hasNext()) {
            Map.Entry<Zone, ZoneTriageInfo> zoneEntry = servicesIterator.next();
            if ( zoneEntry.getValue().getSize() == 0 ) {

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

        for ( Map.Entry<Zone, ZoneTriageInfo> zoneEntry : sortedList ) {
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
}