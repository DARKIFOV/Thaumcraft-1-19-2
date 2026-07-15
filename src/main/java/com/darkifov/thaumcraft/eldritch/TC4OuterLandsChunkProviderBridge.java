package com.darkifov.thaumcraft.eldritch;

import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/** Stage303-322 bridge for TC4 ChunkProviderOuter seed/populate contract. */
public final class TC4OuterLandsChunkProviderBridge {
    private TC4OuterLandsChunkProviderBridge() {}
    public static final long X_SEED = 341873128712L;
    public static final long Z_SEED = 132897987541L;

    public static Random chunkRandom(long worldSeed, int chunkX, int chunkZ) {
        Random random = new Random(worldSeed);
        random.setSeed(chunkX * X_SEED + chunkZ * Z_SEED);
        return random;
    }

    public static boolean populateLikeTC4(ServerLevel level, int chunkX, int chunkZ) {
        // ChunkProviderOuter populated the already-generated global labyrinth.
        // It never created a new maze for every chunk.  Maze creation belongs
        // exclusively to the entry portal path.
        if (!TC4OuterLandsDimensionAdapter.isOuterLands(level.dimension())
                || !TC4OuterLandsMazeHandler.hasMazeCell(chunkX, chunkZ)) {
            return false;
        }
        chunkRandom(level.getSeed(), chunkX, chunkZ); // preserve TC4 seed contract for audits/future decoration use
        TC4OuterLandsMazeHandler.generateForNewChunk(level, new net.minecraft.world.level.ChunkPos(chunkX, chunkZ));
        return true;
    }
}
