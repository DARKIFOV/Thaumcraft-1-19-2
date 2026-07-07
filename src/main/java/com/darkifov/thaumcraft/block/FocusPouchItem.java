package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.menu.FocusPouchContainer;
import com.darkifov.thaumcraft.menu.FocusPouchMenu;
import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage186: strict original Focus Pouch adapter.
 * Original TC4 source of truth: ItemFocusPouch, ItemFocusPouchBauble,
 * InventoryFocusPouch, ContainerFocusPouch and GuiFocusPouch.
 */
public class FocusPouchItem extends Item {
    public static final String TAG_INVENTORY = "Inventory";
    private static final String LEGACY_TAG_FOCI = "Foci";
    private static final String TAG_SELECTED = "SelectedFocus";
    private static final int MAX_FOCI = 18;

    public FocusPouchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** Original ItemFocusPouch#getInventory: NBTTagList "Inventory", byte "Slot", ItemStack.loadItemStackFromNBT. */
    public static ItemStack[] getInventory(ItemStack item) {
        migrateLegacyCounts(item);
        return FocusPouchContainer.readInventoryList(item);
    }

    /** Original ItemFocusPouch#setInventory: writes NBTTagList "Inventory" with byte Slot and saved ItemStacks. */
    public static void setInventory(ItemStack item, ItemStack[] stackList) {
        FocusPouchContainer.writeInventoryList(item, stackList);
    }

    public static int storedCount(ItemStack pouch, WandFocusType type) {
        int total = 0;
        for (ItemStack stack : getInventory(pouch)) {
            if (stack.getItem() instanceof WandFocusItem focusItem && focusItem.focusType() == type) {
                total += 1;
            }
        }
        return total;
    }

    public static int totalStored(ItemStack pouch) {
        int total = 0;
        for (ItemStack stack : getInventory(pouch)) {
            if (!stack.isEmpty()) {
                total += 1;
            }
        }
        return total;
    }

