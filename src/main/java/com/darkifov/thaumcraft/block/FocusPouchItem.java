package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.wand.WandFocusRuntime;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage130: TC4-style focus pouch runtime adapter.
 *
 * Original TC4 uses ItemFocusPouch + InventoryFocusPouch + GuiFocusPouch. The
 * 1.19.2 port keeps the same gameplay role without depending on the old GUI
 * stack: the pouch stores wand foci in NBT, can collect a focus from off-hand,
 * can return one to inventory, and can rotate/equip foci into a wand.
 */
public class FocusPouchItem extends Item {
    private static final String TAG_FOCI = "Foci";
    private static final String TAG_SELECTED = "SelectedFocus";
    private static final int MAX_FOCI = 18;

    public FocusPouchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static int storedCount(ItemStack pouch, WandFocusType type) {
        return pouch.getOrCreateTagElement(TAG_FOCI).getInt(type.id());
    }

    public static int totalStored(ItemStack pouch) {
        int total = 0;
        CompoundTag foci = pouch.getOrCreateTagElement(TAG_FOCI);
        for (WandFocusType type : WandFocusType.values()) {
            total += Math.max(0, foci.getInt(type.id()));
        }
        return total;
    }

    public static boolean addFocus(ItemStack pouch, WandFocusType type, int count) {
        if (type == null || count <= 0) {
            return false;
        }
        int space = MAX_FOCI - totalStored(pouch);
        if (space <= 0) {
            return false;
        }
        int accepted = Math.min(space, count);
        CompoundTag foci = pouch.getOrCreateTagElement(TAG_FOCI);
        foci.putInt(type.id(), Math.max(0, foci.getInt(type.id())) + accepted);
        if (!pouch.getOrCreateTag().contains(TAG_SELECTED)) {
            pouch.getOrCreateTag().putString(TAG_SELECTED, type.id());
        }
        return true;
    }

    public static boolean removeFocus(ItemStack pouch, WandFocusType type, int count) {
        if (type == null || count <= 0) {
            return false;
        }
        CompoundTag foci = pouch.getOrCreateTagElement(TAG_FOCI);
        int current = foci.getInt(type.id());
        if (current < count) {
            return false;
        }
        int next = current - count;
        if (next <= 0) {
            foci.remove(type.id());
        } else {
            foci.putInt(type.id(), next);
        }
        if (type.id().equals(pouch.getOrCreateTag().getString(TAG_SELECTED)) && next <= 0) {
            WandFocusType replacement = firstStored(pouch);
            if (replacement == null) {
                pouch.getOrCreateTag().remove(TAG_SELECTED);
            } else {
                pouch.getOrCreateTag().putString(TAG_SELECTED, replacement.id());
            }
        }
        return true;
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

    public static boolean equipNextFocusFromPouch(ItemStack pouch, ItemStack wandStack, Player player, boolean reverse) {
        WandFocusType current = WandFocusRuntime.getFocus(wandStack);
        List<WandFocusType> stored = storedTypes(pouch);

        if (stored.isEmpty()) {
            if (current != null && reverse) {
                addFocus(pouch, current, 1);
                WandFocusRuntime.setFocus(wandStack, null);
                player.displayClientMessage(Component.literal("Stored " + current.displayName() + " in the Focus Pouch.").withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            player.displayClientMessage(Component.literal("Focus Pouch is empty.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        List<WandFocusType> available = storedTypes(pouch);
        int selectedIndex = 0;
        if (current != null) {
            int currentIndex = available.indexOf(current);
            if (currentIndex >= 0) {
                selectedIndex = reverse ? currentIndex - 1 : currentIndex + 1;
            }
        } else {
            WandFocusType preferred = selected(pouch);
            int preferredIndex = available.indexOf(preferred);
            selectedIndex = preferredIndex >= 0 ? preferredIndex : 0;
        }

        selectedIndex = Math.floorMod(selectedIndex, available.size());
        WandFocusType next = available.get(selectedIndex);
        removeFocus(pouch, next, 1);
        if (current != null) {
            addFocus(pouch, current, 1);
        }
        WandFocusRuntime.setFocus(wandStack, next);
        pouch.getOrCreateTag().putString(TAG_SELECTED, next.id());
        player.displayClientMessage(Component.literal("Equipped " + next.displayName() + " from the Focus Pouch.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }

    private static WandFocusType firstStored(ItemStack pouch) {
        return storedTypes(pouch).stream().findFirst().orElse(null);
    }

    private static List<WandFocusType> storedTypes(ItemStack pouch) {
        List<WandFocusType> result = new ArrayList<>();
        for (WandFocusType type : WandFocusType.values()) {
            if (storedCount(pouch, type) > 0) {
                result.add(type);
            }
        }
        result.sort(Comparator.comparing(WandFocusType::id));
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(pouch);
        }

        ItemStack offhand = player.getOffhandItem();
        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof WandFocusItem focusItem) {
            int before = totalStored(pouch);
            addFocus(pouch, focusItem.focusType(), offhand.getCount());
            int accepted = totalStored(pouch) - before;
            if (accepted <= 0) {
                player.displayClientMessage(Component.literal("Focus Pouch is full.").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.consume(pouch);
            }
            if (!player.getAbilities().instabuild) {
                offhand.shrink(accepted);
            }
            pouch.getOrCreateTag().putString(TAG_SELECTED, focusItem.focusType().id());
            player.displayClientMessage(Component.literal("Stored " + focusItem.focusType().displayName() + " in the Focus Pouch.").withStyle(ChatFormatting.GOLD), true);
            return InteractionResultHolder.consume(pouch);
        }

        WandFocusType selected = selected(pouch);
        if (player.isShiftKeyDown()) {
            if (selected == null) {
                player.displayClientMessage(Component.literal("Focus Pouch is empty.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.consume(pouch);
            }
            removeFocus(pouch, selected, 1);
            ItemStack stack = WandFocusRuntime.focusStack(selected);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            player.displayClientMessage(Component.literal("Removed " + selected.displayName() + " from the Focus Pouch.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.consume(pouch);
        }

        if (selected == null) {
            player.displayClientMessage(Component.literal("Focus Pouch is empty. Hold a focus in off-hand and right-click the pouch to store it.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.consume(pouch);
        }

        List<WandFocusType> types = storedTypes(pouch);
        int nextIndex = Math.floorMod(types.indexOf(selected) + 1, types.size());
        WandFocusType next = types.get(nextIndex);
        pouch.getOrCreateTag().putString(TAG_SELECTED, next.id());
        player.displayClientMessage(Component.literal("Selected " + next.displayName() + " in Focus Pouch.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return InteractionResultHolder.consume(pouch);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int total = totalStored(stack);
        tooltip.add(Component.literal("TC4 focus storage: " + total + "/" + MAX_FOCI + " focus" + (total == 1 ? "" : "es")).withStyle(ChatFormatting.DARK_PURPLE));
        WandFocusType selected = selected(stack);
        if (selected != null) {
            tooltip.add(Component.literal("Selected: " + selected.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        for (WandFocusType type : storedTypes(stack)) {
            tooltip.add(Component.literal("- " + type.displayName() + " x" + storedCount(stack, type)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Off-hand focus + right-click: store").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Off-hand pouch + wand right-click: equip/cycle").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Shift + right-click: remove selected").withStyle(ChatFormatting.DARK_GRAY));
    }
}
