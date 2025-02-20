/**
 * The RerouteSubState represents the rerouting of a drone which is stop dropping agent, and then
 * do the happy path.
 */
public class RerouteSubState implements SchedulerSubState {
    private final Zone zone;
    private boolean notify;
    DroneStateID droneStateID;

    /**
     * Constructor for the RerouteSubState
     *
     * @param zone   The zone the drone is rerouting to
     * @param notify Whether the scheduler should notify the drone
     */
    public RerouteSubState(Zone zone, boolean notify) {
        this.zone = zone;
        this.notify = notify;
        this.droneStateID = DroneStateID.UNDEFINED;
    }

    /**
     * Executes the reroute substate
     */
    @Override
    public DroneTaskType execute() {
        DroneTaskType task = null;
        switch (droneStateID) {
            case DroneStateID.ARRIVED:
                task = DroneTaskType.RELEASE_AGENT;
                break;

            case DroneStateID.IDLE:
                task = DroneTaskType.RECALL;
                break;

            default:
                task = DroneTaskType.REROUTE;
                break;
        }
        return task;
    }

    /**
     * Returns whether the scheduler should make the state transition
     *
     * @return boolean the notify flag
     */
    @Override
    public boolean shouldNotify() {
        return notify;
    }

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

    }

    /**
     * Returns the zone associated with this substate
     *
     * @return Zone the zone to reroute to
     */
    @Override
    public Zone getZone() {
        return zone;
    }
}
