package com.darkifov.thaumcraft.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record EssentiaSuctionPath(BlockPos from, BlockPos to, Direction direction, int suction) {
    public boolean valid() {
        return from != null && to != null && direction != null && suction > 0;
    }
}
