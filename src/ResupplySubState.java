/**
 * The ResupplySubState represents the resupplying of a drone which is stop dropping agent,
 * fly to base and resupply.
 */
public class ResupplySubState implements SchedulerSubState {
    private boolean notify;

    /**
     * Constructor for the ResupplySubState
     * @param notify Whether the scheduler should notify the drone
     */
    public ResupplySubState(boolean notify) {
        this.notify = notify;
    }
    /**
     * Executes the resupply substate
     * @param context The drone to execute the substate on
     */
    @Override
    public void execute(DroneState context) {
        // Do the resupply
        System.out.println("Executing Resupply");
    }

    /**
     * Returns whether the scheduler should make the state transition
     * @return boolean the notify flag
     */
    @Override
    public boolean shouldNotify() {
        return notify;
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
     * @return null since there is no zone
     */
    @Override
    public Zone getZone() {
        return null;
    }
}
