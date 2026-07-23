package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * The original TC4 infusion pillar is a two-block structure: metadata 3 is the
 * lower TileInfusionPillar and metadata 4 is the upper visual half. 1.19.2
 * stores the four original orientations in a horizontal facing state.
 */
public final class InfusionPillarBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public InfusionPillarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos above = context.getClickedPos().above();
        if (context.getLevel().getBlockState(above).canBeReplaced(context)) {
            return defaultBlockState()
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            net.minecraft.world.entity.LivingEntity placer,
                            net.minecraft.world.item.ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        Direction partnerDirection = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == partnerDirection) {
            if (!neighbour.is(this) || neighbour.getValue(HALF) == half
                    || neighbour.getValue(FACING) != state.getValue(FACING)) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }


    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() != newState.getBlock() && !moving) {
            DoubleBlockHalf half = oldState.getValue(HALF);
            BlockPos partnerPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState partner = level.getBlockState(partnerPos);
            if (partner.is(this)
                    && partner.getValue(HALF) != half
                    && partner.getValue(FACING) == oldState.getValue(FACING)) {
                level.destroyBlock(partnerPos, true);
            }
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    /** True only for the lower half used by TileInfusionMatrix#validLocation. */
    public static boolean isLowerPillar(BlockState state) {
        return state.getBlock() instanceof InfusionPillarBlock
                && state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    /** TC4 orientation bytes 2/3/4/5 mapped to the four diagonal pillars. */
    public static Direction facingForOriginalOrientation(int orientation) {
        return switch (orientation) {
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            case 4 -> Direction.EAST;
            case 5 -> Direction.NORTH;
            default -> Direction.NORTH;
        };
    }
}
