package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Dedicated projectile for the original golem dart-launcher decoration. */
public final class GolemDartEntity extends Arrow {
    private boolean emittedLaunchSmoke;

    public GolemDartEntity(EntityType<? extends GolemDartEntity> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
    }

    public GolemDartEntity(Level level, LivingEntity shooter) {
        this(ThaumcraftMod.GOLEM_DART.get(), level);
        setOwner(shooter);
        setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && !emittedLaunchSmoke) {
            emittedLaunchSmoke = true;
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.SMOKE,
                        getX() - getDeltaMovement().x / 1.5D,
                        getY() - getDeltaMovement().y / 1.5D,
                        getZ() - getDeltaMovement().z / 1.5D,
                        getDeltaMovement().x / 9.0D + random.nextGaussian() * 0.01D,
                        getDeltaMovement().y / 9.0D + random.nextGaussian() * 0.01D,
                        getDeltaMovement().z / 9.0D + random.nextGaussian() * 0.01D);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TC4Original", "EntityDart");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
