import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZoneTest {

    private Zone zone;

    @BeforeEach
    public void setUp() {
        zone = new Zone(1, 5.0f, 0, 10, 0, 10);
    }

    @Test
    public void setSeverity() {
        zone.setSeverity(FireSeverity.HIGH);
        assertEquals(FireSeverity.HIGH, zone.getSeverity(), "Severity should be set to HIGH");
    }

    @Test
    public void getSeverity() {
        zone.setSeverity(FireSeverity.LOW);
        assertEquals(FireSeverity.LOW, zone.getSeverity(), "Severity should be LOW");
    }

    @Test
    public void getPosition() {
        Position expectedPosition = new Position(5.0f, 5.0f); // Adjust according to the Zone constructor
        assertEquals(expectedPosition, zone.getPosition(), "Center position should be (5,5)");
    }

    @Test
    public void getId() {
        assertEquals(1, zone.getId(), "Zone ID should be 1");
    }

    @Test
    public void setRequiredAgents() {
        zone.setRequiredAgents(6.0f);
        assertEquals(6.0f, zone.getRequiredAgents(), "Required agents should be updated to 6.0");
    }

    @Test
    public void getRequiredAgents() {
        assertEquals(5.0f, zone.getRequiredAgents(), "Required agents should be 5.0");
    }

    @Test
    public void testEquals() {
        Zone zone2 = new Zone(1, 5.0f, 0, 10, 0, 10);
        assertEquals(zone, zone2, "Zones with the same ID, position, severity, and required agents should be equal");

        zone2.setSeverity(FireSeverity.MODERATE);
        assertNotEquals(zone, zone2, "Zones should not be equal if severity is different");
    }
}