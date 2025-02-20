import java.util.ArrayList;

public class Missions {
    /**
     * A list containing all hazardous zones and associated scores of all drones for each zone
     */
    private ArrayList<Pair<Zone, DroneScores>> missions;

    /**
     * Constructor for the Missions class. Constructs a list of missions.
     */
    public Missions() {
        missions = new ArrayList<>();
    }

    /**
     * Adds an event to missions if zone is unique, else updates the drone affinities for specified
     * zone.
     *
     * @param missionsEntry an entry to the list of active missions
     */
    public void updateQueue(Pair<Zone, DroneScores> missionsEntry) {
        if (missions.contains(missionsEntry)) {
            int index = missions.indexOf(missionsEntry);
            missions.get(index).setValue(missionsEntry.getValue());
        } else {
            missions.add(missionsEntry);
        }
    }

    /**
     * replaces current missions Arraylist with a new one.
     * @param newMissions the new missions to replace current ones
     */
    public void replaceMissions(ArrayList<Pair<Zone, DroneScores>> newMissions) {
        this.missions = newMissions;
    }

    /**
     * Removes an event from missions, fire extinguished
     *
     * @param zone the zone to be removed from active missions
     */
    public boolean remove(Zone zone) { return missions.remove(zone); }


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
     * @return the missions list
     */
    public ArrayList<Pair<Zone,DroneScores>> getMissions() { return this.missions; }

    /**
     * String representation of missions with the <Zone, DroneScores> pair.
     *
     * @return string representation of missions instance.
     */
    @Override
    public String toString() { return missions.toString(); }

}