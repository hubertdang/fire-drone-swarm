import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static long offsetMilliseconds = 0;

    /**
     * Converts a duration in milliseconds to a timestamp in HH:mm:ss:SSS format.
     *
     * @param milliseconds Duration since midnight.
     * @return Timestamp string in HH:mm:ss:SSS format.
     */
    public static String millisecondsToTimestamp(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long remainingMilliseconds = milliseconds % 1000;

        return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, remainingMilliseconds);
    }

    /**
     * Sets the time offset so that the simulated time aligns with a target timestamp.
     *
     * @param targetTimeStr A string in HH:mm:ss format representing the desired start time.
     */
    public static void setOffset(String targetTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime targetTime = LocalTime.parse(targetTimeStr, formatter);
        long targetMillis = targetTime.toSecondOfDay() * 1000L;

        long actualMillis = LocalTime.now().toSecondOfDay() * 1000L;

        offsetMilliseconds = targetMillis - actualMillis;
        System.out.println("Offset set to: " + offsetMilliseconds + "ms");
    }

    /**
     * Returns the simulated current time in milliseconds since midnight.
     * This adds the offset to the actual system time.
     *
     * @return Adjusted time in milliseconds since midnight.
     */
    public static long getCurrentTime() {
        long nowMillis = LocalTime.now().toSecondOfDay() * 1000L;
        return nowMillis + offsetMilliseconds;
    }

    /**
     * Returns the current simulated time as a string timestamp.
     *
     * @return Timestamp in HH:mm:ss:SSS format.
     */
    public static String getCurrentTimestamp() {
        return millisecondsToTimestamp(getCurrentTime());
    }
}