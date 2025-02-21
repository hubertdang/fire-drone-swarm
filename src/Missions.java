import java.util.LinkedHashMap;
import java.util.Map;

public class Missions {
    /**
     * A list containing all hazardous zones and associated scores of all drones for each zone
     */
    private LinkedHashMap<Zone, DroneScores> missions;

    /**
     * Constructor for the Missions class. Constructs a list of missions.
     */
    public Missions() {
        missions = new LinkedHashMap<>();
    }

    /**
     * Adds an event to missions if zone is unique, else updates the drone affinities for specified
     * zone.
     *
     * @param zone the Zone on fire
     * @param scores the drone scores associated with this drone
     */
    public void updateQueue(Zone zone, DroneScores scores) {
        missions.put(zone, scores);
    }

    /**
     * replaces current missions LinkedHashMap with a new one.
     * @param newMissions the new missions to replace current ones
     */
    public void replaceMissions(LinkedHashMap<Zone, DroneScores> newMissions) {
        this.missions = newMissions;
    }

    /**
     * Removes an event from missions, fire extinguished
     *
     * @param zone the zone to be removed from active missions
     */
    public boolean remove(Zone zone) {
        System.out.println("[" + Thread.currentThread().getName()
                + "]: Zone " + zone.getId() + " removed from active missions");
        DroneScores retScores = missions.remove(zone);
        return retScores != null;
    }


    /**
     * Checks if the mission queue is empty
     *
     * @return true if mission queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return missions.isEmpty();
    }

    /**
     * Returns the list of missions
     *
     * @return the missions linked list
     */
    public LinkedHashMap<Zone,DroneScores> getMissions() { return this.missions; }

    /**
     * String representation of missions with the <Zone, DroneScores> pair.
     *
     * @return string representation of missions instance.
     */
    @Override
    public String toString() { return missions.toString(); }

}