/**import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FireIncidentBufferTest {
    private FireIncidentBuffer fireIncidentBuffer;
    private Zone testZone;

    @BeforeEach
    public void setUp() {
        fireIncidentBuffer = new FireIncidentBuffer();
        testZone = new Zone(1, 0, 0, 0, 700, 600);
    }

    @Test
    public void testAddAndPopEventMessage() {
        fireIncidentBuffer.addEventMessage(testZone);
        assertTrue(fireIncidentBuffer.newEvent());
        Zone poppedZone = fireIncidentBuffer.popEventMessage();
        assertEquals(testZone, poppedZone);
        assertFalse(fireIncidentBuffer.newEvent());
    }

    @Test
    public void testAddAndPopAcknowledgementMessage() {
        fireIncidentBuffer.addAcknowledgementMessage(testZone);
        assertTrue(fireIncidentBuffer.newAcknowledgement());
        Zone poppedZone = fireIncidentBuffer.popAcknowledgementMessage();
        assertEquals(testZone, poppedZone);
        assertFalse(fireIncidentBuffer.newAcknowledgement());
    }

    @Test
    public void testPopEventMessageFromEmptyBuffer() {
        assertThrows(IndexOutOfBoundsException.class, () -> fireIncidentBuffer.popEventMessage());
    }

    @Test
    public void testPopAcknowledgementMessageFromEmptyBuffer() {
        assertThrows(IndexOutOfBoundsException.class, () -> fireIncidentBuffer.popAcknowledgementMessage());
    }
}**/