import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private ArrayList<Task> acknowledgementFromDrone;
    private ArrayList<Task> tasksFromScheduler;

    /**
     * Constructs a shared buffer used for message passing.
     */
    public DroneBuffer() {
        acknowledgementFromDrone = new ArrayList<>();
        tasksFromScheduler = new ArrayList<>();
    }

    /**
     * Retrieves an acknowledgement message from the buffer.
     * @return a Task object
     */
    public synchronized Task popDroneAcknowledgement() {
        Task message = this.acknowledgementFromDrone.remove(0);
        notifyAll();
        return message;
    }

    /**
     * Retrieves a task message from the buffer.
     * @return a Task object
     */
    public synchronized Task popSchedulerTask() {
        Task message = this.tasksFromScheduler.remove(0);
        notifyAll();
        return message;
    }

    /**
     * Adds task message to buffer.
     * @param task the message to be added to buffer
     */
    public synchronized void addDroneTask(Task task) {
        tasksFromScheduler.add(task);
        notifyAll();
    }

    /**
     * Adds task acknowledgement message to buffer.
     * @param task the message to be added to buffer
     */
    public synchronized void addSchedulerAcknowledgement(Task task) {
        acknowledgementFromDrone.add(task);
        notifyAll();
    }

    /**
     * Disables drone thread while waiting for a new fire event to service.
     */
    public synchronized void waitForTask() {
        while (tasksFromScheduler.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * newAcknowledgement signifies if there are any additions to event list
     * @return true if acknowledgementMessages not empty, false otherwise
     */
    public synchronized boolean newAcknowledgement() {
        return !acknowledgementFromDrone.isEmpty();
    }
}
