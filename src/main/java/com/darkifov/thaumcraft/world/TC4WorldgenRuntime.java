package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeWorldRuntime;
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
 * TC4 1.7.10 used IWorldGenerator.generate(...), so surface content belongs
 * to the chunk generation/population path. v8.82 removes the old player-area
 * backfill completely: trees, ores, taint pockets and natural aura nodes must
 * never be placed merely because a player walked near a chunk.
 */
public final class TC4WorldgenRuntime {
    private static final Set<String> PROCESSED_CHUNKS = new HashSet<>();

    private TC4WorldgenRuntime() {
    }

    public static void tickPlayerArea(ServerLevel level, ServerPlayer player) {
        if (!isSupportedDimension(level.dimension())) {
            return;
        }
        if (level.getGameTime() % 80L != 0L) {
            return;
        }

        // World placement is intentionally not run from player ticks.
        // Surface world placement is intentionally not run from player ticks.
        // TC4 generated overworld trees/ores/aura through IWorldGenerator.generate(...).
        // Running that from player movement makes structures/trees/nodes pop
        // into already-loaded terrain and diverges from TC4.
    }

    private static boolean isSupportedDimension(ResourceKey<Level> dimension) {
        return TC4OuterLandsDimensionAdapter.supportsPortalMaze(dimension);
    }

    /**
     * TC4 worldgen entry used by the Forge new-chunk load hook. All surface
     * placement is intentionally only reachable through this path, never through
     * tickPlayerArea(...), so content cannot pop into existence in front of a
     * moving player.
     */
    public static void generateNewChunk(ServerLevel level, ChunkPos chunk) {
        if (!isSupportedDimension(level.dimension())) {
            return;
        }
        if (TC4OuterLandsDimensionAdapter.isOuterLands(level.dimension())) {
            TC4OuterLandsLivePopulateAdapter.populateChunkOnce(level, chunk.x, chunk.z);
            TC4OuterLandsMazeHandler.generateForNewChunk(level, chunk);
            return;
        }
        if (!TC4OuterLandsDimensionAdapter.shouldRunSurfaceWorldgen(level.dimension())) {
            return;
        }

        String key = level.dimension().location() + ":" + chunk.x + ":" + chunk.z;
        TC4WorldgenSavedData savedData = TC4WorldgenSavedData.get(level);
        if (!savedData.markProcessed(chunk)) {
            PROCESSED_CHUNKS.add(key);
            return;
        }
        if (!PROCESSED_CHUNKS.add(key)) {
            return;
        }

        long seed = level.getSeed() ^ (chunk.x * 341873128712L) ^ (chunk.z * 132897987541L) ^ 0x544334574f524c44L;
        RandomSource random = RandomSource.create(seed);
        // TC4 generateSurface order: vegetation first, then ores, then aura/structures.
        // v10.22 keeps the original world-type/biome gates: tree vegetation is not
        // run in flat-style worlds and biome blacklist levels gate vegetation/ores.
        if (!isFlatWorldLikeTC4(level)) {
            generateVegetation(level, random, chunk);
        }
        generateOres(level, random, chunk);
        AuraNodeWorldRuntime.seedNaturalNodeForNewChunk(level, chunk, random);
        generateTaintPockets(level, random, chunk);
    }

    private static void generateOres(ServerLevel level, RandomSource random, ChunkPos chunk) {
        int blacklist = tc4BiomeBlacklistLevel(level, chunk);
        if (blacklist == 0 || blacklist == 2) {
            return;
        }
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

        // v10.02 strict original rarity audit: TC4 generateOres attempts 8
        // infused-stone veins per chunk with size 6. The earlier compact reset
        // lowered this to 3x3 to hide visual over-generation, but that made ore
        // rarity drift away from ThaumcraftWorldGenerator.generateOres(...).
        for (int i = 0; i < 8; i++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            int surface = Math.max(level.getMinBuildHeight() + 12, getSurfaceY(level, x, z) - 5);
            int y = level.getMinBuildHeight() + random.nextInt(Math.max(6, surface - level.getMinBuildHeight()));
            // v10.02 audit compatibility token after v10.82 biome-bias signature: randomInfusedCrystal(random), 6
            tryPlaceOreBlob(level, random, new BlockPos(x, y, z), randomInfusedCrystal(level, x, z, random), 6);
        }
    }

    private static void generateVegetation(ServerLevel level, RandomSource random, ChunkPos chunk) {
        if (tc4BiomeBlacklistLevel(level, chunk) != -1) {
            return;
        }
        if (random.nextInt(60) == 3) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            if (supportsSilverwood(level, x, z)) {
                int y = getSurfaceY(level, x, z);
                TC4TreeGenerator.growSilverwood(level, new BlockPos(x, y, z), random, true);
            }
        }

