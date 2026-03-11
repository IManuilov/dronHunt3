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
import java.util.List;
import java.util.Random;

import static org.example.mapgame.GameConfig.BLACK_THRESHOLD;

public class LevelGenerator {
    private final Random random = new Random();

    private int generatedMapCount = 0;
    private int currentMapWidth = GameConfig.MAP_WIDTH;
    private int currentMapHeight = GameConfig.MAP_HEIGHT;

    private int fragmentViewportWidth = 900;
    private int fragmentViewportHeight = 900;

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

        System.out.println("mapw "+ currentMapWidth + "  anch " + anchorCount + "  rect " + rectCount + "  nodes " + roadNodeCount
            + "  maxRectSize " + maxRectSize);

        drawPrimaryRectangles(g2, mapImage, rectCount, minRectSize, maxRectSize, angleAnchors);
        int smallRectCount = Math.max(8, rectCount * 14);
        drawSecondaryRectangles(g2, mapImage, smallRectCount, 10, 60, angleAnchors);

        drawSecondaryRectangles(g2, mapImage, smallRectCount*4, 3, 15, angleAnchors);

        List<RoadPath> roads = new RoadNetworkBuilder(currentMapWidth, currentMapHeight, random).buildRoadPaths(roadNodeCount);
        drawRoadsideRects(g2, roads);
        drawRoads(g2, roads);
        if (GameConfig.GRID_ENABLED) {
            drawGrid(g2);
        }
        g2.dispose();

