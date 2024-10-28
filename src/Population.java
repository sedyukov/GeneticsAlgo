import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Population {
    private final List<Individual> individuals;
    private final Point spawnPoint;
    private final Point target;
    private ObjectsMatcher obstaclesMatcher;  // Механизм для проверки препятствий и границ
    private int lastId;

    public Population(Point spawnPoint, Point target, ObjectsMatcher obstaclesMatcher, int lastId) {
        this.spawnPoint = spawnPoint;
        this.obstaclesMatcher = obstaclesMatcher;
        this.target = target;
        this.lastId = lastId;
        individuals = new ArrayList<>();
    }
    
    public void initIndividuals(int size, int steps) {
    	for (int i = 0; i < size; i++) {
    		Individual individual = new Individual(spawnPoint, steps);
    		individual.setId(lastId + i);
            individuals.add(individual);
        }
    	
    	lastId += size;
    }
    
    public void addIndividual(Individual individual) {
    	if (individual.getId() == 0) {
    		lastId++;
        	individual.setId(lastId);
    	}
    	
        individuals.add(individual);
    }
    
    
    public void updateObstaclesMatcher(ObjectsMatcher obstaclesMatcher) {
    	this.obstaclesMatcher = obstaclesMatcher;
    }

    public List<Individual> getIndividuals() {
        return individuals;
    }
    
    public int getLastId() {
    	return lastId;
    }

    // Перемещение всех особей в соответствии с их генотипом на текущем шаге
    public boolean moveIndividuals(int step) {
        for (Individual individual : individuals) {
            Direction direction = individual.getGenotype().get(step);
            
            // Проверяем, можно ли двигаться в данном направлении
            if (!obstaclesMatcher.canMove(individual, direction)) {
            	 individual.increaseCollisionCount();  // Если движение невозможно, увеличиваем число столкновений
            	 individual.addMutationCandidate(step);
            	 continue;
            }
            
            
            double oldDistance = individual.distance(target);
            boolean targetReached = obstaclesMatcher.reachedTarget(individual, direction);
            
            individual.move(step); 
            
            double newDistance = individual.distance(target);
            
            if (targetReached) {
                return true;  // Особь достигла цели
            }
            
            if (oldDistance < newDistance) {
            	individual.addMutationCandidate(step);
            }
        }
        
		return false;
    }

    // Сброс позиций всех особей
    public void resetPositions() {
        for (Individual individual : individuals) {
        	// Возвращаем всех на начальную позицию
            individual.setX(spawnPoint.x);  
            individual.setY(spawnPoint.y);
        }
    }

    // Отрисовка всех особей
    public void drawIndividuals(Graphics g) {
    	List<Individual> snapshot;

        // Копируем, так как drawIndividuals может быть вызван конкурентно,
        // а individuals будут конкурентно изменяться основным циклом
        synchronized (individuals) {
            snapshot = new ArrayList<>(individuals);
        }

        for (Individual individual : snapshot) {
        	individual.draw(g);
        }
    }
}
