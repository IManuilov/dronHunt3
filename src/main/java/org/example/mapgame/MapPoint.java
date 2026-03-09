package org.example.mapgame;

import java.util.Objects;

public final class MapPoint {
    private final double x;
    private final double y;

    public MapPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapPoint)) {
            return false;
        }
        MapPoint mapPoint = (MapPoint) o;
        return Double.compare(mapPoint.x, x) == 0
                && Double.compare(mapPoint.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "MapPoint[x=" + x + ", y=" + y + "]";
    }
}