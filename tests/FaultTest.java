import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class for unit testing faults in drones.
 */
public class FaultTest {
    private Drone drone1, drone2;
    private DroneFault fault;
    private Thread droneThread1, droneThread2;

    /**
     * Initializes 2 drones and a Drone Stuck Mid-Flight fault.
     */
    @BeforeEach
    void setUp() {
        drone1 = new Drone();
        drone2 = new Drone();
        fault = new DroneFault(2, 5, TimeUtils.csvTimeToMillis("18:40:05"), FaultID.DRONE_STUCK);
        droneThread1 = new Thread(drone1);
        droneThread2 = new Thread(drone2);
    }

    /**
     * Tests the state changes when a drone faults.
     * Uses print statements and assertions.
     * <p>
     * Expected state change for a drone that faults:
     * BASE -> FAULT
     */
    @Test
    void testFault() throws InterruptedException {
        droneThread1.start();
        droneThread2.start();
        Thread.sleep(100);
        assertEquals(DroneStateID.BASE, drone1.getCurrStateID());
        assertEquals(DroneStateID.BASE, drone2.getCurrStateID());

        // wait for the fault time to arrive
        while ((Math.abs(fault.getFaultTime() - TimeUtils.getCurrentTime()) >= 2000)) {
            ;
        }

        if (Math.abs(fault.getFaultTime() - TimeUtils.getCurrentTime()) <= 2000) {
            System.out.println("🚨 " + fault.toString());

            if (fault.getDroneId() == drone1.getId()) {
                drone1.setFault(fault.getFaultType());
                assertEquals(DroneStateID.FAULT, drone1.getCurrStateID());
                assertNull(drone1.getZoneToService());
                assertNull(drone1.getCurrTask());
            }
            else if (fault.getDroneId() == drone2.getId()) {
                drone2.setFault(fault.getFaultType());
                // make the thread sleep to allow for state transition
                Thread.sleep(1000);
                System.out.println("Drone 2 - Current State: " + drone2.getCurrStateID());
                assertEquals(DroneStateID.FAULT, drone2.getCurrStateID());
                assertNull(drone2.getZoneToService());
                assertNull(drone2.getCurrTask());
            }
            else {
                System.out.println("No drone found with id: " + fault.getDroneId());
            }
        }
    }
}
