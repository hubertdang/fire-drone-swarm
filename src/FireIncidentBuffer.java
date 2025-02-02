import java.util.ArrayList;

/**
 * FireIncidentBuffer Class
 * A class in the fire drone swarm system designed to facilitate communication between the scheduler and the
 * fire incident subsystem.
 */

public class FireIncidentBuffer {
    private final ArrayList<Zone> eventMessages;
    private final ArrayList<Zone> acknowledgmentMessages;

    /**
     * Constructs a shared buffer used for message passing.
     */
    public FireIncidentBuffer() {
        this.acknowledgmentMessages = new ArrayList<>();
        this.eventMessages = new ArrayList<>();
    }

    /**
     * Retrieves an event message from the buffer.
     *
     * @return an event object
     */
    public synchronized Zone popEventMessage() {
        Zone eventMessage = eventMessages.remove(0);
        notifyAll();
        return eventMessage;
    }

    /**
     * Retrieves an acknowledgement message from the buffer.
     *
     * @return a String describing what event is now being acknowledged as completed
     */
    public synchronized Zone popAcknowledgementMessage() {
        Zone acknowledgementMessage = acknowledgmentMessages.remove(0);
        notifyAll();
        return acknowledgementMessage;
    }

    /**
     * Adds acknowledgement message to buffer.
     *
     * @param zoneMessage the message to be added to buffer
     */
    public synchronized void addAcknowledgementMessage(Zone zoneMessage) {
        acknowledgmentMessages.add(zoneMessage);
        notifyAll();
    }

    /**
     * Adds event message to buffer.
     *
     * @param event the message to be added to buffer
     */
    public synchronized void addEventMessage(Zone event) {
        eventMessages.add(event);
        notifyAll();
    }

    /**
     * newEvent signifies if there are any additions to event list
     *
     * @return true if eventMessages not empty, false otherwise
     */
    public synchronized boolean newEvent() {
        return !eventMessages.isEmpty();
    }

    /**
     * newAcknowledgement signifies if there are any additions to event list
     *
     * @return true if acknowledgementMessages not empty, false otherwise
     */
    public synchronized boolean newAcknowledgement() {
        return !acknowledgmentMessages.isEmpty();
    }
}
