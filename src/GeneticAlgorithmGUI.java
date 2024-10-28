import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class GeneticAlgorithmGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JPanel drawingPanel;
    private final JButton startButton;
    private final JButton skipGenerationsButton;
    private final JButton stopButton;
    private final JButton addObstacleButton;
    private final JButton removeObstacleButton;
    private final JButton resetButton;
    private final JLabel generationLabel;

    private volatile boolean isRunning = false;
    private volatile boolean skipRequested = false;
    private boolean isInitialized = false;
    private Thread algorithmThread;
    private GeneticAlgorithm algorithm;
    
    private Target target;
    private Point populationSpawn;

    private List<Obstacle> obstacles;
    private Object selectedObject = null;
    private Point dragOffset = null;
    private int generationCount;
    private int currentFitness;

    public GeneticAlgorithmGUI() {
        setTitle("Генетический Алгоритм");
        setSize(800, 800);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initEnvironment();
       
        // Создание меню
        JMenuBar menuBar = getjMenuBar();

        // Добавляем строку меню в окно
        setJMenuBar(menuBar);
        
        // Панель для рисования
        drawingPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawEnvironment(g);
            }
        };
        
        initAlgorithm();
        drawingPanel.setPreferredSize(new Dimension(800, 600));
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Добавляем слушатели для перемещения объектов
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
            	if (isRunning) {
            		return;
            	}
                // Проверяем цель
                if (target.contains(e.getPoint())) {
                    selectedObject = target;
                    dragOffset = new Point(e.getX() - target.getX(), e.getY() - target.getY());
                    isInitialized = false;
                } else {
                    // Проверяем препятствия
                    for (Obstacle obstacle : obstacles) {
                        if (obstacle.contains(e.getPoint())) {
                            selectedObject = obstacle;
                            dragOffset = new Point(e.getX() - obstacle.getX(), e.getY() - obstacle.getY());
                            isInitialized = false;
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedObject = null;
                dragOffset = null;
            }
        });

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedObject != null && dragOffset != null) {
                    if (selectedObject instanceof Target) {
                        target.setPosition(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                    } else if (selectedObject instanceof Obstacle) {
                        ((Obstacle) selectedObject).setPosition(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                    }
                    drawingPanel.repaint();
                }
            }
        });

        generationLabel = new JLabel("Поколение: 0, Приспособленность: 0");
        generationLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Кнопки управления
        startButton = new JButton("Запуск");
        skipGenerationsButton = new JButton("Пропустить поколения");
        stopButton = new JButton("Остановить");
        resetButton = new JButton("Сброс");
        addObstacleButton = new JButton("Добавить препятствие");
        removeObstacleButton = new JButton("Удалить препятствие");

        startButton.addActionListener(e -> startAlgorithm());
        skipGenerationsButton.addActionListener(e -> skipGenerations());
        stopButton.addActionListener(e -> stopAlgorithm());
        resetButton.addActionListener(e -> reset());
        addObstacleButton.addActionListener(e -> addObstacle());
        removeObstacleButton.addActionListener(e -> removeObstacle());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 3, 5, 5));
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(resetButton);
        controlPanel.add(skipGenerationsButton);
        controlPanel.add(addObstacleButton);
        controlPanel.add(removeObstacleButton);

        JPanel generationPanel = new JPanel();
        generationPanel.add(generationLabel);

        add(generationPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        updateControlButtons();

        drawingPanel.repaint();
    }

    private JMenuBar getjMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu("О программе");

        // Вкладка "Справка"
        JMenuItem helpMenuItem = new JMenuItem("Справка");
        helpMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(GeneticAlgorithmGUI.this,
                "Это программа для демонстрации работы генетического алгоритма.",
                "Справка", JOptionPane.INFORMATION_MESSAGE));

        // Вкладка "О разработчике"
        JMenuItem aboutDevMenuItem = new JMenuItem("О разработчике");
        aboutDevMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(GeneticAlgorithmGUI.this,
                "Разработчик: Седюков Ярослав\nКонтакты: sedyukovyaroslav@gmail.com",
                "О разработчике", JOptionPane.INFORMATION_MESSAGE));

        // Добавляем элементы меню
        aboutMenu.add(helpMenuItem);
        aboutMenu.add(aboutDevMenuItem);
        menuBar.add(aboutMenu);
        return menuBar;
    }

    private void initEnvironment() {
    	generationCount = 0;
    	currentFitness = 0;
    	target = new Target(100, 100, 30);  // Цель
	    populationSpawn = new Point(50, 50);  // Начальная точка для популяции
	    obstacles = new ArrayList<>();
    }
    
    private void initAlgorithm() {
    	if (isInitialized) {
    		return;
    	}
    	
    	// Инициализируем obstaclesMatcher перед стартом алгоритма
        ObjectsMatcher obstaclesMatcher = new ObjectsMatcher(new HashSet<>(obstacles), target, 800, 750, 5);

        // Создаем новый экземпляр GeneticAlgorithm с учетом новых препятствий
        if (algorithm == null || generationCount == 0) {
        	algorithm = new GeneticAlgorithm(populationSpawn, target.getPosition(), obstaclesMatcher);
        } else {
        	algorithm.updateObstaclesMatcher(obstaclesMatcher);
        	algorithm.updateTarget(target.getPosition());
        }
        
        isInitialized = true;
        
        drawingPanel.repaint();
    }
    
    // Запуск алгоритма
    private void startAlgorithm() {
        if (isRunning) {
        	return;
        }
        
        initAlgorithm();
        
        isRunning = true;
        updateControlButtons();
        algorithmThread = new Thread(() -> {
            while (generationCount < GeneticAlgorithm.MAX_GENERATIONS) {
                if (skipRequested) {
                	int n = 5;
                	
                	// Пропуск n поколений
	            	for (int i = 0; i < n; i++) {
	                 	algorithm.evaluateFitness();
	                    algorithm.generateNextGeneration();
	                }
                	
                    skipRequested = false;  // Сбрасываем флаг
                    skipGenerationsButton.setEnabled(true);
                    skipGenerationsButton.setText("Пропустить поколения");
                    generationCount += n;  // Пропускаем несколько поколений
                    
                    algorithm.evaluateFitness();
                    currentFitness = algorithm.getMaxFitness();
                }
                
                generationCount++;
                generationLabel.setText("Поколение: " + generationCount + ", Приспособленность: " +  currentFitness);
                
                boolean reachedGoal = runGeneration(algorithm.getPopulation());  // Выполняем одно поколение

                if (reachedGoal) {
                    JOptionPane.showMessageDialog(this, "Цель достигнута! Поколение: " + generationCount);
                    stopAlgorithm();
                    return;
                }

                if (isRunning) {
                	 algorithm.evaluateFitness();
                	 currentFitness = algorithm.getMaxFitness();
                	 SwingUtilities.invokeLater(() -> generationLabel.setText("Поколение: " + generationCount + ", Приспособленность: " + currentFitness));
                	 
                     algorithm.generateNextGeneration();
                      
                     SwingUtilities.invokeLater(drawingPanel::repaint);
                } else {
                	JOptionPane.showMessageDialog(this, "Алгоритм остановлен.");
                	return;
                }
            }

            JOptionPane.showMessageDialog(this, "Достигнуто максимальное количество поколений.");

        });
        
        algorithmThread.start();
    }

    // Пропуск поколений
    private void skipGenerations() {
        if (isRunning) {
            skipRequested = true;  // Устанавливаем флаг для пропуска
            skipGenerationsButton.setText("Запрошено");
            skipGenerationsButton.setEnabled(false);
        }
    }

    // Остановка алгоритма
    private void stopAlgorithm() {
    	 isRunning = false; // Останавливаем поток
    	 if (algorithmThread != null) {
             algorithmThread.interrupt();  // Прерывание потока
         }

        updateControlButtons();
    }
    
    // Остановка алгоритма
    private void reset() {
    	 if (isRunning) {
    		 return;
    	 }
    	 
    	 initEnvironment();
    	 drawingPanel.repaint();
    	 
    	 generationLabel.setText("Поколение: " + generationCount + ", Приспособленность: " +  currentFitness);
    	 isInitialized = false;
		 
    	 initAlgorithm();
    }

    // Метод для выполнения одного поколения
    private boolean runGeneration(Population population) {
        for (int step = 0; step < GeneticAlgorithm.MAX_STEPS; step++) {
        	boolean reachedTarget = population.moveIndividuals(step);
        	drawingPanel.repaint();
        	 
            if (reachedTarget) {
                return true;
            }

            try {
                Thread.sleep(10);  // Пауза для визуализации
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }

    // Обновление состояния кнопок
    private void updateControlButtons() {
        addObstacleButton.setEnabled(!isRunning);
        removeObstacleButton.setEnabled(!isRunning);
        startButton.setEnabled(!isRunning);
        skipGenerationsButton.setEnabled(isRunning);
        skipGenerationsButton.setText("Пропустить поколения");
        stopButton.setEnabled(isRunning);
        resetButton.setEnabled(!isRunning);
    }

    // Метод для добавления препятствия
    private void addObstacle() {
        if (isRunning) {
        	return;
        }
        
        obstacles.add(new Obstacle(0, 0, 50, 50));  // Добавляем новое препятствие
        SwingUtilities.invokeLater(drawingPanel::repaint);
        
        isInitialized = false;
    }

    // Метод для удаления препятствия
    private void removeObstacle() {
        if (isRunning || obstacles.isEmpty()) {
        	return;
        	
        }
        
        obstacles.remove(obstacles.size() - 1);  // Удаляем последнее препятствие
        SwingUtilities.invokeLater(drawingPanel::repaint);
        
        isInitialized = false;
    }

    // Отрисовка среды
    // Вызывается в paintComponent (когда окно выходит/входи в фокус, тоже происходит вызов)
    private void drawEnvironment(Graphics g) {
    	if (algorithm != null) {
    		algorithm.getPopulation().drawIndividuals(g);
    	}
    	
        // Отрисовка препятствий
    	if (obstacles != null) {
    		for (Obstacle obstacle : obstacles) {
            	obstacle.Draw(g);
            }
    	}
                
    	// Отрисовка цели
    	if (target != null) {
    		target.draw(g);
    	}
    }
}
