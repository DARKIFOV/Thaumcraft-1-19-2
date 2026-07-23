package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.HungryChestBlockEntity;
import com.darkifov.thaumcraft.blockentity.TC4HungryChestParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Complete production port of TC4 4.2.3.5 {@code BlockChestHungry}. */
public class HungryChestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    private static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public HungryChestBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HungryChestBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ThaumcraftMod.HUNGRY_CHEST_BLOCK_ENTITY.get(),
                level.isClientSide ? HungryChestBlockEntity::clientTick : HungryChestBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest) {
            if (!level.isClientSide) {
                player.openMenu(chest);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity itemEntity && itemEntity.isAlive()
                && level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest
                && intersectsOriginalCollision(itemEntity.getBoundingBox(), pos)) {
            chest.eat(itemEntity);
        }
        super.entityInside(state, level, pos, entity);
    }

    public static boolean intersectsOriginalCollision(AABB entityBounds, BlockPos pos) {
        return TC4HungryChestParity.intersectsCollision(
                entityBounds.minX, entityBounds.minY, entityBounds.minZ,
                entityBounds.maxX, entityBounds.maxY, entityBounds.maxZ,
                pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest) {
                dropContentsOriginal(level, pos, chest, level.random);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(oldState, level, pos, newState, moving);
        }
    }

    /** Exact 1.7.10 break-drop splitting: one position per slot, chunks of 10..30, copied NBT. */
    public static void dropContentsOriginal(Level level, BlockPos pos, Container inventory, RandomSource random) {
        if (level.isClientSide) {
            return;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stored = inventory.getItem(slot);
            if (stored.isEmpty()) {
                continue;
            }
            float offsetX = random.nextFloat() * TC4HungryChestParity.DROP_POSITION_RANGE
                    + TC4HungryChestParity.DROP_POSITION_MIN;
            float offsetY = random.nextFloat() * TC4HungryChestParity.DROP_POSITION_RANGE
                    + TC4HungryChestParity.DROP_POSITION_MIN;
            float offsetZ = random.nextFloat() * TC4HungryChestParity.DROP_POSITION_RANGE
                    + TC4HungryChestParity.DROP_POSITION_MIN;
            while (!stored.isEmpty()) {
                int count = TC4HungryChestParity.nextDropCount(stored.getCount(),
                        random.nextInt(TC4HungryChestParity.DROP_RANDOM_BOUND));
                ItemStack droppedStack = stored.copy();
                droppedStack.setCount(count);
                stored.shrink(count);
                ItemEntity dropped = new ItemEntity(level,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, droppedStack);
                dropped.setDeltaMovement(
                        random.nextGaussian() * TC4HungryChestParity.DROP_MOTION_SIGMA,
                        random.nextGaussian() * TC4HungryChestParity.DROP_MOTION_SIGMA
                                + TC4HungryChestParity.DROP_MOTION_Y_BIAS,
                        random.nextGaussian() * TC4HungryChestParity.DROP_MOTION_SIGMA);
                level.addFreshEntity(dropped);
            }
            inventory.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(chest)
                : 0;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos,
                                  PathComputationType pathType) {
        return false;
    }
}
