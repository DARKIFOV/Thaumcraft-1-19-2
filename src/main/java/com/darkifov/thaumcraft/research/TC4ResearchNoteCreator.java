package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Exact ResearchManager.createResearchNoteForPlayer adapter. */
public final class TC4ResearchNoteCreator {
    private static final int INK_COST = 1;

    private TC4ResearchNoteCreator() {}

    public static ItemStack create(ServerPlayer player, ResearchEntry entry) {
        if (player == null || entry == null
                || !ResearchRegistry.originalEntries().contains(entry)
                || !OriginalResearchBridge.canUnlock(player, entry)
                || !TC4ResearchFlagPolicy.canCreateNormalResearchNote(player, entry)
                || entry.aspects().isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = findResearchNote(player, entry.key());
        if (!existing.isEmpty()) {
            OriginalResearchSelection.set(player, entry.key());
            return existing;
        }

        int paperSlot = findPaper(player);
        int toolsSlot = findScribingTools(player);
        if (paperSlot < 0 || toolsSlot < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack paper = player.getInventory().getItem(paperSlot);
        ItemStack tools = player.getInventory().getItem(toolsSlot);
        ItemStack paperSnapshot = paper.copy();
        ItemStack toolsSnapshot = tools.copy();
        if (paper.isEmpty() || !paper.is(Items.PAPER) || !ScribingToolsItem.consumeInk(tools, INK_COST)) {
            return ItemStack.EMPTY;
        }
        paper.shrink(1);
        if (paper.getCount() < 0) {
            player.getInventory().setItem(paperSlot, paperSnapshot);
            player.getInventory().setItem(toolsSlot, toolsSnapshot);
            return ItemStack.EMPTY;
        }

        ItemStack note = new ItemStack(ThaumcraftMod.RESEARCH_NOTE.get());
        ResearchNoteState.initialize(note, entry.key(), player.getRandom().nextLong());
        note.setHoverName(Component.translatable("item.researchnotes.name")
                .append(" - ")
                .append(Component.translatable("tc.research_name." + entry.key()))
                .withStyle(ChatFormatting.DARK_PURPLE));
        if (!player.getInventory().add(note)) {
            player.drop(note, false);
        }
        OriginalResearchSelection.set(player, entry.key());
        player.level.playSound(null, player.blockPosition(), TC4Sounds.event("learn"),
                SoundSource.PLAYERS, 0.75F, 1.0F);
        return note;
    }

    private static ItemStack findResearchNote(ServerPlayer player, String researchKey) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ResearchNoteItem
                    && researchKey.equals(ResearchNoteState.target(stack))) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
