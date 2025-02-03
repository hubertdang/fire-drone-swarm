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
        Drone drone = new Drone(1, droneBuffer);


        // Instantiate threads
        Thread fireSubsystemThread = new Thread(fireIncidentSubsystem, "FIS");
        Thread schedulerThread = new Thread(scheduler, "SD");
        Thread droneThread = new Thread(drone, "D");

        // run threads
        fireSubsystemThread.start();
        schedulerThread.start();
        droneThread.start();

        // exit program when Fire Incident Subsystem indicates no more events to service
        while (fireSubsystemThread.isAlive()) {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
