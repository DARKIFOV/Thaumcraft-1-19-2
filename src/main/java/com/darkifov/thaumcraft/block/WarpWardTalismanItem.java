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

public class WarpWardTalismanItem extends Item {
    public WarpWardTalismanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (!PlayerThaumData.hasResearch(player, "WARP_WARDING")) {
                player.displayClientMessage(Component.literal("Research locked: WARP_WARDING").withStyle(ChatFormatting.RED), false);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            int seconds = ThaumcraftConfig.WARP_WARD_SECONDS.get();
            PlayerThaumData.addWarpWard(player, seconds * 20);
            player.displayClientMessage(Component.literal("Warp Ward active for " + seconds + " seconds.").withStyle(ChatFormatting.AQUA), false);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
