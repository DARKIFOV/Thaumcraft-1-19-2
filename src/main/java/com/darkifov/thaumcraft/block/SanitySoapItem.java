package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.config.ThaumcraftConfig;
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

public class SanitySoapItem extends Item {
    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int before = PlayerThaumData.getWarp(player);
            PlayerThaumData.removeWarp(player, ThaumcraftConfig.SANITY_SOAP_WARP_REMOVE.get());
            PlayerThaumData.setWarpEventCooldown(player, ThaumcraftConfig.SANITY_SOAP_COOLDOWN_SECONDS.get() * 20);
            int after = PlayerThaumData.getWarp(player);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.displayClientMessage(Component.literal("Mind cleansed slightly: Warp " + before + " -> " + after + ". Events delayed.").withStyle(ChatFormatting.AQUA), false);

            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
