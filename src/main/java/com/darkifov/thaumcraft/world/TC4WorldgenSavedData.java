package com.darkifov.thaumcraft.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * v9.82 persistent marker for TC4 surface worldgen chunks.
 *
 * TC4's IWorldGenerator ran as part of chunk generation/population. The 1.19.2
 * bridge is event-driven, so keep a SavedData ledger in addition to the in-memory
 * guard: after server restart/reload, already-populated chunks must not receive
 * Greatwood/Silverwood/Aura Node/Ore passes again.
 */
public final class TC4WorldgenSavedData extends SavedData {
    public static final String DATA_NAME = "thaumcraft_tc4_worldgen_chunks";
    private final Set<Long> processedChunks = new HashSet<>();

    public TC4WorldgenSavedData() {
    }

    public TC4WorldgenSavedData(CompoundTag tag) {
        long[] values = tag.getLongArray("ProcessedChunks");
        for (long value : values) {
            processedChunks.add(value);
        }
    }

    public static TC4WorldgenSavedData load(CompoundTag tag) {
        return new TC4WorldgenSavedData(tag);
    }

    public static TC4WorldgenSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TC4WorldgenSavedData::load, TC4WorldgenSavedData::new, DATA_NAME);
    }

    public boolean markProcessed(ChunkPos chunk) {
        long key = chunk.toLong();
        if (!processedChunks.add(key)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean isProcessed(ChunkPos chunk) {
        return processedChunks.contains(chunk.toLong());
    }

    public int size() {
        return processedChunks.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] values = new long[processedChunks.size()];
        int index = 0;
        for (Long value : processedChunks) {
            values[index++] = value;
        }
        tag.putLongArray("ProcessedChunks", values);
        return tag;
    }
}
