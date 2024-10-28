import java.awt.*;
import java.util.Set;

public class ObjectsMatcher {
    private final Set<Obstacle> obstacles;  // Множество прямоугольных препятствий
    private final Target target;  // Цель — это окружность
    private final int screenWidth;
    private final int screenHeight;
    private final int stepSize;

    // Конструктор для инициализации препятствий, цели, размеров экрана и шага перемещения
    public ObjectsMatcher(Set<Obstacle> obstacles, Target target, int screenWidth, int screenHeight, int stepSize) {
        this.obstacles = obstacles;
        this.target = target;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.stepSize = stepSize;
    }

    // Метод для проверки, может ли объект двигаться в заданном направлении
    public boolean canMove(Individual individual, Direction direction) {
        Point newCenter = getNewCenter(individual, direction);

        // Проверяем, находится ли новый центр внутри границ экрана
        if (newCenter.x - individual.getRadius() < 0 || newCenter.x + individual.getRadius() >= screenWidth ||
            newCenter.y - individual.getRadius() < 0 || newCenter.y + individual.getRadius() >= screenHeight) {
            return false;  // Объект выходит за пределы экрана
        }

        // Проверяем столкновение с прямоугольными препятствиями
        for (Obstacle obstacle : obstacles) {
            if (isCircleCollidingWithRectangle(newCenter, individual.getRadius(), obstacle)) {
                return false;  // Обнаружено столкновение с препятствием
            }
        }

        return true;  // Столкновений не обнаружено
    }

    // Метод для проверки, достиг ли объект цели
    public boolean reachedTarget(Individual individual, Direction direction) {
        Point newCenter = getNewCenter(individual, direction);
        // Вычисляем центр цели вручную и проверяем столкновение объекта с целью, используя проверку столкновения окружностей
        Point targetCenter = calculateTargetCenter();
        return isCircleCollidingWithCircle(newCenter, individual.getRadius(), targetCenter, target.getRadius());
    }

    // Вспомогательный метод для вычисления нового центра объекта на основе движения
    private Point getNewCenter(Individual individual, Direction direction) {
        Point currentCenter = calculateCenter(individual);

        switch (direction) {
            case UP:
                return new Point(currentCenter.x, currentCenter.y - stepSize);
            case DOWN:
                return new Point(currentCenter.x, currentCenter.y + stepSize);
            case LEFT:
                return new Point(currentCenter.x - stepSize, currentCenter.y);
            case RIGHT:
                return new Point(currentCenter.x + stepSize, currentCenter.y);
            default:
                throw new IllegalArgumentException("Неизвестное направление: " + direction);
        }
    }

    // Метод для вычисления центра объекта (окружности)
    private Point calculateCenter(Individual individual) {
        int centerX = individual.getX() + individual.getRadius();
        int centerY = individual.getY() + individual.getRadius();
        return new Point(centerX, centerY);
    }

    // Метод для вычисления центра цели (окружности)
    private Point calculateTargetCenter() {
        int centerX = target.getX() + target.getWidth() / 2;
        int centerY = target.getY() + target.getHeight() / 2;
        return new Point(centerX, centerY);
    }

    // Проверка на столкновение окружности (объекта) с прямоугольником (препятствием)
    private boolean isCircleCollidingWithRectangle(Point circleCenter, int radius, Obstacle obstacle) {
        Rectangle obstacleBounds = obstacle.getBounds();

        // Находим ближайшую точку на прямоугольнике к центру окружности
        int closestX = clamp(circleCenter.x, obstacleBounds.x, obstacleBounds.x + obstacleBounds.width);
        int closestY = clamp(circleCenter.y, obstacleBounds.y, obstacleBounds.y + obstacleBounds.height);

        // Вычисляем расстояние от центра окружности до ближайшей точки
        int distanceX = circleCenter.x - closestX;
        int distanceY = circleCenter.y - closestY;
        int distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        // Если квадрат расстояния меньше или равен квадрату радиуса, значит, есть столкновение
        return distanceSquared <= (radius * radius);
    }

    // Проверка на столкновение двух окружностей (объекта и цели)
    private boolean isCircleCollidingWithCircle(Point center1, int radius1, Point center2, int radius2) {
        // Вычисляем расстояние между центрами двух окружностей
        int deltaX = center1.x - center2.x;
        int deltaY = center1.y - center2.y;
        int distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);

        // Если расстояние между центрами меньше или равно сумме радиусов, значит, есть столкновение
        int radiiSum = radius1 + radius2;
        return distanceSquared <= (radiiSum * radiiSum);
    }

    // Вспомогательный метод для ограничения значения между минимумом и максимумом
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
