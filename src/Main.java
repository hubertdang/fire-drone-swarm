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
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem(9000);
        fireIncidentSubsystem.readSimZoneFile(new File("./sample_input_files/zones.csv"));
        fireIncidentSubsystem.readSimEventFile(new File("./sample_input_files/events.csv"));

        // Instantiate Scheduler
        Scheduler scheduler = new Scheduler();

        // Instantiate threads
        System.out.println("--- \nNote: \nFIS-fireSubsystemThread \nDM-droneSubsystemThread \nSD-schedulerThread \nD-droneThread \n---");
        Thread fireSubsystemThread = new Thread(fireIncidentSubsystem, "🐦‍🔥FIS");
        //Thread schedulerThread = new Thread(scheduler, "📅SD");

        // run threads
        fireSubsystemThread.start();
        //schedulerThread.start();

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
