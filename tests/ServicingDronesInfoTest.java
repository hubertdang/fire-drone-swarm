import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicingDronesInfoTest {
    private static ServicingDronesInfo servicing;

    @BeforeAll
    static void start() {
        Zone zone = new Zone(1, 200, 0,4,0,4);
        servicing = new ServicingDronesInfo(zone);
    }

    @Test
    void getCurrentResponseTime() {
        Position drone1Pos = new Position(2, 102);
        Position drone2Pos = new Position(2, 152);
        Position drone3Pos = new Position(2, 302);
        servicing.addDrone(1, drone1Pos);
        servicing.addDrone(2, drone2Pos);
        servicing.addDrone(3, drone3Pos); // check if sorting method works
        float responseTime = servicing.getCurrentResponseTime();
        assertEquals(42.5, responseTime, 0.1);
        // 42.5 is hard coded make dynamic ToDo
    }

    @Test
    void getZoneId() {
    }

    @Test
    void addDrone() {
        Position dronePos = new Position(2, 100);
        boolean droneAdded = servicing.addDrone(4,  dronePos);
        assertTrue(droneAdded);

        boolean droneRemoved = servicing.removeDrone(4);
        assertTrue(droneRemoved);

        droneRemoved = servicing.removeDrone(4);
        assertFalse(droneRemoved);
    }

    @Test
    void addDuplicateDrone() {
        Position dronePos = new Position(2, 100);
        servicing.addDrone(5,  dronePos);
        boolean droneAdded = servicing.addDrone(5,  dronePos);
        assertFalse(droneAdded);
        servicing.removeDrone(5);
    }
}