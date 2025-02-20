/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo {
    final int droneID;
    final Position position;
    final float agentTankAmount;
    final DroneStateID stateID;
    final Zone zoneToService;

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

    /**
     * Checks if this DroneInfo is equal to another object. Two DroneInfos are equal if they have
     * the same ID, agent amount, position, and stateID.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DroneInfo droneInfo = (DroneInfo) obj;
        return this.droneID == droneInfo.getDroneID() &&
                Float.compare(droneInfo.agentTankAmount, this.getAgentTankAmount()) == 0 &&
                this.position.equals(droneInfo.getPosition()) &&
                this.stateID == droneInfo.getStateID();
    }
}
