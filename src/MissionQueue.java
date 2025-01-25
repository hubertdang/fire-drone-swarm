import java.util.Queue;

public class MissionQueue {
    public static final int HIGH_SEVERITY = 0;
    public static final int MODERATE_SEVERITY = 1;
    public static final int LOW_SEVERITY = 2;

    private Queue<Queue<Zone>> missions;

    public MissionQueue() {
    }

    public void queue(Zone zone) {
    }

    public Zone peek() {
        return null;
    }

    public Zone pop() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }
}