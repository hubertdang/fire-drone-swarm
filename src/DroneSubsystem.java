import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DroneSubsystem {

    private static ArrayList<DroneFault> droneFaults;

    public static void readFaultsFile(String filePath) throws IOException {
        String currentLine = null;
        boolean isHeader = true;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((currentLine = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = currentLine.split(",");
                int droneId = Integer.parseInt(data[0].trim());
                int faultCode = Integer.parseInt(data[1].trim());

                String[] time = data[2].trim().split(":");
                int hours = Integer.parseInt(time[0]);
                int minutes = Integer.parseInt(time[1]);
                int seconds = Integer.parseInt(time[2]);
                long faultTime = hours * 3600L + minutes * 60L + seconds; // actual time
                //long faultTime = minutes * 60L + seconds; // sped up time

                Faults faultType = Faults.valueOf(String.valueOf(data[3].trim()));
                droneFaults.add(new DroneFault(droneId, faultCode, faultTime, faultType));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        sortDroneFaults(droneFaults);
    }

    private static void sortDroneFaults(ArrayList<DroneFault> droneFaults) {
        droneFaults.sort((fault1, fault2) -> {
            long time1 = fault1.getFaultTime();
            long time2 = fault2.getFaultTime();
            return Long.compare(time1, time2);
        });
    }

    public ArrayList<DroneFault> getDroneFaults() {
        return droneFaults;
    }

    public void setDroneFaults(ArrayList<DroneFault> droneFaults) {
        this.droneFaults = droneFaults;
    }

    public static void main(String[] args) throws IOException {
        /* TODO: Find a way to not hardcode port numbers (maybe with static field and offsetting */
        Drone drone = new Drone(1, 5001);
        DroneController droneController = new DroneController(drone, 6001);
        Drone drone1 = new Drone(2, 5002);
        DroneController droneController1 = new DroneController(drone1, 6002);
        droneFaults = new ArrayList<>();

        readFaultsFile("./sample_input_files/faults.csv");
        sortDroneFaults(droneFaults);

        /*for (DroneFault fault : droneFaults) {
          System.out.println(fault.toString());
        }*/

        Thread droneThread = new Thread(drone, "🛫D");
        Thread droneControllerThread = new Thread(droneController, "🛫DC");

        Thread droneThread1 = new Thread(drone1, "🛫D");
        Thread droneControllerThread1 = new Thread(droneController1, "🛫DC2");

        droneThread.start();
        droneControllerThread.start();
        droneThread1.start();
        droneControllerThread1.start();

        while (true) {

            //System.out.println(getCurrentTime());
            for (DroneFault f : droneFaults) {
                //System.out.println(f.getFaultTime());
                if (f.getFaultTime() == getCurrentTime()) {
                    System.out.println(f.toString());
                    if (f.getFaultType().equals(Faults.NJ)) {

                    }
                    else if (f.getFaultType().equals(Faults.DSMF)) {

                    }
                    else if (f.getFaultType().equals(Faults.PLCM)) {

                    }
                    else {
                        System.out.println("Unknown Fault: " + f.getFaultType());
                    }
                }
            }
        }
    }

    /**
     * Get the current time in milliseconds
     *
     * @return the current time in milliseconds
     */
    private static long getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long offset = ZoneOffset.systemDefault()
                .getRules()
                .getOffset(java.time.Instant.ofEpochMilli(currentTimeMillis))
                .getTotalSeconds() * 1000L;
        return (currentTimeMillis + offset) % (24 * 60 * 60 * 1000L);
    }
}
