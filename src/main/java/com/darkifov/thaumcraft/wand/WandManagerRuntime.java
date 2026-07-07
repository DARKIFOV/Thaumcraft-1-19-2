package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stage188 strict adapter for original TC4 WandManager.changeFocus.
 *
 * Original source of truth:
 * - thaumcraft.common.items.wands.WandManager.changeFocus(ItemStack, World, EntityPlayer, String)
 * - thaumcraft.common.lib.network.misc.PacketFocusChangeToServer
 * - thaumcraft.client.lib.events.KeyHandler keyF / shift+F REMOVE
 *
 * TC4 1.7.10 scans Baubles focus pouches first, then inventory foci, then
 * inventory pouches. Forge 1.19.2 has no Baubles dependency in this port, so
 * the off-hand pouch is the explicit bauble-slot adapter and is scanned before
 * the vanilla inventory. Focus ordering is still TreeMap/higherKey/wrap like
 * the original, and the sound remains thaumcraft:cameraticks with pitch 0.9
 * for REMOVE and 1.0 for focus changes.
 */
public final class WandManagerRuntime {
    public static final String REMOVE = "REMOVE";

    private WandManagerRuntime() {}

    public static String currentFocusSortKey(ItemStack wandStack) {
        ItemStack focusStack = WandFocusRuntime.getFocusStack(wandStack);
        if (!focusStack.isEmpty()) {
            return FocusPouchItem.sortingHelper(focusStack);
        }
        WandFocusType type = WandFocusRuntime.getFocus(wandStack);
        return type == null ? "" : FocusPouchItem.sortingHelper(WandFocusRuntime.focusStack(type));
    }

    public static boolean changeFocus(ItemStack wandStack, Level level, Player player, String requestedFocus) {
        if (!(wandStack.getItem() instanceof WandItem) || WandComponentData.isSceptre(wandStack)) {
            return false;
        }

        ItemStack current = WandFocusRuntime.getFocusStack(wandStack);
        boolean remove = REMOVE.equals(requestedFocus);
        if (remove) {
            boolean changed = removeCurrentFocus(wandStack, current, player);
            playCameraTicks(level, player, 0.9F);
            return changed;
        }

        TreeMap<String, FocusLocation> foci = collectAvailableFoci(player);
        if (foci.isEmpty()) {
            if (!current.isEmpty()) {
                removeCurrentFocus(wandStack, current, player);
                playCameraTicks(level, player, 0.9F);
                return true;
            }
            player.displayClientMessage(Component.literal("No wand foci found.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        String key = requestedFocus == null || requestedFocus.isBlank() ? currentFocusSortKey(wandStack) : requestedFocus;
        Map.Entry<String, FocusLocation> selected = foci.ceilingEntry(key);
        if (selected != null && selected.getKey().equals(currentFocusSortKey(wandStack))) {
            selected = foci.higherEntry(selected.getKey());
        }
        if (selected == null) {
            selected = foci.firstEntry();
        }

        if (selected == null) {
            return false;
        }

        ItemStack next = selected.getValue().take(player);
        if (next.isEmpty() || !(next.getItem() instanceof WandFocusItem focusItem)) {
            return false;
        }
        if (!current.isEmpty()) {
            storeFocus(player, current);
        }
        WandFocusRuntime.setFocusStack(wandStack, next);
        playCameraTicks(level, player, 1.0F);
        player.displayClientMessage(Component.literal("Equipped " + focusItem.focusType().displayName() + ".").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }

    private static boolean removeCurrentFocus(ItemStack wandStack, ItemStack current, Player player) {
        WandFocusType currentType = WandFocusRuntime.getFocus(wandStack);
        if (current.isEmpty() && currentType != null) {
            current = WandFocusRuntime.focusStack(currentType);
        }
        WandFocusRuntime.setFocus(wandStack, null);
        if (!current.isEmpty()) {
            storeFocus(player, current);
        }
        return currentType != null || !current.isEmpty();
    }

    private static TreeMap<String, FocusLocation> collectAvailableFoci(Player player) {
        TreeMap<String, FocusLocation> foci = new TreeMap<>();
        int sequence = 0;

        // 1. Original Baubles pouch slots 0..3 adapter: off-hand pouch first.
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof FocusPouchItem) {
            sequence = collectFromPouch(foci, offhand, true, -1, sequence);
        }

        // 2. Original main inventory foci slots 0..35.
        int inventoryLimit = Math.min(36, player.getInventory().items.size());
        for (int i = 0; i < inventoryLimit; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WandFocusItem) {
                foci.put(uniqueKey(FocusPouchItem.sortingHelper(stack), sequence++), FocusLocation.inventory(i));
            }
        }

        // 3. Original inventory pouches, pouchcount*1000 + slot encoding adapter.
        for (int i = 0; i < inventoryLimit; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof FocusPouchItem) {
                sequence = collectFromPouch(foci, stack, false, i, sequence);
            }
        }
        return foci;
    }

    private static int collectFromPouch(TreeMap<String, FocusLocation> foci, ItemStack pouch, boolean offhand, int inventorySlot, int sequence) {
        ItemStack[] inv = FocusPouchItem.getInventory(pouch);
        for (int slot = 0; slot < inv.length; slot++) {
            ItemStack stack = inv[slot];
            if (stack != null && stack.getItem() instanceof WandFocusItem) {
                foci.put(uniqueKey(FocusPouchItem.sortingHelper(stack), sequence++), FocusLocation.pouch(offhand, inventorySlot, slot));
            }
        }
        return sequence;
    }

    private static String uniqueKey(String base, int sequence) {
        return (base == null || base.isBlank() ? "focus" : base) + String.format("#%04d", sequence);
    }

    private static void storeFocus(Player player, ItemStack focus) {
        if (focus.isEmpty()) {
            return;
        }
        ItemStack copy = focus.copy();
        copy.setCount(1);

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof FocusPouchItem && FocusPouchItem.addExactFocusStack(offhand, copy)) {
            return;
        }
        int inventoryLimit = Math.min(36, player.getInventory().items.size());
        for (int i = 0; i < inventoryLimit; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof FocusPouchItem && FocusPouchItem.addExactFocusStack(stack, copy)) {
                return;
            }
        }
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
    }

    private static void playCameraTicks(Level level, Player player, float pitch) {
        level.playSound(null, player.blockPosition(), TC4Sounds.event("cameraticks"), SoundSource.PLAYERS, 0.3F, pitch);
    }

    private record FocusLocation(boolean offhandPouch, int inventorySlot, int pouchSlot, boolean directInventory) {
        static FocusLocation inventory(int slot) {
            return new FocusLocation(false, slot, -1, true);
        }

        static FocusLocation pouch(boolean offhand, int inventorySlot, int pouchSlot) {
            return new FocusLocation(offhand, inventorySlot, pouchSlot, false);
        }

        ItemStack take(Player player) {
            if (directInventory) {
                ItemStack stack = player.getInventory().getItem(inventorySlot).copy();
                stack.setCount(1);
                player.getInventory().getItem(inventorySlot).shrink(1);
                return stack;
            }
            ItemStack pouch = offhandPouch ? player.getOffhandItem() : player.getInventory().getItem(inventorySlot);
            return FocusPouchItem.removeFocusAt(pouch, pouchSlot);
        }
    }
}
