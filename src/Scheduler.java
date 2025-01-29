public class Scheduler implements Runnable {
    private Drone drone;
    private MissionQueue missionQueue;

    public Scheduler() {
        missionQueue = new MissionQueue();
    }

    public void addDrone(Drone drone) {
        this.drone = drone;
    }

    @Override
    public void run() {
    }

    public void handleFireReq(Zone zone) {
        missionQueue.queue(zone);
    }

    public void droneStatusUpdated(DroneStatus status) {
        if (status == DroneStatus.ARRIVED){
            drone.releaseAgent();
        }
        else if (status == DroneStatus.BASE) {
            //drone.recover();
        }
        else {}
    }

    public void zoneSeverityUpdated(Zone zone) {
        if (zone.getSeverity() == FireSeverity.NO_FIRE){
            drone.stopAgent();
            drone.fly(new Position(0 ,0));
        }
    }

    public void dispatch(Drone drone, Zone zone) {
        if (drone.getZoneToService() == null || comparePriority(drone.getZoneToService(), zone)){
            drone.setZoneToService(missionQueue.pop());
            drone.fly(zone.getPosition());
        }
    }

    public boolean comparePriority(Zone zone1, Zone zone2) {
       return true;
    }
}