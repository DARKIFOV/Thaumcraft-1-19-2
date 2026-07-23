package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.InfusionPillarBlock;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exact TC4 altar location, cached pedestal scan and symmetry adapter. */
public final class InfusionAltarStructure {
    public static final int PEDESTAL_RADIUS = TC4InfusionAltarFullClosureParity.PEDESTAL_HORIZONTAL_RADIUS;
    public static final int STABILIZER_RADIUS = TC4InfusionAltarFullClosureParity.STABILIZER_HORIZONTAL_RADIUS;

    private InfusionAltarStructure() {}

    /** TileInfusionMatrix#getSurroundings: rebuilds the ordered pedestal cache and symmetry. */
    public static InfusionStructureReport analyze(Level level, BlockPos matrixPos,
                                                   ArcanePedestalBlockEntity catalystPedestal) {
        List<BlockPos> pedestalPositions = scanPedestalPositions(level, matrixPos);
        int symmetry = calculateOriginalSymmetry(level, matrixPos, pedestalPositions);
        return reportFor(level, matrixPos, catalystPedestal, pedestalPositions, symmetry);
    }

    /**
     * Runtime view between getSurroundings calls. New pedestals are deliberately
     * invisible until a rescan; removed cached positions remain in the list but
     * simply fail their current block-entity lookup, matching TC4.
     */
    public static InfusionStructureReport snapshot(Level level, BlockPos matrixPos,
                                                    ArcanePedestalBlockEntity catalystPedestal,
                                                    List<BlockPos> cachedPedestalPositions,
                                                    int cachedSymmetry) {
        return reportFor(level, matrixPos, catalystPedestal,
                cachedPedestalPositions == null ? List.of() : List.copyOf(cachedPedestalPositions),
                cachedSymmetry);
    }

    private static List<BlockPos> scanPedestalPositions(Level level, BlockPos matrixPos) {
        List<BlockPos> positions = new ArrayList<>();
        for (int dx = -PEDESTAL_RADIUS; dx <= PEDESTAL_RADIUS; dx++) {
            for (int dz = -PEDESTAL_RADIUS; dz <= PEDESTAL_RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                for (int dy = TC4InfusionAltarFullClosureParity.PEDESTAL_TOP_OFFSET_FROM_MATRIX;
                     dy >= TC4InfusionAltarFullClosureParity.PEDESTAL_BOTTOM_OFFSET_FROM_MATRIX; dy--) {
                    BlockPos scan = matrixPos.offset(dx, dy, dz);
                    if (level.getBlockEntity(scan) instanceof ArcanePedestalBlockEntity) {
                        positions.add(scan.immutable());
                        break; // TC4 skip=true: only first pedestal in an x/z column.
                    }
                }
            }
        }
        return List.copyOf(positions);
    }

    private static int calculateOriginalSymmetry(Level level, BlockPos matrixPos, List<BlockPos> positions) {
        Set<BlockPos> set = new HashSet<>(positions);
        int symmetry = 0;
        for (BlockPos pos : positions) {
            boolean hasItem = level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal
                    && !pedestal.stored().isEmpty();
            symmetry += hasItem ? 3 : 2;
            BlockPos mirror = new BlockPos(matrixPos.getX() * 2 - pos.getX(), pos.getY(),
                    matrixPos.getZ() * 2 - pos.getZ());
            if (set.contains(mirror)
                    && level.getBlockEntity(mirror) instanceof ArcanePedestalBlockEntity mirrorPedestal) {
                symmetry -= 2;
                if (hasItem && !mirrorPedestal.stored().isEmpty()) symmetry -= 1;
            }
        }

        TC4InfusionStabilityParity.StabilitySnapshot stabilizers =
                TC4InfusionStabilityParity.scan(level, matrixPos);
        float stabilizerSymmetry = stabilizers.unpaired() * 0.1F
                - stabilizers.mirroredPairs() * 0.2F;
        return (int) (symmetry + stabilizerSymmetry);
    }

