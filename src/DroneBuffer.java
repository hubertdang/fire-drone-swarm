import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private final ArrayList<DroneInfo> droneInfos;
    private final ArrayList<DroneTask> droneTasks;

    /**
     * Constructs a shared priority buffer used for message passing.
     */
    public DroneBuffer() {
        droneInfos = new ArrayList<>();
        droneTasks = new ArrayList<>();
    }


    /**
     * Removes and returns a DroneInfo message from the buffer.
     *
     * @return DroneInfo object from droneInfos buffer if not empty, otherwise, returns null
     */
    public synchronized DroneInfo popDroneInfo() {
        DroneInfo message = null;
        if (!droneInfos.isEmpty()) {
            message = droneInfos.remove(0);
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

        if (!droneTasks.isEmpty()) {
            message = droneTasks.remove(0);
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
        droneTasks.add(task);
        notifyAll();
    }

    /**
     * Adds Drone information  to buffer.
     *
     * @param info the Drone information to be added to buffer
     */
    public synchronized void addDroneInfo(DroneInfo info) {
        droneInfos.add(info);
        notifyAll();
    }

    /**
     * Disables drone thread while waiting for a new fire event to service.
     */
    public synchronized void waitForTask() {
        while (droneTasks.isEmpty()) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * check if there is any Drone Information from DroneSubsystem updated to DroneBuffer and ready to send to Scheduler
     *
     * @return true if droneStateMessage is not empty, false otherwise
     */
    public synchronized boolean hasDroneInfo() {
        return !droneInfos.isEmpty();
    }

    /**
     * new Tasks signifies if there are any additions to event list
     *
     * @return true if buffer has tasks from scheduler, false otherwise
     */
    public boolean hasDroneTask() {
        return !droneTasks.isEmpty();
    }
}
