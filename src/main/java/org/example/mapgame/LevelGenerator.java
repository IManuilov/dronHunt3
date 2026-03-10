package org.example.mapgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.example.mapgame.GameConfig.BLACK_THRESHOLD;

public class LevelGenerator {
    private final Random random = new Random();

    private int generatedMapCount = 0;
    private int currentMapWidth = GameConfig.MAP_WIDTH;
    private int currentMapHeight = GameConfig.MAP_HEIGHT;

    private int fragmentViewportWidth = 900;
    private int fragmentViewportHeight = 900;

    private static final class Node {
        private final int x;
        private final int y;

        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class AngleAnchor {
        private final int x;
        private final int y;
        private final double angleRad;

        private AngleAnchor(int x, int y, double angleRad) {
            this.x = x;
            this.y = y;
            this.angleRad = angleRad;
        }
    }

    private static final class GeneratedMap {
        private final BufferedImage mapImage;
        private final List<RoadPath> roads;

        private GeneratedMap(BufferedImage mapImage, List<RoadPath> roads) {
            this.mapImage = mapImage;
            this.roads = roads;
        }

        private BufferedImage mapImage() {
            return mapImage;
        }

        private List<RoadPath> roads() {
            return roads;
        }
    }

    public void setFragmentViewportSize(int width, int height) {
        this.fragmentViewportWidth = Math.max(1, width);
        this.fragmentViewportHeight = Math.max(1, height);
    }

    public LevelData generateLevel() {
        GeneratedMap generatedMap = generateMapData();
        return createLevelData(generatedMap.mapImage(), generatedMap.roads());
    }

    public LevelData generateLevelForExistingMap(LevelData currentLevel) {
        return createLevelData(currentLevel.mapImage(), currentLevel.roads());
    }

    private LevelData createLevelData(BufferedImage mapImage, List<RoadPath> roads) {
        int mapWidth = mapImage.getWidth();
        int mapHeight = mapImage.getHeight();

        double panelW = Math.max(1, fragmentViewportWidth);
        double panelH = Math.max(1, fragmentViewportHeight);
        double refBaseScale = Math.max(panelW / GameConfig.MAP_WIDTH, panelH / GameConfig.MAP_HEIGHT);
        double refHalfW = panelW / refBaseScale / 2.0;
        double refHalfH = panelH / refBaseScale / 2.0;
        double maxZoom = GameConfig.TARGET_MIN_ZOOM + GameConfig.TARGET_ZOOM_RANGE;

        // Pick angle first, then compute minimum zoom that keeps
        // the rotated fragment fully inside the map with margins.
        for (int attempt = 0; attempt < 320; attempt++) {
            double targetAngleRad = random.nextDouble() * Math.PI * 2.0;
            double cos = Math.abs(Math.cos(targetAngleRad));
            double sin = Math.abs(Math.sin(targetAngleRad));

            double maxAllowedX = mapWidth / 2.0 - GameConfig.TARGET_MARGIN_X;
            double maxAllowedY = mapHeight / 2.0 - GameConfig.TARGET_MARGIN_Y;
            if (maxAllowedX <= 2.0 || maxAllowedY <= 2.0) {
                break;
            }

            double needZoomX = (cos * refHalfW + sin * refHalfH) / maxAllowedX;
            double needZoomY = (sin * refHalfW + cos * refHalfH) / maxAllowedY;
            double minZoomForFit = Math.max(GameConfig.TARGET_MIN_ZOOM, Math.max(needZoomX, needZoomY));
            if (minZoomForFit > maxZoom) {
                continue;
            }

            double targetZoom = minZoomForFit + random.nextDouble() * (maxZoom - minZoomForFit);

            double halfViewW = refHalfW / targetZoom;
            double halfViewH = refHalfH / targetZoom;

            int marginX = (int) Math.ceil(cos * halfViewW + sin * halfViewH) + 1;
            int marginY = (int) Math.ceil(sin * halfViewW + cos * halfViewH) + 1;
            marginX = Math.max(marginX, GameConfig.TARGET_MARGIN_X);
            marginY = Math.max(marginY, GameConfig.TARGET_MARGIN_Y);

            int rangeX = mapWidth - marginX * 2;
            int rangeY = mapHeight - marginY * 2;
            if (rangeX <= 0 || rangeY <= 0) {
                continue;
            }

            int targetX = marginX + random.nextInt(rangeX);
            int targetY = marginY + random.nextInt(rangeY);
            return new LevelData(mapImage, roads, targetX, targetY, targetAngleRad, targetZoom);
        }

        int fallbackX = mapWidth / 2;
        int fallbackY = mapHeight / 2;
        double fallbackAngle = 0.0;
        double fallbackZoom = maxZoom;
        return new LevelData(mapImage, roads, fallbackX, fallbackY, fallbackAngle, fallbackZoom);
    }

