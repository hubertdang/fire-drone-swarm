import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private Map<Integer, ArrayList<Task>> acknowledgementFromDrone;
    private Map<Integer, ArrayList<Task>> tasksFromScheduler;

    /**
     * Constructs a shared priority buffer used for message passing.
     */
    public DroneBuffer() {
        acknowledgementFromDrone = new HashMap<>();
        tasksFromScheduler = new HashMap<>();

        // initializes priority array lists
        for (int i = 0 ; i < 2 ; i++) {
            acknowledgementFromDrone.put(i, new ArrayList<>());
            tasksFromScheduler.put(i, new ArrayList<>());
        }
    }

    /**
     * Retrieves an acknowledgement message from the buffer.
     * @return a Task object
     */
    public synchronized Task popDroneAcknowledgement() {
        Task message = null;
        if (!acknowledgementFromDrone.get(0).isEmpty()) {
            message = acknowledgementFromDrone.get(0).remove(0);
        } else if (!acknowledgementFromDrone.get(1).isEmpty()) {
            message = acknowledgementFromDrone.get(1).remove(0);
        }
        notifyAll();
        return message;
    }

    /**
     * Retrieves the highest priority task from the buffer.
     * @return a Task object
     */
    public synchronized Task popSchedulerTask() {
        Task message = null;

        if (!tasksFromScheduler.get(0).isEmpty()) {
            message = tasksFromScheduler.get(0).remove(0);
        } else if (!tasksFromScheduler.get(1).isEmpty()) {
            message = tasksFromScheduler.get(1).remove(0);
        }

        notifyAll();
        return message;
    }

    /**
     * Adds task message to buffer.
     * @param task the message to be added to buffer
     */
    public synchronized void addDroneTask(Task task) {
        tasksFromScheduler.get(task.getTaskPriority()).add(task);
        notifyAll();
    }

    /**
     * Adds task acknowledgement message to buffer.
     * @param task the message to be added to buffer
     */
    public synchronized void addSchedulerAcknowledgement(Task task) {
        acknowledgementFromDrone.get(task.getTaskPriority()).add(task);
        notifyAll();
    }

    /**
     * Disables drone thread while waiting for a new fire event to service.
     */
    public synchronized void waitForTask() {
        while (tasksFromScheduler.get(0).isEmpty() && tasksFromScheduler.get(1).isEmpty()) {
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
        return !acknowledgementFromDrone.get(0).isEmpty() ||
                !acknowledgementFromDrone.get(1).isEmpty();
    }
}
