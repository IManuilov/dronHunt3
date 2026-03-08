package org.example.snake;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.random.RandomGenerator;

public final class SnakeApp {
    private SnakeApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeApp::createAndShow);
    }

    private static void createAndShow() {
        SnakeGame game = new SnakeGame(20, 20, RandomGenerator.getDefault());
        SnakePanel panel = new SnakePanel(game, 24, 130);

        JLabel status = new JLabel();
        panel.setStatusSink(status::setText);
        panel.refreshStatus();

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> {
            game.reset();
            panel.refreshStatus();
            panel.repaint();
            panel.requestFocusInWindow();
        });

        JButton pauseButton = new JButton("Pause/Resume");
        pauseButton.addActionListener(e -> {
            game.togglePause();
            panel.refreshStatus();
            panel.requestFocusInWindow();
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(status);
        topBar.add(restartButton);
        topBar.add(pauseButton);

        JPanel controls = new JPanel(new GridLayout(2, 3, 4, 4));
        controls.add(new JLabel());
        controls.add(button("Up", () -> panel.changeDirection(Direction.UP)));
        controls.add(new JLabel());
        controls.add(button("Left", () -> panel.changeDirection(Direction.LEFT)));
        controls.add(button("Down", () -> panel.changeDirection(Direction.DOWN)));
        controls.add(button("Right", () -> panel.changeDirection(Direction.RIGHT)));

        JFrame frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        panel.requestFocusInWindow();
        panel.start();
    }

    private static JButton button(String title, Runnable action) {
        JButton button = new JButton(title);
        button.addActionListener(e -> action.run());
        return button;
    }
}
