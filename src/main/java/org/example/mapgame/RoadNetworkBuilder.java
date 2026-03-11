package org.example.mapgame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class RoadNetworkBuilder {
    private final int mapWidth;
    private final int mapHeight;
    private final Random random;

    private static final class Node {
        private final int x;
        private final int y;

        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public RoadNetworkBuilder(int mapWidth, int mapHeight, Random random) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.random = random;
    }

    public List<RoadPath> buildRoadPaths(int roadNodeCount) {
        int overflowX = (int) Math.round(mapWidth * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(mapHeight * GameConfig.ROAD_OVERFLOW_RATIO);

        List<Node> nodes = new ArrayList<>();
        int maxAttempts = roadNodeCount * 120;
        int attempts = 0;

        while (nodes.size() < roadNodeCount && attempts < maxAttempts) {
            attempts++;
            int x = -overflowX + random.nextInt(mapWidth + overflowX * 2);
            int y = -overflowY + random.nextInt(mapHeight + overflowY * 2);
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
            RoadPath road = buildRoadBetweenNodes(nodes.get(a), nodes.get(b));
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

    private RoadPath buildRoadBetweenNodes(Node a, Node b) {
        double dist = Math.hypot(b.x - a.x, b.y - a.y);
        if (dist < 1.0) {
            return null;
        }

        List<MapPoint> points = new ArrayList<>();
        points.add(new MapPoint(a.x, a.y));
        appendFractalRoadSegment(points, a.x, a.y, b.x, b.y, 0);

        Color roadColor = randomCoolRoadColor(185 + random.nextInt(50));
        return new RoadPath(Collections.unmodifiableList(new ArrayList<MapPoint>(points)), roadColor);
    }

    private void appendFractalRoadSegment(List<MapPoint> points,
                                          double x1, double y1, double x2, double y2, int depth) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.hypot(dx, dy);

        if (dist < 9.0) {
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

        int overflowX = (int) Math.round(mapWidth * GameConfig.ROAD_OVERFLOW_RATIO);
        int overflowY = (int) Math.round(mapHeight * GameConfig.ROAD_OVERFLOW_RATIO);
        midX = clamp((int) Math.round(midX), -overflowX, mapWidth - 1 + overflowX);
        midY = clamp((int) Math.round(midY), -overflowY, mapHeight - 1 + overflowY);

        appendFractalRoadSegment(points, x1, y1, midX, midY, depth + 1);
        appendFractalRoadSegment(points, midX, midY, x2, y2, depth + 1);
    }

    private Color randomCoolRoadColor(int alpha) {
        float hue = 0.38f + random.nextFloat() * 0.28f;
        float saturation = 0.25f + random.nextFloat() * 0.25f;
        float brightness = 0.75f + random.nextFloat() * 0.20f;
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}