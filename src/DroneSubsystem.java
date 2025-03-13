public class DroneSubsystem {
    public static void main(String[] args) {
        /* TODO: Find a way to not hardcode port numbers (maybe with static field and offsetting */
        Drone drone = new Drone(1, 5000);
        DroneController droneController = new DroneController(drone, 5001);

        Thread droneThread = new Thread(drone, "🛫D");
        Thread droneControllerThread = new Thread(droneController, "🛫DC");

        droneThread.start();
        droneControllerThread.start();
    }
}
