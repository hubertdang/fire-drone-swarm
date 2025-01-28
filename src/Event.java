public class Event {
    private long time;
    private int zoneId;
    private String eventType;
    private String severity;

    public Event(long time, int zoneId, String eventType, String severity) {
        this.time = time;
        this.zoneId = zoneId;
        this.eventType = eventType;
        this.severity = severity;
    }

    public long getTime() {
        return time;
    }

    public int getZoneId() {
        return zoneId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return "Event[Time=" + time + ", ZoneID=" + zoneId + ", Type=" + eventType + ", Severity=" + severity + "]";
    }
}
