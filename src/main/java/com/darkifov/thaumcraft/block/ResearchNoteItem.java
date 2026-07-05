package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.OriginalResearchSelection;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
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
        ResearchNoteState.initialize(stack, "");

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.openResearchNote(serverPlayer, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!level.isClientSide()) {
            ResearchEntry target = OriginalResearchBridge.selectedOrFirstAvailable(player).orElse(null);

            if (target == null) {
                player.displayClientMessage(Component.literal("No selected or available research is currently available.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.success(stack);
            }

            if (OriginalResearchBridge.completeWithAspectCost(player, target)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.fail(stack);
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
        int progress = stack.getOrCreateTag().getInt("TheoryProgress");
        ResearchNoteState.initialize(stack, "");
        tooltip.add(Component.literal("Theory progress: " + ResearchNoteState.progress(stack) + " / 100").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Shift-right-click: open research note puzzle.").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("Target: " + ResearchNoteState.target(stack)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Required: " + ResearchNoteState.requiredAspects(stack).size() + " aspects").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click: complete selected Thaumonomicon research.").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Prepared and improved on a Research Table.").withStyle(ChatFormatting.GRAY));
    }
}
