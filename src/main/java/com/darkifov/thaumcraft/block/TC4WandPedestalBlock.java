package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.TC4WandPedestalBlockEntity;
import com.darkifov.thaumcraft.item.simple.TC4VisAmuletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** TC4 wand recharge pedestal: one wand/vis-amulet slot, node charging and comparator output. */
public final class TC4WandPedestalBlock extends BaseEntityBlock {
    public TC4WandPedestalBlock(Properties properties) {
        super(properties);
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof WandItem || stack.getItem() instanceof TC4VisAmuletItem);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TC4WandPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,
                ThaumcraftMod.TC4_WAND_PEDESTAL_BLOCK_ENTITY.get(), TC4WandPedestalBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.is(ThaumcraftMod.TC4_WAND_PEDESTAL_FOCUS_ITEM.get())) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof TC4WandPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack stored = pedestal.stored();
        if (held.isEmpty()) {
            if (stored.isEmpty()) return InteractionResult.CONSUME;
            ItemStack removed = pedestal.removeStored();
            if (!player.getInventory().add(removed)) {
                Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + 1.05D, pos.getZ() + .5D, removed);
            }
            return InteractionResult.CONSUME;
        }
        if (!stored.isEmpty() || !accepts(held)) return InteractionResult.CONSUME;
        ItemStack one = held.copy();
        one.setCount(1);
        pedestal.setStored(one);
        if (!player.getAbilities().instabuild) held.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof TC4WandPedestalBlockEntity pedestal
                && !pedestal.stored().isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + 1.05D, pos.getZ() + .5D,
                    pedestal.stored().copy());
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TC4WandPedestalBlockEntity pedestal
                ? pedestal.comparatorSignal() : 0;
    }
}
