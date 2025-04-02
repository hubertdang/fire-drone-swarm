/**
 * Represents a tank that stores a liquid agent, with the ability to open and close a nozzle
 * to release the agent at a specified rate.
 */
public class AgentTank {
    public static final float CAPACITY = 11.0f;       // L
    public static final float AGENT_DROP_RATE = 2.0f; // L/sec
    public static final long NOZZLE_TIME = 2000;      // sec
    private float currAgentAmount;
    private boolean isNozzleOpen;

    /**
     * Constructs an AgentTank with a full capacity of agent and a closed nozzle.
     */
    public AgentTank() {
        this.refill();
        this.isNozzleOpen = false;
    }

    /**
     * Opens the nozzle, allowing the agent to be released.
     */
    public synchronized void openNozzle() {
        try {
            Thread.sleep(NOZZLE_TIME);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.isNozzleOpen = true;
    }

    /**
     * Closes the nozzle, stopping the release of the agent.
     */
    public synchronized void closeNozzle() {
        try {
            Thread.sleep(NOZZLE_TIME);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.isNozzleOpen = false;
    }

    /**
     * Checks if the nozzle is currently open.
     *
     * @return true if the nozzle is open, false otherwise.
     */
    public synchronized boolean isNozzleOpen() {
        return this.isNozzleOpen;
    }

    /**
     * Refills the tank to its maximum capacity.
     */
    public synchronized void refill() {
        this.currAgentAmount = CAPACITY;
    }

    /**
     * Checks if the tank is empty.
     *
     * @return true if the current agent amount is zero or less, false otherwise.
     */
    public synchronized boolean isEmpty() {
        return this.getCurrAgentAmount() <= 0;
    }

    /**
     * Gets the current amount of agent remaining in the tank.
     *
     * @return The current agent amount in liters.
     */
    public synchronized float getCurrAgentAmount() {
        return this.currAgentAmount;
    }

    /**
     * Decreases the amount of agent in the tank by a specified amount.
     * The agent is released at a rate of 1 liter per second, meaning the function
     * will wait proportionally based on the amount of agent being released.
     * <p>
     * If the nozzle is closed or the tank is empty, no agent will be released.
     *
     * @param amount The amount to decrease in liters.
     */
    public synchronized void decreaseAgent(float amount) {
        if (this.isNozzleOpen() && !this.isEmpty()) {
            if (this.currAgentAmount < amount) {
                this.currAgentAmount = 0.0f;
            }
            else {
                this.currAgentAmount -= amount;
            }
        }
    }
}
