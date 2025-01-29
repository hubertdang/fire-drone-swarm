/**
 * Represents a zone in a 2D space with a unique ID, center position, fire severity,
 * and required agent amount for firefighting.
 */
public class Zone {
    private int id;
    private Position position;
    private FireSeverity severity;
    private float requiredAgents;

    /**
     * Constructs a Zone with a given ID, required agent amount, and boundaries.
     * The center position is calculated as the midpoint of the given coordinates.
     *
     * @param id the unique identifier of the zone
     * @param requiredAgents the amount of agent required for firefighting
     * @param startX the starting x-coordinate of the zone
     * @param endX the ending x-coordinate of the zone
     * @param startY the starting y-coordinate of the zone
     * @param endY the ending y-coordinate of the zone
     */
    public Zone(int id, float requiredAgents, int startX, int endX, int startY, int endY) {
        this.id = id;
        this.requiredAgents = requiredAgents;

        float centerX = (float)(startX + endX) / 2;
        float centerY = (float)(startY + endY) / 2;
        this.position = new Position( centerX, centerY);
    }

    /**
     * Sets the severity level of the fire in this zone.
     *
     * @param severity the fire severity level
     */
    public void setSeverity(FireSeverity severity) {
        this.severity = severity;
    }

    /**
     * Gets the severity level of the fire in this zone.
     *
     * @return the fire severity level
     */
    public FireSeverity getSeverity() {
        return this.severity;
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
     * Sets the required amount of agent needed for firefighting in this zone.
     *
     * @param requiredAgents the amount of agent required
     */
    public void setRequiredAgents(float requiredAgents) {
        this.requiredAgents = requiredAgents;
    }

    /**
     * Gets the required amount of agent needed for firefighting in this zone.
     *
     * @return the required agent amount
     */
    public float getRequiredAgents() {
        return this.requiredAgents;
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
        return id == zone.id &&
                Float.compare(zone.requiredAgents, requiredAgents) == 0 &&
                position.equals(zone.position) &&
                severity == zone.severity;
    }
}