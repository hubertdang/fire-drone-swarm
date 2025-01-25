public class Zone {
    private int id;
    private Position position;
    private FireSeverity severity;
    private float requiredAgentAmount;

    public Zone(Position position, float requiredAgentAmount) {
    }

    public void setSeverity(FireSeverity severity) {
    }

    public FireSeverity getSeverity() {
        return null;
    }

    public void setPosition(Position position) {
    }

    public Position getPosition() {
        return null;
    }

    public int getId() {
        return 0;
    }

    public void setId(int id) {
    }

    public float getRequiredAgent() {
        return 0;
    }

    public void setRequiredAgentAmount(float amountRequired) {
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}