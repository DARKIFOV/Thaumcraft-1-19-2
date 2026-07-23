package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
import com.darkifov.thaumcraft.porting.TC4LegacyStackMigrationTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Exact one-slot TC4 TilePedestal inventory, automation and persistence contract. */
public class ArcanePedestalBlockEntity extends BlockEntity implements TC4LegacyStackMigrationTarget {
    private ItemStack stored = ItemStack.EMPTY;
    private String customName = "";

    private final IItemHandler sidedInventory = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            return stored;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            validateSlot(slot);
            if (stack.isEmpty() || !stored.isEmpty()) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            ItemStack one = remainder.split(1);
            if (!simulate) {
                setStored(one);
            }
            return remainder;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            validateSlot(slot);
            if (amount <= 0 || stored.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, stored.getCount());
            ItemStack result = stored.copy();
            result.setCount(extracted);
            if (!simulate) {
                stored.shrink(extracted);
                if (stored.isEmpty()) stored = ItemStack.EMPTY;
                setChangedAndSync();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            validateSlot(slot);
            // TilePedestal#isItemValidForSlot always returns true. Whether the
            // slot is currently empty is handled by canInsert/insertItem.
            return true;
        }

        private void validateSlot(int slot) {
            if (slot != 0) {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0,1)");
            }
        }
    };
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> sidedInventory);

    public ArcanePedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack stored() {
        return stored;
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isBlank();
    }

    public String customName() {
        return hasCustomName() ? customName : "container.pedestal";
    }

    public void setCustomName(String name) {
        customName = name == null ? "" : name;
        setChangedAndSync();
    }

    public void setStored(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            stored = ItemStack.EMPTY;
        } else {
            stored = stack.copy();
            stored.setCount(1);
        }
        setChangedAndSync();
    }

    /** Original TilePedestal#setInventorySlotContentsFromInfusion bypasses the normal slot limit. */
    public void setStoredFromInfusion(ItemStack stack) {
        stored = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        setChangedAndSync();
    }

    public void sendOriginalBlockEvent(int eventId) {
        if (level != null && !level.isClientSide) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), eventId, 0);
        }
    }

    public ItemStack takeStored() {
        ItemStack result = stored.copy();
        stored = ItemStack.EMPTY;
        setChangedAndSync();
        return result;
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    @Override
    public int migrateLegacyStacks() {
        TC4LegacyDuplicateItemMigrator.MigrationResult result =
                TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus(stored);
        if (!result.changed()) {
            return 0;
        }
        stored = result.stack();
        setChangedAndSync();
        return result.changedStacks();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag items = new ListTag();
        if (!stored.isEmpty()) {
            CompoundTag item = new CompoundTag();
            item.putByte(TC4InfusionAltarFullClosureParity.PEDESTAL_SLOT_NBT, (byte) 0);
            stored.save(item);
            items.add(item);
        }
        tag.put(TC4InfusionAltarFullClosureParity.PEDESTAL_ITEMS_NBT, items);
        if (hasCustomName()) {
            tag.putString(TC4InfusionAltarFullClosureParity.PEDESTAL_CUSTOM_NAME_NBT, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        stored = ItemStack.EMPTY;
        customName = tag.contains(TC4InfusionAltarFullClosureParity.PEDESTAL_CUSTOM_NAME_NBT)
                ? tag.getString(TC4InfusionAltarFullClosureParity.PEDESTAL_CUSTOM_NAME_NBT) : "";

        if (tag.contains(TC4InfusionAltarFullClosureParity.PEDESTAL_ITEMS_NBT, Tag.TAG_LIST)) {
            ListTag items = tag.getList(TC4InfusionAltarFullClosureParity.PEDESTAL_ITEMS_NBT, Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag item = items.getCompound(i);
                if (item.getByte(TC4InfusionAltarFullClosureParity.PEDESTAL_SLOT_NBT) == 0) {
                    stored = ItemStack.of(item);
                    break;
                }
            }
        } else if (tag.contains("Stored", Tag.TAG_COMPOUND)) {
            // One-time migration from the pre-v11.64.34 port-only layout.
            stored = ItemStack.of(tag.getCompound("Stored"));
        }

    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (level != null && level.isClientSide && (id == 11 || id == 12)) {
            int count = id == 12 ? 10 : 5;
            for (int i = 0; i < count; i++) {
                double x = worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.75D;
                double y = worldPosition.getY() + 0.9D + level.random.nextDouble() * 0.5D;
                double z = worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.75D;
                level.addParticle(id == 12 ? net.minecraft.core.particles.ParticleTypes.ENCHANT
                                : net.minecraft.core.particles.ParticleTypes.WITCH,
                        x, y, z, 0.0D, 0.02D, 0.0D);
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            load(packet.getTag());
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> sidedInventory);
    }
}
