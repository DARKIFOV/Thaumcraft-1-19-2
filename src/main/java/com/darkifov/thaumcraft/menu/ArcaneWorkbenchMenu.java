package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.blockentity.ArcaneWorkbenchBlockEntity;
import com.darkifov.thaumcraft.arcane.ArcaneWorkbenchRecipe;
import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchParity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    private final Container workbench;
    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final ContainerData arcaneData;

    // v11.62.11: server-authoritative GuiArcaneWorkbench state. TC4 shows
    // recipe costs even without a wand, while the real output slot exists only
    // when the inserted non-staff wand can pay. These data slots synchronize the
    // exact matched result/cost instead of re-guessing recipes on the client.
    public static final int DATA_RECIPE_PRESENT = 0;
    public static final int DATA_AFFORDABLE = 1;
    public static final int DATA_GHOST_ITEM_ID = 2;
    public static final int DATA_GHOST_COUNT = 3;
    public static final int DATA_GHOST_ROD = 4;
    public static final int DATA_GHOST_CAP = 5;
    public static final int DATA_GHOST_SCEPTRE = 6;
    public static final int DATA_COST_START = 7;
    public static final int DATA_COUNT = DATA_COST_START + 6;

    /** Menu indices mirror original TC4 ContainerArcaneWorkbench. */
    public static final int MENU_SLOT_OUTPUT = 0;
    public static final int MENU_SLOT_WAND = 1;
    public static final int MENU_SLOT_GRID_START = 2;
    public static final int MENU_SLOT_GRID_END = 10;
    public static final int MENU_PLAYER_INV_START = 11;
    public static final int MENU_PLAYER_INV_END = 37;
    public static final int MENU_HOTBAR_START = 38;
    public static final int MENU_HOTBAR_END = 46;

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getContainer(playerInventory, data.readBlockPos()), new SimpleContainerData(DATA_COUNT));
    }

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, Container workbench) {
        this(containerId, playerInventory, workbench, new SimpleContainerData(DATA_COUNT));
    }

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, Container workbench, ContainerData arcaneData) {
        super(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), containerId);
        checkContainerSize(workbench, ArcaneWorkbenchBlockEntity.SIZE);
        if (arcaneData.getCount() != DATA_COUNT) {
            throw new IllegalArgumentException("Arcane Workbench data size must be " + DATA_COUNT);
        }
        this.workbench = workbench;
        this.playerInventory = playerInventory;
        this.blockPos = workbench instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        this.arcaneData = arcaneData;
        addDataSlots(arcaneData);
        workbench.startOpen(playerInventory.player);

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_OUTPUT, TC4ArcaneWorkbenchParity.OUTPUT_SLOT_X, TC4ArcaneWorkbenchParity.OUTPUT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench && arcaneWorkbench.canTakeOutput(player);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
                    arcaneWorkbench.craftFromOutput(player);
                }
            }
        });

        addSlot(new Slot(workbench, ArcaneWorkbenchBlockEntity.SLOT_WAND, TC4ArcaneWorkbenchParity.WAND_SLOT_X, TC4ArcaneWorkbenchParity.WAND_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack);
            }
        });

        int index = ArcaneWorkbenchBlockEntity.SLOT_INGREDIENT_START;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(workbench, index++, TC4ArcaneWorkbenchParity.gridSlotX(col), TC4ArcaneWorkbenchParity.gridSlotY(row)));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, TC4ArcaneWorkbenchParity.PLAYER_INV_X + col * 18, TC4ArcaneWorkbenchParity.PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, TC4ArcaneWorkbenchParity.PLAYER_INV_X + col * 18, TC4ArcaneWorkbenchParity.HOTBAR_Y));
        }

        if (workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
            arcaneWorkbench.updateOutputPreview(playerInventory.player);
        }
        refreshArcaneState();
    }

    private static Container getContainer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level.getBlockEntity(pos);

        if (blockEntity instanceof ArcaneWorkbenchBlockEntity workbench) {
            return workbench;
        }

        throw new IllegalStateException("Arcane Workbench block entity missing at " + pos);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == workbench && workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench) {
            arcaneWorkbench.updateOutputPreview(playerInventory.player);
            refreshArcaneState();
        }
    }

    @Override
    public void broadcastChanges() {
        refreshArcaneState();
        super.broadcastChanges();
    }

    private void refreshArcaneState() {
        Player player = playerInventory.player;
        if (player == null || player.level.isClientSide || !(workbench instanceof ArcaneWorkbenchBlockEntity arcaneWorkbench)) {
            return;
        }

        for (int i = 0; i < DATA_COUNT; i++) {
            arcaneData.set(i, 0);
        }

        ArcaneWorkbenchRecipe recipe = arcaneWorkbench.findMatchingArcaneRecipeForGrid(player);
        if (recipe == null) {
            return;
        }

        ItemStack result = arcaneWorkbench.previewArcaneResult(recipe);
        if (result.isEmpty()) {
            return;
        }

        arcaneData.set(DATA_RECIPE_PRESENT, 1);
        arcaneData.set(DATA_AFFORDABLE, arcaneWorkbench.canAffordArcaneRecipe(player, recipe) ? 1 : 0);
        arcaneData.set(DATA_GHOST_ITEM_ID, Math.max(0, Item.getId(result.getItem())));
        arcaneData.set(DATA_GHOST_COUNT, Math.max(1, result.getCount()));

        if (result.getItem() instanceof WandItem) {
            WandComponentData components = WandComponentData.from(result);
            arcaneData.set(DATA_GHOST_ROD, components.rod().ordinal() + 1);
            arcaneData.set(DATA_GHOST_CAP, components.cap().ordinal() + 1);
            arcaneData.set(DATA_GHOST_SCEPTRE, WandComponentData.isSceptre(result) ? 1 : 0);
        }

        for (int i = 0; i < TC4ArcaneWorkbenchParity.PRIMALS.length; i++) {
            Aspect aspect = TC4ArcaneWorkbenchParity.PRIMALS[i];
            arcaneData.set(DATA_COST_START + i, arcaneWorkbench.modifiedArcaneCost(recipe, aspect, player));
        }
    }

    public boolean hasArcaneRecipe() {
        return arcaneData.get(DATA_RECIPE_PRESENT) != 0;
    }

    public boolean canAffordArcaneRecipe() {
        return arcaneData.get(DATA_AFFORDABLE) != 0;
    }

    public int arcaneCost(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        for (int i = 0; i < TC4ArcaneWorkbenchParity.PRIMALS.length; i++) {
            if (TC4ArcaneWorkbenchParity.PRIMALS[i] == aspect) {
                return arcaneData.get(DATA_COST_START + i);
            }
        }
        return 0;
    }

    public ItemStack ghostArcaneResult() {
        if (!hasArcaneRecipe()) {
            return ItemStack.EMPTY;
        }
        Item item = Item.byId(arcaneData.get(DATA_GHOST_ITEM_ID));
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(item, Math.max(1, arcaneData.get(DATA_GHOST_COUNT)));
        int rodIndex = arcaneData.get(DATA_GHOST_ROD) - 1;
        int capIndex = arcaneData.get(DATA_GHOST_CAP) - 1;
        if (result.getItem() instanceof WandItem
                && rodIndex >= 0 && rodIndex < WandRodType.values().length
                && capIndex >= 0 && capIndex < WandCapType.values().length) {
            WandComponentData.write(result, WandRodType.values()[rodIndex], WandCapType.values()[capIndex]);
            WandComponentData.setSceptre(result, arcaneData.get(DATA_GHOST_SCEPTRE) != 0);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return workbench.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();

            if (index == MENU_SLOT_OUTPUT && !slot.mayPickup(player)) {
                // v7.62: shift-click must obey SlotCraftingArcaneWorkbench#canTakeStack.
                // Otherwise a stale preview could be moved without paying wand vis.
                return ItemStack.EMPTY;
            }

            if (index == MENU_SLOT_OUTPUT) {
                // Original func_82846_b: output only moves into player inventory + hotbar (11..47).
                if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, original);
            } else if (index >= MENU_PLAYER_INV_START && index <= MENU_PLAYER_INV_END) {
                // Original TC4 does not shift-click ordinary inventory stacks into the 3x3 grid.
                if (stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack)) {
                    if (!moveItemStackTo(stack, MENU_SLOT_WAND, MENU_SLOT_WAND + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, MENU_HOTBAR_START, MENU_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= MENU_HOTBAR_START && index <= MENU_HOTBAR_END) {
                if (stack.getItem() instanceof WandItem && !WandItem.isStaffStack(stack)) {
                    if (!moveItemStackTo(stack, MENU_SLOT_WAND, MENU_SLOT_WAND + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, MENU_PLAYER_INV_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Grid and wand slot move back to inventory/hotbar, mirroring TC4's final branch.
                if (!moveItemStackTo(stack, MENU_PLAYER_INV_START, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return original;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        // Original func_75144_a: drop-click mode forces dragType=1, and right-clicks
        // on output/wand slots are coerced to left-click semantics.
        if (clickType == ClickType.THROW) {
            dragType = 1;
        }
        if ((slotId == MENU_SLOT_OUTPUT || slotId == MENU_SLOT_WAND) && dragType > 0) {
            dragType = 0;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        // Original func_94530_a rejects drag-splitting into TileArcaneWorkbench slots.
        return slot.container != workbench && super.canDragTo(slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        workbench.stopOpen(player);
    }
}
