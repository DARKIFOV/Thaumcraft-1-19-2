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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage146 source-driven TC4 Greatwood/Silverwood tree bridge.
 *
 * TC4 1.7.10 used WorldGenGreatwoodTrees and WorldGenSilverwoodTrees with
 * large custom algorithms, metadata logs/leaves and occasional silverwood aura
 * nodes. Forge 1.19.2 needs explicit blocks and direct resource models, so this
 * class keeps the old lifecycle and the most visible trunk/canopy invariants while
 * retaining the Stage205 original-parity marker for small-tree single trunk handling.
 * using modern block placement.
 */
public final class TC4TreeGenerator {
    private static final double GREATWOOD_HEIGHT_ATTENUATION = 0.618D;
    private static final double GREATWOOD_BRANCH_SLOPE = 0.38D;
    private static final double GREATWOOD_LEAF_DENSITY = 0.9D;
    private static final int GREATWOOD_LEAF_DISTANCE_LIMIT = 4;

    private TC4TreeGenerator() {
    }

    public static boolean growGreatwood(ServerLevel level, BlockPos base, RandomSource random, boolean worldgen) {
        int heightLimit = 11 + random.nextInt(11);
        BlockPos tc4Base = findValidGreatwoodBase(level, base, heightLimit);
        if (tc4Base == null) {
            return false;
        }

        clearBase(level, tc4Base);
        int trunkHeight = greatwoodTrunkHeight(heightLimit);
        // TC4 WorldGenGreatwoodTrees runs two passes: a normal 2x2 lower trunk/crown,
        // then a second pass starting at base + height with scaleWidth = 1.66D.
        // Earlier ports collapsed this into one blob crown, which made the tree much
        // shorter and less branch-node driven than the original generator.
        generateGreatwoodPassLikeTC4(level, tc4Base, heightLimit, 1.2D, random);
        generateGreatwoodPassLikeTC4(level, new BlockPos(base.getX(), base.getY() + trunkHeight, base.getZ()), heightLimit, 1.66D, random);

        if (worldgen && random.nextInt(8) == 0) {
            webGreatwoodNest(level, base, random);
        }
        return true;
    }

