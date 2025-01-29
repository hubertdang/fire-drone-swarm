import java.util.*;

public class MissionQueue {
    public static final int HIGH_SEVERITY = 0;
    public static final int MODERATE_SEVERITY = 1;
    public static final int LOW_SEVERITY = 2;

    private Map<Integer, ArrayList<Zone>> missions;

    public MissionQueue() {
        missions = new HashMap<>();
        for (int i = 0; i < 3; i++){
            missions.put(i, new ArrayList<>());
        }
    }

    public void queue(Zone zone) {
        if (zone.getSeverity() == FireSeverity.HIGH){
            missions.get(HIGH_SEVERITY).add(zone);
            //Collections.sort(missions.get(HIGH_SEVERITY)); --need to add a compareTo method in zone class for sorting zones by required agent amount
        }
        else if (zone.getSeverity() == FireSeverity.MODERATE){
            missions.get(MODERATE_SEVERITY).add(zone);
            //Collections.sort(missions.get(MODERATE));
        }
        else if (zone.getSeverity() == FireSeverity.LOW) {
            missions.get(LOW_SEVERITY).add(zone);
            //Collections.sort(missions.get(LOW));
        }
        else{
            ;
        }
    }

    public Zone peek() {
        if (missions.get(HIGH_SEVERITY).size() != 0){
            return missions.get(HIGH_SEVERITY).getFirst();
        }
        else if (missions.get(MODERATE_SEVERITY).size() != 0){
            return missions.get(MODERATE_SEVERITY).getFirst();
        }
        else if (missions.get(LOW_SEVERITY).size() != 0) {
            return missions.get(LOW_SEVERITY).getFirst();
        }
        else {
            return null;
        }
    }

    public Zone pop() {
        if (missions.get(HIGH_SEVERITY).size() != 0){
            return missions.get(HIGH_SEVERITY).removeFirst();
        }
        else if (missions.get(MODERATE_SEVERITY).size() != 0){
            return missions.get(MODERATE_SEVERITY).removeFirst();
        }
        else if (missions.get(LOW_SEVERITY).size() != 0) {
            return missions.get(LOW_SEVERITY).removeFirst();
        }
        else{
            return null;
        }
    }

    public boolean isEmpty() {
        if (missions.get(HIGH_SEVERITY).isEmpty() && missions.get(MODERATE_SEVERITY).isEmpty() && missions.get(LOW_SEVERITY).isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }
}