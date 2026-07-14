package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.HungryChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GenericContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** 27-slot TileChestHungry port with lid/eating animation and hopper capability. */
public class HungryChestBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SIZE = 27;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    private int openCount;
    private float lidAngle;
    private float previousLidAngle;

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.HUNGRY_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.tickAnimation();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.tickAnimation();
    }

    private void tickAnimation() {
        previousLidAngle = lidAngle;
        float target = openCount > 0 ? 1.0F : 0.0F;
        float speed = 0.10F;
        if (lidAngle < target) {
            lidAngle = Math.min(target, lidAngle + speed);
        } else if (lidAngle > target) {
            lidAngle = Math.max(target, lidAngle - speed);
        }
    }

    public float lidAngle(float partialTick) {
        return previousLidAngle + (lidAngle - previousLidAngle) * partialTick;
    }

    /** Inserts as much of the dropped stack as possible and updates the entity. */
    public void eat(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || itemEntity.getItem().isEmpty()) {
            return;
        }
        ItemStack original = itemEntity.getItem();
        ItemStack remainder = insert(original.copy());
        int moved = original.getCount() - remainder.getCount();
        if (moved <= 0) {
            return;
        }

        level.playSound(null, itemEntity.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.BLOCKS, 0.25F,
                0.9F + level.random.nextFloat() * 0.2F);
        triggerEatAnimation();
        if (remainder.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remainder);
        }
        inventoryChanged();
    }

    private void inventoryChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private ItemStack insert(ItemStack offered) {
        if (offered.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = offered.copy();

        for (int slot = 0; slot < items.size() && !remainder.isEmpty(); slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, remainder)) {
                continue;
            }
            int room = Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getCount();
            if (room <= 0) {
                continue;
            }
            int moved = Math.min(room, remainder.getCount());
            existing.grow(moved);
            remainder.shrink(moved);
        }

        for (int slot = 0; slot < items.size() && !remainder.isEmpty(); slot++) {
            if (!items.get(slot).isEmpty()) {
                continue;
            }
            int moved = Math.min(remainder.getCount(), Math.min(remainder.getMaxStackSize(), getMaxStackSize()));
            ItemStack inserted = remainder.copy();
            inserted.setCount(moved);
            items.set(slot, inserted);
            remainder.shrink(moved);
        }
        return remainder;
    }

    private void triggerEatAnimation() {
        // TC4 sends block event (2, 2), which nudges the lid to 0.2 and lets
        // the normal chest tick close it again instead of holding it open.
        lidAngle = Math.max(lidAngle, 0.2F);
        if (level != null) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 2);
        }
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == 1) {
            openCount = Math.max(0, data);
            return true;
        }
        if (id == 2) {
            lidAngle = Math.max(lidAngle, Math.max(0, data) / 10.0F);
            return true;
        }
        return super.triggerEvent(id, data);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumcraft.hungry_chest");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return GenericContainerMenu.threeRows(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            inventoryChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        inventoryChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void startOpen(Player player) {
        if (player.isSpectator() || level == null) {
            return;
        }
        openCount++;
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
        if (openCount == 1) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 0.95F);
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (player.isSpectator() || level == null) {
            return;
        }
        openCount = Math.max(0, openCount - 1);
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
        if (openCount == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.95F);
        }
    }

    @Override
    public void clearContent() {
        items.clear();
        inventoryChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    }
}
