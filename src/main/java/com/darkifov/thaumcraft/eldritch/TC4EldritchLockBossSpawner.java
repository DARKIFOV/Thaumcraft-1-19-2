package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.CultistPortalEntity;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import com.darkifov.thaumcraft.entity.TaintacleGiantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;

/**
 * Stage273-282 bridge for TileEldritchLock.doBossSpawn.
 * Stage283-302 supersedes the placeholder markers EntityCultistPortal-equivalent
 * and EntityTaintacleGiant-equivalent-pending with real registered entities.
 *
 * <p>The original lock delayed for 100 ticks, incremented MapBossData.bossCount
 * with a 25% extra increment, then selected Warden/Golem/Cultist/Taint rooms by
 * bossCount % 4.  This 1.19.2 adapter keeps that cycle and spawns the currently
 * ported boss family at a safe local anchor while preserving TC4 NBT breadcrumbs.</p>
 */
public final class TC4EldritchLockBossSpawner {
    private TC4EldritchLockBossSpawner() {}

    public static void spawnFromLock(ServerLevel level, BlockPos lockPos) {
        int bossCount = TC4OuterLandsBossCycleData.get(level).advance(level);
        int variant = Math.floorMod(bossCount, 4);
        BossRoomContext room = locateBossRoom(lockPos);
        announce(level, lockPos, variant);

        BlockPos spawn = switch (variant) {
            case 0 -> room.center().above(3);
            case 1 -> room.quadrantCenter().above(3);
            case 2 -> room.center().above(2);
            default -> room.center().above(3);
        };
        clearSpawnPocket(level, spawn);
        switch (variant) {
            case 0 -> spawnGolem(level, spawn, room.center());
            case 1 -> spawnWarden(level, spawn, room.center());
            case 2 -> spawnCultistPortalEquivalent(level, spawn, room.center());
            default -> spawnTaintBossEquivalent(level, spawn, room.center());
        }
    }

    private static BossRoomContext locateBossRoom(BlockPos lockPos) {
        int chunkX = Math.floorDiv(lockPos.getX(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE);
        int chunkZ = Math.floorDiv(lockPos.getZ(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE);
        int centerX = chunkX;
        int centerZ = chunkZ;
        int exitFeature = TC4OuterLandsMazeGenerator.FEATURE_BOSS_2;

        // TileEldritchLock#doBossSpawn searches the surrounding 5x5 cell area:
        // feature 2 identifies the north-west 2x2 room anchor, while feature
        // 2..5 with a doorway selects the quadrant used by the Warden pedestal.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                TC4OuterLandsMazeCell cell = TC4OuterLandsMazeHandler.getFromHashMap(
                        new TC4OuterLandsMazeCellLoc(chunkX + dx, chunkZ + dz)
                );
                if (cell == null) {
                    continue;
                }
                int feature = cell.feature & 255;
                if (feature == TC4OuterLandsMazeGenerator.FEATURE_BOSS_2) {
                    centerX = chunkX + dx;
                    centerZ = chunkZ + dz;
                }
                if (feature >= TC4OuterLandsMazeGenerator.FEATURE_BOSS_2
                        && feature <= TC4OuterLandsMazeGenerator.FEATURE_BOSS_5
                        && (cell.north || cell.south || cell.east || cell.west)) {
                    exitFeature = feature;
                }
            }
        }

        BlockPos center = new BlockPos(
                centerX * TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE + TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE,
                TC4OuterLandsDimensionAdapter.ORIGINAL_ROOM_Y,
                centerZ * TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE + TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE
        );
        int offsetX = switch (exitFeature) {
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_2,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_4 -> 8;
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_3,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_5 -> -8;
            default -> 0;
        };
        int offsetZ = switch (exitFeature) {
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_2,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_3 -> 8;
            case TC4OuterLandsMazeGenerator.FEATURE_BOSS_4,
                    TC4OuterLandsMazeGenerator.FEATURE_BOSS_5 -> -8;
            default -> 0;
        };
        return new BossRoomContext(center, center.offset(offsetX, 0, offsetZ), exitFeature);
    }

    private static void announce(ServerLevel level, BlockPos pos, int variant) {
        String key = switch (variant) {
            case 0 -> "tc.boss.golem";
            case 1 -> "tc.boss.warden";
            case 2 -> "tc.boss.cultist";
            default -> "tc.boss.taint";
        };
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 300.0D) {
                player.displayClientMessage(Component.translatable(key), false);
            }
        }
    }

    private static void clearSpawnPocket(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 3, 2))) {
            if (!level.getBlockState(p).is(ThaumcraftMod.ELDRITCH_LOCK.get()) && !level.getBlockState(p).isAir()) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        level.setBlock(center.below(), ThaumcraftMod.ELDRITCH_CAP.get().defaultBlockState(), 3);
    }

    private static void spawnWarden(ServerLevel level, BlockPos center, BlockPos home) {
        EldritchWardenEntity boss = ThaumcraftMod.ELDRITCH_WARDEN.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        boss.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnWardenBossRoom");
        bindHome(boss, home, 32);
        level.addFreshEntity(boss);
    }

    private static void spawnGolem(ServerLevel level, BlockPos center, BlockPos home) {
        EldritchGolemEntity boss = ThaumcraftMod.ELDRITCH_GOLEM.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        boss.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnGolemBossRoom");
        bindHome(boss, home, 32);
        level.addFreshEntity(boss);
    }

    private static void spawnCultistPortalEquivalent(ServerLevel level, BlockPos center, BlockPos home) {
        CultistPortalEntity portal = ThaumcraftMod.CULTIST_PORTAL.get().create(level);
        if (portal == null) return;
        portal.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        portal.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        portal.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnCultistPortal");
        bindHome(portal, home, 32);
        level.addFreshEntity(portal);
    }

    private static void spawnTaintBossEquivalent(ServerLevel level, BlockPos center, BlockPos home) {
        TaintacleGiantEntity boss = ThaumcraftMod.TAINTACLE_GIANT.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        boss.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnTaintacleGiant");
        bindHome(boss, home, 32);
        level.addFreshEntity(boss);
    }

    private static void bindHome(Mob mob, BlockPos home, int radius) {
        mob.restrictTo(home, radius);
        mob.getPersistentData().putInt("HomeX", home.getX());
        mob.getPersistentData().putInt("HomeY", home.getY());
        mob.getPersistentData().putInt("HomeZ", home.getZ());
        mob.getPersistentData().putInt("HomeD", radius);
    }

    private record BossRoomContext(BlockPos center, BlockPos quadrantCenter, int exitFeature) {
    }
}
