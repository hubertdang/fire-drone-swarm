/**
 * Task Class
 * A class in the fire drone swarm system designed to aid in the generation
 * of messages passed between the scheduler and the drone.
 */

public class DroneTask {
    private final int droneID;
    private final DroneTaskType taskType;
    private final Zone zone;

    /**
     * Task constructor 1 takes a desired task, and sets the event to a default value;
     * to convey message between scheduler and drone.
     *
     * @param droneTaskType the status message to be interpreted by a Drone.
     */
    public DroneTask(int droneID, DroneTaskType droneTaskType) {
        this.droneID = droneID;
        this.taskType = droneTaskType;
        this.zone = null;
    }

    /**
     * Task constructor 2 takes a desired task, and Zone to convey message between scheduler and drone
     *
     * @param zone          the Zone to service.
     * @param droneTaskType the status message to be interpreted by a Drone.
     */
    public DroneTask(int droneID, DroneTaskType droneTaskType, Zone zone) {
        this.droneID = droneID;
        this.taskType = droneTaskType;
        this.zone = zone;
    }

    /**
     * Gets the ID of the drone that this task is assigned to.
     *
     * @return The ID of the drone that this task is assigned to.
     */
    public int getDroneID() {
        return droneID;
    }

    /**
     * getDroneStatus
     *
     * @return a status enum representing the desired state scheduler wants the drone to be.
     */
    public DroneTaskType getDroneTaskType() {
        return this.taskType;
    }

    /**
     * getZone
     *
     * @return the zone to service.
     */
    public Zone getZone() {
        return this.zone;
    }


    /**
     * get taskType
     *
     * @return the taskType for droneSubSystem.
     */
    public DroneTaskType getTaskType() {
        return taskType;
    }

}
