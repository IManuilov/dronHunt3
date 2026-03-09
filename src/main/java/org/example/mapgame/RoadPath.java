package org.example.mapgame;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RoadPath {
    private final List<MapPoint> points;

    public RoadPath(List<MapPoint> points) {
        this.points = Collections.unmodifiableList(points);
    }

    public List<MapPoint> points() {
        return points;
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
        return Objects.equals(points, roadPath.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "RoadPath[points=" + points + "]";
    }
}