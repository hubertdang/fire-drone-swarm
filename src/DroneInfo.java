import java.io.Serializable;
import java.util.ArrayList;

/**
 * Drone information Class
 * A class to pass drone's info from DroneSubsystem to Scheduler
 */

public class DroneInfo implements Serializable {
    final int droneID;
    final Position position;
    final float agentTankAmount;
    final DroneStateID stateID;
    final Zone zoneToService;
    final FaultID  fault;
    final float releasedAgentAmount;

    public DroneInfo(int droneID, DroneStateID stateID, Position position, float agentTankAmount, Zone zoneToService, FaultID fault, float releasedAgentAmount) {
        this.droneID = droneID;
        this.stateID = stateID;
        this.position = position;
        this.agentTankAmount = agentTankAmount;
        this.zoneToService = zoneToService;
        this.fault = fault;
        this.releasedAgentAmount = releasedAgentAmount;
    }

    public float getReleasedAgentAmount() {
        return releasedAgentAmount;
    }

    public FaultID  getFault() {
        return fault;
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

    public Zone getZoneToService() {
        return zoneToService;
    }

    /**
     * droneInfoLookUp returns the ID specified Drone Info object from a list
     *
     * @param droneId  the id of drone to be looked up
     * @param infoList a list of DroneInfo objects
     * @return desired DroneInfo object
     */
    public static DroneInfo droneInfoLookUp(int droneId, ArrayList<DroneInfo> infoList) {
        for (DroneInfo info : infoList) {
            if (info.droneID == droneId) { return info; }
        }
        return null;
    }

    @Override
    public String toString() {
        return "[DroneInfo: | ID = " + this.droneID
                + " | STATE = " + this.stateID
                + " | POSITION = " + this.position
                + " | AGENT = " + this.agentTankAmount + "]";
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
