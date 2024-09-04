import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 900;
    private final int HEIGHT = 350;
    private final int GROUND_Y = HEIGHT - 120;
    private final int PLAYER_X = 100;
    private final int GRAVITY = 1;
    private Timer timer;

    private boolean inAir = false;
    private int playerY = GROUND_Y;
    private final int playerWidth = 50;
    private final int playerHeight = 50;
    private int yVelocity = 0;

    private BufferedImage playerImageStanding;
    private BufferedImage playerImageJumping;
    private BufferedImage obstacleImage;
    private BufferedImage backgroundImage;

    private int scrollSpeed = 5;
    private int obstacleSpawnRate = 400;
    private List<Rectangle> obstacles = new ArrayList<>();
    private int score = 0;

    private int lives = 3;
    private boolean gameRunning = true;

    private JButton startButton;
    private JButton restartButton;
    private JButton saveMeButton;
    private JLabel lifeLabel;

    private Clip backgroundMusic;
    private Clip jumpSound;

    private String[][] quizQuestions = {
        {"What are Python strings?", " Immutable sequences of characters", " Mutable data types", "Numerical values", "Boolean variables"},
        {"Which of the following is NOT true about Python strings?", "They can be changed after creation", "They support indexing and slicing", "They can be enclosed in single or double quotes", "They are sequences of characters"},
        {"What does string concatenation mean in Python?", "Combining two or more strings into one", " Breaking down a string into substrings", "Removing spaces from a string", "Replacing characters in a string"},
        {"Which method allows dynamic and formatted output with strings in Python?", "format()", "split()", "replace()", "lower()"},
        {"What operation is performed by the '+' operator with strings in Python?", "String concatenation", "String splitting", "String formatting", "String comparison"}
    };

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.CYAN);
        this.setFocusable(true);
        this.addKeyListener(this);

        try {
            playerImageStanding = ImageIO.read(new File("D:\\H\\H\\king_stilll.png"));
            playerImageJumping = ImageIO.read(new File("D:\\H\\H\\king_jump.png"));
            obstacleImage = ImageIO.read(new File("D:\\H\\H\\obstacle.png"));
            backgroundImage = ImageIO.read(new File("D:\\H\\H\\background_2.jpg"));

            backgroundMusic = AudioSystem.getClip();
            AudioInputStream backgroundMusicStream = AudioSystem.getAudioInputStream(new File("D:\\H\\H\\bg.wav"));

            backgroundMusic.open(backgroundMusicStream);

            jumpSound = AudioSystem.getClip();
            AudioInputStream jumpSoundStream = AudioSystem.getAudioInputStream(new File("D:\\H\\H\\jumping.wav"));

            jumpSound.open(jumpSoundStream);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        saveMeButton = new JButton("Save Me!");
        saveMeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lives == 0) {
                    showQuiz();
                }
            }
        });
        saveMeButton.setEnabled(false); // Disabled initially

        lifeLabel = new JLabel("Lives: " + lives);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(saveMeButton);
        buttonPanel.add(lifeLabel);

        add(buttonPanel);

        timer = new Timer(20, this);
    }

    private void startGame() {
        startButton.setEnabled(false);
        gameRunning = true;
        timer.start();
        startBackgroundMusic();
        requestFocusInWindow();
    }

    private void restartGame() {
        if (lives > 0) {
            restartButton.setEnabled(false);
            playerY = GROUND_Y;
            obstacles.clear();
            score = 0;
            lives--;
            lifeLabel.setText("Lives: " + lives);
            gameRunning = true;
            timer.restart();
            startBackgroundMusic();
            requestFocusInWindow();
        }
    }

    private void showQuiz() {
        String[] question = getRandomQuestion();
        String correctAnswer = question[1];
        String[] options = {question[1], question[2], question[3], question[4]};
        int response = JOptionPane.showOptionDialog(this, question[0], "Quiz", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (response != -1 && options[response].equals(correctAnswer)) {
            lives++;
            lifeLabel.setText("Lives: " + lives);
            JOptionPane.showMessageDialog(this, "Correct! You earned an extra life.");

            // Ask if the player wants to restart the game
            int restartChoice = JOptionPane.showConfirmDialog(this, "Do you want to restart the game?", "Restart", JOptionPane.YES_NO_OPTION);
            if (restartChoice == JOptionPane.YES_OPTION) {
                restartGame();
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "<html><body style='width: 400px'>" +
                "Incorrect answer. Study this paragraph for better understanding.<br><br>" +
                "Python strings are sequences of characters, enclosed within either single quotes (' ') or double quotes (\"). " +
                "Strings are immutable, meaning they cannot be changed after creation. However, various string methods allow manipulation of string data. " +
                "Python provides powerful string formatting capabilities, including the format() method and f-strings, which enable dynamic and formatted output. " +
                "Additionally, strings support indexing and slicing operations to access individual characters or substrings. " +
                "String concatenation can be performed using the '+' operator. " +
                "Python strings are extensively used in tasks such as data processing, text manipulation, and user interaction in applications." +
                "</body></html>",
                "Study Material",
                JOptionPane.PLAIN_MESSAGE
            );
        }
    }

    private String[] getRandomQuestion() {
        Random rand = new Random();
        int index = rand.nextInt(quizQuestions.length);
        return quizQuestions[index];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Calculate the number of times to repeat the background image
        int numRepeats = (int) Math.ceil((double) WIDTH / backgroundImage.getWidth());
        
        // Draw the background image multiple times to cover the entire width
        for (int i = 0; i < numRepeats + 1; i++) {
            g.drawImage(backgroundImage, i * backgroundImage.getWidth() - score % backgroundImage.getWidth(), 0, this);
        }

        if (gameRunning) {
            BufferedImage currentImage = inAir ? playerImageJumping : playerImageStanding;
            if (currentImage != null) {
                g.drawImage(currentImage, PLAYER_X, playerY, playerWidth, playerHeight, this);
            } else {
                g.setColor(Color.RED);
                g.fillRect(PLAYER_X, playerY, playerWidth, playerHeight);
            }

            for (Rectangle obstacle : obstacles) {
                if (obstacleImage != null) {
                    g.drawImage(obstacleImage, obstacle.x, obstacle.y, obstacle.width, obstacle.height, this);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
                }
            }
            g.setColor(Color.BLACK);
            g.drawString("Score: " + score, 10, 20);
        } else {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 30);
            saveMeButton.setEnabled(true); // Enable "Save Me" button after all lives are lost
        }
    }

    private void moveObstacles() {
        for (Rectangle obstacle : obstacles) {
            obstacle.x -= scrollSpeed;
        }
        obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);
        if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).x <= WIDTH - obstacleSpawnRate) {
            int obstacleHeight = 50;
            int obstacleY = GROUND_Y - obstacleHeight+15;
            Rectangle newObstacle = new Rectangle(WIDTH, obstacleY, 50, obstacleHeight);
            obstacles.add(newObstacle);
        }
    }

    private boolean checkCollision() {
        Rectangle playerRect = new Rectangle(PLAYER_X, playerY, playerWidth, playerHeight);
        for (Rectangle obstacle : obstacles) {
            if (playerRect.intersects(obstacle)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        applyGravity();
        moveObstacles();
        if (checkCollision()) {
            gameRunning = false;
            timer.stop();
            stopBackgroundMusic();
            if (lives > 0) {
                restartButton.setEnabled(true);
            }
        }
        repaint();
        score += 1;

        // Check if score is a multiple of 200
        if (score % 200 == 0) {
            increaseObstacleSpeed();
        }
    }

    private void increaseObstacleSpeed() {
        // Increase the scroll speed by a small amount
        scrollSpeed += 1;
    }

    private void applyGravity() {
        if (inAir) {
            yVelocity += GRAVITY;
            playerY += yVelocity;
            if (playerY >= GROUND_Y) {
                playerY = GROUND_Y;
                inAir = false;
                yVelocity = 0;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameRunning && e.getKeyCode() == KeyEvent.VK_SPACE && !inAir) {
            inAir = true;
            yVelocity = -19;
            playJumpSound();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private void startBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning()) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    private void playJumpSound() {
        if (jumpSound != null) {
            jumpSound.setFramePosition(0); // Rewind to the beginning
            jumpSound.start();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple 2D Adventure Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GamePanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