    private GeneratedMap generateMapData() {
        currentMapWidth = nextMapDimension(GameConfig.MAP_WIDTH, generatedMapCount);
        currentMapHeight = nextMapDimension(GameConfig.MAP_HEIGHT, generatedMapCount);
        generatedMapCount++;

        BufferedImage mapImage = new BufferedImage(currentMapWidth, currentMapHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mapImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(18, 22, 28));
        g2.fillRect(0, 0, currentMapWidth, currentMapHeight);

        double areaScale = ((double) currentMapWidth * currentMapHeight)
                / ((double) GameConfig.MAP_WIDTH * GameConfig.MAP_HEIGHT);

        int anchorCount = scaleCount(GameConfig.RECT_ANGLE_ANCHOR_COUNT, areaScale, 8);
        int rectCount = scaleCount(GameConfig.MAP_RECTANGLES, areaScale, 8);
        int roadNodeCount = scaleCount(GameConfig.ROAD_NODE_COUNT, areaScale, 12);


        List<AngleAnchor> angleAnchors = createAngleAnchors(anchorCount);

        int baseRectCount = 1400;
        double sizeScale = Math.sqrt((double) baseRectCount / Math.max(1, rectCount));
        sizeScale = Math.max(0.45, Math.min(8.2, sizeScale));

        int minRectSize = 50;//Math.max(12, (int) Math.round(30 * sizeScale));
        int maxRectSize = 700;//Math.max(minRectSize + 2, (int) Math.round(554 * sizeScale));
        int rectSpan = maxRectSize - minRectSize + 1;

        System.out.println("mapw "+ currentMapWidth + "  anch " + anchorCount + "  rect " + rectCount + "  nodes " + roadNodeCount
            + "  maxRectSize " + maxRectSize);


        for (int i = 0; i < rectCount; i++) {
            int w = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int h = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int cx = random.nextInt(currentMapWidth);
            int cy = random.nextInt(currentMapHeight);

            int area = w * h;
            int minArea = minRectSize * minRectSize;
            int maxArea = maxRectSize * maxRectSize;
            double t = (double) (area - minArea) / (maxArea - minArea);
            t = Math.max(0.0, Math.min(1.0, t));
            int alpha = (int) Math.round(220 - t * 150);
            Color color = random.nextDouble() < 0.80
                    ? colorFromMapAtPointOrGenerated(mapImage, cx, cy, alpha)
                    : randomWarmColor(alpha);

            double angle = getNearestAnchorAngle(angleAnchors, cx, cy)
                    + (random.nextDouble() - 0.5) * 2.0 * GameConfig.LOCAL_ANGLE_JITTER_RAD;

            AffineTransform old = g2.getTransform();
            g2.translate(cx, cy);
            g2.rotate(angle);
            g2.setColor(color);
            g2.fillRect(-w / 2, -h / 2, w, h);
            g2.setTransform(old);
        }

        List<RoadPath> roads = drawRoads(g2, roadNodeCount);
        if (GameConfig.GRID_ENABLED) {
            drawGrid(g2);
        }
        g2.dispose();

        return new GeneratedMap(mapImage, Collections.unmodifiableList(new ArrayList<RoadPath>(roads)));
    }

