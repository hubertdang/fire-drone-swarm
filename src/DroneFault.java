public class DroneFault {
    private final int droneId;
    private final int faultCode;
    private final long faultTime;
    private final Faults faultType;

    public DroneFault(int droneId, int faultCode, long faultTime, Faults faultType) {
        this.droneId = droneId;
        this.faultCode = faultCode;
        this.faultTime = faultTime;
        this.faultType = faultType;
    }

    public int getDroneId() {
        return droneId;
    }

    public int getFaultCode() {
        return faultCode;
    }

    public long getFaultTime() {
        return faultTime;
    }

    public Faults getFaultType() {
        return faultType;
    }

    @Override
    public String toString() {
        return "Fault[DroneId = " + droneId + ", Fault Code = " + faultCode + ", Fault Time = " +
                faultTime + ", Fault Type = " + faultType + "]";
    }
}
