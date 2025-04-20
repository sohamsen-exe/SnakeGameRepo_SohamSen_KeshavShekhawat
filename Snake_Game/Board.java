package Snake_Game;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.*;
import java.util.stream.Collectors;

public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 300;
    private final int B_HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 140;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;

    private Timer timer;
    private Image ball;
    private Image apple;
    private Image head;

    private int score = 0;
    private String username;

    public Board(String username) {
        this.username = username;
        initBoard();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));
        loadImages();
        initGame();
    }

    private void loadImages() {
        ImageIcon iid = new ImageIcon("src/resources/dot.png");
        ball = iid.getImage();

        ImageIcon iia = new ImageIcon("src/resources/apple.png");
        apple = iia.getImage();

        ImageIcon iih = new ImageIcon("src/resources/head.png");
        head = iih.getImage();
    }

    private void initGame() {
        dots = 3;
        score = 0;
        inGame = true;

        for (int i = 0; i < dots; i++) {
            x[i] = 50 - i * DOT_SIZE;
            y[i] = 50;
        }

        locateApple();
        timer = new Timer(DELAY, this);
        timer.start();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inGame) {
            doDrawing(g);
        }
        else {
            gameOver(g);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void doDrawing(Graphics g) {
        if (inGame) {
            g.drawImage(apple, apple_x, apple_y, this);
            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z], y[z], this);
                }
                else {
                    g.drawImage(ball, x[z], y[z], this);
                }
            }

            g.setColor(Color.white);
            g.drawString("Score: " + score, 10, 20);

            Toolkit.getDefaultToolkit().sync();

        }
    }

    private JButton playAgainButton;
    private JButton exitButton;

    private void gameOver(Graphics g) {
        Font titleFont = new Font("Arial", Font.BOLD, 22);
        Font regularFont = new Font("Arial", Font.PLAIN, 16);

        g.setColor(Color.WHITE);

        // Game Over title
        g.setFont(titleFont);
        String msg = "Game Over";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (B_WIDTH - fm.stringWidth(msg)) / 2, 100);

        // Final score
        g.setFont(regularFont);
        fm = g.getFontMetrics();
        String finalScore = "Final Score: " + score;
        g.drawString(finalScore, (B_WIDTH - fm.stringWidth(finalScore)) / 2, 140);

        // High score
        try {
            int highScore = ScoreManager.getHighScore(username);
            String highScoreStr = "High Score: " + highScore;
            g.drawString(highScoreStr, (B_WIDTH - fm.stringWidth(highScoreStr)) / 2, 170);

            // Leaderboard
            g.drawString("--- Leaderboard ---", (B_WIDTH - fm.stringWidth("--- Leaderboard ---")) / 2, 200);

            int yOffset = 225;
            Map<String, Integer> leaderboard = ScoreManager.getLeaderboard();
            for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
                String entryStr = entry.getKey() + ": " + entry.getValue();
                g.drawString(entryStr, (B_WIDTH - fm.stringWidth(entryStr)) / 2, yOffset);
                yOffset += 20;
            }

            // Restart instructions
            String restart = "Press SPACE to play again or ESC to exit";
            g.drawString(restart, (B_WIDTH - fm.stringWidth(restart)) / 2, yOffset + 20);

        }
        catch (IOException e) {
            g.drawString("Error loading high score", 10, 170);
        }
    }

    private void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;
            score += 10;
            locateApple();
        }
    }

    private void move() {
        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private void checkCollision() {
        for (int z = dots; z > 0; z--) {
            if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
            }
        }

        if (x[0] >= B_WIDTH) {
            x[0] = 0;
        }
        if (x[0] < 0) {
            x[0] = B_WIDTH - DOT_SIZE;
        }
        if (y[0] >= B_HEIGHT) {
            y[0] = 0;
        }
        if (y[0] < 0) {
            y[0] = B_HEIGHT - DOT_SIZE;
        }


        if (!inGame) {
            timer.stop();
            gameOver(getGraphics());
            try {
                ScoreManager.saveHighScore(username, score);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));
        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }

            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                rightDirection = false;
                leftDirection = false;
            }

            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                rightDirection = false;
                leftDirection = false;
            }
            if (!inGame) {
                if (key == KeyEvent.VK_SPACE) {
                    initGame();
                    repaint();
                }
                else if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        }
    }
}
