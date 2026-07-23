package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4 {@code EntityPermanentItem}: a SpecialItem whose despawn age never wins. */
public final class PermanentItemEntity extends SpecialItemEntity {
    public PermanentItemEntity(EntityType<? extends PermanentItemEntity> type, Level level) {
        super(type, level);
        setUnlimitedLifetime();
    }

    public PermanentItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ThaumcraftMod.PERMANENT_ITEM.get(), level, x, y, z, stack);
        setUnlimitedLifetime();
    }

    @Override
    public void tick() {
        setUnlimitedLifetime();
        super.tick();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TC4PermanentItem", true);
        tag.putString("TC4Original", "EntityPermanentItem");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setUnlimitedLifetime();
    }
}
