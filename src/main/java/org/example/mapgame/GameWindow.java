package org.example.mapgame;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class GameWindow {
    private final LevelGenerator levelGenerator = new LevelGenerator();

    private LevelData currentLevel;
    private int score = 0;
    private int levelNumber = 1;

    private JLabel statusLabel;
    private MapPanel mapPanel;
    private FragmentPanel fragmentPanel;

    public void showWindow() {
        currentLevel = levelGenerator.generateLevel();

        JFrame frame = new JFrame("Map Fragment Hunt");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mapPanel = new MapPanel(new MapPanel.ClickListener() {
            @Override
            public void onMapClick(int mapX, int mapY) {
                handleMapClick(mapX, mapY);
            }

            @Override
            public void onOutsideMapClick() {
                updateStatus("Click inside the visible map area.");
            }
        });

        fragmentPanel = new FragmentPanel();

        JPanel content = new JPanel(new GridLayout(1, 2, 8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(mapPanel);
        content.add(fragmentPanel);

        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        applyLevelToViews();
        updateStatus("Find the rotated view on the map and click near its center.");

        frame.setSize(1400, 860);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void handleMapClick(int mapX, int mapY) {
        mapPanel.setLastClick(mapX, mapY);

        double dist = Math.hypot(mapX - currentLevel.targetX(), mapY - currentLevel.targetY());
        if (dist <= GameConfig.HIT_RADIUS) {
            int reward = 100 + Math.max(0, (int) ((GameConfig.HIT_RADIUS - dist) * 2));
            score += reward;
            levelNumber++;

            currentLevel = levelGenerator.generateLevel();
            applyLevelToViews();
            updateStatus("Hit! +" + reward + " points. New rotated view.");
        } else {
            score = Math.max(0, score - 15);
            updateStatus("Miss (" + (int) dist + " px). Try again.");
        }
    }

    private void applyLevelToViews() {
        mapPanel.setMapImage(currentLevel.mapImage());
        mapPanel.clearLastClick();
        fragmentPanel.setLevelData(currentLevel);
    }

    private void updateStatus(String message) {
        statusLabel.setText("Level: " + levelNumber + "   |   Score: " + score + "   |   " + message);
    }
}