        return new GeneratedMap(mapImage, Collections.unmodifiableList(new ArrayList<RoadPath>(roads)));
    }

    private void drawPrimaryRectangles(Graphics2D g2,
                                       BufferedImage mapImage,
                                       int rectCount,
                                       int minRectSize,
                                       int maxRectSize,
                                       List<AngleAnchor> angleAnchors) {
        RectanglePainter.drawRectangles(
                g2,
                mapImage,
                currentMapWidth,
                currentMapHeight,
                rectCount,
                minRectSize,
                maxRectSize,
                random,
                new RectanglePainter.AngleProvider() {
                    @Override
                    public double angleRadAt(int x, int y) {
                        return getNearestAnchorAngle(angleAnchors, x, y);
                    }
                },
                new RectanglePainter.ColorProvider() {
                    @Override
                    public Color colorAt(BufferedImage img, int x, int y, int w, int h, int alpha) {
                        if (random.nextDouble() < 0.80) {
                            return colorFromMapAtPointOrGenerated(img, x, y, alpha);
                        }
                        return randomWarmColor(alpha);
                    }
                },
                GameConfig.LOCAL_ANGLE_JITTER_RAD);
    }

    private void drawSecondaryRectangles(Graphics2D g2,
                                         BufferedImage mapImage,
                                         int rectCount,
                                         int minRectSize,
                                         int maxRectSize,
                                         List<AngleAnchor> angleAnchors) {
        RectanglePainter.drawRectangles(
                g2,
                mapImage,
                currentMapWidth,
                currentMapHeight,
                rectCount,
                minRectSize,
                maxRectSize,
                random,
                new RectanglePainter.AngleProvider() {
                    @Override
                    public double angleRadAt(int x, int y) {
                        return getNearestAnchorAngle(angleAnchors, x, y);
                    }
                },
                new RectanglePainter.ColorProvider() {
                    @Override
                    public Color colorAt(BufferedImage img, int x, int y, int w, int h, int alpha) {
                        int softAlpha = clamp((int) Math.round(alpha * 0.55), 18, 150);
                        int radius = 2 * Math.max(1, Math.max(w, h));
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double distance = Math.sqrt(random.nextDouble()) * radius;
                        int sx = clamp((int) Math.round(x + Math.cos(angle) * distance), 0, currentMapWidth - 1);
                        int sy = clamp((int) Math.round(y + Math.sin(angle) * distance), 0, currentMapHeight - 1);
                        return colorFromMapAtPointWithVariation(img, sx, sy, softAlpha);
                    }
                },
                GameConfig.LOCAL_ANGLE_JITTER_RAD);
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

    private Color colorFromMapAtPointWithVariation(BufferedImage mapImage, int x, int y, int alpha) {
        Color base = new Color(mapImage.getRGB(x, y));

        int retryRadius = 44;
        if (isTooDark(base)) {
            for (int attempt = 0; attempt < 5; attempt++) {
                int nx = clamp(x + random.nextInt(retryRadius * 2 + 1) - retryRadius, 0, currentMapWidth - 1);
                int ny = clamp(y + random.nextInt(retryRadius * 2 + 1) - retryRadius, 0, currentMapHeight - 1);
                Color nearby = new Color(mapImage.getRGB(nx, ny));
                if (!isTooDark(nearby)) {
                    base = nearby;
                    break;
                }
            }
        }

        int dr = random.nextInt(31) - 15;
        int dg = random.nextInt(31) - 15;
        int db = random.nextInt(31) - 15;

        int r = clamp(base.getRed() + dr, 0, 255);
        int g = clamp(base.getGreen() + dg, 0, 255);
        int b = clamp(base.getBlue() + db, 0, 255);
        return new Color(r, g, b, alpha);
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

    private void drawRoads(Graphics2D g2, List<RoadPath> roads) {
        for (RoadPath road : roads) {
            drawRoadPath(g2, road);
        }
    }

    private void drawRoadPath(Graphics2D g2, RoadPath roadPath) {
        List<MapPoint> points = roadPath.points();
        if (points.size() < 2) {
            return;
        }

        Path2D.Double road = new Path2D.Double();
        MapPoint start = points.get(0);
        road.moveTo(start.x(), start.y());
        for (int i = 1; i < points.size(); i++) {
            MapPoint p = points.get(i);
            road.lineTo(p.x(), p.y());
        }

        float roadWidth = 2.0f + random.nextFloat() * 2.6f;
        Color roadColor = roadPath.color();
        g2.setStroke(new BasicStroke(roadWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(roadColor);
        g2.draw(road);
    }

    private void drawRoadsideRects(Graphics2D g2, List<RoadPath> roads) {
        if (roads.isEmpty()) {
            return;
        }

        double areaScale = ((double) currentMapWidth * currentMapHeight)
                / ((double) GameConfig.MAP_WIDTH * GameConfig.MAP_HEIGHT);
        int roadsideRectCount = Math.max(1, (int) Math.round(500_000 * areaScale));

        List<Double> roadLengths = new ArrayList<>(roads.size());
        List<Double> cumulativeLengths = new ArrayList<>(roads.size());
        double totalLength = 0.0;

        for (RoadPath road : roads) {
            double length = computeRoadLength(road);
            roadLengths.add(length);
            totalLength += length;
            cumulativeLengths.add(totalLength);
        }

        if (totalLength <= 0.0) {
            return;
        }

        for (int i = 0; i < roadsideRectCount; i++) {
            double pick = random.nextDouble() * totalLength;
            int roadIndex = pickRoadIndexByLength(cumulativeLengths, pick);
            RoadPath road = roads.get(roadIndex);
            double roadLength = roadLengths.get(roadIndex);
            if (roadLength <= 0.0) {
                continue;
            }

            MapPoint roadPoint = randomPointOnRoad(road, roadLength);
            if (roadPoint == null) {
                continue;
            }

            double maxOffset = 20;//0.5 + Math.min(22.0, roadLength * 0.04);
            double offsetAngle = random.nextDouble() * Math.PI * 2.0;
            double offsetDistance = random.nextDouble() * maxOffset;

            int x = clamp((int) Math.round(roadPoint.x() + Math.cos(offsetAngle) * offsetDistance),
                    0, currentMapWidth - 1);
            int y = clamp((int) Math.round(roadPoint.y() + Math.sin(offsetAngle) * offsetDistance),
                    0, currentMapHeight - 1);

            double distanceRatio = offsetDistance / maxOffset;
            int w = 9 + (int) (random.nextInt(17) * (distanceRatio));
            int h = 9 + (int) (random.nextInt(17) * (distanceRatio));

            int maxAlpha = 79;
            int alpha = clamp((int) Math.round(maxAlpha * (1.0 - distanceRatio)), 8, 80);
            Color varied = varyColor(road.color(), 16, alpha);

            AffineTransform old = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(random.nextDouble() * Math.PI * 2.0);
            g2.setColor(varied);
            g2.fillRect(-w / 2, -h / 2, w, h);
            g2.setTransform(old);
        }
    }

    private double computeRoadLength(RoadPath road) {
        List<MapPoint> points = road.points();
        if (points.size() < 2) {
            return 0.0;
        }

        double length = 0.0;
        for (int i = 1; i < points.size(); i++) {
            MapPoint a = points.get(i - 1);
            MapPoint b = points.get(i);
            length += Math.hypot(b.x() - a.x(), b.y() - a.y());
        }
        return length;
    }

    private int pickRoadIndexByLength(List<Double> cumulativeLengths, double target) {
        int low = 0;
        int high = cumulativeLengths.size() - 1;

        while (low < high) {
            int mid = (low + high) >>> 1;
            if (cumulativeLengths.get(mid) < target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    private MapPoint randomPointOnRoad(RoadPath road, double roadLength) {
        List<MapPoint> points = road.points();
        if (points.size() < 2 || roadLength <= 0.0) {
            return null;
        }

        double distance = random.nextDouble() * roadLength;
        for (int i = 1; i < points.size(); i++) {
            MapPoint a = points.get(i - 1);
            MapPoint b = points.get(i);
            double segLen = Math.hypot(b.x() - a.x(), b.y() - a.y());
            if (segLen <= 0.0) {
                continue;
            }
            if (distance <= segLen) {
                double t = distance / segLen;
                return new MapPoint(a.x() + (b.x() - a.x()) * t,
                        a.y() + (b.y() - a.y()) * t);
            }
            distance -= segLen;
        }

        return points.get(points.size() - 1);
    }

    private Color varyColor(Color color, int delta, int alpha) {
        int r = clamp(color.getRed() + random.nextInt(delta * 2 + 1) - delta, 0, 255);
        int g = clamp(color.getGreen() + random.nextInt(delta * 2 + 1) - delta, 0, 255);
        int b = clamp(color.getBlue() + random.nextInt(delta * 2 + 1) - delta, 0, 255);
        return new Color(r, g, b, clamp(alpha, 0, 255));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
