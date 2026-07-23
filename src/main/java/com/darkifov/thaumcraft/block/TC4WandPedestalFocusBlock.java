package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Focused topper for the TC4 wand recharge pedestal. */
public final class TC4WandPedestalFocusBlock extends Block {
    public TC4WandPedestalFocusBlock(Properties properties) { super(properties); }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ThaumcraftMod.TC4_WAND_PEDESTAL.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState changedState,
                                  LevelAccessor level, BlockPos pos, BlockPos changedPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, changedState, level, pos, changedPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos base = pos.below();
        BlockState baseState = level.getBlockState(base);
        return baseState.is(ThaumcraftMod.TC4_WAND_PEDESTAL.get())
                ? baseState.use(level, player, hand, new BlockHitResult(hit.getLocation(), hit.getDirection(), base, hit.isInside()))
                : InteractionResult.PASS;
    }
}
