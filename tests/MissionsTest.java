import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MissionsTest {
    private Missions missionQueue;

    @BeforeEach
    public void setUp() {
        missionQueue = new Missions();
    }

    @Test
    public void testQueue() {
        Zone zone = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone.setSeverity(FireSeverity.LOW);
        assertTrue(missionQueue.isEmpty());
        //missionQueue.queue(zone);
        //assertEquals(missionQueue.getMissions().get(2).size(), 1);
        //assertEquals(missionQueue.peek(), zone);
    }

    @Test
    public void testPeek() {
        Zone zone1 = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone1.setSeverity(FireSeverity.LOW);
        //missionQueue.queue(zone1);

        Zone zone2 = new Zone(2, 40.0F, 60, 90, 80, 100);
        zone2.setSeverity(FireSeverity.HIGH);
        //missionQueue.queue(zone2);

        //assertEquals(missionQueue.peek(), zone2);
    }

    @Test
    public void testPop() {
        Zone zone1 = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone1.setSeverity(FireSeverity.HIGH);
        //missionQueue.queue(zone1);

        Zone zone2 = new Zone(2, 40.0F, 60, 90, 80, 100);
        zone2.setSeverity(FireSeverity.LOW);
        //missionQueue.queue(zone2);

        //assertEquals(missionQueue.pop(), zone1);
        //assertEquals(missionQueue.getMissions().get(0).size(), 0);
        //assertEquals(missionQueue.getMissions().get(2).size(), 1);
        //ToDo
    }

    @Test
    public void testIsEmpty() {
        assertTrue(missionQueue.isEmpty());
        Zone zone1 = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone1.setSeverity(FireSeverity.LOW);
        //missionQueue.queue(zone1);
        //assertFalse(missionQueue.isEmpty());
        //missionQueue.pop();
        //assertTrue(missionQueue.isEmpty());
        //ToDo
    }

    @Test
    public void testGetMissions() {
        Zone zone1 = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone1.setSeverity(FireSeverity.LOW);
        //missionQueue.queue(zone1);

//        Map<Integer, ArrayList<Zone>> missions = new HashMap<Integer, ArrayList<Zone>>();
//        for (int i = 0; i < 3; i++) {
//            missions.put(i, new ArrayList<>());
//        }
//
//        missions.put(2, new ArrayList<>());
//        missions.get(2).add(zone1);
//
//        assertEquals(missionQueue.getMissions(), missions);
        //ToDo
    }

    @Test
    public void testEquals() {
        Zone zone1 = new Zone(1, 50.0F, 0, 60, 0, 80);
        zone1.setSeverity(FireSeverity.LOW);
//        missionQueue.queue(zone1);
//
//        Missions missionQueue2 = new Missions();
//        missionQueue2.queue(zone1);
//        assertEquals(missionQueue, missionQueue2);
        //ToDo
    }
}


