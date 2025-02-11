/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo {
    int droneID;
    DroneState droneState;
    Position position;
    float agentTankAmount;

    public DroneInfo(int droneID, DroneState droneState, Position position, float agentTankAmount) {
        this.droneID = droneID;
        this.droneState = droneState;
        this.position = position;
        this.agentTankAmount = agentTankAmount;
    }

    public int getDroneID() {
        return droneID;
    }

    public DroneState getDroneState() {
        return droneState;
    }

    public void setDroneState(DroneState droneState) {
        this.droneState = droneState;
    }

    public Position getPosition() {
        return position;
    }

    public float getAgentTankAmount() {
        return agentTankAmount;
    }

    public String toString() {
        return "[DroneInfo: ID=" + droneID + ", state=" + droneState + ", position=(" + position.getX() + "," + position.getY() + ")" + ", agentTank=" + agentTankAmount + "]";
    }

}
