package com.darkifov.thaumcraft.porting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Per-dimension ledger for bounded, repeatable world inventory migrations.
 *
 * <p>Bumping {@link #CURRENT_SCHEMA} intentionally invalidates the processed
 * chunk set so a later release can add another safe migration pass. The data is
 * stored through vanilla/Forge SavedData and is therefore written with the
 * dimension rather than to an external side file.</p>
 */
public final class TC4WorldMigrationSavedData extends SavedData {
    public static final String DATA_NAME = "thaumcraft_legacy_world_migration";
    public static final int CURRENT_SCHEMA = 116343;

    private final Set<Long> processedChunks = new HashSet<>();
    private long migratedStacks;
    private long migratedBlockEntities;

    public TC4WorldMigrationSavedData() {
    }

    public TC4WorldMigrationSavedData(CompoundTag tag) {
        int storedSchema = tag.getInt("Schema");
        if (storedSchema == CURRENT_SCHEMA) {
            for (long value : tag.getLongArray("ProcessedChunks")) {
                processedChunks.add(value);
            }
            migratedStacks = tag.getLong("MigratedStacks");
            migratedBlockEntities = tag.getLong("MigratedBlockEntities");
        }
    }

    public static TC4WorldMigrationSavedData load(CompoundTag tag) {
        return new TC4WorldMigrationSavedData(tag);
    }

    public static TC4WorldMigrationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TC4WorldMigrationSavedData::load,
                TC4WorldMigrationSavedData::new,
                DATA_NAME);
    }

    public boolean isProcessed(ChunkPos pos) {
        return processedChunks.contains(pos.toLong());
    }

    public void markProcessed(ChunkPos pos, int changedStacks, int changedBlockEntities) {
        boolean changed = processedChunks.add(pos.toLong());
        if (changedStacks > 0) {
            migratedStacks += changedStacks;
            changed = true;
        }
        if (changedBlockEntities > 0) {
            migratedBlockEntities += changedBlockEntities;
            changed = true;
        }
        if (changed) {
            setDirty();
        }
    }

    public int processedChunkCount() {
        return processedChunks.size();
    }

    public long migratedStackCount() {
        return migratedStacks;
    }

    public long migratedBlockEntityCount() {
        return migratedBlockEntities;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Schema", CURRENT_SCHEMA);
        tag.putLongArray("ProcessedChunks", processedChunks.stream().mapToLong(Long::longValue).toArray());
        tag.putLong("MigratedStacks", migratedStacks);
        tag.putLong("MigratedBlockEntities", migratedBlockEntities);
        return tag;
    }
}