    private void drawGrid(Graphics2D g2) {
        int step = Math.max(8, GameConfig.GRID_STEP);
        g2.setColor(new Color(255, 255, 255, clamp(GameConfig.GRID_ALPHA, 0, 255)));
        g2.setStroke(new BasicStroke(1f));

        for (int x = 0; x <= currentMapWidth; x += step) {
            g2.drawLine(x, 0, x, currentMapHeight);
        }
        for (int y = 0; y <= currentMapHeight; y += step) {
            g2.drawLine(0, y, currentMapWidth, y);
        }
    }
    private int nextMapDimension(int baseDimension, int mapIndex) {
        int start = Math.max(320, baseDimension / 4);
        double growth = Math.pow(1.09, mapIndex);
        int value = (int) Math.round(start * growth);
        return Math.min(baseDimension, value);
    }

    private int scaleCount(int baseCount, double areaScale, int minValue) {
        int value = (int) Math.round(baseCount * areaScale);
        return Math.max(minValue, value);
    }

    private List<AngleAnchor> createAngleAnchors(int anchorCount) {
        int overflowX = (int) Math.round(currentMapWidth * GameConfig.RECT_ANGLE_ANCHOR_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(currentMapHeight * GameConfig.RECT_ANGLE_ANCHOR_OVERFLOW_RATIO);

        List<AngleAnchor> anchors = new ArrayList<>();
        for (int i = 0; i < anchorCount; i++) {
            int x = -overflowX + random.nextInt(currentMapWidth + overflowX * 2);
            int y = -overflowY + random.nextInt(currentMapHeight + overflowY * 2);
            double angleRad = random.nextDouble() * Math.PI * 2.0;
            anchors.add(new AngleAnchor(x, y, angleRad));
        }

        return anchors;
    }

    private double getNearestAnchorAngle(List<AngleAnchor> anchors, int x, int y) {
        AngleAnchor best = anchors.get(0);
        long bestDistSq = Long.MAX_VALUE;

        for (AngleAnchor anchor : anchors) {
            long dx = (long) x - anchor.x;
            long dy = (long) y - anchor.y;
            long distSq = dx * dx + dy * dy;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = anchor;
            }
        }

        return best.angleRad;
    }

    private Color colorFromMapAtPointOrGenerated(BufferedImage mapImage, int x, int y, int alpha) {
        Color base = new Color(mapImage.getRGB(x, y));
        if (!isTooDark(base)) {
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
        }

        int retryRadius = 94;
        for (int attempt = 0; attempt < 5; attempt++) {
            int nx = clamp(x + random.nextInt(retryRadius * 2 + 1) - retryRadius, 0, currentMapWidth - 1);
            int ny = clamp(y + random.nextInt(retryRadius * 2 + 1) - retryRadius, 0, currentMapHeight - 1);
            Color nearby = new Color(mapImage.getRGB(nx, ny));
            if (!isTooDark(nearby)) {
                return new Color(nearby.getRed(), nearby.getGreen(), nearby.getBlue(), alpha);
            }
        }

        return randomWarmColor(alpha);
    }

    private boolean isTooDark(Color color) {
        return color.getRed() < BLACK_THRESHOLD
                && color.getGreen() < BLACK_THRESHOLD
                && color.getBlue() < BLACK_THRESHOLD;
    }

