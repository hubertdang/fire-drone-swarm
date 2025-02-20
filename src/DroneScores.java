import java.util.ArrayList;

/**
 * Drone Scores Class
 * A class to act as an ordered collection drone scores used by the Scheduler.
 *  Ordered list of <DroneID|Score>
 */
public class DroneScores {
    private final ArrayList<Pair<Integer, Float>> scores;

    public DroneScores() {
        scores = new ArrayList<>();
    }

    /**
     * Adds drone score to an ordered list.
     * @param droneScore a pair containing drone id and drone score
     */
    public void add(Pair<Integer, Float> droneScore) {
        int i = 0;
        while (i < scores.size() && scores.get(i).getValue() < droneScore.getValue()) { i++; }
        scores.add(i, droneScore);
    }

    /**
     * getScores retrieves ordered list of scores.
     * @return ordered list of scores
     */
    public ArrayList<Pair<Integer, Float>> getScores() { return scores; }

    /**
     * String representation of DroneScores
     *
     * @return string representation of DroneScores instance
     */
    @Override
    public String toString() { return scores.toString(); }
}
