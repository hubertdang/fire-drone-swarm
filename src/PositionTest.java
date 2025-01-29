import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PositionTest {

    @Test
    public void getX() {
        Position position = new Position(5.0f, 10.0f);
        assertEquals(5.0f, position.getX(), 0.0001f, "getX() should return the correct x-coordinate");
    }

    @Test
    public void getY() {
        Position position = new Position(5.0f, 10.0f);
        assertEquals(10.0f, position.getY(), 0.0001f, "getY() should return the correct y-coordinate");
    }

    @Test
    public void update() {
        Position position = new Position(2.5f, 3.5f);
        position.update(8.0f, 12.0f);

        assertEquals(8.0f, position.getX(), 0.0001f, "update() should correctly update the x-coordinate");
        assertEquals(12.0f, position.getY(), 0.0001f, "update() should correctly update the y-coordinate");
    }

    @Test
    public void distanceFrom() {
        Position position1 = new Position(0.0f, 0.0f);
        Position position2 = new Position(3.0f, 4.0f); // Should be 5.0 (3-4-5 triangle)

        float expectedDistance = 5.0f;
        float actualDistance = position1.distanceFrom(position2);

        assertEquals(expectedDistance, actualDistance, 0.0001f, "distanceFrom() should return the correct Euclidean distance");
    }
}
