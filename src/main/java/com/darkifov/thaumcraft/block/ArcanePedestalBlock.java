package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

/** Original TC4 one-slot arcane pedestal interaction and comparator behaviour. */
public class ArcanePedestalBlock extends BaseEntityBlock {
    public ArcanePedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePedestalBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal) {
            pedestal.setCustomName(stack.getHoverName().getString());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!pedestal.isEmpty()) {
            ItemStack removed = pedestal.takeStored();
            ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight() * 0.5D,
                    player.getZ(), removed);
            level.addFreshEntity(item);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 1.5F);
            level.updateNeighbourForOutputSignal(pos, this);
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }

        ItemStack one = held.copy();
        one.setCount(1);
        pedestal.setStored(one);
        // TC4 decrements the held stack unconditionally; creative mode is not a special path here.
        held.shrink(1);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F,
                ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 1.6F);
        level.updateNeighbourForOutputSignal(pos, this);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal
                && !pedestal.stored().isEmpty()) {
            ItemStack dropped = pedestal.takeStored();
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, dropped);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(oldState, level, pos, newState, moving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal
                ? TC4InfusionAltarFullClosureParity.pedestalComparator(!pedestal.isEmpty())
                : TC4InfusionAltarFullClosureParity.PEDESTAL_COMPARATOR_EMPTY;
    }
}
