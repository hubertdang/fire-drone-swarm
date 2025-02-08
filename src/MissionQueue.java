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
     * Adds an event to the correct position in the mission queue
     *
     * @param zone
     */
    public void queue(Zone zone) {
        if (zone.getSeverity() == FireSeverity.HIGH) {
            missions.get(HIGH_SEVERITY).add(zone);
            //Collections.sort(missions.get(HIGH_SEVERITY)); --need to add a compareTo method in zone class for sorting zones by required agent amount
        }
        else if (zone.getSeverity() == FireSeverity.MODERATE) {
            missions.get(MODERATE_SEVERITY).add(zone);
            //Collections.sort(missions.get(MODERATE));
        }
        else if (zone.getSeverity() == FireSeverity.LOW) {
            missions.get(LOW_SEVERITY).add(zone);
            //Collections.sort(missions.get(LOW));
        }
    }

    /**
     * Returns the event with the highest severity and highest required agent amount
     * from the mission queue without removing it.
     *
     * @return event with highest severity and required agent amount
     */
    public Zone peek() {
        if (!missions.get(HIGH_SEVERITY).isEmpty()) {
            return missions.get(HIGH_SEVERITY).get(0);
        }
        else if (!missions.get(MODERATE_SEVERITY).isEmpty()) {
            return missions.get(MODERATE_SEVERITY).get(0);
        }
        else if (!missions.get(LOW_SEVERITY).isEmpty()) {
            return missions.get(LOW_SEVERITY).get(0);
        }
        else {
            return null;
        }
    }

    /**
     * Removes the SimEvent with the highest priority and highest required agent amount
     *
     * @return event with highest severity and required agent amount
     */
    public Zone pop() {
        if (!missions.get(HIGH_SEVERITY).isEmpty()) {
            return missions.get(HIGH_SEVERITY).remove(0);
        }
        else if (!missions.get(MODERATE_SEVERITY).isEmpty()) {
            return missions.get(MODERATE_SEVERITY).remove(0);
        }
        else if (!missions.get(LOW_SEVERITY).isEmpty()) {
            return missions.get(LOW_SEVERITY).remove(0);
        }
        else {
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

    /**
     * Returns the missions hashmap of the mission queue
     *
     * @return the missions hashmap
     */
    public Map<Integer, ArrayList<Zone>> getMissions() {
        return this.missions;
    }

    /**
     * Checks if two mission queues are equal.
     * Two MissionQueue objects are equal if they have the same key and value pairs.
     *
     * @param obj, the mission queue to compare
     * @return true if mission queues are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MissionQueue missionQueue = (MissionQueue) obj;
        return this.getMissions().size() == missionQueue.getMissions().size()
                && this.getMissions().get(0).equals(missionQueue.getMissions().get(0))
                && this.getMissions().get(1).equals(missionQueue.getMissions().get(1))
                && this.getMissions().get(2).equals(missionQueue.getMissions().get(2));
    }
}