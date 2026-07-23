package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/** Exact gameplay port of TC4 TileJarBrain, adapted to the 1.19.2 BlockEntity lifecycle. */
public final class BrainJarBlockEntity extends BlockEntity {
    public static final int MAX_XP = TC4BrainJarParity.MAX_XP;
    private static final AABB LOCAL_ABSORB_BOX = new AABB(
            TC4BrainJarParity.TOUCH_MIN, TC4BrainJarParity.TOUCH_MIN, TC4BrainJarParity.TOUCH_MIN,
            TC4BrainJarParity.TOUCH_MAX, TC4BrainJarParity.TOUCH_MAX, TC4BrainJarParity.TOUCH_MAX);

    private int xp;
    private int eatDelay;
    private float rotation;
    private float previousRotation;
    private long nextAmbientSoundAtMillis = System.currentTimeMillis()
            + TC4BrainJarParity.AMBIENT_INITIAL_DELAY_MILLIS;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.BRAIN_JAR_BLOCK_ENTITY.get(), pos, state);
    }

    public int storedExperience() {
        return xp;
    }

    public int eatDelayTicks() {
        return eatDelay;
    }

    public float rotation(float partialTick) {
        float delta = wrapRadians(rotation - previousRotation);
        return previousRotation + delta * partialTick;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity brain) {
        int clamped = TC4BrainJarParity.clampAtTickStart(brain.xp);
        if (clamped != brain.xp) {
            brain.xp = clamped;
            brain.setChangedAndSync();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        ExperienceOrb closest = TC4BrainJarParity.mayAttract(brain.xp, brain.eatDelay)
                ? closestOrb(level, pos) : null;
        if (closest != null) {
            Vec3 center = Vec3.atCenterOf(pos);
            TC4BrainJarParity.Pull pull = TC4BrainJarParity.pull(
                    center.x, center.y, center.z, closest.getX(), closest.getY(), closest.getZ());
            if (pull != TC4BrainJarParity.Pull.ZERO) {
                closest.setDeltaMovement(closest.getDeltaMovement().add(pull.x(), pull.y(), pull.z()));
                closest.hasImpulse = true;
            }
        }

        if (brain.eatDelay > 0) {
            brain.eatDelay--;
            return;
        }
        if (!TC4BrainJarParity.mayAbsorb(brain.xp, brain.eatDelay)) {
            return;
        }

        List<ExperienceOrb> touching = level.getEntitiesOfClass(ExperienceOrb.class, LOCAL_ABSORB_BOX.move(pos));
        if (touching.isEmpty()) {
            return;
        }
        for (ExperienceOrb orb : touching) {
            brain.xp += orb.getValue();
            level.playSound(null, orb.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS,
                    TC4BrainJarParity.EAT_VOLUME,
                    (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
            orb.discard();
        }
        // TC4 intentionally allows this tick to overflow 2000. The next tick-start clamp fixes it.
        brain.setChangedAndSync();
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity brain) {
        brain.previousRotation = brain.rotation;
        ExperienceOrb orb = brain.xp < MAX_XP ? closestOrb(level, pos) : null;
        Player target = orb == null ? level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, TC4BrainJarParity.SEARCH_RADIUS, false) : null;

        if (target != null && brain.nextAmbientSoundAtMillis < System.currentTimeMillis()) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    TC4Sounds.event("brain"), SoundSource.BLOCKS, TC4BrainJarParity.AMBIENT_VOLUME,
                    TC4BrainJarParity.AMBIENT_PITCH_BASE
                            + level.random.nextFloat() * TC4BrainJarParity.AMBIENT_PITCH_RANGE,
                    false);
            brain.nextAmbientSoundAtMillis = System.currentTimeMillis()
                    + TC4BrainJarParity.AMBIENT_BASE_DELAY_MILLIS
                    + level.random.nextInt(TC4BrainJarParity.AMBIENT_RANDOM_BOUND_MILLIS);
        }

        if (orb != null || target != null) {
            double tx = orb != null ? orb.getX() : target.getX();
            double tz = orb != null ? orb.getZ() : target.getZ();
            float desired = (float) Math.atan2(tz - (pos.getZ() + 0.5D), tx - (pos.getX() + 0.5D));
            brain.rotation += wrapRadians(desired - brain.rotation) * TC4BrainJarParity.ROTATION_LERP;
        } else {
            brain.rotation += TC4BrainJarParity.IDLE_ROTATION_STEP;
        }
        brain.rotation = wrapRadians(brain.rotation);
    }

    private static float wrapRadians(float angle) {
        final float pi = (float) Math.PI;
        final float twoPi = pi * 2.0F;
        angle %= twoPi;
        if (angle >= pi) {
            angle -= twoPi;
        } else if (angle < -pi) {
            angle += twoPi;
        }
        return angle;
    }

    private static ExperienceOrb closestOrb(Level level, BlockPos pos) {
        AABB search = new AABB(pos).inflate(TC4BrainJarParity.SEARCH_RADIUS);
        return level.getEntitiesOfClass(ExperienceOrb.class, search).stream()
                .min(Comparator.comparingDouble(orb -> orb.distanceToSqr(Vec3.atCenterOf(pos))))
                .orElse(null);
    }

    public void releaseRandomExperience(ServerLevel level) {
        eatDelay = TC4BrainJarParity.INTERACTION_EAT_DELAY_TICKS;
        int amount = level.random.nextInt(TC4BrainJarParity.randomReleaseBound(xp));
        if (amount > 0) {
            xp -= amount;
            spawnExperience(level, Vec3.atCenterOf(worldPosition), amount);
            setChangedAndSync();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void releaseAllExperience(ServerLevel level) {
        if (xp > 0) {
            spawnExperience(level, new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), xp);
            xp = 0;
            setChanged();
        }
    }

    private static void spawnExperience(ServerLevel level, Vec3 position, int total) {
        int remaining = total;
        while (remaining > 0) {
            int split = ExperienceOrb.getExperienceValue(remaining);
            remaining -= split;
            level.addFreshEntity(new ExperienceOrb(level, position.x, position.y, position.z, split));
        }
    }

    public int comparatorOutput() {
        return TC4BrainJarParity.comparatorOutput(xp);
    }

    public void playJarSound() {
        if (level != null) {
            level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, TC4Sounds.event("jar"), SoundSource.BLOCKS,
                    TC4BrainJarParity.JAR_SHAKE_VOLUME, TC4BrainJarParity.JAR_SHAKE_PITCH, false);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("XP", xp);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        xp = tag.getInt("XP");
        // eatDelay was transient in TC4. Ignore legacy port tags and reset after reload.
        eatDelay = 0;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("XP", xp);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
