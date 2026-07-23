package com.darkifov.thaumcraft.research;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ResearchNoteItem;
import com.darkifov.thaumcraft.block.ScribingToolsItem;
import com.darkifov.thaumcraft.menu.ResearchTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Stage163: 1.19.2 adapter for the original TC4 TileResearchTable inventory contract.
 *
 * Original TC4 TileResearchTable has exactly two inventory slots:
 *   slot 0 = IScribeTools
 *   slot 1 = itemResearchNotes with damage < 64
 * and every note edit is gated by ResearchManager.consumeInkFromTable(slot0, ...).
 *
 * Stage165 restores the persistent block-entity/container inventory. The old held-note
 * lookup remains only as a compatibility fallback for packets/screens opened by older
 * test worlds, not as the primary table model.
 */
public final class ResearchTableInventoryRuntime {
    public static final int SLOT_SCRIBING_TOOLS = 0;
    public static final int SLOT_RESEARCH_NOTE = 1;
    public static final int INK_PER_NOTE_CREATE = 1;
    public static final int INK_PER_NOTE_EDIT = 1;

    private ResearchTableInventoryRuntime() {
    }

    public static boolean isScribingTools(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ScribingToolsItem;
    }

    public static boolean isResearchNote(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ResearchNoteItem;
    }

