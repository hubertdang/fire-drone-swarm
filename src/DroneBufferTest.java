import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DroneBufferTest {
    private DroneBuffer droneBuffer;
    private Task testTask;

    @BeforeEach
    public void setUp() {
        droneBuffer = new DroneBuffer();
        testTask = new Task(DroneStatus.IDLE, new Zone(1, 0, 0, 0, 700, 600));
    }

    @Test
    public void testAddAndPopDroneTask() {
        droneBuffer.addDroneTask(testTask);
        Task poppedTask = droneBuffer.popSchedulerTask();
        assertEquals(testTask, poppedTask);
        assertFalse(droneBuffer.newAcknowledgement());
    }

    @Test
    public void testAddAndPopSchedulerAcknowledgement() {
        droneBuffer.addSchedulerAcknowledgement(testTask);
        assertTrue(droneBuffer.newAcknowledgement());
        Task poppedTask = droneBuffer.popDroneAcknowledgement();
        assertEquals(testTask, poppedTask);
        assertFalse(droneBuffer.newAcknowledgement());
    }

    @Test
    public void testPopDroneAcknowledgementFromEmptyBuffer() {
        assertNull(droneBuffer.popDroneAcknowledgement());
    }

    @Test
    public void testPopSchedulerTaskFromEmptyBuffer() {
        assertNull(droneBuffer.popSchedulerTask());
    }

    @Test
    public void testWaitForTask() {
        Thread thread = new Thread(() -> {
            droneBuffer.waitForTask();
            assertTrue(droneBuffer.newAcknowledgement());
        });
        thread.start();

        try {
            Thread.sleep(1000); // Ensure the thread is waiting
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        droneBuffer.addDroneTask(testTask);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}