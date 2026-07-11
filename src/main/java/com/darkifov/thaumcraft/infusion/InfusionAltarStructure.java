package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
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

/**
 * Stage123: closer TC4 1.7.10 infusion altar validation and symmetry model.
 *
 * TC4 validLocation requires a center pedestal exactly two blocks below the runic
 * matrix and four infusion pillars at the diagonal corners. For 1.19.2 we support
 * the newly registered tc4 infusion_pillar and keep arcane stone bricks as a
 * temporary compatibility pillar while the exact tile renderer is still being
 * ported.
 */
public final class InfusionAltarStructure {
    public static final int PEDESTAL_RADIUS = 8;
    public static final int STABILIZER_RADIUS = 12;
    public static final int VERTICAL_SCAN_DOWN = 8;
    public static final int VERTICAL_SCAN_UP = 1;

    private InfusionAltarStructure() {
    }

    public static InfusionStructureReport analyze(Level level, BlockPos matrixPos, ArcanePedestalBlockEntity catalystPedestal) {
        List<ArcanePedestalBlockEntity> allPedestals = new ArrayList<>();
        List<ArcanePedestalBlockEntity> componentPedestals = new ArrayList<>();
        Set<BlockPos> pedestalPositions = new HashSet<>();
        Set<BlockPos> filledPedestalPositions = new HashSet<>();
        List<BlockPos> stabilizerPositions = new ArrayList<>();

        BlockPos center = matrixPos.below(2);
        boolean strictCenterPedestal = catalystPedestal != null && catalystPedestal.getBlockPos().equals(center);
        int tc4PillarCount = countTc4Pillars(level, matrixPos);
        boolean strictTc4Location = strictCenterPedestal && tc4PillarCount == 4;

        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-PEDESTAL_RADIUS, -VERTICAL_SCAN_DOWN, -PEDESTAL_RADIUS), center.offset(PEDESTAL_RADIUS, VERTICAL_SCAN_UP, PEDESTAL_RADIUS))) {
            if (level.getBlockEntity(scan) instanceof ArcanePedestalBlockEntity pedestal) {
                allPedestals.add(pedestal);
                pedestalPositions.add(pedestal.getBlockPos().immutable());

                if (!pedestal.stored().isEmpty()) {
                    filledPedestalPositions.add(pedestal.getBlockPos().immutable());
                }

                if (pedestal != catalystPedestal && !pedestal.stored().isEmpty()) {
                    componentPedestals.add(pedestal);
                }
            }
        }

        int symmetricalPairs = 0;
        int missingSymmetry = 0;
        int tc4SymmetryPenalty = 0;

        for (ArcanePedestalBlockEntity pedestal : allPedestals) {
            BlockPos p = pedestal.getBlockPos();

            if (p.equals(center)) {
                continue;
            }

            int dx = center.getX() - p.getX();
            int dz = center.getZ() - p.getZ();

            if (dx == 0 && dz == 0) {
                continue;
            }

            boolean hasItem = filledPedestalPositions.contains(p);
            tc4SymmetryPenalty += hasItem ? 3 : 2;
            BlockPos mirror = new BlockPos(center.getX() + dx, p.getY(), center.getZ() + dz);

            if (pedestalPositions.contains(mirror)) {
                symmetricalPairs++;
                tc4SymmetryPenalty -= 2;

                if (hasItem && filledPedestalPositions.contains(mirror)) {
                    tc4SymmetryPenalty -= 1;
                }
            } else {
                missingSymmetry++;
            }
        }

        symmetricalPairs = symmetricalPairs / 2;
        missingSymmetry = missingSymmetry / 2;

        int matrixAccelerators = 0;
        int matrixStabilizers = 0;

        // v7.82: keep structure summary on the same TC4 getSurroundings adapter
        // used by matrix runtime.  TC4 scans x/z +-12 and y matrix-10..matrix+5,
        // then mirrors stabilizers around the runic matrix block itself.
        TC4InfusionStabilityParity.StabilitySnapshot stabilizerSnapshot = TC4InfusionStabilityParity.scan(level, matrixPos);
        stabilizerPositions.addAll(stabilizerSnapshot.positions());
        int stabilizingBlocks = stabilizerSnapshot.positions().size();
        double stabilizerSymmetry = stabilizerSnapshot.unpaired() * 0.1D - stabilizerSnapshot.mirroredPairs() * 0.2D;

        for (BlockPos scan : BlockPos.betweenClosed(matrixPos.offset(-STABILIZER_RADIUS, -10, -STABILIZER_RADIUS), matrixPos.offset(STABILIZER_RADIUS, 5, STABILIZER_RADIUS))) {
            if (scan.getX() == matrixPos.getX() && scan.getZ() == matrixPos.getZ()) {
                continue;
            }

            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.MATRIX_ACCELERATOR.get())) {
                matrixAccelerators++;
            }

            if (state.is(ThaumcraftMod.MATRIX_STABILIZER.get())) {
                matrixStabilizers++;
            }
        }

        int cappedAccelerators = Math.min(4, matrixAccelerators);
        int cappedStabilizers = Math.min(4, matrixStabilizers);
        int speedMultiplier = 1 + cappedAccelerators;
        int durationModifierPercent = Math.max(20, 100 / speedMultiplier);
        int matrixStabilizationPercent = cappedStabilizers * 25;

        int tc4Penalty = Math.max(0, (int) Math.floor(tc4SymmetryPenalty + stabilizerSymmetry));
        int stabilityScore = Math.max(0, stabilizingBlocks + symmetricalPairs * 2 - tc4Penalty * 2);
        int instabilityPenalty = Math.max(0, tc4Penalty - cappedStabilizers * 2);

        boolean valid = strictTc4Location && componentPedestals.size() > 0;

        Component validity = Component.translatable(valid ? "thaumcraft.infusion.valid" : "thaumcraft.infusion.invalid")
                .withStyle(valid ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component centerState = Component.translatable(strictCenterPedestal
                ? "thaumcraft.infusion.center.ok" : "thaumcraft.infusion.center.missing");
        Component summary = Component.translatable("thaumcraft.infusion.summary.primary",
                        validity, centerState, tc4PillarCount, allPedestals.size(), componentPedestals.size())
                .append(Component.literal("\n"))
                .append(Component.translatable("thaumcraft.infusion.summary.stability",
                        symmetricalPairs, missingSymmetry, tc4Penalty, stabilizingBlocks,
                        speedMultiplier, matrixStabilizationPercent));

        return new InfusionStructureReport(
                valid,
                strictTc4Location,
                allPedestals.size(),
                componentPedestals.size(),
                symmetricalPairs,
                missingSymmetry,
                tc4PillarCount,
                stabilizingBlocks,
                matrixAccelerators,
                matrixStabilizers,
                speedMultiplier,
                durationModifierPercent,
                matrixStabilizationPercent,
                stabilityScore,
                instabilityPenalty,
                tc4Penalty,
                componentPedestals,
                summary
        );
    }

    private static int countTc4Pillars(Level level, BlockPos matrixPos) {
        int count = 0;
        int y = matrixPos.getY() - 2;
        int[][] offsets = new int[][]{{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        for (int[] offset : offsets) {
            BlockPos pos = new BlockPos(matrixPos.getX() + offset[0], y, matrixPos.getZ() + offset[1]);
            if (isInfusionPillar(level.getBlockState(pos))) {
                count++;
            }
        }

        return count;
    }

    private static boolean isInfusionPillar(BlockState state) {
        return state.is(ThaumcraftMod.INFUSION_PILLAR.get()) || state.is(ThaumcraftMod.ARCANE_STONE_BRICKS.get());
    }

    private static boolean isStabilizer(BlockState state) {
        // Stage723-742: keep structure summary and matrix runtime on the same
        // original TC4 stabilizer whitelist/symmetry adapter.
        return TC4InfusionStabilityParity.isOriginalStyleStabilizer(state);
    }
}
