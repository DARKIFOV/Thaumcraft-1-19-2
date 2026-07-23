package com.darkifov.thaumcraft.menu;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcaneSpaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/** Modern ContainerSpa adaptation preserving the original one-slot layout. */
public class ArcaneSpaMenu extends AbstractContainerMenu {
    public static final int BUTTON_TOGGLE_MIX = 1;
    private static final int SPA_SLOT_COUNT = 1;

    private final ArcaneSpaBlockEntity spa;
    private final ContainerData data;
    private final BlockPos pos;

    public ArcaneSpaMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer.readBlockPos()), new SimpleContainerData(3));
    }

    public ArcaneSpaMenu(int id, Inventory inventory, ArcaneSpaBlockEntity spa, ContainerData data) {
        super(ThaumcraftMod.ARCANE_SPA_MENU.get(), id);
        this.spa = spa;
        this.data = data;
        this.pos = spa.getBlockPos();
        checkContainerDataCount(data, 3);
        addDataSlots(data);

        addSlot(new SlotItemHandler(spa.saltsHandler(), 0, 65, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ThaumcraftMod.BATH_SALTS.get());
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    private static ArcaneSpaBlockEntity resolve(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level.getBlockEntity(pos);
        if (blockEntity instanceof ArcaneSpaBlockEntity spa) {
            return spa;
        }
        throw new IllegalStateException("Missing Arcane Spa at " + pos);
    }

    public boolean isMixing() {
        return data.get(0) != 0;
    }

    public int fluidAmount() {
        return Math.max(0, Math.min(ArcaneSpaBlockEntity.CAPACITY, data.get(1)));
    }

    public int fluidCapacity() {
        return ArcaneSpaBlockEntity.CAPACITY;
    }

    public FluidStack fluidStack() {
        int amount = fluidAmount();
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = Registry.FLUID.byId(data.get(2));
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    public int scaledFluidHeight(int pixels) {
        return Math.max(0, Math.min(pixels, fluidAmount() * pixels / fluidCapacity()));
    }

    /** Exact TC4 GUI mask: (int)(pixels - pixels * amount/capacity). */
    public int emptyFluidMaskHeight(int pixels) {
        return com.darkifov.thaumcraft.block.TC4ArcaneSpaParity.emptyFluidMaskHeight(fluidAmount(), pixels);
    }

    public BlockPos blockPos() {
        return pos;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_MIX) {
            return false;
        }
        if (!player.level.isClientSide) {
            spa.toggleMixing();
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return spa.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < SPA_SLOT_COUNT) {
            if (!moveItemStackTo(stack, SPA_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ThaumcraftMod.BATH_SALTS.get())) {
            if (!moveItemStackTo(stack, 0, SPA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }
}
