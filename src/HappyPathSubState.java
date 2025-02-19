/**
 * The HappyPathSubState represents the happy path of a drone which is fly to zone, drop agent
 * and stop dropping agent.
 */
public class HappyPathSubState implements SchedulerSubState {
    private final Zone zone;
    private boolean notify;

    /**
     * Constructor for the HappyPathSubState
     *
     * @param zone   The zone the drone is flying to
     * @param notify Whether the scheduler should notify the drone
     */
    public HappyPathSubState(Zone zone, boolean notify) {
        this.zone = zone;
        this.notify = notify;
    }

    /**
     * Executes the happypath substate
     *
     * @param context The drone to execute the substate on
     */
    @Override
    public void execute(DroneState context) {
        // Do the happy path
        System.out.println("Executing Happy Path");
    }

    /**
     * Returns whether the scheduler should make the state transition
     *
     * @return boolean
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
     * Returns the zone associated with this substate
     *
     * @return Zone
     */
    @Override
    public Zone getZone() {
        return zone;
    }
}
