public enum DroneStateID {
    BASE,               /* in base, full tank, nothing to do */
    TAKEOFF,            /* accelerating upwards to reach top speed at max height*/
    ACCELERATING,       /* accelerating only in xy plane to reach top speed at max height */
    FLYING,             /* flying to a zone to service (does not include base) */
    DECELERATING,       /* decelerating only in xy plane to become static, but still at max height */
    LANDING,            /* decelerating downwards to reach speed 0 and land on the ground */
    ARRIVED,            /* static, at the location of the zone to service */
    RELEASING_AGENT,
    FAULT,
    IDLE,               /* no zone to service, maybe have agent left, static */
}
