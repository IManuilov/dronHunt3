package org.example.mapgame;

import java.awt.Color;
import java.util.Objects;

public final class CarVisual {
    private final double x;
    private final double y;
    private final Color color;

    public CarVisual(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public Color color() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarVisual)) {
            return false;
        }
        CarVisual carVisual = (CarVisual) o;
        return Double.compare(carVisual.x, x) == 0
                && Double.compare(carVisual.y, y) == 0
                && Objects.equals(color, carVisual.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, color);
    }

    @Override
    public String toString() {
        return "CarVisual[x=" + x + ", y=" + y + ", color=" + color + "]";
    }
}