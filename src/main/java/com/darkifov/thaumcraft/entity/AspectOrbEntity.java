package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;

/**
 * Forge 1.19.2 port of TC4 EntityAspectOrb.
 *
 * The orb is emitted by unstable aura nodes, lives for 150 ticks, seeks a
 * player with a compatible wand in the hotbar, and transfers its primal vis
 * directly into that wand when collected.
 */
public class AspectOrbEntity extends Entity {
    private static final EntityDataAccessor<String> ASPECT_ID =
            SynchedEntityData.defineId(AspectOrbEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ASPECT_VALUE =
            SynchedEntityData.defineId(AspectOrbEntity.class, EntityDataSerializers.INT);

    public static final int MAX_AGE = 150;
    private static final double SEEK_RANGE = 8.0D;

    private int orbAge;
    private int orbCooldown;
    private int orbHealth = 5;
    private int closestPlayerId = -1;

    public AspectOrbEntity(EntityType<? extends AspectOrbEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = false;
    }

    public AspectOrbEntity(EntityType<? extends AspectOrbEntity> type, Level level,
                           double x, double y, double z, Aspect aspect, int value) {
        this(type, level);
        setPos(x, y, z);
        setYRot(random.nextFloat() * 360.0F);
        setDeltaMovement(
                (random.nextDouble() * 0.2D - 0.1D) * 2.0D,
                random.nextDouble() * 0.2D * 2.0D,
                (random.nextDouble() * 0.2D - 0.1D) * 2.0D
        );
        setAspect(aspect);
        setAspectValue(value);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ASPECT_ID, Aspect.AER.id());
        entityData.define(ASPECT_VALUE, 1);
    }

    public Aspect getAspect() {
        Aspect aspect = Aspect.byId(entityData.get(ASPECT_ID));
        return aspect == null ? Aspect.AER : aspect;
    }

    public void setAspect(Aspect aspect) {
        entityData.set(ASPECT_ID, aspect == null ? Aspect.AER.id() : aspect.id());
    }

    public int getAspectValue() {
        return Math.max(1, entityData.get(ASPECT_VALUE));
    }

    public void setAspectValue(int value) {
        entityData.set(ASPECT_VALUE, Math.max(1, value));
    }

    public int orbAge() {
        return orbAge;
    }

    public int orbCooldown() {
        return orbCooldown;
    }

    public void setOrbCooldown(int ticks) {
        orbCooldown = Math.max(0, ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (orbCooldown > 0) {
            orbCooldown--;
        }

        Vec3 motion = getDeltaMovement().add(0.0D, -0.03D, 0.0D);
        if (isInLava()) {
            motion = new Vec3(
                    (random.nextFloat() - random.nextFloat()) * 0.2F,
                    0.2D,
                    (random.nextFloat() - random.nextFloat()) * 0.2F
            );
            level.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.NEUTRAL, 0.4F, 2.0F + random.nextFloat() * 0.4F);
        }

        // Direct Forge 1.19.2 equivalent of EntityAspectOrb#pushOutOfBlocks.
        AABB bounds = getBoundingBox();
        moveTowardsClosestSpace(getX(), (bounds.minY + bounds.maxY) * 0.5D, getZ());

        Player target = resolveTarget();
        if (target != null) {
            double dx = (target.getX() - getX()) / SEEK_RANGE;
            double dy = (target.getEyeY() - getY()) / SEEK_RANGE;
            double dz = (target.getZ() - getZ()) / SEEK_RANGE;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double attraction = 1.0D - distance;
            if (attraction > 0.0D && distance > 1.0E-6D) {
                attraction *= attraction;
                motion = motion.add(
                        dx / distance * attraction * 0.1D,
                        dy / distance * attraction * 0.1D,
                        dz / distance * attraction * 0.1D
                );
            }
        }

        setDeltaMovement(motion);
        move(MoverType.SELF, getDeltaMovement());

        float friction = 0.98F;
        if (onGround) {
            friction = level.getBlockState(blockPosition().below()).getFriction(level, blockPosition().below(), this) * 0.98F;
        }
        setDeltaMovement(getDeltaMovement().multiply(friction, 0.98D, friction));
        if (onGround) {
            setDeltaMovement(getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        }

        orbAge++;
        if (orbAge >= MAX_AGE) {
            discard();
        }
    }

    private Player resolveTarget() {
        if (level.isClientSide) {
            Entity target = closestPlayerId < 0 ? null : level.getEntity(closestPlayerId);
            return target instanceof Player player ? player : null;
        }

        if (tickCount % 5 == 0) {
            Player current = null;
            if (closestPlayerId >= 0) {
                Entity entity = level.getEntity(closestPlayerId);
                if (entity instanceof Player player && player.isAlive() && distanceToSqr(player) <= SEEK_RANGE * SEEK_RANGE
                        && findHotbarWandWithRoom(player) >= 0) {
                    current = player;
                }
            }
            if (current == null) {
                List<Player> candidates = level.getEntitiesOfClass(Player.class,
                        getBoundingBox().inflate(SEEK_RANGE),
                        player -> player.isAlive() && !player.isSpectator() && findHotbarWandWithRoom(player) >= 0);
                current = candidates.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            }
            closestPlayerId = current == null ? -1 : current.getId();
        }

        Entity target = closestPlayerId < 0 ? null : level.getEntity(closestPlayerId);
        return target instanceof Player player ? player : null;
    }

    private int findHotbarWandWithRoom(Player player) {
        Aspect aspect = getAspect();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof WandItem wand) || WandItem.hasInfiniteVis(stack)) {
                continue;
            }
            int current = WandItem.getVis(stack, aspect);
            if (current < wand.stackVisCapacity(stack)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public void playerTouch(Player player) {
        if (level.isClientSide || orbCooldown > 0 || player.takeXpDelay > 0 || !getAspect().isPrimal()) {
            return;
        }
        int slot = findHotbarWandWithRoom(player);
        if (slot < 0) {
            return;
        }
        ItemStack wand = player.getInventory().getItem(slot);
        WandItem.addVis(wand, getAspect(), getAspectValue());
        player.takeXpDelay = 2;
        player.take(this, 1);
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.1F, 0.5F * ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.8F));
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        markHurt();
        orbHealth -= Mth.ceil(amount);
        if (!level.isClientSide && orbHealth <= 0) {
            discard();
        }
        return false;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Health", orbHealth);
        tag.putInt("Age", orbAge);
        tag.putInt("Value", getAspectValue());
        tag.putString("Aspect", getAspect().id());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        orbHealth = tag.contains("Health") ? Math.max(1, tag.getInt("Health")) : 5;
        orbAge = Math.max(0, tag.getInt("Age"));
        setAspectValue(tag.contains("Value") ? tag.getInt("Value") : 1);
        setAspect(Aspect.byId(tag.getString("Aspect")));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
