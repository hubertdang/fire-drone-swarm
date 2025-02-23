import java.io.File;

import static java.lang.Thread.sleep;

/**
 * Main Class
 * A class in the fire drone swarm system designed to run the application.
 */
public class Main {
    public static void main(String[] args) {
        // Instantiate message passing buffers
        DroneBuffer droneBuffer = new DroneBuffer();
        FireIncidentBuffer fireBuffer = new FireIncidentBuffer();

        // Instantiate Fire Incident Subsystem
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem(fireBuffer);
        fireIncidentSubsystem.readSimZoneFile(new File("./sample_input_files/zones.csv"));
        fireIncidentSubsystem.readSimEventFile(new File("./sample_input_files/events.csv"));

        // Instantiate Scheduler
        Scheduler scheduler = new Scheduler(droneBuffer, fireBuffer);

        // Instantiate Drone
        DroneManager droneManager = new DroneManager(droneBuffer);
        Drone drone1 = new Drone(1, droneManager);
        Drone drone2 = new Drone(2, droneManager);
        droneManager.addDrone(drone1);
        droneManager.addDrone(drone2);


        // Instantiate threads
        System.out.println("--- \nNote: \nFIS-fireSubsystemThread \nDSS-droneSubsystemThread \nSD-schedulerThread \nD-droneThread \n---");
        Thread droneThread1 = new Thread(drone1, "🛸D");
        Thread droneThread2 = new Thread(drone2, "🛸D");
        Thread fireSubsystemThread = new Thread(fireIncidentSubsystem, "🐦‍🔥FIS");
        Thread schedulerThread = new Thread(scheduler, "📅SD");
        Thread droneSubsystemThread = new Thread(droneManager, "⚒️DM");

        // run threads
        fireSubsystemThread.start();
        schedulerThread.start();
        droneSubsystemThread.start();
        droneThread1.start();
        droneThread2.start();

        // exit program when Fire Incident Subsystem indicates no more events to service
        while (fireSubsystemThread.isAlive()) {
            try {
                sleep(5000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
