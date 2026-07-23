package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneLevitatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Direct modern port of TC4 BlockLifter. */
public final class ArcaneLevitatorBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape FULL_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    /** Horizontal faces are sturdy, while UP/DOWN are deliberately not. */
    private static final VoxelShape HORIZONTAL_SUPPORT_SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D),
            Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D),
            Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D));

    public ArcaneLevitatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return defaultBlockState().setValue(POWERED,
                ArcaneLevitatorBlockEntity.isPowered(context.getLevel(), pos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneLevitatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ThaumcraftMod.ARCANE_LEVITATOR_BLOCK_ENTITY.get(), ArcaneLevitatorBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return FULL_SHAPE;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return HORIZONTAL_SUPPORT_SHAPE;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        return direction != null && direction.getAxis().isHorizontal();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!oldState.is(state.getBlock())) {
            markStackBelow(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            markStackBelow(level, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean moving) {
        boolean powered = ArcaneLevitatorBlockEntity.isPowered(level, pos);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ArcaneLevitatorBlockEntity levitator
                && powered != levitator.lastPowerState()) {
            markStackBelow(level, pos);
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ArcaneLevitatorBlockEntity levitator) || levitator.rangeAbove() <= 0) {
            return;
        }
        double x = pos.getX() + 0.2F + random.nextFloat() * 0.6F;
        double z = pos.getZ() + 0.2F + random.nextFloat() * 0.6F;
        TC4ArcaneLevitatorEffectsBridge.spawn(level, x, pos.getY() + 1.0D, z, random);
    }

    /** TC4 updateLifterStack starts one block below the changed lifter. */
    private static void markStackBelow(Level level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = origin.below().mutable();
        while (cursor.getY() >= level.getMinBuildHeight()
                && level.getBlockState(cursor).getBlock() instanceof ArcaneLevitatorBlock) {
            if (level.getBlockEntity(cursor) instanceof ArcaneLevitatorBlockEntity levitator) {
                levitator.requestRangeUpdate();
            }
            cursor.move(Direction.DOWN);
        }
    }
}
