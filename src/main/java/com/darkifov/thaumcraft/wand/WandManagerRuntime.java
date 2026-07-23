package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.block.FocusPouchItem;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.porting.TC4Sounds;
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
 * TC4 1.7.10 scans Baubles focus pouches first, then inventory slots in
 * order, adding a direct focus and the contents of a pouch as each slot is visited. Forge 1.19.2 has no Baubles dependency in this port, so
 * the off-hand pouch is the explicit bauble-slot adapter and is scanned before
 * the vanilla inventory. Focus ordering is still TreeMap/higherKey/wrap like
 * the original, and the sound remains thaumcraft:cameraticks with pitch 0.9
 * for REMOVE and 1.0 for focus changes.
 */
public final class WandManagerRuntime {
    public static final String REMOVE = TC4WandFocusContract.REMOVE_SENTINEL;

    private WandManagerRuntime() {}

    public static String currentFocusSortKey(ItemStack wandStack) {
        ItemStack focusStack = WandFocusRuntime.getFocusStack(wandStack);
        if (!focusStack.isEmpty()) {
            return FocusPouchItem.sortingHelper(focusStack);
        }
        WandFocusType type = WandFocusRuntime.getFocus(wandStack);
        return type == null ? "" : FocusPouchItem.sortingHelper(WandFocusRuntime.focusStack(type));
    }

    /**
     * Client-safe snapshot used by the original-style radial HUD. Ordering and
     * duplicate-key replacement are taken from the same TreeMap as changeFocus.
     */
    public static List<AvailableFocus> availableFoci(Player player) {
        TreeMap<String, FocusLocation> locations = collectAvailableFoci(player);
        List<AvailableFocus> result = new ArrayList<>(locations.size());
        for (Map.Entry<String, FocusLocation> entry : locations.entrySet()) {
            ItemStack stack = entry.getValue().peek(player);
            if (!stack.isEmpty() && stack.getItem() instanceof WandFocusItem) {
                stack = stack.copy();
                stack.setCount(1);
                result.add(new AvailableFocus(entry.getKey(), stack));
            }
        }
        return List.copyOf(result);
    }

    public record AvailableFocus(String sortKey, ItemStack stack) {
        public AvailableFocus {
            stack = stack.copy();
            stack.setCount(1);
        }
    }

    public static boolean changeFocus(ItemStack wandStack, Level level, Player player, String requestedFocus) {
        if (!(wandStack.getItem() instanceof WandItem) || WandComponentData.isSceptre(wandStack)) {
            return false;
        }

        ItemStack current = WandFocusRuntime.getFocusStack(wandStack);
        WandFocusType currentType = WandFocusRuntime.getFocus(wandStack);
        boolean remove = REMOVE.equals(requestedFocus);
        TreeMap<String, FocusLocation> foci = collectAvailableFoci(player);

        // Original WandManager uses the same branch for Shift+F and for an empty
        // focus list. It only clears the wand when the installed focus can be
        // returned to a pouch or the player inventory; a full inventory must not
        // eject or delete the focus.
        if (remove || foci.isEmpty()) {
            boolean changed = removeCurrentFocus(wandStack, current, currentType, player);
            if (changed) {
                playCameraTicks(level, player, 0.9F);
            }
            return changed;
        }

        String key = requestedFocus == null || requestedFocus.isBlank()
                ? currentFocusSortKey(wandStack)
                : requestedFocus;
        FocusLocation selectedLocation = foci.get(key);
        if (selectedLocation == null) {
            Map.Entry<String, FocusLocation> higher = foci.higherEntry(key);
            selectedLocation = higher == null ? foci.firstEntry().getValue() : higher.getValue();
        }
        if (selectedLocation == null) {
            return false;
        }

        ItemStack next = selectedLocation.take(player);
        if (next.isEmpty() || !(next.getItem() instanceof WandFocusItem)) {
            return false;
        }

        ItemStack installed = current;
        if (installed.isEmpty() && currentType != null) {
            installed = WandFocusRuntime.focusStack(currentType);
        }
        if (!installed.isEmpty()) {
            if (!tryStoreFocus(player, installed)) {
                // The original keeps the old focus installed and returns the newly
                // selected focus to the slot that was just vacated.
                if (!selectedLocation.putBack(player, next) && !tryStoreFocus(player, next)) {
                    player.drop(next, false);
                }
                return false;
            }
            WandFocusRuntime.setFocus(wandStack, null);
        }

        WandFocusRuntime.setFocusStack(wandStack, next);
        playCameraTicks(level, player, 1.0F);
        return true;
    }

