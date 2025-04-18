/**
 * SimEvent class represents an event that occurs in the simulation.
 */
public class SimEvent {
    private final long time;
    private final int zoneId;
    private final String eventType;
    private final FireSeverity severity;

    /**
     * Constructs a new Event with the given time, zone ID, event type, and severity.
     *
     * @param time      the time the event occurred
     * @param zoneId    the ID of the zone where the event occurred
     * @param eventType the type of the event
     * @param severity  the severity of the event
     */
    public SimEvent(long time, int zoneId, String eventType, String severity) {
        this.time = time;
        this.zoneId = zoneId;
        this.eventType = eventType;
        // convert string to enum
        this.severity = FireSeverity.valueOf(severity.toUpperCase());
    }

    /**
     * Returns the time the event occurred.
     *
     * @return the time the event occurred
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the ID of the zone where the event occurred.
     *
     * @return the ID of the zone where the event occurred
     */
    public int getZoneId() {
        return zoneId;
    }

    /**
     * Returns the type of the event.
     *
     * @return the type of the event
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the severity of the event.
     *
     * @return the severity of the event
     */
    public FireSeverity getSeverity() {
        return severity;
    }

    /**
     * Returns a string representation of the event.
     *
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        return "Event[Time=" + time + ", ZoneID=" + zoneId + ", Type=" + eventType + ", Severity=" + severity +
                ", Location=" + "]";
    }
}
