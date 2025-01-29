import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ZoneTest {

    @Test
    public void setSeverity() {
        Zone zone = new Zone(1, 5.0f, 0, 10, 0, 10);
        zone.setSeverity(FireSeverity.HIGH);
        assertEquals(FireSeverity.HIGH, zone.getSeverity(), "Severity should be set to HIGH");
    }

    @Test
    public void getSeverity() {
        Zone zone = new Zone(2, 4.5f, 5, 15, 5, 15);
        zone.setSeverity(FireSeverity.LOW);
        assertEquals(FireSeverity.LOW, zone.getSeverity(), "Severity should be LOW");
    }

    @Test
    public void getPosition() {
        Zone zone = new Zone(3, 3.0f, 0, 20, 0, 20);
        Position expectedPosition = new Position(10.0f, 10.0f);
        assertEquals(expectedPosition, zone.getPosition(), "Center position should be (10,10)");
    }

    @Test
    public void getId() {
        Zone zone = new Zone(4, 2.0f, 10, 30, 10, 30);
        assertEquals(4, zone.getId(), "Zone ID should be 4");
    }

    @Test
    public void setRequiredAgents() {
        Zone zone = new Zone(5, 2.5f, 0, 15, 0, 15);
        zone.setRequiredAgents(6.0f);
        assertEquals(6.0f, zone.getRequiredAgents(), "Required agents should be updated to 6.0");
    }

    @Test
    public void getRequiredAgents() {
        Zone zone = new Zone(6, 7.5f, 0, 10, 0, 10);
        assertEquals(7.5f, zone.getRequiredAgents(), "Required agents should be 7.5");
    }

    @Test
    public void testEquals() {
        Zone zone1 = new Zone(7, 3.5f, 0, 10, 0, 10);
        Zone zone2 = new Zone(7, 3.5f, 0, 10, 0, 10);

        assertEquals(zone1, zone2, "Zones with the same ID, position, severity, and required agents should be equal");

        zone2.setSeverity(FireSeverity.MODERATE);
        assertNotEquals(zone1, zone2, "Zones should not be equal if severity is different");
    }
}
