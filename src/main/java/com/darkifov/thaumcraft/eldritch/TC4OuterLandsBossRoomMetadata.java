package com.darkifov.thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/**
 * Stage216 immutable metadata for the TC4 Outer Lands boss/key-room doorway.
 *
 * This does not reintroduce the old 1.7.10 dimension generator directly; it captures
 * the exact GenBossRoom PAT_DOORWAY contract and lock-facing payload so the 1.19.2
 * structure implementation can consume the same data without legacy worldgen APIs.
 */
public final class TC4OuterLandsBossRoomMetadata {
    public static final String FEATURE_TAG = "tc4OuterFeature";
    public static final String LOCK_FACING_TAG = "tc4LockFacing";
    public static final String BOSS_ROOM_TAG = "tc4BossRoom";
    public static final String KEY_ROOM_TAG = "tc4KeyRoom";

    public static final int FEATURE_UPPER_LEFT = 2;
    public static final int FEATURE_UPPER_RIGHT = 3;
    public static final int FEATURE_LOWER_LEFT = 4;
    public static final int FEATURE_LOWER_RIGHT = 5;

    /** TC4 GenBossRoom.PAT_DOORWAY copied verbatim. Values: 1 lock, 2 frame, 9 void/inner. */
    public static final int[][] PAT_DOORWAY = new int[][] {
            {0, 2, 2, 2, 2, 2, 0},
            {2, 2, 9, 9, 9, 2, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 9, 9, 1, 9, 9, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 2, 9, 9, 9, 2, 2},
            {0, 2, 2, 2, 2, 2, 0}
    };

    private TC4OuterLandsBossRoomMetadata() {
    }

    public static CompoundTag bossRoomTag(int feature, Direction doorwayFacing) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(BOSS_ROOM_TAG, true);
        tag.putInt(FEATURE_TAG, feature);
        tag.putString(LOCK_FACING_TAG, doorwayFacing == null ? Direction.NORTH.getName() : doorwayFacing.getName());
        return tag;
    }

    public static CompoundTag keyRoomTag(Direction doorwayFacing) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_ROOM_TAG, true);
        tag.putString(LOCK_FACING_TAG, doorwayFacing == null ? Direction.NORTH.getName() : doorwayFacing.getName());
        return tag;
    }

    public static BlockPos doorwayOrigin(int chunkX, int chunkZ, int y, Direction facing) {
        int x = chunkX * 16;
        int z = chunkZ * 16;
        return switch (facing == null ? Direction.NORTH : facing) {
            case NORTH -> new BlockPos(x + 5, y + 2, z + 3);
            case SOUTH -> new BlockPos(x + 5, y + 2, z + 13);
            case EAST -> new BlockPos(x + 13, y + 2, z + 5);
            case WEST -> new BlockPos(x + 3, y + 2, z + 5);
            default -> new BlockPos(x + 5, y + 2, z + 3);
        };
    }

    public static boolean isLockCell(int a, int b) {
        return inBounds(a, b) && PAT_DOORWAY[a][b] == 1;
    }

    public static boolean isFrameCell(int a, int b) {
        return inBounds(a, b) && PAT_DOORWAY[a][b] == 2;
    }

    public static boolean isInnerCell(int a, int b) {
        return inBounds(a, b) && PAT_DOORWAY[a][b] == 9;
    }

    private static boolean inBounds(int a, int b) {
        return a >= 0 && b >= 0 && a < PAT_DOORWAY.length && b < PAT_DOORWAY[a].length;
    }
}
