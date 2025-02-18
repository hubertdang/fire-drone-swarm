import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
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
                DroneInfo droneInfo = droneBuffer.popDroneInfo();
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
     * Updates the fire severity of a zone
     *
     * @param zone
     */
//    public void zoneSeverityUpdated(Zone zone) {
//        if (zone.getSeverity() == FireSeverity.NO_FIRE) {
//            drone.stopAgent();
//            drone.fly(BASE);
//        }
//    }


    /**
     * Dispatches a drone to a zone in the mission queue
     *
     * @param drone
     * @param zone
     */
//    public void dispatch() {
//        drone.setZoneToService(missionQueue.pop());
//        drone.fly(zone.getPosition());
//    }

    /**
     * Compares priority of 2 zones - takes into account fire severity and amount of agent needed
     *
     * @param zone1
     * @param zone2
     * @return true if zone1 has higher priority than zone2, false otherwise
     */
//    public boolean comparePriority(Zone zone1, Zone zone2) {
//        if (zone1.getSeverity() == FireSeverity.LOW) {
//            if (zone2.getSeverity() == FireSeverity.MODERATE || zone2.getSeverity() == FireSeverity.HIGH) {
//                return false;
//            } else {
//                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
//            }
//        } else if (zone1.getSeverity() == FireSeverity.MODERATE) {
//            if (zone2.getSeverity() == FireSeverity.HIGH) {
//                return false;
//            } else if (zone2.getSeverity() == FireSeverity.LOW) {
//                return true;
//            } else {
//                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
//            }
//        } else if (zone1.getSeverity() == FireSeverity.HIGH) {
//            if (zone2.getSeverity() == FireSeverity.MODERATE || zone2.getSeverity() == FireSeverity.LOW) {
//                return true;
//            } else {
//                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
//            }
//        } else {
//            return false;
//        }
//    }

    /**
     * Returns the mission queue of the scheduler
     *
     * @return MissionQueue
     */
    public MissionQueue getMissionQueue() {
        return this.missionQueue;
    }

}