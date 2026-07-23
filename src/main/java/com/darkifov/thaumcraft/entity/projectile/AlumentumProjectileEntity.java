package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Forge 1.19.2 adapter for TC4 {@code EntityAlumentum}.
 *
 * <p>The original projectile launches at 0.75 velocity, leaves bright wisps and
 * explodes with strength 1.66. Block damage follows the mobGriefing gamerule.</p>
 */
public final class AlumentumProjectileEntity extends ThrowableItemProjectile {
    public AlumentumProjectileEntity(EntityType<? extends AlumentumProjectileEntity> type, Level level) {
        super(type, level);
    }

    public AlumentumProjectileEntity(EntityType<? extends AlumentumProjectileEntity> type,
                                     LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    public AlumentumProjectileEntity(EntityType<? extends AlumentumProjectileEntity> type,
                                     Level level, double x, double y, double z) {
        super(type, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_alumentum").get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            for (int i = 0; i < 3; i++) {
                double ox = (random.nextDouble() - random.nextDouble()) * 0.16D;
                double oy = (random.nextDouble() - random.nextDouble()) * 0.16D;
                double oz = (random.nextDouble() - random.nextDouble()) * 0.16D;
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX() + ox, getY() + oy, getZ() + oz,
                        0.0D, 0.015D, 0.0D);
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        (getX() + xo) * 0.5D + ox,
                        (getY() + yo) * 0.5D + oy,
                        (getZ() + zo) * 0.5D + oz,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level.isClientSide) {
            boolean grief = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            level.explode(null, getX(), getY(), getZ(), 1.66F,
                    grief ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TC4Original", "EntityAlumentum");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
