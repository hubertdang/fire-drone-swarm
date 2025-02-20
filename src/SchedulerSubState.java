public interface SchedulerSubState {
    // Executes the substate
    DroneTaskType execute();

    // Tells scheduler if it should transition this drone
    boolean shouldNotify();

    // Allow scheduler to set notify flag
    void setNotify(DroneStateID droneStateID);

    // Allow scheduler to reset notify flag
    void resetNotify();

    // Returns the zone this substate is associated with
    Zone getZone();
}
