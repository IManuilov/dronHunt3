package org.example.mapgame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class RectanglePainter {
    public interface AngleProvider {
        double angleRadAt(int x, int y);
    }

    public interface ColorProvider {
        Color colorAt(BufferedImage mapImage, int x, int y, int width, int height, int alpha);
    }

    private RectanglePainter() {
    }

    public static void drawRectangles(Graphics2D g2,
                                      BufferedImage mapImage,
                                      int mapWidth,
                                      int mapHeight,
                                      int rectCount,
                                      int minRectSize,
                                      int maxRectSize,
                                      Random random,
                                      AngleProvider angleProvider,
                                      ColorProvider colorProvider,
                                      double angleJitterRad) {
        if (g2 == null || mapImage == null || random == null || rectCount <= 0) {
            return;
        }
        if (minRectSize <= 0 || maxRectSize < minRectSize) {
            return;
        }

        int rectSpan = maxRectSize - minRectSize + 1;
        int minArea = minRectSize * minRectSize;
        int maxArea = maxRectSize * maxRectSize;
        if (maxArea <= minArea) {
            maxArea = minArea + 1;
        }

        for (int i = 0; i < rectCount; i++) {
            int w = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int h = minRectSize + (int) (random.nextDouble() * random.nextDouble() * rectSpan);
            int cx = random.nextInt(mapWidth);
            int cy = random.nextInt(mapHeight);

            int area = w * h;
            double t = (double) (area - minArea) / (maxArea - minArea);
            t = Math.max(0.0, Math.min(1.0, t));
            int alpha = (int) Math.round(220 - t * 150);

            Color color = colorProvider != null
                    ? colorProvider.colorAt(mapImage, cx, cy, w, h, alpha)
                    : new Color(255, 255, 255, alpha);

            double baseAngle = angleProvider != null ? angleProvider.angleRadAt(cx, cy) : 0.0;
            double angle = baseAngle + (random.nextDouble() - 0.5) * 2.0 * angleJitterRad;

            AffineTransform old = g2.getTransform();
            g2.translate(cx, cy);
            g2.rotate(angle);
            g2.setColor(color);
            g2.fillRect(-w / 2, -h / 2, w, h);
            g2.setTransform(old);
        }
    }
}