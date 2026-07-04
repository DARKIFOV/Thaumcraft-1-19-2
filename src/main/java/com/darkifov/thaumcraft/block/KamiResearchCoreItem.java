package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class KamiResearchCoreItem extends Item {
    public KamiResearchCoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int warp = PlayerThaumData.getWarp(player);
            boolean eldritch = PlayerThaumData.hasResearch(player, "ELDRITCH_ARENA")
                    || PlayerThaumData.hasResearch(player, "ELDRITCH_PORTAL")
                    || player.getAbilities().instabuild;

            if (!eldritch) {
                player.displayClientMessage(Component.literal("KAMI Core is dormant. Complete Eldritch progression first.").withStyle(ChatFormatting.DARK_PURPLE), false);
            } else if (warp < 10 && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("KAMI Core resists you. Required warp: 10. Current: " + warp).withStyle(ChatFormatting.DARK_PURPLE), false);
            } else {
                PlayerThaumData.unlockResearch(player, "KAMI_COMPLETION_GATE");
                PlayerThaumData.unlockResearch(player, "ICHOR_GEAR_REALIZATION");
                PlayerThaumData.addWarp(player, 2);
                player.giveExperiencePoints(250);
                player.displayClientMessage(Component.literal("KAMI Core awakens. KAMI progression unlocked. Warp +2.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Endgame KAMI unlock core.").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Requires Eldritch progression and warp 10.").withStyle(ChatFormatting.GRAY));
    }
}
