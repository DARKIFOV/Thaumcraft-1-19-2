package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Exact 27-slot TC4 Hungry Chest inventory, eating event and lid lifecycle. */
public class HungryChestBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SIZE = TC4HungryChestParity.INVENTORY_SIZE;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final InvWrapper forgeItemHandler = new InvWrapper(this);
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> forgeItemHandler);

    private int openCount;
    private float lidAngle;
    private float previousLidAngle;

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.HUNGRY_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.tickAnimation(true);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        chest.tickAnimation(false);
    }

    private void tickAnimation(boolean playSounds) {
        previousLidAngle = lidAngle;
        if (TC4HungryChestParity.shouldPlayOpenSound(openCount, lidAngle) && playSounds && level != null) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
                    TC4HungryChestParity.CHEST_SOUND_VOLUME,
                    TC4HungryChestParity.chestSoundPitch(level.random.nextFloat()));
        }

        float oldAngle = lidAngle;
        lidAngle = TC4HungryChestParity.nextLidAngle(lidAngle, openCount);
        if (TC4HungryChestParity.shouldPlayCloseSound(oldAngle, lidAngle) && playSounds && level != null) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
                    TC4HungryChestParity.CHEST_SOUND_VOLUME,
                    TC4HungryChestParity.chestSoundPitch(level.random.nextFloat()));
        }
    }

    public float lidAngle(float partialTick) {
        return previousLidAngle + (lidAngle - previousLidAngle) * partialTick;
    }

    public int openCount() {
        return openCount;
    }

    /** Inserts as much of the dropped stack as possible and updates the entity exactly once. */
    public void eat(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || itemEntity.getItem().isEmpty()) {
            return;
        }
        ItemStack original = itemEntity.getItem();
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(forgeItemHandler, original.copy(), false);
        int moved = original.getCount() - remainder.getCount();
        if (moved <= 0) {
            return;
        }

        level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, TC4HungryChestParity.EAT_SOUND_VOLUME,
                TC4HungryChestParity.eatSoundPitch(level.random.nextFloat(), level.random.nextFloat()));
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

    private void triggerEatAnimation() {
        lidAngle = Math.max(lidAngle, TC4HungryChestParity.EAT_LID_NUDGE);
        if (level != null) {
            level.blockEvent(worldPosition, getBlockState().getBlock(),
                    TC4HungryChestParity.EAT_EVENT_ID, TC4HungryChestParity.EAT_EVENT_DATA);
        }
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == TC4HungryChestParity.OPENERS_EVENT_ID) {
            openCount = data;
            return true;
        }
        if (id == TC4HungryChestParity.EAT_EVENT_ID) {
            float eventAngle = data / 10.0F;
            if (lidAngle < eventAngle) {
                lidAngle = eventAngle;
            }
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
        return ChestMenu.threeRows(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public int getMaxStackSize() {
        return TC4HungryChestParity.MAX_STACK_SIZE;
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

    /** TC4 only checked that the same tile entity still occupied the position. */
    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void startOpen(Player player) {
        if (level == null) {
            return;
        }
        openCount += 1;
        level.blockEvent(worldPosition, getBlockState().getBlock(),
                TC4HungryChestParity.OPENERS_EVENT_ID, openCount);
    }

    @Override
    public void stopOpen(Player player) {
        if (level == null) {
            return;
        }
        openCount -= 1;
        level.blockEvent(worldPosition, getBlockState().getBlock(),
                TC4HungryChestParity.OPENERS_EVENT_ID, openCount);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
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
        itemHandler = LazyOptional.of(() -> forgeItemHandler);
    }
}
