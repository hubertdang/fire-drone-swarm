import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DroneSubsystem {

    private static final int NUMBER_OF_DRONES = 2;
    private static ArrayList<DroneFault> droneFaults; // a list of drone faults that will be injected
    private static HashMap<Integer, Drone> drones;

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

                FaultID faultType = FaultID.valueOf(String.valueOf(data[3].trim()));
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
        drones = new HashMap<>();
        droneFaults = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_DRONES; i++) {
            Drone drone = new Drone();
            DroneController controller = new DroneController(drone);
            drones.put(drone.getId(), drone);

            Thread droneThread = new Thread(drone, "🛫D");
            Thread controllerThread = new Thread(controller, "🛫DC" + drone.getId());

            droneThread.start();
            controllerThread.start();
        }

        readFaultsFile("./sample_input_files/faults.csv");

        // a loop that injects faults when fault time is reached
        while (!droneFaults.isEmpty()) {
            System.out.println("#AASHNA FAULT HERE?");
            Iterator<DroneFault> iterator = droneFaults.iterator();
            while (iterator.hasNext()) {
                DroneFault currentFault = iterator.next();
                System.out.println(currentFault.getFaultTime() + " | " + getCurrentTime());

                if (currentFault.getFaultTime() <= getCurrentTime()) {
                    System.out.println(currentFault.getFaultTime() + " | " + getCurrentTime() + " #AASHNA FAULT DETECTED");
                    //System.out.println(currentFault.toString());

                    Drone affectedDrone = drones.get(currentFault.getDroneId());
                    if (affectedDrone != null) {
                        affectedDrone.setFault(currentFault.getFaultType());
                    } else {
                        System.out.println("No drone found with ID: " + currentFault.getDroneId());
                    }

                    iterator.remove();
                }
            }
            try {
                Thread.sleep(1000); // check every second
            } catch (InterruptedException e) {
                e.printStackTrace();
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
