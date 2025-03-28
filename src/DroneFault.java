import java.sql.Time;

/**
 * DroneFault class represents faults that will be injected in the drone.
 */
public class DroneFault {
    private final int droneId;
    private final int faultCode;
    private final long faultTime;
    private final Faults faultType;

    /**
     * Constructor for Drone Faults.
     * Constructs a new fault.
     *
     * @param droneId   id of drone that is associated with the fault
     * @param faultCode the code of the fault
     * @param faultTime then time when fault is supposed to occur
     * @param faultType the type of fault
     */
    public DroneFault(int droneId, int faultCode, long faultTime, Faults faultType) {
        this.droneId = droneId;
        this.faultCode = faultCode;
        this.faultTime = faultTime;
        this.faultType = faultType;
    }

    /**
     * Returns the droneId
     *
     * @return droneId
     */
    public int getDroneId() {
        return droneId;
    }

    /**
     * Returns the fault code
     *
     * @return faultCode
     */
    public int getFaultCode() {
        return faultCode;
    }

    /**
     * Returns the time when fault occurs
     *
     * @return faultTime
     */
    public long getFaultTime() {
        return faultTime;
    }

    /**
     * Returns the type of fault i.e.
     * Nozzle Jam, or
     * Drone Stuck Mid-Flight, or
     * Packet Loss/Corrupted Message
     *
     * @return faultType
     */
    public Faults getFaultType() {
        return faultType;
    }

    /**
     * Returns a String representation of the fault
     *
     * @return String representation of the fault
     */
    @Override
    public String toString() {
        return "Fault[DroneId = " + droneId + ", Fault Code = " + faultCode + ", Fault Time = " +
                faultTime + ", Fault Type = " + faultType + "]";
    }
}
