package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage233-242 1.19.2 replacement for TC4 blockEldritchNothing.
 *
 * <p>TC4 used this block as an invisible, unbreakable Outer Lands boundary/void
 * skin.  Barrier was too visible in creative and changed structure parity, so
 * the port now has a dedicated invisible block for GenCommon code 8/17.</p>
 */
public class EldritchNothingBlock extends Block {
    public EldritchNothingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
