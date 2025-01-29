public class Scheduler implements Runnable {
    private Drone drone;
    private MissionQueue missionQueue;

    /**
     * Constructs a scheduler for the system
     *
     */
    public Scheduler() {
        missionQueue = new MissionQueue();
    }

    /**
     * Adds a drone to the system
     *
     * @param drone
     */
    public void addDrone(Drone drone) {
        this.drone = drone;
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
     * Adds a zone to mission queue based on fire severity and amount of agent needed
     *
     * @param zone
     */
    public void handleFireReq(Zone zone) {
        missionQueue.queue(zone);
    }

    /**
     * Checks the drone status and issues commands to the drone
     *
     * @param status
     */
    public void droneStatusUpdated(DroneStatus status) {
        if (status == DroneStatus.ARRIVED){
            drone.releaseAgent();
        }
        else if (status == DroneStatus.BASE) {
            //drone.recover(); - method/flag does not exist in the drone class yet
        }
        else {}
    }

    /**
     * Updates the fire severity of a zone
     *
     * @param zone
     */
    public void zoneSeverityUpdated(Zone zone) {
        if (zone.getSeverity() == FireSeverity.NO_FIRE){
            drone.stopAgent();
            drone.fly(new Position(0 ,0));
        }
    }

    /**
     * Dispatches a drone to a zone in the mission queue
     *
     * @param drone
     * @param zone
     */
    public void dispatch(Drone drone, Zone zone) {
        if (drone.getZoneToService() == null || comparePriority(drone.getZoneToService(), zone)){
            drone.setZoneToService(missionQueue.pop());
            drone.fly(zone.getPosition());
        }
    }

    /**
     * Compares priority of 2 zones - takes into account fire severity and amount of agent needed
     *
     * @param zone1
     * @param zone2
     * @return true if zone1 has higher priority than zone2, false otherwise
     */
    public boolean comparePriority(Zone zone1, Zone zone2) {
       return true;
    }
}