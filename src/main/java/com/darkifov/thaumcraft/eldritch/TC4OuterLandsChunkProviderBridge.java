package com.darkifov.thaumcraft.eldritch;

import net.minecraft.core.BlockPos;
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

    public static void populateLikeTC4(ServerLevel level, int chunkX, int chunkZ) {
        Random random = chunkRandom(level.getSeed(), chunkX, chunkZ);
        BlockPos origin = new BlockPos(chunkX * 16 + 8, TC4OuterLandsDimensionParity.AVERAGE_GROUND_LEVEL, chunkZ * 16 + 8);
        TC4OuterLandsMazeHandler.ensurePortalMaze(level, origin);
        TC4OuterLandsMazeHandler.generateAround(level, origin, 2);
    }
}
