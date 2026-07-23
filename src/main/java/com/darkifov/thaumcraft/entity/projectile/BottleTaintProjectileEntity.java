package com.darkifov.thaumcraft.entity.projectile;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TaintFibresBlock;
import com.darkifov.thaumcraft.entity.TaintedMob;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/** Forge 1.19.2 adapter for TC4 {@code EntityBottleTaint}. */
public final class BottleTaintProjectileEntity extends ThrowableItemProjectile {
    public BottleTaintProjectileEntity(EntityType<? extends BottleTaintProjectileEntity> type, Level level) {
        super(type, level);
    }

    public BottleTaintProjectileEntity(EntityType<? extends BottleTaintProjectileEntity> type,
                                       LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    public BottleTaintProjectileEntity(EntityType<? extends BottleTaintProjectileEntity> type,
                                       Level level, double x, double y, double z) {
        super(type, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_bottle_taint").get();
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level instanceof ServerLevel server)) return;

        AABB area = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(5.0D);
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living instanceof TaintedMob || living.getMobType() == MobType.UNDEAD) continue;
            living.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), 100, 0, false, true, true));
        }

        BlockPos center = new BlockPos(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));
        for (int i = 0; i < 10; i++) {
            int dx = (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
            int dz = (int) ((random.nextFloat() - random.nextFloat()) * 5.0F);
            BlockPos column = center.offset(dx, 0, dz);
            if (!random.nextBoolean() || TaintSpreadRuntime.isColumnTainted(server, column)) continue;

            TaintSpreadRuntime.markTaintedColumn(server, column);
            BlockState below = server.getBlockState(column.below());
            BlockState at = server.getBlockState(column);
            if (!below.getCollisionShape(server, column.below()).isEmpty()
                    && (at.isAir() || at.getMaterial().isReplaceable())) {
                server.setBlock(column,
                        ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState().setValue(TaintFibresBlock.AGE, 0), 3);
            }
        }

        server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 100,
                0.9D, 0.7D, 0.9D, 0.08D);
        server.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY(), getZ(), 32,
                0.55D, 0.45D, 0.55D, 0.12D);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("TC4Original", "EntityBottleTaint");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
