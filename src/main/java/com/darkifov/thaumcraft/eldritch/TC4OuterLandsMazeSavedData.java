package com.darkifov.thaumcraft.eldritch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Stage233-242 persistence bridge for TC4 MazeHandler.labyrinth.dat.
 *
 * <p>TC4 wrote a compressed file named labyrinth.dat containing a Data compound
 * with a cells list.  Forge 1.19.2 exposes the same concept through SavedData;
 * this class preserves the original payload shape while letting the server save
 * it with the rest of the dimension data.</p>
 */
public final class TC4OuterLandsMazeSavedData extends SavedData {
    public static final String DATA_NAME = "thaumcraft_labyrinth";
    private final Set<Long> populatedChunks = new HashSet<>();

    public TC4OuterLandsMazeSavedData() {
    }

    public TC4OuterLandsMazeSavedData(CompoundTag tag) {
        CompoundTag data = tag.contains("Data") ? tag.getCompound("Data") : tag;
        TC4OuterLandsMazeHandler.readNBT(data);
        long[] chunks = data.getLongArray("populatedChunks");
        for (long chunk : chunks) {
            populatedChunks.add(chunk);
        }
    }

    public static TC4OuterLandsMazeSavedData load(CompoundTag tag) {
        return new TC4OuterLandsMazeSavedData(tag);
    }

    public static TC4OuterLandsMazeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TC4OuterLandsMazeSavedData::load, TC4OuterLandsMazeSavedData::new, DATA_NAME);
    }

    public boolean markChunkPopulated(int chunkX, int chunkZ) {
        return populatedChunks.add(net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ));
    }

    public boolean isChunkPopulated(int chunkX, int chunkZ) {
        return populatedChunks.contains(net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag data = TC4OuterLandsMazeHandler.writeNBT();
        data.putLongArray("populatedChunks", populatedChunks.stream().mapToLong(Long::longValue).toArray());
        tag.put("Data", data);
        return tag;
    }
}
