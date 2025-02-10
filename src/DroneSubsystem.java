import java.util.ArrayList;
import java.util.List;

public class DroneSubsystem implements Runnable {
    private final List<Drone> drones;
    private final DroneBuffer droneBuffer;


    public DroneSubsystem(DroneBuffer droneBuffer) {
        this.droneBuffer = droneBuffer;
        this.drones = new ArrayList<Drone>();
    }

    /**
     * Adds a new drone to the subsystem.
     *
     * @param drone The drone to be added.
     */
    public void addDrone(Drone drone) {
        drones.add(drone);
    }

    /**
     * Retrieves the state of a specific drone by its ID and sends it to the shared buffer.
     *
     * @param droneID The ID of the drone whose state needs to be updated in the buffer.
     */
    public void sendDroneStatesToBuffer(int droneID) {
        for (Drone drone : drones) {
            if (drone.getId() == droneID) {
                DroneState state = drone.getState();
                droneBuffer.addDroneState(state);
                System.out.println("DroneSubsystem: Sent drone state " + state + " to buffer.");
                break;
            }
        }
    }

    /**
     * Processes incoming tasks from the scheduler
     */
    public void processSchedulerTasks() {
        while (droneBuffer.hasSchedulerTask()) {
            DroneTask task = droneBuffer.popSchedulerTask();
            if (task != null) {
                System.out.println("DroneSubsystem: Received task " + task.getTaskType());

                Drone drone = drones.get(0); //change this while have more drones join in
                dispatchTaskToDrone(drone, task);
            }
        }
    }

    /**
     * Dispatches a given task to a specific drone
     *
     * @param drone The drone to which the task should be assigned.
     * @param task  The task to be executed.
     */
    private void dispatchTaskToDrone(Drone drone, DroneTask task) {
        switch (task.getTaskType()) {
            case SERVICE_ZONE:
                if (task.getZone() != null) {
                    System.out.println("DroneSubsystem: Dispatching SERVICE_ZONE task to drone " + drone.getId());
                    drone.setZoneToService(task.getZone());
                    drone.fly(task.getZone().getPosition());   //mistake here, still busy waiting, shouldn't let DroneSubsystem run it
                }
                else {
                    System.out.println("DroneSubsystem: SERVICE_ZONE task missing Zone information.");
                }
                break;
            case RELEASE_AGENT:
                System.out.println("DroneSubsystem: Dispatching RELEASE_AGENT task to drone " + drone.getId());
                drone.releaseAgent();    //mistake here, still busy waiting, shouldn't let DroneSubsystem run it
                break;
            case STOP_AGENT:
                System.out.println("DroneSubsystem: Dispatching STOP_AGENT task to drone " + drone.getId());
                drone.stopAgent();
                break;
            case RECALL:
                System.out.println("DroneSubsystem: Dispatching RECALL task to drone " + drone.getId());

                drone.fly(new Position(0, 0));
                break;
            default:
                System.out.println("DroneSubsystem: Unknown task type.");
                break;
        }
    }

    public void run() {
        System.out.println("DroneSubsystem thread started.");
        while (true) {

            processSchedulerTasks();

            sendDroneStatesToBuffer(1);


            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("DroneSubsystem thread terminated.");
    }
}
