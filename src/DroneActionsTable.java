import java.util.HashMap;

/**
 * A DroneActionsTable to store the actions of drones.
 * Stored by DroneID|SchedulerSubState
 */
public class DroneActionsTable {
    private final HashMap<Integer, SchedulerSubState> actionsTable;

    /**
     * Constructor for the DroneActionsTable class. Constructs a drone actions table.
     */
    public DroneActionsTable() {
        this.actionsTable = new HashMap<Integer, SchedulerSubState>();
    }

    /**
     * Adds an action to the drone actions table.
     *
     * @param droneId the drone ID
     * @param action  the action to be added
     */
    public void addAction(int droneId, SchedulerSubState action) {
        actionsTable.put(droneId, action);
    }

    /**
     * Retrieves the action of a specific drone by its ID.
     *
     * @param droneId the drone ID
     * @return the substate of the drone
     */
    public SchedulerSubState getAction(int droneId) {
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
     * Updates the action of a specific drone.
     *
     * @param droneId the drone ID
     * @param action  the action to be updated
     */
    public void updateAction(int droneId, SchedulerSubState action) {
        actionsTable.put(droneId, action);
    }


}
