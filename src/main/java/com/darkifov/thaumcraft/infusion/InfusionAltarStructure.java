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

public final class InfusionAltarStructure {
    public static final int RADIUS = 4;

    private InfusionAltarStructure() {
    }

    public static InfusionStructureReport analyze(Level level, BlockPos matrixPos, ArcanePedestalBlockEntity catalystPedestal) {
        List<ArcanePedestalBlockEntity> allPedestals = new ArrayList<>();
        List<ArcanePedestalBlockEntity> componentPedestals = new ArrayList<>();
        Set<BlockPos> pedestalPositions = new HashSet<>();

        BlockPos center = catalystPedestal == null ? matrixPos.below(2) : catalystPedestal.getBlockPos();

        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-RADIUS, -2, -RADIUS), center.offset(RADIUS, 2, RADIUS))) {
            if (level.getBlockEntity(scan) instanceof ArcanePedestalBlockEntity pedestal) {
                allPedestals.add(pedestal);
                pedestalPositions.add(pedestal.getBlockPos().immutable());

                if (pedestal != catalystPedestal && !pedestal.stored().isEmpty()) {
                    componentPedestals.add(pedestal);
                }
            }
        }

        int symmetricalPairs = 0;
        int missingSymmetry = 0;

        for (ArcanePedestalBlockEntity pedestal : componentPedestals) {
            BlockPos p = pedestal.getBlockPos();
            int dx = p.getX() - center.getX();
            int dy = p.getY() - center.getY();
            int dz = p.getZ() - center.getZ();

            if (dx == 0 && dz == 0) {
                continue;
            }

            BlockPos mirror = new BlockPos(center.getX() - dx, center.getY() + dy, center.getZ() - dz);

            if (pedestalPositions.contains(mirror)) {
                symmetricalPairs++;
            } else {
                missingSymmetry++;
            }
        }

        symmetricalPairs = symmetricalPairs / 2;

        int stabilizingBlocks = 0;
        int matrixAccelerators = 0;
        int matrixStabilizers = 0;

        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-RADIUS, -2, -RADIUS), center.offset(RADIUS, 2, RADIUS))) {
            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.ARCANE_STONE_BRICKS.get())) {
                stabilizingBlocks++;
            }

            if (state.is(ThaumcraftMod.NODE_STABILIZER.get())) {
                stabilizingBlocks += 2;
            }

            if (state.is(ThaumcraftMod.MATRIX_ACCELERATOR.get())) {
                matrixAccelerators++;
            }

            if (state.is(ThaumcraftMod.MATRIX_STABILIZER.get())) {
                matrixStabilizers++;
                stabilizingBlocks += 10;
            }
        }

        int cappedAccelerators = Math.min(4, matrixAccelerators);
        int cappedStabilizers = Math.min(4, matrixStabilizers);
        int speedMultiplier = 1 + cappedAccelerators;
        int durationModifierPercent = Math.max(20, 100 / speedMultiplier);
        int matrixStabilizationPercent = cappedStabilizers * 25;

        int stabilityScore = stabilizingBlocks + symmetricalPairs * 3 - missingSymmetry * 2;
        int instabilityPenalty = Math.max(0, missingSymmetry * 2 - stabilizingBlocks / 3 - cappedStabilizers * 2);

        boolean valid = catalystPedestal != null && allPedestals.size() >= 2;

        Component summary = Component.literal("Infusion altar: ")
                .append(Component.literal(valid ? "VALID" : "WEAK").withStyle(valid ? ChatFormatting.GREEN : ChatFormatting.RED))
                .append(Component.literal(" | Pedestals: " + allPedestals.size()))
                .append(Component.literal(" | Components: " + componentPedestals.size()))
                .append(Component.literal(" | Symmetry pairs: " + symmetricalPairs))
                .append(Component.literal(" | Missing symmetry: " + missingSymmetry))
                .append(Component.literal(" | Stabilizers: " + stabilizingBlocks))
                .append(Component.literal(" | Matrix accelerators: " + matrixAccelerators))
                .append(Component.literal(" | Matrix pylons: " + matrixStabilizers))
                .append(Component.literal(" | Speed: x" + speedMultiplier))
                .append(Component.literal(" | Stabilization: " + matrixStabilizationPercent + "%"))
                .append(Component.literal(" | Stability score: " + stabilityScore));

        return new InfusionStructureReport(
                valid,
                allPedestals.size(),
                componentPedestals.size(),
                symmetricalPairs,
                missingSymmetry,
                stabilizingBlocks,
                matrixAccelerators,
                matrixStabilizers,
                speedMultiplier,
                durationModifierPercent,
                matrixStabilizationPercent,
                stabilityScore,
                instabilityPenalty,
                componentPedestals,
                summary
        );
    }
}
