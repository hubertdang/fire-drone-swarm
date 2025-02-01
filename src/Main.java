import java.io.File;

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
        Drone drone = new Drone(1, droneBuffer);


        // Instantiate threads
        Thread fireSubsystemThread = new Thread(fireIncidentSubsystem);
        Thread schedulerThread = new Thread(scheduler);
        Thread droneThread = new Thread(drone);

        // run threads
        fireSubsystemThread.start();
        schedulerThread.start();
        droneThread.start();

    }
}
