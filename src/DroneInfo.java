/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo {
    int droneID;
    Position position;
    float agentTankAmount;

    public DroneInfo(int droneID, Position position, float agentTankAmount) {
        this.droneID = droneID;
        this.position = position;
        this.agentTankAmount = agentTankAmount;
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
        return "[DroneInfo: ID=" + droneID + ", position=(" + position.getX() + "," + position.getY() + ")" + ", agentTank=" + agentTankAmount + "]";
    }

}
