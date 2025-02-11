public class Arrived implements DroneState {
    /**
     * Handles the event when a new mission is assigned.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean acceptMission(Drone context) {
        return false;
    }

    /**
     * Handles the event when a request to release agent is made.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean releaseAgent(Drone context) {
        context.setCurrState(DroneStateID.RELEASING_AGENT);
        context.releaseAgent();
        return true;
    }

    /**
     * Handles the event when a request to recall to base is made.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean recall(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone is out of agent.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean emptyTank(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone's zone to service's fire is extinguished.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean fireExtinguished(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone reaches the range where it can start to decelerating.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean reachDecelRange(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone arrives, meaning it is within its arrival distance
     * threshold.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean arrived(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone reaches max height.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean reachMaxHeight(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone lands.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean landed(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone reaches top speed.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean reachTopSpeed(Drone context) {
        return false;
    }

    /**
     * Handles the event when the drone is halted (speed = 0).
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    @Override
    public boolean halted(Drone context) {
        return false;
    }
}
