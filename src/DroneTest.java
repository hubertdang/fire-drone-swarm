import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DroneTest {
    private Drone drone;


    @BeforeEach
    void setUp() {
        DroneBuffer buffer = new DroneBuffer();

        drone = new Drone(1, buffer);

    }

    @Test
    void getPosition() {
        assertEquals(0, drone.getPosition().getX());
        assertEquals(0, drone.getPosition().getY());
    }

    @Test
    void getId() {
        assertEquals(1, drone.getId());
    }


    @Test
    void getStatus() {
        assertEquals(DroneStatus.BASE, drone.getStatus());
    }

    @Test
    void setStatus() {
        drone.setStatus(DroneStatus.ENROUTE);
        assertEquals(DroneStatus.ENROUTE, drone.getStatus());

        drone.setStatus(DroneStatus.IDLE);
        assertEquals(DroneStatus.IDLE, drone.getStatus());
    }

}