    public static boolean addFocus(ItemStack pouch, WandFocusType type, int count) {
        if (type == null || count <= 0) {
            return false;
        }
        ItemStack[] inv = getInventory(pouch);
        boolean changed = false;
        for (int n = 0; n < count; n++) {
            int slot = firstEmpty(inv);
            if (slot < 0) {
                break;
            }
            inv[slot] = WandFocusRuntime.focusStack(type);
            inv[slot].setCount(1);
            changed = true;
        }
        if (changed) {
            setInventory(pouch, inv);
            if (!pouch.getOrCreateTag().contains(TAG_SELECTED)) {
                pouch.getOrCreateTag().putString(TAG_SELECTED, type.id());
            }
        }
        return changed;
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
            if (type.id().equals(pouch.getOrCreateTag().getString(TAG_SELECTED)) && storedCount(pouch, type) <= 0) {
                WandFocusType replacement = firstStored(pouch);
                if (replacement == null) {
                    pouch.getOrCreateTag().remove(TAG_SELECTED);
                } else {
                    pouch.getOrCreateTag().putString(TAG_SELECTED, replacement.id());
                }
            }
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

    public static String sortingHelper(ItemStack stack) {
        if (stack.getItem() instanceof WandFocusItem focusItem) {
            return sortKey(focusItem.focusType(), stack);
        }
        return "";
    }

    public static int maxFocusSlots() {
        return MAX_FOCI;
    }

    public static WandFocusType selected(ItemStack pouch) {
        WandFocusType preferred = WandFocusType.byId(pouch.getOrCreateTag().getString(TAG_SELECTED));
        if (preferred != null && storedCount(pouch, preferred) > 0) {
            return preferred;
        }
        WandFocusType first = firstStored(pouch);
        if (first != null) {
            pouch.getOrCreateTag().putString(TAG_SELECTED, first.id());
        }
        return first;
    }

    /**
     * Stage186 WandManager.changeFocus adapter: pull one focus from pouch Inventory slots,
     * return the previous wand focus to the first empty pouch slot, and preserve exact focus ItemStack NBT.
     */
    public static boolean equipNextFocusFromPouch(ItemStack pouch, ItemStack wandStack, Player player, boolean reverse) {
        ItemStack[] inv = getInventory(pouch);
        List<FocusSlot> foci = focusSlots(inv);
        ItemStack currentStack = WandFocusRuntime.getFocusStack(wandStack);
        WandFocusType currentType = WandFocusRuntime.getFocus(wandStack);

        if (foci.isEmpty()) {
            if (currentType != null && storeFocusStack(inv, currentStack.isEmpty() ? WandFocusRuntime.focusStack(currentType) : currentStack)) {
                WandFocusRuntime.setFocus(wandStack, null);
                setInventory(pouch, inv);
                player.displayClientMessage(Component.literal("Stored " + currentType.displayName() + " in the Focus Pouch.").withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            player.displayClientMessage(Component.literal("Focus Pouch is empty.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        foci.sort(Comparator.comparing(FocusSlot::sortKey));
        int nextIndex = 0;
        if (currentType != null) {
            String key = sortKey(currentType, currentStack);
            int found = -1;
            for (int i = 0; i < foci.size(); i++) {
                if (foci.get(i).sortKey().compareTo(key) > 0) {
                    found = i;
                    break;
                }
            }
            nextIndex = found >= 0 ? found : 0;
            if (reverse) {
                nextIndex = Math.floorMod(nextIndex - 1, foci.size());
            }
        } else {
            WandFocusType preferred = selected(pouch);
            for (int i = 0; i < foci.size(); i++) {
                if (foci.get(i).type() == preferred) {
                    nextIndex = i;
                    break;
                }
            }
        }

        FocusSlot picked = foci.get(nextIndex);
        ItemStack nextFocus = inv[picked.slot()].copy();
        inv[picked.slot()] = ItemStack.EMPTY;
        if (currentType != null) {
            ItemStack old = currentStack.isEmpty() ? WandFocusRuntime.focusStack(currentType) : currentStack.copy();
            old.setCount(1);
            if (!storeFocusStack(inv, old)) {
                if (!player.getInventory().add(old)) {
                    player.drop(old, false);
                }
            }
        }
        WandFocusRuntime.setFocusStack(wandStack, nextFocus);
        setInventory(pouch, inv);
        pouch.getOrCreateTag().putString(TAG_SELECTED, picked.type().id());
        player.displayClientMessage(Component.literal("Equipped " + picked.type().displayName() + " from the Focus Pouch.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }

    private static void migrateLegacyCounts(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null || tag.contains(TAG_INVENTORY, 9) || !tag.contains(LEGACY_TAG_FOCI, 10)) {
            return;
        }
        ItemStack[] inv = new ItemStack[MAX_FOCI];
        for (int i = 0; i < inv.length; i++) {
            inv[i] = ItemStack.EMPTY;
        }
        CompoundTag foci = tag.getCompound(LEGACY_TAG_FOCI);
        int slot = 0;
        for (WandFocusType type : WandFocusType.values()) {
            int count = Math.max(0, foci.getInt(type.id()));
            for (int i = 0; i < count && slot < inv.length; i++) {
                inv[slot++] = WandFocusRuntime.focusStack(type);
            }
        }
        setInventory(pouch, inv);
        tag.remove(LEGACY_TAG_FOCI);
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

    private static WandFocusType firstStored(ItemStack pouch) {
        return storedTypes(pouch).stream().findFirst().orElse(null);
    }

    private static List<WandFocusType> storedTypes(ItemStack pouch) {
        List<WandFocusType> result = new ArrayList<>();
        for (FocusSlot slot : focusSlots(getInventory(pouch))) {
            if (!result.contains(slot.type())) {
                result.add(slot.type());
            }
        }
        result.sort(Comparator.comparing(WandFocusType::id));
        return result;
    }

    private static List<FocusSlot> focusSlots(ItemStack[] inv) {
        List<FocusSlot> result = new ArrayList<>();
        for (int i = 0; i < inv.length; i++) {
            ItemStack stack = inv[i];
            if (stack != null && stack.getItem() instanceof WandFocusItem focusItem) {
                result.add(new FocusSlot(i, focusItem.focusType(), sortKey(focusItem.focusType(), stack)));
            }
        }
        return result;
    }

    private static String sortKey(WandFocusType type, ItemStack stack) {
        // Original getSortingHelper is focus-defined; use original focus id plus saved NBT as a deterministic 1.19.2 adapter.
        return type.id() + ":" + stack.getHoverName().getString();
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
                            Component.literal("Focus Pouch")
                    ),
                    buffer -> buffer.writeBoolean(mainHand)
            );
        }
        return InteractionResultHolder.success(pouch);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int total = totalStored(stack);
        tooltip.add(Component.literal("TC4 Focus Pouch: " + total + "/" + MAX_FOCI).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Right-click: open original focus pouch GUI.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Off-hand pouch + wand right-click: cycle/equip focus.").withStyle(ChatFormatting.DARK_GRAY));
    }

    private record FocusSlot(int slot, WandFocusType type, String sortKey) {}
}
