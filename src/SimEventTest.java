import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;

public class SimEventTest {
    private SimEvent simEvent;

    @BeforeEach
    public void setUp() {
        simEvent = new SimEvent(1000, 1, "FIRE_DETECTED", "High");
    }

    @Test
    public void testGetTime() {
        assertEquals(1000, simEvent.getTime());
    }

    @Test
    public void testGetZoneId() {
        assertEquals(1, simEvent.getZoneId());
    }

    @Test
    public void testGetEventType() {
        assertEquals("FIRE_DETECTED", simEvent.getEventType());
    }

    @Test
    public void testGetSeverity() {
        assertEquals(FireSeverity.HIGH, simEvent.getSeverity());
    }
}
