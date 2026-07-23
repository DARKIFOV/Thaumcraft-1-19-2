package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.19.2 port of TC4 {@code EntitySpecialItem}.
 *
 * <p>The original entity cancels vanilla item gravity by adding +0.04 before
 * the normal item tick, damps only upward movement, uses a small 0.25 cube and
 * ignores explosion damage. It is used for crucible products, the first
 * Thaumonomicon and boss/key rewards that must not be destroyed by the effect
 * which created them.</p>
 */
public class SpecialItemEntity extends ItemEntity {
    public SpecialItemEntity(EntityType<? extends SpecialItemEntity> type, Level level) {
        super(type, level);
    }

    public SpecialItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(ThaumcraftMod.SPECIAL_ITEM.get(), level);
        setPos(x, y, z);
        setItem(stack.copy());
        setYRot((float) (random.nextDouble() * 360.0D));
        setDeltaMovement((random.nextDouble() * 0.2D) - 0.1D,
                0.2D,
                (random.nextDouble() * 0.2D) - 0.1D);
    }

    protected SpecialItemEntity(EntityType<? extends SpecialItemEntity> type, Level level,
                                double x, double y, double z, ItemStack stack) {
        this(type, level);
        setPos(x, y, z);
        setItem(stack.copy());
        setYRot((float) (random.nextDouble() * 360.0D));
        setDeltaMovement((random.nextDouble() * 0.2D) - 0.1D,
                0.2D,
                (random.nextDouble() * 0.2D) - 0.1D);
    }

    @Override
    public void tick() {
        Vec3 motion = getDeltaMovement();
        if (motion.y > 0.0D) {
            setDeltaMovement(motion.x, motion.y * 0.9D, motion.z);
        }
        // ItemEntity applies the matching -0.04 gravity during super.tick().
        setDeltaMovement(getDeltaMovement().add(0.0D, 0.04D, 0.0D));
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isExplosion()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TC4Original", "EntitySpecialItem");
    }
}
