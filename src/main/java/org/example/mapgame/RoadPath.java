package org.example.mapgame;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RoadPath {
    private final List<MapPoint> points;
    private final Color color;

    public RoadPath(List<MapPoint> points, Color color) {
        this.points = Collections.unmodifiableList(points);
        this.color = color;
    }

    public List<MapPoint> points() {
        return points;
    }

    public Color color() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoadPath)) {
            return false;
        }
        RoadPath roadPath = (RoadPath) o;
        return Objects.equals(points, roadPath.points)
                && Objects.equals(color, roadPath.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, color);
    }

    @Override
    public String toString() {
        return "RoadPath[points=" + points + ", color=" + color + "]";
    }
}