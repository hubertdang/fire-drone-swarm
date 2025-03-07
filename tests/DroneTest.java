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
    void testExternalEvent() throws InterruptedException {
        Thread droneThread = new Thread(drone, "🛫D");
        Zone zone = new Zone(1, 10, 10, 20, 10, 20);
        DroneTask task = new DroneTask(1, DroneTaskType.SERVICE_ZONE, zone);
        droneThread.start(); // Start the drone thread

        // External @ takeOff
        drone.updateState(DroneStateID.TAKEOFF);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        sleep(2000); // Allow some time for state change
        assertEquals(DroneStateID.TAKEOFF, drone.getCurrStateID(), "external event when taking off");

        // External @ accelerating
        drone.updateState(DroneStateID.ACCELERATING);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        sleep(2000); // Allow some time for state change
        assertEquals(DroneStateID.ACCELERATING, drone.getCurrStateID(), "external event when accelerating");

        // External @ flying
        drone.updateState(DroneStateID.FLYING);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        sleep(2000); // Allow some time for state change
        assertEquals(DroneStateID.FLYING, drone.getCurrStateID(), "external event when flying");

        // External @ decelerating
        drone.updateState(DroneStateID.DECELERATING);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        Thread.sleep(3000); // Allow some time for state change
        assertEquals(DroneStateID.ACCELERATING, drone.getCurrStateID(), "external event when decelerating");

        // External @ landing
        drone.updateState(DroneStateID.LANDING);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        sleep(2000); // Allow some time for state change
        assertEquals(DroneStateID.TAKEOFF, drone.getCurrStateID(), "external event when landing");

        // External @ releasing agent
        drone.updateState(DroneStateID.RELEASING_AGENT);
        drone.setCurrTask(task);
        drone.setNewTaskFlag();
        sleep(3000); // Allow some time for state change
        assertEquals(DroneStateID.ACCELERATING, drone.getCurrStateID(), "external event when releasing agent");

        // Attempt to stop the drone
        droneThread.interrupt(); // Safe way to stop the thread if Drone handles interruption
        droneThread.join(1000);
    }

    @Test
    void testSetAndGetDestination() {
        Position destination = new Position(100, 100);
        drone.setDestination(destination);
        assertEquals(destination, drone.getDestination());
    }
}