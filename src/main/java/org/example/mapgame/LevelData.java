package org.example.mapgame;

import java.awt.image.BufferedImage;
import java.util.List;

public record LevelData(
        BufferedImage mapImage,
        List<RoadPath> roads,
        int targetX,
        int targetY,
        double targetAngleRad,
        double targetZoom
) {
}