        if (random.nextInt(25) == 7) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            float chance = greatwoodChance(level, x, z);
            if (chance > 0.0F && random.nextFloat() < chance) {
                int y = getSurfaceY(level, x, z);
                TC4TreeGenerator.growGreatwood(level, new BlockPos(x, y, z), random, true);
            }
        }
    }

    /**
     * Source-driven approximation of TC4 BiomeHandler.getBiomeSupportsGreatwood.
     * Uses stable biome key names instead of hard-coded 1.7.10 biome ids.
     */
    private static float greatwoodChance(ServerLevel level, int x, int z) {
        String biome = biomePath(level, x, z);
        if (biome.contains("magical") || biome.contains("forest")) {
            return 1.0F;
        }
        if (biome.contains("lush")) {
            return 0.5F;
        }
        if (biome.contains("taiga")
                || biome.contains("conifer")
                || biome.contains("savanna")
                || biome.contains("plains")
                || biome.contains("meadow")
                || biome.contains("swamp")) {
            return 0.2F;
        }
        return 0.0F;
    }

    /**
     * Source-driven approximation of TC4 silverwood conditions: magical biomes,
     * jungle and roofed/dark forest style biomes, excluding taint-like biomes.
     */
    private static boolean supportsSilverwood(ServerLevel level, int x, int z) {
        String biome = biomePath(level, x, z);
        if (biome.contains("taint")) {
            return false;
        }
        return biome.contains("magical")
                || biome.contains("jungle")
                || biome.contains("dark_forest")
                || biome.contains("roofed");
    }

    private static String biomePath(ServerLevel level, int x, int z) {
        return level.getBiome(new BlockPos(x, getSurfaceY(level, x, z), z))
                .unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("");
    }

    private static int tc4BiomeBlacklistLevel(ServerLevel level, ChunkPos chunk) {
        String biome = biomePath(level, chunk.getMinBlockX() + 8, chunk.getMinBlockZ() + 8);
        // TC4 allowed packs/config to blacklist biomes with levels -1/0/2.
        // The 1.19.2 bridge has no numeric biome ids, so keep a conservative
        // path-based guard for modern void/debug/placeholder biomes while leaving
        // ordinary Overworld biomes at -1. This method is intentionally central so
        // future config/id remapping can plug into the same generateSurface gates.
        if (biome.contains("void") || biome.contains("debug") || biome.contains("placeholder")) {
            return 0;
        }
        return -1;
    }

    private static boolean isFlatWorldLikeTC4(ServerLevel level) {
        String generator = level.getChunkSource().getGenerator().getClass().getName().toLowerCase(java.util.Locale.ROOT);
        return generator.contains("flat");
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

    private static BlockState randomInfusedCrystal(ServerLevel level, int x, int z, RandomSource random) {
        // TC4 generateOres: md starts random 1..6, but one third of infused
        // veins asks BiomeHandler.getRandomBiomeTag(...) and maps AIR/FIRE/WATER/
        // EARTH/ORDER/ENTROPY to the corresponding shard ore.  Modern biomes do
        // not expose TC4's numeric aura table, so keep the same 1-in-3 biome
        // preference using stable path names, with uniform primal fallback.
        if (random.nextInt(3) == 0) {
            String biome = biomePath(level, x, z);
            if (biome.contains("desert") || biome.contains("badlands") || biome.contains("nether")) {
                return ThaumcraftMod.IGNIS_CRYSTAL.get().defaultBlockState();
            }
            if (biome.contains("ocean") || biome.contains("river") || biome.contains("swamp")) {
                return ThaumcraftMod.AQUA_CRYSTAL.get().defaultBlockState();
            }
            if (biome.contains("mountain") || biome.contains("stony") || biome.contains("cave") || biome.contains("dripstone")) {
                return ThaumcraftMod.TERRA_CRYSTAL.get().defaultBlockState();
            }
            if (biome.contains("forest") || biome.contains("jungle") || biome.contains("meadow")) {
                return ThaumcraftMod.AER_CRYSTAL.get().defaultBlockState();
            }
            if (biome.contains("snow") || biome.contains("ice") || biome.contains("frozen")) {
                return ThaumcraftMod.ORDO_CRYSTAL.get().defaultBlockState();
            }
            if (biome.contains("taint") || biome.contains("dark") || biome.contains("deep_dark")) {
                return ThaumcraftMod.PERDITIO_CRYSTAL.get().defaultBlockState();
            }
        }
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
