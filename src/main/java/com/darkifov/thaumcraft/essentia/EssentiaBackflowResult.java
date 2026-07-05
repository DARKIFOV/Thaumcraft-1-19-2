package com.darkifov.thaumcraft.essentia;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record EssentiaBackflowResult(
        Aspect aspect,
        BlockPos source,
        Direction sourceSide,
        BlockPos destination,
        Direction destinationSide,
        int sourcePressure,
        int destinationSuction,
        int conflictCount,
        boolean backflowBlocked
) {
    public boolean canMove() {
        return aspect != null
                && source != null
                && destination != null
                && destinationSuction > sourcePressure
                && !backflowBlocked;
    }

    public int netPull() {
        return destinationSuction - sourcePressure;
    }
}
