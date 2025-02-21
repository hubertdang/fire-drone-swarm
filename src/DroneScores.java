import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Drone Scores Class
 * A class to act as an ordered collection drone scores used by the Scheduler.
 *  Ordered list of <DroneID|Score>
 */
public class DroneScores {
    private final ArrayList<AbstractMap.Entry<Integer, Float>> scores;

    public DroneScores() {
        scores = new ArrayList<>();
    }

    /**
     * Adds drone score to an ordered list.
     * @param droneId the id of drone
     * @param droneScore the score of drone corresponding to a zone
     */
    public void add(Integer droneId, Float droneScore) {
        int i = 0;
        while (i < scores.size() && scores.get(i).getValue() < droneScore) { i++; }
        scores.add(i, new AbstractMap.SimpleEntry<>(droneId, droneScore));
    }

    /**
     * getScores retrieves ordered list of scores.
     * @return ordered list of scores
     */
    public ArrayList<Map.Entry<Integer, Float>> getScores() { return scores; }

    /**
     * String representation of DroneScores
     *
     * @return string representation of DroneScores instance
     */
    @Override
    public String toString() { return scores.toString(); }
}
