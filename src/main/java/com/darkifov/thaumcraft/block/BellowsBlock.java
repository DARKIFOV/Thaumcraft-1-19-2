package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.BellowsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Full six-direction TC4 Arcane Bellows block and TileBellows host. */
public class BellowsBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(
            TC4ArcaneBellowsParity.SHAPE_MIN_XZ * 16.0D,
            TC4ArcaneBellowsParity.SHAPE_MIN_Y * 16.0D,
            TC4ArcaneBellowsParity.SHAPE_MIN_XZ * 16.0D,
            TC4ArcaneBellowsParity.SHAPE_MAX_XZ * 16.0D,
            TC4ArcaneBellowsParity.SHAPE_MAX_Y * 16.0D,
            TC4ArcaneBellowsParity.SHAPE_MAX_XZ * 16.0D);

    public BellowsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // BlockWoodenDeviceItem stored clicked-side.getOpposite(): the nozzle points back
        // toward the face/support/machine that the player clicked.
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BellowsBlockEntity bellows) {
            bellows.refreshAttachment();
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BellowsBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ThaumcraftMod.BELLOWS_BLOCK_ENTITY.get(),
                level.isClientSide ? BellowsBlockEntity::clientTick : BellowsBlockEntity::serverTick);
    }

    /**
     * BlockWoodenDevice metadata 0 always used one fixed 0.1..0.9 X/Z, full-height
     * selection and collision box. It did not rotate the box with orientation.
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static boolean facesTarget(BlockState state, Direction directionFromBellowsToTarget) {
        return state.getBlock() instanceof BellowsBlock
                && state.hasProperty(FACING)
                && state.getValue(FACING) == directionFromBellowsToTarget;
    }

    /** TileBellows.getBellows admission: real bellows tile, matching orientation, no redstone power. */
    public static boolean isActiveBellows(Level level, BlockPos pos, Direction directionFromBellowsToTarget) {
        BlockState state = level.getBlockState(pos);
        return level.getBlockEntity(pos) instanceof BellowsBlockEntity
                && facesTarget(state, directionFromBellowsToTarget)
                && !level.hasNeighborSignal(pos);
    }

    /** Exact generic TileBellows.getBellows scan used by furnace and essentia consumers. */
    public static int countActiveBellows(Level level, BlockPos targetPos, Direction[] directions) {
        int bellows = 0;
        for (Direction direction : directions) {
            BlockPos bellowsPos = targetPos.relative(direction);
            if (isActiveBellows(level, bellowsPos, direction.getOpposite())) {
                bellows++;
            }
        }
        return bellows;
    }
}
