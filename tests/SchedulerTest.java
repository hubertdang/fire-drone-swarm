import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {
    private Scheduler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new Scheduler();
    }

    @Test
    public void testPutZoneOnFire() {
        Zone zone = new Zone(1, 80.0f, 0, 50, 0, 40);
        scheduler.putZoneOnFire(zone);
        assertFalse(scheduler.getZonesOnFire().isEmpty());
        assertTrue(scheduler.getZonesOnFire().containsKey(zone));
    }

    @Test
    public void testScheduleDrones() {
        Zone zone1 = new Zone(1, 80.0f, 0, 50, 0, 40);
        zone1.setSeverity(FireSeverity.MODERATE);
        Zone zone2 = new Zone(2, 100.0f, 50, 100, 0, 50);
        zone2.setSeverity(FireSeverity.HIGH);
        scheduler.putZoneOnFire(zone1);
        scheduler.putZoneOnFire(zone2);

        Drone drone = new Drone();
        ArrayList<DroneInfo> droneInfoList = new ArrayList<>();
        droneInfoList.add(new DroneInfo(drone.getId(), drone.getCurrStateID(), drone.getPosition(), drone.getAgentTankAmount(), drone.getZoneToService(), drone.getFault()));

        scheduler.scheduleDrones(droneInfoList);

        DroneActionsTable droneActionsTable = scheduler.getDroneActionsTable();
        assertEquals(DroneTaskType.SERVICE_ZONE, droneActionsTable.getAction(1).getTaskType());
        assertEquals(zone2, droneActionsTable.getAction(1).getZone());
    }
}