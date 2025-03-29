import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class DroneSubsystemTest {
    private ArrayList<DroneFault> faults;

    @BeforeEach
    void setUp() {
        faults = new ArrayList<>();
        faults.add(new DroneFault(1, 2, 14 * 3600 + 34 * 60 + 24, Faults.NOZZLE_JAMMED)); // 52441
        faults.add(new DroneFault(2, 3, 14 * 3600 + 34 * 60 + 1, Faults.CORRUPTED_MESSAGE)); // 52464
        faults.add(new DroneFault(1, 1, 14 * 3600 + 34 * 60 + 47, Faults.DRONE_STUCK)); // 52487
        faults.sort(Comparator.comparingLong(DroneFault::getFaultTime));

    }

    @Test
    void testFaultContent() {
        DroneFault f1 = faults.get(0);
        assertEquals(2, f1.getDroneId());
        assertEquals(3, f1.getFaultCode());
        assertEquals(52441L, f1.getFaultTime());
        assertEquals(Faults.CORRUPTED_MESSAGE, f1.getFaultType());

        DroneFault f2 = faults.get(1);
        assertEquals(1, f2.getDroneId());
        assertEquals(2, f2.getFaultCode());
        assertEquals(52464L, f2.getFaultTime());
        assertEquals(Faults.NOZZLE_JAMMED, f2.getFaultType());

        DroneFault f3 = faults.get(2);
        assertEquals(1, f3.getDroneId());
        assertEquals(1, f3.getFaultCode());
        assertEquals(52487L, f3.getFaultTime());
        assertEquals(Faults.DRONE_STUCK, f3.getFaultType());
    }

    @Test
    void testSortingByTime() {

        assertEquals(52441L, faults.get(0).getFaultTime());
        assertEquals(52464L, faults.get(1).getFaultTime());
        assertEquals(52487L, faults.get(2).getFaultTime());
    }

    @Test
    void testToStringFormat() {
        DroneFault f = faults.get(0);
        String expected = "Fault[DroneId = 2, Fault Code = 3, Fault Time = 52441, Fault Type = CORRUPTED_MESSAGE]";
        assertEquals(expected, f.toString());
    }
}