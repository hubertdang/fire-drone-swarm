import static java.lang.Thread.sleep;

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
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 25.0f;  //25m  which means if the distance is less than 20m assume it is arrived

    private final int id;
    private final AgentTank agentTank;
    private final Position position;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService; // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private FireSeverity zoneSeverity;
    private volatile DroneStatus status;  // make sure thread will check status everytime
    private float currentSpeed = 0f;


    public Drone(int id) {
        this.id = id;
        this.position = new Position(BASE_POSITION.getX(), BASE_POSITION.getY());
        this.currentSpeed = 0f;
        this.status = DroneStatus.BASE;
        this.agentTank = new AgentTank();
        this.zoneToService = null;
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
     * Get current zoneToService
     */
    public Zone getZoneToService() {
        return this.zoneToService;
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
     * Based on Drone status, return Drone state send to Dronebuffer
     */
    public DroneState getState() {
        switch (status) {
            case BASE:
                return DroneState.BASE;
            case ENROUTE:
                return DroneState.EN_ROUTE;
            case ARRIVED:
                return DroneState.ARRIVED;
            case DROPPING_AGENT:
                return DroneState.RELEASING_AGENT;
            case IDLE:
                return DroneState.IDLE;
            default:
                return DroneState.IDLE;
        }
    }

    /**
     * Retrieves the severity of the zone to be serviced.
     *
     * @return FireSeverity enum
     */
    private FireSeverity getZoneSeverity() {
        return this.zoneSeverity;
    }


    /**
     * Executes agent release operation
     */
    public void releaseAgent() {
        setStatus(DroneStatus.DROPPING_AGENT);
        System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "💦Starting agent release.");

        long previousTime = System.nanoTime();
        long currentTime;
        float deltaTime;
        float agentToDrop;
        agentTank.openNozzle();
        while (true) {
            //the status will change if agent is empty or call stopAgent()
            if (getStatus() != DroneStatus.DROPPING_AGENT) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "💦Release agent stopped. Current status: " + getStatus());
                break;
            }

            currentTime = System.nanoTime();
            deltaTime = (currentTime - previousTime) / 1_000_000_000f; // convert to second
            previousTime = currentTime;

            if (agentTank.isEmpty()) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "Tank is empty. Stopping agent release.");
                setStatus(DroneStatus.EMPTY);
                break;
            }

            //check how much agent can drop vs how much agent left
            agentToDrop = AgentTank.AGENT_DROP_RATE * deltaTime;

            agentTank.decreaseAgent(agentToDrop);
            zoneToService.setRequiredAgentAmount(zoneToService.getRequiredAgentAmount() - agentToDrop);
            System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "💧Releasing " + agentToDrop + "L. Tank=" + agentTank.getCurrAgentAmount());

            if (zoneToService.getRequiredAgentAmount() <= 0) {
                this.setStatus(DroneStatus.FIRE_STOPPED);
                System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "🧯Fire Extinguished.");
            }
            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * The scheduler can ask drone stopAgent, it will close the agent nozzle and set agentStatus to IDLE
     */
    public void stopAgent() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "handleStopAgent() called.");
        if (status == DroneStatus.FIRE_STOPPED) {
            agentTank.closeNozzle();
            System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "Stopped releasing agent.");
            setStatus(DroneStatus.IDLE);
        }
        else {
            System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "Not currently releasing agent. No action taken.");
        }
    }

    /**
     * Incremental approach to flight. If no destination is set, do nothing.
     * If arrived, set status=ARRIVED or BASE if the destination was BASE_POSITION.
     *
     * @param destination the destination for drone to go
     */
    public void fly(Position destination) {
        setStatus(DroneStatus.ENROUTE);
        System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "Starting flight.");

        long previousTime = System.nanoTime(); //get current system time before get into while loop
        long currentTime;
        float deltaTime;  //time duration
        float distanceFromDestination;
        float stepDist;  //distance traveled each time duration
        float newX, newY;
        float angle;
        float stoppingDistance;

        while (true) {
            currentTime = System.nanoTime();
            deltaTime = (currentTime - previousTime) / 1_000_000_000f; // convert into seconds
            previousTime = currentTime;

            distanceFromDestination = position.distanceFrom(destination);

            // If arrived (close enough ), stop, and check if Drone is at base or arrived at destination
            if (distanceFromDestination < ARRIVAL_DISTANCE_THRESHOLD) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "handleFly: Arrived at dest. speed=" + currentSpeed);
                currentSpeed = 0;
                if (destination.equals(BASE_POSITION)) {
                    setStatus(DroneStatus.BASE);
                }
                else {
                    setStatus(DroneStatus.ARRIVED);
                }
                return;
            }

            stoppingDistance = (currentSpeed * currentSpeed) / (2 * LAND_DECEL_RATE); //use s=(v^2/2a) calculate stop distance, when hit this distance, start to decelerate

            //when get in stoppingDistance -> decelerate OR  if not hit TOP_SPEED -> accelerate

            if (distanceFromDestination <= stoppingDistance) {
                currentSpeed -= LAND_DECEL_RATE * deltaTime;
                if (currentSpeed < 0) {
                    currentSpeed = 0;
                }
            }
            else {
                if (currentSpeed < TOP_SPEED) {
                    currentSpeed += TAKEOFF_ACCEL_RATE * deltaTime;
                    if (currentSpeed > TOP_SPEED) {
                        currentSpeed = TOP_SPEED;
                    }
                }
            }

            // Update the new position based on distance travelled


            stepDist = currentSpeed * deltaTime;
            angle = (float) Math.atan2(destination.getY() - position.getY(), destination.getX() - position.getX());

            if (stepDist > distanceFromDestination) {
                stepDist = distanceFromDestination;
            }

            newX = position.getX() + stepDist * (float) Math.cos(angle);
            newY = position.getY() + stepDist * (float) Math.sin(angle);
            position.update(newX, newY);


            System.out.println("[" + Thread.currentThread().getName() + id + "]: " + "🛸Flying: pos=(" + newX + "," + newY + "), speed=" + currentSpeed + " m/s, distance=" + distanceFromDestination + " m");

            // sleep to minimize logs
            // threshold must be >= 20m to account for this sleep call
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

        /*while (true) {
            // wait until given a directive by scheduler
            droneBuffer.waitForTask();

            // process tasks received
            DroneTask taskToDo = droneBuffer.popSchedulerTask();

            System.out.println("[" + Thread.currentThread().getName() + "]: Drone " + this.id + " received a new task to: " + taskToDo.getDroneStatus());

            switch (taskToDo.getDroneStatus()) {
                case BASE -> {
                    this.setStatus(DroneStatus.ENROUTE);
                    fly(BASE_POSITION);
                }
                case ENROUTE -> {
                    this.setStatus(DroneStatus.ENROUTE);
                    // need to inform scheduler drone is enroute
                    // for state change ENROUTE -> ARRIVED in scheduler
                    droneBuffer.addSchedulerAcknowledgement(new DroneTask(this.getStatus()));
                    fly(taskToDo.getZone().getPosition());
                    setZoneToService(taskToDo.getZone());
                }
                case DROPPING_AGENT -> {
                    this.setStatus(DroneStatus.DROPPING_AGENT);
                    releaseAgent();
                }
                case STOP_DROPPING_AGENT -> {
                    this.stopAgent();
                }
                case STATUS_UPDATE -> {
                    break;
                }
                case IDLE -> {
                    setStatus(DroneStatus.IDLE);
                }
            }

            // tell scheduler drones current state after executing task
            droneBuffer.addSchedulerAcknowledgement(new DroneTask(this.getStatus(), zoneToService));

            // allow scheduler time to receive and compute acknowledgement
            try {
                sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/
    }
}

