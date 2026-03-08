package org.example.mapgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LevelGenerator {
    private final Random random = new Random();

    private record Node(int x, int y) {
    }

    private record AngleAnchor(int x, int y, double angleRad) {
    }

    public LevelData generateLevel() {
        return generateLevelForMap(generateMapImage());
    }

    public LevelData generateLevelForMap(BufferedImage mapImage) {
        int marginX = Math.max(GameConfig.TARGET_MARGIN_X, GameConfig.MAP_WIDTH / 8);
        int marginY = Math.max(GameConfig.TARGET_MARGIN_Y, GameConfig.MAP_HEIGHT / 8);

        int targetX = marginX + random.nextInt(Math.max(1, GameConfig.MAP_WIDTH - marginX * 2));
        int targetY = marginY + random.nextInt(Math.max(1, GameConfig.MAP_HEIGHT - marginY * 2));
        double targetAngleRad = random.nextDouble() * Math.PI * 2.0;
        double targetZoom = GameConfig.TARGET_MIN_ZOOM + random.nextDouble() * GameConfig.TARGET_ZOOM_RANGE;

        return new LevelData(mapImage, targetX, targetY, targetAngleRad, targetZoom);
    }

    private BufferedImage generateMapImage() {
        BufferedImage mapImage = new BufferedImage(GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = mapImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(18, 22, 28));
        g2.fillRect(0, 0, GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT);

        List<AngleAnchor> angleAnchors = createAngleAnchors();

        int baseRectCount = GameConfig.MAP_RECTANGLES;
        double sizeScale = Math.sqrt((double) baseRectCount / Math.max(1, GameConfig.MAP_RECTANGLES));
        sizeScale = Math.max(0.45, Math.min(2.2, sizeScale));

        int minRectSize = Math.max(4, (int) Math.round(20 * sizeScale));
        int maxRectSize = Math.max(minRectSize + 2, (int) Math.round(289 * sizeScale));
        int rectSpan = maxRectSize - minRectSize + 1;

        for (int i = 0; i < GameConfig.MAP_RECTANGLES; i++) {
            int w = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int h = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int cx = random.nextInt(GameConfig.MAP_WIDTH);
            int cy = random.nextInt(GameConfig.MAP_HEIGHT);

            int area = w * h;
            int minArea = minRectSize * minRectSize;
            int maxArea = maxRectSize * maxRectSize;
            double t = (double) (area - minArea) / (maxArea - minArea);
            t = Math.max(0.0, Math.min(1.0, t));
            int alpha = (int) Math.round(220 - t * 150);
            Color color = random.nextDouble() < 0.60
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

        drawRoads(g2);
        g2.dispose();

        return mapImage;
    }

    private List<AngleAnchor> createAngleAnchors() {
        int overflowX = (int) Math.round(GameConfig.MAP_WIDTH * GameConfig.RECT_ANGLE_ANCHOR_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(GameConfig.MAP_HEIGHT * GameConfig.RECT_ANGLE_ANCHOR_OVERFLOW_RATIO);

        List<AngleAnchor> anchors = new ArrayList<>();
        for (int i = 0; i < GameConfig.RECT_ANGLE_ANCHOR_COUNT; i++) {
            int x = -overflowX + random.nextInt(GameConfig.MAP_WIDTH + overflowX * 2);
            int y = -overflowY + random.nextInt(GameConfig.MAP_HEIGHT + overflowY * 2);
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
        int rgb = mapImage.getRGB(x, y);
        Color base = new Color(rgb);
        if (base.getRed() == 0 && base.getGreen() == 0 && base.getBlue() == 0) {
            return randomWarmColor(alpha);
        }
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private Color randomWarmColor(int alpha) {
        float hue = random.nextFloat() * 0.55f;

        float satPick = random.nextFloat();
        float saturation;
        if (satPick < 0.35f) {
            saturation = 0.18f + random.nextFloat() * 0.16f;
        } else if (satPick < 0.8f) {
            saturation = 0.34f + random.nextFloat() * 0.22f;
        } else {
            saturation = 0.56f + random.nextFloat() * 0.18f;
        }

        float brightness = 0.52f + random.nextFloat() * 0.36f;
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

    private void drawRoads(Graphics2D g2) {
        int overflowX = (int) Math.round(GameConfig.MAP_WIDTH * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(GameConfig.MAP_HEIGHT * GameConfig.ROAD_OVERFLOW_RATIO);

        List<Node> nodes = new ArrayList<>();
        int maxAttempts = GameConfig.ROAD_NODE_COUNT * 120;
        int attempts = 0;

        while (nodes.size() < GameConfig.ROAD_NODE_COUNT && attempts < maxAttempts) {
            attempts++;
            int x = -overflowX + random.nextInt(GameConfig.MAP_WIDTH + overflowX * 2);
            int y = -overflowY + random.nextInt(GameConfig.MAP_HEIGHT + overflowY * 2);
            Node candidate = new Node(x, y);

            if (isFarFromExistingNodes(candidate, nodes, GameConfig.ROAD_MIN_NODE_SPACING)) {
                nodes.add(candidate);
            }
        }

        Set<Long> edges = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            connectNodeWithDirectionalSpread(nodes, i, edges);
        }

        for (long edge : edges) {
            int a = (int) (edge >> 32);
            int b = (int) (edge & 0xFFFFFFFFL);
            drawRoadBetweenNodes(g2, nodes.get(a), nodes.get(b));
        }
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

    private void connectNodeWithDirectionalSpread(List<Node> nodes, int index, Set<Long> edges) {
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

            if (tryAddEdge(edges, index, candidate)) {
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

            if (tryAddEdge(edges, index, candidate)) {
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

    private boolean tryAddEdge(Set<Long> edges, int i, int j) {
        if (i == j) {
            return false;
        }
        return edges.add(edgeKey(i, j));
    }

    private long edgeKey(int i, int j) {
        int a = Math.min(i, j);
        int b = Math.max(i, j);
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    private void drawRoadBetweenNodes(Graphics2D g2, Node a, Node b) {
        double dist = Math.hypot(b.x - a.x, b.y - a.y);
        if (dist < 1.0) {
            return;
        }

        Path2D.Double road = new Path2D.Double();
        road.moveTo(a.x, a.y);
        appendFractalRoadSegment(road, a.x, a.y, b.x, b.y, 0);

        float roadWidth = 2.0f + random.nextFloat() * 2.6f;
        Color roadColor = randomCoolDarkColor(185 + random.nextInt(50));

        g2.setStroke(new BasicStroke(roadWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(roadColor);
        g2.draw(road);
    }

    private void appendFractalRoadSegment(Path2D.Double path, double x1, double y1, double x2, double y2, int depth) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);

        if (dist < 5.0) {
            path.lineTo(x2, y2);
            return;
        }

        double midX = (x1 + x2) * 0.5;
        double midY = (y1 + y2) * 0.5;

        double decay = Math.pow(0.72, depth);
        double maxShift = dist * 0.30 * decay;
        double shift = random.nextDouble() * maxShift;
        double angle = random.nextDouble() * Math.PI * 2.0;
        midX += Math.cos(angle) * shift;
        midY += Math.sin(angle) * shift;

        int overflowX = (int) Math.round(GameConfig.MAP_WIDTH * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(GameConfig.MAP_HEIGHT * GameConfig.ROAD_OVERFLOW_RATIO);
        midX = clamp((int) Math.round(midX), -overflowX, GameConfig.MAP_WIDTH - 1 + overflowX);
        midY = clamp((int) Math.round(midY), -overflowY, GameConfig.MAP_HEIGHT - 1 + overflowY);

        appendFractalRoadSegment(path, x1, y1, midX, midY, depth + 1);
        appendFractalRoadSegment(path, midX, midY, x2, y2, depth + 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
