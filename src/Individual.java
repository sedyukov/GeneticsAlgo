import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Individual extends CircleShape {
    public static final int DIAMETER = 20;  // Константный диаметр для всех особей
    private static final Random random = new Random();
    private final List<Direction> genotype;  // Генотип — генетические инструкции для перемещения

    private int fitness;  // Фитнес-функция для оценки "здоровья" особи
    private int collisionCount;  // Счётчик числа столкновений
    private List<Integer> candidatesForMutation;  // Кандидаты на мутацию
    private int id;  // Идентификатор особи

    // Конструктор класса Individual, который создает особь и генерирует случайный генотип
    public Individual(Point start, int steps) {
        super(start.x, start.y, DIAMETER, java.awt.Color.BLUE);  // Вызов конструктора родительского класса CircleShape
        this.genotype = generateRandomGenotype(steps);  // Генерация случайного генотипа при создании особи
        this.candidatesForMutation = new ArrayList<>();
    }

    // Метод для получения идентификатора особи
    public int getId() {
        return id;
    }

    // Метод для установки идентификатора особи
    public void setId(int id) {
        this.id = id;
    }

    // Метод для получения текущего фитнеса особи
    public int getFitness() {
        return fitness;
    }

    // Метод для установки фитнеса особи
    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    // Метод для увеличения числа столкновений
    public void increaseCollisionCount() {
        this.collisionCount++;
    }

    // Метод для получения числа столкновений
    public int getCollisionCount() {
        return collisionCount;
    }

    // Метод для добавления гена в список кандидатов на мутацию
    public void addMutationCandidate(int geneIdx) {
        candidatesForMutation.add(geneIdx);
    }

    // Метод для получения списка кандидатов на мутацию
    public List<Integer> getMutationCandidates() {
        return candidatesForMutation;
    }

    // Метод для очистки списка кандидатов на мутацию
    public void clearCandidatesForMutation() {
        candidatesForMutation = new ArrayList<>();
    }

    // Метод для генерации случайного генотипа (последовательности инструкций по перемещению)
    public List<Direction> generateRandomGenotype(int steps) {
        List<Direction> genotype = new ArrayList<>();
        Direction currentDirection = Direction.All()[random.nextInt(Direction.All().length)];  // Выбираем случайное начальное направление

        // Генерация генотипа на основе случайного выбора направления
        for (int i = 0; i < steps; i++) {
            // С вероятностью 20% меняем направление
            if (random.nextDouble() < 0.2) {
                currentDirection = Direction.All()[random.nextInt(Direction.All().length)];
            }
            genotype.add(currentDirection);  // Добавляем текущее направление в генотип
        }
        return genotype;
    }

    // Метод для перемещения особи на текущем шаге в соответствии с генотипом
    public void move(int step) {
        if (step >= genotype.size()) return;  // Если шаг больше длины генотипа, не двигаемся

        Direction direction = genotype.get(step);  // Получаем направление для текущего шага
        Point newPosition = direction.move(getPosition(), 5);  // Вычисляем новое положение
        setPosition(newPosition.x, newPosition.y);  // Устанавливаем новое положение особи
    }

    // Метод для получения генотипа
    public List<Direction> getGenotype() {
        return genotype;
    }

    // Переопределение метода toString() для вывода генотипа особи
    @Override
    public String toString() {
        return "Genotype: " + genotype;
    }
}
