/**
 * <p>Drone</p>
 *
 * // Example usage:
 * Drone drone = new Drone(1, scheduler);
 * drone.run();
 * }
 *
 * @author Shenhao Gong
 *
 * @version 2025-Jan-26th
 */

public class Drone implements Runnable {
    private static final Position BASE_POSITION = new Position(0, 0);
    private static final float TOP_SPEED = 20.0f;
    private static final float TAKEOFF_ACCEL_RATE = 3.0f;
    private static final float LAND_DECEL_RATE = 5.0f;
    private static final float ARRIVAL_DISTANCE_THRESHOLD = 20f;

    private int id;
    private Position position;
    //private float rating;           //for scheduling algorithm later
    private Zone zoneToService;       // The zone assigned by the Scheduler. The drone won't pick tasks itself
    private DroneStatus status;
    private Scheduler scheduler;
    private AgentTank agentTank;
    private float currentSpeed= 0f;

    private float localUsedAmount = 0.0f;    //buffer to calculate the agent used

    private volatile boolean stopRequested = false;    //flags that if Scheduler  calls stopAgent()

    public Drone(int id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
        this.position = new Position(BASE_POSITION.getX(), BASE_POSITION.getY());
        this.currentSpeed = 0f;
        this.status=DroneStatus.BASE;
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

    public void setZoneToService(Zone zone) {
        this.zoneToService = zone;
    }

    public Zone getZoneToService() {
        return zoneToService;
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
    }

    public DroneStatus getStatus() {
        return status;
    }



    /**
     * In Iteration #1, this just simulates the release process:
     *  - If the tank is not empty, keep spraying at 1L per 1s
     *  - Decrease the zone's required agent accordingly
     *  - Update status via scheduler
     *
     */
    private void releaseAgent() {
        stopRequested = false;
        if (agentTank.isEmpty()) {
            System.out.println("[Drone#" + id + "] Tank empty, cannot release agent.");
            setStatus(DroneStatus.EMPTY);
            return;
        }

        agentTank.openNozzle();
        System.out.println("[Drone#" + id + "] Nozzle open. Releasing agent...");

        // Keep releasing until zone is satisfied, tank is empty, or stopRequested
        while (!stopRequested      //when scheduler call stopAgent(), this flag will be set to true
                && zoneToService != null
                && zoneToService.getRequiredAgent() > 0
                && !agentTank.isEmpty()) {
            agentTank.decreaseAgent(1.0f); // 1L each loop
            float newRequired = zoneToService.getRequiredAgent() - 1.0f;
            zoneToService.setRequiredAgentAmount(Math.max(0, newRequired));

            // If tank became empty, report it
            if (agentTank.isEmpty()) {
                System.out.println("[Drone#" + id + "] Tank became empty.");
                scheduler.droneStatusUpdated(DroneStatus.EMPTY);
            }

            // Sleep a bit to simulate time
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("[Drone#" + id + "] Interrupted in releaseAgent.");
                break;
            }
        }

        agentTank.closeNozzle();

        if (stopRequested) {
            System.out.println("[Drone#" + id + "] stopRequested => Stopped release mid-process.");
        } else {
            System.out.println("[Drone#" + id + "] Done releasing agent.");
        }
    }

    /**
     * The scheduler can call this to stop agent release mid-process.
     * The drone will also decide to return to base if it's in the middle of releasing.
     */
    public void stopAgent() {
        stopRequested = true;
        agentTank.closeNozzle();
        System.out.println("[Drone#" + id + "] stopAgent() called. stopRequested=true");
    }



    private void fly(Position target) throws InterruptedException {
        final long stepMillis = 100;
        final float stepSec   = stepMillis / 1000f;

        while (true) {
            float distance = position.distanceFrom(target);

            // If drone is very close but speed is still high, continue decelerating
            // or if speed is near 0 but distance is still not negligible, keep going
            if (distance < ARRIVAL_DISTANCE_THRESHOLD && currentSpeed < 0.5f) {
                // We consider this truly arrived
                break;
            }

            // Accelerate or decelerate
            if (currentSpeed < TOP_SPEED) {
                currentSpeed += TAKEOFF_ACCEL_RATE * stepSec;
                if (currentSpeed > TOP_SPEED) {
                    currentSpeed = TOP_SPEED;
                }
            }
            if (distance < currentSpeed * 5) {
                currentSpeed -= LAND_DECEL_RATE * stepSec;
                if (currentSpeed < 0) {
                    currentSpeed = 0;
                }
            }

            float stepDist = currentSpeed * stepSec;
            float dx = target.getX() - position.getX();
            float dy = target.getY() - position.getY();
            float angle = (float) Math.atan2(dy, dx);

            // If stepDist > distance, do a partial step
            if (stepDist > distance) {
                stepDist = distance; // only move exactly the needed distance
            }

            float newX = position.getX() + stepDist * (float)Math.cos(angle);
            float newY = position.getY() + stepDist * (float)Math.sin(angle);
            position.update(newX, newY);

            System.out.println("[Drone#" + id + "] Position: ("
                    + position.getX() + "," + position.getY()
                    + "), speed=" + currentSpeed + ", dist=" + distance);

            Thread.sleep(stepMillis);
        }

        System.out.println("[Drone#" + id + "] Arrived at ("
                + position.getX() + "," + position.getY()
                + ") with speed ~" + currentSpeed);
        currentSpeed = 0;
    }

