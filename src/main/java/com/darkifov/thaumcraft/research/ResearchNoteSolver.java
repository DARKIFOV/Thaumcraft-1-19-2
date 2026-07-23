package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
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
            return false;
        }

        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            return false;
        }

        if (!PlayerAspectKnowledge.knows(player, aspect)) {
            return false;
        }

        // Original GuiResearchTable accepted every existing empty type-0 hex.
        // Aspect compatibility is evaluated by the completion graph, not by
        // placement admission. The server still rejects inactive, occupied or
        // anchor slots so forged packets cannot overwrite note state.
        if (!ResearchNoteState.canPlaceAspect(note, slot, aspect)) {
            return false;
        }

        // TC4 evaluates the RESEARCHER2 10% free-placement roll before it
        // asks whether the player/table currently owns the aspect.  The old
        // port checked the pool first, making Mastery unable to rescue a zero-
        // stock placement even when the original roll succeeded.
        boolean freePlacement = shouldPreservePlacedAspect(player);
        if (TC4ResearchEfficiencyParity.placementNeedsAspectSource(freePlacement)
                && !ResearchTableInventoryRuntime.hasPoolOrTableBonus(player, aspect)) {
            return false;
        }

        boolean debitAspect = TC4ResearchEfficiencyParity.placementNeedsAspectSource(freePlacement);
        Optional<ResearchTableInventoryRuntime.ResearchNotePlacementDebit> debit =
                ResearchTableInventoryRuntime.debitResearchNotePlacementAtomically(
                        player, aspect, debitAspect);
        if (debit.isEmpty()) {
            return false;
        }

        boolean placed = ResearchNoteState.place(note, slot, aspect);
        if (!placed) {
            ResearchTableInventoryRuntime.rollbackResearchNotePlacementDebit(player, debit.get());
            return false;
        }

        if (ResearchNoteState.isSolvedForPlayer(note, player)) {
            ResearchNoteState.markSolved(note);
        }

        return true;
    }

    public static boolean clearSlot(Player player, ItemStack note, int slot) {
        if (player == null) {
            return false;
        }
        return clearSlotWithRoll(player, note, slot, player.getRandom().nextFloat());
    }

    /** Deterministic seam used by GameTests; production delegates with the real player RNG. */
    public static boolean clearSlotWithRoll(
            Player player, ItemStack note, int slot, float refundRoll) {
        if (player == null || note == null || note.isEmpty()) {
            return false;
        }
        if (!ResearchTableInventoryRuntime.hasOpenResearchTable(player)
                || ResearchTableInventoryRuntime.findOpenTableResearchNote(player)
                        .filter(open -> open == note).isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (ResearchNoteState.solved(note)) {
            return false;
        }
        Optional<Aspect> clearable = ResearchNoteState.clearableAspect(note, slot);
        if (clearable.isEmpty()) {
            return false;
        }
        if (!ResearchTableInventoryRuntime.checkInkForEdit(player)) {
            return false;
        }

        boolean refund = shouldRefundClearedAspect(player, refundRoll);
        Optional<ResearchTableInventoryRuntime.ResearchNoteClearDebit> debit =
                ResearchTableInventoryRuntime.debitResearchNoteClearAtomically(
                        player, note, slot);
        if (debit.isEmpty()) {
            return false;
        }

        Aspect removed = debit.get().aspect();
        if (refund) {
            PlayerAspectKnowledge.addPool(player, removed, 1);
        }
        return true;
    }

    public static boolean solve(Player player, ItemStack note) {
        if (player == null || note == null || note.isEmpty()) {
            return false;
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (ResearchNoteState.solved(note)) {
            return false;
        }

        Optional<ResearchTableInventoryRuntime.ResearchNoteCompletionSnapshot> snapshot =
                ResearchTableInventoryRuntime.beginResearchNoteCompletion(player, note);
        if (snapshot.isEmpty()) {
            return false;
        }
        if (!ResearchNoteState.hasAllRequired(note)) {
            return false;
        }
        if (!ResearchNoteState.isSolvedForPlayer(note, player)) {
            return false;
        }
        if (!ResearchTableInventoryRuntime.commitResearchNoteCompletion(player, snapshot.get())) {
            return false;
        }
        return true;
    }

    public record SolvedNoteConversionSnapshot(
            ItemStack note, CompoundTag noteTagBefore, int countBefore, String targetBefore) {
    }

    public static Optional<SolvedNoteConversionSnapshot> beginSolvedNoteConversion(
            Player player, ItemStack note) {
        if (player == null || note == null || note.isEmpty()) {
            return Optional.empty();
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (!ResearchNoteState.solved(note)) {
            return Optional.empty();
        }
        String targetKey = ResearchNoteState.target(note);
        Optional<ResearchEntry> target = OriginalResearchBridge.byKey(targetKey);
        if (target.isEmpty() || !OriginalResearchBridge.canUnlock(player, target.get())) {
            return Optional.empty();
        }
        return Optional.of(new SolvedNoteConversionSnapshot(
                note, note.getTag() == null ? null : note.getTag().copy(),
                note.getCount(), targetKey));
    }

    public static boolean commitSolvedNoteConversion(
            Player player, SolvedNoteConversionSnapshot snapshot) {
        if (player == null || snapshot == null || snapshot.note() == null
                || snapshot.note().isEmpty() || snapshot.note().getCount() != snapshot.countBefore()) {
            return false;
        }
        CompoundTag current = snapshot.note().getTag();
        boolean exactSnapshot = snapshot.noteTagBefore() == null
                ? current == null
                : snapshot.noteTagBefore().equals(current);
        if (!exactSnapshot || !snapshot.targetBefore().equals(ResearchNoteState.target(snapshot.note()))
                || !ResearchNoteState.solved(snapshot.note())) {
            return false;
        }
        Optional<ResearchEntry> target = OriginalResearchBridge.byKey(snapshot.targetBefore());
        if (target.isEmpty() || !OriginalResearchBridge.canUnlock(player, target.get())) {
            return false;
        }
        if (!OriginalResearchBridge.unlock(player, target.get())) {
            return false;
        }
        OriginalResearchBridge.unlockEligibleSiblings(player, target.get());

        // Original ItemResearchNotes decremented stackSize unconditionally,
        // including creative mode. Do not retain a port-only instabuild bypass.
        snapshot.note().shrink(TC4ResearchNoteCompletionParity.completedDiscoveryConsumeCount());
        player.level.playSound(null, player.blockPosition(), TC4Sounds.event("learn"),
                SoundSource.PLAYERS, 0.75F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
            ThaumcraftNetwork.syncAspectKnowledge(serverPlayer);
        }
        return true;
    }

    public static boolean convertSolvedNote(Player player, ItemStack note) {
        Optional<SolvedNoteConversionSnapshot> snapshot = beginSolvedNoteConversion(player, note);
        if (snapshot.isEmpty()) {
            if (player != null && note != null && !note.isEmpty() && ResearchNoteState.solved(note)) {
                player.displayClientMessage(Component.translatable("tc.researcherror"), false);
            }
            return false;
        }
        return commitSolvedNoteConversion(player, snapshot.get());
    }

    private static boolean shouldPreservePlacedAspect(Player player) {
        return player != null
                && TC4ResearchEfficiencyParity.masteryFreePlacement(
                        PlayerThaumData.hasResearch(player, "RESEARCHER2"),
                        player.getRandom().nextFloat());
    }

    private static boolean shouldRefundClearedAspect(Player player, float roll) {
        if (player == null) {
            return false;
        }
        return TC4ResearchNoteClearParity.shouldRefundClearedAspect(
                PlayerThaumData.hasResearch(player, "RESEARCHER1"),
                PlayerThaumData.hasResearch(player, "RESEARCHER2"),
                player.getAbilities().instabuild,
                roll);
    }

    public static Component debugSummary(ItemStack note) {
        Map<Integer, Aspect> slots = ResearchNoteState.slots(note);
        return Component.translatable("thaumcraft.message.research.debug",
                slots.size(), ResearchNoteState.slotTypes(note).size(), ResearchNoteState.progress(note));
    }
}
