import java.util.Objects;

public class Zone {
    private int id;
    private Position position;
    private FireSeverity severity;
    private float requiredAgentAmount;

    public Zone(Position position, float requiredAgentAmount) {
        this.position = position;
        this.requiredAgentAmount = requiredAgentAmount;
    }

    public void setSeverity(FireSeverity severity) {
        this.severity = severity;
    }

    public FireSeverity getSeverity() {
        return this.severity;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setRequiredAgentAmount(float amountRequired) {
        this.requiredAgentAmount = amountRequired;
    }

    public float getRequiredAgent() {
        return this.requiredAgentAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Zone zone = (Zone) obj;
        return id == zone.id &&
                Float.compare(zone.requiredAgentAmount, requiredAgentAmount) == 0 &&
                Objects.equals(position, zone.position) &&
                severity == zone.severity;
    }
}