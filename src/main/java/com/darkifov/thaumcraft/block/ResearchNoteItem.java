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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ResearchNoteItem extends Item {
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean unlocked = false;

            for (ResearchEntry entry : ResearchRegistry.entries()) {
                if (!PlayerThaumData.hasResearch(player, entry.key()) && requirementsMet(player, entry)) {
                    unlocked = PlayerThaumData.unlockResearch(player, entry.key());
                    player.displayClientMessage(Component.literal("Research note completed: " + entry.title()).withStyle(ChatFormatting.GOLD), false);

                    if (player instanceof ServerPlayer serverPlayer) {
                        ThaumcraftNetwork.syncResearch(serverPlayer);
                    }

                    break;
                }
            }

            if (!unlocked) {
                player.displayClientMessage(Component.literal("No available research can be completed by this note yet.").withStyle(ChatFormatting.GRAY), false);
            } else if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    private boolean requirementsMet(Player player, ResearchEntry entry) {
        for (String parent : entry.requirements()) {
            if (!PlayerThaumData.hasResearch(player, parent)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click: complete the next available research.").withStyle(ChatFormatting.GRAY));
    }
}
