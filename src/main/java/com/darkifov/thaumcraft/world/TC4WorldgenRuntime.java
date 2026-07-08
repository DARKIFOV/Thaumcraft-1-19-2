package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsDimensionAdapter;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsMazeHandler;
import com.darkifov.thaumcraft.eldritch.TC4OuterLandsLivePopulateAdapter;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashSet;
import java.util.Set;

/**
 * Stage146 runtime worldgen bridge for the TC4 surface pass.
 *
 * TC4 1.7.10 used IWorldGenerator. This project currently avoids fragile
 * configured-feature bootstrapping while the port is still source-driven, so it
 * seeds newly seen nearby chunks from server ticks. The constants follow the
 * original generator: cinnabar 18 attempts per chunk under world-height/5,
 * amber 20 attempts near surface, silverwood 1/60 and greatwood 1/25.
 */
public final class TC4WorldgenRuntime {
    private static final Set<String> PROCESSED_CHUNKS = new HashSet<>();
    private static final int PLAYER_CHUNK_RADIUS = 2;

    private TC4WorldgenRuntime() {
    }

    public static void tickPlayerArea(ServerLevel level, ServerPlayer player) {
        if (!isSupportedDimension(level.dimension())) {
            return;
        }
        if (level.getGameTime() % 80L != 0L) {
            return;
        }

        TC4OuterLandsMazeHandler.tickPlayerArea(level, player);
        TC4OuterLandsLivePopulateAdapter.tickPlayerArea(level, player);

        if (!TC4OuterLandsDimensionAdapter.shouldRunSurfaceWorldgen(level.dimension())) {
            return;
        }

        ChunkPos center = player.chunkPosition();
        for (int dx = -PLAYER_CHUNK_RADIUS; dx <= PLAYER_CHUNK_RADIUS; dx++) {
            for (int dz = -PLAYER_CHUNK_RADIUS; dz <= PLAYER_CHUNK_RADIUS; dz++) {
                seedChunkOnce(level, new ChunkPos(center.x + dx, center.z + dz));
            }
        }
    }

    private static boolean isSupportedDimension(ResourceKey<Level> dimension) {
        return TC4OuterLandsDimensionAdapter.supportsPortalMaze(dimension);
    }

    private static void seedChunkOnce(ServerLevel level, ChunkPos chunk) {
        String key = level.dimension().location() + ":" + chunk.x + ":" + chunk.z;
        if (!PROCESSED_CHUNKS.add(key)) {
            return;
        }

        long seed = level.getSeed() ^ (chunk.x * 341873128712L) ^ (chunk.z * 132897987541L) ^ 0x544334574f524c44L;
        RandomSource random = RandomSource.create(seed);
        generateOres(level, random, chunk);
        generateVegetation(level, random, chunk);
        generateTaintPockets(level, random, chunk);
    }

    private static void generateOres(ServerLevel level, RandomSource random, ChunkPos chunk) {
        for (int i = 0; i < 18; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int yRange = Math.max(8, (level.getMaxBuildHeight() - level.getMinBuildHeight()) / 5);
            int y = level.getMinBuildHeight() + random.nextInt(yRange);
            tryPlaceOreBlob(level, random, new BlockPos(x, y, z), ThaumcraftMod.CINNABAR_ORE.get().defaultBlockState(), 2);
        }

        for (int i = 0; i < 20; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int surface = getSurfaceY(level, x, z);
            int y = Math.max(level.getMinBuildHeight() + 4, surface - random.nextInt(25));
            tryPlaceOreBlob(level, random, new BlockPos(x, y, z), ThaumcraftMod.AMBER_ORE.get().defaultBlockState(), 2);
        }

        // Stage205 hard parity reset: the previous adapter produced too many
        // visible shard/crystal clusters. TC4 infused stone is sparse; use fewer
        // attempts and small clusters until the exact IWorldGenerator pass is fully
        // ported.
        for (int i = 0; i < 3; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int surface = Math.max(level.getMinBuildHeight() + 12, getSurfaceY(level, x, z) - 5);
            int y = level.getMinBuildHeight() + random.nextInt(Math.max(6, surface - level.getMinBuildHeight()));
            tryPlaceOreBlob(level, random, new BlockPos(x, y, z), randomInfusedCrystal(random), 3);
        }
    }

    private static void generateVegetation(ServerLevel level, RandomSource random, ChunkPos chunk) {
        if (random.nextInt(60) == 3) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int y = getSurfaceY(level, x, z);
            TC4TreeGenerator.growSilverwood(level, new BlockPos(x, y, z), random, true);
        }

        if (random.nextInt(25) == 7) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int y = getSurfaceY(level, x, z);
            TC4TreeGenerator.growGreatwood(level, new BlockPos(x, y, z), random, true);
        }
    }

    private static void generateTaintPockets(ServerLevel level, RandomSource random, ChunkPos chunk) {
        if (random.nextInt(210) != 0) {
            return;
        }
        int x = chunk.getMinBlockX() + random.nextInt(16);
        int z = chunk.getMinBlockZ() + random.nextInt(16);
        BlockPos base = new BlockPos(x, getSurfaceY(level, x, z), z);
        if (!isNaturalSurface(level.getBlockState(base.below()))) {
            return;
        }
        level.setBlock(base.below(), ThaumcraftMod.TAINT_SOIL.get().defaultBlockState(), 3);
        for (int i = 0; i < 10; i++) {
            TaintSpreadRuntime.trySpreadNear(level, base.below(), random, 3, false);
        }
    }

    private static int getSurfaceY(ServerLevel level, int x, int z) {
        return Math.max(level.getMinBuildHeight() + 1, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
    }

    private static boolean tryPlaceOreBlob(ServerLevel level, RandomSource random, BlockPos center, BlockState ore, int size) {
        boolean placed = false;
        for (int i = 0; i < size; i++) {
            BlockPos pos = center.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (isStoneReplaceable(level.getBlockState(pos))) {
                level.setBlock(pos, ore, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean isStoneReplaceable(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF);
    }

    private static boolean isNaturalSurface(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.STONE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND);
    }

    private static BlockState randomInfusedCrystal(RandomSource random) {
        return switch (random.nextInt(6)) {
            case 0 -> ThaumcraftMod.AER_CRYSTAL.get().defaultBlockState();
            case 1 -> ThaumcraftMod.IGNIS_CRYSTAL.get().defaultBlockState();
            case 2 -> ThaumcraftMod.AQUA_CRYSTAL.get().defaultBlockState();
            case 3 -> ThaumcraftMod.TERRA_CRYSTAL.get().defaultBlockState();
            case 4 -> ThaumcraftMod.ORDO_CRYSTAL.get().defaultBlockState();
            default -> ThaumcraftMod.PERDITIO_CRYSTAL.get().defaultBlockState();
        };
    }
}
