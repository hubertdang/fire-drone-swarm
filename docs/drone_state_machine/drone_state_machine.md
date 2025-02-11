# Drone State Machine

## Events

### External
* Scheduler gives a new mission (DroneTaskType.SERVICE_ZONE)
* Scheduler commands the drone to start releasing agent (DroneTaskType.RELEASE_AGENT)
* Scheduler commands the drone to recall (DroneTaskType.RECALL)

### Internal
* Drone itself sees that its agent tank is empty
* Drone sees that the fire is extinguished
* Drone has arrived to its destination (zone to service or base)
* Drone has reached max height
* Drone has reached max speed
* Drone has reached 0 speed
* Drone has landed