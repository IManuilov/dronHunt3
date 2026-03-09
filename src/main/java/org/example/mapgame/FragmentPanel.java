package org.example.mapgame;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;

public class FragmentPanel extends JPanel {
    private LevelData levelData;
    private List<CarVisual> cars = Collections.emptyList();

    private long fallStartMs = 0L;
    private long fallDurationMs = 0L;
    private boolean fallActive = false;

    public FragmentPanel() {
        setBackground(new Color(12, 12, 12));
    }

    public void setLevelData(LevelData levelData) {
        this.levelData = levelData;
        this.fallActive = false;
        repaint();
    }

    public void setCars(List<CarVisual> cars) {
        this.cars = cars;
        repaint();
    }

    public void startFallAnimation(long durationMs) {
        this.fallStartMs = System.currentTimeMillis();
        this.fallDurationMs = Math.max(1L, durationMs);
        this.fallActive = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (levelData == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0 || panelH <= 0) {
            return;
        }

        double baseScale = Math.max((double) panelW / GameConfig.MAP_WIDTH, (double) panelH / GameConfig.MAP_HEIGHT);
        double scale = baseScale * levelData.targetZoom();
        int whiteFadeAlpha = 0;

        if (fallActive) {
            long elapsed = System.currentTimeMillis() - fallStartMs;
            double t = Math.max(0.0, (double) elapsed / fallDurationMs);
            // Softer acceleration and no stop at animation end:
            // scale keeps growing until the next level is applied.
            double fallMultiplier = 1.0 + 1.2 * t * t * t;
            scale *= fallMultiplier;

            double fadeT = Math.min(1.0, t);
            whiteFadeAlpha = (int) Math.round(255.0 * fadeT * fadeT);
        }

        int cx = panelW / 2;
        int cy = panelH / 2;

        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy);
        g2.rotate(levelData.targetAngleRad());
        g2.scale(scale, scale);
        g2.translate(-levelData.targetX(), -levelData.targetY());

        g2.drawImage(levelData.mapImage(), 0, 0, null);

        for (CarVisual car : cars) {
            int x = (int) Math.round(car.x());
            int y = (int) Math.round(car.y());
            g2.setColor(car.color());
            g2.fillOval(x - 3, y - 3, 6, 6);
        }

        g2.setTransform(old);

        if (whiteFadeAlpha > 0) {
            g2.setColor(new Color(255, 255, 255, whiteFadeAlpha));
            g2.fillRect(0, 0, panelW, panelH);
        }

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(1, 1, panelW - 3, panelH - 3);
        g2.drawLine(cx - 14, cy, cx + 14, cy);
        g2.drawLine(cx, cy - 14, cx, cy + 14);
    }
}