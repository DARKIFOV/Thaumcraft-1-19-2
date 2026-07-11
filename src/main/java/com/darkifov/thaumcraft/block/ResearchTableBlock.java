package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ResearchTableBlockEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.19.2 owner block for the original two-table TC4 research station.
 *
 * <p>FACING stores the direction from the inventory/master half toward the
 * partner half, replacing the old 1.7.10 metadata values 2..5. PRIMARY keeps
 * both occupied block positions in the world while ensuring only one half owns
 * the inventory, renderer and menu.</p>
 */
public class ResearchTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty PRIMARY = BooleanProperty.create("primary");

    private static final VoxelShape X_LOW = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(2, 0, 2, 6, 12, 6),
            Block.box(2, 0, 10, 6, 12, 14),
            Block.box(4, 2, 6, 16, 6, 10));
    private static final VoxelShape X_HIGH = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(10, 0, 2, 14, 12, 6),
            Block.box(10, 0, 10, 14, 12, 14),
            Block.box(0, 2, 6, 12, 6, 10));
    private static final VoxelShape Z_LOW = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(2, 0, 2, 6, 12, 6),
            Block.box(10, 0, 2, 14, 12, 6),
            Block.box(6, 2, 4, 10, 6, 16));
    private static final VoxelShape Z_HIGH = Shapes.or(
            Block.box(0, 12, 0, 16, 16, 16),
            Block.box(2, 0, 10, 6, 12, 14),
            Block.box(10, 0, 10, 14, 12, 14),
            Block.box(6, 2, 0, 10, 6, 12));

    public ResearchTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.EAST)
                .setValue(PRIMARY, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PRIMARY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getClockWise())
                .setValue(PRIMARY, true);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PRIMARY) ? new ResearchTableBlockEntity(pos, state) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // The exact two-block model is drawn only by the primary half's BER.
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean primary = state.getValue(PRIMARY);
        if (facing.getAxis() == Direction.Axis.X) {
            boolean lowSide = (facing == Direction.EAST) == primary;
            return lowSide ? X_LOW : X_HIGH;
        }
        boolean lowSide = (facing == Direction.SOUTH) == primary;
        return lowSide ? Z_LOW : Z_HIGH;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide || !state.getValue(PRIMARY)) {
            return null;
        }
        return createTickerHelper(type, ThaumcraftMod.RESEARCH_TABLE_BLOCK_ENTITY.get(),
                ResearchTableBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos primaryPos = state.getValue(PRIMARY)
                ? pos
                : pos.relative(state.getValue(FACING).getOpposite());

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(primaryPos);
            if (blockEntity instanceof ResearchTableBlockEntity table && player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
                ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
                NetworkHooks.openScreen(serverPlayer, table, buffer -> buffer.writeBlockPos(primaryPos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            boolean primary = state.getValue(PRIMARY);
            BlockPos otherPos = primary ? pos.relative(facing) : pos.relative(facing.getOpposite());
            BlockState other = level.getBlockState(otherPos);

            if (primary) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ResearchTableBlockEntity table) {
                    Containers.dropContents(level, pos, table);
                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }

            if (other.is(this)
                    && other.getValue(PRIMARY) != primary
                    && other.getValue(FACING) == facing) {
                // Replacing with air without drops avoids a second Research Table
                // item while still running the paired half's cleanup path.
                level.removeBlock(otherPos, false);
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }
}
