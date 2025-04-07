import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling and simulating time in a controlled environment.
 * Allows accelerated or decelerated simulation using a time factor.
 */
public class TimeUtils {
    public static final float TIME_FACTOR = 50;
    private static final String RELATIVE_START_TIME = "17:04:10";

    private static long startSimTimeMillis = 0;
    private static long startNanoTime = 0;

    /**
     * Initializes the simulation by setting the start time.
     * The simulated time will start from {@link #RELATIVE_START_TIME},
     * and the real-world reference time will be stored using {@link System#nanoTime()}.
     */
    public static void setOffset() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime targetTime = LocalTime.parse(RELATIVE_START_TIME, formatter);
        startSimTimeMillis = targetTime.toSecondOfDay() * 1000L;
        startNanoTime = System.nanoTime();
    }

    /**
     * Returns the current simulated time in milliseconds since midnight.
     * This is calculated using the simulation start time and the real-world
     * time elapsed, scaled by {@link #TIME_FACTOR}.
     *
     * @return Simulated current time in milliseconds since midnight.
     */
    public static long getCurrentTime() {
        long elapsedNanos = System.nanoTime() - startNanoTime;
        long elapsedMillis = (long) ((elapsedNanos / 1_000_000.0) * TIME_FACTOR);
        return startSimTimeMillis + elapsedMillis;
    }

    /**
     * Returns the current simulated time as a formatted timestamp.
     * Format: HH:mm:ss
     *
     * @return Simulated timestamp string.
     */
    public static String getCurrentTimestamp() {
        return millisecondsToTimestamp(getCurrentTime());
    }

    /**
     * Converts a time in milliseconds since midnight into a timestamp string.
     *
     * @param milliseconds Milliseconds since midnight.
     * @return Formatted timestamp string in HH:mm:ss.
     */
    public static String millisecondsToTimestamp(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        return String.format("%02d:%02d:%02d ", hours, minutes, seconds);
    }

    /**
     * Converts a time string in the format HH:mm:ss to milliseconds since midnight.
     *
     * @param timeStr Time string (e.g., "13:45:30").
     * @return Milliseconds since midnight.
     */
    public static long csvTimeToMillis(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
        return time.toSecondOfDay() * 1000L;
    }
}
