/**
 * Represents a position in a 2D space with x and y coordinates.
 */
public class Position {
    private float x;
    private float y;

    /**
     * Constructs a new Position with the given x and y coordinates.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     */
    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of the position.
     *
     * @return the x-coordinate as a float
     */
    public float getX() {
        return this.x;
    }

    /**
     * Returns the y-coordinate of the position.
     *
     * @return the y-coordinate as a float
     */
    public float getY() {
        return this.y;
    }

    /**
     * Updates the position with new x and y coordinates.
     *
     * @param x the new x-coordinate
     * @param y the new y-coordinate
     */
    public void update(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the Euclidean distance between this position and another position.
     *
     * @param position the target position
     * @return the distance between this position and the given position as a float
     */
    public float distanceFrom(Position position) {
        float xDiff = position.getX() - this.x;
        float yDiff = position.getY() - this.y;
        return (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }
}