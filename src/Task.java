/**
 * Task Class
 * A class in the fire drone swarm system designed to aid in the generation
 * of messages passed between the scheduler and the drone.
 */

public class Task {

    private DroneStatus droneStatus;
    private SimEvent event;

    private int taskPriority;

    /**
     * Task constructor 1 takes a desired task, and sets the event to a default value;
     * to convey message between scheduler and drone.
     * @param droneStatus the status message to be interpreted by a Drone.
     * @param taskPriority the priority of task being sent on the buffer.
     *                     Priority 0 -> Indicates synchronous message passing
     *                     Priority 1 -> Indicates asynchronous message passing
     */
    public Task(DroneStatus droneStatus, int taskPriority) {
        this.droneStatus = droneStatus;
        this.event = null;
        this.taskPriority = taskPriority;
    }

    /**
     * Task constructor 2 takes a desired task, and Zone to convey message between scheduler and drone
     * @param event the Zone to service.
     * @param droneStatus the status message to be interpreted by a Drone.
     * @param taskPriority the priority of task being sent on the buffer.
     */
    public Task(DroneStatus droneStatus, SimEvent event, int taskPriority) {
        this.droneStatus = droneStatus;
        this.event = event;
        this.taskPriority = taskPriority;
    }

    /**
     * getDroneStatus
     * @return a status enum representing the desired state scheduler wants the drone to be.
     */
    public DroneStatus getDroneStatus() { return this.droneStatus; }

    /**
     * getEvent
     * @return the event to service.
     */
    public SimEvent getEvent() { return this.event; }

    /**
     * getTaskPriority
     * @return the priority of task to be completed
     */
    public int getTaskPriority() { return this.taskPriority; }
}
