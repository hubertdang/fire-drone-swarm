public enum DroneState {
    BASE,               /* in base, full tank, nothing to do */
    EN_ROUTE,           /* flying to a zone to service (does not include base) */
    ARRIVED,            /* static, at the location of the zone to service */
    RELEASING_AGENT,
    IDLE,               /* no zone to service, maybe have agent left, static */
    RECALLING           /* otw back to base */
}
