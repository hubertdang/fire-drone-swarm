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
    private Position position;
    private final Scheduler scheduler;
    private final AgentTank agentTank;
    //flags for scheduler to change
    private boolean flyCmdFlag = false;
    private boolean relAgentCmdFlag = false;
    private boolean stopAgentCmdFlag = false;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService;       // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private DroneStatus status;
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
     * @return Position object containing current coordinates
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the id of this drone
     * @return drone ID number
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the current amount of agent in the tank
     * @return current agent quantity in liters
     */
    public float getTankCapacity() {
        return agentTank.getCurrAgentAmount();
    }

    /**
     * Gets the currently assigned zone
     * @return Zone object representing current destination, could be null
     */
    public Zone getZoneToService() {
        return zoneToService;
    }

    /**
     * Assigns a new zone to this drone
     * @param zone Target zone for firefighting operations.
     */
    public void setZoneToService(Zone zone) {
        this.zoneToService = zone;
    }

    /**
     * Gets the current operational status of the drone
     * @return DroneStatus enum value indicating current state
     */
    public DroneStatus getStatus() {
        return status;
    }

    /**
     * Sets the  status of the drone
     * @param status New DroneStatus to set
     */
    public void setStatus(DroneStatus status) {
        this.status = status;
    }



    /**
     * Executes agent release operation
     *
     * @param deltaTime Time elapsed since last update (unit in seconds)
     */
    public void releaseAgent(float deltaTime) {
        // Open the nozzle if it's not opened
        if (!agentTank.isNozzleOpen()) {
            agentTank.openNozzle();
            setStatus(DroneStatus.DROPPING_AGENT);
            scheduler.droneStatusUpdated(getStatus());
            System.out.println("[Drone#" + id + "] Starting agent release...");
        }

        if (agentTank.isEmpty()){
            setStatus(DroneStatus.EMPTY);
            scheduler.droneStatusUpdated(getStatus());
        }

        float theoreticalDropAmount = AgentTank.getAgentDropRate() * deltaTime;
        float agentToDrop= Math.min(agentTank.getCurrAgentAmount(), theoreticalDropAmount);
        // Check if release should continue
        if (!agentTank.isEmpty() && zoneToService.getSeverity()!= FireSeverity.NO_FIRE) {
            agentTank.decreaseAgent(agentToDrop);
            zoneToService.setRequiredAgentAmount(zoneToService.getRequiredAgentAmount()-agentToDrop);

            System.out.println("[Drone#" + id + "] Releasing " + agentToDrop + "L. Tank="
                    + getTankCapacity() + ", zoneNeed=" + zoneToService.getRequiredAgentAmount());
        }
    }

    /**
     * The scheduler can set flag to let drone stopAgent, it will close the agent nozzle
     */
    public void stopAgent() {
        System.out.println("[Drone#" + id + "] handleStopAgent() called.");
        // if currently releasing agent, close nozzle
        if (agentTank.isNozzleOpen()) {
            agentTank.closeNozzle();
            System.out.println("[Drone#" + id + "] Stopped releasing agent.");
        }
    }

    /**
     * Incremental approach to flight. If no destination is set, do nothing.
     * If arrived, set status=ARRIVED or BASE if the destination was BASE_POSITION.
     *
     * @param deltaTime is passed in run, by using systemTime in run()
     *
     */
    private void fly(float deltaTime) {

        if (zoneToService == null) {
            flyCmdFlag = false;
            System.out.println("[Drone#" + id + "] No zone assigned. Cannot fly.");
            return;
        }
        Position destination = zoneToService.getPosition();
        float distance = position.distanceFrom(destination);
        float stoppingDistance = (currentSpeed * currentSpeed) / (2 * LAND_DECEL_RATE);

        // If arrived (close enough + speed near 0), stop
        if (distance < ARRIVAL_DISTANCE_THRESHOLD ) {
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

        float stepDist = currentSpeed * deltaTime;
        float dx = destination.getX() - position.getX();
        float dy = destination.getY() - position.getY();
        float angle = (float) Math.atan2(dy, dx);

        if (stepDist > distance) {
            stepDist = distance;
        }

        float newX = position.getX() + stepDist * (float) Math.cos(angle);
        float newY = position.getY() + stepDist * (float) Math.sin(angle);
        position.update(newX, newY);

        System.out.println("[Drone#" + id + "] Flying: pos=(" + newX + "," + newY + "), speed=" + currentSpeed + ", dist=" + distance);
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
        System.out.println("[Drone#" + id + "] Thread started. Initial status: " + status);

        long previousTime = System.nanoTime(); // Use nanoseconds
        while (true) {
            long currentTime = System.nanoTime();

            float deltaTime = (currentTime - previousTime) / 1_000_000_000f; // Convert ns to seconds
            previousTime = currentTime; // Update for next iteration

        }

    }

}

