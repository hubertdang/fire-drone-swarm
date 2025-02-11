public interface DroneState {
    /**
     * Handles the event when a new mission is assigned.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reqServiceZone(Drone context);

    /**
     * Handles the event when a request to release agent is made.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reqRelAgent(Drone context);

    /**
     * Handles the event when a request to recall to base is made.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reqRecall(Drone context);

    /**
     * Handles the event when the drone is out of agent.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean emptyTank(Drone context);

    /**
     * Handles the event when the drone's zone to service's fire is extinguished.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean fireExtinguished(Drone context);

    /**
     * Handles the event when the drone reaches the range where it can start to decelerating.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reachDecelRange(Drone context);

    /**
     * Handles the event when the drone arrives, meaning it is within its arrival distance
     * threshold.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean arrived(Drone context);

    /**
     * Handles the event when the drone reaches max height.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reachMaxHeight(Drone context);

    /**
     * Handles the event when the drone lands.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean landed(Drone context);

    /**
     * Handles the event when the drone reaches top speed.
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean reachTopSpeed(Drone context);

    /**
     * Handles the event when the drone is halted (speed = 0).
     *
     * @param context The context of the drone.
     * @return true if the event was valid, false otherwise.
     */
    boolean halted(Drone context);
}
