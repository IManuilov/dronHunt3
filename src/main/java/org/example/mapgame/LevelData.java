package org.example.mapgame;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LevelData {
    private final BufferedImage mapImage;
    private final List<RoadPath> roads;
    private final int targetX;
    private final int targetY;
    private final double targetAngleRad;
    private final double targetZoom;

    public LevelData(BufferedImage mapImage,
                     List<RoadPath> roads,
                     int targetX,
                     int targetY,
                     double targetAngleRad,
                     double targetZoom) {
        this.mapImage = mapImage;
        this.roads = Collections.unmodifiableList(roads);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetAngleRad = targetAngleRad;
        this.targetZoom = targetZoom;
    }

    public BufferedImage mapImage() {
        return mapImage;
    }

    public List<RoadPath> roads() {
        return roads;
    }

    public int targetX() {
        return targetX;
    }

    public int targetY() {
        return targetY;
    }

    public double targetAngleRad() {
        return targetAngleRad;
    }

    public double targetZoom() {
        return targetZoom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LevelData)) {
            return false;
        }
        LevelData levelData = (LevelData) o;
        return targetX == levelData.targetX
                && targetY == levelData.targetY
                && Double.compare(levelData.targetAngleRad, targetAngleRad) == 0
                && Double.compare(levelData.targetZoom, targetZoom) == 0
                && Objects.equals(mapImage, levelData.mapImage)
                && Objects.equals(roads, levelData.roads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapImage, roads, targetX, targetY, targetAngleRad, targetZoom);
    }

    @Override
    public String toString() {
        return "LevelData[targetX=" + targetX + ", targetY=" + targetY + "]";
    }
}