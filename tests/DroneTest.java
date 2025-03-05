import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static java.lang.Thread.sleep;

class DroneTest {
    private Drone drone;
    private DroneManager droneManager;
    private DroneBuffer droneBuffer;

    @BeforeEach
    void setUp() {
        droneManager = new DroneManager(droneBuffer);
        drone = new Drone(1, droneManager);
    }

    @Test
    void testGetPosition() {
        assertEquals(0.0f, drone.getPosition().getX());
        assertEquals(0.0f, drone.getPosition().getY());
    }

    @Test
    void testGetId() {
        assertEquals(1, drone.getId());
    }

    @Test
    void testSetZoneToServiceAndGetZoneToService() {
        Zone testZone = new Zone(1,99, 0, 0, 0, 0);
        drone.setZoneToService(testZone);
        assertEquals(testZone, drone.getZoneToService());
    }

    @Test
    void testEquals() {
        Drone sameDrone = drone;
        Drone diffDrone = new Drone(2, droneManager);
        assertEquals(drone, sameDrone, "Drones with same ID should be equal.");
        assertNotEquals(drone, diffDrone, "Drones with different IDs should not be equal.");
    }

    @Test
    void testToString() {
        assertEquals("[Drone#1, pos=(0.0,0.0)]", drone.toString());
    }

    @Test
    void testUpdateState() {
        drone.updateState(DroneStateID.TAKEOFF);
        assertEquals(DroneStateID.TAKEOFF, drone.getCurrStateID());
    }

    @Test
    void testSetAndGetDestination() {
        Position destination = new Position(100, 100);
        drone.setDestination(destination);
        assertEquals(destination, drone.getDestination());
    }
}