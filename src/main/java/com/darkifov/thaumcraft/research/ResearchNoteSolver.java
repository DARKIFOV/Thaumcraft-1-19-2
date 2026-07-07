package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public final class ResearchNoteSolver {
    private ResearchNoteSolver() {
    }

    public static boolean placeAspect(Player player, ItemStack note, int slot, Aspect aspect) {
        if (player == null || note.isEmpty() || aspect == null) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));

        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }

        if (!PlayerAspectKnowledge.knows(player, aspect)) {
            player.displayClientMessage(Component.literal("You have not discovered that aspect.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!player.getAbilities().instabuild && !ResearchTableInventoryRuntime.hasPoolOrTableBonus(player, aspect)) {
            player.displayClientMessage(Component.literal("You do not have that aspect in your research pool or table bonus aspects.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean placed = ResearchNoteState.place(note, slot, aspect);

        if (!placed) {
            player.displayClientMessage(Component.literal("That hex is missing, fixed, or already filled.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.consumeInkForEdit(player)) {
            ResearchNoteState.clearSlot(note, slot);
            player.displayClientMessage(Component.literal("Your Scribing Tools are out of ink.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!player.getAbilities().instabuild && !ResearchTableInventoryRuntime.consumePoolOrTableBonus(player, aspect)) {
            ResearchNoteState.clearSlot(note, slot);
            player.displayClientMessage(Component.literal("The required aspect was no longer available.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (ResearchNoteState.isSolvedForPlayer(note, player)) {
            ResearchNoteState.markSolved(note);
            player.displayClientMessage(Component.literal("Research note complete.").withStyle(ChatFormatting.GOLD), false);
        } else {
            player.displayClientMessage(Component.literal("Aspect placed: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(aspect.displayName()).withStyle(style -> style.withColor(aspect.textColor()))), false);
        }

        return true;
    }

    public static boolean clearSlot(Player player, ItemStack note, int slot) {
        if (player == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }
        Optional<Aspect> removed = ResearchNoteState.clearSlot(note, slot);

        if (removed.isEmpty()) {
            player.displayClientMessage(Component.literal("That research hex cannot be cleared.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.consumeInkForEdit(player)) {
            ResearchNoteState.place(note, slot, removed.get());
            player.displayClientMessage(Component.literal("Your Scribing Tools are out of ink.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean refund = shouldRefundClearedAspect(player);
        if (!player.getAbilities().instabuild && refund) {
            PlayerAspectKnowledge.addPool(player, removed.get(), 1);
        }

        player.displayClientMessage(Component.literal(refund ? "Aspect returned: " : "Aspect cleared: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(removed.get().displayName()).withStyle(style -> style.withColor(removed.get().textColor()))), false);
        return true;
    }

    public static boolean solve(Player player, ItemStack note) {
        if (player == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));

        if (!ResearchNoteState.hasAllRequired(note)) {
            player.displayClientMessage(Component.literal("The note is missing its original research anchors.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean solved = ResearchNoteState.isSolvedForPlayer(note, player);

        if (!solved) {
            player.displayClientMessage(Component.literal("The theory is not connected through known original TC4 aspect links yet.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ResearchNoteState.markSolved(note);
        player.displayClientMessage(Component.literal("Theory complete. Right-click the note to convert it into research.").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static boolean convertSolvedNote(Player player, ItemStack note) {
        if (player == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (!ResearchNoteState.solved(note)) {
            return false;
        }
        Optional<ResearchEntry> target = OriginalResearchBridge.byKey(ResearchNoteState.target(note));
        if (target.isEmpty()) {
            player.displayClientMessage(Component.literal("This note is not bound to an original research key.").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!OriginalResearchBridge.canUnlock(player, target.get())) {
            player.displayClientMessage(Component.literal("The original research prerequisites are not met.").withStyle(ChatFormatting.RED), true);
            return false;
        }
        OriginalResearchBridge.unlock(player, target.get());
        if (!player.getAbilities().instabuild) {
            note.shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
        }
        player.displayClientMessage(Component.literal("Research completed: " + target.get().key()).withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    private static boolean shouldRefundClearedAspect(Player player) {
        if (player == null || player.getAbilities().instabuild) {
            return true;
        }
        float roll = player.getRandom().nextFloat();
        if (PlayerThaumData.hasResearch(player, "RESEARCHER2")) {
            return roll < 0.50F;
        }
        if (PlayerThaumData.hasResearch(player, "RESEARCHER1")) {
            return roll < 0.25F;
        }
        return false;
    }

    public static Component debugSummary(ItemStack note) {
        Map<Integer, Aspect> slots = ResearchNoteState.slots(note);
        return Component.literal("Research Note: " + slots.size() + " filled hexes, "
                + ResearchNoteState.slotTypes(note).size() + " active hexes, progress "
                + ResearchNoteState.progress(note) + "%");
    }
}
