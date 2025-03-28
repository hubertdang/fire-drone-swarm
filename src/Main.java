import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

/**
 * Main Class
 * A class in the fire drone swarm system designed to run the application.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Instantiate Application
        SchedulerSubsystem.main(args);
        DroneSubsystem.main(args);
        FireIncidentSubsystem.main(args);
    }
}
