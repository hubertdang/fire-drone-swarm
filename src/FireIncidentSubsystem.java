import java.io.File;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class FireIncidentSubsystem implements Runnable {
    private Scheduler scheduler;
    private HashMap<Integer, Zone> clearZones;
    private HashMap<Integer, Zone> fireZones;
    private ArrayList<HashMap<String, Object>> events;
    public static final HashMap<FireSeverity,Integer> AGENT_AMOUNT = new HashMap<FireSeverity,Integer>() {{
        put(FireSeverity.NO_FIRE, 0);
        put(FireSeverity.LOW, 10);
        put(FireSeverity.MODERATE, 20);
        put(FireSeverity.HIGH, 30);
    }};


    public FireIncidentSubsystem(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.clearZones = new HashMap<Integer, Zone>();
        this.fireZones = new HashMap<Integer, Zone>(); // no fires when we initialize
        this.events = new ArrayList<>();

    }

    /**
     * Request a drone manually to be sent to a zone
     * @param zone the zone the drone will be sent to
     */
    public void manualReqDrone(Zone zone) {
        if(!isOnFire(zone)){
            System.out.println("Manual request for drone at zone: "+zone.getId());
            clearZones.remove(zone.getId());
            fireZones.put(zone.getId(), zone);
            scheduler.handleFireReq(zone);
        }
        else{
            System.out.println("Zone "+zone.getId()+" is already on fire");
        }
    }

    /**
     * Detect a fire at a zone and send a request to the scheduler
     * @param zone the zone the drone will be sent to
     */
    public void detectFire(Zone zone){
        if(!isOnFire(zone)){
            System.out.println("Fire detected at zone: "+zone.getId());
            clearZones.remove(zone.getId());
            fireZones.put(zone.getId(), zone);
            scheduler.handleFireReq(zone);
        }
        else{
            System.out.println("Zone "+zone.getId()+" is already on fire");
        }
    }

    /**
     * Read the simulation event file and add the events to the arraylist of events
     * @param eventFile the event file to be read
     */
    public void readSimEventFile(File eventFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(eventFile))) {
            String line = br.readLine(); // skip the header of the file
            while ((line = br.readLine()) != null) {
                String[] eventData = line.trim().split("\\s+");
                long time = timeToMillis(eventData[0].trim());
                int zoneId = Integer.parseInt(eventData[1].trim());
                String eventType = eventData[2].trim();
                String severity = eventData[3].trim();

                HashMap<String, Object> event = new HashMap<>();
                event.put("time", time);
                event.put("zoneId", zoneId);
                event.put("eventType", eventType);
                event.put("severity", severity);
                events.add(event);
            }
            sortEventsByTime(events);
            System.out.println("Events added successfully from event file");
        } catch (IOException e) {
            System.err.println("Error reading zone file: " + e.getMessage());
        }
    }

    /**
     * Read the simulation zone file and add the zones to the hashmap of zones
     * @param zoneFile the zone file containing the zones
     */
    public void readSimZoneFile(File zoneFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(zoneFile))) {
            String line = br.readLine(); // skip the header of the file
            while ((line = br.readLine()) != null) {
                String[] zoneData = line.trim().split("\\s+");
                int id = Integer.parseInt(zoneData[0].trim());
                int startX = Integer.parseInt(zoneData[1].trim());
                int startY = Integer.parseInt(zoneData[2].trim());
                int endX = Integer.parseInt(zoneData[3].trim());
                int endY = Integer.parseInt(zoneData[4].trim());

                // Find center of zone
                int centerX = (startX + endX) / 2;
                int centerY = (startY + endY) / 2;
                clearZones.put(id,new Zone(id,new Position(centerX, centerY),0));
            }
            System.out.println("Zones added successfully from zone file");
        } catch (IOException e) {
            System.err.println("Error reading zone file: " + e.getMessage());
        }
    }

    /**
     * Run the simulation
     */
    @Override
    public void run() {
        System.out.println("FireIncidentSubsystem is starting the simulation");
        simStartFire();
    }

    /**
     * Start the simulation, send events to the scheduler and check if the fires have been put out
     */
    public void simStartFire(){
        long startTime = getCurrentTime();
        int eventIndex = 0;
        System.out.println("Simulation started at: "+startTime);

        while(!fireZones.isEmpty() || eventIndex < events.size()){
            long currentTime = getCurrentTime();
            long eventIndexTime=0;
            if(eventIndex < events.size()){ // Check if we have reached the end of the events
                eventIndexTime = (long) events.get(eventIndex).get("time");
            }
            System.out.println("Current time: "+currentTime);
            System.out.println("Event index time: "+eventIndexTime);

            // Check if we need to send an event and send event to scheduler
            while(eventIndex < events.size() && eventIndexTime <= currentTime){
                System.out.println("Sending event to scheduler");
                sendEvent(events.get(eventIndex));
                eventIndex++;
                currentTime = getCurrentTime();
                if(events.size() == eventIndex){ // Check if we have reached the end of the events
                    break;
                }
                eventIndexTime = (long) events.get(eventIndex).get("time");
            }

            // Check out the fire zones to see if the fires have been put out
            pollFireZones();

            // Sleep to avoid busy waiting
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error in simulation: "+e.getMessage());
            }
        }
        System.out.println("Simulation completed. All fires have been extinguished.");
    }

    /**
     * Poll the fire zones to see if the fires have been put out
     */
    private void pollFireZones(){
        System.out.println("Polling fire zones");
        for (Zone zone : fireZones.values()) {
            if (zone.getSeverity() == FireSeverity.NO_FIRE) {
                fireZones.remove(zone.getId());
                clearZones.put(zone.getId(), zone);
            }
        }
        if(fireZones.isEmpty()){
            System.out.println("No fires have been found");
        }
    }

    /**
     * Send an event to the scheduler
     * @param event the event being sent to the scheduler
     */
    private void sendEvent(HashMap<String, Object> event){
        String eventType = (String) event.get("eventType");
        String severity = (String) event.get("severity");
        int zoneId = (int) event.get("zoneId");
        Zone zone = clearZones.get(zoneId);
        FireSeverity fireSeverity = FireSeverity.valueOf(severity.toUpperCase());
        clearZones.get(zoneId).setSeverity(fireSeverity);
        clearZones.get(zoneId).setRequiredAgentAmount(AGENT_AMOUNT.get(fireSeverity));

        if (eventType.equals("FIRE_DETECTED")) {
            detectFire(zone);
        } else if (eventType.equals("DRONE_REQUEST")) {
            manualReqDrone(zone);
        }
        else{
            System.out.println("Invalid event type: "+eventType);
        }
    }

    /**
     * Check if a zone is on fire
     * @param zone the zone that is being checked
     * @return true if the zone is on fire, false otherwise
     */
    private boolean isOnFire(Zone zone) {
        return fireZones.containsKey(zone.getId());
    }

    /**
     * Sort the events that were read from the file by time
     * @param events the events hashmap to be sorted
     */
    private void sortEventsByTime(ArrayList<HashMap<String, Object>> events) {
        events.sort((event1, event2) -> {
            long time1 = (long) event1.get("time");
            long time2 =  (long) event2.get("time");
            return Long.compare(time1, time2);
        });
    }

    /**
     * Convert time to milliseconds
     * @param time the time to be converted
     * @return the time in milliseconds
     */
    private long timeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return (hours * 3600L + minutes * 60L + seconds) * 1000L;
    }

    /**
     * Get the current time in milliseconds
     * @return the current time in milliseconds
     */
    private long getCurrentTime(){
        long currentTimeMillis= System.currentTimeMillis();
        long offset = ZoneOffset.systemDefault()
                .getRules()
                .getOffset(java.time.Instant.ofEpochMilli(currentTimeMillis))
                .getTotalSeconds() * 1000L;
        return (currentTimeMillis + offset) % (24 * 60 * 60 * 1000L);
    }
    public static void main(String[] args) {
        FireIncidentSubsystem fireIncidentSubsystem = new FireIncidentSubsystem(new Scheduler());
        fireIncidentSubsystem.readSimZoneFile(new File("./sample_input_files/zones.csv"));
        fireIncidentSubsystem.readSimEventFile(new File("./sample_input_files/events.csv"));
        fireIncidentSubsystem.run();
    }
}