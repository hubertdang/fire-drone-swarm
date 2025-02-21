public enum DroneTaskType {
    SERVICE_ZONE,            /* which zone indicated in DroneTask object */
    RELEASE_AGENT,
    RECALL,                  /* fly back to base, no need to pass a zone */
    REQUEST_ALL_INFO
}
