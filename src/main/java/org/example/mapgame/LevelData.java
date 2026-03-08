package org.example.mapgame;

import java.awt.image.BufferedImage;

public record LevelData(
        BufferedImage mapImage,
        int targetX,
        int targetY,
        double targetAngleRad,
        double targetZoom
) {
}
