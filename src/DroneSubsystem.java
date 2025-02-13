import java.util.HashMap;
import java.util.Map;

public class DroneSubsystem implements Runnable {
    private final Map<Integer, Drone> dronesMap;
    private final DroneBuffer droneBuffer;


    public DroneSubsystem(DroneBuffer droneBuffer) {
        this.droneBuffer = droneBuffer;
        this.dronesMap = new HashMap<>();
    }

    /**
     * Adds a new drone to the subsystem.
     *
     * @param drone The drone to be added.
     */
    public void addDrone(Drone drone) {
        dronesMap.put(drone.getId(), drone);
    }

    /**
     * Retrieves the Info of a specific drone by its ID and sends it to the shared buffer.
     *
     * @param droneID The ID of the drone whose info needs to pass to DroneBuffer.
     */
    public void sendDroneInfo(int droneID) {
        Drone drone = dronesMap.get(droneID);
        DroneInfo info = new DroneInfo(drone.getId(), drone.getPosition(), drone.getAgentTankAmount());
        droneBuffer.addDroneInfo(info);
        System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem has sent drone info" + info + " to droneBuffer");
    }

    /**
     * Processes incoming tasks from the scheduler.
     */
    public void processSchedulerTasks() {
        while (droneBuffer.hasDroneTask()) {
            DroneTask task = droneBuffer.popSchedulerTask();
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Received task " + task.getTaskType());
            Drone drone = dronesMap.get(1);             // currently only have Drone id:1
            dispatchTaskToDrone(drone, task);
        }
    }

    /**
     * Assigns a task to a specific drone.
     *
     * @param drone The drone to which the task should be assigned.
     * @param task  The task to be executed.
     */
    private void dispatchTaskToDrone(Drone drone, DroneTask task) {
        switch (task.getTaskType()) {
            case SERVICE_ZONE:
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Dispatching SERVICE_ZONE task to drone #" + drone.getId());
                drone.setZoneToService(task.getZone());
                drone.setCurrentTask(task);
                break;
            case RELEASE_AGENT:
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Dispatching RELEASE_AGENT task to drone #" + drone.getId());
                drone.setCurrentTask(task);
                break;
            case STOP_AGENT:
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Dispatching STOP_AGENT task to drone #" + drone.getId());
                drone.setCurrentTask(task);
                break;
            case RECALL:
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Dispatching RECALL task to drone #" + drone.getId());
                drone.setCurrentTask(task);
                break;
            default:
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "DroneSubsystem: Unknown task type.");
                break;
        }
    }

    public void run() {
        sendDroneInfo(1);
        while (true) {
            processSchedulerTasks();
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
