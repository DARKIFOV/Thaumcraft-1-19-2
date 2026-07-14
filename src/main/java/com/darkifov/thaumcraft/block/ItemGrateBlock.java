package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.ItemGrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Port of TC4's item grate / closed hatch pair. Open grate mode lets dropped
 * item entities pass through and accepts automated insertion from above. The
 * closed state blocks items. Right-clicking toggles the state, while redstone
 * closes the grate exactly like the original metadata 5/6 behavior.
 */
public class ItemGrateBlock extends BaseEntityBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape PLATE = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public ItemGrateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(OPEN, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemGrateBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return PLATE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN)
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof ItemEntity) {
            return Shapes.empty();
        }
        return PLATE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            setOpen(level, pos, state, !state.getValue(OPEN), player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbourBlock,
                                BlockPos neighbourPos, boolean moving) {
        boolean shouldOpen = !level.hasNeighborSignal(pos);
        if (state.getValue(OPEN) != shouldOpen) {
            setOpen(level, pos, state, shouldOpen, null);
        }
        super.neighborChanged(state, level, pos, neighbourBlock, neighbourPos, moving);
    }

    private void setOpen(Level level, BlockPos pos, BlockState state, boolean open, @Nullable Player source) {
        if (level.isClientSide || state.getValue(OPEN) == open) {
            return;
        }
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        level.playSound(source, pos,
                open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                SoundSource.BLOCKS, 0.8F, open ? 1.05F : 0.95F);
    }
}
