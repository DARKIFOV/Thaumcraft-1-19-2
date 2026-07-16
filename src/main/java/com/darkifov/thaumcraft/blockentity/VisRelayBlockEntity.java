package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render anchor for the original TC4 vis-relay OBJ. */
public final class VisRelayBlockEntity extends BlockEntity {
    public VisRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.VIS_RELAY_BLOCK_ENTITY.get(), pos, state);
    }
}
