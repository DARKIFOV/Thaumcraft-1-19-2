package com.darkifov.thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/** Stage233-242: explicit 1.19.2 selector for TC4 MazeHandler.generateEldritch feature ids. */
public final class TC4OuterLandsFeatureSelector {
    private TC4OuterLandsFeatureSelector() {
    }

    public static void generateFeature(ServerLevel level, BlockPos cellOrigin, TC4OuterLandsMazeCell mazeCell) {
        int feature = mazeCell.feature & 255;
        TC4OuterLandsGenCommonAdapter.Cell genCell = mazeCell.toGenCell();
        TC4OuterLandsDecorationAdapter.beginRoom();
        switch (feature) {
            case TC4OuterLandsMazeGenerator.FEATURE_PORTAL -> TC4OuterLandsGenCommonAdapter.generatePortalRoom(level, cellOrigin, genCell);
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_2,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_3,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_4,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_5 ->
                    TC4OuterLandsBossRoomPlacer.placeBossRoomCell(level, cellOrigin, genCell, feature);
            case TC4OuterLandsMazeGenerator.FEATURE_KEY_ROOM -> TC4OuterLandsBossRoomPlacer.placeKeyRoomCell(level, cellOrigin, genCell);
            case TC4OuterLandsMazeGenerator.FEATURE_NEST_ROOM -> TC4OuterLandsGenCommonAdapter.generateNestRoom(level, cellOrigin, genCell);
            case TC4OuterLandsMazeGenerator.FEATURE_LIBRARY_ROOM -> TC4OuterLandsGenCommonAdapter.generateLibraryRoom(level, cellOrigin, genCell);
            case 10 -> TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, genCell, TC4OuterLandsRoomAdapter.Quadrant.UPPER_LEFT);
            case 11 -> TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, genCell, TC4OuterLandsRoomAdapter.Quadrant.UPPER_RIGHT);
            case 12 -> TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, genCell, TC4OuterLandsRoomAdapter.Quadrant.LOWER_LEFT);
            case 13 -> TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, genCell, TC4OuterLandsRoomAdapter.Quadrant.LOWER_RIGHT);
            case 14 -> TC4OuterLandsRoomAdapter.generate2x2(level, cellOrigin, genCell, TC4OuterLandsRoomAdapter.Quadrant.UPPER_LEFT);
            default -> TC4OuterLandsRoomAdapter.generatePassage(level, cellOrigin, genCell);
        }
        TC4OuterLandsDecorationAdapter.processDecorations(level);
    }

    public static Direction doorwayFacing(TC4OuterLandsGenCommonAdapter.Cell cell) {
        if (cell.north()) return Direction.NORTH;
        if (cell.south()) return Direction.SOUTH;
        if (cell.east()) return Direction.EAST;
        if (cell.west()) return Direction.WEST;
        return Direction.NORTH;
    }
}
