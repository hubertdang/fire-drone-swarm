public interface DroneState {
    /**
     * Handles the event when a new mission is assigned.
     *
     * @param context The context of the drone.
     */
    void reqServiceZone(Drone context);

    /**
     * Handles the event when a request to release agent is made.
     *
     * @param context The context of the drone.
     */
    void reqRelAgent(Drone context);

    /**
     * Handles the event when a request to recall to base is made.
     *
     * @param context The context of the drone.
     */
    void reqRecall(Drone context);

    /**
     * Handles the event when a drone has faulted.
     *
     * @param context The context of the drone.
     */
    void handleFault(Drone context);

    /**
     * Handles the event when the drone is out of agent.
     *
     * @param context The context of the drone.
     */
    void emptyTank(Drone context);

    /**
     * Handles the event when the drone's zone to service's fire is extinguished.
     *
     * @param context The context of the drone.
     */
    void fireExtinguished(Drone context);

    /**
     * Handles the event when the drone reaches the range where it can start to decelerating.
     *
     * @param context The context of the drone.
     */
    void reachDecelRange(Drone context);

    /**
     * Handles the event when the drone arrives, meaning it is within its arrival distance
     * threshold.
     *
     * @param context The context of the drone.
     */
    void reachDestination(Drone context);

    /**
     * Handles the event when the drone reaches max height.
     *
     * @param context The context of the drone.
     */
    void reachMaxHeight(Drone context);

    /**
     * Handles the event when the drone lands.
     *
     * @param context The context of the drone.
     */
    void landed(Drone context);

    /**
     * Handles the event when the drone reaches top speed.
     *
     * @param context The context of the drone.
     */
    void reachTopSpeed(Drone context);
}