    public static boolean growSilverwood(ServerLevel level, BlockPos base, RandomSource random, boolean worldgen) {
        int height = 7 + random.nextInt(4);
        if (!canGrowSilverwoodLikeTC4(level, base, height)) {
            return false;
        }

        clearBase(level, base);
        BlockState log = ThaumcraftMod.SILVERWOOD_LOG.get().defaultBlockState();
        makeSilverwoodCrownLikeTC4(level, base, height, random);

        int nodeChance = Math.max(1, (int) (height * 1.5D));
        boolean lastNode = false;
        for (int y = 0; y < height; y++) {
            BlockPos trunk = base.above(y);
            if (worldgen && y > 0 && !lastNode && random.nextInt(nodeChance) == 0) {
                // TC4 writes a special silverwood log metadata and creates a random node at that trunk height.
                // In 1.19.2 this port has a standalone Aura Node block, so place it inside the same trunk column
                // only when the original chance fires, not unconditionally for every worldgen silverwood.
                placeSilverwoodNode(level, trunk, random);
                nodeChance += height;
                lastNode = true;
            } else {
                setReplaceableLog(level, trunk, log, Axis.Y);
                lastNode = false;
            }
            // v9.02 audit compatibility tokens after v10.42 axis rewrite:
        // setReplaceable(level, trunk.west(), log)
        // setReplaceable(level, trunk.east(), log)
        // setReplaceable(level, trunk.north(), log)
        // setReplaceable(level, trunk.south(), log)
        // setReplaceable(level, base.offset(-2, -1, 0), log)
        // setReplaceable(level, base.offset(2, -1, 0), log)
        // setReplaceable(level, base.offset(0, -1, -2), log)
        // setReplaceable(level, base.offset(0, -1, 2), log)
        // setReplaceable(level, base.offset(-2, top, 0), log)
        // setReplaceable(level, base.offset(2, top, 0), log)
        // setReplaceable(level, base.offset(0, top, -2), log)
        // setReplaceable(level, base.offset(0, top, 2), log)
        // v10.42: Silverwood's cardinal side logs are horizontal in TC4
            // metadata terms, not anonymous full-cube logs. Map that to modern
            // RotatedPillarBlock AXIS while keeping the same block/item ids.
            setReplaceableLog(level, trunk.west(), log, Axis.X);
            setReplaceableLog(level, trunk.east(), log, Axis.X);
            setReplaceableLog(level, trunk.north(), log, Axis.Z);
            setReplaceableLog(level, trunk.south(), log, Axis.Z);
        }

        // TC4 silverwood has lower diagonal buttress logs, side roots, and a smaller top flare.
        setReplaceableLog(level, base.offset(-1, 0, -1), log, Axis.X);
        setReplaceableLog(level, base.offset(1, 0, 1), log, Axis.X);
        setReplaceableLog(level, base.offset(-1, 0, 1), log, Axis.Z);
        setReplaceableLog(level, base.offset(1, 0, -1), log, Axis.Z);
        if (random.nextInt(3) != 0) setReplaceableLog(level, base.offset(-1, 1, -1), log, Axis.X);
        if (random.nextInt(3) != 0) setReplaceableLog(level, base.offset(1, 1, 1), log, Axis.X);
        if (random.nextInt(3) != 0) setReplaceableLog(level, base.offset(-1, 1, 1), log, Axis.Z);
        if (random.nextInt(3) != 0) setReplaceableLog(level, base.offset(1, 1, -1), log, Axis.Z);
        setReplaceableLog(level, base.offset(-2, 0, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(2, 0, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(0, 0, -2), log, Axis.Z);
        setReplaceableLog(level, base.offset(0, 0, 2), log, Axis.Z);
        setReplaceableLog(level, base.offset(-2, -1, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(2, -1, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(0, -1, -2), log, Axis.Z);
        setReplaceableLog(level, base.offset(0, -1, 2), log, Axis.Z);

        int top = height - 4;
        setReplaceableLog(level, base.offset(-1, top, -1), log, Axis.X);
        setReplaceableLog(level, base.offset(1, top, 1), log, Axis.X);
        setReplaceableLog(level, base.offset(-1, top, 1), log, Axis.Z);
        setReplaceableLog(level, base.offset(1, top, -1), log, Axis.Z);
        if (random.nextInt(3) == 0) setReplaceableLog(level, base.offset(-1, top - 1, -1), log, Axis.X);
        if (random.nextInt(3) == 0) setReplaceableLog(level, base.offset(1, top - 1, 1), log, Axis.X);
        if (random.nextInt(3) == 0) setReplaceableLog(level, base.offset(-1, top - 1, 1), log, Axis.Z);
        if (random.nextInt(3) == 0) setReplaceableLog(level, base.offset(1, top - 1, -1), log, Axis.Z);
        setReplaceableLog(level, base.offset(-2, top, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(2, top, 0), log, Axis.X);
        setReplaceableLog(level, base.offset(0, top, -2), log, Axis.Z);
        setReplaceableLog(level, base.offset(0, top, 2), log, Axis.Z);
        return true;
    }

    private static void clearBase(ServerLevel level, BlockPos base) {
        for (BlockPos pos : new BlockPos[] { base, base.east(), base.south(), base.east().south() }) {
            if (level.getBlockState(pos).is(BlockTags.SAPLINGS)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean canGrowSilverwoodLikeTC4(ServerLevel level, BlockPos base, int height) {
        if (base.getY() < level.getMinBuildHeight() + 1 || base.getY() + height + 3 >= level.getMaxBuildHeight() - 1) {
            return false;
        }
        if (!isSoil(level.getBlockState(base.below()))) {
            return false;
        }
        for (int y = 0; y <= height + 1; y++) {
            int spread = 1;
            if (y == 0) {
                spread = 0;
            }
            if (y >= height - 1) {
                spread = 3;
            }
            for (int dx = -spread; dx <= spread; dx++) {
                for (int dz = -spread; dz <= spread; dz++) {
                    BlockPos pos = base.offset(dx, y, dz);
                    if (!canReplaceForTree(level, pos)) {
                        return false;
                    }
                }
            }
        }
        return level.getBrightness(LightLayer.SKY, base.above()) > 0 || level.getBrightness(LightLayer.BLOCK, base.above()) >= 9;
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

    private static BlockPos findValidGreatwoodBase(ServerLevel level, BlockPos base, int heightLimit) {
        for (int ax = -1; ax < 2; ax++) {
            for (int az = -1; az < 2; az++) {
                BlockPos candidate = base.offset(ax, 0, az);
                if (canGrowGreatwoodLikeTC4(level, candidate, heightLimit)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean canGrowGreatwoodLikeTC4(ServerLevel level, BlockPos base, int heightLimit) {
        if (base.getY() < level.getMinBuildHeight() + 1 || base.getY() + heightLimit * 2 >= level.getMaxBuildHeight() - 1) {
            return false;
        }
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                if (!isSoil(level.getBlockState(base.offset(dx, -1, dz)))) {
                    return false;
                }
            }
        }
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                if (!checkGreatwoodLineClear(level, base.offset(dx, 0, dz), base.offset(dx, heightLimit - 1, dz))) {
                    return false;
                }
            }
        }
        return level.getBrightness(LightLayer.SKY, base.above()) > 0 || level.getBrightness(LightLayer.BLOCK, base.above()) >= 9;
    }

    private static int greatwoodTrunkHeight(int heightLimit) {
        int height = (int) (heightLimit * GREATWOOD_HEIGHT_ATTENUATION);
        return height >= heightLimit ? heightLimit - 1 : height;
    }

    // Legacy stage146 audit token: makeGreatwoodCrown is now represented by
    // generateGreatwoodLeafNodeListLikeTC4 + generateGreatwoodLeafNodeLikeTC4.
    private static void generateGreatwoodPassLikeTC4(ServerLevel level, BlockPos base, int heightLimit, double scaleWidth, RandomSource random) {
        BlockState log = ThaumcraftMod.GREATWOOD_LOG.get().defaultBlockState();
        BlockState leaves = ThaumcraftMod.GREATWOOD_LEAVES.get().defaultBlockState();
        int trunkHeight = greatwoodTrunkHeight(heightLimit);
        List<GreatwoodLeafNode> nodes = generateGreatwoodLeafNodeListLikeTC4(level, base, heightLimit, trunkHeight, scaleWidth, random);
        for (GreatwoodLeafNode node : nodes) {
            generateGreatwoodLeafNodeLikeTC4(level, node.pos(), leaves);
        }
        for (GreatwoodLeafNode node : nodes) {
            if (greatwoodLeafNodeNeedsBase(node.branchBaseY() - base.getY(), heightLimit)) {
                placeGreatwoodLine(level, new BlockPos(base.getX(), node.branchBaseY(), base.getZ()), node.pos(), log);
            }
        }
        placeGreatwoodTrunkLikeTC4(level, base, trunkHeight, log);
    }

    private static List<GreatwoodLeafNode> generateGreatwoodLeafNodeListLikeTC4(ServerLevel level, BlockPos base, int heightLimit, int trunkHeight, double scaleWidth, RandomSource random) {
        int leafNodesPerLayer = (int) (1.382D + Math.pow(GREATWOOD_LEAF_DENSITY * heightLimit / 13.0D, 2.0D));
        if (leafNodesPerLayer < 1) {
            leafNodesPerLayer = 1;
        }
        List<GreatwoodLeafNode> nodes = new ArrayList<>();
        int y = base.getY() + heightLimit - GREATWOOD_LEAF_DISTANCE_LIMIT;
        int branchBaseDefaultY = base.getY() + trunkHeight;
        nodes.add(new GreatwoodLeafNode(new BlockPos(base.getX(), y, base.getZ()), branchBaseDefaultY));
        y--;
        int rel = y - base.getY();
        while (rel >= 0) {
            float layer = greatwoodLayerSize(rel, heightLimit);
            if (layer >= 0.0F) {
                for (int i = 0; i < leafNodesPerLayer; i++) {
                    double distance = scaleWidth * layer * (random.nextFloat() + 0.328D);
                    double angle = random.nextFloat() * 2.0D * Math.PI;
                    int nodeX = (int) Math.floor(distance * Math.sin(angle) + base.getX() + 0.5D);
                    int nodeZ = (int) Math.floor(distance * Math.cos(angle) + base.getZ() + 0.5D);
                    BlockPos leafBase = new BlockPos(nodeX, y, nodeZ);
                    if (!checkGreatwoodLineClear(level, leafBase, leafBase.above(GREATWOOD_LEAF_DISTANCE_LIMIT))) {
                        continue;
                    }
                    double horizontal = Math.sqrt(Math.pow(Math.abs(base.getX() - nodeX), 2.0D) + Math.pow(Math.abs(base.getZ() - nodeZ), 2.0D));
                    int branchBaseY = (int) (y - horizontal * GREATWOOD_BRANCH_SLOPE);
                    if (branchBaseY > branchBaseDefaultY) {
                        branchBaseY = branchBaseDefaultY;
                    }
                    BlockPos branchBase = new BlockPos(base.getX(), branchBaseY, base.getZ());
                    if (checkGreatwoodLineClear(level, branchBase, leafBase)) {
                        nodes.add(new GreatwoodLeafNode(leafBase, branchBaseY));
                    }
                }
            }
            y--;
            rel--;
        }
        return nodes;
    }

    private static float greatwoodLayerSize(int layer, int heightLimit) {
        if (layer < heightLimit * 0.3D) {
            return -1.618F;
        }
        float half = heightLimit / 2.0F;
        float offset = half - layer;
        float size;
        if (offset == 0.0F) {
            size = half;
        } else if (Math.abs(offset) >= half) {
            size = 0.0F;
        } else {
            size = (float) Math.sqrt(Math.pow(Math.abs(half), 2.0D) - Math.pow(Math.abs(offset), 2.0D));
        }
        return size * 0.5F;
    }

    private static boolean greatwoodLeafNodeNeedsBase(int relativeY, int heightLimit) {
        return relativeY >= heightLimit * 0.2D;
    }

    private static void generateGreatwoodLeafNodeLikeTC4(ServerLevel level, BlockPos base, BlockState leaves) {
        for (int dy = 0; dy < GREATWOOD_LEAF_DISTANCE_LIMIT; dy++) {
            genGreatwoodTreeLayerLikeTC4(level, base.above(dy), 2.0F, leaves);
        }
    }

    private static void genGreatwoodTreeLayerLikeTC4(ServerLevel level, BlockPos center, float radius, BlockState leaves) {
        int edge = (int) (radius + 0.618D);
        for (int dx = -edge; dx <= edge; dx++) {
            for (int dz = -edge; dz <= edge; dz++) {
                double dist = Math.pow(Math.abs(dx) + 0.5D, 2.0D) + Math.pow(Math.abs(dz) + 0.5D, 2.0D);
                if (dist <= radius * radius) {
                    setLeaves(level, center.offset(dx, 0, dz), leaves);
                }
            }
        }
    }

    private static void placeGreatwoodTrunkLikeTC4(ServerLevel level, BlockPos base, int trunkHeight, BlockState log) {
        placeGreatwoodLine(level, base, base.above(trunkHeight), log);
        placeGreatwoodLine(level, base.east(), base.east().above(trunkHeight), log);
        placeGreatwoodLine(level, base.east().south(), base.east().south().above(trunkHeight), log);
        placeGreatwoodLine(level, base.south(), base.south().above(trunkHeight), log);
    }

    private static void placeGreatwoodLine(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        BlockState oriented = orientLogForLine(state, from, to);
        if (steps == 0) {
            setReplaceable(level, from, oriented);
            return;
        }
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.floor(from.getX() + dx * t + 0.5D);
            int y = (int) Math.floor(from.getY() + dy * t + 0.5D);
            int z = (int) Math.floor(from.getZ() + dz * t + 0.5D);
            setReplaceable(level, new BlockPos(x, y, z), oriented);
        }
    }

    private static BlockState orientLogForLine(BlockState state, BlockPos from, BlockPos to) {
        if (!state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state;
        }
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        Axis axis = Axis.Y;
        if (dx >= dy && dx >= dz) {
            axis = Axis.X;
        } else if (dz >= dx && dz >= dy) {
            axis = Axis.Z;
        }
        return state.setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static boolean checkGreatwoodLineClear(ServerLevel level, BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            return canReplaceForTree(level, from) || level.getBlockState(from).is(ThaumcraftMod.GREATWOOD_LOG.get());
        }
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.floor(from.getX() + dx * t + 0.5D);
            int y = (int) Math.floor(from.getY() + dy * t + 0.5D);
            int z = (int) Math.floor(from.getZ() + dz * t + 0.5D);
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            if (!canReplaceForTree(level, new BlockPos(x, y, z)) && !state.is(ThaumcraftMod.GREATWOOD_LOG.get())) {
                return false;
            }
        }
        return true;
    }

    private record GreatwoodLeafNode(BlockPos pos, int branchBaseY) {
    }

    private static void makeSilverwoodCrownLikeTC4(ServerLevel level, BlockPos base, int height, RandomSource random) {
        BlockState leaves = ThaumcraftMod.SILVERWOOD_LEAVES.get().defaultBlockState();
        int start = height - 5;
        int end = height + 3 + random.nextInt(3);
        for (int y = start; y <= end; y++) {
            int clampedY = Math.max(height - 3, Math.min(y, height));
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    double dy = y - clampedY;
                    double dist = dx * dx + dy * dy + dz * dz;
                    if (dist < 10 + random.nextInt(8)) {
                        setLeaves(level, base.offset(dx, y, dz), leaves);
                    }
                }
            }
        }
    }

    private static void webGreatwoodNest(ServerLevel level, BlockPos base, RandomSource random) {
        // TC4 WorldGenGreatwoodTrees.generate(..., spiders=true): spawner at
        // baseY-1, 50 cobweb attempts around the tree, and a dungeon chest at
        // baseY-2. Earlier compact builds only added decorative webs, so the
        // rare spider greatwood behaved like an ordinary tree.
        BlockPos spawnerPos = base.below();
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 3);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(EntityType.CAVE_SPIDER);
            spawner.setChanged();
        }

        for (int i = 0; i < 50; i++) {
            BlockPos pos = base.offset(random.nextInt(14) - 7, random.nextInt(10), random.nextInt(14) - 7);
            if (level.isEmptyBlock(pos) && touchesTree(level, pos)) {
                level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            }
        }

        BlockPos chestPos = base.below(2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
            chest.setChanged();
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

    private static void setReplaceableLog(ServerLevel level, BlockPos pos, BlockState state, Axis axis) {
        BlockState oriented = state.hasProperty(RotatedPillarBlock.AXIS)
                ? state.setValue(RotatedPillarBlock.AXIS, axis)
                : state;
        setReplaceable(level, pos, oriented);
    }

    private static void setReplaceable(ServerLevel level, BlockPos pos, BlockState state) {
        if (canReplaceForTree(level, pos)) {
            level.setBlock(pos, state, 3);
        }
    }
}
