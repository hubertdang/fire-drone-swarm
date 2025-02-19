import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static final float Wnz = 1F; // weight of new zone for calcs
    private static final float Wcz = 1F; // weight of current zone for calcs
    private static final float Wd = 0.5F; // weight of drone distance from a zone
    private static final Position BASE = new Position(0, 0);
    private final MissionQueue missionQueue;
    private final DroneBuffer droneBuffer;
    private final FireIncidentBuffer fireBuffer;

    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler(DroneBuffer droneBuffer, FireIncidentBuffer fireBuffer) {

        this.missionQueue = new MissionQueue();
        this.droneBuffer = droneBuffer;
        this.fireBuffer = fireBuffer;
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
            /* check fireBuffer for new messages, add to mission queue */
            if (fireBuffer.newEvent()) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a new event.\n\t" + " adding to mission queue.");
                Zone zoneToService = fireBuffer.popEventMessage();
                handleFireReq(zoneToService);
            }

            /* check for drone messages */
            if (droneBuffer.hasDroneInfo()) {
                DroneInfo droneInfo = null;
                Object droneInfoObj = droneBuffer.popDroneInfo();
                if (droneInfoObj instanceof DroneInfo) {
                    droneInfo = (DroneInfo) droneInfoObj;
                } else {
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler has received a invalid message, " +
                            "cannot process DroneInfo");
                    break;
                }

                System.out.println("[" + Thread.currentThread().getName()
                        + "]: Scheduler has received a drone info from" + " drone#"
                        + droneInfo.droneID + ": Position: (" + droneInfo.getPosition().getX()
                        + "," + droneInfo.getPosition().getY() + ")" + " AgentLeft: "
                        + droneInfo.getAgentTankAmount());
                /* hard coding this for now, TODO: real scheduling */
                switch (droneInfo.getStateID()) {
                    case DroneStateID.ARRIVED:
                        droneBuffer.addDroneTask(
                                new DroneTask(droneInfo.getDroneID(), DroneTaskType.RELEASE_AGENT));
                        break;
                    case DroneStateID.IDLE:
                        /* only putting out one zone for now */
                        droneBuffer.addDroneTask(
                                new DroneTask(droneInfo.getDroneID(), DroneTaskType.RECALL));
                }
            }

            /* handle missions from the mission queue */
            if (!missionQueue.isEmpty()) {
                Zone missionZone = missionQueue.pop();
                DroneTask newMission = new DroneTask(1, DroneTaskType.SERVICE_ZONE, missionZone);
                droneBuffer.addDroneTask(newMission);
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
        missionQueue.queue(zone);
    }

    /**
     * Returns the mission queue of the scheduler
     *
     * @return MissionQueue
     */
    public MissionQueue getMissionQueue() {
        return this.missionQueue;
    }

    /**
     * Schedule drones to fight fires by calculating drone scores based on defined algorithm,
     * and updating the drone actions table based on results.
     *
     * @param newZone the Zone to be serviced
     */
    private void scheduleDrones(Zone newZone) {
        DroneScores droneScores = new DroneScores();
        /* Gather all drone data */

        DroneTask getAllInfo = new DroneTask(0, DroneTaskType.REQUEST_ALL_INFO, null);
        droneBuffer.addDroneTask(getAllInfo);
        while (!droneBuffer.hasDroneInfo()) {
            try {
                sleep(500); // remove when subsystems are decoupled and use udp
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        ArrayList<DroneInfo> droneInfoList = null;
        Object droneInfoListObj = droneBuffer.popDroneInfo();
        if (droneInfoListObj instanceof ArrayList) {
            droneInfoList = (ArrayList<DroneInfo>) droneInfoListObj;
        } else {
            System.out.println("[" + Thread.currentThread().getName()
                    + "]: Scheduler has received a invalid message, " +
                    "cannot process DroneInfo List");
            return;
        }

        /* Score drones */

        for (DroneInfo droneInfo : droneInfoList) {
            droneScores.add(new Pair<>(droneInfo.droneID, calculateDroneScore(droneInfo, newZone)));
        }

        /* Do this for all zones in actions table

        /* Update drone actions table */

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