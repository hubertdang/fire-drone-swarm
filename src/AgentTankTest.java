import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentTankTest {

    private AgentTank agentTank;

    @BeforeEach
    public void setUp() {
        agentTank = new AgentTank();
    }

    @Test
    public void openNozzle() {
        agentTank.openNozzle();
        assertTrue(agentTank.isNozzleOpen(), "Nozzle should be open after calling openNozzle()");
    }

    @Test
    public void closeNozzle() {
        agentTank.openNozzle();
        agentTank.closeNozzle();
        assertFalse(agentTank.isNozzleOpen(), "Nozzle should be closed after calling closeNozzle()");
    }

    @Test
    public void isNozzleOpen() {
        assertFalse(agentTank.isNozzleOpen(), "Nozzle should be closed by default");
        agentTank.openNozzle();
        assertTrue(agentTank.isNozzleOpen(), "Nozzle should be open after calling openNozzle()");
    }

    @Test
    public void refill() {
        agentTank.openNozzle();
        agentTank.decreaseAgent(5.0f);
        assertTrue(agentTank.getCurrAgentAmount() < 11.0f, "Current agent amount should be less than full after decrease");

        agentTank.refill();
        assertEquals(11.0f, agentTank.getCurrAgentAmount(), 0.001, "Tank should be refilled to full capacity");
    }

    @Test
    public void isEmpty() {
        assertFalse(agentTank.isEmpty(), "Tank should not be empty initially");
        agentTank.openNozzle();

        agentTank.decreaseAgent(11.0f);
        assertTrue(agentTank.isEmpty(), "Tank should be empty after using all agent");

        agentTank.refill();
        assertFalse(agentTank.isEmpty(), "Tank should not be empty after refill");
    }

    @Test
    public void getCurrAgentAmount() {
        assertEquals(11.0f, agentTank.getCurrAgentAmount(), 0.001, "Initial amount should be full capacity");

        agentTank.openNozzle();
        agentTank.decreaseAgent(3.0f);
        assertEquals(8.0f, agentTank.getCurrAgentAmount(), 0.001, "Amount should decrease correctly");

        agentTank.decreaseAgent(8.0f);
        assertEquals(0.0f, agentTank.getCurrAgentAmount(), 0.001, "Tank should be empty after using all agent");
    }

    @Test
    public void decreaseAgent() {
        agentTank.decreaseAgent(5.0f);
        assertEquals(11.0f, agentTank.getCurrAgentAmount(), 0.001, "Agent should not decrease, nozzle is not open");

        agentTank.openNozzle();
        agentTank.decreaseAgent(5.0f);
        assertEquals(6.0f, agentTank.getCurrAgentAmount(), 0.001, "Agent should not go below zero");

        agentTank.decreaseAgent(10.0f); // Attempt to decrease beyond zero
        assertEquals(0.0f, agentTank.getCurrAgentAmount(), 0.001, "Agent should not go below zero");

        assertTrue(agentTank.isEmpty(), "Tank should be empty after depleting all agent");
    }
}