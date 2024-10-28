import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class Obstacle {
    private int x;
    private int y;
    private final int width;
    private final int height;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean contains(Point p) {
        return p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public void Draw(Graphics g) {
    	 g.setColor(Color.BLACK);
         g.fillRect(x, y, width, height);
    }
}
