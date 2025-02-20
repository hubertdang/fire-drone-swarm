/**
 * The ResupplySubState represents the resupplying of a drone which is stop dropping agent,
 * fly to base and resupply.
 */
public class ResupplySubState implements SchedulerSubState {
    private boolean notify;
    DroneStateID droneStateID;

    /**
     * Constructor for the ResupplySubState
     *
     * @param notify Whether the scheduler should notify the drone
     */
    public ResupplySubState(boolean notify) {
        this.droneStateID = DroneStateID.UNDEFINED;
        this.notify = notify;
    }

    /**
     * Executes the resupply substate
     */
    @Override
    public DroneTaskType execute() { return DroneTaskType.RECALL; }

    /**
     * Returns whether the scheduler should make the state transition
     *
     * @return boolean the notify flag
     */
    @Override
    public boolean shouldNotify() { return notify; }

    /**
     * Sets notify flag enabling state transition
     */
    @Override
    public void setNotify(DroneStateID droneStateID) {
        this.droneStateID = droneStateID;
        this.notify = true;
    }

    /**
     * Resets the notify flag
     */
    @Override
    public void resetNotify() {
        this.notify = false;
    }

    /**
     * No zone since the drone is flying to base
     *
     * @return null since there is no zone
     */
    @Override
    public Zone getZone() {
        return null;
    }
}
