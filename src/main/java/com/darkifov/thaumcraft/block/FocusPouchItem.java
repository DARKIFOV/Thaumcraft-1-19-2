package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.menu.FocusPouchContainer;
import com.darkifov.thaumcraft.menu.FocusPouchMenu;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Strict modern adapter for original TC4 ItemFocusPouch / InventoryFocusPouch.
 * Production NBT contains only the original Inventory list with byte Slot entries.
 */
public class FocusPouchItem extends Item {
    public static final String TAG_INVENTORY = "Inventory";
    private static final String LEGACY_TAG_FOCI = "Foci";
    private static final String LEGACY_TAG_SELECTED = "SelectedFocus";
    private static final int MAX_FOCI = 18;

    public FocusPouchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** Original ItemFocusPouch#getInventory. */
    public static ItemStack[] getInventory(ItemStack item) {
        migrateLegacyCounts(item);
        removeInventedSelectionTag(item);
        return FocusPouchContainer.readInventoryList(item);
    }

    /** Original ItemFocusPouch#setInventory. */
    public static void setInventory(ItemStack item, ItemStack[] stackList) {
        removeInventedSelectionTag(item);
        FocusPouchContainer.writeInventoryList(item, stackList);
    }

    public static int storedCount(ItemStack pouch, WandFocusType type) {
        int total = 0;
        for (ItemStack stack : getInventory(pouch)) {
            if (stack.getItem() instanceof WandFocusItem focusItem && focusItem.focusType() == type) {
                total++;
            }
        }
        return total;
    }

    public static int totalStored(ItemStack pouch) {
        int total = 0;
        for (ItemStack stack : getInventory(pouch)) {
            if (!stack.isEmpty()) {
                total++;
            }
        }
        return total;
    }

    public static boolean addFocus(ItemStack pouch, WandFocusType type, int count) {
        if (type == null || count <= 0) {
            return false;
        }
        ItemStack[] inv = getInventory(pouch);
        int added = 0;
        while (added < count) {
            int slot = firstEmpty(inv);
            if (slot < 0) {
                break;
            }
            ItemStack focus = WandFocusRuntime.focusStack(type);
            focus.setCount(1);
            inv[slot] = focus;
            added++;
        }
        if (added > 0) {
            setInventory(pouch, inv);
        }
        return added == count;
    }

    public static boolean removeFocus(ItemStack pouch, WandFocusType type, int count) {
        if (type == null || count <= 0) {
            return false;
        }
        ItemStack[] inv = getInventory(pouch);
        int removed = 0;
        for (int i = 0; i < inv.length && removed < count; i++) {
            ItemStack stack = inv[i];
            if (stack.getItem() instanceof WandFocusItem focusItem && focusItem.focusType() == type) {
                inv[i] = ItemStack.EMPTY;
                removed++;
            }
        }
        if (removed > 0) {
            setInventory(pouch, inv);
        }
        return removed == count;
    }

    public static boolean addExactFocusStack(ItemStack pouch, ItemStack focus) {
        ItemStack[] inv = getInventory(pouch);
        boolean stored = storeFocusStack(inv, focus);
        if (stored) {
            setInventory(pouch, inv);
        }
        return stored;
    }

    public static ItemStack removeFocusAt(ItemStack pouch, int slot) {
        ItemStack[] inv = getInventory(pouch);
        if (slot < 0 || slot >= inv.length || inv[slot] == null || inv[slot].isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack out = inv[slot].copy();
        out.setCount(1);
        inv[slot] = ItemStack.EMPTY;
        setInventory(pouch, inv);
        return out;
    }

    /** Restore the exact source slot when a transactional WandManager swap fails. */
    public static boolean putFocusAt(ItemStack pouch, int slot, ItemStack focus) {
        if (focus.isEmpty() || !(focus.getItem() instanceof WandFocusItem)) {
            return false;
        }
        ItemStack[] inv = getInventory(pouch);
        if (slot < 0 || slot >= inv.length || !inv[slot].isEmpty()) {
            return false;
        }
        ItemStack copy = focus.copy();
        copy.setCount(1);
        inv[slot] = copy;
        setInventory(pouch, inv);
        return true;
    }

    public static String sortingHelper(ItemStack stack) {
        if (stack.getItem() instanceof WandFocusItem focusItem) {
            return sortKey(focusItem.focusType(), stack);
        }
        return "";
    }

    public static int maxFocusSlots() {
        return MAX_FOCI;
    }

    private static void migrateLegacyCounts(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null || tag.contains(TAG_INVENTORY, 9) || !tag.contains(LEGACY_TAG_FOCI, 10)) {
            return;
        }
        ItemStack[] inv = emptyInventory();
        CompoundTag foci = tag.getCompound(LEGACY_TAG_FOCI);
        int slot = 0;
        for (WandFocusType type : WandFocusType.values()) {
            int count = Math.max(0, foci.getInt(type.id()));
            for (int i = 0; i < count && slot < inv.length; i++) {
                inv[slot++] = WandFocusRuntime.focusStack(type);
            }
        }
        FocusPouchContainer.writeInventoryList(pouch, inv);
        tag.remove(LEGACY_TAG_FOCI);
        tag.remove(LEGACY_TAG_SELECTED);
    }

    private static void removeInventedSelectionTag(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag != null) {
            tag.remove(LEGACY_TAG_SELECTED);
        }
    }

    private static ItemStack[] emptyInventory() {
        ItemStack[] inv = new ItemStack[MAX_FOCI];
        java.util.Arrays.fill(inv, ItemStack.EMPTY);
        return inv;
    }

    private static int firstEmpty(ItemStack[] inv) {
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] == null || inv[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static boolean storeFocusStack(ItemStack[] inv, ItemStack focus) {
        if (focus.isEmpty() || !(focus.getItem() instanceof WandFocusItem)) {
            return false;
        }
        int slot = firstEmpty(inv);
        if (slot < 0) {
            return false;
        }
        ItemStack copy = focus.copy();
        copy.setCount(1);
        inv[slot] = copy;
        return true;
    }

    private static String sortKey(WandFocusType type, ItemStack stack) {
        String prefix = switch (type) {
            case FIRE -> "AF";
            case EXCAVATION -> "BE";
            case FROST -> "BF";
            case SHOCK -> "BL";
            case EQUAL_TRADE -> "BT";
            case PORTABLE_HOLE -> "BPH";
            case WARDING -> "BWA";
            case HELLBAT -> "HH";
            case PECH_CURSE -> "PP";
            case PRIMAL -> "FP";
        };
        return prefix + FocusUpgradeRuntime.sortingHelper(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean mainHand = hand == InteractionHand.MAIN_HAND;
            NetworkHooks.openScreen(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (int id, Inventory inventory, Player menuPlayer) -> new FocusPouchMenu(id, inventory, pouch),
                            Component.translatable("container.focuspouch")
                    ),
                    buffer -> buffer.writeBoolean(mainHand)
            );
        }
        return InteractionResultHolder.success(pouch);
    }
}
