package org.example.mapgame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameWindow {
    private static final int MAX_SHOT_HISTORY = 15;

    private static final long HIT_ANIMATION_TOTAL_MS = 2600L;
    private static final long LEFT_HIT_FADE_DELAY_MS = 350L;
    private static final long LEFT_CLICK_EXPLOSION_MS = HIT_ANIMATION_TOTAL_MS;
    private static final long LEFT_HIT_FADE_MS = HIT_ANIMATION_TOTAL_MS - LEFT_HIT_FADE_DELAY_MS;
    private static final long RIGHT_FALL_MS = HIT_ANIMATION_TOTAL_MS;

    private static final int MAX_HIT_REWARD = 200;
    private static final int MIN_HIT_REWARD = 20;
    private static final long REWARD_DECAY_PERIOD_MS = 10_000L;
    private static final double REWARD_DECAY_FACTOR = 0.80;

    private final LevelGenerator levelGenerator = new LevelGenerator();
    private final Random random = new Random();

    private LevelData currentLevel;
    private BufferedImage displayedMapImage;

    private int score = 0;
    private int levelNumber = 1;
    private int nextMapScoreThreshold = 1000;

    private JLabel statusLabel;
    private MapPanel mapPanel;
    private FragmentPanel fragmentPanel;

    private final List<CarState> cars = new ArrayList<>();
    private final List<CarVisual> carVisuals = new ArrayList<>();
    private final List<Boolean> shotHistory = new ArrayList<>();

    private int fps = 0;
    private int fpsFrames = 0;
    private long fpsWindowStartMs = System.currentTimeMillis();
    private String lastStatusMessage = "";

    private long gameStartMs = 0L;
    private long levelStartMs = 0L;

    private boolean pendingPostHitTransition = false;
    private boolean pendingRegenerateMap = false;
    private long pendingTransitionAtMs = 0L;
    private long lastStatusRefreshMs = 0L;

    private static class CarState {
        private final RoadPath road;
        private final double[] cumulativeLengths;
        private final double totalLength;
        private final Color color;

        private double progress;
        private double speed;

        private CarState(RoadPath road, double progress, double speed,
                         double[] cumulativeLengths, double totalLength, Color color) {
            this.road = road;
            this.progress = progress;
            this.speed = speed;
            this.cumulativeLengths = cumulativeLengths;
            this.totalLength = totalLength;
            this.color = color;
        }
    }

    public void showWindow() {
        gameStartMs = System.currentTimeMillis();
        levelStartMs = gameStartMs;

        JFrame frame = new JFrame("Map Fragment Hunt");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton regenerateButton = new JButton("Regenerate Map");
        regenerateButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        regenerateButton.setMargin(new Insets(2, 8, 2, 8));
        regenerateButton.setPreferredSize(new Dimension(130, 24));
        regenerateButton.addActionListener(e -> regenerateMap());

        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(3, 6, 0, 6));
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
        fragmentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLevelGeneratorViewport();
                updateFragmentGuideOverlay();
            }
        });

        JPanel content = new JPanel(new GridLayout(1, 2, 8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(mapPanel);
        content.add(fragmentPanel);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        updateLevelGeneratorViewport();
        currentLevel = levelGenerator.generateLevel();
        applyLevelToViews();
        updateStatus("Find right fragment on the left map and click near its center.");

        Timer timer = new Timer(33, e -> {
            updateCars();
            pushCarVisualsToPanels();
            processPendingPostHitTransition();
            updateFps();
            refreshDynamicStatus();
        });
        timer.start();
    }

    private void regenerateMap() {
        pendingPostHitTransition = false;
        updateLevelGeneratorViewport();
        currentLevel = levelGenerator.generateLevel();

        applyLevelToViews();
        updateStatus("Map updated.");
    }

    private void handleMapClick(int mapX, int mapY) {
        if (currentLevel == null || pendingPostHitTransition) {
            return;
        }

        mapPanel.setLastClick(mapX, mapY);
        mapPanel.showClickExplosion(mapX, mapY, LEFT_CLICK_EXPLOSION_MS);

        double dist = Math.hypot(mapX - currentLevel.targetX(), mapY - currentLevel.targetY());
        if (dist <= GameConfig.HIT_RADIUS) {
            registerShot(true);

            int previousTargetX = currentLevel.targetX();
            int previousTargetY = currentLevel.targetY();

            int reward = computeCurrentHitReward();
            score += reward;
            levelNumber++;

            pendingRegenerateMap = false;
            while (score >= nextMapScoreThreshold) {
                pendingRegenerateMap = true;
                nextMapScoreThreshold += 1000;
            }
            pendingPostHitTransition = true;

            mapPanel.showFadingTarget(previousTargetX, previousTargetY, LEFT_HIT_FADE_MS, LEFT_HIT_FADE_DELAY_MS);
            fragmentPanel.startFallAnimation(RIGHT_FALL_MS);

            pendingTransitionAtMs = System.currentTimeMillis() + HIT_ANIMATION_TOTAL_MS;

            updateStatus("Hit! +" + reward + " points.");
        } else {
            registerShot(false);
            score = Math.max(0, score - 15);
            updateStatus("Miss (" + (int) dist + " px). Try again.");
        }
    }

    private void processPendingPostHitTransition() {
        if (!pendingPostHitTransition) {
            return;
        }
        if (System.currentTimeMillis() < pendingTransitionAtMs) {
            return;
        }

        pendingPostHitTransition = false;

        if (pendingRegenerateMap) {
            updateLevelGeneratorViewport();
            currentLevel = levelGenerator.generateLevel();
    
            applyLevelToViews();
        } else {
            updateLevelGeneratorViewport();
            currentLevel = levelGenerator.generateLevelForExistingMap(currentLevel);
            levelStartMs = System.currentTimeMillis();
            mapPanel.clearLastClick();
            fragmentPanel.setLevelData(currentLevel);
            updateFragmentGuideOverlay();
            pushCarVisualsToPanels();
        }
    }

    private void updateLevelGeneratorViewport() {
        if (fragmentPanel == null) {
            return;
        }
        levelGenerator.setFragmentViewportSize(fragmentPanel.getWidth(), fragmentPanel.getHeight());
    }

    private void updateFragmentGuideOverlay() {
        if (mapPanel == null) {
            return;
        }
        if (!GameConfig.GRID_ENABLED || currentLevel == null || fragmentPanel == null) {
            mapPanel.clearFragmentGuide();
            return;
        }

        int panelW = Math.max(1, fragmentPanel.getWidth());
        int panelH = Math.max(1, fragmentPanel.getHeight());
        double baseScale = Math.max((double) panelW / GameConfig.MAP_WIDTH,
                (double) panelH / GameConfig.MAP_HEIGHT);
        double scale = baseScale * currentLevel.targetZoom();
        double halfW = panelW / (2.0 * scale);
        double halfH = panelH / (2.0 * scale);

        double c = Math.cos(currentLevel.targetAngleRad());
        double s = Math.sin(currentLevel.targetAngleRad());
        double cx = currentLevel.targetX();
        double cy = currentLevel.targetY();

        double[] dx = new double[]{-halfW, halfW, halfW, -halfW};
        double[] dy = new double[]{-halfH, -halfH, halfH, halfH};
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
            xs[i] = cx + dx[i] * c - dy[i] * s;
            ys[i] = cy + dx[i] * s + dy[i] * c;
        }

        mapPanel.setFragmentGuide(cx, cy, xs, ys);
    }

    private void applyLevelToViews() {
        if (displayedMapImage != currentLevel.mapImage()) {
            displayedMapImage = currentLevel.mapImage();
            mapPanel.setMapImage(displayedMapImage);
            initCarsForCurrentMap();
        }
        levelStartMs = System.currentTimeMillis();
        mapPanel.clearLastClick();
        fragmentPanel.setLevelData(currentLevel);
        updateFragmentGuideOverlay();
        pushCarVisualsToPanels();
    }

    private void initCarsForCurrentMap() {
        cars.clear();
        carVisuals.clear();

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

            Color color = randomCarColor();
            cars.add(new CarState(road, progress, speed, cumulative, totalLength, color));
        }

        updateCars();
    }

    private Color randomCarColor() {
        float hue = random.nextFloat();
        float saturation = 0.75f + random.nextFloat() * 0.2f;
        float brightness = 0.88f + random.nextFloat() * 0.12f;
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 190);
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
            carVisuals.clear();
            return;
        }

        carVisuals.clear();
        for (CarState car : cars) {
            car.progress += car.speed;
            while (car.progress >= 1.0) {
                car.progress -= 1.0;
            }
            while (car.progress < 0.0) {
                car.progress += 1.0;
            }

            MapPoint p = samplePointOnRoad(car);
            carVisuals.add(new CarVisual(p.x(), p.y(), car.color));
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

    private void pushCarVisualsToPanels() {
        List<CarVisual> snapshot = Collections.unmodifiableList(new ArrayList<CarVisual>(carVisuals));
        mapPanel.setCars(snapshot);
        fragmentPanel.setCars(snapshot);
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

    private void refreshDynamicStatus() {
        if (pendingPostHitTransition || currentLevel == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastStatusRefreshMs < 120L) {
            return;
        }
        lastStatusRefreshMs = now;
        updateStatus(lastStatusMessage);
    }

    private int computeCurrentHitReward() {
        if (currentLevel == null) {
            return MAX_HIT_REWARD;
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - levelStartMs);
        double decaySteps = (double) elapsed / REWARD_DECAY_PERIOD_MS;
        double span = MAX_HIT_REWARD - MIN_HIT_REWARD;
        double value = MIN_HIT_REWARD + span * Math.pow(REWARD_DECAY_FACTOR, decaySteps);
        return Math.max(MIN_HIT_REWARD, (int) Math.round(value));
    }

    private double getCurrentRewardRatio() {
        double ratio = (double) computeCurrentHitReward() / MAX_HIT_REWARD;
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private String buildRewardBarHtml() {
        int segments = 20;
        int filled = (int) Math.round(getCurrentRewardRatio() * segments);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                sb.append("<span style='color:#f0c75e;'>&#9646;</span>");
            } else {
                sb.append("<span style='color:#555555;'>&#9646;</span>");
            }
        }
        return sb.toString();
    }

    private void registerShot(boolean hit) {
        shotHistory.add(hit);
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
        String totalTime = formatDuration(System.currentTimeMillis() - gameStartMs);
        String levelTime = formatDuration(System.currentTimeMillis() - levelStartMs);
        int mapW = currentLevel != null ? currentLevel.mapImage().getWidth() : GameConfig.MAP_WIDTH;
        int mapH = currentLevel != null ? currentLevel.mapImage().getHeight() : GameConfig.MAP_HEIGHT;
        String mapSize = mapW + "x" + mapH;

        int potentialReward = computeCurrentHitReward();
        String rewardBar = buildRewardBarHtml();

        String text = "Level: " + levelNumber + "   |   Score: " + score + "   |   FPS: " + fps
                + "   |   Map: " + mapSize
                + "   |   Potential: " + potentialReward + " " + rewardBar
                + "   |   Total: " + totalTime + "   |   Level: " + levelTime
                + "   |   " + message + "   |   " + buildShotHistoryHtml();
        statusLabel.setText("<html><div style='text-align:center;'>" + text + "</div></html>");
    }

    private String formatDuration(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
