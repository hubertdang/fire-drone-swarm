public class EmptyTank implements DroneState {
    /**
     * Handles the event when a new mission is assigned.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reqServiceZone(Drone context) {
        context.requestTask();
        context.setZoneToService(null);


    }

    /**
     * Handles the event when a request to release agent is made.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reqRelAgent(Drone context) {
        context.requestTask();
        context.setZoneToService(null);

    }

    /**
     * Handles the event when a request to recall to base is made.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reqRecall(Drone context) {
        context.updateState(DroneStateID.ACCELERATING);
        context.accelerate();
    }

    /**
     * Handles the event when a drone has faulted.
     *
     * @param context The context of the drone.
     */
    @Override
    public void faultDetected(Drone context) {
        context.updateState(DroneStateID.FAULT);
        context.handleFault();
    }

    /**
     * Handles the event when the drone is out of agent.
     *
     * @param context The context of the drone.
     */
    @Override
    public void emptyTank(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone's zone to service's fire is extinguished.
     *
     * @param context The context of the drone.
     */
    @Override
    public void fireExtinguished(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone reaches the range where it can start to decelerating.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reachDecelRange(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone arrives, meaning it is within its arrival distance
     * threshold.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reachDestination(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone reaches max height.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reachMaxHeight(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone lands.
     *
     * @param context The context of the drone.
     */
    @Override
    public void landed(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }

    /**
     * Handles the event when the drone reaches top speed.
     *
     * @param context The context of the drone.
     */
    @Override
    public void reachTopSpeed(Drone context) {
        throw new IllegalStateException("Invalid event for the current state.");
    }
}
