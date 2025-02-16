public interface SchedulerSubState {

    // Tells scheduler if it should transition this drone
    boolean shouldNotify();

    // Allow scheduler to reset notify flag
    void resetNotify();

    // Returns the zone this substate is associated with
    Zone getZone();
}
