public class Scheduler implements Runnable {
    private static final Position BASE = new Position(0, 0);
    private final Drone drone;
    private final MissionQueue missionQueue;

    /**
     * Constructs a scheduler for the system.
     * Initialises the drone and mission queue.
     */
    public Scheduler() {

        missionQueue = new MissionQueue();
        drone = new Drone(1, this);
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
        if (drone.getZoneToService() == null || comparePriority(zone, drone.getZoneToService())) {
            dispatch(drone, zone);
        }
    }

    /**
     * Checks the drone status and issues commands to the drone
     *
     * @param status
     */
    public void droneStatusUpdated(DroneStatus status) {
        if (status == DroneStatus.ARRIVED) {
            drone.releaseAgent();
        } else if (status == DroneStatus.BASE) {
            //drone.recover(); - method/flag does not exist in the drone class yet
        } else {
        }
    }

    /**
     * Updates the fire severity of a zone
     *
     * @param zone
     */
    public void zoneSeverityUpdated(Zone zone) {
        if (zone.getSeverity() == FireSeverity.NO_FIRE) {
            drone.stopAgent();
            drone.fly(BASE);
        }
    }

    /**
     * Dispatches a drone to a zone in the mission queue
     *
     * @param drone
     * @param zone
     */
    public void dispatch(Drone drone, Zone zone) {
        drone.setZoneToService(missionQueue.pop());
        drone.fly(zone.getPosition());
    }

    /**
     * Compares priority of 2 zones - takes into account fire severity and amount of agent needed
     *
     * @param zone1
     * @param zone2
     * @return true if zone1 has higher priority than zone2, false otherwise
     */
    public boolean comparePriority(Zone zone1, Zone zone2) {
        if (zone1.getSeverity() == FireSeverity.LOW) {
            if (zone2.getSeverity() == FireSeverity.MODERATE || zone2.getSeverity() == FireSeverity.HIGH) {
                return false;
            } else {
                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
            }
        } else if (zone1.getSeverity() == FireSeverity.MODERATE) {
            if (zone2.getSeverity() == FireSeverity.HIGH) {
                return false;
            } else if (zone2.getSeverity() == FireSeverity.LOW) {
                return true;
            } else {
                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
            }
        } else if (zone1.getSeverity() == FireSeverity.HIGH) {
            if (zone2.getSeverity() == FireSeverity.MODERATE || zone2.getSeverity() == FireSeverity.LOW) {
                return true;
            } else {
                return (zone1.getRequiredAgentAmount() > zone2.getRequiredAgentAmount());
            }
        } else {
            return false;
        }
    }

    public void setFireInfo(Position position, FireSeverity severity, long eventTime) {
    }
}