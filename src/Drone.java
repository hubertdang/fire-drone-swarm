/**
 * <p>Drone</p>
 *
 * @author Shenhao Gong
 * @version 2025-Jan-29th
 */

public class Drone implements Runnable {
    private static final Position BASE_POSITION = new Position(0, 0);
    private static final float TOP_SPEED = 20.0f;     // 20m/s
    private static final float TAKEOFF_ACCEL_RATE = 3.0f;  //3m/s^2
    private static final float LAND_DECEL_RATE = 5.0f;   //5m/s^2
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 1f;  //1m  which means if the distance is less than 20m assume it is arrived
    private final int id;
    private final Scheduler scheduler;
    private final AgentTank agentTank;
    private final Position position;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService;       // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private volatile DroneStatus status;  // make sure thread will check status everytime
    private float currentSpeed = 0f;


    public Drone(int id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
        this.position = new Position(BASE_POSITION.getX(), BASE_POSITION.getY());
        this.currentSpeed = 0f;
        this.status = DroneStatus.BASE;
        this.agentTank = new AgentTank();
    }


    /**
     * return current position of the drone
     *
     * @return Position object containing current coordinates
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the id of this drone
     *
     * @return drone ID number
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the current amount of agent in the tank
     *
     * @return current agent quantity in liters
     */
    public float getTankCapacity() {
        return agentTank.getCurrAgentAmount();
    }

    /**
     * Gets the currently assigned zone
     *
     * @return Zone object representing current destination, could be null
     */
    public Zone getZoneToService() {
        return zoneToService;
    }

    /**
     * Assigns a new zone to this drone
     *
     * @param zone Target zone for firefighting operations.
     */
    public void setZoneToService(Zone zone) {
        this.zoneToService = zone;
    }

    /**
     * Gets the current operational status of the drone
     *
     * @return DroneStatus enum value indicating current state
     */
    public synchronized DroneStatus getStatus() {
        return status;
    }

    /**
     * Sets the  status of the drone
     *
     * @param status New DroneStatus to set
     */
    public synchronized void setStatus(DroneStatus status) {
        this.status = status;
    }


    /**
     * make a new thread while do releaseAgent, because scheduler may call stopAgent
     */
    public void releaseAgent() {
        new Thread(this::releaseAgentLoop).start();
    }

    /**
     * Executes agent release operation
     */
    public void releaseAgentLoop() {

        setStatus(DroneStatus.DROPPING_AGENT);
        scheduler.droneStatusUpdated(getStatus());
        System.out.println("[Drone#" + id + "] Starting agent release.");

        long previousTime = System.nanoTime();

        while (true) {
            //the status will change if agent is empty or call stopAgent()
            if (getStatus() != DroneStatus.DROPPING_AGENT) {
                System.out.println("[Drone#" + id + "] Release agent stopped. Current status: " + getStatus());
                break;
            }


            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f; // convert to second
            previousTime = currentTime;

            if (agentTank.isEmpty()) {
                System.out.println("[Drone#" + id + "] Tank is empty. Stopping agent release.");
                setStatus(DroneStatus.EMPTY);
                break;
            }

            //check how much agent can drop vs how much agent left
            float agentToDrop = Math.min(agentTank.getCurrAgentAmount(), AgentTank.AGENT_DROP_RATE * deltaTime);

            agentTank.decreaseAgent(agentToDrop);
            zoneToService.setRequiredAgentAmount(zoneToService.getRequiredAgentAmount() - agentToDrop);

            System.out.println("[Drone#" + id + "] Releasing " + agentToDrop + "L. Tank=" + getTankCapacity() + ", zoneNeed=" + zoneToService.getRequiredAgentAmount());
        }
    }

    /**
     * The scheduler can ask drone stopAgent, it will close the agent nozzle and set agentStatus to IDLE
     */
    public void stopAgent() {
        System.out.println("[Drone#" + id + "] handleStopAgent() called.");
        synchronized (this) {
            if (status == DroneStatus.DROPPING_AGENT) {
                agentTank.closeNozzle();
                System.out.println("[Drone#" + id + "] Stopped releasing agent.");
                setStatus(DroneStatus.IDLE);
            } else {
                System.out.println("[Drone#" + id + "] Not currently releasing agent. No action taken.");
            }
        }
    }

    /**
     * Incremental approach to flight. If no destination is set, do nothing.
     * If arrived, set status=ARRIVED or BASE if the destination was BASE_POSITION.
     *
     * @param destination the destination for drone to go
     */
    private void fly(Position destination) {
        setStatus(DroneStatus.ENROUTE);
        System.out.println("[Drone#" + id + "] Starting flight.");

        long previousTime = System.nanoTime(); //get current system time before get into while loop

        while (true) {

            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f; // convert into seconds
            previousTime = currentTime;

            float distance;
            synchronized (this) {
                distance = position.distanceFrom(destination); //destination may change by scheduler
            }

            // If arrived (close enough + speed near 0), stop, and check if Drone is at base or arrived at destination
            if (distance < ARRIVAL_DISTANCE_THRESHOLD) {
                System.out.println("[Drone#" + id + "] handleFly: Arrived at dest. speed=" + currentSpeed);
                currentSpeed = 0;
                if (destination.equals(BASE_POSITION)) {
                    setStatus(DroneStatus.BASE);
                    scheduler.droneStatusUpdated(getStatus());
                } else {
                    setStatus(DroneStatus.ARRIVED);
                    scheduler.droneStatusUpdated(getStatus());
                }
                return;
            }

            float stoppingDistance = (currentSpeed * currentSpeed) / (2 * LAND_DECEL_RATE); //use s=(v^2/2a) calculate stop distance, when hit this distance, start to decelerate

            //when get in stoppingDistance -> decelerate OR  if not hit TOP_SPEED -> accelerate
            synchronized (this) {
                if (distance <= stoppingDistance) {
                    currentSpeed -= LAND_DECEL_RATE * deltaTime;
                    if (currentSpeed < 0) {
                        currentSpeed = 0;
                    }
                } else {
                    if (currentSpeed < TOP_SPEED) {
                        currentSpeed += TAKEOFF_ACCEL_RATE * deltaTime;
                        if (currentSpeed > TOP_SPEED) {
                            currentSpeed = TOP_SPEED;
                        }
                    }
                }
            }

            // Update the new position by calculating the distance traveled, calculate angle using Math.tan(), and get the new x and y coordinates with sin() and cos()
            float stepDist;
            float newX, newY;
            synchronized (this) {
                stepDist = currentSpeed * deltaTime;
                float dx = destination.getX() - position.getX();
                float dy = destination.getY() - position.getY();
                float angle = (float) Math.atan2(dy, dx);

                if (stepDist > distance) {
                    stepDist = distance;
                }

                newX = position.getX() + stepDist * (float) Math.cos(angle);
                newY = position.getY() + stepDist * (float) Math.sin(angle);
                position.update(newX, newY);
            }

            System.out.println("[Drone#" + id + "] Flying: pos=(" + newX + "," + newY + "), speed=" + currentSpeed + " m/s, distance=" + distance + " m");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Drone) && ((Drone) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "[Drone#" + id + ", status=" + status + ", pos=(" + position.getX() + "," + position.getY() + ")]";
    }

    /**
     * Run the simulation
     */
    @Override
    public void run() {

    }


}

