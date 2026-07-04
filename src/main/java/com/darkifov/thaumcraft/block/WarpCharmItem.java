package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WarpCharmItem extends Item {
    public WarpCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (!PlayerThaumData.hasResearch(player, "WARP")) {
                player.displayClientMessage(Component.literal("Research locked: WARP").withStyle(ChatFormatting.RED), false);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            int before = PlayerThaumData.getWarp(player);
            PlayerThaumData.removeWarp(player, 4);
            int after = PlayerThaumData.getWarp(player);

            if (after < before) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                player.displayClientMessage(Component.literal("Warp reduced: " + before + " -> " + after).withStyle(ChatFormatting.AQUA), false);

                if (player instanceof ServerPlayer serverPlayer) {
                    ThaumcraftNetwork.syncResearch(serverPlayer);
                }
            } else {
                player.displayClientMessage(Component.literal("You have no Warp to cleanse.").withStyle(ChatFormatting.GRAY), false);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
