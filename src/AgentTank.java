public class AgentTank {
    private static final float CAPACITY = 11.0f;       // 11 L
    private static final float AGENT_DROP_RATE = 1.0f; // 1 L/sec
    private static final float NOZZLE_TIME = 2.0f;     // 2 sec

    private float currAgentAmount;
    private boolean nozzlesOpen;

    public AgentTank() {
    }

    public void openNozzle() {
    }

    public void closeNozzle() {
    }

    public boolean isEmpty() {
        return false;
    }

    public float getCurrAgentAmount() {
        return 0;
    }

    public void decreaseAgent(float amount) {
    }
}
