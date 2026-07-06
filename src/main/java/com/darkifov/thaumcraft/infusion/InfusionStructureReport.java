package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public record InfusionStructureReport(
        boolean valid,
        boolean strictTc4Location,
        int pedestalCount,
        int componentPedestalCount,
        int symmetricalPairs,
        int missingSymmetry,
        int tc4PillarCount,
        int stabilizingBlocks,
        int matrixAccelerators,
        int matrixStabilizers,
        int speedMultiplier,
        int durationModifierPercent,
        int matrixStabilizationPercent,
        int stabilityScore,
        int instabilityPenalty,
        List<ArcanePedestalBlockEntity> componentPedestals,
        Component summary
) {
}
