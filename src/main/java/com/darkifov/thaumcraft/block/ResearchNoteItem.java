package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.research.OriginalResearchBridge;
import com.darkifov.thaumcraft.research.ResearchEntry;
import com.darkifov.thaumcraft.research.ResearchNoteState;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
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
import java.util.Optional;

public class ResearchNoteItem extends Item {
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ResearchNoteState.initialize(stack, ResearchNoteState.target(stack));

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (ResearchNoteState.solved(stack)) {
                Optional<ResearchEntry> target = OriginalResearchBridge.byKey(ResearchNoteState.target(stack));
                if (target.isPresent() && OriginalResearchBridge.canUnlock(player, target.get())) {
                    OriginalResearchBridge.unlock(player, target.get());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    ThaumcraftNetwork.syncResearch(serverPlayer);
                    return InteractionResultHolder.success(stack);
                }
                player.displayClientMessage(Component.literal("This theory has already been resolved.").withStyle(ChatFormatting.GRAY), true);
            } else {
                ThaumcraftNetwork.openResearchNote(serverPlayer, stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ResearchNoteState.initialize(stack, ResearchNoteState.target(stack));
        String target = ResearchNoteState.target(stack);
        tooltip.add(Component.literal("Target: " + (target.isBlank() ? "unbound" : target)).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Theory progress: " + ResearchNoteState.progress(stack) + " / 100").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Required: " + ResearchNoteState.requiredAspects(stack).size() + " original TC4 aspects").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click: open the research note puzzle.").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("Shift-right-click: legacy shortcut kept compatible; right-click opens it now.").withStyle(ChatFormatting.GRAY));
        if (ResearchNoteState.solved(stack)) {
            tooltip.add(Component.literal("Solved: right-click to convert into completed research.").withStyle(ChatFormatting.GREEN));
        }
    }
}
