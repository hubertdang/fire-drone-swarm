import static java.lang.Thread.sleep;

public class Scheduler implements Runnable {
    private static final Position BASE = new Position(0, 0);
    private final MissionQueue missionQueue;
    private DroneBuffer droneBuffer;
    private FireIncidentBuffer fireBuffer;

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
        while(true) {

            // check fireBuffer for new SimEvent messages
            if (fireBuffer.newEvent()) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a new event.\n\t" +
                        "Requesting a drone to fly to fire.");
                SimEvent fireToService = fireBuffer.popEventMessage();
                Task dispatchDroneTask = new Task(DroneStatus.ENROUTE, fireToService);
                droneBuffer.addDroneTask(dispatchDroneTask);
            }

            // check to see if the drones status has updated
            if (droneBuffer.newAcknowledgement()) {
                Task acknowledgementStatus = droneBuffer.popDroneAcknowledgement();
                System.out.println("[" + Thread.currentThread().getName() + "]: Scheduler " +
                        "a drone has sent back an acknowledgement\n\t" +
                        "Status: " + acknowledgementStatus.getDroneStatus());

                switch (acknowledgementStatus.getDroneStatus()) {
                    case ARRIVED -> {
                        System.out.println("[" + Thread.currentThread().getName() + "]: Scheduler " +
                                "requesting drone to drop agent.");
                        droneBuffer.addDroneTask(new Task(DroneStatus.DROPPING_AGENT));
                    }
                    case EMPTY -> {
                        System.out.println("[" + Thread.currentThread().getName() + "]: Scheduler " +
                                "requesting drone to fly to base to resupply agent.");
                        droneBuffer.addDroneTask(new Task(DroneStatus.BASE));
                    }
                    case FIRE_STOPPED -> {
                        System.out.println("[" + Thread.currentThread().getName() + "]: Scheduler" +
                                "requesting drone to enter idle state.");
                        droneBuffer.addDroneTask(new Task(DroneStatus.IDLE));

                        // relay information to Fire subsystem...this is not correct
                        // pls suggest how else the fire subsystem can detect change and tell
                        // drone to stop
                        fireBuffer.addAcknowledgementMessage("Fire has been put out.");
                    }
                }
            }





            // give other threads oppurtunity to access shared buffers
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Adds a zone to mission queue based on fire severity and amount of agent needed.
     * Dispatches a drone to the new zone if the new zone has a higher priority than the
     * zone being currently serviced by the drone.
     *
     * @param zone
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
}