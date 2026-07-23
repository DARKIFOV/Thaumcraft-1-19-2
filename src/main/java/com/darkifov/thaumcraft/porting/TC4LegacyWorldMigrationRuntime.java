package com.darkifov.thaumcraft.porting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Deferred legacy-stack migration for loaded chunks.
 *
 * <p>Chunk load callbacks only enqueue work. At most two chunks are inspected
 * at the end of a server level tick, and getChunkNow is used so migration never
 * force-loads neighbours or recursively enters chunk IO.</p>
 */
public final class TC4LegacyWorldMigrationRuntime {
    public static final int CHUNKS_PER_TICK = 2;

    private static final Map<ServerLevel, Queue<Long>> PENDING = new WeakHashMap<>();
    private static final Map<ServerLevel, Set<Long>> QUEUED = new WeakHashMap<>();

    private TC4LegacyWorldMigrationRuntime() {
    }

    public static void queueLoadedChunk(ServerLevel level, LevelChunk chunk) {
        if (level == null || chunk == null || TC4WorldMigrationSavedData.get(level).isProcessed(chunk.getPos())) {
            return;
        }
        long key = chunk.getPos().toLong();
        synchronized (PENDING) {
            Set<Long> queued = QUEUED.computeIfAbsent(level, ignored -> new HashSet<>());
            if (queued.add(key)) {
                PENDING.computeIfAbsent(level, ignored -> new ArrayDeque<>()).add(key);
            }
        }
    }

    public static void drainDeferredChunkQueue(ServerLevel level) {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < CHUNKS_PER_TICK; i++) {
            Long key = poll(level);
            if (key == null) {
                return;
            }
            ChunkPos pos = new ChunkPos(key);
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk == null) {
                continue;
            }
            migrateChunk(level, chunk);
        }
    }

    private static Long poll(ServerLevel level) {
        synchronized (PENDING) {
            Queue<Long> queue = PENDING.get(level);
            Long key = queue == null ? null : queue.poll();
            if (key != null) {
                Set<Long> queued = QUEUED.get(level);
                if (queued != null) {
                    queued.remove(key);
                }
            }
            if (queue != null && queue.isEmpty()) {
                PENDING.remove(level);
                QUEUED.remove(level);
            }
            return key;
        }
    }

    private static void migrateChunk(ServerLevel level, LevelChunk chunk) {
        TC4WorldMigrationSavedData data = TC4WorldMigrationSavedData.get(level);
        ChunkPos pos = chunk.getPos();
        if (data.isProcessed(pos)) {
            return;
        }

        int changedStacks = 0;
        int changedBlockEntities = 0;
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            int changed = TC4LegacyDuplicateItemMigrator.migrateBlockEntity(blockEntity);
            changedStacks += changed;
            if (changed > 0) {
                changedBlockEntities++;
            }
        }

        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        AABB bounds = new AABB(minX, level.getMinBuildHeight(), minZ,
                minX + 16, level.getMaxBuildHeight(), minZ + 16);
        for (Entity entity : level.getEntities((Entity) null, bounds, Entity::isAlive)) {
            changedStacks += TC4LegacyDuplicateItemMigrator.migrateJoinedEntity(entity);
        }

        data.markProcessed(pos, changedStacks, changedBlockEntities);
    }
}
