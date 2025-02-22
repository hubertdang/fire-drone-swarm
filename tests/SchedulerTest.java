import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {
    private Scheduler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler(new DroneBuffer(), new FireIncidentBuffer());
    }

    @Test
    public void testHandleFireReq() throws Exception {
        Zone zone = new Zone(1, 50.0F, 50, 70, 0, 40);
        zone.setSeverity(FireSeverity.MODERATE);
        assertTrue(scheduler.getMissionQueue().isEmpty());
        scheduler.handleFireReq(zone);
        assertFalse(scheduler.getMissionQueue().isEmpty());
        //assertEquals(scheduler.getMissionQueue().peek(), zone); Todo
    }

    @Test
    public void testGetMissionQueue() throws Exception {
        Missions missions = new Missions();
        Zone zone = new Zone(1, 50.0F, 50, 70, 0, 40);
        zone.setSeverity(FireSeverity.MODERATE);
        //missions.queue(zone);
        //scheduler.getMissionQueue().queue(zone);
        //assertEquals(missions, scheduler.getMissionQueue());
        //ToDo

    }
}
