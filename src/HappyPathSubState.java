/**
 * The HappyPathSubState represents the happy path of a drone which is fly to zone, drop agent
 * and stop dropping agent.
 */
public class HappyPathSubState implements SchedulerSubState {
    private final Zone zone;
    private boolean notify;
    DroneStateID droneStateID;

    /**
     * Constructor for the HappyPathSubState
     *
     * @param zone   The zone the drone is flying to
     * @param notify Whether the scheduler should notify the drone
     */
    public HappyPathSubState(Zone zone, boolean notify) {
        this.zone = zone;
        this.notify = notify;
        this.droneStateID = DroneStateID.UNDEFINED;
    }

    /**
     * Executes the happypath substate
     */
    @Override
    public DroneTaskType execute() {
        DroneTaskType task = null;
        switch (droneStateID) {
            case DroneStateID.UNDEFINED:
                task = DroneTaskType.SERVICE_ZONE;
                break;
            case DroneStateID.ARRIVED:
                task = DroneTaskType.RELEASE_AGENT;
                break;
            case DroneStateID.IDLE:
                task = DroneTaskType.RECALL;
                break;
            default:
                break;
        }
        return task;
    }

    /**
     * Returns whether the scheduler should make the state transition
     *
     * @return boolean
     */
    @Override
    public boolean shouldNotify() { return notify; }

    /**
     * Sets notify flag enabling state transition
     */
    @Override
    public void setNotify(DroneStateID droneStateID) {
        this.notify = true;
        this.droneStateID = droneStateID;
    }

    /**
     * Resets the notify flag
     */
    @Override
    public void resetNotify() {
        this.notify = false;
    }

    /**
     * Returns the zone associated with this substate
     *
     * @return Zone
     */
    @Override
    public Zone getZone() {
        return zone;
    }
}
