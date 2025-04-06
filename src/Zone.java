import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a zone in a 2D space with a unique ID, center position, fire severity,
 * and required agent amount for firefighting.
 */
public class Zone implements Serializable {
    private final int id;
    private final Position position;
    private FireSeverity severity;
    private float requiredAgentAmount;

    private final int startx;
    private final int starty;
    private final int endx;
    private final int endy;

    /**
     * Constructs a Zone with a given ID, required agent amount, and boundaries.
     * The center position is calculated as the midpoint of the given coordinates.
     *
     * @param id                  the unique identifier of the zone
     * @param requiredAgentAmount the amount of agent required for firefighting
     * @param startX              the starting x-coordinate of the zone
     * @param endX                the ending x-coordinate of the zone
     * @param startY              the starting y-coordinate of the zone
     * @param endY                the ending y-coordinate of the zone
     */
    public Zone(int id, float requiredAgentAmount, int startX, int endX, int startY, int endY) {
        this.id = id;
        this.requiredAgentAmount = requiredAgentAmount;
        this.startx = startX;
        this.starty = startY;
        this.endx = endX;
        this.endy = endY;
        this.severity = FireSeverity.NO_FIRE;

        float centerX = (float) (startX + endX) / 2;
        float centerY = (float) (startY + endY) / 2;
        this.position = new Position(centerX, centerY);
    }

    /**
     * Gets the severity level of the fire in this zone.
     *
     * @return the fire severity level
     */
    public synchronized FireSeverity getSeverity() {
        return this.severity;
    }

    /**
     * Sets the severity level of the fire in this zone.
     *
     * @param severity the fire severity level
     */
    public synchronized void setSeverity(FireSeverity severity) {
        this.severity = severity;
            }

    /**
     * Gets the center position of this zone.
     *
     * @return the center position of the zone
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Gets the unique identifier of this zone.
     *
     * @return the ID of the zone
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the required amount of agent needed for firefighting in this zone.
     *
     * @return the required agent amount
     */
    public synchronized float getRequiredAgentAmount() {
        return this.requiredAgentAmount;
    }

    /**
     * Sets the required amount of agent needed for firefighting in this zone.
     *
     * @param requiredAgentAmount the amount of agent required
     */
    public synchronized void setRequiredAgentAmount(float requiredAgentAmount) {
        this.requiredAgentAmount = requiredAgentAmount;
    }

    /**
     * Checks if this zone is equal to another object. Two zones are equal if they have the same ID,
     * required agent amount, center position, and severity level.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Zone zone = (Zone) obj;
        return this.id == zone.getId();
    }

    /**
     * Generates hash code for a zone based on its zone id
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * String representation of Zone
     *
     * @return string representation of zone instance
     */
    @Override
    public String toString() { return "Zone[id: " + id + " Required Agent: "
            + requiredAgentAmount + "]"; }

    public int[] getZoneCoordinates() {
        return new int[] { this.startx, this.starty, this.endx, this.endy };
    }

    public Color getZoneColor() {
        return switch (getSeverity()) {
            case HIGH -> new Color(255, 0, 0, 50);
            case MODERATE -> new Color(255, 165, 0, 50);
            case LOW -> new Color(255, 255, 0, 50);
            default -> new Color(0, 128, 0, 50);
        };
    }

}