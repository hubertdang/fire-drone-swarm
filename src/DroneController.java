public class DroneController extends MessagePasser implements Runnable {
    private final Drone drone;

    /**
     * Creates a MessagePasser object with a specific port to bind its socket to.
     *
     * @param port The port to
     */
    public DroneController(Drone drone) {
        super(6000 + drone.getId());
        this.drone = drone;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        DroneTask task;

        while (true) {
            task = (DroneTask) receive();
            processTask(task);
            dispatchTask(task);
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Processes a task by identifying the task type, and then preparing the drone to perform the
     * task.
     *
     * @param task The task to process.
     */
    private void processTask(DroneTask task) {
        String zoneId = (task.getZone() != null) ? String.valueOf(task.getZone().getId()) : "null";

        System.out.println("[" + Thread.currentThread().getName() + "]: " + "Received task "
                + task.getTaskType() + " @ Zone# " + zoneId + " for Drone# " + task.getDroneID());

        switch (task.getTaskType()) {
            case SERVICE_ZONE:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching SERVICE_ZONE task to Drone# " + drone.getId());
                drone.setDestination(task.getZone().getPosition());
                drone.setZoneToService(task.getZone());
                break;
            case RELEASE_AGENT:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching RELEASE_AGENT task to Drone# " + drone.getId());
                break;
            case RECALL:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Dispatching RECALL task to Drone# " + drone.getId());
                drone.setDestination(new Position(Drone.BASE_X, Drone.BASE_Y));
                break;
            case REQUEST_INFO:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Processing REQUEST_ALL_INFO task for Drone# " + drone.getId());
                DroneInfo info = new DroneInfo(
                        drone.getId(),
                        drone.getCurrStateID(),
                        drone.getPosition(),
                        drone.getAgentTankAmount(),
                        drone.getZoneToService());
                /* TODO: create a method to get scheduler's IP address and port instead of hard-coding */
                send(info, "localhost", 7000);
                System.out.println(info.toString());
                break;
            default:
                System.out.println("[" + Thread.currentThread().getName() + "]: "
                        + "Unknown task type.");
                break;
        }
    }

    /**
     * Dispatches a task to the drone.
     *
     * @param task The task to dispatch.
     */
    private void dispatchTask(DroneTask task) {
        if (task.getTaskType() == DroneTaskType.REQUEST_INFO) {
            return;
        }
        drone.setCurrTask(task);
        drone.setExternalEventFlag();
    }
}
