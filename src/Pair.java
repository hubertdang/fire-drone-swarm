/**
 * Pair Class to emulate a tuple linking drone id to score
 */

public class Pair<Key, Value> {
    private final  Key first;
    private  Value second;

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

    /**
     * update the value associated with a key
     */
    public void setValue(Value value) {second = value; }

    /**
     * Checks if this Key is equal to another object, by using the keys equals method.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || first.getClass() != obj.getClass()) return false;
        return this.first.equals(obj);
    }

    /**
     * String representation of Pair with the <Key, Value> pair.
     *
     * @return string representation of Pair instance
     */
    @Override
    public String toString() { return "{" + first.toString() + " : " + second.toString() + "}"; }
}
