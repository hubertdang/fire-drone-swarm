import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DroneTest {
    private Drone drone;


    @BeforeEach
    void testSetUp() {
        DroneBuffer buffer = new DroneBuffer();

        //drone = new Drone(1);
        //ToDo

    }

    @Test
    void testGetPosition() {
        assertEquals(0, drone.getPosition().getX());
        assertEquals(0, drone.getPosition().getY());
    }

    @Test
    void testGetId() {
        assertEquals(1, drone.getId());
    }


//    @Test
//    void testGetStatus() {
//        assertEquals(DroneStatus.BASE, drone.getStatus());
//    }
    //ToDo

//    @Test
//    void testSetStatus() {
//        drone.setStatus(DroneStatus.ENROUTE);
//        assertEquals(DroneStatus.ENROUTE, drone.getStatus());
//
//        drone.setStatus(DroneStatus.IDLE);
//        assertEquals(DroneStatus.IDLE, drone.getStatus());
//    }
    // ToDo

    @Test
    void testSetZoneToServiceAndGetZoneToService() {
        Zone testZone = new Zone(99, 10f, 0, 50, 0, 50);
        drone.setZoneToService(testZone);

        assertEquals(testZone, drone.getZoneToService());
    }

//    @Test
//    void testEquals() {
//        Drone sameDrone = drone;
//        Drone diffDrone = new Drone(2);
//
//        assertEquals(drone, sameDrone, "Drones with same ID should be equal.");
//
//        assertNotEquals(drone, diffDrone, "Drones with different IDs should not be equal.");
//    }
//ToDo
    //@Test
//    void testToString() {
//        assertEquals("[Drone#1, status=BASE, pos=(0.0,0.0)]", drone.toString());
//        drone.setStatus(DroneStatus.ENROUTE);
//        assertEquals("[Drone#1, status=ENROUTE, pos=(0.0,0.0)]", drone.toString());
//    }

    //ToDo

}