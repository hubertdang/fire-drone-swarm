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
    private static final float NO_SPEED_FLOOR = 0.5f;  // if speed <0.5m/s assume it is 0
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 10f;  //10m  which means if the distance is less than 20m assume it is arrived
    private final int id;
    private final Position position;
    private final Scheduler scheduler;
    private final AgentTank agentTank;
    //flags for scheduler to change
    private volatile boolean flyCmdFlag = false;
    private volatile boolean relAgentCmdFlag = false;
    private volatile boolean stopAgentCmdFlag = false;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService;       // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private DroneStatus status;
    private float currentSpeed = 0f;

    private Position destination = null;
    private final boolean releasingAgent = false;  //flags for releasing agent

    public Drone(int id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
        this.position = new Position(BASE_POSITION.getX(), BASE_POSITION.getY());
        this.currentSpeed = 0f;
        this.status = DroneStatus.BASE;
        this.agentTank = new AgentTank();


    }


    public Position getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public float getTankCapacity() {
        return agentTank.getCurrAgentAmount();
    }

    public Zone getZoneToService() {
        return zoneToService;
    }

    public void setZoneToService(Zone zone) {
        this.zoneToService = zone;
    }

    public DroneStatus getStatus() {
        return status;
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
    }

    public Position getDestination() {
        return this.destination;
    }

    /**
     * The Scheduler might call this to forcibly change the drone's flight target mid-route.
     */
    public void setDestination(Position newDest) {
        this.destination = newDest;
    }

    private void autoRefillIfAtBase() {
        // If  "at" base & tank isn't full, refill
        float distToBase = position.distanceFrom(BASE_POSITION);
        if (distToBase < ARRIVAL_DISTANCE_THRESHOLD && !agentTank.isFull()) {
            System.out.println("[Drone#" + id + "] At base. Refilling agent tank...");
            agentTank.refill();
        }
    }

    /**
     *
     *
     */
    public void releaseAgent(float deltaTime) {
        // Open the nozzle if it's not opened
        if (!agentTank.isNozzleOpen()) {
            agentTank.openNozzle();
            setStatus(DroneStatus.DROPPING_AGENT);
            System.out.println("[Drone#" + id + "] Starting agent release...");
        }

        // If no zone is assigned or zone doesn't need agent, stop immediately
        if (zoneToService == null || zoneToService.getRequiredAgentAmount() <= 0 || agentTank.isEmpty()) {
            finalizeRelease();
            return;
        }

        float agentToDrop = AgentTank.getAgentDropRate() * deltaTime;
        float remainingNeed = zoneToService.getRequiredAgentAmount();
        float actualDrop = Math.min(agentToDrop, remainingNeed);  // Only release what’s needed

        // Check if release should continue
        if (!agentTank.isEmpty() && remainingNeed > 0) {
            agentTank.decreaseAgent(actualDrop);
            zoneToService.setRequiredAgentAmount(Math.max(0, remainingNeed - actualDrop));

            System.out.println("[Drone#" + id + "] Releasing " + actualDrop + "L. Tank="
                    + getTankCapacity() + ", zoneNeed=" + zoneToService.getRequiredAgentAmount());
        }

        // Stop releasing if tank is empty or zone no longer needs agent
        if (agentTank.isEmpty() || zoneToService.getRequiredAgentAmount() <= 0) {
            finalizeRelease();
        }
    }

    private void finalizeRelease() {
        agentTank.closeNozzle();
        relAgentCmdFlag = false;
        if (agentTank.isEmpty()) {
            setStatus(DroneStatus.EMPTY);
        } else {
            System.out.println("[Drone#" + id + "] Done releasing agent (zone or tank condition).");
            // Possibly set status=ARRIVED or something else
        }
    }

    /**
     * The scheduler can call this to stop agent release mid-process.
     * The drone will also decide to return to base if it's in the middle of releasing.
     */
    public void stopAgent() {
        System.out.println("[Drone#" + id + "] handleStopAgent() called.");
        // if currently releasing agent, close nozzle
        if (agentTank.isNozzleOpen()) {
            agentTank.closeNozzle();
            System.out.println("[Drone#" + id + "] Stopped releasing agent mid-process.");
        }
        relAgentCmdFlag = false; // ensure we don't keep releasing
    }

    /**
     * Incremental approach to flight. If no destination is set, do nothing.
     * If arrived, set status=ARRIVED or BASE if the destination was BASE_POSITION.
     *
     * @param deltaTime is passed in run, by using systemTime in run()
     *
     */
    private void fly(float deltaTime) {

        // If no destination yet, see if the zone is set
        if (destination == null) {
            if (zoneToService != null) {
                destination = zoneToService.getPosition();
                setStatus(DroneStatus.ENROUTE);
                System.out.println("[Drone#" + id + "] handleFly: heading to zone " + zoneToService.getId());
            } else {
                // no zone, no destination => ignore
                flyCmdFlag = false;
                return;
            }
        }

        float distance = position.distanceFrom(destination);
        float stoppingDistance = (currentSpeed * currentSpeed) / (2 * LAND_DECEL_RATE);

        // If arrived (close enough + speed near 0), stop
        if (distance < ARRIVAL_DISTANCE_THRESHOLD && currentSpeed < NO_SPEED_FLOOR) {
            System.out.println("[Drone#" + id + "] handleFly: Arrived at dest. speed=" + currentSpeed);
            currentSpeed = 0;
            if (destination.equals(BASE_POSITION)) {
                setStatus(DroneStatus.BASE);
            } else {
                setStatus(DroneStatus.ARRIVED);
            }
            flyCmdFlag = false; // done flying
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
     * The main thread logic:
     * Check if a zone is assigned and needs servicing.
     * If yes, fly there, release agent, then return to base(should be set by scheduler but call it here for now).
     * Otherwise, idle for a moment and check again.<
     * In a real system, the Scheduler would update zoneToService or call stopAgent(),
     * and the drone must respond
     */
    @Override
    public void run() {
        System.out.println("[Drone#" + id + "] Thread started. Initial status: " + status);

        long previousTime = System.nanoTime(); // Use nanoseconds
        final long stepSize = 100_000_000L; // 100ms in nanoseconds

        while (!Thread.interrupted()) {
            long currentTime = System.nanoTime();

            // Only process updates every 100ms (simulate real-world time)
            if ((currentTime - previousTime) >= stepSize) {
                float deltaTime = (currentTime - previousTime) / 1_000_000_000f; // Convert ns to seconds
                previousTime = currentTime; // Update for next iteration

                // 1) check if the drone is at base , refill if at base and agent not full
                autoRefillIfAtBase();

                // 2) Read and clear command flags set by the scheduler

                if (stopAgentCmdFlag) {
                    stopAgent();         // closes nozzle or sets internal flags
                    stopAgentCmdFlag = false;  // reset
                } else if (flyCmdFlag) {
                    // Move the drone towards some assigned position (zone or otherwise)
                    fly(deltaTime);
                } else if (relAgentCmdFlag) {
                    // Release agent based on dt
                    releaseAgent(deltaTime);
                }

                // 3) Periodically update scheduler about position & status
                scheduler.droneStatusUpdated(this.getStatus());

            }
        }
        System.out.println("[Drone#" + id + "] Thread stopping.");
    }


    /**
     * tests
     * */
    /*
    public static void main(String[] args) {
        // 1) Create a mock Scheduler
        Scheduler mockScheduler = new Scheduler(); // or a mock
        Drone drone = new Drone(1, mockScheduler);
        drone.setZoneToService(new Zone(1, 1, 0, 700, 0, 600));

        // 2) Start the test with real-world pacing
        float totalTime = 0f;
        float endTime = 30.0f; // Test for 30 seconds
        long startTime = System.nanoTime();
        final long stepSize = 100_000_000L; // 100ms in nanoseconds

        while (totalTime < endTime) {
            long currentTime = System.nanoTime();
            if ((currentTime - startTime) >= stepSize) { // Enforce real-world pacing
                float deltaTime = (currentTime - startTime) / 1_000_000_000f; // Convert to seconds
                startTime = currentTime; // Update for next step

                drone.fly(deltaTime); // Step the simulation
                totalTime += deltaTime; // Accumulate elapsed time
            }
        }

        // 3) Check results
        float distFromStart = drone.getPosition().distanceFrom(new Position(0, 0));
        System.out.println("TestflyIncrementally done. distFromStart=" + distFromStart);

        // Optional: Check if drone reached top speed
        // System.out.println("Final speed=" + drone.getCurrentSpeed());
    }*/


    /*test for release agent*/
    /*
    public static void main(String[] args) {
        Scheduler mockScheduler = new Scheduler();

        Drone drone = new Drone(1, mockScheduler);
        drone.setZoneToService(new Zone(1, 10, 0, 700, 0, 600));

        System.out.println("\n [Test Start] Drone releasing agent...");

        float totalTime = 0f;
        float deltaTime = 1.0f; // Simulate 1 second per update

        while (totalTime < 20.0f) { // Let it run for 20 seconds
            drone.releaseAgent(deltaTime);
            totalTime += deltaTime;

            System.out.println(" Time: " + totalTime + "s | Tank: " + drone.getTankCapacity() +
                    "L | Zone Need: " + drone.getZoneToService().getRequiredAgentAmount() + "L");

            if (drone.getTankCapacity() <= 0 || drone.getZoneToService().getRequiredAgentAmount() <= 0) {
                System.out.println(" [Test End] Agent release stopped.");
                System.out.println(drone.getStatus());
                break;
            }

            //Simulate real-world time (optional)
            try {
                Thread.sleep(100); // Sleep 1 second to simulate real time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/

}

