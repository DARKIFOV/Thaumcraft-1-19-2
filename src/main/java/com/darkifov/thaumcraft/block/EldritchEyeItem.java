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

public class EldritchEyeItem extends Item {
    public EldritchEyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int warp = PlayerThaumData.getWarp(player);

            if (warp < 12) {
                player.displayClientMessage(Component.literal("The eye remains shut. More Warp is required.").withStyle(ChatFormatting.DARK_PURPLE), false);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            PlayerThaumData.addWarp(player, 2);
            PlayerThaumData.addEldritchAttunement(player, 10);
            PlayerThaumData.unlockResearch(player, "ELDRITCH_WHISPERS");

            if (PlayerThaumData.getEldritchAttunement(player) >= 30) {
                PlayerThaumData.unlockResearch(player, "ELDRITCH_START");
            }

            player.displayClientMessage(Component.literal("The eye opens. Something notices you.").withStyle(ChatFormatting.DARK_PURPLE), false);

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
