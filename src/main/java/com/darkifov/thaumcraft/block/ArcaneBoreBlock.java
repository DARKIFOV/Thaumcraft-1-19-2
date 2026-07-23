package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneBoreBlockEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/** Full TC4 TileArcaneBore block contract: base-only placement, six-way aiming and extended collision. */
public final class ArcaneBoreBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public ArcaneBoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(INVERTED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction supportFace = context.getClickedFace();
        if (supportFace.getAxis() != Direction.Axis.Y) return null;
        BlockPos placedPos = context.getClickedPos();
        BlockPos basePos = placedPos.relative(supportFace.getOpposite());
        if (!context.getLevel().getBlockState(basePos).is(ThaumcraftMod.ARCANE_BORE_BASE.get())) return null;
        return defaultBlockState()
                .setValue(INVERTED, supportFace == Direction.DOWN)
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, INVERTED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ThaumcraftMod.ARCANE_BORE_BLOCK_ENTITY.get(),
                level.isClientSide ? ArcaneBoreBlockEntity::clientTick : ArcaneBoreBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public static BlockPos basePos(BlockPos pos, BlockState state) {
        return state.getValue(INVERTED) ? pos.above() : pos.below();
    }

    public static Direction baseOrientation(BlockState state) {
        return state.getValue(INVERTED) ? Direction.DOWN : Direction.UP;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos base = basePos(pos, state);
        return level.getBlockState(base).is(ThaumcraftMod.ARCANE_BORE_BASE.get())
                && level.getBlockState(base).isFaceSturdy(level, base, baseOrientation(state));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == baseOrientation(state).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    private static VoxelShape extendedShape(Direction direction) {
        double minX = direction.getStepX() < 0 ? -1.0D : 0.0D;
        double minY = direction.getStepY() < 0 ? -1.0D : 0.0D;
        double minZ = direction.getStepZ() < 0 ? -1.0D : 0.0D;
        double maxX = direction.getStepX() > 0 ? 2.0D : 1.0D;
        double maxY = direction.getStepY() > 0 ? 2.0D : 1.0D;
        double maxZ = direction.getStepZ() > 0 ? 2.0D : 1.0D;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return extendedShape(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return extendedShape(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof WandItem) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FACING, hit.getDirection()), 3);
                if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) bore.onOrientationChanged();
                level.playSound(null, pos, TC4Sounds.event("tool"), SoundSource.BLOCKS,
                        0.5F, 0.9F + level.random.nextFloat() * 0.2F);
            }
            player.swing(hand, true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            NetworkHooks.openScreen(serverPlayer, bore, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            for (int slot = 0; slot < bore.inventory().getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                        bore.inventory().extractItem(slot, 64, false));
            }
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }
}
