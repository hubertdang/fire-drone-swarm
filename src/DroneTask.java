/**
 * Task Class
 * A class in the fire drone swarm system designed to aid in the generation
 * of messages passed between the scheduler and the drone.
 */

public class DroneTask {

    private final DroneTaskType droneTaskType;
    private final Zone zone;

    /**
     * Task constructor 1 takes a desired task, and sets the event to a default value;
     * to convey message between scheduler and drone.
     *
     * @param droneTaskType the status message to be interpreted by a Drone.
     */
    public DroneTask(DroneTaskType droneTaskType) {
        this.droneTaskType = droneTaskType;
        this.zone = null;
    }

    /**
     * Task constructor 2 takes a desired task, and Zone to convey message between scheduler and drone
     *
     * @param zone        the Zone to service.
     * @param droneTaskType the status message to be interpreted by a Drone.
     */
    public DroneTask(DroneTaskType droneTaskType, Zone zone) {
        this.droneTaskType = droneTaskType;
        this.zone = zone;
    }

    /**
     * getDroneStatus
     *
     * @return a status enum representing the desired state scheduler wants the drone to be.
     */
    public DroneTaskType getDroneStatus() {
        return this.droneTaskType;
    }

    /**
     * getZone
     *
     * @return the zone to service.
     */
    public Zone getZone() {
        return this.zone;
    }

    public DroneTaskType getTaskType() {
        return droneTaskType;
    }

}
