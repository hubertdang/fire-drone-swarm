/**
 * A DroneRequestHandler to handle drone requests.
 */
public class DroneRequestHandler extends MessagePasser implements Runnable {
    private Scheduler scheduler;

    /**
     * Constructs a DroneRequestHandler for the system.
     */
    public DroneRequestHandler(int port, Scheduler scheduler) {
        super(port);
        this.scheduler = scheduler;
    }

    /**
     * Run method for the DroneRequestHandler thread.
     * Receives drone information and processes it.
     */
    @Override
    public void run() {
        while (true) {
            DroneInfo droneInfo = (DroneInfo) receive();
            if (droneInfo != null) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a drone info from Drone#" + droneInfo.droneID
                        + " | STATE = " + droneInfo.stateID
                        + " | POSITION = " + droneInfo.getPosition()
                        + " | TANK = " + String.format("%.2f L", droneInfo.getAgentTankAmount()));
                scheduler.processDroneInfo(droneInfo, this);
                scheduler.dispatchActions(this, droneInfo.droneID);
            }
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
