import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Drone implements Runnable {
    public static final float BASE_X = 0.0f;
    public static final float BASE_Y = 0.0f;
    public static final float ARRIVAL_DISTANCE_THRESHOLD = 10.0f;           // m
    private static final float TOP_SPEED = 20.0f;                           // m/s
    private static final float ACCEL_RATE = 3.0f;                           // m/s^2
    private static final float DECEL_RATE = -5.0f;                          // m/s^2
    private static final float CRUISE_ALTITUDE = 50.0f;                     // arbitrary choice for demo
    private static final float VERTICAL_SPEED = 5.0f;                       // m/s upward/downward

    private final int id;
    private final AgentTank agentTank;
    private final Position position;
    private final Map<DroneStateID, DroneState> states;
    private DroneState currState;
    private float currSpeed;
    private float currAltitude;
    private float decelDistance;
    private final DroneManager droneManager;

    /* fields accessed by other threads */
    private volatile DroneStateID currStateID;
    private volatile DroneTask currTask;
    private volatile boolean newTaskFlag;
    private volatile Position destination;
    private volatile Zone zoneToService;

    public Drone(int id, DroneManager droneManager) {
        this.id = id;
        position = new Position(BASE_X, BASE_Y);
        currSpeed = 0f;
        currAltitude = 0f;
        agentTank = new AgentTank();
        zoneToService = null;
        states = new HashMap<>();
        this.droneManager = droneManager;

        addState(DroneStateID.BASE, new Base());
        addState(DroneStateID.TAKEOFF, new Takeoff());
        addState(DroneStateID.ACCELERATING, new Accelerating());
        addState(DroneStateID.FLYING, new Flying());
        addState(DroneStateID.DECELERATING, new Decelerating());
        addState(DroneStateID.ARRIVED, new Arrived());
        addState(DroneStateID.RELEASING_AGENT, new ReleasingAgent());
        addState(DroneStateID.IDLE, new Idle());
        addState(DroneStateID.LANDING, new Landing());

        updateState(DroneStateID.BASE);
    }

    /**
     * Executes the drone's main loop.
     */
    @Override
    public void run() {
        while (true) {
            if (newTaskFlag) {
                newTaskFlag = false;
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "Drone has received an new task: " + currTask.getTaskType());
                switch (currTask.getTaskType()) {
                    case DroneTaskType.SERVICE_ZONE:
                        eventReqServiceZone();
                        break;
                    case DroneTaskType.RELEASE_AGENT:
                        eventReqRelAgent();
                        break;
                    case DroneTaskType.RECALL:
                        eventReqRecall();
                        break;
                }
            }
            try {
                sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Adds a state to the drone.
     *
     * @param stateID The enum ID of the state.
     * @param state   The state to be added.
     */
    private void addState(DroneStateID stateID, DroneState state) {
        states.put(stateID, state);
    }

    /**
     * Updates the current state of the drone.
     *
     * @param stateID The enum ID of the state.
     */
    public void updateState(DroneStateID stateID) {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "State change | " + currStateID + " -> " + stateID);
        currState = states.get(stateID);
        currStateID = stateID;
    }

    /**
     * Gets the ID of the drone's current state.
     *
     * @return The ID of the drone's current state.
     */
    public synchronized DroneStateID getCurrStateID() {
        return currStateID;
    }

    /**
     * Sets the "new task" flag.
     */
    public synchronized void setNewTaskFlag() {
        newTaskFlag = true;
    }

    /**
     * Refills the drone's agent tank.
     */
    public void refillAgentTank() {
        agentTank.refill();
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
    public synchronized Position getDestination() {
        return destination;
    }

    /**
     * Set the position the drone will fly to
     *
     * @param position the drone will fly to
     */
    public synchronized void setDestination(Position position) {
        this.destination = position;
    }

    /**
     * Returns the distance required for the drone to decelerate to zero speed.
     *
     * @return the current deceleration distance
     */
    private float getDecelDistance() {
        return this.decelDistance;
    }

    /**
     * Calculates and sets the deceleration distance based on the drone's
     * current speed and the land deceleration rate.
     */
    private void setDecelDistance() {
        // d = v² / 2a
        this.decelDistance = (float) (Math.pow(this.currSpeed, 2) / (2 * (-1) * DECEL_RATE));
    }

    /**
     * Returns the distance from the current position to the drone's destination.
     *
     * @return the distance to the destination
     */
    public float getDistanceFromDestination() {
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
     * @return agentTank object
     */
    public synchronized float getAgentTankAmount() {
        return this.agentTank.getCurrAgentAmount();
    }

    /**
     * @param task that sent by DroneBuffer
     */
    public synchronized void setCurrTask(DroneTask task) {
        this.currTask = task;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Drone) && ((Drone) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "[Drone#" + id + ", pos=(" + position.getX() + "," + position.getY() + ")]";
    }

    /**
     * Sends the drone's info to the scheduler.
     */
    public void sendDroneInfo() {
        droneManager.sendDroneInfo(id);
    }

    /* ------------------------------ EVENT TRIGGERS ------------------------------ */

    /**
     * Triggers the event of being requested to service a zone in the current state.
     */
    public void eventReqServiceZone() {
        currState.reqServiceZone(this);
    }

    /**
     * Triggers the event of reaching max height in the current state.
     */
    public void eventReachMaxHeight() {
        currState.reachMaxHeight(this);
    }

    /**
     * Triggers the event of reaching top speed in the current state.
     */
    public void eventReachTopSpeed() {
        currState.reachTopSpeed(this);
    }

    /**
     * Triggers the event of reaching deceleration range in the current state.
     */
    public void eventReachDecelRange() {
        currState.reachDecelRange(this);
    }

    /**
     * Triggers the event of arriving at its destination in the current state.
     */
    public void eventReachDestination() {
        currState.reachDestination(this);
    }

    /**
     * Triggers the event of being requested to release agent in the current state.
     */
    public void eventReqRelAgent() {
        currState.reqRelAgent(this);
    }

    /**
     * Triggers the event of the drone's zone to service's fire extinguishing in the current state.
     */
    public void eventFireExtinguished() {
        currState.fireExtinguished(this);
    }

    /**
     * Triggers the event of being requested to recall in the current state.
     */
    public void eventReqRecall() {
        currState.reqRecall(this);
    }

    /**
     * Triggers the event of landing the drone.
     */
    public void eventLanded() {
        currState.landed(this);
    }

    /* ------------------------------ AGENT CONTROL ------------------------------ */

    /**
     * Executes agent release operation
     */
    public void releaseAgent() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "💦Starting agent release.");

        long previousTime = System.nanoTime();
        long currentTime;
        float deltaTime;
        float agentToDrop;

        agentTank.openNozzle();

        while (agentTank.isNozzleOpen()) {
            currentTime = System.nanoTime();
            deltaTime = (currentTime - previousTime) / 1_000_000_000f; // convert to second
            previousTime = currentTime;

            // check how much agent can drop vs how much agent left
            agentToDrop = AgentTank.AGENT_DROP_RATE * deltaTime;

            agentTank.decreaseAgent(agentToDrop);
            zoneToService.setRequiredAgentAmount(zoneToService.getRequiredAgentAmount()
                    - agentToDrop);
            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "💧Releasing " + agentToDrop + "L. Tank=" + agentTank.getCurrAgentAmount());

            if (zoneToService.getRequiredAgentAmount() <= 0) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "🧯Fire Extinguished.");
                eventFireExtinguished();
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
     * Stops releasing agent.
     */
    public void stopAgent() {
        agentTank.closeNozzle();
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "💦Stopped releasing agent.");
    }

    /* ------------------------------ FLYING MECHANISMS ------------------------------ */

    /**
     * Raises the drone from ground level to the designated cruise altitude.
     * This only affects the z plane.
     */
    public void takeoff() {
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Taking off..."
                + "| ALTITUDE = " + this.currAltitude + "m");

        long previousTime = System.nanoTime();
        while (this.currAltitude < CRUISE_ALTITUDE) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // Increase altitude at a constant vertical speed
            this.currAltitude += VERTICAL_SPEED * deltaTime;

            // Clamp altitude so we do not overshoot
            if (this.currAltitude > CRUISE_ALTITUDE) {
                this.currAltitude = CRUISE_ALTITUDE;
            }

            System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                    + "Climbing... "
                    + "| ALTITUDE = " + this.currAltitude + "m");

            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Takeoff complete. "
                + "| ALTITUDE = " + this.currAltitude + "m");
        eventReachMaxHeight();
    }

    /**
     * Accelerates the drone from its current speed until it reaches top speed
     * or the required deceleration distance.
     * This only affects the x & y plane.
     */
    public void accelerate() {
        System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                + "Start Accelerating... "
                + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);

        float distance, initialVelocity;
        long previousTime = System.nanoTime();

        while (true) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            this.setDecelDistance();

            // 1. If we're already within the deceleration distance, don't try to reach max speed.
            if (this.getDistanceFromDestination() <= this.getDecelDistance()) {
                System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                        + "Reached deceleration distance, cannot reach max speed. Stopping acceleration. "
                        + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);
                break;
            }

            // 2. We haven't reached top speed yet, so accelerate. v = vᵢ +at
            initialVelocity = this.currSpeed;
            this.currSpeed += ACCEL_RATE * deltaTime;
            System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                    + "Accelerating... "
                    + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);

            // 3. If this acceleration pushes us to or beyond top speed, cap it and break.
            if (this.currSpeed >= TOP_SPEED) {
                this.currSpeed = TOP_SPEED;
                System.out.println("[" + Thread.currentThread().getName() + this.id + "]: "
                        + "Reached Max Speed. Stopping acceleration. "
                        + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);
                break;
            }

            // d = Vᵢt + 0.5at²
            distance = (float) ((initialVelocity * deltaTime)
                    + (0.5 * ACCEL_RATE * Math.pow(deltaTime, 2)));
            this.updatePosition(distance);

            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        eventReachTopSpeed();
    }

    /**
     * Maintains forward movement at the current speed until the drone is ready to decelerate.
     * This only affects the x & y plane.
     */
    public void fly() {
        long previousTime = System.nanoTime();
        long currentTime;
        float deltaTime;

        while (true) {
            currentTime = System.nanoTime();
            deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "Flying... "
                    + " | POSITION = " + this.position);

            // If we're at or within the deceleration distance, exit the loop
            if (this.getDistanceFromDestination() <= this.getDecelDistance()) {
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "Reached deceleration distance. Ending flight. "
                        + " | POSITION = " + this.position);
                break;
            }

            float distance = currSpeed * deltaTime;
            this.updatePosition(distance);

            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        eventReachDecelRange();
    }

    /**
     * Gradually reduces the drone's speed as it approaches the destination, eventually stopping.
     * This only affects the x & y plane.
     */
    public void decelerate() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Starting deceleration... "
                + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);

        float initialVelocity;
        long previousTime = System.nanoTime();

        while (true) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // 1. If we're basically at the destination, stop.
            if (this.getDistanceFromDestination() < ARRIVAL_DISTANCE_THRESHOLD) {
                currSpeed = 0;
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "At the destination. Ending deceleration. "
                        + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);
                break;
            }

            // 2. We haven't reached destination yet, so decelerate. v = vᵢ +at
            initialVelocity = currSpeed;
            currSpeed += DECEL_RATE * deltaTime;
            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "Decelerating ..."
                    + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);

            // 3. If we've reached zero speed, there's no further deceleration to do.
            if (currSpeed <= 0) {
                currSpeed = 0;
                System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                        + "Have completely decelerated and stopped."
                        + "| SPEED = " + this.currSpeed + " | POSITION = " + this.position);
                break;
            }

            // d = Vᵢt + 0.5at²
            float distance = (float) ((initialVelocity * deltaTime)
                    + (0.5 * DECEL_RATE * Math.pow(deltaTime, 2)));
            this.updatePosition(distance);

            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        eventReachDestination();
    }

    /**
     * Lowers the drone altitude until it completes the landing process.
     * This only affects the z plane.
     */
    public void land() {
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Begin Landing..."
                + "| ALTITUDE = " + currAltitude + "m");

        long previousTime = System.nanoTime();

        while (currAltitude > 0f) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
            previousTime = currentTime;

            // Increase altitude at a constant vertical speed
            currAltitude -= VERTICAL_SPEED * deltaTime;

            // Clamp altitude so we do not overshoot
            if (currAltitude <= 0f) {
                currAltitude = 0f;
            }

            System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                    + "Descending ... "
                    + "| ALTITUDE = " + currAltitude + "m");

            // sleep thread to allow other threads to run/ not flood logs
            try {
                sleep(1000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("[" + Thread.currentThread().getName() + id + "]: "
                + "Landing complete. "
                + "| ALTITUDE = " + currAltitude + "m");
        eventLanded();
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
}

