package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Stage118: TC4-style research start flow. Clicking an available research in
 * the Thaumonomicon now creates a targeted research note from paper + scribing
 * tools ink, instead of instantly completing a rebuild placeholder entry.
 */
public final class TC4ResearchNoteCreator {
    private static final int INK_COST = 1;

    private TC4ResearchNoteCreator() {}

    public static ItemStack create(ServerPlayer player, ResearchEntry entry) {
        if (player == null || entry == null) {
            return ItemStack.EMPTY;
        }
        if (!OriginalResearchBridge.canUnlock(player, entry)) {
            player.displayClientMessage(Component.literal("This research is not available yet.").withStyle(ChatFormatting.RED), false);
            return ItemStack.EMPTY;
        }
        if (hasOpenNote(player, entry.key())) {
            player.displayClientMessage(Component.literal("You already have a research note for ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(entry.title()).withStyle(ChatFormatting.LIGHT_PURPLE)), false);
            return ItemStack.EMPTY;
        }

        boolean creative = player.getAbilities().instabuild;
        int paperSlot = findPaper(player);
        int toolsSlot = findScribingTools(player);
        if (!creative && paperSlot < 0) {
            player.displayClientMessage(Component.literal("You need paper to begin this research.").withStyle(ChatFormatting.RED), false);
            return ItemStack.EMPTY;
        }
        if (!creative && toolsSlot < 0) {
            player.displayClientMessage(Component.literal("You need scribing tools with ink to begin this research.").withStyle(ChatFormatting.RED), false);
            return ItemStack.EMPTY;
        }

        if (!creative) {
            player.getInventory().getItem(paperSlot).shrink(1);
            ScribingToolsItem.consumeInk(player.getInventory().getItem(toolsSlot), INK_COST);
        }

        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        ResearchNoteState.initialize(note, entry.key());
        note.setHoverName(Component.literal("Research Notes - " + entry.title()).withStyle(ChatFormatting.DARK_PURPLE));
        if (!player.getInventory().add(note)) {
            player.drop(note, false);
        }
        OriginalResearchSelection.set(player, entry.key());
        player.displayClientMessage(Component.literal("Research note created: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(entry.title()).withStyle(ChatFormatting.YELLOW)), false);
        return note;
    }

    private static boolean hasOpenNote(ServerPlayer player, String researchKey) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ResearchNoteItem && researchKey.equals(ResearchNoteState.target(stack)) && !ResearchNoteState.solved(stack)) {
                return true;
            }
        }
        return false;
    }

    private static int findPaper(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Items.PAPER)) {
                return i;
            }
        }
        return -1;
    }

    private static int findScribingTools(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ScribingToolsItem && ScribingToolsItem.hasInk(stack)) {
                return i;
            }
        }
        return -1;
    }
}
