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
    private static final float ACCEL_RATE = 3.0f;  //3m/s^2
    private static final float DECEL_RATE = -5.0f;   //-5m/s^2
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 10.0f;  //25m  which means if the distance is less than 20m assume it is arrived
    private static final float CRUISE_ALTITUDE = 50.0f; // arbitrary choice for demo
    private static final float VERTICAL_SPEED = 5.0f;   // m/s upward/downward

    private final int id;
    private final AgentTank agentTank;
    private final DroneBuffer droneBuffer;
    private final Position position;
    private Position destination;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService; // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private FireSeverity zoneSeverity;
    private volatile DroneStatus status;  // make sure thread will check status everytime
    private float currentSpeed;
    private float currentAltitude;
    private float decelerationDistance;


    public Drone(int id, DroneBuffer droneBuffer) {
        this.id = id;
        this.position = new Position(BASE_POSITION.getX(), BASE_POSITION.getY());
        this.currentSpeed = 0f;
        this.currentAltitude = 0f;
        this.status = DroneStatus.BASE;
        this.agentTank = new AgentTank();
        this.droneBuffer = droneBuffer;
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
     * Return the destination the drone is flying to
     *
     * @return the destination the drone is flying to
     */
    public Position getDestination() {
        return destination;
    }

    /**
     * Set the position the drone will fly to
     *
     * @param position the drone will fly to
     */
    public void setDestination(Position position) {
        this.destination = position;
    }

    /**
     * Returns the distance required for the drone to decelerate to zero speed.
     *
     * @return the current deceleration distance
     */
    public float getDecelerationDistance(){
        return this.decelerationDistance;
    }

    /**
     * Calculates and sets the deceleration distance based on the drone's
     * current speed and the land deceleration rate.
     */
    public void setDecelerationDistance(){
        // d = v² / 2a
        this.decelerationDistance = (float) (Math.pow(this.currentSpeed, 2) / (2 * DECEL_RATE));
    }

    /**
     * Returns the distance from the current position to the drone's destination.
     *
     * @return the distance to the destination
     */
    public float getDistanceFromDestination(){
        return position.distanceFrom(this.getDestination());
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
     * Raises the drone from ground level to the designated cruise altitude.
     */
    public void takeoff() {
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Taking off..."
                + "| ALTITUDE = " + this.currentAltitude + "m");

        long previousTime = System.nanoTime();
        while (this.currentAltitude < CRUISE_ALTITUDE) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // Increase altitude at a constant vertical speed
            this.currentAltitude += VERTICAL_SPEED * deltaTime;

            // Clamp altitude so we do not overshoot
            if (this.currentAltitude > CRUISE_ALTITUDE) {
                this.currentAltitude = CRUISE_ALTITUDE;
            }

            System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                    + "Climbing... "
                    + "| ALTITUDE = " + this.currentAltitude + "m");

        }
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Takeoff complete. "
                + "| ALTITUDE = " + this.currentAltitude + "m");
    }

    /**
     * Accelerates the drone from its current speed until it reaches top speed
     * or the required deceleration distance.
     */
    public void accelerate() {
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Start Accelerating... "
                + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);

        float distance, initialVelocity;
        long previousTime = System.nanoTime();
        
        while (true) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            this.setDecelerationDistance();

            // 1. If we're already within the deceleration distance, don't try to reach max speed.
            if (this.getDistanceFromDestination() <= this.getDecelerationDistance()) {
                System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                        + "Reached deceleration distance, cannot reach max speed. Stopping acceleration. "
                        + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);
                break;
            }

            // 2. We haven't reached top speed yet, so accelerate. v = vᵢ +at
            initialVelocity = this.currentSpeed;
            this.currentSpeed += ACCEL_RATE * deltaTime;
            System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                    + "Accelerating... " 
                    + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);

            // 3. If this acceleration pushes us to or beyond top speed, cap it and break.
            if (this.currentSpeed >= TOP_SPEED) {
                this.currentSpeed = TOP_SPEED;
                System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                        + "Reached Max Speed. Stopping acceleration. "
                        + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);
                break;
            }

            // d = Vᵢt + 0.5at²
            distance = (float) ((initialVelocity * deltaTime) + (0.5 * ACCEL_RATE * Math.pow(deltaTime, 2)));
            this.updatePosition(distance);

        }
    }

    /**
     * Maintains forward movement at the current speed until the drone is ready to decelerate.
     */
    public void fly() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Flying... "
                + " | POSITION = " + this.position);

        long previousTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // If we're at or within the deceleration distance, exit the loop
            if (this.getDistanceFromDestination() <= this.getDecelerationDistance()) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "Reached deceleration distance. Ending flight. "
                        + " | POSITION = " + this.position);;
                break;
            }

            float distance = currentSpeed * deltaTime;
            this.updatePosition(distance);

        }
    }

    /**
     * Gradually reduces the drone's speed as it approaches the destination, eventually stopping.
     */
    public void decelerate() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Starting deceleration... "
                + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);

        float initialVelocity;

        long previousTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // 1. If we're basically at the destination, stop.
            if (this.getDistanceFromDestination() < ARRIVAL_DISTANCE_THRESHOLD) {
                currentSpeed = 0;
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "At the destination. Ending deceleration. "
                        + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);
                break;
            }

            // 2. We haven't reached destination yet, so decelerate. v = vᵢ +at
            initialVelocity = currentSpeed;
            currentSpeed += DECEL_RATE * deltaTime;
            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "Decelerating ..."
                    + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);

            // 3. If we've reached zero speed, there's no further deceleration to do.
            if (currentSpeed <= 0) {
                currentSpeed = 0;
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "Have completely decelerated and stopped."
                        + "| SPEED = " + this.currentSpeed + " | POSITION = " + this.position);
                break;
            }

            // d = Vᵢt + 0.5at²
            float distance = (float) ((initialVelocity * deltaTime) + (0.5 * DECEL_RATE * Math.pow(deltaTime, 2)));
            this.updatePosition(distance);
        }
    }

    /**
     * Lowers the drone altitude until it completes the landing process.
     */
    public void land () {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Begin Landing..."
                + "| ALTITUDE = " + currentAltitude + "m");

        long previousTime = System.nanoTime();
        while (currentAltitude <= 0f) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // Increase altitude at a constant vertical speed
            currentAltitude -= VERTICAL_SPEED * deltaTime;

            // Clamp altitude so we do not overshoot
            if (currentAltitude <= 0f) {
                currentAltitude = 0f;
            }

            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "Descending ... "
                    + "| ALTITUDE = " + currentAltitude + "m");

        }
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Landing complete. "
                + "| ALTITUDE = " + currentAltitude + "m");
    }

    /**
     * Updates the drone's position based on the given distance in the current flight direction.
     *
     * @param distance the distance to move the drone.
     */
    public void updatePosition(float distance) {
        float angle = (float) Math.atan2(
                this.getDestination().getY() - position.getY(),
                this.getDestination().getX() - position.getX());

        float newX = position.getX() + distance * (float) Math.cos(angle);
        float newY = position.getY() + distance * (float) Math.sin(angle);
        position.update(newX, newY);
    }

    @Override
    public boolean equals(Object obj){
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

        while (true) {
            // wait until given a directive by scheduler
            droneBuffer.waitForTask();

            // process tasks received
            DroneTask taskToDo = droneBuffer.popSchedulerTask();

            System.out.println("[" + Thread.currentThread().getName() + "]: Drone " + this.id + " received a new task to: " + taskToDo.getDroneStatus());

            switch (taskToDo.getDroneStatus()) {
                case BASE -> {
                    this.setStatus(DroneStatus.ENROUTE);
                    fly();
                }
                case ENROUTE -> {
                    this.setStatus(DroneStatus.ENROUTE);
                    // need to inform scheduler drone is enroute
                    // for state change ENROUTE -> ARRIVED in scheduler
                    droneBuffer.addSchedulerAcknowledgement(new DroneTask(this.getStatus()));
                    fly();
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
        }
    }
}

