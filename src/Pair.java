/**
 * Pair Class to emulate a tuple linking drone id to score
 */

public class Pair<Key, Value> {
    private final Key first;
    private final Value second;

    public Pair(Key first, Value second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Retrieve key of pair
     */
    public Key getKey() { return first; }

    /**
     * Retrieve value of pair
     */
    public Value getValue() { return second; }
}
