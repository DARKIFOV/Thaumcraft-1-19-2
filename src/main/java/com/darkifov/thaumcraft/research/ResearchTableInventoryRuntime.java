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
        return player != null && player.containerMenu instanceof ResearchTableMenu;
    }

    public static Optional<ItemStack> findOpenTableResearchNote(Player player) {
        if (player != null && player.containerMenu instanceof ResearchTableMenu menu) {
            ItemStack tableNote = menu.tableStack(SLOT_RESEARCH_NOTE);
            return isResearchNote(tableNote) ? Optional.of(tableNote) : Optional.empty();
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> findOpenTableScribingTools(Player player) {
        if (player != null && player.containerMenu instanceof ResearchTableMenu menu) {
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
        return player != null && (player.getAbilities().instabuild || findInkedScribingTools(player).isPresent());
    }

    public static boolean checkInkForEdit(Player player) {
        return player != null && (player.getAbilities().instabuild || findInkedScribingTools(player).isPresent());
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

    private static boolean consumeInk(Player player, int amount) {
        if (player == null) {
            return false;
        }
        if (player.getAbilities().instabuild) {
            return true;
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
