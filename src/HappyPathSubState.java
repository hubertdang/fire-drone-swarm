/**
 * The HappyPathSubState represents the happy path of a drone which is fly to zone, drop agent
 * and stop dropping agent.
 */
public class HappyPathSubState implements SchedulerSubState {
    private final Zone zone;
    private boolean notify;

    /**
     * Constructor for the HappyPathSubState
     * @param zone The zone the drone is flying to
     * @param notify Whether the drone should notify the scheduler
     */
    public HappyPathSubState(Zone zone, boolean notify) {
        this.zone = zone;
        this.notify = notify;
    }

    /**
     * Returns whether the scheduler should make the state transition
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
     * @return Zone
     */
    @Override
    public Zone getZone() {
        return zone;
    }
}
