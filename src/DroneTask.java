/**
 * Task Class
 * A class in the fire drone swarm system designed to aid in the generation
 * of messages passed between the scheduler and the drone.
 */

public class DroneTask {

    private final DroneStatus droneStatus;
    private final Zone zone;

    /**
     * Task constructor 1 takes a desired task, and sets the event to a default value;
     * to convey message between scheduler and drone.
     *
     * @param droneStatus the status message to be interpreted by a Drone.
     */
    public DroneTask(DroneStatus droneStatus) {
        this.droneStatus = droneStatus;
        this.zone = null;
    }

    /**
     * Task constructor 2 takes a desired task, and Zone to convey message between scheduler and drone
     *
     * @param zone        the Zone to service.
     * @param droneStatus the status message to be interpreted by a Drone.
     */
    public DroneTask(DroneStatus droneStatus, Zone zone) {
        this.droneStatus = droneStatus;
        this.zone = zone;
    }

    /**
     * getDroneStatus
     *
     * @return a status enum representing the desired state scheduler wants the drone to be.
     */
    public DroneStatus getDroneStatus() {
        return this.droneStatus;
    }

    /**
     * getZone
     *
     * @return the zone to service.
     */
    public Zone getZone() {
        return this.zone;
    }

}
