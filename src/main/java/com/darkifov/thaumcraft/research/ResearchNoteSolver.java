package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ResearchNoteSolver {
    private ResearchNoteSolver() {
    }

    public static boolean placeAspect(Player player, ItemStack note, int slot, Aspect aspect) {
        if (player == null || note.isEmpty() || aspect == null) {
            return false;
        }

        if (!PlayerAspectKnowledge.knows(player, aspect)) {
            player.displayClientMessage(Component.literal("You have not discovered that aspect.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!player.getAbilities().instabuild && !PlayerAspectKnowledge.pool(player).contains(aspect, 1)) {
            player.displayClientMessage(Component.literal("You do not have that aspect in your research pool.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean placed = ResearchNoteState.place(note, slot, aspect);

        if (!placed) {
            player.displayClientMessage(Component.literal("That position is locked, already filled or not connected to a compatible neighbouring aspect.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!player.getAbilities().instabuild) {
            PlayerAspectKnowledge.consumePool(player, aspect, 1);
        }

        player.displayClientMessage(Component.literal("Aspect placed: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(aspect.displayName()).withStyle(style -> style.withColor(aspect.textColor()))), false);

        return true;
    }

    public static boolean clearSlot(Player player, ItemStack note, int slot) {
        if (player == null || note.isEmpty()) {
            return false;
        }

        java.util.Optional<Aspect> removed = ResearchNoteState.clearSlot(note, slot);

        if (removed.isEmpty()) {
            player.displayClientMessage(Component.literal("That research position cannot be cleared.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!player.getAbilities().instabuild) {
            PlayerAspectKnowledge.addPool(player, removed.get(), 1);
        }

        player.displayClientMessage(Component.literal("Aspect returned: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(removed.get().displayName()).withStyle(style -> style.withColor(removed.get().textColor()))), false);
        return true;
    }

    public static boolean solve(Player player, ItemStack note) {
        if (player == null || note.isEmpty()) {
            return false;
        }

        if (!ResearchNoteState.hasAllRequired(note)) {
            player.displayClientMessage(Component.literal("The note is missing required aspects.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean solved = ResearchNoteState.isSolved(note);

        if (!solved) {
            player.displayClientMessage(Component.literal("The theory is not connected yet.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ResearchNoteState.markSolved(note);
        player.displayClientMessage(Component.literal("Theory complete. The note can be converted into research.").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static Component debugSummary(ItemStack note) {
        Map<Integer, Aspect> slots = ResearchNoteState.slots(note);
        return Component.literal("Research Note: " + slots.size() + " filled slots, progress " + ResearchNoteState.progress(note) + "%");
    }
}
