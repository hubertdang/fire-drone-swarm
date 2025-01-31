import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MissionQueue {
    public static final int HIGH_SEVERITY = 0;
    public static final int MODERATE_SEVERITY = 1;
    public static final int LOW_SEVERITY = 2;
    private static final int SEVERITIES = 3;

    /**
     * A map containing missions ordered by severity and required agent amount
     * keys: represent severity as mentioned at the top as static final integers
     * values: an array list containing zones ordered by required agent amount
     */
    private final Map<Integer, ArrayList<Zone>> missions;

    /**
     * Constructor for the MissionQueue class. Constructs a mission queue.
     */
    public MissionQueue() {
        missions = new HashMap<>();
        for (int i = 0; i < SEVERITIES; i++) {
            missions.put(i, new ArrayList<>());
        }
    }

    /**
     * Adds a zone to the correct position in the mission queue
     *
     * @param zone
     */
    public void queue(Zone zone) {
        if (zone.getSeverity() == FireSeverity.HIGH) {
            missions.get(HIGH_SEVERITY).add(zone);
            //Collections.sort(missions.get(HIGH_SEVERITY)); --need to add a compareTo method in zone class for sorting zones by required agent amount
        } else if (zone.getSeverity() == FireSeverity.MODERATE) {
            missions.get(MODERATE_SEVERITY).add(zone);
            //Collections.sort(missions.get(MODERATE));
        } else if (zone.getSeverity() == FireSeverity.LOW) {
            missions.get(LOW_SEVERITY).add(zone);
            //Collections.sort(missions.get(LOW));
        }
    }

    /**
     * Returns the zone with the highest severity and highest required agent amount
     * from the mission queue without removing it.
     *
     * @return zone with highest severity and required agent amount
     */
    public Zone peek() {
        if (!missions.get(HIGH_SEVERITY).isEmpty()) {
            return missions.get(HIGH_SEVERITY).getFirst();
        } else if (!missions.get(MODERATE_SEVERITY).isEmpty()) {
            return missions.get(MODERATE_SEVERITY).getFirst();
        } else if (!missions.get(LOW_SEVERITY).isEmpty()) {
            return missions.get(LOW_SEVERITY).getFirst();
        } else {
            return null;
        }
    }

    /**
     * Removes the zone with the highest priority and highest required agent amount
     *
     * @return zone with highest severity and required agent amount
     */
    public Zone pop() {
        if (!missions.get(HIGH_SEVERITY).isEmpty()) {
            return missions.get(HIGH_SEVERITY).removeFirst();
        } else if (!missions.get(MODERATE_SEVERITY).isEmpty()) {
            return missions.get(MODERATE_SEVERITY).removeFirst();
        } else if (!missions.get(LOW_SEVERITY).isEmpty()) {
            return missions.get(LOW_SEVERITY).removeFirst();
        } else {
            return null;
        }
    }

    /**
     * Checks if the mission queue is empty
     *
     * @return true if mission queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return missions.get(HIGH_SEVERITY).isEmpty() && missions.get(MODERATE_SEVERITY).isEmpty() && missions.get(LOW_SEVERITY).isEmpty();
    }
}