    private Color randomWarmColor(int alpha) {
        float hue = random.nextFloat() * 0.59f;
        float saturation = 0.09f + random.nextFloat() * 0.60f;
        float brightness = 0.40f + random.nextFloat() * 0.39f;
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    private Color randomCoolDarkColor(int alpha) {
        float hue = 0.50f + random.nextFloat() * 0.22f;
        float saturation = 0.35f + random.nextFloat() * 0.45f;
        float brightness = 0.18f + random.nextFloat() * 0.28f;
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    private List<RoadPath> drawRoads(Graphics2D g2, int roadNodeCount) {
        int overflowX = (int) Math.round(currentMapWidth * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(currentMapHeight * GameConfig.ROAD_OVERFLOW_RATIO);

        List<Node> nodes = new ArrayList<>();
        int maxAttempts = roadNodeCount * 120;
        int attempts = 0;

        while (nodes.size() < roadNodeCount && attempts < maxAttempts) {
            attempts++;
            int x = -overflowX + random.nextInt(currentMapWidth + overflowX * 2);
            int y = -overflowY + random.nextInt(currentMapHeight + overflowY * 2);
            Node candidate = new Node(x, y);

            if (isFarFromExistingNodes(candidate, nodes, GameConfig.ROAD_MIN_NODE_SPACING)) {
                nodes.add(candidate);
            }
        }

        Set<Long> edges = new HashSet<>();
        int[] degrees = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            connectNodeWithDirectionalSpread(nodes, i, edges, degrees);
        }

        List<RoadPath> roads = new ArrayList<>();
        for (long edge : edges) {
            int a = (int) (edge >> 32);
            int b = (int) (edge & 0xFFFFFFFFL);
            RoadPath road = drawRoadBetweenNodes(g2, nodes.get(a), nodes.get(b));
            if (road != null && road.points().size() >= 2) {
                roads.add(road);
            }
        }
        return roads;
    }

    private boolean isFarFromExistingNodes(Node candidate, List<Node> nodes, int minSpacing) {
        int minDistSq = minSpacing * minSpacing;
        for (Node n : nodes) {
            int dx = candidate.x - n.x;
            int dy = candidate.y - n.y;
            int distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                return false;
            }
        }
        return true;
    }

    private void connectNodeWithDirectionalSpread(List<Node> nodes, int index, Set<Long> edges, int[] degrees) {
        if (degrees[index] >= GameConfig.ROAD_MAX_EDGES_PER_NODE) {
            return;
        }

        Node center = nodes.get(index);
        List<Integer> candidates = collectCandidatesSortedByDistance(nodes, index);
        if (candidates.isEmpty()) {
            return;
        }

        boolean[] usedSector = new boolean[GameConfig.ROAD_DIRECTION_SECTORS];
        List<Double> usedAngles = new ArrayList<>();
        int connected = 0;

        for (int candidate : candidates) {
            if (connected >= GameConfig.ROAD_LINKS_PER_NODE) {
                break;
            }

            Node other = nodes.get(candidate);
            double angle = Math.atan2(other.y - center.y, other.x - center.x);
            int sector = angleToSector(angle);

            if (usedSector[sector]) {
                continue;
            }

            if (tryAddEdge(edges, index, candidate, degrees)) {
                usedSector[sector] = true;
                usedAngles.add(angle);
                connected++;
            }
        }

        for (int candidate : candidates) {
            if (connected >= GameConfig.ROAD_LINKS_PER_NODE) {
                break;
            }

            Node other = nodes.get(candidate);
            double angle = Math.atan2(other.y - center.y, other.x - center.x);
            if (!isAngleFarEnough(angle, usedAngles, GameConfig.ROAD_MIN_ANGLE_SEPARATION_RAD)) {
                continue;
            }

            if (tryAddEdge(edges, index, candidate, degrees)) {
                usedAngles.add(angle);
                connected++;
            }
        }
    }

    private List<Integer> collectCandidatesSortedByDistance(List<Node> nodes, int index) {
        Node center = nodes.get(index);
        List<Integer> candidates = new ArrayList<>();

        for (int j = 0; j < nodes.size(); j++) {
            if (j == index) {
                continue;
            }

            Node other = nodes.get(j);
            double dist = Math.hypot(center.x - other.x, center.y - other.y);
            if (dist >= GameConfig.ROAD_MIN_DIST && dist <= GameConfig.ROAD_MAX_DIST) {
                candidates.add(j);
            }
        }

        candidates.sort((a, b) -> {
            Node na = nodes.get(a);
            Node nb = nodes.get(b);
            double da = Math.hypot(center.x - na.x, center.y - na.y);
            double db = Math.hypot(center.x - nb.x, center.y - nb.y);
            return Double.compare(da, db);
        });

        return candidates;
    }

    private int angleToSector(double angle) {
        double normalized = angle;
        while (normalized < 0) {
            normalized += Math.PI * 2.0;
        }
        while (normalized >= Math.PI * 2.0) {
            normalized -= Math.PI * 2.0;
        }

        int sector = (int) (normalized / (Math.PI * 2.0) * GameConfig.ROAD_DIRECTION_SECTORS);
        return clamp(sector, 0, GameConfig.ROAD_DIRECTION_SECTORS - 1);
    }

    private boolean isAngleFarEnough(double angle, List<Double> usedAngles, double minSeparation) {
        for (double used : usedAngles) {
            double diff = Math.abs(angle - used);
            if (diff > Math.PI) {
                diff = Math.PI * 2.0 - diff;
            }
            if (diff < minSeparation) {
                return false;
            }
        }
        return true;
    }

    private boolean tryAddEdge(Set<Long> edges, int i, int j, int[] degrees) {
        if (i == j) {
            return false;
        }
        if (degrees[i] >= GameConfig.ROAD_MAX_EDGES_PER_NODE || degrees[j] >= GameConfig.ROAD_MAX_EDGES_PER_NODE) {
            return false;
        }

        long key = edgeKey(i, j);
        if (!edges.add(key)) {
            return false;
        }

        degrees[i]++;
        degrees[j]++;
        return true;
    }

    private long edgeKey(int i, int j) {
        int a = Math.min(i, j);
        int b = Math.max(i, j);
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    private RoadPath drawRoadBetweenNodes(Graphics2D g2, Node a, Node b) {
        double dist = Math.hypot(b.x - a.x, b.y - a.y);
        if (dist < 1.0) {
            return null;
        }

        Path2D.Double road = new Path2D.Double();
        road.moveTo(a.x, a.y);

        List<MapPoint> points = new ArrayList<>();
        points.add(new MapPoint(a.x, a.y));
        appendFractalRoadSegment(road, points, a.x, a.y, b.x, b.y, 0);

        float roadWidth = 2.0f + random.nextFloat() * 2.6f;
        Color roadColor = randomCoolDarkColor(185 + random.nextInt(50));

        g2.setStroke(new BasicStroke(roadWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(roadColor);
        g2.draw(road);

        return new RoadPath(Collections.unmodifiableList(new ArrayList<MapPoint>(points)));
    }

    private void appendFractalRoadSegment(Path2D.Double path, List<MapPoint> points,
                                          double x1, double y1, double x2, double y2, int depth) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);

        if (dist < 9.0) {
            path.lineTo(x2, y2);
            points.add(new MapPoint(x2, y2));
            return;
        }

        double midX = (x1 + x2) * 0.5;
        double midY = (y1 + y2) * 0.5;

        double decay = Math.pow(0.70, depth);
        double maxShift = dist * 0.32 * decay;
        double shift = random.nextDouble() * maxShift;
        double angle = random.nextDouble() * Math.PI * 2.0;
        midX += Math.cos(angle) * shift;
        midY += Math.sin(angle) * shift;

        int overflowX = (int) Math.round(currentMapWidth * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(currentMapHeight * GameConfig.ROAD_OVERFLOW_RATIO);
        midX = clamp((int) Math.round(midX), -overflowX, currentMapWidth - 1 + overflowX);
        midY = clamp((int) Math.round(midY), -overflowY, currentMapHeight - 1 + overflowY);

        appendFractalRoadSegment(path, points, x1, y1, midX, midY, depth + 1);
        appendFractalRoadSegment(path, points, midX, midY, x2, y2, depth + 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
