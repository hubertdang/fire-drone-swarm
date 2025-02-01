import java.util.ArrayList;

/**
 * DroneBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the drone.
 */

public class DroneBuffer {

    private ArrayList<Task> tasksFromDrone;
    private ArrayList<Task> tasksFromScheduler;

    public DroneBuffer() {
        tasksFromDrone = new ArrayList<>();
        tasksFromScheduler = new ArrayList<>();
        notifyAll();
    }

    public synchronized void popDroneTask() {
        this.tasksFromDrone.remove(0);
        notifyAll();
    }

    public synchronized void popSchedulerTask() {
        this.tasksFromScheduler.remove(0);
        notifyAll();
    }

    public synchronized void addDroneTask(Task task) {
        tasksFromDrone.add(task);
        notifyAll();
    }

    public synchronized void addSchedulerTask(Task task) {
        tasksFromScheduler.add(task);
        notifyAll();
    }

    public synchronized void waitForTask() {
        while (tasksFromScheduler.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        notifyAll();
    }
}
