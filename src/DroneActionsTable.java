import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    /**
     * Dispatches actions over the given communication channel to drones.
     *
     * @param droneBuffer the communication channel for drones
     */
    public void dispatchActions(DroneBuffer droneBuffer, Missions missions) {

        Iterator<Map.Entry<Integer, SchedulerSubState>> iterator =
                actionsTable.entrySet().iterator();

//        System.out.println(actionsTable.get(missions.getMissions()));
        while (iterator.hasNext()) {

            Map.Entry<Integer, SchedulerSubState> entry = iterator.next();
            if (entry.getValue().shouldNotify()) {
                DroneTaskType task = entry.getValue().execute();
                if (task == null) {
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: A Scheduler SubState cannot process unexpected drone context.");
                } else {
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler sending new task to drone#" + entry.getKey());
                    droneBuffer.addDroneTask(new DroneTask(entry.getKey(), task, entry.getValue().getZone()));
                }
                entry.getValue().resetNotify(); // resets message send boolean

                // remove drone from actions table & zone from missions if dispatched task is a RECALL
                if (task == DroneTaskType.RECALL) {
                    System.out.println("[" + Thread.currentThread().getName()
                            + "]: Scheduler removing drone#" + entry.getKey()
                            + " from DroneActionsTable, tasks complete.");
                    iterator.remove();

                    //missions.remove(entry.getValue().getZone());
                }
            }
        }
    }


}
