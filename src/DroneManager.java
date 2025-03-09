import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DroneManager implements Runnable {
    private final Map<Integer, Drone> dronesMap;
    private final DroneBuffer droneBuffer;

    public DroneManager(DroneBuffer droneBuffer) {
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
    public synchronized void sendDroneInfo(int droneID) {
        Drone drone = dronesMap.get(droneID);
        DroneInfo info = new DroneInfo(drone.getId(), drone.getCurrStateID(), drone.getPosition(), drone.getAgentTankAmount(), drone.getZoneToService());
        droneBuffer.addDroneInfo(info);
        System.out.println("[" + Thread.currentThread().getName() + droneID + "]: "
                + "has sent drone info " + info + " to droneBuffer");
    }

    /**
     * Retrieves the Info of all drones and sends it to the shared buffer.
     */
    public synchronized void sendAllDroneInfo() {
        ArrayList<DroneInfo> infoList = new ArrayList<>();
        for (Integer droneID : dronesMap.keySet()) {
            Drone drone = dronesMap.get(droneID);
            infoList.add(new DroneInfo(drone.getId(), drone.getCurrStateID(), drone.getPosition(), drone.getAgentTankAmount(), drone.getZoneToService()));
        }
        droneBuffer.addDroneInfo(infoList);
        System.out.println("[" + Thread.currentThread().getName() + "]: "
                + "has sent all drone info to droneBuffer");
    }

    /**
     * Processes incoming tasks from the scheduler.
     */
    public void processSchedulerTasks() {
        while (droneBuffer.hasDroneTask()) {
            DroneTask task = droneBuffer.popDroneTask();
            String zoneId = (task.getZone() != null) ? task.getZone().getId() + "" : "null";
            if (task.getTaskType() != DroneTaskType.REQUEST_ALL_INFO) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Received task " + task.getTaskType() + " @ zone#" + zoneId + " for drone#"
                        + task.getDroneID());
                dispatchTaskToDrone(dronesMap.get(task.getDroneID()), task);
            } else {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Received task " + task.getTaskType());
                sendAllDroneInfo();
            }

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
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching SERVICE_ZONE task to drone#"
                        + drone.getId());
                drone.setDestination(task.getZone().getPosition());
                drone.setZoneToService(task.getZone());
                drone.setCurrTask(task);
                break;
            case RELEASE_AGENT:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching RELEASE_AGENT task to drone#"
                        + drone.getId());
                drone.setCurrTask(task);
                break;
            case RECALL:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching RECALL task to drone#"
                        + drone.getId());
                drone.setDestination(new Position(Drone.BASE_X, Drone.BASE_Y));
                drone.setCurrTask(task);
                break;
            default:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Unknown task type.");
                break;
        }
        drone.setNewTaskFlag();
    }

    public void run() {
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
