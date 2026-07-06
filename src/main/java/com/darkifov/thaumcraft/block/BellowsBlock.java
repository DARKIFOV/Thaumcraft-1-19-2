package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Stage128: TC4 Bellows port. Original TC4 stores this as blockWoodenDevice meta 0 + TileBellows.
 * In 1.19.2 the direction is carried by blockstate FACING and is counted by Crucible/Arcane Furnace logic.
 */
public class BellowsBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape SHAPE_NS = box(2.0D, 2.0D, 4.0D, 14.0D, 14.0D, 12.0D);
    private static final VoxelShape SHAPE_EW = box(4.0D, 2.0D, 2.0D, 12.0D, 14.0D, 14.0D);

    public BellowsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.EAST || facing == Direction.WEST ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static boolean facesTarget(BlockState state, Direction directionFromBellowsToTarget) {
        return state.getBlock() instanceof BellowsBlock && state.getValue(FACING) == directionFromBellowsToTarget;
    }
}
