import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private final ArrayList<DroneState> droneStateMessage;
    private final ArrayList<DroneTask> tasksFromScheduler;

    /**
     * Constructs a shared priority buffer used for message passing.
     */
    public DroneBuffer() {
        droneStateMessage = new ArrayList<>();
        tasksFromScheduler = new ArrayList<>();
    }


    /**
     * Retrieves an acknowledgement message from the buffer.
     *
     * @return a DroneState
     */
    public synchronized DroneState popDroneState() {
        DroneState message = null;
        if (!droneStateMessage.isEmpty()) {
            message = droneStateMessage.remove(0);
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
     * @param state the DroneState to be added to buffer
     */
    public synchronized void addDroneState(DroneState state) {
        droneStateMessage.add(state);
        notifyAll();
    }

    /**
     * Disables drone thread while waiting for a new fire event to service.
     */
    public synchronized void waitForTask() {
        while (tasksFromScheduler.isEmpty()) {
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
     * @return true if droneStateMessage is not empty, false otherwise
     */
    public synchronized boolean newAcknowledgement() {
        return !droneStateMessage.isEmpty();
    }

    /**
     * new Tasks signifies if there are any additions to event list
     *
     * @return true if buffer has tasks from scheduler, false otherwise
     */
    public boolean hasSchedulerTask() {
        return !tasksFromScheduler.isEmpty();
    }
}
