import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A DroneActionsTable to store the actions of drones.
 * Stored by HashMap<DroneID, <Notify, Task>>
 */
public class DroneActionsTable {
    private final HashMap<Integer, DroneTask> actionsTable;

    /**
     * Constructor for the DroneActionsTable class. Constructs a drone actions table.
     */
    public DroneActionsTable() {
        this.actionsTable = new HashMap<>();
    }

    /**
     * Adds an action to the drone actions table.
     *
     * @param droneId the drone ID
     * @param task  the task to be added
     */
    public void addAction(int droneId, DroneTask task) {
        actionsTable.put(droneId, task);
    }

    /**
     * Retrieves the task of a specific drone by its ID.
     *
     * @param droneId the drone ID
     * @return the task of the drone
     */
    public DroneTask getAction(int droneId) {
        return actionsTable.getOrDefault(droneId, null);
    }

    /**
     * Removes an action from the drone actions table.
     *
     * @param droneId the drone ID
     */
    public void removeAction(int droneId) {
        actionsTable.remove(droneId);
    }

    /**
     * Dispatches actions over the given communication channel to drones.
     *
     * @param droneBuffer the communication channel for drones
     */
    public void dispatchActions(DroneBuffer droneBuffer) {

        Iterator<Map.Entry<Integer, DroneTask>> iterator =
                actionsTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, DroneTask> entry = iterator.next();
            droneBuffer.addDroneTask(entry.getValue());
            System.out.println("[" + Thread.currentThread().getName()
                    + "]: Scheduler has dispatched Task: " + entry.getValue().getTaskType() + " to Drone: "
                    + entry.getKey());
            iterator.remove();

        }
    }
}
