public class DroneSubsystem {
    public static void main(String[] args) {
        /* TODO: Find a way to not hardcode port numbers (maybe with static field and offsetting */
        Drone drone = new Drone(1, 5001);
        DroneController droneController = new DroneController(drone, 6001);
        Drone drone1 = new Drone(2, 5002);
        DroneController droneController1 = new DroneController(drone1, 6002);


        Thread droneThread = new Thread(drone, "🛫D");
        Thread droneControllerThread = new Thread(droneController, "🛫DC");

        Thread droneThread1 = new Thread(drone1, "🛫D");
        Thread droneControllerThread1 = new Thread(droneController1, "🛫DC2");

        droneThread.start();
        droneControllerThread.start();
        droneThread1.start();
        droneControllerThread1.start();

    }
}