    private boolean hasArrived(Position target) {
        float dist = position.distanceFrom(target);
        return dist <= ARRIVAL_DISTANCE_THRESHOLD;
    }



    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Drone) && ((Drone) obj).id == this.id;
    }

    /**
     * The main thread logic:
     *
     *   Check if a zone is assigned and needs servicing.
     *   If yes, fly there, release agent, then return to base(should be set by scheduler but call it here for now).
     *   Otherwise, idle for a moment and check again.<
     *
     *
     * In a real system, the Scheduler would update zoneToService or call stopAgent(),
     * and the drone must respond
     */
    @Override
    public void run() {
        System.out.println("[Drone#" + id + "] Thread started. Initial status: " + status);

        while (true) {
            try {
                // If there's a zone needing service
                if (zoneToService != null && zoneToService.getRequiredAgent() > 0) {

                    // 1) Fly to zone
                    setStatus(DroneStatus.ENROUTE);
                    scheduler.droneStatusUpdated(DroneStatus.ENROUTE);
                    fly(zoneToService.getPosition());

                    // 2) Arrive
                    setStatus(DroneStatus.ARRIVED);
                    scheduler.droneStatusUpdated(DroneStatus.ARRIVED);

                    // 3) Release agent
                    setStatus(DroneStatus.DROPPING_AGENT);
                    scheduler.droneStatusUpdated(DroneStatus.DROPPING_AGENT);
                    releaseAgent();

                    // 4) Fly back to base
                    setStatus(DroneStatus.ENROUTE);
                    scheduler.droneStatusUpdated(DroneStatus.ENROUTE);
                    fly(BASE_POSITION);

                    setStatus(DroneStatus.BASE);
                    scheduler.droneStatusUpdated(DroneStatus.BASE);

                    // Reset
                    zoneToService = null;
                    stopRequested = false;
                } else {
                    // No zone or no need, just idle
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("[Drone#" + id + "] Interrupted → stopping thread.");
                break;
            }
        }
    }

    /**
     * check if zone still need agent
     */
    private boolean zoneStillNeedsAgent(Zone zone) {
        if (zone == null) return false;
        return zone.getRequiredAgent() > 0;
    }



    /**
     *
     * quickly did an fly(150.0,80.0) test, the result is:
     * [FlyTest] Initial drone position: 0.0, 0.0
     * [FlyTest] Calling fly(...) to 150.0, 80.0
     * [Drone#1] Arrived at (132.95303, 70.90828)
     * .
     * .
     * .
     * [FlyTest] Final drone position: 132.95303, 70.90828
     * [FlyTest] Distance to target = 19.319899
     * [FlyTest] PASS: Drone arrived within threshold.
     * */
   /* public static void main(String[] args) {
        // Create a minimal mock of a Scheduler if needed
        Scheduler mockScheduler = new Scheduler() {
            //@Override
            public void droneStatusUpdated(DroneStatus status, Drone drone) {
                System.out.println("[MockScheduler] Drone#" + drone.getId()
                        + " status: " + status);
            }
        };

        // Create a Drone (not started in a separate thread yet)
        Drone drone = new Drone(1, mockScheduler);

        // Print the initial position (should be base, e.g., (0,0))
        System.out.println("[FlyTest] Initial drone position: "
                + drone.getPosition().getX() + ", " + drone.getPosition().getY());

        // Now directly call fly(...) to see how it updates position
        // For demonstration, let's fly to (150, 80).
        Position target = new Position(200, 150);

        try {
            System.out.println("[FlyTest] Calling fly(...) to "
                    + target.getX() + ", " + target.getY());
            drone.fly(target);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // After fly(...) returns, the drone should be at or near the target
        float finalDist = drone.getPosition().distanceFrom(target);
        System.out.println("[FlyTest] Final drone position: "
                + drone.getPosition().getX() + ", " + drone.getPosition().getY());
        System.out.println("[FlyTest] Distance to target = " + finalDist);

        // Simple assertion or check
        if (finalDist <= 20f) { // or ARRIVAL_DISTANCE_THRESHOLD
            System.out.println("[FlyTest] PASS: Drone arrived within threshold.");
        } else {
            System.out.println("[FlyTest] FAIL: Drone too far from target. (dist=" + finalDist + ")");
        }
    }*/
}