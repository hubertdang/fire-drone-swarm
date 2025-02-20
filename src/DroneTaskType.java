public enum DroneTaskType {
    SERVICE_ZONE,            /* which zone indicated in DroneTask object */
    RELEASE_AGENT,
    REROUTE,                 /* stop releasing agent if the drone is, and service new zone */
    RECALL,                  /* fly back to base, no need to pass a zone */
    REQUEST_ALL_INFO
}
