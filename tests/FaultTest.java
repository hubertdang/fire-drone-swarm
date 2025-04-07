import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class for unit testing faults in drones.
 */
public class FaultTest {
    private Drone drone1, drone2;
    private Thread droneThread1, droneThread2;
    private Scheduler scheduler;
    private DroneRequestHandler droneRequestHandler;

    /**
     * Initializes 2 drones and a Drone Stuck Mid-Flight fault.
     */
    @BeforeEach
    void setUp() {
        drone1 = new Drone();
        drone2 = new Drone();
        droneThread1 = new Thread(drone1);
        droneThread2 = new Thread(drone2);
        scheduler = new Scheduler();
        droneRequestHandler =  new DroneRequestHandler(7001, scheduler);
        droneThread1.start();
        droneThread2.start();
        Thread droneRequestHandlerThread = new Thread(droneRequestHandler);
        droneRequestHandlerThread.start();
    }

    /**
     * Tests the state changes when a drone faults.
     * Uses print statements and assertions.
     *
     * Expected state change for a drone that faults:
     * BASE -> FAULT
     */
    @Test
    void testFault() throws InterruptedException {
        Thread.sleep(100);
        Zone testZone = new Zone(1, 25, 100, 300, 0, 100);

        drone2.setDestination(testZone.getPosition());
        drone2.setZoneToService(testZone);
        drone2.setCurrTask(new DroneTask(1, DroneTaskType.SERVICE_ZONE, null, 0));

        assertEquals(DroneStateID.BASE, drone1.getCurrStateID());
        assertEquals(DroneStateID.BASE, drone2.getCurrStateID());
        drone2.setFault(FaultID.DRONE_STUCK);
        drone2.eventFaultDetected();
        // make the thread sleep to allow for state transition
        Thread.sleep(2000);

        assertEquals(DroneStateID.FAULT, drone2.getCurrStateID());
        //assertEquals(DroneTaskType.RECALL, drone2.getCurrTask().getTaskType());
        //assertEquals(DroneStateID.BASE, drone2.getCurrStateID());
    }


    @Test
    void test2Faults() throws InterruptedException {
        Thread.sleep(100);
        assertEquals(DroneStateID.BASE, drone1.getCurrStateID());
        assertEquals(DroneStateID.BASE, drone2.getCurrStateID());

        drone1.setFault(FaultID.CORRUPTED_MESSAGE);
        Thread.sleep(2000);
        assertEquals(drone1.getCurrStateID(), DroneStateID.FAULT);
        Thread.sleep(2000);

        drone1.setFault(FaultID.DRONE_STUCK);
        Thread.sleep(2000);
        assertEquals(drone1.getCurrStateID(), DroneStateID.FAULT);
    }
}
