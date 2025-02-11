import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private final ArrayList<DroneInfo> droneInfoMessages;
    private final ArrayList<DroneTask> tasksFromScheduler;

    /**
     * Constructs a shared priority buffer used for message passing.
     */
    public DroneBuffer() {
        droneInfoMessages = new ArrayList<>();
        tasksFromScheduler = new ArrayList<>();
    }


    /**
     * Retrieves an DroneInformation message from the buffer.
     *
     * @return a DroneInfo
     */
    public synchronized DroneInfo popDroneInfo() {
        DroneInfo message = null;
        if (!droneInfoMessages.isEmpty()) {
            message = droneInfoMessages.remove(0);
            System.out.println("A DroneInfo has been removed from the buffer and send to Scheduler");
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
            System.out.println("A SchedulerTask has been removed from the buffer and send to DroneSubsystem");
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
     * Adds Drone information  to buffer.
     *
     * @param info the Drone information to be added to buffer
     */
    public synchronized void addDroneInfo(DroneInfo info) {
        droneInfoMessages.add(info);
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
     * check if there is any Drone Information from DroneSubsystem updated to DroneBuffer and ready to send to Scheduler
     *
     * @return true if droneStateMessage is not empty, false otherwise
     */
    public synchronized boolean hasDroneInfo() {
        return !droneInfoMessages.isEmpty();
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
