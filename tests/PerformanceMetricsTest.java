import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

public class PerformanceMetricsTest {

    @Test
    public void testGetExtinguishedTimes() {
        HashMap<Integer, Zone> clearZones = new HashMap<>();
        ArrayList<UISubsystem.FireRow> fireRows = new ArrayList<>();


        Zone zone1 = new Zone(1, 99, 0, 0, 0, 0);
        Zone zone2 = new Zone(2, 99,10,10,10,10); // Zone with ID 2
        clearZones.put(1, zone1);
        clearZones.put(2, zone2);

        UISubsystem.FireRow row1 = new UISubsystem.FireRow();
        row1.zoneId = 1;
        row1.appearedAtMillis = TimeUtils.getCurrentTime() - 5000;
        fireRows.add(row1);

        UISubsystem.FireRow row2 = new UISubsystem.FireRow();
        row2.zoneId = 2;
        row2.appearedAtMillis = TimeUtils.getCurrentTime() - 10000;
        fireRows.add(row2);

        UISubsystem.FireRow row3 = new UISubsystem.FireRow();
        row3.zoneId = 3;
        row3.appearedAtMillis = TimeUtils.getCurrentTime() - 15000;
        fireRows.add(row3);

        // Get extinguished times
        for (Zone zone : clearZones.values()) {
            for (UISubsystem.FireRow row : fireRows) {
                if (row.zoneId == zone.getId() && row.extinguishedTime == null) {
                    long now = TimeUtils.getCurrentTime();
                    row.extinguishedTime = TimeUtils.millisecondsToTimestamp((long) ((now - row.appearedAtMillis)));
                }
            }
        }


        assertEquals("00:00:05 ", row1.extinguishedTime, "Row1 extinguished time should be 5 seconds.");
        assertTrue(row2.extinguishedTime.compareTo("00:00:10") >= 0 && row2.extinguishedTime.compareTo("00:00:12") <= 0, "Row2 extinguished time should be between 10 and 12 seconds.");
    }

    @Test
    public void testThroughPut() {
        HashMap<Integer, Zone> clearZones = new HashMap<>();
        ArrayList<UISubsystem.FireRow> fireRows = new ArrayList<>();


        for (int i = 1; i <= 10; i++) {
            Zone zone = new Zone(i, 99, 0, 0, 0, 0);
            clearZones.put(i, zone);

            UISubsystem.FireRow row = new UISubsystem.FireRow();
            row.zoneId = i;
            row.appearedAtMillis = TimeUtils.getCurrentTime() - (i * 1000); // Staggered appearance times
            fireRows.add(row);
        }

        long startTime = TimeUtils.getCurrentTime();

        for (Zone zone : clearZones.values()) {
            for (UISubsystem.FireRow row : fireRows) {
                if (row.zoneId == zone.getId() && row.extinguishedTime == null) {
                    long now = TimeUtils.getCurrentTime();
                    row.extinguishedTime = TimeUtils.millisecondsToTimestamp((long) ((now - row.appearedAtMillis)));
                }
            }
        }

        long endTime = TimeUtils.getCurrentTime();
        long elapsedTimeMillis = endTime - startTime;

        System.out.println("Throughput: " + clearZones.size() + " zones serviced in " + elapsedTimeMillis + " ms");
        assertTrue(elapsedTimeMillis <= 5000, "Throughput time exceeded acceptable limit!");
    }




}