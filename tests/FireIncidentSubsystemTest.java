import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class FireIncidentSubsystemTest {
    private FireIncidentSubsystem fireIncidentSubsystem;

    @BeforeEach
    public void setUp() {
        fireIncidentSubsystem = new FireIncidentSubsystem();
    }

    @Test
    public void testReadSimZoneFile() throws Exception {
        // Create a temporary zone file for testing
        Path tempFile = Files.createTempFile("zones", ".csv");
        Files.writeString(tempFile, """
                    Zone ID,Zone Start,Zone End
                    1,(0;0),(700;600)
                    2,(0;600),(650;1500)
                """);

        // Read the zone file
        fireIncidentSubsystem.readSimZoneFile(tempFile.toFile());

        // Verify that zones are added correctly
        HashMap<Integer, Zone> clearZones = fireIncidentSubsystem.getClearZones();
        assertEquals(2, clearZones.size());
        assertNotNull(clearZones.get(1));
        assertNotNull(clearZones.get(2));
        System.out.println(clearZones.get(1).getPosition().getX());
        assertEquals(1, clearZones.get(1).getId());
        assertEquals(2, clearZones.get(2).getId());
    }

    @Test
    public void testReadSimEventFile() throws Exception {
        // Create a temporary event file for testing
        Path tempFile = Files.createTempFile("events", ".csv");
        Files.writeString(tempFile, """
                    Time,Zone ID,Event type,Severity
                    14:03:15,1,FIRE_DETECTED,High
                    14:10:00,2,DRONE_REQUEST,Moderate
                """);

        // Read the event file
        fireIncidentSubsystem.readSimEventFile(tempFile.toFile());

        // Verify that events are added correctly
        assertEquals(2, fireIncidentSubsystem.getEvents().size());
        SimEvent event1 = fireIncidentSubsystem.getEvents().get(0);
        assertEquals(1, event1.getZoneId());
        assertEquals(195, event1.getTime());
        assertEquals("FIRE_DETECTED", event1.getEventType());
        assertEquals(FireSeverity.HIGH, event1.getSeverity());
    }

    @Test
    void testIsEventReadyToProcess() {
        // Add an event
        fireIncidentSubsystem.getEvents().add(new SimEvent(14 * 3600 * 1000 + 3 * 60 * 1000 + 15 * 1000, 1, "FIRE_DETECTED", "High"));

        // Verify event readiness
        assertTrue(fireIncidentSubsystem.isEventReadyToProcess(0, 14 * 3600 * 1000 + 3 * 60 * 1000 + 15 * 1000, 14 * 3600 * 1000 + 3 * 60 * 1000 + 15 * 1000));
        assertFalse(fireIncidentSubsystem.isEventReadyToProcess(0, 14 * 3600 * 1000 + 3 * 60 * 1000 + 15 * 1000, 14 * 3600 * 1000 + 3 * 60 * 1000));
    }

}