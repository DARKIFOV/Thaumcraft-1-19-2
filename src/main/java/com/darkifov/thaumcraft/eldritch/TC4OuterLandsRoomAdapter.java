package com.darkifov.thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * Stage221: direct 1.19.2-safe room generators for TC4 Gen2x2 and GenPassage.
 * These methods preserve original loop bounds and palette codes, while delegating
 * actual block mapping to TC4OuterLandsGenCommonAdapter.placeBlock().
 */
public final class TC4OuterLandsRoomAdapter {
    private TC4OuterLandsRoomAdapter() {}

    public enum Quadrant { UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT }

    public static void generate2x2(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell, Quadrant quadrant) {
        switch (quadrant) {
            case UPPER_LEFT -> generateUpperLeft(level, origin, cell);
            case UPPER_RIGHT -> generateUpperRight(level, origin, cell);
            case LOWER_LEFT -> generateLowerLeft(level, origin, cell);
            case LOWER_RIGHT -> generateLowerRight(level, origin, cell);
        }
    }

    private static void generateUpperLeft(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 1; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 13; c++) if (a == 1 || b == 1) p(level, origin, a, c, b, 1, Direction.NORTH, cell);
        for (int a = 2; a <= 15; a++) for (int b = 2; b <= 15; b++) for (int c = 1; c < 12; c++) if ((a == 2 || b == 2) && ((a != 2) || b <= 4 || b >= 12 || !cell.west() || c >= 10) && ((b != 2) || a <= 4 || a >= 12 || !cell.north() || c >= 10)) p(level, origin, a, c, b, 8, Direction.NORTH, cell);
        for (int a = 3; a <= 15; a++) for (int b = 3; b <= 15; b++) for (int c = 2; c < 11; c++) if (a == 3 || b == 3) p(level, origin, a, c, b, 18, Direction.NORTH, cell);
        floorCeiling(level, origin, cell, 2, 15, 2, 15, 13, 19);
        for (int g = 4; g <= 15; g++) { p(level, origin, g, 2, 4, 10, Direction.NORTH, cell); p(level, origin, g, 10, 4, 11, Direction.NORTH, cell); p(level, origin, 4, 2, g, 10, Direction.WEST, cell); p(level, origin, 4, 10, g, 11, Direction.WEST, cell); }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
    }

    private static void generateUpperRight(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 0; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 13; c++) if (a == 15 || b == 1) p(level, origin, a, c, b, 1, Direction.NORTH, cell);
        for (int a = 0; a <= 14; a++) for (int b = 2; b <= 15; b++) for (int c = 1; c < 12; c++) if ((a == 14 || b == 2) && ((a != 14) || b <= 4 || b >= 12 || !cell.east() || c >= 10) && ((b != 2) || a <= 4 || a >= 12 || !cell.north() || c >= 10)) p(level, origin, a, c, b, 8, Direction.NORTH, cell);
        for (int a = 0; a <= 13; a++) for (int b = 3; b <= 15; b++) for (int c = 2; c < 11; c++) if (a == 13 || b == 3) p(level, origin, a, c, b, 18, Direction.NORTH, cell);
        floorCeiling(level, origin, cell, 0, 14, 2, 15, 13, 19);
        for (int g = 0; g <= 11; g++) { p(level, origin, g, 2, 4, 10, Direction.NORTH, cell); p(level, origin, g, 10, 4, 11, Direction.NORTH, cell); }
        for (int g = 4; g <= 15; g++) { p(level, origin, 12, 2, g, 10, Direction.EAST, cell); p(level, origin, 12, 10, g, 11, Direction.EAST, cell); }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
    }

    private static void generateLowerLeft(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 1; a <= 15; a++) for (int b = 0; b <= 15; b++) for (int c = 0; c < 13; c++) if (a == 1 || b == 15) p(level, origin, a, c, b, 1, Direction.NORTH, cell);
        for (int a = 2; a <= 15; a++) for (int b = 0; b <= 14; b++) for (int c = 1; c < 12; c++) if ((a == 2 || b == 14) && ((a != 2) || b <= 4 || b >= 12 || !cell.west() || c >= 10) && ((b != 14) || a <= 4 || a >= 12 || !cell.south() || c >= 10)) p(level, origin, a, c, b, 8, Direction.NORTH, cell);
        for (int a = 3; a <= 15; a++) for (int b = 0; b <= 13; b++) for (int c = 2; c < 11; c++) if (a == 3 || b == 13) p(level, origin, a, c, b, 18, Direction.NORTH, cell);
        floorCeiling(level, origin, cell, 2, 15, 0, 14, 13, 19);
        for (int g = 4; g <= 15; g++) { p(level, origin, g, 2, 12, 10, Direction.SOUTH, cell); p(level, origin, g, 10, 12, 11, Direction.SOUTH, cell); }
        for (int g = 0; g <= 11; g++) { p(level, origin, 4, 2, g, 10, Direction.WEST, cell); p(level, origin, 4, 10, g, 11, Direction.WEST, cell); }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
    }

    private static void generateLowerRight(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 0; a <= 15; a++) for (int b = 0; b <= 15; b++) for (int c = 0; c < 13; c++) if (a == 15 || b == 15) p(level, origin, a, c, b, 1, Direction.NORTH, cell);
        for (int a = 0; a <= 14; a++) for (int b = 0; b <= 14; b++) for (int c = 1; c < 12; c++) if ((a == 14 || b == 14) && ((a != 14) || b <= 4 || b >= 12 || !cell.east() || c >= 10) && ((b != 14) || a <= 4 || a >= 12 || !cell.south() || c >= 10)) p(level, origin, a, c, b, 8, Direction.NORTH, cell);
        for (int a = 0; a <= 13; a++) for (int b = 0; b <= 13; b++) for (int c = 2; c < 11; c++) if (a == 13 || b == 13) p(level, origin, a, c, b, 18, Direction.NORTH, cell);
        floorCeiling(level, origin, cell, 0, 14, 0, 14, 13, 19);
        for (int g = 0; g <= 11; g++) { p(level, origin, g, 2, 12, 10, Direction.SOUTH, cell); p(level, origin, g, 10, 12, 11, Direction.SOUTH, cell); }
        for (int g = 0; g <= 12; g++) { p(level, origin, 12, 2, g, 10, Direction.EAST, cell); p(level, origin, 12, 10, g, 11, Direction.EAST, cell); }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
    }

    public static void generatePassage(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell) {
        for (int a = 1; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 9; c++) if (a == 1 || a == 15 || b == 1 || b == 15) p(level, origin, a, c, b, 1, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) for (int c = 1; c < 8; c++) if ((a == 2 || a == 14 || b == 2 || b == 14) && ((a != 2) || b <= 3 || b >= 12 || !cell.west() || c >= 6) && ((a != 14) || b <= 3 || b >= 12 || !cell.east() || c >= 6) && ((b != 2) || a <= 3 || a >= 12 || !cell.north() || c >= 6) && ((b != 14) || a <= 3 || a >= 12 || !cell.south() || c >= 6)) p(level, origin, a, c, b, 8, Direction.NORTH, cell);
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) { p(level, origin, a, -1, b, 1, Direction.NORTH, cell); p(level, origin, a, 0, b, 8, Direction.NORTH, cell); p(level, origin, a, 1, b, 2, Direction.NORTH, cell); p(level, origin, a, 9, b, 1, Direction.NORTH, cell); p(level, origin, a, 8, b, 8, Direction.NORTH, cell); p(level, origin, a, 7, b, 2, Direction.NORTH, cell); }
        TC4OuterLandsGenCommonAdapter.generateConnections(level, origin, cell, 3, true);
        TC4OuterLandsPassageFeatureAdapter.apply(level, origin, cell);
    }

    private static void floorCeiling(ServerLevel level, BlockPos origin, TC4OuterLandsGenCommonAdapter.Cell cell, int ax0, int ax1, int bz0, int bz1, int topY, int floorCode) {
        for (int a = ax0; a <= ax1; a++) for (int b = bz0; b <= bz1; b++) {
            p(level, origin, a, -1, b, 1, Direction.NORTH, cell);
            p(level, origin, a, 0, b, 8, Direction.NORTH, cell);
            p(level, origin, a, 1, b, floorCode, Direction.NORTH, cell);
            p(level, origin, a, topY, b, 1, Direction.NORTH, cell);
            p(level, origin, a, topY - 1, b, 8, Direction.NORTH, cell);
            p(level, origin, a, topY - 2, b, 2, Direction.NORTH, cell);
        }
    }

    private static void p(ServerLevel level, BlockPos origin, int x, int y, int z, int code, Direction dir, TC4OuterLandsGenCommonAdapter.Cell cell) {
        TC4OuterLandsGenCommonAdapter.placeBlock(level, origin.offset(x, y, z), code, dir, cell);
    }
}
