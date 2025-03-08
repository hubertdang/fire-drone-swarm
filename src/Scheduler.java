import java.sql.Array;
import java.util.*;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static final float Wnz = 4F; // weight of new zone for calcs
    private static final float Wcz = 2F; // weight of current zone for calcs
    private static final float Wd = 0.1F; // weight of drone distance from a zone
    private static final float scoreThreshold = 0F; // the score needed in order to reschedule drone
    private static final Position BASE = new Position(0, 0);
    private final HashMap<Zone, ServicingDronesInfo> zonesOnFire;
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
                zonesOnFire.put(zoneToService, null);
                scheduleDrones(); // scheduling algorithm updates drone actions table
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
                        + " | POSITION = " + droneInfo.getPosition()
                        + " | TANK = " + String.format("%.2f L", droneInfo.getAgentTankAmount()));

                /* Update drone actions table */
                if (droneInfo.getStateID() == DroneStateID.IDLE || droneInfo.getStateID() == DroneStateID.BASE) {
                    scheduleDrone(droneInfo);
                }
            }

            /* Send messages to drones using drone actions table */
            droneActionsTable.dispatchActions(droneBuffer);


            /* remove Zone from on fire list id it has been serviced */
            if (droneInfo != null && droneInfo.getStateID() == DroneStateID.IDLE
                    && droneInfo.zoneToService.getSeverity() == FireSeverity.NO_FIRE){
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
     * Schedule drones to fight fires by calculating drone scores for each zone
     * based on defined algorithm,
     * and updating the drone actions table based on results.
     *
     * Updating Drone Actions Table options
     *  1. No current drone actions, add HappyPath SubState
     *  2. Has drone actions but threshold met, Reroute SubState
     *  3. Drone is empty, Recall SubState *** currently handled by drone itself
     */
    private void scheduleDrones() {
       /* Gather all drone data */

        DroneTask getAllInfo = new DroneTask(0, DroneTaskType.REQUEST_ALL_INFO, null);
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

        /* Run algorithm for all hazardous zones */

        for (DroneInfo droneInfo : droneInfoList) {
            scheduleDrone(droneInfo);
        }

    }

    /**
     * Assigns task to a drone using Worst Zone Response Time First Algorithm
     * @param droneInfo the current info of a drone
     * @return true if drone was scheduled, false otherwise
     */

    // ToDo add reroute drones capability
    public boolean scheduleDrone(DroneInfo droneInfo) {
        if (zonesOnFire.isEmpty()) { return false; }
        /* if there is a zone without a drone go there */

        Iterator<Map.Entry<Zone, ServicingDronesInfo>> servicesIterator =
                zonesOnFire.entrySet().iterator();
        while (servicesIterator.hasNext()) {
            Map.Entry<Zone, ServicingDronesInfo> zoneEntry = servicesIterator.next();
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

        /* All zones have 1 drone assign the drone to zone with worst response time */

        Zone worstZone = null;
        float worstResponseTime = 0;

        for (Map.Entry<Zone, ServicingDronesInfo> zoneEntry : zonesOnFire.entrySet() ) {
            float currEntryResponseTime = zoneEntry.getValue().getCurrentResponseTime();
            if (currEntryResponseTime > worstResponseTime) {
                worstZone = zoneEntry.getKey();
                worstResponseTime = currEntryResponseTime;
            }
        }

        // create new task to service this zone
        DroneTask newTask = new DroneTask(droneInfo.getDroneID()
                , DroneTaskType.SERVICE_ZONE, worstZone);
        // add drone to servicing structure to keep track of response time
        zonesOnFire.get(worstZone).addDrone(droneInfo.droneID, droneInfo.position);
        // add task to actions table
        droneActionsTable.addAction(droneInfo.droneID, newTask);

        return true;
    }

    /**
     * Calculate weighted drone score.
     * Drone Score = Wnz x (CA - RAnz) - Wcz x (CA - RAcz) - Wd x Distance From Zone
     *
     * @param droneInfo the most recent info of drone
     * @param newZone the new zone needing service
     * @return the drones weighted score for servicing new zone
     */
    private float calculateDroneScore(DroneInfo droneInfo, Zone newZone) {
        return Wnz * (droneInfo.agentTankAmount - newZone.getRequiredAgentAmount())
                - Wcz * (droneInfo.agentTankAmount - droneInfo.getCurrentZoneAgentTankAmount())
                - Wd * newZone.getPosition().distanceFrom(droneInfo.position);
    }

}