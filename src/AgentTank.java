public class AgentTank {
    private static final float CAPACITY = 11.0f;       // 11 L
    private static final float AGENT_DROP_RATE = 1.0f; // 1 L/sec
    private static final float NOZZLE_TIME = 2.0f;     // 2 sec

    private float currAgentAmount;
    private boolean isNozzleOpen;

    public AgentTank() {
        this.refill();
        this.closeNozzle();
    }

    public void openNozzle() {
        this.isNozzleOpen = true;
    }

    public void closeNozzle() {
        this.isNozzleOpen = false;
    }

    public boolean isNozzleOpen() {
        return this.isNozzleOpen;
    }

    public void refill() {
        this.currAgentAmount = CAPACITY;
    }

    public boolean isEmpty() {
        return this.getCurrAgentAmount() <= 0;
    }

    public float getCurrAgentAmount() {
        return this.currAgentAmount;
    }

    public void decreaseAgent(float amount) {
        if (!this.isEmpty()){
            this.currAgentAmount -= amount;
        }

        this.isEmpty();
    }
}
