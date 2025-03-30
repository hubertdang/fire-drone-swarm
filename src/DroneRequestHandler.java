import java.util.*;

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
     * Gets all drone information from the drones.
     *
     * @return an ArrayList of DroneInfo objects
     */
    public ArrayList<DroneInfo> getAllDroneInfos() {
        ArrayList<DroneInfo> droneInfos = new ArrayList<>();
        // Currently, the number of drones is hardcoded in a for loop.
        // Each drone is assumed to have a unique ID starting from 1.
        // The system uses a convention where the port numbers for Drones are 5000 + droneID
        // and the port numbers for DroneControllers are 6000 + droneID.
        // For example, Drone with ID 1 will have ports 5001 and 6001 for its Drone
        // and DroneController respectively.
        for (int i = 1; i < 3; i++) {
            DroneTask getInfo = new DroneTask(i, DroneTaskType.REQUEST_INFO, null);
            System.out.println("[" + Thread.currentThread().getName() + "]: "
                    + "Requesting info from Drone#" + i);

            Object message = null;
            send(getInfo, "localhost", 6000 + i);
            message = receive(1000);
            if (message == null) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Receive message timeout for Drone#" + i);
                continue;
            }
            System.out.println("[" + Thread.currentThread().getName() + "]: "
                    + "Received DroneInfo from Drone#" + i);
            DroneInfo droneInfo = (DroneInfo) message;
            droneInfos.add(droneInfo);
        }
        return droneInfos;
    }

    /**
     * Run method for the DroneRequestHandler thread.
     * Receives drone information and processes it.
     */
    @Override
    public void run() {
        while (true) {
            DroneInfo droneInfo = (DroneInfo) receive();
            if (droneInfo != null && droneInfo.fault == null) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a drone info from Drone#" + droneInfo.droneID
                        + " | STATE = " + droneInfo.stateID
                        + " | POSITION = " + droneInfo.getPosition()
                        + " | TANK = " + String.format("%.2f L", droneInfo.getAgentTankAmount()));
                scheduler.processDroneInfo(droneInfo, this);
                scheduler.dispatchActions(this, droneInfo.droneID);
            } else if (droneInfo.fault != null) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Scheduler has received a drone info from Drone#" + droneInfo.droneID
                        + " | FAULT = " + droneInfo.fault);
                // remove the faulted drone from servicing list
                HashMap<Zone, ZoneTriageInfo> zoneServices = scheduler.getZonesOnFire();
                Iterator<Map.Entry<Zone, ZoneTriageInfo>> servicesIterator = zoneServices.entrySet().iterator();
                while (servicesIterator.hasNext()) {
                    Map.Entry<Zone, ZoneTriageInfo> zoneEntry = servicesIterator.next();
                    LinkedHashMap<Integer, Map.Entry<Float, Float>> servicingDrones =
                            zoneEntry.getValue().getServicingDrones();
                    servicingDrones.remove(droneInfo.droneID);
                }
                scheduler.scheduleDrones(getAllDroneInfos());
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