    private static InfusionStructureReport reportFor(Level level, BlockPos matrixPos,
                                                      ArcanePedestalBlockEntity catalystPedestal,
                                                      List<BlockPos> pedestalPositions,
                                                      int originalSymmetryPenalty) {
        BlockPos center = matrixPos.below(2);
        boolean strictCenter = catalystPedestal != null && catalystPedestal.getBlockPos().equals(center);
        int pillarCount = countTc4Pillars(level, matrixPos);
        boolean strictLocation = strictCenter && pillarCount == 4;

        List<ArcanePedestalBlockEntity> allPedestals = new ArrayList<>();
        List<ArcanePedestalBlockEntity> components = new ArrayList<>();
        Set<BlockPos> currentPositions = new HashSet<>();
        Set<BlockPos> filled = new HashSet<>();
        for (BlockPos pos : pedestalPositions) {
            if (level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal) {
                allPedestals.add(pedestal);
                currentPositions.add(pos);
                if (!pedestal.stored().isEmpty()) {
                    components.add(pedestal);
                    filled.add(pos);
                }
            }
        }

        int pairs = 0;
        int missing = 0;
        for (BlockPos pos : currentPositions) {
            BlockPos mirror = new BlockPos(matrixPos.getX() * 2 - pos.getX(), pos.getY(),
                    matrixPos.getZ() * 2 - pos.getZ());
            if (currentPositions.contains(mirror)) pairs++; else missing++;
        }
        pairs /= 2;
        missing /= 2;

        TC4InfusionStabilityParity.StabilitySnapshot stabilizerSnapshot =
                TC4InfusionStabilityParity.scan(level, matrixPos);
        int stabilizingBlocks = stabilizerSnapshot.positions().size();
        int matrixAccelerators = 0;
        int matrixStabilizers = 0;
        for (BlockPos scan : BlockPos.betweenClosed(
                matrixPos.offset(-STABILIZER_RADIUS,
                        TC4InfusionAltarFullClosureParity.STABILIZER_BOTTOM_OFFSET_FROM_MATRIX,
                        -STABILIZER_RADIUS),
                matrixPos.offset(STABILIZER_RADIUS,
                        TC4InfusionAltarFullClosureParity.STABILIZER_TOP_OFFSET_FROM_MATRIX,
                        STABILIZER_RADIUS))) {
            if (scan.getX() == matrixPos.getX() && scan.getZ() == matrixPos.getZ()) continue;
            BlockState state = level.getBlockState(scan);
            if (state.is(ThaumcraftMod.MATRIX_ACCELERATOR.get())) matrixAccelerators++;
            if (state.is(ThaumcraftMod.MATRIX_STABILIZER.get())) matrixStabilizers++;
        }
        int cappedAccelerators = Math.min(4, matrixAccelerators);
        int cappedStabilizers = Math.min(4, matrixStabilizers);
        int speedMultiplier = 1 + cappedAccelerators;
        int durationModifierPercent = Math.max(20, 100 / speedMultiplier);
        int matrixStabilizationPercent = cappedStabilizers * 25;
        int tc4Penalty = Math.max(0, originalSymmetryPenalty);
        int stabilityScore = Math.max(0, stabilizingBlocks + pairs * 2 - tc4Penalty * 2);
        int instabilityPenalty = Math.max(0, tc4Penalty - cappedStabilizers * 2);

        Component validity = Component.translatable(strictLocation
                        ? "thaumcraft.infusion.valid" : "thaumcraft.infusion.invalid")
                .withStyle(strictLocation ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component centerState = Component.translatable(strictCenter
                ? "thaumcraft.infusion.center.ok" : "thaumcraft.infusion.center.missing");
        Component summary = Component.translatable("thaumcraft.infusion.summary.primary",
                        validity, centerState, pillarCount, allPedestals.size(), components.size())
                .append(Component.literal("\n"))
                .append(Component.translatable("thaumcraft.infusion.summary.stability",
                        pairs, missing, tc4Penalty, stabilizingBlocks,
                        speedMultiplier, matrixStabilizationPercent));

        return new InfusionStructureReport(
                strictLocation, strictLocation,
                allPedestals.size(), components.size(), pairs, missing, pillarCount,
                stabilizingBlocks, matrixAccelerators, matrixStabilizers,
                speedMultiplier, durationModifierPercent, matrixStabilizationPercent,
                stabilityScore, instabilityPenalty, originalSymmetryPenalty,
                List.copyOf(pedestalPositions), List.copyOf(allPedestals), summary);
    }

    private static int countTc4Pillars(Level level, BlockPos matrixPos) {
        int count = 0;
        int[][] offsets = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int[] offset : offsets) {
            BlockPos lower = matrixPos.offset(offset[0], -2, offset[1]);
            BlockState state = level.getBlockState(lower);
            BlockState upper = level.getBlockState(lower.above());
            if (InfusionPillarBlock.isLowerPillar(state)
                    && upper.is(ThaumcraftMod.INFUSION_PILLAR.get())
                    && upper.getValue(InfusionPillarBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER
                    && upper.getValue(InfusionPillarBlock.FACING) == state.getValue(InfusionPillarBlock.FACING)) {
                count++;
            }
        }
        return count;
    }
}
