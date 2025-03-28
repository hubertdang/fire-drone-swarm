import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DroneSubsystem {

    // a list of drone faults that will be injected
    private static ArrayList<DroneFault> droneFaults;

    /**
     * Reads a file containing drone faults and adds them to arraylist droneFaults.
     *
     * @param filePath The path to the file containing drone faults
     * @throws IOException
     */
    public static void readFaultsFile(String filePath) throws IOException {
        String currentLine = null;
        boolean isHeader = true;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((currentLine = br.readLine()) != null) {
                if (isHeader) { // skip the header line of the file
                    isHeader = false;
                    continue;
                }

                String[] data = currentLine.split(","); // get data from each line
                int droneId = Integer.parseInt(data[0].trim());
                int faultCode = Integer.parseInt(data[1].trim());

                String[] time = data[2].trim().split(":");
                int hours = Integer.parseInt(time[0]);
                int minutes = Integer.parseInt(time[1]);
                int seconds = Integer.parseInt(time[2]);
                // convert fault time to seconds since midnight
                long faultTime = hours * 3600L + minutes * 60L + seconds;

                Faults faultType = Faults.valueOf(String.valueOf(data[3].trim()));
                droneFaults.add(new DroneFault(droneId, faultCode, faultTime, faultType));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        sortDroneFaults(droneFaults); // sort droneFaults arraylist in ascending order of faultTime
    }

    /**
     * Method for sorting arraylist droneFaults in ascending order of faultTime.
     *
     * @param droneFaults the arraylist containing drone faults
     */
    private static void sortDroneFaults(ArrayList<DroneFault> droneFaults) {
        droneFaults.sort((fault1, fault2) -> {
            long time1 = fault1.getFaultTime();
            long time2 = fault2.getFaultTime();
            return Long.compare(time1, time2);
        });
    }

    public static void main(String[] args) throws IOException {
        /* TODO: Find a way to not hardcode port numbers (maybe with static field and offsetting */
        Drone drone = new Drone(1, 5001);
        DroneController droneController = new DroneController(drone, 6001);
        Drone drone1 = new Drone(2, 5002);
        DroneController droneController1 = new DroneController(drone1, 6002);
        droneFaults = new ArrayList<>(); // initialize the arraylist for storing drone faults

        // read the file containing drone faults and store all faults in the arraylist droneFaults
        readFaultsFile("./sample_input_files/faults.csv");

        Thread droneThread = new Thread(drone, "🛫D");
        Thread droneControllerThread = new Thread(droneController, "🛫DC");

        Thread droneThread1 = new Thread(drone1, "🛫D");
        Thread droneControllerThread1 = new Thread(droneController1, "🛫DC2");

        droneThread.start();
        droneControllerThread.start();
        droneThread1.start();
        droneControllerThread1.start();

        // a loop that injects faults when fault time is reached
        while (true) {
            for (DroneFault f : droneFaults) {
                if (f.getFaultTime() == getCurrentTime()) {
                    System.out.println(f.toString());
                    DroneFault currentFault = droneFaults.remove(droneFaults.indexOf(f));

                    if (f.getFaultType().equals(Faults.NJ)) {
                        // actions to be taken when nozzle jams
                        /*TODO: Actions for Nozzle Jam Fault*/
                    }
                    else if (f.getFaultType().equals(Faults.DSMF)) {
                        // actions to be taken when drone is stuck mid-flight
                        /*TODO: Actions for Drone Stuck Mid-Flight Fault*/
                    }
                    else if (f.getFaultType().equals(Faults.PLCM)) {
                        // actions to be taken when there is a packet loss or corrupted message
                        /*TODO: Actions for Packet Loss or Corrupted Message Fault*/
                    }
                    else {
                        System.out.println("Unknown Fault: " + f.getFaultType());
                    }
                }
            }
        }
    }

    /**
     * Get the current time in seconds passed since midnight
     *
     * @return the current time in seconds passed since midnight
     */
    private static long getCurrentTime() {
        LocalTime now = LocalTime.now(); // Get current local time
        return now.toSecondOfDay();
    }

    /**
     * Returns the arraylist droneFaults.
     *
     * @return droneFaults
     */
    public ArrayList<DroneFault> getDroneFaults() {
        return droneFaults;
    }

    /**
     * Sets the arraylist droneFaults.
     *
     * @param droneFaults empty arraylist that will be used to store drone faults
     */
    public void setDroneFaults(ArrayList<DroneFault> droneFaults) {
        this.droneFaults = droneFaults;
    }
}
