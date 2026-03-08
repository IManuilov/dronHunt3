package org.example.mapgame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameWindow {
    private final LevelGenerator levelGenerator = new LevelGenerator();
    private final Random random = new Random();

    private LevelData currentLevel;
    private BufferedImage displayedMapImage;

    private int score = 0;
    private int levelNumber = 1;
    private int levelsPassedOnCurrentMap = 0;

    private JLabel statusLabel;
    private MapPanel mapPanel;
    private FragmentPanel fragmentPanel;

    private static final int MAX_SHOT_HISTORY = 15;

    private final List<CarState> cars = new ArrayList<>();
    private final List<MapPoint> carPositions = new ArrayList<>();
    private final List<Boolean> shotHistory = new ArrayList<>();

    private int fps = 0;
    private int fpsFrames = 0;
    private long fpsWindowStartMs = System.currentTimeMillis();
    private String lastStatusMessage = "";

    private static class CarState {
        private final RoadPath road;
        private final double[] cumulativeLengths;
        private final double totalLength;

        private double progress;
        private double speed;

        private CarState(RoadPath road, double progress, double speed,
                         double[] cumulativeLengths, double totalLength) {
            this.road = road;
            this.progress = progress;
            this.speed = speed;
            this.cumulativeLengths = cumulativeLengths;
            this.totalLength = totalLength;
        }
    }

    public void showWindow() {
        currentLevel = levelGenerator.generateLevel();

        JFrame frame = new JFrame("Map Fragment Hunt");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton regenerateButton = new JButton("Regenerate Map");
        regenerateButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        regenerateButton.addActionListener(e -> regenerateMap());

        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(regenerateButton, BorderLayout.EAST);

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

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        applyLevelToViews();
        updateStatus("Find the rotated view on the map and click near its center.");

        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        Timer timer = new Timer(33, e -> {
            updateCars();
            pushCarPositionsToPanels();
            updateFps();
        });
        timer.start();
    }

    private void regenerateMap() {
        currentLevel = levelGenerator.generateLevel();
        levelsPassedOnCurrentMap = 0;
        applyLevelToViews();
        updateStatus("Map updated.");
    }

    private void handleMapClick(int mapX, int mapY) {
        mapPanel.setLastClick(mapX, mapY);
        mapPanel.showClickExplosion(mapX, mapY, 1500);

        double dist = Math.hypot(mapX - currentLevel.targetX(), mapY - currentLevel.targetY());
        if (dist <= GameConfig.HIT_RADIUS) {
            registerShot(true);
            int previousTargetX = currentLevel.targetX();
            int previousTargetY = currentLevel.targetY();
            int reward = 100 + Math.max(0, (int) ((GameConfig.HIT_RADIUS - dist) * 2));
            score += reward;
            levelNumber++;
            levelsPassedOnCurrentMap++;

            if (levelsPassedOnCurrentMap >= GameConfig.LEVELS_PER_MAP) {
                currentLevel = levelGenerator.generateLevel();
                levelsPassedOnCurrentMap = 0;
                applyLevelToViews();
                updateStatus("Hit! +" + reward + " points.");
            } else {
                currentLevel = levelGenerator.generateLevelForExistingMap(currentLevel);
                mapPanel.clearLastClick();
                mapPanel.showFadingTarget(previousTargetX, previousTargetY, 3000);
                fragmentPanel.setLevelData(currentLevel);
                updateStatus("Hit! +" + reward + " points.");
            }
        } else {
            registerShot(false);
            score = Math.max(0, score - 15);
            updateStatus("Miss (" + (int) dist + " px). Try again.");
        }
    }

    private void applyLevelToViews() {
        if (displayedMapImage != currentLevel.mapImage()) {
            displayedMapImage = currentLevel.mapImage();
            mapPanel.setMapImage(displayedMapImage);
            initCarsForCurrentMap();
        }
        mapPanel.clearLastClick();
        fragmentPanel.setLevelData(currentLevel);
        pushCarPositionsToPanels();
    }

    private void initCarsForCurrentMap() {
        cars.clear();
        carPositions.clear();

        List<RoadPath> roads = currentLevel.roads();
        if (roads == null || roads.isEmpty()) {
            return;
        }

        int targetCarCount = Math.max(18, roads.size() / 2);
        int attempts = targetCarCount * 8;

        for (int i = 0; i < attempts && cars.size() < targetCarCount; i++) {
            RoadPath road = roads.get(random.nextInt(roads.size()));
            if (road.points().size() < 2) {
                continue;
            }

            double[] cumulative = buildCumulativeLengths(road);
            double totalLength = cumulative[cumulative.length - 1];
            if (totalLength < 8.0) {
                continue;
            }

            double progress = random.nextDouble();
            double speed = 0.0015 + random.nextDouble() * 0.004;
            if (random.nextBoolean()) {
                speed = -speed;
            }

            cars.add(new CarState(road, progress, speed, cumulative, totalLength));
        }

        updateCars();
    }

    private double[] buildCumulativeLengths(RoadPath road) {
        List<MapPoint> points = road.points();
        double[] cumulative = new double[points.size()];
        cumulative[0] = 0.0;

        for (int i = 1; i < points.size(); i++) {
            MapPoint a = points.get(i - 1);
            MapPoint b = points.get(i);
            double segmentLength = Math.hypot(b.x() - a.x(), b.y() - a.y());
            cumulative[i] = cumulative[i - 1] + segmentLength;
        }

        return cumulative;
    }

    private void updateCars() {
        if (cars.isEmpty()) {
            carPositions.clear();
            return;
        }

        carPositions.clear();
        for (CarState car : cars) {
            car.progress += car.speed;
            while (car.progress >= 1.0) {
                car.progress -= 1.0;
            }
            while (car.progress < 0.0) {
                car.progress += 1.0;
            }

            carPositions.add(samplePointOnRoad(car));
        }
    }

    private MapPoint samplePointOnRoad(CarState car) {
        List<MapPoint> points = car.road.points();
        double targetDistance = car.progress * car.totalLength;

        int idx = 1;
        while (idx < car.cumulativeLengths.length && car.cumulativeLengths[idx] < targetDistance) {
            idx++;
        }

        if (idx >= car.cumulativeLengths.length) {
            return points.get(points.size() - 1);
        }

        double prevDistance = car.cumulativeLengths[idx - 1];
        double nextDistance = car.cumulativeLengths[idx];
        double segmentLength = Math.max(1e-9, nextDistance - prevDistance);
        double t = (targetDistance - prevDistance) / segmentLength;

        MapPoint a = points.get(idx - 1);
        MapPoint b = points.get(idx);
        double x = a.x() + (b.x() - a.x()) * t;
        double y = a.y() + (b.y() - a.y()) * t;
        return new MapPoint(x, y);
    }

    private void pushCarPositionsToPanels() {
        List<MapPoint> snapshot = List.copyOf(carPositions);
        mapPanel.setCarPositions(snapshot);
        fragmentPanel.setCarPositions(snapshot);
    }

    private void updateFps() {
        fpsFrames++;
        long now = System.currentTimeMillis();
        long elapsed = now - fpsWindowStartMs;
        if (elapsed >= 1000) {
            fps = (int) Math.round(fpsFrames * 1000.0 / Math.max(1L, elapsed));
            fpsFrames = 0;
            fpsWindowStartMs = now;
            updateStatus(lastStatusMessage);
        }
    }

    private void registerShot(boolean hit) {        shotHistory.add(hit);
        if (shotHistory.size() > MAX_SHOT_HISTORY) {
            shotHistory.remove(0);
        }
    }

    private String buildShotHistoryHtml() {
        StringBuilder sb = new StringBuilder();

        int missing = MAX_SHOT_HISTORY - shotHistory.size();
        for (int i = 0; i < missing; i++) {
            sb.append("<span style='color:#6f6f6f;'>&#9632;</span>");
        }

        for (boolean hit : shotHistory) {
            if (hit) {
                sb.append("<span style='color:#36c66d;'>&#9632;</span>");
            } else {
                sb.append("<span style='color:#d84e4e;'>&#9632;</span>");
            }
        }

        return sb.toString();
    }

    private void updateStatus(String message) {
        lastStatusMessage = message;
        String text = "Level: " + levelNumber + "   |   Score: " + score + "   |   FPS: " + fps
                + "   |   " + message + "   |   " + buildShotHistoryHtml();
        statusLabel.setText("<html><div style='text-align:center;'>" + text + "</div></html>");
    }
}