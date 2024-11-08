package org.example;

import javax.sound.sampled.*; // Импорт для работы со звуком
import javax.swing.*; // Импорт для создания графического интерфейса
import java.awt.*; // Импорт для работы с графикой
import java.awt.event.ActionEvent; // Импорт для обработки событий
import java.awt.event.ActionListener; // Импорт для слушателей действий
import java.awt.event.KeyAdapter; // Импорт для обработки нажатий клавиш
import java.awt.event.KeyEvent; // Импорт для событий клавиатуры
import java.io.*; // Импорт для работы с файлами
import java.util.ArrayList; // Импорт для использования списка
import java.util.Random; // Импорт для генерации случайных чисел

public class FlappyBird extends JPanel implements ActionListener {
    private final int WIDTH = 400, HEIGHT = 600; // Ширина и высота игрового окна
    private final int BIRD_DIAMETER = 65; // Диаметр птицы
    private final int GRAVITY = 1; // Сила притяжения (2)
    private final int JUMP_STRENGTH = -8; // Сила прыжка (-10)
    private final int PIPE_WIDTH = 52; // Ширина трубы
    private final int PIPE_GAP = 150; // Интервал между верхней и нижней трубами

    private Image birdImage; // Изображение птицы
    private Image pipeImage; // Изображение трубы
    private Image backgroundImage; // Изображение фона

    private int birdY, birdVelocity, score, highScore; // Позиция Y птицы, скорость, счёт, лучший счёт
    private ArrayList<Rectangle> pipes; // Список труб
    private boolean gameOver; // Флаг окончания игры

    private JButton startButton; // Кнопка входа в игру

    private boolean gameStarted; // Флаг старта игры

