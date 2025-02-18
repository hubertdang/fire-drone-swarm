/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo {
    int droneID;
    Position position;
    float agentTankAmount;
    DroneStateID stateID;

    public DroneInfo(int droneID, DroneStateID stateID, Position position, float agentTankAmount) {
        this.droneID = droneID;
        this.stateID = stateID;
        this.position = position;
        this.agentTankAmount = agentTankAmount;
    }

    public DroneStateID getStateID() {
        return stateID;
    }

    public int getDroneID() {
        return droneID;
    }

    public Position getPosition() {
        return position;
    }

    public float getAgentTankAmount() {
        return agentTankAmount;
    }

    public String toString() {
        return "[DroneInfo: ID=" + droneID + ", state=" + stateID + ", position=(" + position.getX() + "," + position.getY() + ")" + ", agentTank=" + agentTankAmount + "]";
    }

}
