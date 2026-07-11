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

        if (ResearchNoteState.solved(note)) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.already_complete").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }

        if (!PlayerAspectKnowledge.knows(player, aspect)) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.not_discovered").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.hasPoolOrTableBonus(player, aspect)) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.no_pool").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean placed = ResearchNoteState.place(note, slot, aspect);

        if (!placed) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.hex_invalid").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.consumeInkForEdit(player)) {
            ResearchNoteState.clearSlot(note, slot);
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.ink_empty").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.consumePoolOrTableBonus(player, aspect)) {
            ResearchNoteState.clearSlot(note, slot);
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.aspect_unavailable").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (ResearchNoteState.isSolvedForPlayer(note, player)) {
            ResearchNoteState.markSolved(note);
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.note_complete").withStyle(ChatFormatting.GOLD), false);
        } else {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.aspect_placed",
                    Component.translatable("aspect.thaumcraft." + aspect.id()).withStyle(style -> style.withColor(aspect.textColor())))
                    .withStyle(ChatFormatting.GRAY), false);
        }

        return true;
    }

    public static boolean clearSlot(Player player, ItemStack note, int slot) {
        if (player == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (ResearchNoteState.solved(note)) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.already_complete").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            player.displayClientMessage(ResearchTableInventoryRuntime.missingToolsMessage(), false);
            return false;
        }
        Optional<Aspect> removed = ResearchNoteState.clearSlot(note, slot);

        if (removed.isEmpty()) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.hex_cannot_clear").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!ResearchTableInventoryRuntime.consumeInkForEdit(player)) {
            ResearchNoteState.place(note, slot, removed.get());
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.ink_empty").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean refund = shouldRefundClearedAspect(player);
        if (refund) {
            PlayerAspectKnowledge.addPool(player, removed.get(), 1);
        }

        player.displayClientMessage(Component.translatable(refund
                        ? "thaumcraft.message.research.aspect_returned" : "thaumcraft.message.research.aspect_cleared",
                        Component.translatable("aspect.thaumcraft." + removed.get().id())
                                .withStyle(style -> style.withColor(removed.get().textColor())))
                .withStyle(ChatFormatting.GRAY), false);
        return true;
    }

    public static boolean solve(Player player, ItemStack note) {
        if (player == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));

        if (!ResearchNoteState.hasAllRequired(note)) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.anchors_missing").withStyle(ChatFormatting.RED), false);
            return false;
        }

        boolean solved = ResearchNoteState.isSolvedForPlayer(note, player);

        if (!solved) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.not_connected").withStyle(ChatFormatting.RED), false);
            return false;
        }

        ResearchNoteState.markSolved(note);
        player.displayClientMessage(Component.translatable("thaumcraft.message.research.theory_complete").withStyle(ChatFormatting.GOLD), false);
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
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.no_target").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!OriginalResearchBridge.canUnlock(player, target.get())) {
            player.displayClientMessage(Component.translatable("thaumcraft.message.research.prerequisites").withStyle(ChatFormatting.RED), true);
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
        player.displayClientMessage(Component.translatable("thaumcraft.message.research.completed",
                Component.translatable("tc.research_name." + target.get().key())).withStyle(ChatFormatting.GOLD), true);
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
        return Component.translatable("thaumcraft.message.research.debug",
                slots.size(), ResearchNoteState.slotTypes(note).size(), ResearchNoteState.progress(note));
    }
}
