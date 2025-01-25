public class Scheduler implements Runnable {
    private Drone drone;
    private MissionQueue missionQueue;

    public Scheduler() {
    }

    public void addDrone(Drone drone) {
    }

    @Override
    public void run() {
    }

    public void handleFireReq(Zone zone) {
    }

    public void droneStatusUpdated(DroneStatus status) {
    }

    public void zoneSeverityUpdated(Zone zone) {
    }

    public void dispatch(Drone drone, Zone zone) {
    }

    public boolean comparePriority(Zone zone1, Zone zone2) {
        return false;
    }
}