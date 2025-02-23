import java.util.*;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static final float Wnz = 4F; // weight of new zone for calcs
    private static final float Wcz = 2F; // weight of current zone for calcs
    private static final float Wd = 0.1F; // weight of drone distance from a zone
    private static final float scoreThreshold = 0F; // the score needed in order to reschedule drone
    private static final Position BASE = new Position(0, 0);
    private final Missions missionQueue;
    private final DroneBuffer droneBuffer;
    private final FireIncidentBuffer fireBuffer;
    private final DroneActionsTable droneActionsTable;

    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler(DroneBuffer droneBuffer, FireIncidentBuffer fireBuffer) {

        this.missionQueue = new Missions();
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
                handleFireReq(zoneToService);
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
                    // scheduling algorithm updates drone actions table, may be able to service a
                    // new zone after refilling
                    if (!missionQueue.isEmpty()) {
                        scheduleDrones();
                    }
                }

                if (droneActionsTable.getAction(droneInfo.droneID) != null /*&& droneActionsTable.getAction(droneInfo.droneID).getState() != DroneStateID.UNDEFINED*/){
                    droneActionsTable.getAction(droneInfo.droneID).setNotify(droneInfo.getStateID());
                }
            }

            /* Send messages to drones using drone actions table */
            droneActionsTable.dispatchActions(droneBuffer, missionQueue);


            if (droneInfo != null && droneInfo.getStateID() == DroneStateID.IDLE
                    && droneActionsTable.getAction(droneInfo.droneID) != null
                    && droneActionsTable.getAction(droneInfo.droneID).getZone().getSeverity() == FireSeverity.NO_FIRE){
                System.out.println("remove1");
                missionQueue.remove(droneInfo.zoneToService);
                System.out.println("ZONE REMOVED = " + droneInfo.zoneToService + "| MISSION QUEUE = " + missionQueue.getMissions());
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
     * Adds an event to mission queue based on fire severity and amount of agent needed.
     * Dispatches a drone to the new zone if the new zone has a higher priority than the
     * zone being currently serviced by the drone.
     *
     * @param zone a Zone object
     */
    public void handleFireReq(Zone zone) {
        missionQueue.updateQueue(zone, null);
    }

    /**
     * Returns the mission queue of the scheduler
     *
     * @return MissionQueue
     */
    public Missions getMissionQueue() {
        return this.missionQueue;
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
                    + "]: AAA Scheduler has received a invalid message, " +
                    "cannot process DroneInfo List");
            return;
        }

        /* Run algorithm for all hazardous zones */

        Iterator<Map.Entry<Zone, DroneScores>> missionsIterator =
                missionQueue.getMissions().entrySet().iterator();
        LinkedHashMap<Zone, DroneScores> tempMissions = new LinkedHashMap<>();
        while (missionsIterator.hasNext()) {
            /* Score drones */
            Map.Entry<Zone, DroneScores> zoneFighters = missionsIterator.next();
            DroneScores droneScores = new DroneScores();
            for (DroneInfo droneInfo : droneInfoList) {
                SchedulerSubState currDroneActionsTable = droneActionsTable.getAction(droneInfo.droneID);

                float score = calculateDroneScore(droneInfo, zoneFighters.getKey());
                droneScores.add(droneInfo.droneID, score);

                if (currDroneActionsTable != null && score > currDroneActionsTable.getScore()
                        && zoneFighters.getKey() == currDroneActionsTable.getZone()){
                    currDroneActionsTable.setScore(score);
                }
            }
            tempMissions.put(zoneFighters.getKey(), droneScores);


        }

        missionQueue.replaceMissions(tempMissions);
        System.out.println("[" + Thread.currentThread().getName()
                + "]: Mission Queue Updated: " + missionQueue);

        /* Update drone actions table */

        /* Possibility that a drone has same score for 2 different zones but we arent going to
            account for that :)
         */

        Iterator<Map.Entry<Zone, DroneScores>> missionsIterator2 =
                missionQueue.getMissions().entrySet().iterator();
        while ( missionsIterator2.hasNext() ) {
            Map.Entry<Zone, DroneScores> zoneFighters = missionsIterator2.next();
            int numDrones = zoneFighters.getValue().getScores().size();

            // set drones to fight fires
            for ( int i = 0 ; i < numDrones ; i++) {
                // get the drone id with the highest score for "this" zone
                int droneId = zoneFighters.getValue().getScores().get(i).getKey();
                float curDroneScore = zoneFighters.getValue().getScores().get(i).getValue();
                SchedulerSubState droneActions = droneActionsTable.getAction(droneId);

                System.out.println("HERE");
                if (droneActions == null) {
                    System.out.println("AAAAA FIRST CASE");
                    droneActionsTable.addAction(droneId, new HappyPathSubState(zoneFighters.getKey()
                            , true, curDroneScore));
                }
                else if (curDroneScore > scoreThreshold
                        && (curDroneScore > droneActions.getScore() || droneActions.getZone().getSeverity() == FireSeverity.NO_FIRE)
                        && droneActions.getZone() != zoneFighters.getKey()) {
                    System.out.println("AAAAA SECOND CASE");
                    if (droneActions.getZone().getSeverity() == FireSeverity.NO_FIRE) {
                        System.out.println("remove2");
                        missionQueue.remove(droneActions.getZone());
                        System.out.println("ZONE REMOVED = " + droneActions.getZone() + "| MISSION QUEUE = " + missionQueue.getMissions());
                    }
                    droneActionsTable.updateAction(droneId, new HappyPathSubState(zoneFighters.getKey()
                            , true, curDroneScore));
                }
            }
        }
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