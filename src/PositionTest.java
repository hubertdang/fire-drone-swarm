import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PositionTest {

    private Position position;

    @BeforeEach
    public void setUp() {
        position = new Position(5.0f, 10.0f);
    }

    @Test
    public void getX() {
        assertEquals(5.0f, position.getX(), 0.0001f, "getX() should return the correct x-coordinate");
    }

    @Test
    public void getY() {
        assertEquals(10.0f, position.getY(), 0.0001f, "getY() should return the correct y-coordinate");
    }

    @Test
    public void update() {
        position.update(8.0f, 12.0f);
        assertEquals(8.0f, position.getX(), 0.0001f, "update() should correctly update the x-coordinate");
        assertEquals(12.0f, position.getY(), 0.0001f, "update() should correctly update the y-coordinate");
    }

    @Test
    public void distanceFrom() {
        Position other = new Position(3.0f, 4.0f); // Expected distance: 6.7082
        float expectedDistance = (float) Math.sqrt(Math.pow(5.0f - 3.0f, 2) + Math.pow(10.0f - 4.0f, 2));

        assertEquals(expectedDistance, position.distanceFrom(other), 0.0001f, "distanceFrom() should return the correct Euclidean distance");
    }

    @Test
    public void equals() {
        Position pos1 = new Position(3.5f, 7.2f);
        Position pos2 = new Position(3.5f, 7.2f);
        Position pos3 = new Position(1.0f, 2.0f);
        Position pos4 = new Position(3.000001f, 4.000001f);
        Position pos5 = new Position(3.000002f, 4.000002f);

        assertEquals(pos1, pos1, "A position should be equal to itself");
        assertEquals(pos1, pos2, "Two positions with the same x and y should be equal");
        assertNotEquals(pos1, pos3, "Positions with different x or y values should not be equal");
        assertNotEquals(pos1, null, "A position should not be equal to null");
        assertNotEquals(pos1, "Not a Position", "A position should not be equal to an object of a different class");
        assertNotEquals(pos4, pos5, "Positions that are very close but not exactly equal should not be considered equal");
    }
}