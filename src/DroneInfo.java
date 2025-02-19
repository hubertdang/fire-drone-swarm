/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo {
    int droneID;
    Position position;
    float agentTankAmount;
    DroneStateID stateID;
    Zone zoneToService;

    public DroneInfo(int droneID, DroneStateID stateID, Position position, float agentTankAmount, Zone zoneToService) {
        this.droneID = droneID;
        this.stateID = stateID;
        this.position = position;
        this.agentTankAmount = agentTankAmount;
        this.zoneToService = zoneToService;
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

    public float getCurrentZoneAgentTankAmount() {
        if (zoneToService == null) {
            return 0;
        }
        return zoneToService.getRequiredAgentAmount();
    }

    public String toString() {
        return "[DroneInfo: ID=" + droneID + ", state=" + stateID + ", position=(" + position.getX() + "," + position.getY() + ")" + ", agentTank=" + agentTankAmount + "]";
    }

}
