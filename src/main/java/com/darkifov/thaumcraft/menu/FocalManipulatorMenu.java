package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.blockentity.FocalManipulatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Original ContainerFocalManipulator slot positions and server button bridge. */
public class FocalManipulatorMenu extends AbstractContainerMenu {
    public static final int FOCUS_SLOT = 0;
    public static final int PLAYER_INV_START = 1;
    public static final int PLAYER_INV_END = 27;
    public static final int HOTBAR_START = 28;
    public static final int HOTBAR_END = 36;

    private final Container container;
    private final BlockPos blockPos;
    private final ContainerData data;

    public FocalManipulatorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, getTile(inventory, buffer.readBlockPos()),
                new SimpleContainerData(FocalManipulatorBlockEntity.DATA_COUNT));
    }

    public FocalManipulatorMenu(int id, Inventory inventory, FocalManipulatorBlockEntity tile, ContainerData data) {
        super(ThaumcraftMod.FOCAL_MANIPULATOR_MENU.get(), id);
        this.container = tile;
        this.blockPos = tile.getBlockPos();
        this.data = data;
        checkContainerSize(container, FocalManipulatorBlockEntity.SIZE);
        checkContainerDataCount(data, FocalManipulatorBlockEntity.DATA_COUNT);
        addDataSlots(data);
        container.startOpen(inventory.player);

        addSlot(new Slot(container, FocalManipulatorBlockEntity.SLOT_FOCUS, 88, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandFocusItem && !isCrafting();
            }

            @Override
            public boolean mayPickup(Player player) {
                return !isCrafting();
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 16 + col * 18, 209));
        }
    }

    private static FocalManipulatorBlockEntity getTile(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level.getBlockEntity(pos);
        if (blockEntity instanceof FocalManipulatorBlockEntity tile) return tile;
        throw new IllegalStateException("Focal Manipulator block entity missing at " + pos);
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public ItemStack focusStack() {
        return container.getItem(FocalManipulatorBlockEntity.SLOT_FOCUS);
    }

    public boolean isCrafting() {
        return data.get(FocalManipulatorBlockEntity.DATA_INITIAL_SIZE) > 0;
    }

    public int initialSize() {
        return data.get(FocalManipulatorBlockEntity.DATA_INITIAL_SIZE);
    }

    public int activeUpgrade() {
        return data.get(FocalManipulatorBlockEntity.DATA_UPGRADE);
    }

    public int activeRank() {
        return data.get(FocalManipulatorBlockEntity.DATA_RANK);
    }

    public int remainingAspect(int primalIndex) {
        return data.get(FocalManipulatorBlockEntity.DATA_ASPECT_START + primalIndex);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(container instanceof FocalManipulatorBlockEntity tile)) return false;
        boolean started = tile.startCraft(id, player);
        if (!started && !player.level.isClientSide) {
            player.level.playSound(null, tile.getBlockPos(), TC4Sounds.event("craftfail"),
                    SoundSource.BLOCKS, 0.33F, 1.0F);
        }
        return started;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isCrafting() || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == FOCUS_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END + 1, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof WandFocusItem) {
            if (!moveItemStackTo(stack, FOCUS_SLOT, FOCUS_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (index >= PLAYER_INV_START && index <= PLAYER_INV_END) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END + 1, false)) return ItemStack.EMPTY;
        } else if (index >= HOTBAR_START && index <= HOTBAR_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
