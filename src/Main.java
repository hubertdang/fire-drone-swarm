import java.io.File;

import static java.lang.Thread.sleep;

/**
 * Main Class
 * A class in the fire drone swarm system designed to run the application.
 */
public class Main {
    public static void main(String[] args) {
        // Instantiate Application
        SchedulerSubsystem.main(args);
        DroneSubsystem.main(args);
        FireIncidentSubsystem.main(args);
    }
}
