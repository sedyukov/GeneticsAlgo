import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Target extends CircleShape {
    public Target(int x, int y, int diameter) {
        super(x, y, diameter, Color.RED);
    }

    public Point getPosition() {
        return new Point(x, y);  // Метод для возвращения объекта Point с координатами
    }

    public boolean contains(Point p) {
        return (new Ellipse2D.Double(x, y, diameter, diameter)).contains(p);
    }
}
