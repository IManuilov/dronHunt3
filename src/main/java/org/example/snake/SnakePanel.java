package org.example.snake;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class SnakePanel extends JPanel {
    private final SnakeGame game;
    private final int cellSize;
    private final Timer timer;
    private Consumer<String> statusSink = text -> { };

    public SnakePanel(SnakeGame game, int cellSize, int tickMs) {
        this.game = game;
        this.cellSize = cellSize;
        this.timer = new Timer(tickMs, e -> {
            game.tick();
            refreshStatus();
            repaint();
        });

        setPreferredSize(new Dimension(game.getWidth() * cellSize, game.getHeight() * cellSize));
        setBackground(new Color(245, 245, 245));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> changeDirection(Direction.UP);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> changeDirection(Direction.DOWN);
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> changeDirection(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> changeDirection(Direction.RIGHT);
                    case KeyEvent.VK_SPACE -> {
                        game.togglePause();
                        refreshStatus();
                    }
                    case KeyEvent.VK_R -> {
                        game.reset();
                        refreshStatus();
                    }
                    default -> {
                    }
                }
            }
        });
    }

    public void setStatusSink(Consumer<String> statusSink) {
        this.statusSink = statusSink;
    }

    public void start() {
        timer.start();
    }

    public void changeDirection(Direction direction) {
        game.setDirection(direction);
        requestFocusInWindow();
    }

    public void refreshStatus() {
        String state;
        if (game.isGameOver()) {
            state = "Game Over";
        } else if (game.isPaused()) {
            state = "Paused";
        } else {
            state = "Running";
        }
        statusSink.accept("Score: " + game.getScore() + " | " + state + " | R: restart, Space: pause");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setColor(new Color(210, 210, 210));
        for (int x = 0; x <= game.getWidth(); x++) {
            int px = x * cellSize;
            g2.drawLine(px, 0, px, game.getHeight() * cellSize);
        }
        for (int y = 0; y <= game.getHeight(); y++) {
            int py = y * cellSize;
            g2.drawLine(0, py, game.getWidth() * cellSize, py);
        }

        Position food = game.getFood();
        if (food != null) {
            g2.setColor(new Color(200, 40, 40));
            g2.fillRect(food.x() * cellSize, food.y() * cellSize, cellSize, cellSize);
        }

        var snake = game.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            Position segment = snake.get(i);
            g2.setColor(i == 0 ? new Color(30, 130, 50) : new Color(50, 180, 70));
            g2.fillRect(segment.x() * cellSize, segment.y() * cellSize, cellSize, cellSize);
        }

        g2.dispose();
    }
}
