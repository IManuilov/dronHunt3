package org.example.mapgame;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MapPanel extends JPanel {
    public interface ClickListener {
        void onMapClick(int mapX, int mapY);

        void onOutsideMapClick();
    }

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 8.0;
    private static final double ZOOM_STEP = 1.15;

    private final ClickListener clickListener;
    private BufferedImage mapImage;

    private int lastClickX = -1;
    private int lastClickY = -1;

    private double zoom = 1.0;
    private double centerX = GameConfig.MAP_WIDTH / 2.0;
    private double centerY = GameConfig.MAP_HEIGHT / 2.0;

    private boolean dragging = false;
    private boolean suppressNextClick = false;
    private int dragStartX;
    private int dragStartY;
    private double dragStartCenterX;
    private double dragStartCenterY;

    private record MapPoint(double x, double y) {
    }

    public MapPanel(ClickListener clickListener) {
        this.clickListener = clickListener;
        setBackground(Color.BLACK);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Rectangle drawRect = getMapDrawRect(getWidth(), getHeight());
                if (drawRect.contains(e.getPoint())) {
                    dragging = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    dragStartCenterX = centerX;
                    dragStartCenterY = centerY;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!dragging) {
                    return;
                }

                Rectangle drawRect = getMapDrawRect(getWidth(), getHeight());
                if (drawRect.width <= 0 || drawRect.height <= 0) {
                    return;
                }

                int dx = e.getX() - dragStartX;
                int dy = e.getY() - dragStartY;
                if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
                    suppressNextClick = true;
                }

                double mapPerPixelX = getViewWidth() / drawRect.width;
                double mapPerPixelY = getViewHeight() / drawRect.height;

                centerX = dragStartCenterX - dx * mapPerPixelX;
                centerY = dragStartCenterY - dy * mapPerPixelY;
                clampCenter();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (suppressNextClick) {
                    suppressNextClick = false;
                    return;
                }
                handleClick(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleZoom(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void setMapImage(BufferedImage mapImage) {
        this.mapImage = mapImage;
        zoom = 1.0;
        centerX = GameConfig.MAP_WIDTH / 2.0;
        centerY = GameConfig.MAP_HEIGHT / 2.0;
        clampCenter();
        repaint();
    }

    public void setLastClick(int mapX, int mapY) {
        this.lastClickX = mapX;
        this.lastClickY = mapY;
        repaint();
    }

    public void clearLastClick() {
        this.lastClickX = -1;
        this.lastClickY = -1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapImage == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        Rectangle drawRect = getMapDrawRect(getWidth(), getHeight());
        if (drawRect.width <= 0 || drawRect.height <= 0) {
            return;
        }

        int dx1 = drawRect.x;
        int dy1 = drawRect.y;
        int dx2 = drawRect.x + drawRect.width;
        int dy2 = drawRect.y + drawRect.height;

        double left = centerX - getViewWidth() / 2.0;
        double top = centerY - getViewHeight() / 2.0;
        double right = left + getViewWidth();
        double bottom = top + getViewHeight();

        int sx1 = clamp((int) Math.floor(left), 0, GameConfig.MAP_WIDTH - 1);
        int sy1 = clamp((int) Math.floor(top), 0, GameConfig.MAP_HEIGHT - 1);
        int sx2 = clamp((int) Math.ceil(right), sx1 + 1, GameConfig.MAP_WIDTH);
        int sy2 = clamp((int) Math.ceil(bottom), sy1 + 1, GameConfig.MAP_HEIGHT);

        g2.drawImage(mapImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);

        if (lastClickX >= 0 && lastClickY >= 0) {
            int drawX = mapToScreenX(lastClickX, drawRect);
            int drawY = mapToScreenY(lastClickY, drawRect);
            g2.setStroke(new BasicStroke(2.4f));
            g2.drawOval(drawX - 10, drawY - 10, 20, 20);
            g2.drawLine(drawX - 14, drawY, drawX + 14, drawY);
            g2.drawLine(drawX, drawY - 14, drawX, drawY + 14);
        }
    }

    private void handleClick(MouseEvent e) {
        MapPoint mapPoint = screenToMap(e.getX(), e.getY());
        if (mapPoint == null) {
            clickListener.onOutsideMapClick();
            return;
        }

        int mapX = clamp((int) Math.round(mapPoint.x), 0, GameConfig.MAP_WIDTH - 1);
        int mapY = clamp((int) Math.round(mapPoint.y), 0, GameConfig.MAP_HEIGHT - 1);
        clickListener.onMapClick(mapX, mapY);
    }

    private void handleZoom(MouseWheelEvent e) {
        double factor = Math.pow(ZOOM_STEP, -e.getPreciseWheelRotation());
        double newZoom = clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
        if (Math.abs(newZoom - zoom) < 1e-9) {
            return;
        }

        Rectangle drawRect = getMapDrawRect(getWidth(), getHeight());
        if (drawRect.width <= 0 || drawRect.height <= 0) {
            return;
        }

        MapPoint anchor = screenToMap(e.getX(), e.getY());
        if (anchor == null) {
            zoom = newZoom;
            clampCenter();
            repaint();
            return;
        }

        double relX = (e.getX() - drawRect.x) / (double) drawRect.width;
        double relY = (e.getY() - drawRect.y) / (double) drawRect.height;

        zoom = newZoom;

        double newViewW = getViewWidth();
        double newViewH = getViewHeight();
        centerX = anchor.x - (relX - 0.5) * newViewW;
        centerY = anchor.y - (relY - 0.5) * newViewH;

        clampCenter();
        repaint();
    }

    private MapPoint screenToMap(int sx, int sy) {
        Rectangle drawRect = getMapDrawRect(getWidth(), getHeight());
        if (!drawRect.contains(sx, sy) || drawRect.width <= 0 || drawRect.height <= 0) {
            return null;
        }

        double relX = (sx - drawRect.x) / (double) drawRect.width;
        double relY = (sy - drawRect.y) / (double) drawRect.height;

        double mapX = centerX + (relX - 0.5) * getViewWidth();
        double mapY = centerY + (relY - 0.5) * getViewHeight();

        return new MapPoint(
                clamp(mapX, 0.0, GameConfig.MAP_WIDTH - 1.0),
                clamp(mapY, 0.0, GameConfig.MAP_HEIGHT - 1.0)
        );
    }

    private int mapToScreenX(double mapX, Rectangle drawRect) {
        double rel = (mapX - (centerX - getViewWidth() / 2.0)) / getViewWidth();
        return drawRect.x + (int) Math.round(rel * drawRect.width);
    }

    private int mapToScreenY(double mapY, Rectangle drawRect) {
        double rel = (mapY - (centerY - getViewHeight() / 2.0)) / getViewHeight();
        return drawRect.y + (int) Math.round(rel * drawRect.height);
    }

    private double getViewWidth() {
        return GameConfig.MAP_WIDTH / zoom;
    }

    private double getViewHeight() {
        return GameConfig.MAP_HEIGHT / zoom;
    }

    private void clampCenter() {
        double viewW = getViewWidth();
        double viewH = getViewHeight();

        double minX = viewW / 2.0;
        double maxX = GameConfig.MAP_WIDTH - viewW / 2.0;
        double minY = viewH / 2.0;
        double maxY = GameConfig.MAP_HEIGHT - viewH / 2.0;

        centerX = clamp(centerX, minX, maxX);
        centerY = clamp(centerY, minY, maxY);
    }

    private Rectangle getMapDrawRect(int panelW, int panelH) {
        if (panelW <= 0 || panelH <= 0) {
            return new Rectangle(0, 0, 0, 0);
        }

        double mapAspect = (double) GameConfig.MAP_WIDTH / GameConfig.MAP_HEIGHT;
        int drawW = panelW;
        int drawH = (int) Math.round(drawW / mapAspect);

        if (drawH > panelH) {
            drawH = panelH;
            drawW = (int) Math.round(drawH * mapAspect);
        }

        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;
        return new Rectangle(x, y, drawW, drawH);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}