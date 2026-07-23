package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Render anchor for TC4's coordinate-seeded candle geometry.
 *
 * <p>The original block had no persistent tile data; a lightweight block
 * entity is used only because 1.19.2's block-entity renderer receives the
 * world position required to reproduce {@code new Random(x + y * z)}.</p>
 */
public final class TallowCandleBlockEntity extends BlockEntity {
    public TallowCandleBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TALLOW_CANDLE_BLOCK_ENTITY.get(), pos, state);
    }
}
