package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.TransvectorInterfaceBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class TransvectorInterfaceBlock extends BaseEntityBlock {
    public TransvectorInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransvectorInterfaceBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof TransvectorInterfaceBlockEntity transvector)) {
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof TransvectorBinderItem) {
            BlockPos target = TransvectorBinderItem.getTarget(held);

            if (target == null) {
                player.displayClientMessage(Component.literal("Binder не имеет цели. Сначала ПКМ Binder по блоку.").withStyle(ChatFormatting.RED), false);
            } else if (target.equals(pos)) {
                player.displayClientMessage(Component.literal("Нельзя привязать Transvector Interface к самому себе.").withStyle(ChatFormatting.RED), false);
            } else {
                transvector.bind(target);
                player.displayClientMessage(Component.literal("Transvector Interface привязан к: " + target.toShortString()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }

            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            transvector.inspectTarget(player);
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, (MenuProvider) transvector, buffer -> buffer.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }
}