    public FlappyBird() {
        loadImages(); // Загрузка изображений
        playBackgroundMusic(); // Воспроизведение фоновой музыки
        highScore = loadHighScore(); // Загрузка лучшего результата
        setPreferredSize(new Dimension(WIDTH, HEIGHT)); // Установка размеров панели
        setBackground(Color.cyan); // Установка фона панели
        setFocusable(true); // Делает панель фокусируемой
        birdY = HEIGHT / 2; // Начальная позиция Y для птицы
        birdVelocity = 0; // Начальная скорость
        score = 0; // Начальные очки
        pipes = new ArrayList<>(); // Инициализация списка труб
        gameOver = false; // Игра не закончена
        gameStarted = false; // Запуск игры без начала движения

        // Кнопка входа в игру
        startButton = new JButton("Start"); // Текст кнопки
        startButton.setFont(new Font("Corbel", Font.BOLD, 20)); // Шрифт кнопки
        startButton.setForeground(Color.black); // Цвет кнопки
        startButton.setBackground(new Color(255, 244, 255)); // Задний фон кнопки
        startButton.setBounds(WIDTH / 3 + 12, HEIGHT / 2 + 50, 100,50); // Размер кнопки

        // Перезапуск игры
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
                gameStarted = true;
            }
        });

        setLayout(null); // Установка кнопки
        add(startButton);

        // Обработка нажатий клавиш
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) { // При нажатии пробела
                    birdVelocity = JUMP_STRENGTH; // Устанавливаем скорость прыжка
                } else if (gameOver) { // Если игра окончена
                    resetGame(); // Сброс игры
                }
            }
        });

        Timer timer = new Timer(25, this); // Таймер обновления игры
        timer.start(); // Запуск таймера
        spawnPipe(); // Создание первой трубы

        //
    }

    private void loadImages() {
        // Загрузка изображений
        birdImage = new ImageIcon("src/main/resources/bird.png").getImage();
        pipeImage = new ImageIcon("src/main/resources/column.png").getImage();
        backgroundImage = new ImageIcon("src/main/resources/background.png").getImage(); // Убедитесь, что файл существует

        // Проверка на загрузку изображений
        if (birdImage == null) System.out.println("Bird image not found.");
        if (pipeImage == null) System.out.println("Pipe image not found.");
        if (backgroundImage == null) System.out.println("Background image not found.");
    }

    private void playBackgroundMusic() {
        try {
            File musicFile = new File("src/main/resources/background_music.wav"); // Путь к файлу музыки
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile); // Создание потока для аудио
            Clip clip = AudioSystem.getClip(); // Получение клипа
            clip.open(audioInputStream); // Открытие потока аудио
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Зацикливание музыки
            clip.start(); // Старт музыки
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace(); // Обработка исключений
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Вызов метода родительского класса
        g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null); // Отрисовка фона
        g.drawImage(birdImage, 100, birdY, BIRD_DIAMETER, BIRD_DIAMETER, null); // Отрисовка птицы
        for (Rectangle pipe : pipes) {
            g.drawImage(pipeImage, pipe.x, pipe.y, PIPE_WIDTH, pipe.height, null); // Отрисовка труб
        }
        g.setColor(Color.black); // Установка цвета текста
        g.setFont(new Font("Corbel", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, 20); // Отображение счёта
        g.drawString("High Score: " + highScore, 10, 40); // Отображение лучшего счёта
        if (gameOver) { // Если игра окончена
            g.setFont(new Font("Corbel", Font.BOLD, 30)); // Установка шрифта
            g.drawString("Game Over", WIDTH / 3, HEIGHT / 2); // Сообщение об окончании игры
            g.drawString("Press Button to Restart", WIDTH / 12, HEIGHT / 2 + 40);// Подсказка для перезапуска
            startButton.setVisible(true); // Отображение кнопки
        }
        else {
            startButton.setVisible(!gameStarted); // Скрытие кнопки
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver&&gameStarted) { // Если игра не окончена
            birdY += birdVelocity; // Обновление позиции Y птицы
            birdVelocity += GRAVITY; // Применение силы притяжения

            for (int i = 0; i < pipes.size(); i++) {
                Rectangle pipe = pipes.get(i); // Получение текущей трубы
                pipe.x -= 5; // Движение трубы влево

                // Проверка на выход трубы за экран и увеличение счета
                if (pipe.x + PIPE_WIDTH < 0) {
                    pipes.remove(i); // Удаление трубы, если она ушла за экран
                    i--; // Уменьшение индекса для корректного прохода по списку
                    score++; // Увеличение счета при пропуске трубы
                    System.out.println("Score: " + score); // Отладочная информация
                }

                // Проверка на столкновение
                if (pipe.intersects(new Rectangle(100, birdY, BIRD_DIAMETER, BIRD_DIAMETER)) || birdY > HEIGHT) { // 100
                    gameOver = true; // Если есть столкновение, игра окончена
                    saveScore(); // Вызов метода для сохранения счета
                }
            }

            // Спавн новой трубы, если необходимо
            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).x < WIDTH - 200) {
                spawnPipe(); // Создание новой трубы
            }
        }
        repaint(); // Перерисовка панели
    }

    private void spawnPipe() {
        int height = new Random().nextInt(300) + 80; // Генерация случайной высоты трубы
        pipes.add(new Rectangle(WIDTH, 0, PIPE_WIDTH, height)); // Добавление верхней трубы
        pipes.add(new Rectangle(WIDTH, height + PIPE_GAP, PIPE_WIDTH, HEIGHT - height - PIPE_GAP)); // Добавление нижней трубы
    }

    private void resetGame() {
        birdY = HEIGHT / 2; // Сброс позиции Y птицы
        birdVelocity = 0; // Сброс скорости
        score = 0; // Сброс счета
        pipes.clear(); // Очистка списка труб
        gameOver = false; // Установка флага окончания игры
        gameStarted = false; // Изменяем флаг начала игры
        startButton.setVisible(true); // изменяем флаг кнопки

        spawnPipe(); // Создание новой трубы
    }

    private void saveScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/highscore.txt", true))) {
            // Сохранение счета в текстовый файл
            writer.write("Score: " + score); // Запись счета
            writer.newLine(); // Перевод строки
            // Обновление лучшего результата
            if (score > highScore) {
                highScore = score; // Обновление лучшего результата
            }
        } catch (IOException e) {
            e.printStackTrace(); // Обработка исключений
        }
    }

    private int loadHighScore() {
        int highScore = 0; // Начальное значение для лучшего результата
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/highscore.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Извлечение лучшего результата из файла
                String[] parts = line.split(": ");
                if (parts.length > 1) {
                    int score = Integer.parseInt(parts[1]);
                    if (score > highScore) {
                        highScore = score; // Обновление лучшего результата, если текущий больше
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Обработка исключений
        }
        return highScore; // Возвращаем лучший результат
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird"); // Создание окна игры
        FlappyBird gamePanel = new FlappyBird(); // Создание экземпляра игры
        frame.add(gamePanel); // Добавление панели игры в окно
        frame.pack(); // Установка размеров окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Завершение приложения при закрытии окна
        frame.setVisible(true); // Отображение окна
        frame.setResizable(false); // Запрещение изменения размера окна
    }
}
// Finally