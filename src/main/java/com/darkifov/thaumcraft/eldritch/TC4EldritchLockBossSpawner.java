package com.darkifov.thaumcraft.eldritch;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import com.darkifov.thaumcraft.entity.CultistPortalEntity;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import com.darkifov.thaumcraft.entity.TaintacleGiantEntity;
import com.darkifov.thaumcraft.entity.TaintacleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        BlockPos center = lockPos.offset(0, 3, 0);
        announce(level, lockPos, variant);
        clearSpawnPocket(level, center);
        switch (variant) {
            case 0 -> spawnGolem(level, center, lockPos);
            case 1 -> spawnWarden(level, center, lockPos);
            case 2 -> spawnCultistPortalEquivalent(level, center, lockPos);
            default -> spawnTaintBossEquivalent(level, center, lockPos);
        }
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
        boss.getPersistentData().putLong("Home", home.asLong());
        level.addFreshEntity(boss);
    }

    private static void spawnGolem(ServerLevel level, BlockPos center, BlockPos home) {
        EldritchGolemEntity boss = ThaumcraftMod.ELDRITCH_GOLEM.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        boss.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnGolemBossRoom");
        boss.getPersistentData().putLong("Home", home.asLong());
        level.addFreshEntity(boss);
    }

    private static void spawnCultistPortalEquivalent(ServerLevel level, BlockPos center, BlockPos home) {
        CultistPortalEntity portal = ThaumcraftMod.CULTIST_PORTAL.get().create(level);
        if (portal == null) return;
        portal.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        portal.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        portal.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnCultistPortal");
        portal.getPersistentData().putLong("Home", home.asLong());
        level.addFreshEntity(portal);
    }

    private static void spawnTaintBossEquivalent(ServerLevel level, BlockPos center, BlockPos home) {
        TaintacleGiantEntity boss = ThaumcraftMod.TAINTACLE_GIANT.get().create(level);
        if (boss == null) return;
        boss.moveTo(center.getX() + 0.5D, center.getY(), center.getZ() + 0.5D, 0.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.TRIGGERED, null, null);
        boss.getPersistentData().putString("TC4Original", "TileEldritchLock.spawnTaintacleGiant");
        boss.getPersistentData().putLong("Home", home.asLong());
        level.addFreshEntity(boss);
    }
}
