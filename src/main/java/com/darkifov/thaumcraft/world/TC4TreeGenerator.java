package com.darkifov.thaumcraft.world;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeProfile;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage146 source-driven TC4 Greatwood/Silverwood tree bridge.
 *
 * TC4 1.7.10 used WorldGenGreatwoodTrees and WorldGenSilverwoodTrees with
 * large custom algorithms, metadata logs/leaves and occasional silverwood aura
 * nodes. Forge 1.19.2 needs explicit blocks and direct resource models, so this
 * class keeps the old silhouettes and generation chances while using modern
 * block placement.
 */
public final class TC4TreeGenerator {
    private TC4TreeGenerator() {
    }

    public static boolean growGreatwood(ServerLevel level, BlockPos base, RandomSource random, boolean worldgen) {
        int height = 11 + random.nextInt(11);
        if (!canGrow2x2(level, base, height + 10, 5)) {
            return false;
        }

        clearBase(level, base);
        for (int y = 0; y < height; y++) {
            placeLog2x2(level, base.above(y), ThaumcraftMod.GREATWOOD_LOG.get().defaultBlockState());
        }

        makeGreatwoodCrown(level, base.above(height - 2), random);
        makeGreatwoodCrown(level, base.above(Math.max(4, height / 2 + 2)), random);
        makeBranches(level, base.above(Math.max(5, height / 2)), height, random, ThaumcraftMod.GREATWOOD_LOG.get().defaultBlockState(), ThaumcraftMod.GREATWOOD_LEAVES.get().defaultBlockState());

        if (worldgen && random.nextInt(8) == 0) {
            webGreatwoodNest(level, base, random);
        }
        return true;
    }

    public static boolean growSilverwood(ServerLevel level, BlockPos base, RandomSource random, boolean worldgen) {
        int height = 7 + random.nextInt(4);
        if (!canGrow2x2(level, base, height + 8, 5)) {
            return false;
        }

        clearBase(level, base);
        for (int y = 0; y < height; y++) {
            placeCrossLog(level, base.above(y), ThaumcraftMod.SILVERWOOD_LOG.get().defaultBlockState());
        }

        makeSilverwoodCrown(level, base.above(height - 2), random);
        if (worldgen || random.nextInt(3) != 0) {
            placeSilverwoodNode(level, base.above(Math.max(3, height - 4)), random);
        }
        return true;
    }

