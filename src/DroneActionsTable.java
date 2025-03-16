import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

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
     * @param task    the task to be added
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
     * @param messagePasser the message passer to send the actions
     */
    public synchronized void dispatchActions(MessagePasser messagePasser, int droneID) {

        Iterator<Map.Entry<Integer, DroneTask>> iterator =
                actionsTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, DroneTask> entry = iterator.next();

            if (Objects.equals(Thread.currentThread().getName(), "📅FEH")) {
                // If it's the FireEventHandler thread, send to DroneController port
                messagePasser.send(entry.getValue(), "localhost", 6000 + entry.getKey());
                System.out.println("[" + Thread.currentThread().getName()
                        + "]: Scheduler has dispatched Task: " + entry.getValue().getTaskType() + " to DroneController: "
                        + entry.getKey() + " for Zone " + entry.getValue().getZone());
            }
            else {
                if (entry.getKey() == droneID) {
                    // If the droneID is specified, send the task to that drone only.
                    messagePasser.send(entry.getValue(), "localhost", 5000 + entry.getKey());
                    if (entry.getValue().getTaskType() == DroneTaskType.SERVICE_ZONE) {
                        // For SERVICE_ZONE tasks, send the task to both the Drone and the DroneController.
                        // This unblocks the Drone and dispatches the zone service to the DroneController.
                        messagePasser.send(entry.getValue(), "localhost", 6000 + entry.getKey());
                    }
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler has dispatched Task: " + entry.getValue().getTaskType() + " to Drone: "
                            + entry.getKey() + " for Zone: " + entry.getValue().getZone());
                }
            }
            iterator.remove();
        }
    }
}
