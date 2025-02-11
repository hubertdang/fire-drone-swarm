import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DroneBufferTest {
    private DroneBuffer droneBuffer;
    private DroneTask testTask;

    @BeforeEach
    public void setUp() {
        droneBuffer = new DroneBuffer();
        testTask = new DroneTask(DroneTaskType.SERVICE_ZONE, new Zone(1, 0, 0, 0, 700, 600));
    }

    @Test
    public void testAddAndPopDroneTask() {
        droneBuffer.addDroneTask(testTask);
        DroneTask poppedTask = droneBuffer.popSchedulerTask();
        assertEquals(testTask, poppedTask);
        assertFalse(droneBuffer.hasDroneInfo());
    }


    @Test
    public void testPopDroneInfoFromEmptyBuffer() {
        assertNull(droneBuffer.popDroneInfo());
    }

    @Test
    public void testPopSchedulerTaskFromEmptyBuffer() {
        assertNull(droneBuffer.popSchedulerTask());
    }

    @Test
    public void testWaitForTask() {
        Thread thread = new Thread(() -> {
            droneBuffer.waitForTask();
            assertTrue(droneBuffer.hasDroneInfo());
        });
        thread.start();

        try {
            Thread.sleep(1000); // Ensure the thread is waiting
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        droneBuffer.addDroneTask(testTask);
        try {
            thread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}