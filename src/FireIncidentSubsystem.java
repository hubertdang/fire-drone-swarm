import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;


public class FireIncidentSubsystem implements Runnable {
    public static final HashMap<FireSeverity, Integer> AGENT_AMOUNT = new HashMap<FireSeverity, Integer>() {{ // Amount of agents required in liters for each fire severity
        put(FireSeverity.NO_FIRE, 0);
        put(FireSeverity.LOW, 10);
        put(FireSeverity.MODERATE, 20);
        put(FireSeverity.HIGH, 30);
    }};
    private final FireIncidentBuffer fireBuffer;
    private final HashMap<Integer, Zone> clearZones;
    private final HashMap<Integer, Zone> fireZones;
    private final ArrayList<SimEvent> events;

    /**
     * Constructs a FireIncidentSubsystem with the given fireBuffer.
     *
     * @param fireBuffer the scheduler to be used
     */
    public FireIncidentSubsystem(FireIncidentBuffer fireBuffer) {
        this.fireBuffer = fireBuffer;
        this.clearZones = new HashMap<Integer, Zone>();
        this.fireZones = new HashMap<Integer, Zone>(); // no fires when we initialize
        this.events = new ArrayList<SimEvent>();
    }

    /**
     * Request a drone manually to be sent to a zone
     *
     * @param zone      the zone the drone will be sent to
     * @param eventTime the time the event occurred
     * @param eventType the type of event that has occurred
     */
    private void manualReqDrone(Zone zone, long eventTime, String eventType) {
        if (!isOnFire(zone)) {
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "🤖Manual request for drone at zone: " + zone.getId());
            clearZones.remove(zone.getId());
            fireZones.put(zone.getId(), zone);
            fireBuffer.addEventMessage(zone);
        }
        else {
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "🔥Zone " + zone.getId() + " is already on fire");
        }
    }

    /**
     * Detect a fire at a zone and send a request to the scheduler
     *
     * @param zone      the zone the drone will be sent to
     * @param eventTime the time the event occurred
     */
    private void trackFire(Zone zone, long eventTime) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + "🔥Fire detected at zone: " + zone.getId());
        clearZones.remove(zone.getId());
        fireZones.put(zone.getId(), zone);
        fireBuffer.addEventMessage(zone);
    }

    /**
     * Read the simulation event file and add the events to the arraylist of events
     *
     * @param eventFile the event file to be read
     */
    public void readSimEventFile(File eventFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(eventFile))) {
            String line = br.readLine(); // skip the header of the file
            while ((line = br.readLine()) != null) {
                String[] eventData = line.split(",");
                long time = timeToMillis(eventData[0].trim());
                int zoneId = Integer.parseInt(eventData[1].trim());
                String eventType = eventData[2].trim();
                String severity = eventData[3].trim();

                SimEvent event = new SimEvent(time, zoneId, eventType, severity);
                events.add(event);
            }
            sortEventsByTime(events);
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "Events added successfully from event file");
        }
        catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "]: " + "‼️Error reading zone file: " + e.getMessage());
        }
        catch (NumberFormatException e) {
            System.err.println("[" + Thread.currentThread().getName() + "]: " + "‼️Error parsing event data: " + e.getMessage());
        }
    }

    /**
     * Read the simulation zone file and add the zones to the hashmap of zones
     *
     * @param zoneFile the zone file containing the zones
     */
    public void readSimZoneFile(File zoneFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(zoneFile))) {
            String line = br.readLine(); // skip the header of the file
            while ((line = br.readLine()) != null) {
                String[] zoneData = line.split(",");
                int id = Integer.parseInt(zoneData[0].trim());

                String[] startCoords = zoneData[1].replace("(", "").replace(")", "").split(";");
                int startX = Integer.parseInt(startCoords[0].trim());
                int startY = Integer.parseInt(startCoords[1].trim());

                String[] endCoords = zoneData[2].replace("(", "").replace(")", "").split(";");
                int endX = Integer.parseInt(endCoords[0].trim());
                int endY = Integer.parseInt(endCoords[1].trim());

                clearZones.put(id, new Zone(id, 0, startX, endX, startY, endY));
            }
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "Zones added successfully from zone file");
        }
        catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "]: " + "Error reading zone file: " + e.getMessage());
        }
    }

    /**
     * Run the simulation
     */
    @Override
    public void run() {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + "FireIncidentSubsystem is starting the simulation");
        simStart();
    }

    /**
     * Start the simulation, send events to the scheduler and check if the fires have been put out
     */
    public void simStart() {
        long startTime = getCurrentTime();
        int eventIndex = 0;
        long eventIndexTime = 0;
        System.out.println("[" + Thread.currentThread().getName() + "]: " + "Simulation started at: "
                + TimeUtils.secondsToTimestamp(startTime));

        while (hasActiveFiresOrUpcomingEvents(eventIndex)) {
            long currentTime = getCurrentTime();
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "Current time: "
                    + TimeUtils.secondsToTimestamp(currentTime));
            if (eventIndex < events.size()) { // Check if we have reached the end of the events
                eventIndexTime = events.get(eventIndex).getTime();
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "Next event time: "
                        + TimeUtils.secondsToTimestamp(eventIndexTime));
            }


            // Check if we need to send an event and send event to scheduler
            while (isEventReadyToProcess(eventIndex, eventIndexTime, currentTime)) {
                System.out.println("[" + Thread.currentThread().getName() + "]: " + "Sending event to scheduler");
                sendEvent(events.get(eventIndex));
                eventIndex++;
                currentTime = getCurrentTime();
                if (events.size() == eventIndex) { // Check if we have reached the end of the events
                    break;
                }
                eventIndexTime = events.get(eventIndex).getTime();
            }

            // check buffer for acknowledgement that fire has been put out
            if (fireBuffer.newAcknowledgement()) {
                Zone servicedZone = fireBuffer.popAcknowledgementMessage();
                if (servicedZone.getSeverity() == FireSeverity.NO_FIRE) {
                    clearZones.put(servicedZone.getId(), servicedZone);
                    fireZones.remove(servicedZone.getId());
                    System.out.println("FireIncidentSubsystem: Zone: " + servicedZone.getId()
                            + "'s fire has been extinguished.");
                }
            }

            // sleep polling thread to allow other threads to run
            try {
                sleep(5000);
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Simulation completed. All fires have been extinguished.");

    }

    /**
     * Check if there are active fires or upcoming events
     *
     * @param eventIndex the index to be used in the events arraylist
     * @return true if there are active fires or upcoming events, false otherwise
     */
    private boolean hasActiveFiresOrUpcomingEvents(int eventIndex) {
        boolean firesStillActive = !fireZones.isEmpty(); // Check if there are active fires
        boolean eventsRemaining = eventIndex < events.size(); // Check if there are more events to process
        return firesStillActive || eventsRemaining;
    }

    /**
     * Check if an event is ready to be processed
     *
     * @param eventIndex     the current index of the events arraylist
     * @param eventIndexTime the time the current indexed event should take place
     * @param currentTime    the current time of the system
     * @return true if the event is ready to be processed, false otherwise
     */
    public boolean isEventReadyToProcess(int eventIndex, long eventIndexTime, long currentTime) {
        boolean hasPendingEvents = eventIndex < events.size(); // Check if there are events left to process
        boolean isEventTimeReached = hasPendingEvents && eventIndexTime <= currentTime; // Check if the current event's time has been reached
        return hasPendingEvents && isEventTimeReached;
    }

    /**
     * Send an event to the scheduler
     *
     * @param event the event being sent to the scheduler
     */
    private void sendEvent(SimEvent event) {
        String eventType = event.getEventType();
        FireSeverity severity = event.getSeverity();
        int zoneId = event.getZoneId();
        long eventTime = event.getTime();
        Zone zone = clearZones.get(zoneId);
        zone.setSeverity(severity);
        zone.setRequiredAgentAmount(AGENT_AMOUNT.get(severity));

        if (eventType.equals("FIRE_DETECTED")) {
            trackFire(zone, eventTime);
        }
        else if (eventType.equals("DRONE_REQUEST")) {
            manualReqDrone(zone, eventTime, "DRONE_REQUEST");
        }
        else {
            System.out.println("[" + Thread.currentThread().getName() + "]: " + "Invalid event type: " + eventType);
        }
    }

    /**
     * Check if a zone is on fire
     *
     * @param zone the zone that is being checked
     * @return true if the zone is on fire, false otherwise
     */
    private boolean isOnFire(Zone zone) {
        return fireZones.containsKey(zone.getId());
    }

    /**
     * Sort the events that were read from the file by time
     *
     * @param events the events hashmap to be sorted
     */
    private void sortEventsByTime(ArrayList<SimEvent> events) {
        events.sort((event1, event2) -> {
            long time1 = event1.getTime();
            long time2 = event2.getTime();
            return Long.compare(time1, time2);
        });
    }

    /**
     * Convert time to milliseconds
     *
     * @param time the time to be converted
     * @return the time in milliseconds
     */
    private long timeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        // temp to speed upn simulation
        //return (hours * 3600L + minutes * 60L + seconds) * 1000L;
        return minutes * 60L + seconds;
    }

    /**
     * Get the current time in milliseconds
     *
     * @return the current time in milliseconds
     */
    private long getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long offset = ZoneOffset.systemDefault()
                .getRules()
                .getOffset(java.time.Instant.ofEpochMilli(currentTimeMillis))
                .getTotalSeconds() * 1000L;
        return (currentTimeMillis + offset) % (24 * 60 * 60 * 1000L);
    }

    /**
     * Getters for unit test
     */
    public HashMap<Integer, Zone> getClearZones() {
        return clearZones;
    }

    public ArrayList<SimEvent> getEvents() {
        return events;
    }

    public HashMap<Integer, Zone> getFireZones() {
        return fireZones;
    }
}