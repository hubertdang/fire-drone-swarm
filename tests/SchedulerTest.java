import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {
    private Scheduler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler(new DroneBuffer(), new FireIncidentBuffer());
    }

}
