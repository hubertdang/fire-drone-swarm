public class Drone implements Runnable {
    private static final Position BASE_POSITION = null;
    private static final float TOP_SPEED = 0;
    private static final float TAKEOFF_ACCEL_RATE = 0;
    private static final float LAND_DECEL_RATE = 0;
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 0;

    private int id;
    private Position position;
    private float rating;
    private Zone zoneToService;
    private DroneStatus status;
    private Scheduler scheduler;
    private AgentTank agentTank;

    public Drone(int id, Scheduler scheduler) {
    }

    public Position getPosition() {
        return null;
    }

    public void releaseAgent() {
    }

    public void stopAgent() {
    }

    public float getTankCapacity() {
        return 0;
    }

    public void fly(Position position){

    }

    public void setZoneToService(Zone zone) {
    }

    public Zone getZoneToService() {
        return null;
    }

    public void setStatus(DroneStatus status) {
    }

    public DroneStatus getStatus() {
        return null;
    }

    @Override
    public boolean equals(Object obj){
        return false;
    }

    @Override
    public void run() {
    }
}