    private static boolean removeCurrentFocus(ItemStack wandStack, ItemStack current,
                                              WandFocusType currentType, Player player) {
        ItemStack installed = current;
        if (installed.isEmpty() && currentType != null) {
            installed = WandFocusRuntime.focusStack(currentType);
        }
        if (installed.isEmpty() || !tryStoreFocus(player, installed)) {
            return false;
        }
        WandFocusRuntime.setFocus(wandStack, null);
        return true;
    }

    /**
     * Original scan order: bauble pouches first, then inventory slots 0..35;
     * each inventory slot contributes a direct focus and/or a pouch immediately.
     * A TreeMap intentionally overwrites exact duplicate sorting keys, matching
     * TC4 rather than inventing sequence suffixes that alter focus cycling.
     */
    private static TreeMap<String, FocusLocation> collectAvailableFoci(Player player) {
        TreeMap<String, FocusLocation> foci = new TreeMap<>();

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof FocusPouchItem) {
            collectFromPouch(foci, offhand, true, -1);
        }

        int inventoryLimit = Math.min(36, player.getInventory().items.size());
        for (int i = 0; i < inventoryLimit; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof WandFocusItem) {
                foci.put(FocusPouchItem.sortingHelper(stack), FocusLocation.inventory(i));
            }
            if (stack.getItem() instanceof FocusPouchItem) {
                collectFromPouch(foci, stack, false, i);
            }
        }
        return foci;
    }

    private static void collectFromPouch(TreeMap<String, FocusLocation> foci, ItemStack pouch,
                                         boolean offhand, int inventorySlot) {
        ItemStack[] inv = FocusPouchItem.getInventory(pouch);
        for (int slot = 0; slot < inv.length; slot++) {
            ItemStack stack = inv[slot];
            if (stack != null && stack.getItem() instanceof WandFocusItem) {
                foci.put(FocusPouchItem.sortingHelper(stack), FocusLocation.pouch(offhand, inventorySlot, slot));
            }
        }
    }

    private static boolean tryStoreFocus(Player player, ItemStack focus) {
        if (focus.isEmpty()) {
            return true;
        }
        ItemStack copy = focus.copy();
        copy.setCount(1);

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof FocusPouchItem && FocusPouchItem.addExactFocusStack(offhand, copy)) {
            return true;
        }
        int inventoryLimit = Math.min(36, player.getInventory().items.size());
        for (int i = 0; i < inventoryLimit; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof FocusPouchItem && FocusPouchItem.addExactFocusStack(stack, copy)) {
                return true;
            }
        }
        return player.getInventory().add(copy);
    }

    private record FocusLocation(boolean offhandPouch, int inventorySlot, int pouchSlot, boolean directInventory) {
        static FocusLocation inventory(int slot) {
            return new FocusLocation(false, slot, -1, true);
        }

        static FocusLocation pouch(boolean offhand, int inventorySlot, int pouchSlot) {
            return new FocusLocation(offhand, inventorySlot, pouchSlot, false);
        }

        ItemStack peek(Player player) {
            if (directInventory) {
                ItemStack source = player.getInventory().getItem(inventorySlot);
                return source.isEmpty() ? ItemStack.EMPTY : source.copy();
            }
            ItemStack pouch = offhandPouch ? player.getOffhandItem() : player.getInventory().getItem(inventorySlot);
            ItemStack[] inventory = FocusPouchItem.getInventory(pouch);
            if (pouchSlot < 0 || pouchSlot >= inventory.length || inventory[pouchSlot] == null) {
                return ItemStack.EMPTY;
            }
            return inventory[pouchSlot].copy();
        }

        ItemStack take(Player player) {
            if (directInventory) {
                ItemStack source = player.getInventory().getItem(inventorySlot);
                if (source.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack stack = source.copy();
                stack.setCount(1);
                source.shrink(1);
                return stack;
            }
            ItemStack pouch = offhandPouch ? player.getOffhandItem() : player.getInventory().getItem(inventorySlot);
            return FocusPouchItem.removeFocusAt(pouch, pouchSlot);
        }

        boolean putBack(Player player, ItemStack focus) {
            if (focus.isEmpty()) {
                return true;
            }
            if (directInventory) {
                ItemStack slot = player.getInventory().getItem(inventorySlot);
                if (slot.isEmpty()) {
                    ItemStack copy = focus.copy();
                    copy.setCount(1);
                    player.getInventory().setItem(inventorySlot, copy);
                    return true;
                }
                return false;
            }
            ItemStack pouch = offhandPouch ? player.getOffhandItem() : player.getInventory().getItem(inventorySlot);
            return FocusPouchItem.putFocusAt(pouch, pouchSlot, focus);
        }
    }

    private static void playCameraTicks(Level level, Player player, float pitch) {
        level.playSound(null, player.blockPosition(), TC4Sounds.event("cameraticks"), SoundSource.PLAYERS, TC4WandFocusContract.CAMERA_TICKS_VOLUME, pitch);
    }

}
