package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResearchPointItem extends Item {
    public ResearchPointItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean unlocked = false;

            for (ResearchEntry entry : ResearchRegistry.entries()) {
                if (!PlayerThaumData.hasResearch(player, entry.key()) && requirementsMet(player, entry)) {
                    PlayerThaumData.unlockResearch(player, entry.key());

                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    player.displayClientMessage(
                            Component.literal("Research unlocked: ").withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(entry.title()).withStyle(ChatFormatting.LIGHT_PURPLE)),
                            false
                    );

                    if (player instanceof ServerPlayer serverPlayer) {
                        ThaumcraftNetwork.syncResearch(serverPlayer);
                    }

                    unlocked = true;
                    break;
                }
            }

            if (!unlocked) {
                player.displayClientMessage(Component.literal("No available research to unlock right now. Check requirements.").withStyle(ChatFormatting.GRAY), false);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean requirementsMet(Player player, ResearchEntry entry) {
        for (String requirement : entry.requirements()) {
            if (!PlayerThaumData.hasResearch(player, requirement)) {
                return false;
            }
        }

        return true;
    }
}