    private static void clearBase(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] { base, base.east(), base.south(), base.east().south() }) {
            if (level.getBlockState(pos).is(BlockTags.SAPLINGS)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean canGrow2x2(ServerLevel level, BlockPos base, int height, int radius) {
        if (base.getY() < level.getMinBuildHeight() + 1 || base.getY() + height >= level.getMaxBuildHeight() - 1) {
            return false;
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                if (!isSoil(level.getBlockState(base.offset(dx, -1, dz)))) {
                    return false;
                }
            }
        }
        for (int y = 0; y <= height; y++) {
            int scanRadius = y < 2 ? 1 : radius;
            for (int dx = -scanRadius; dx <= scanRadius; dx++) {
                for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                    BlockPos pos = base.offset(dx, y, dz);
                    if (!canReplaceForTree(level, pos)) {
                        return false;
                    }
                }
            }
        }
        return level.getBrightness(LightLayer.SKY, base.above()) > 0 || level.getBrightness(LightLayer.BLOCK, base.above()) >= 9;
    }

    private static boolean isSoil(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.MYCELIUM);
    }

    private static boolean canReplaceForTree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.getMaterial().isReplaceable();
    }

    private static void placeLog2x2(ServerLevel level, BlockPos pos, BlockState log) {
        setReplaceable(level, pos, log);
        setReplaceable(level, pos.east(), log);
        setReplaceable(level, pos.south(), log);
        setReplaceable(level, pos.east().south(), log);
    }

    private static void placeCrossLog(ServerLevel level, BlockPos pos, BlockState log) {
        setReplaceable(level, pos, log);
        setReplaceable(level, pos.east(), log);
        setReplaceable(level, pos.west(), log);
        setReplaceable(level, pos.north(), log);
        setReplaceable(level, pos.south(), log);
    }

    private static void makeGreatwoodCrown(ServerLevel level, BlockPos center, RandomSource random) {
        BlockState leaves = ThaumcraftMod.GREATWOOD_LEAVES.get().defaultBlockState();
        for (int y = -3; y <= 4; y++) {
            int radius = y < -1 ? 4 : y < 2 ? 5 : y < 4 ? 4 : 2;
            placeLeafBlob(level, center.above(y), radius, leaves, random);
        }
    }

    private static void makeSilverwoodCrown(ServerLevel level, BlockPos center, RandomSource random) {
        BlockState leaves = ThaumcraftMod.SILVERWOOD_LEAVES.get().defaultBlockState();
        for (int y = -2; y <= 4; y++) {
            int radius = y < 0 ? 4 : y < 3 ? 5 : 3;
            placeLeafBlob(level, center.above(y), radius, leaves, random);
        }
    }

    private static void placeLeafBlob(ServerLevel level, BlockPos center, int radius, BlockState leaves, RandomSource random) {
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= r2 - random.nextInt(Math.max(1, radius + 2))) {
                    setLeaves(level, center.offset(dx, 0, dz), leaves);
                }
            }
        }
    }

    private static void makeBranches(ServerLevel level, BlockPos start, int height, RandomSource random, BlockState log, BlockState leaves) {
        int[][] directions = new int[][] { {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1} };
        for (int i = 0; i < directions.length; i++) {
            if (random.nextInt(3) == 0 && i > 3) {
                continue;
            }
            int length = 3 + random.nextInt(4);
            BlockPos pos = start.above(random.nextInt(Math.max(1, height / 3)));
            for (int step = 1; step <= length; step++) {
                pos = pos.offset(directions[i][0], step % 2 == 0 ? 1 : 0, directions[i][1]);
                setReplaceable(level, pos, log);
            }
            placeLeafBlob(level, pos, 3 + random.nextInt(2), leaves, random);
        }
    }

    private static void webGreatwoodNest(ServerLevel level, BlockPos base, RandomSource random) {
        for (int i = 0; i < 28; i++) {
            BlockPos pos = base.offset(random.nextInt(13) - 6, 2 + random.nextInt(8), random.nextInt(13) - 6);
            if (level.isEmptyBlock(pos) && touchesTree(level, pos)) {
                level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            }
        }
    }

    private static boolean touchesTree(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.north()).is(BlockTags.LOGS) || level.getBlockState(pos.north()).is(BlockTags.LEAVES)
                || level.getBlockState(pos.south()).is(BlockTags.LOGS) || level.getBlockState(pos.south()).is(BlockTags.LEAVES)
                || level.getBlockState(pos.east()).is(BlockTags.LOGS) || level.getBlockState(pos.east()).is(BlockTags.LEAVES)
                || level.getBlockState(pos.west()).is(BlockTags.LOGS) || level.getBlockState(pos.west()).is(BlockTags.LEAVES)
                || level.getBlockState(pos.above()).is(BlockTags.LOGS) || level.getBlockState(pos.above()).is(BlockTags.LEAVES)
                || level.getBlockState(pos.below()).is(BlockTags.LOGS) || level.getBlockState(pos.below()).is(BlockTags.LEAVES);
    }

    private static void placeSilverwoodNode(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canReplaceForTree(level, pos)) {
            return;
        }
        level.setBlock(pos, ThaumcraftMod.AURA_NODE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node) {
            AuraNodeProfile profile = new AuraNodeProfile(AuraNodeType.PURE, AuraNodeModifier.BRIGHT, com.darkifov.thaumcraft.aura.AuraNodeWorldRuntime.createProfile(pos).aspects());
            node.initializeAs(profile.type(), profile.modifier(), profile.aspects());
        }
    }

    private static void setLeaves(ServerLevel level, BlockPos pos, BlockState leaves) {
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.is(BlockTags.LEAVES) || current.getMaterial().isReplaceable()) {
            level.setBlock(pos, leaves, 3);
        }
    }

    private static void setReplaceable(ServerLevel level, BlockPos pos, BlockState state) {
        if (canReplaceForTree(level, pos)) {
            level.setBlock(pos, state, 3);
        }
    }
}