    public static Optional<ItemStack> findScribingTools(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        if (player.containerMenu instanceof ResearchTableMenu menu) {
            ItemStack tableTools = menu.tableStack(SLOT_SCRIBING_TOOLS);
            if (isScribingTools(tableTools)) {
                return Optional.of(tableTools);
            }
            // Stage603-622: when the original research table container is open,
            // slot 0 is authoritative. Do not fall back to carried tools; that was
            // a temporary 1.19.2 adapter drift and allowed editing without the
            // visible TC4 inkwell/scribing-tools slot.
            return Optional.empty();
        }
        ItemStack main = player.getMainHandItem();
        if (isScribingTools(main)) {
            return Optional.of(main);
        }
        ItemStack off = player.getOffhandItem();
        if (isScribingTools(off)) {
            return Optional.of(off);
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isScribingTools(stack)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> findInkedScribingTools(Player player) {
        Optional<ItemStack> stack = findScribingTools(player);
        if (stack.isPresent() && ScribingToolsItem.hasInk(stack.get())) {
            return stack;
        }
        return Optional.empty();
    }


    public static boolean hasOpenResearchTable(Player player) {
        boolean menuOpen = player != null && player.containerMenu instanceof ResearchTableMenu;
        boolean stillValid = menuOpen && ((ResearchTableMenu) player.containerMenu).stillValid(player);
        return TC4ResearchNoteGraphParity.acceptsServerTableContext(menuOpen, stillValid);
    }

    public static Optional<ItemStack> findOpenTableResearchNote(Player player) {
        if (hasOpenResearchTable(player) && player.containerMenu instanceof ResearchTableMenu menu) {
            ItemStack tableNote = menu.tableStack(SLOT_RESEARCH_NOTE);
            return isResearchNote(tableNote) ? Optional.of(tableNote) : Optional.empty();
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> findOpenTableScribingTools(Player player) {
        if (hasOpenResearchTable(player) && player.containerMenu instanceof ResearchTableMenu menu) {
            ItemStack tableTools = menu.tableStack(SLOT_SCRIBING_TOOLS);
            return isScribingTools(tableTools) ? Optional.of(tableTools) : Optional.empty();
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> findHeldResearchNote(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        if (player.containerMenu instanceof ResearchTableMenu menu) {
            ItemStack tableNote = menu.tableStack(SLOT_RESEARCH_NOTE);
            if (isResearchNote(tableNote)) {
                return Optional.of(tableNote);
            }
            // Stage603-622: while the table GUI is open, slot 1 is the original
            // note slot. Held notes are only a compatibility path outside the
            // table screen, not a parallel duplicate workflow.
            return Optional.empty();
        }
        ItemStack main = player.getMainHandItem();
        if (isResearchNote(main)) {
            return Optional.of(main);
        }
        ItemStack off = player.getOffhandItem();
        if (isResearchNote(off)) {
            return Optional.of(off);
        }
        return Optional.empty();
    }

    public static boolean hasEditableTableSetup(Player player) {
        return findInkedScribingTools(player).isPresent() && findHeldResearchNote(player).isPresent();
    }

    public static boolean checkInkForCreate(Player player) {
        return player != null && findInkedScribingTools(player).isPresent();
    }

    public static boolean checkInkForEdit(Player player) {
        return player != null && findInkedScribingTools(player).isPresent();
    }

    public static boolean consumeInkForCreate(Player player) {
        return consumeInk(player, INK_PER_NOTE_CREATE);
    }

    public static boolean consumeInkForEdit(Player player) {
        return consumeInk(player, INK_PER_NOTE_EDIT);
    }


    public static boolean hasPoolOrTableBonus(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        if (PlayerAspectKnowledge.pool(player).contains(aspect, 1)) {
            return true;
        }
        return tableBonusAmount(player, aspect) > 0;
    }

    public static int tableBonusAmount(Player player, Aspect aspect) {
        if (player != null && aspect != null && player.containerMenu instanceof ResearchTableMenu menu) {
            return menu.tableBonusAmount(aspect);
        }
        return 0;
    }

    public static AspectList tableBonusAspects(Player player) {
        if (player != null && player.containerMenu instanceof ResearchTableMenu menu) {
            return menu.tableBonusAspects();
        }
        return new AspectList();
    }

    public static boolean consumePoolOrTableBonus(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        if (PlayerAspectKnowledge.pool(player).contains(aspect, 1)) {
            return PlayerAspectKnowledge.consumePool(player, aspect, 1);
        }
        if (player.containerMenu instanceof ResearchTableMenu menu && menu.consumeBonusAspect(aspect)) {
            return true;
        }
        return false;
    }

    /**
     * Snapshot of the resources committed for one research-note placement.
     * The solver keeps it until the note mutation succeeds and can restore both
     * ink and aspect source if the final write is rejected.
     */
    public record ResearchNotePlacementDebit(
            Aspect aspect, int inkDamageBefore, int poolBefore, int bonusBefore,
            boolean aspectDebited, boolean creative) {
    }

    public static Optional<ResearchNotePlacementDebit> debitResearchNotePlacementAtomically(
            Player player, Aspect aspect, boolean debitAspect) {
        if (player == null || aspect == null || !hasOpenResearchTable(player)) {
            return Optional.empty();
        }
        if (!checkInkForEdit(player)) {
            return Optional.empty();
        }
        if (debitAspect && !hasPoolOrTableBonus(player, aspect)) {
            return Optional.empty();
        }

        boolean creative = player.getAbilities().instabuild;
        int inkDamageBefore = currentOpenTableInkDamage(player);
        int poolBefore = PlayerAspectKnowledge.pool(player).get(aspect);
        int bonusBefore = tableBonusAmount(player, aspect);
        ResearchNotePlacementDebit debit = new ResearchNotePlacementDebit(
                aspect, inkDamageBefore, poolBefore, bonusBefore, debitAspect, creative);

        if (!consumeInkForEdit(player)) {
            return Optional.empty();
        }
        if (debitAspect && !consumePoolOrTableBonus(player, aspect)) {
            rollbackResearchNotePlacementDebit(player, debit);
            return Optional.empty();
        }
        return Optional.of(debit);
    }

    public static void rollbackResearchNotePlacementDebit(
            Player player, ResearchNotePlacementDebit debit) {
        if (player == null || debit == null) {
            return;
        }
        restoreOpenTableInkDamage(player, debit.inkDamageBefore());
        if (debit.aspectDebited()) {
            restoreAspectSourceAmounts(
                    player, debit.aspect(), debit.poolBefore(), debit.bonusBefore());
        }
        markOpenTableChanged(player);
    }

    /**
     * Immutable preflight for one server-side Research Note completion. No
     * resources are debited here: TC4 consumed ink on the edit that made the
     * graph complete, not on the completion check itself. The exact NBT snapshot
     * prevents a stale solve packet from completing a different/replaced note.
     */
    public record ResearchNoteCompletionSnapshot(
            ItemStack note, CompoundTag noteTagBefore, String targetBefore) {
    }

    public static Optional<ResearchNoteCompletionSnapshot> beginResearchNoteCompletion(
            Player player, ItemStack note) {
        if (player == null || note == null || note.isEmpty() || !hasOpenResearchTable(player)) {
            return Optional.empty();
        }
        Optional<ItemStack> openNote = findOpenTableResearchNote(player);
        boolean sameIdentity = openNote.isPresent() && openNote.get() == note;
        if (!TC4ResearchNoteCompletionParity.acceptsCompletionContext(
                player.containerMenu instanceof ResearchTableMenu,
                ((ResearchTableMenu) player.containerMenu).stillValid(player),
                sameIdentity)) {
            return Optional.empty();
        }
        ResearchNoteState.initialize(note, ResearchNoteState.target(note));
        if (ResearchNoteState.solved(note)) {
            return Optional.empty();
        }
        return Optional.of(new ResearchNoteCompletionSnapshot(
                note, note.getTag() == null ? null : note.getTag().copy(),
                ResearchNoteState.target(note)));
    }

    public static boolean commitResearchNoteCompletion(
            Player player, ResearchNoteCompletionSnapshot snapshot) {
        if (player == null || snapshot == null || snapshot.note() == null
                || snapshot.note().isEmpty() || !hasOpenResearchTable(player)) {
            return false;
        }
        Optional<ItemStack> openNote = findOpenTableResearchNote(player);
        boolean sameIdentity = openNote.isPresent() && openNote.get() == snapshot.note();
        CompoundTag current = snapshot.note().getTag();
        boolean exactSnapshot = snapshot.noteTagBefore() == null
                ? current == null
                : snapshot.noteTagBefore().equals(current);
        boolean targetStable = snapshot.targetBefore().equals(ResearchNoteState.target(snapshot.note()));
        boolean graphSolved = targetStable
                && ResearchNoteState.isSolvedForPlayer(snapshot.note(), player);
        if (!TC4ResearchNoteCompletionParity.acceptsCompletionContext(
                    player.containerMenu instanceof ResearchTableMenu,
                    ((ResearchTableMenu) player.containerMenu).stillValid(player),
                    sameIdentity)
                || !TC4ResearchNoteCompletionParity.canCommitCompletion(
                    ResearchNoteState.solved(snapshot.note()), exactSnapshot, graphSolved)) {
            return false;
        }

        CompoundTag rollback = current == null ? null : current.copy();
        try {
            ResearchNoteState.markSolved(snapshot.note());
            if (!ResearchNoteState.solved(snapshot.note())) {
                snapshot.note().setTag(rollback == null ? null : rollback.copy());
                return false;
            }
            return true;
        } catch (RuntimeException failure) {
            snapshot.note().setTag(rollback == null ? null : rollback.copy());
            return false;
        }
    }

    /**
     * Snapshot for one accepted Research Note clear. The note NBT, ink damage
     * and player pool are all retained so any later adapter failure can restore
     * the complete pre-edit state instead of leaving a half-cleared hex.
     */
    public record ResearchNoteClearDebit(
            ItemStack note, Aspect aspect, CompoundTag noteTagBefore,
            int inkDamageBefore, int poolBefore, boolean creative) {
    }

    public static Optional<ResearchNoteClearDebit> debitResearchNoteClearAtomically(
            Player player, ItemStack note, int slot) {
        if (player == null || note == null || note.isEmpty() || !hasOpenResearchTable(player)) {
            return Optional.empty();
        }
        Optional<ItemStack> openNote = findOpenTableResearchNote(player);
        if (openNote.isEmpty() || openNote.get() != note) {
            return Optional.empty();
        }
        Optional<Aspect> clearable = ResearchNoteState.clearableAspect(note, slot);
        if (clearable.isEmpty() || !checkInkForEdit(player)) {
            return Optional.empty();
        }

        boolean creative = player.getAbilities().instabuild;
        CompoundTag noteTagBefore = note.getTag() == null ? null : note.getTag().copy();
        int inkDamageBefore = currentOpenTableInkDamage(player);
        int poolBefore = PlayerAspectKnowledge.pool(player).get(clearable.get());
        ResearchNoteClearDebit debit = new ResearchNoteClearDebit(
                note, clearable.get(), noteTagBefore, inkDamageBefore, poolBefore, creative);

        if (!consumeInkForEdit(player)) {
            return Optional.empty();
        }
        Optional<Aspect> removed = ResearchNoteState.clearSlot(note, slot);
        if (removed.isEmpty() || removed.get() != clearable.get()) {
            rollbackResearchNoteClearDebit(player, debit);
            return Optional.empty();
        }
        return Optional.of(debit);
    }

    public static void rollbackResearchNoteClearDebit(
            Player player, ResearchNoteClearDebit debit) {
        if (player == null || debit == null) {
            return;
        }
        debit.note().setTag(debit.noteTagBefore() == null ? null : debit.noteTagBefore().copy());
        restoreOpenTableInkDamage(player, debit.inkDamageBefore());
        PlayerAspectKnowledge.setPoolAmountForTransaction(
                player, debit.aspect(), debit.poolBefore());
        markOpenTableChanged(player);
    }

    private static int currentOpenTableInkDamage(Player player) {
        return findOpenTableScribingTools(player)
                .map(ItemStack::getDamageValue)
                .orElse(-1);
    }

    private static void restoreOpenTableInkDamage(Player player, int damage) {
        if (damage < 0) {
            return;
        }
        findOpenTableScribingTools(player).ifPresent(stack -> stack.setDamageValue(damage));
    }

    public static boolean consumeAspectPairAtomically(Player player, Aspect first, Aspect second) {
        if (player == null || first == null || second == null) {
            return false;
        }

        int firstPoolBefore = PlayerAspectKnowledge.pool(player).get(first);
        int firstBonusBefore = tableBonusAmount(player, first);
        boolean sameAspect = first == second;
        int secondPoolBefore = sameAspect ? firstPoolBefore : PlayerAspectKnowledge.pool(player).get(second);
        int secondBonusBefore = sameAspect ? firstBonusBefore : tableBonusAmount(player, second);

        var plan = TC4ResearchMasteryCombinationParity.plan(
                firstPoolBefore, firstBonusBefore, secondPoolBefore, secondBonusBefore, sameAspect);
        if (plan.isEmpty()) {
            return false;
        }

        TC4ResearchMasteryCombinationParity.PairDebitPlan debit = plan.get();
        boolean committed = consumePlanned(player, first,
                debit.firstPoolTotal(), debit.firstBonusTotal());
        if (committed && !sameAspect) {
            committed = consumePlanned(player, second,
                    debit.secondPoolTotal(), debit.secondBonusTotal());
        }
        if (committed) {
            return true;
        }

        // Defensive rollback: packet/menu state should not change between the
        // preflight and commit on the server thread, but a failed adapter call
        // must never destroy only the first component.
        restoreAspectSourceAmounts(player, first, firstPoolBefore, firstBonusBefore);
        if (!sameAspect) {
            restoreAspectSourceAmounts(player, second, secondPoolBefore, secondBonusBefore);
        }
        return false;
    }

    private static boolean consumePlanned(Player player, Aspect aspect, int poolAmount, int bonusAmount) {
        if (poolAmount > 0 && !PlayerAspectKnowledge.consumePool(player, aspect, poolAmount)) {
            return false;
        }
        if (bonusAmount > 0) {
            if (!(player.containerMenu instanceof ResearchTableMenu menu)
                    || !menu.consumeBonusAspect(aspect, bonusAmount)) {
                return false;
            }
        }
        return true;
    }

    private static void restoreAspectSourceAmounts(
            Player player, Aspect aspect, int playerPoolAmount, int tableBonusAmount) {
        PlayerAspectKnowledge.setPoolAmountForTransaction(player, aspect, playerPoolAmount);
        if (player.containerMenu instanceof ResearchTableMenu menu) {
            menu.setBonusAmountForTransaction(aspect, tableBonusAmount);
        }
    }

    private static boolean consumeInk(Player player, int amount) {
        if (player == null) {
            return false;
        }
        if (player.containerMenu instanceof ResearchTableMenu menu) {
            return menu.consumeInk(amount, player);
        }
        Optional<ItemStack> stack = findInkedScribingTools(player);
        return stack.isPresent() && ScribingToolsItem.consumeInk(stack.get(), amount);
    }

    public static void markOpenTableChanged(Player player) {
        if (player != null && player.containerMenu instanceof ResearchTableMenu menu) {
            menu.markTableChanged();
        }
    }

    public static Component missingToolsMessage() {
        return Component.literal("Research Table slot 0 needs Scribing Tools with ink.").withStyle(ChatFormatting.RED);
    }

    public static Component missingNoteMessage() {
        return Component.literal("Research Table slot 1 needs a Research Note.").withStyle(ChatFormatting.RED);
    }

    public static Component inventorySummary(Player player) {
        Optional<ItemStack> tools = findScribingTools(player);
        Optional<ItemStack> note = findHeldResearchNote(player);
        String ink = tools.map(stack -> String.valueOf(ScribingToolsItem.inkLeft(stack))).orElse("-");
        boolean realTable = player != null && player.containerMenu instanceof ResearchTableMenu;
        return Component.literal("TC4 table slots" + (realTable ? "" : " (compat fallback)") + ": [0] scribing tools ink=" + ink + ", [1] note=" + note.isPresent())
                .withStyle(ChatFormatting.DARK_AQUA);
    }

    /**
     * Stage169 replacement: use the strict table-local bonus runtime. This method
     * remains only for older callers/tests; it no longer invents capped fake aspects
     * or merges anything into player data.
     */
    public static AspectList recalculateNearbyBonusAspects(Level level, BlockPos pos) {
        AspectList bonus = new AspectList();
        ResearchTableBonusRuntime.recalculateInto(level, pos, bonus);
        return bonus;
    }

    /**
     * Deprecated Stage163 compatibility method. Stage169 restores original TC4
     * ownership: bonus aspects stay on ResearchTableBlockEntity.bonusAspects and
     * are consumed by placement instead of being merged into the player pool.
     */
    @Deprecated
    public static int mergeBonusAspectsIntoPlayer(Player player, Level level, BlockPos pos) {
        return 0;
    }
}
