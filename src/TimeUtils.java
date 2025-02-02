/**
 * Formats timestamps for the fire drone swarm system.
 */
public class TimeUtils {
    public static String secondsToTimestamp(long milliseconds) {

        // Calculate minutes, seconds, and remaining milliseconds
        long minutes = milliseconds / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long remainingMilliseconds = milliseconds % 1000;

        // Format the result as "minutes:seconds:milliseconds"
        String formattedTime = String.format("%02d:%02d:%03d", minutes, seconds, remainingMilliseconds);
        return formattedTime;
    }
}
