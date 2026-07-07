package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.ThaumGolemEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * Stage203: original ContainerGolem + ContainerGhostSlots parity adapter.
 *
 * Original TC4 uses SlotGhost for all golem filter/inventory configuration slots and
 * SlotGhostFluid for liquid cores. Those slots copy stacks into the golem inventory
 * and never consume or return real player items. The Forge 1.19.2 menu keeps that
 * behavior by intercepting click handling for GolemGhostSlot before vanilla slot
 * mutation can happen.
 */
public class GolemMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final ThaumGolemEntity golem;
    private final GolemInventoryContainer golemInventory;
    private int currentScroll;
    private int maxScroll;
    private int visibleGolemSlots;

    public GolemMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, readGolem(inventory.player.level, data));
    }

    public GolemMenu(int containerId, Inventory inventory, ThaumGolemEntity golem) {
        super(ThaumcraftMod.GOLEM_MENU.get(), containerId);
        this.playerInventory = inventory;
        this.golem = golem;
        this.golemInventory = new GolemInventoryContainer(golem);
        if (this.golem != null) {
            this.golem.setPausedByGolemGui(true);
        }
        bindGolemInventory();
        bindPlayerInventory();
    }

    private static ThaumGolemEntity readGolem(Level level, FriendlyByteBuf data) {
        int entityId = data.readVarInt();
        Entity entity = level.getEntity(entityId);
        return entity instanceof ThaumGolemEntity found ? found : null;
    }

    private void bindGolemInventory() {
        this.visibleGolemSlots = 0;
        int slots = golemInventory.getContainerSize();
        this.maxScroll = Math.max(0, (slots / 6) - 1);
        this.currentScroll = Math.max(0, Math.min(currentScroll, maxScroll));
        if (golem == null || !golem.getCoreType().hasInventory()) {
            return;
        }
        this.visibleGolemSlots = Math.min(6, slots);
        for (int a = 0; a < visibleGolemSlots; a++) {
            addSlot(new GolemVisibleGhostSlot(a, 100 + a / 2 * 28, 16 + a % 2 * 31));
        }
    }

    private void bindPlayerInventory() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int currentScroll() {
        return currentScroll;
    }

    public int maxScroll() {
        return maxScroll;
    }

    public int visibleGolemSlots() {
        return visibleGolemSlots;
    }

    public int totalGolemSlots() {
        return golemInventory.getContainerSize();
    }

    public ThaumGolemEntity golem() {
        return golem;
    }

    public int golemColor(int slot) {
        return golem == null ? -1 : golem.getGolemColor(slot);
    }

    public byte golemToggle(int index) {
        return golem == null ? 0 : golem.getGolemToggle(index);
    }

    private int originalGhostLimit() {
        if (golem == null) {
            return 1;
        }
        // ContainerGolem line 37/42: FILL core uses SlotGhost default limit 256;
        // liquid and all other ghost slots use limit 1.
        return golem.getCoreType() == GolemCoreType.FILL ? 256 : 1;
    }

    private boolean originalLiquidGhost() {
        return golem != null && golem.getCoreType() == GolemCoreType.LIQUID;
    }

    private static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    private boolean sameGhostStack(ItemStack left, ItemStack right) {
        return !left.isEmpty() && !right.isEmpty() && ItemStack.isSameItemSameTags(left, right);
    }

    private void setGhostStack(GolemVisibleGhostSlot slot, ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            slot.set(ItemStack.EMPTY);
            return;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.max(1, Math.min(count, slot.getMaxStackSize())));
        slot.set(copy);
    }

    private boolean applyGhostClick(GolemVisibleGhostSlot slot, int button, ClickType clickType) {
        if (slot == null) {
            return false;
        }

        if (clickType == ClickType.THROW) {
            slot.set(ItemStack.EMPTY);
            return true;
        }

        if (clickType == ClickType.QUICK_MOVE) {
            if (slot.hasItem()) {
                if (button == 0) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    ItemStack stack = slot.getItem();
                    stack.setCount(Math.min(slot.getMaxStackSize(), stack.getCount() + 16));
                    slot.set(stack);
                }
                return true;
            }
            return false;
        }

        if (clickType != ClickType.PICKUP && clickType != ClickType.CLONE) {
            return true;
        }

        ItemStack carried = getCarried();
        ItemStack current = slot.getItem();

        if (clickType == ClickType.CLONE) {
            if (carried.isEmpty() && !current.isEmpty()) {
                ItemStack clone = current.copy();
                clone.setCount(current.getMaxStackSize());
                setCarried(clone);
            }
            return true;
        }

        if (carried.isEmpty()) {
            if (current.isEmpty()) {
                return true;
            }
            int next = button == 0 ? current.getCount() - 1 : current.getCount() + 1;
            if (next <= 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                current.setCount(Math.min(slot.getMaxStackSize(), next));
                slot.set(current);
            }
            return true;
        }

        if (!slot.mayPlace(carried)) {
            return true;
        }

        if (current.isEmpty() || !sameGhostStack(current, carried)) {
            setGhostStack(slot, carried, button == 0 ? carried.getCount() : 1);
            return true;
        }

        int add = button == 0 ? carried.getCount() : 1;
        current.setCount(Math.min(slot.getMaxStackSize(), current.getCount() + add));
        slot.set(current);
        return true;
    }

    private void copyToFirstGhostSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int i = 0; i < visibleGolemSlots; i++) {
            Slot slot = slots.get(i);
            if (slot instanceof GolemVisibleGhostSlot ghost && ghost.mayPlace(stack)) {
                if (!ghost.hasItem() || sameGhostStack(ghost.getItem(), stack)) {
                    int count = ghost.hasItem() ? ghost.getItem().getCount() + stack.getCount() : stack.getCount();
                    setGhostStack(ghost, stack, count);
                    return;
                }
            }
        }
    }

    // Stage201 compatibility: GolemVisibleSlot semantics are now implemented by GolemVisibleGhostSlot.
    private final class GolemVisibleGhostSlot extends Slot {
        private final int visibleIndex;

        private GolemVisibleGhostSlot(int visibleIndex, int x, int y) {
            super(golemInventory, visibleIndex, x, y);
            this.visibleIndex = visibleIndex;
        }

        private int realIndex() {
            return visibleIndex + currentScroll * 6;
        }

        @Override
        public ItemStack getItem() {
            return golemInventory.getItem(realIndex());
        }

        @Override
        public void set(ItemStack stack) {
            ItemStack ghost = stack == null ? ItemStack.EMPTY : stack.copy();
            if (!ghost.isEmpty()) {
                ghost.setCount(Math.min(ghost.getCount(), getMaxStackSize()));
            }
            golemInventory.setItem(realIndex(), ghost);
            setChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            // SlotGhost never removes a real item. Its client interaction changes only
            // the ghost count/filter stack through the menu click adapter.
            return ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return originalGhostLimit();
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !originalLiquidGhost() || isFluidContainer(stack);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot clicked = slots.get(slotId);
            if (clicked instanceof GolemVisibleGhostSlot ghost) {
                applyGhostClick(ghost, button, clickType);
                return;
            }
            if (clickType == ClickType.QUICK_MOVE && slotId >= visibleGolemSlots && clicked.hasItem() && visibleGolemSlots > 0) {
                copyToFirstGhostSlot(clicked.getItem());
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (golem == null) {
            return false;
        }
        if (button == 66 && currentScroll > 0) {
            currentScroll--;
            player.level.playSound(null, golem.blockPosition(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.2F, 0.8F);
            return true;
        }
        if (button == 67 && currentScroll < maxScroll) {
            currentScroll++;
            player.level.playSound(null, golem.blockPosition(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.2F, 0.8F);
            return true;
        }
        if (button >= 50 && button <= 57) {
            golem.toggleGolemFlag(button - 50);
            player.level.playSound(null, golem.blockPosition(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.2F, 0.8F);
            return true;
        }
        int slots = totalGolemSlots();
        if (button >= 0 && button < slots) {
            golem.cycleGolemColor(button, false);
            return true;
        }
        if (button >= slots && button < slots * 2) {
            golem.cycleGolemColor(button - slots, true);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int golemSlots = visibleGolemSlots;
        if (slotIndex < golemSlots) {
            // Ghost slots do not transfer real contents into the player inventory.
            return copy;
        }
        if (golemSlots > 0) {
            copyToFirstGhostSlot(stack);
            return copy;
        }
        if (!moveItemStackTo(stack, golemSlots, slots.size(), true)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return golemInventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (golem != null) {
            golem.setPausedByGolemGui(false);
            golemInventory.setChanged();
        }
    }
}
