import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private final ArrayList<DroneTask> acknowledgementFromDrone;
    private final ArrayList<DroneTask> tasksFromScheduler;

    /**
     * Constructs a shared priority buffer used for message passing.
     */
    public DroneBuffer() {
        acknowledgementFromDrone = new ArrayList<>();
        tasksFromScheduler = new ArrayList<>();
    }

    /**
     * Retrieves an acknowledgement message from the buffer.
     *
     * @return a Task object
     */
    public synchronized DroneTask popDroneAcknowledgement() {
        DroneTask message = null;
        if (!acknowledgementFromDrone.isEmpty()) {
            message = acknowledgementFromDrone.remove(0);
        }
        else if (!acknowledgementFromDrone.isEmpty()) {
            message = acknowledgementFromDrone.remove(0);
        }
        notifyAll();
        return message;
    }

    /**
     * Retrieves the highest priority task from the buffer.
     *
     * @return a Task object
     */
    public synchronized DroneTask popSchedulerTask() {
        DroneTask message = null;

        if (!tasksFromScheduler.isEmpty()) {
            message = tasksFromScheduler.remove(0);
        }
        else if (!tasksFromScheduler.isEmpty()) {
            message = tasksFromScheduler.remove(0);
        }

        notifyAll();
        return message;
    }

    /**
     * Adds task message to buffer.
     *
     * @param task the message to be added to buffer
     */
    public synchronized void addDroneTask(DroneTask task) {
        tasksFromScheduler.add(task);
        notifyAll();
    }

    /**
     * Adds task acknowledgement message to buffer.
     *
     * @param task the message to be added to buffer
     */
    public synchronized void addSchedulerAcknowledgement(DroneTask task) {
        acknowledgementFromDrone.add(task);
        notifyAll();
    }

    /**
     * Disables drone thread while waiting for a new fire event to service.
     */
    public synchronized void waitForTask() {
        while (tasksFromScheduler.isEmpty() && tasksFromScheduler.isEmpty()) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * newAcknowledgement signifies if there are any additions to event list
     *
     * @return true if acknowledgementMessages not empty, false otherwise
     */
    public synchronized boolean newAcknowledgement() {
        return !acknowledgementFromDrone.isEmpty();
    }
}
