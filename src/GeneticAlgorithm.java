import java.awt.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class GeneticAlgorithm {
    public static final int MAX_GENERATIONS = 100;
    public static final int MAX_STEPS = 300;
    private static final double ELITE_RATIO = 0.3;
    private static final int POPULATION_SIZE = 200;
    private static final double BONUS_DISTANCE_RATIO = 0.2;
    private static final double MUTATION_RATE = 0.1;
    private static final double TOURNAMENT_RATIO = 0.3;
    public static final int DISTANCE_FACTOR = 100000;
    public static final int COLLISION_PENALTY = 100;
    public static final int DISTANCE_BONUS = 10000;

    private final Random random = new Random();
    private final int elitesCount;
    private final double distanceForBonus;  // Порог для поощрения близости к цели
    private final int tournamentParticipants;
    private final Point spawnPoint;

    private Population population;  // Популяция особей
    private Point target;  // Цель для всех особей

    private ObjectsMatcher obstaclesMatcher;
    
    private int maxFitness;

    public GeneticAlgorithm(Point spawnPoint, Point target, ObjectsMatcher obstaclesMatcher) {
        this.target = target;
        this.spawnPoint = spawnPoint;
        this.distanceForBonus = this.target.distance(spawnPoint) * BONUS_DISTANCE_RATIO;
        this.obstaclesMatcher = obstaclesMatcher;
        this.elitesCount =  (int)((double)(POPULATION_SIZE) * ELITE_RATIO); 
        this.tournamentParticipants = (int)((double)(POPULATION_SIZE) * TOURNAMENT_RATIO); 
        this.population = new Population(spawnPoint, target, obstaclesMatcher, 1);  // Передаем obstaclesMatcher в Population
        this.population.initIndividuals(POPULATION_SIZE,  MAX_STEPS);
    }
    
    public Population getPopulation() {
        return population;
    }
    
    public int getMaxFitness() {
    	return maxFitness;
    }
    
    public void updateObstaclesMatcher(ObjectsMatcher obstaclesMatcher) {
    	this.obstaclesMatcher = obstaclesMatcher;
    	population.updateObstaclesMatcher(obstaclesMatcher);
    }

    public void updateTarget(Point target) {
    	this.target = target;
    }
    
    // Оценка приспособленности с учетом близости к цели и препятствий
    public void evaluateFitness() {
    	maxFitness = 0;
        for (Individual individual : population.getIndividuals()) {
           double distanceToTarget = individual.distance(target);
           int fitness = (int) (DISTANCE_FACTOR * (1 / distanceToTarget));  // Обратная пропорция расстоянию
           
           // Добавляем штрафы за количество столкновений
           fitness -= (individual.getCollisionCount() * COLLISION_PENALTY);
           
           // Бонус за приближение к цели
           if (distanceToTarget < distanceForBonus) {
              fitness += DISTANCE_BONUS;
                
           }
           
           fitness = Math.max(0, fitness);
          
           if (fitness > maxFitness) {
        	   maxFitness = fitness;
           }
           
           individual.setFitness(fitness);
        }
    }

    // Скрещивание двух родителей
    public Individual crossover(Individual parent1, Individual parent2) {
        Individual child = new Individual(spawnPoint, MAX_STEPS);
        for (int i = 0; i < MAX_STEPS; i++) {
            child.getGenotype().set(i, random.nextBoolean() ? parent1.getGenotype().get(i) : parent2.getGenotype().get(i));
        }
        return child;
    }

    // Адаптивная мутация генотипа особи
    public void mutate(Individual individual, int generationCount) {
        // Вероятность мутации, изменяемая динамически
        double adjustedMutationRate = MUTATION_RATE * (1 - (double) generationCount / MAX_GENERATIONS);  // Адаптивная мутация
        List<Integer> mutationCandidates = individual.getMutationCandidates();
        for (Integer mutationCandidate : mutationCandidates) {
            if (random.nextDouble() < adjustedMutationRate) {
                int idx = mutationCandidate;
                Direction oldDirection = individual.getGenotype().get(idx);
                Direction newDirection = oldDirection.Opposite();
                individual.getGenotype().set(idx, newDirection);
            }
        }
        
        individual.clearCandidatesForMutation();
    }

    // Генерация следующего поколения с учетом элитизма
    public void generateNextGeneration() {	
        List<Individual> oldIndividuals = population.getIndividuals();
        Population newPopulation = new Population(spawnPoint, target, obstaclesMatcher, population.getLastId());

        // Сортировка по приспособленности
        oldIndividuals.sort((ind1, ind2) -> Integer.compare(ind2.getFitness(), ind1.getFitness()));


        // Берем лучших (элитизм) и запускаем для них мутацию
        for (int i = 0; i < elitesCount; i++) {
        	Individual individual = oldIndividuals.get(i);
        	mutate(individual, newPopulation.getIndividuals().size());  
        	newPopulation.addIndividual(oldIndividuals.get(i));
        }

        // Генерация новых особей на основе скрещивания и мутации
        while (newPopulation.getIndividuals().size() < oldIndividuals.size()) {
            Individual parent1 = selectParent(oldIndividuals);
            Individual parent2 = selectParent(oldIndividuals);
            Individual child = crossover(parent1, parent2); 
            newPopulation.addIndividual(child);
        }
        
        population = newPopulation;
        population.resetPositions();
    }

    // Турнирная селекция: выбор родителя на основе небольшой группы
    private Individual selectParent(List<Individual> population) {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentParticipants; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        tournament.sort((ind1, ind2) -> Integer.compare(ind2.getFitness(), ind1.getFitness()));
        return tournament.get(0);  // Лучший из турнира
    }
}
