/**
 * The RerouteSubState represents the rerouting of a drone which is stop dropping agent, and then
 * do the happy path.
 */
public class RerouteSubState implements SchedulerSubState {
    private final Zone zone;
    private final boolean notify;

    /**
     * Constructor for the RerouteSubState
     * @param zone The zone the drone is rerouting to
     * @param notify Whether the drone should notify the scheduler
     */
    public RerouteSubState(Zone zone, boolean notify) {
        this.zone = zone;
        this.notify = notify;
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

    }

    /**
     * Returns the zone associated with this substate
     * @return Zone the zone to reroute to
     */
    @Override
    public Zone getZone() {
        return zone;
    }
}
