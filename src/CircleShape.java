import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

public class CircleShape {
    protected int x;
    protected int y;
    protected final int diameter;
    protected final Color color;

    public CircleShape(int x, int y, int diameter, Color color) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public void setX(int x) {
    	this.x = x;
    }
    
    public void setY(int y) {
    	this.y = y;
    }

    public int getRadius() {
        return diameter / 2;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public int getWidth() {
        return diameter;
    }

    public int getHeight() {
        return diameter;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, diameter, diameter);
    }
    
    public double distance(Point p) {
        // Возвращаем расстояние, используя теорему Пифагора
        return (new Point(x, y)).distance(p);
    }
}
