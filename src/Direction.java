import java.awt.Point;

public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static Direction[] All() {
    	return Direction.values();
    }
    
    public Direction Opposite() {
    	switch (this) {
    	case UP:
    		return DOWN;
    	case DOWN:
    		return UP;
    	case LEFT:
    		return RIGHT;
    	case RIGHT:
    		return LEFT;
    	default:
    		throw new IllegalArgumentException("Unknown direction: " + this);
    	}
    }
     
    // Apply the direction to a point and return the new position
    public Point move(Point currentPosition, int stepSize) {
        return new Point(currentPosition.x + dx * stepSize, currentPosition.y + dy * stepSize);
    }
}
