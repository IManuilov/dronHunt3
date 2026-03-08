package org.example.mapgame;

public final class GameConfig {
    public static final int MAP_WIDTH = 1800;
    public static final int MAP_HEIGHT = 1400;
    public static final int MAP_RECTANGLES = 1400;

    public static final int ROAD_NODE_COUNT = 72;
    public static final int ROAD_LINKS_PER_NODE = 4;
    public static final int ROAD_DIRECTION_SECTORS = 8;
    public static final int ROAD_MIN_DIST = 110;
    public static final int ROAD_MAX_DIST = 620;
    public static final int ROAD_MIN_NODE_SPACING = 140;
    public static final double ROAD_MIN_ANGLE_SEPARATION_RAD = Math.toRadians(38.0);
    public static final double ROAD_OVERFLOW_RATIO = 0.30;

    public static final int ANGLE_CELL_SIZE = 110;
    public static final double ANGLE_VARIATION_RAD = Math.toRadians(22.0);
    public static final double LOCAL_ANGLE_JITTER_RAD = Math.toRadians(9.0);

    public static final int HIT_RADIUS = 45;

    public static final int TARGET_MARGIN_X = 60;
    public static final int TARGET_MARGIN_Y = 50;
    public static final double TARGET_MIN_ZOOM = 1.8;
    public static final double TARGET_ZOOM_RANGE = 1.4;

    private GameConfig() {
    }
}
