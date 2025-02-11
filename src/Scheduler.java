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
        DroneState currentDroneState = DroneState.BASE;
        DroneState previousDroneState = null;
        boolean droneOnMission = false;

        while (true) {

            // check fireBuffer for new messages, add to mission queue
            if (fireBuffer.newEvent()) {
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "Scheduler has received a new event.\n\t" + "adding to mission queue.");
                Zone zoneToService = fireBuffer.popEventMessage();
                handleFireReq(zoneToService);
            }

            // check for drone acknowledgements
            if (droneBuffer.hasDroneInfo()) {
                DroneInfo droneInfo = droneBuffer.popDroneInfo();
                DroneState newState = droneInfo.getDroneState();
                System.out.println("[" + Thread.currentThread().getName() + "]: Scheduler " + "drone#" + droneInfo.droneID + " has sent back an new Info\n\t" + "State: " + newState + " Position: (" + droneInfo.getPosition().getX() + "," + droneInfo.getPosition().getY() + ")" + " AgentLeft: " + droneInfo.getAgentTankAmount());

                previousDroneState = currentDroneState;
                currentDroneState = newState;

                System.out.println("DEBUG: Previous State: " + previousDroneState);
                System.out.println("DEBUG: Current State: " + currentDroneState);

                switch (currentDroneState) {
                    case ARRIVED -> {
                        // drop agent, to location
                        if (previousDroneState == DroneState.EN_ROUTE) {
                            droneBuffer.addDroneTask(new DroneTask(DroneTaskType.RELEASE_AGENT));
                        }
                    }

                    case IDLE -> {
                        droneOnMission = false; // open to receiving new missions
                        if (previousDroneState == DroneState.RELEASING_AGENT) {
                            // indicate fire has been put out to the fire incident subsystem
                            //Zone servicedZone = acknowledgementState.getZone();
                            //  servicedZone.setSeverity(FireSeverity.NO_FIRE); // where should this code be?
                            // fireBuffer.addAcknowledgementMessage(servicedZone);
                        }
                    }
                }
            }

            // handle missions from the mission queue
            if (!missionQueue.isEmpty() && ((currentDroneState == DroneState.IDLE) || (currentDroneState == DroneState.BASE)) && !droneOnMission) {
                Zone missionZone = missionQueue.pop();
                DroneTask newMission = new DroneTask(DroneTaskType.SERVICE_ZONE, missionZone);
                droneBuffer.addDroneTask(newMission);
                droneOnMission = true;
            }

            // give other threads oppurtunity to access shared buffers
            try {
                sleep(2000);
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