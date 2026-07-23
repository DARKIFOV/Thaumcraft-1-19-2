package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneSpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/** Functional 1.19.2 adaptation of TC4's Arcane Spa. */
public class ArcaneSpaBlock extends BaseEntityBlock {
    public ArcaneSpaBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneSpaBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ThaumcraftMod.ARCANE_SPA_BLOCK_ENTITY.get(),
                ArcaneSpaBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        FilledContainer filled = inspectFilledContainer(held);
        if (filled != null) {
            // TC4 consumed this interaction even when the tank was full or held another fluid.
            if (!level.isClientSide && spa.fillFromHeldContainer(filled.fluid()) > 0) {
                consumeFilledContainer(player, hand, held, filled.emptyContainer());
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS,
                        0.33F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, spa, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private static FilledContainer inspectFilledContainer(ItemStack held) {
        if (held.isEmpty()) {
            return null;
        }
        ItemStack single = held.copy();
        single.setCount(1);
        LazyOptional<IFluidHandlerItem> capability = single.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem handler = capability.orElse(null);
        if (handler == null) {
            return null;
        }
        FluidStack fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (fluid.isEmpty()) {
            return null;
        }
        FluidStack drained = handler.drain(fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return null;
        }
        ItemStack empty = handler.getContainer().copy();
        empty.setCount(1);
        return new FilledContainer(drained.copy(), empty);
    }

    private static void consumeFilledContainer(Player player, InteractionHand hand,
                                               ItemStack held, ItemStack emptyContainer) {
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        if (!emptyContainer.isEmpty() && !player.getInventory().add(emptyContainer.copy())) {
            player.drop(emptyContainer.copy(), false);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }

    private record FilledContainer(FluidStack fluid, ItemStack emptyContainer) {
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!oldState.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa) {
                ItemStack salts = spa.removeAllBathSalts();
                if (!salts.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
                            pos.getZ() + 0.5D, salts);
                }
            }
            super.onRemove(oldState, level, pos, newState, moving);
        }
    }
}
