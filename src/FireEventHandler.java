import java.util.*;

/**
 * A FireEventHandler to handle fire events.
 */
public class FireEventHandler extends MessagePasser implements Runnable {
    private Scheduler scheduler;
    private LinkedList<Zone> fireQueue;

    /**
     * Constructs a FireEventHandler for the system.
     * Initialises the scheduler and fire queue.
     */
    public FireEventHandler(int port, Scheduler scheduler) {
        super(port);
        this.scheduler = scheduler;
        this.fireQueue = new LinkedList<>();
    }

    /**
     * Run method for the FireEventHandler thread.
     * Receives fire events and schedules drones to respond to them.
     */
    @Override
    public void run() {
        while (true) {
            // check if there are any fire events in the queue, execute first
            if (!fireQueue.isEmpty()) {
                Zone storedZone = fireQueue.pop();
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Received a new event");
                scheduler.putZoneOnFire(storedZone);
                scheduleAllDrones(); // scheduling algorithm updates drone actions table for all drones
                scheduler.dispatchActions(this,-1);
            }

            Object data = receive();
            //if (data instanceof Zone) fireZone
            Zone fireZone = (Zone) data;
            if (fireZone != null) {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Received a new event");
                scheduler.putZoneOnFire(fireZone);
                scheduleAllDrones(); // scheduling algorithm updates drone actions table for all drones
                scheduler.dispatchActions(this,-1);
            }
            else {
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Received unknown message");
            }
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Gets all drone information from the drones.
     *
     * @return an ArrayList of DroneInfo objects
     */
    public ArrayList<DroneInfo> getAllDroneInfos() {
        ArrayList<DroneInfo> droneInfos = new ArrayList<>();
        // TODO: Implement a method to dynamically determine the number of drones in the system.
        // Currently, the number of drones is hardcoded in a for loop.
        // Each drone is assumed to have a unique ID starting from 1.
        // The system uses a convention where the port numbers for Drones are 5000 + droneID
        // and the port numbers for DroneControllers are 6000 + droneID.
        // For example, Drone with ID 1 will have ports 5001 and 6001 for its Drone
        // and DroneController respectively.
        for (int i = 1; i < DroneSubsystem.getNumberOfDrones(); i++) {
            DroneTask getInfo = new DroneTask(i, DroneTaskType.REQUEST_INFO, null, Scheduler.FEH_PORT);
            System.out.println("[" + Thread.currentThread().getName() + "]: "
                    + "Requesting info from Drone#" + i);

            Object message = null;
            send(getInfo, "localhost", 6000 + i);
            while (true) {
                message = receive(2000);
                if (message instanceof Zone) {
                    // we received a fire event, add to queue
                    System.out.println("[" + Thread.currentThread().getName() + "]: "
                            + "Received a fire event, adding it the fire queue");
                    fireQueue.add((Zone) message);
                }
                else if (message instanceof DroneInfo droneInfo) {
                    System.out.println("[" + Thread.currentThread().getName() + "]: "
                            + "Received DroneInfo from Drone#" + i);
                    //if (droneInfo.getStateID() != DroneStateID.FAULT) droneInfos.add(droneInfo);
                    droneInfos.add(droneInfo);
                    break;
                } else {break;}
            }
        }
        return droneInfos;
    }

    /**
     * Schedules all drones to respond to the fire events.
     */
    private void scheduleAllDrones() {
        ArrayList<DroneInfo> droneInfoList = getAllDroneInfos();
        scheduler.scheduleDrones(droneInfoList);
    }

}
