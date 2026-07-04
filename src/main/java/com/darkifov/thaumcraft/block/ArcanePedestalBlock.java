package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof ArcanePedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }

        if (held.isEmpty()) {
            if (pedestal.isEmpty()) {
                player.displayClientMessage(Component.literal("Arcane Pedestal is empty.").withStyle(ChatFormatting.GRAY), false);
                return InteractionResult.CONSUME;
            }

            ItemStack taken = pedestal.takeStored();

            if (!player.getInventory().add(taken)) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, taken);
            }

            player.displayClientMessage(Component.literal("Removed ").append(taken.getHoverName()).append(Component.literal(" from pedestal.")), false);
            return InteractionResult.CONSUME;
        }

        if (!pedestal.isEmpty()) {
            player.displayClientMessage(Component.literal("Arcane Pedestal already contains ").append(pedestal.stored().getHoverName()), false);
            return InteractionResult.CONSUME;
        }

        ItemStack one = held.copy();
        one.setCount(1);
        pedestal.setStored(one);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        player.displayClientMessage(Component.literal("Placed ").append(one.getHoverName()).append(Component.literal(" on pedestal.")), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof ArcanePedestalBlockEntity pedestal && !pedestal.stored().isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, pedestal.stored().copy());
            }
        }

        super.onRemove(oldState, level, pos, newState, moving);
    }
}
