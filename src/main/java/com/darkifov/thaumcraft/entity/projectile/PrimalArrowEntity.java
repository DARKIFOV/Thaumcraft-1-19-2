package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.19.2 adaptation of TC4 {@code EntityPrimalArrow}.
 *
 * <p>Type order is the original metadata order: air, fire, water, earth,
 * order and entropy. The entity keeps vanilla arrow collision/enchantment
 * behavior while restoring TC4 damage multipliers, fire and potion effects.</p>
 */
public final class PrimalArrowEntity extends Arrow {
    private static final EntityDataAccessor<Byte> PRIMAL_TYPE =
            SynchedEntityData.defineId(PrimalArrowEntity.class, EntityDataSerializers.BYTE);

    private static final int[] COLORS = {
            0xFFFF7E, // air
            0xFF3C01, // fire
            0x0090FF, // water
            0x00A000, // earth
            0xEEDC7F, // order
            0x555577  // entropy
    };

    public PrimalArrowEntity(EntityType<? extends PrimalArrowEntity> type, Level level) {
        super(type, level);
        setBaseDamage(2.1D);
    }

    public PrimalArrowEntity(Level level, LivingEntity shooter, int type) {
        this(ThaumcraftMod.PRIMAL_ARROW.get(), level);
        setOwner(shooter);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        setPrimalType(type);
        applyTypeDamageMultiplier();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PRIMAL_TYPE, (byte) 0);
    }

    public int getPrimalType() {
        return entityData.get(PRIMAL_TYPE) & 0xFF;
    }

    public void setPrimalType(int type) {
        entityData.set(PRIMAL_TYPE, (byte) Math.max(0, Math.min(5, type)));
    }

    private void applyTypeDamageMultiplier() {
        switch (getPrimalType()) {
            case 3 -> setBaseDamage(getBaseDamage() * 1.5D);
            case 4, 5 -> setBaseDamage(getBaseDamage() * 0.8D);
            default -> { }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        int type = getPrimalType();
        if (type == 1 && !(target instanceof net.minecraft.world.entity.monster.EnderMan)) {
            target.setSecondsOnFire(isOnFire() ? 10 : 5);
        }

        super.onHitEntity(hit);

        if (target instanceof LivingEntity living) {
            switch (type) {
                case 2 -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4), getEffectSource());
                case 4 -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 4), getEffectSource());
                case 5 -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), getEffectSource());
                default -> { }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && !inGround) {
            int color = COLORS[Math.min(getPrimalType(), COLORS.length - 1)];
            Vector3f rgb = new Vector3f(
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F);
            level.addParticle(new DustParticleOptions(rgb, 0.75F),
                    getX() - getDeltaMovement().x * 0.35D,
                    getY() - getDeltaMovement().y * 0.35D,
                    getZ() - getDeltaMovement().z * 0.35D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        String id = switch (getPrimalType()) {
            case 1 -> "tc4_el_arrow_fire";
            case 2 -> "tc4_el_arrow_water";
            case 3 -> "tc4_el_arrow_earth";
            case 4 -> "tc4_el_arrow_order";
            case 5 -> "tc4_el_arrow_entropy";
            default -> "tc4_el_arrow_air";
        };
        return TC4ResearchItems.registered(id)
                .map(item -> new ItemStack(item.get()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("TC4PrimalType", (byte) getPrimalType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPrimalType(tag.getByte("TC4PrimalType") & 0xFF);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
