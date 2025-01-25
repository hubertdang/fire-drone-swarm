import java.io.File;
import java.util.ArrayList;

public class FireIncidentSubsystem implements Runnable {
    private Scheduler scheduler;
    private ArrayList<Zone> clearZones;
    private ArrayList<Zone> fireZones;

    public FireIncidentSubsystem(Scheduler scheduler, ArrayList<Zone> zones) {
    }

    public void manualReqDrone(Zone zone) {
    }

    public void readSimEventFile(File eventFile) {
    }

    public void readSimZoneFile(File zoneFile) {
    }

    @Override
    public void run() {
    }

    public void simStartFire() {
    }

    public boolean isOnFire(Zone zone) {
        return false;
    }
}