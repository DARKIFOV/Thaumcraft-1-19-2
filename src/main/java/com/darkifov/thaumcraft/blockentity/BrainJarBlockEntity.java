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
import net.minecraft.util.Mth;
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
    public static final int MAX_XP = 2000;
    private static final AABB LOCAL_ABSORB_BOX = new AABB(-0.1D, -0.1D, -0.1D, 1.1D, 1.1D, 1.1D);

    private int xp;
    private int eatDelay;
    private float rotation;
    private float previousRotation;
    private int ambientSoundDelay = 100;

    public BrainJarBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.BRAIN_JAR_BLOCK_ENTITY.get(), pos, state);
    }

    public int storedExperience() {
        return xp;
    }

    public float rotation(float partialTick) {
        float delta = wrapRadians(rotation - previousRotation);
        return previousRotation + delta * partialTick;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity brain) {
        brain.xp = Math.min(brain.xp, MAX_XP);
        ExperienceOrb closest = brain.xp < MAX_XP ? closestOrb(level, pos) : null;
        if (closest != null && brain.eatDelay == 0) {
            Vec3 center = Vec3.atCenterOf(pos);
            double dx = (center.x - closest.getX()) / 7.0D;
            double dy = (center.y - closest.getY()) / 7.0D;
            double dz = (center.z - closest.getZ()) / 7.0D;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double pull = 1.0D - distance;
            if (pull > 0.0D && distance > 1.0E-5D) {
                pull *= pull;
                Vec3 motion = closest.getDeltaMovement().add(
                        dx / distance * pull * 0.15D,
                        dy / distance * pull * 0.33D,
                        dz / distance * pull * 0.15D);
                closest.setDeltaMovement(motion);
                closest.hasImpulse = true;
            }
        }

        if (brain.eatDelay > 0) {
            brain.eatDelay--;
            return;
        }
        if (brain.xp >= MAX_XP) {
            return;
        }

        List<ExperienceOrb> touching = level.getEntitiesOfClass(ExperienceOrb.class, LOCAL_ABSORB_BOX.move(pos));
        if (touching.isEmpty()) {
            return;
        }
        for (ExperienceOrb orb : touching) {
            brain.xp += orb.getValue();
            level.playSound(null, orb.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS,
                    0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
            orb.discard();
        }
        brain.xp = Math.min(brain.xp, MAX_XP);
        brain.setChangedAndSync();
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity brain) {
        brain.previousRotation = brain.rotation;
        ExperienceOrb orb = closestOrb(level, pos);
        Player target = orb == null ? level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 6.0D, false) : null;
        double tx = orb != null ? orb.getX() : target != null ? target.getX() : pos.getX() + 0.5D;
        double tz = orb != null ? orb.getZ() : target != null ? target.getZ() : pos.getZ() + 0.5D;
        float desired = orb != null || target != null
                ? (float) Math.atan2(tz - (pos.getZ() + 0.5D), tx - (pos.getX() + 0.5D))
                : brain.rotation + 0.01F;
        brain.rotation += wrapRadians(desired - brain.rotation) * 0.04F;

        if (target != null && --brain.ambientSoundDelay <= 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    TC4Sounds.event("brain"), SoundSource.BLOCKS, 0.15F,
                    0.8F + level.random.nextFloat() * 0.4F, false);
            brain.ambientSoundDelay = 100 + level.random.nextInt(500);
        }
    }

    /**
     * Forge 1.19.2 / Mojang 1.19.2 exposes degree wrapping in {@link Mth}, but not
     * the later {@code wrapRadians} helper. Keep the renderer in radians and
     * normalize to [-PI, PI) without converting back and forth to degrees.
     */
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
        AABB search = new AABB(pos).inflate(6.0D);
        return level.getEntitiesOfClass(ExperienceOrb.class, search).stream()
                .min(Comparator.comparingDouble(orb -> orb.distanceToSqr(Vec3.atCenterOf(pos))))
                .orElse(null);
    }

    public void releaseRandomExperience(ServerLevel level) {
        eatDelay = 40;
        int amount = level.random.nextInt(Math.min(xp + 1, 64));
        if (amount > 0) {
            xp -= amount;
            ExperienceOrb.award(level, Vec3.atCenterOf(worldPosition), amount);
            setChangedAndSync();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void releaseAllExperience(ServerLevel level) {
        if (xp > 0) {
            ExperienceOrb.award(level, Vec3.atCenterOf(worldPosition), xp);
            xp = 0;
            setChanged();
        }
    }

    public int comparatorOutput() {
        return Mth.floor((xp / (float) MAX_XP) * 14.0F) + (xp > 0 ? 1 : 0);
    }

    public void playJarSound() {
        if (level != null) {
            level.playLocalSound(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, TC4Sounds.event("jar"), SoundSource.BLOCKS,
                    0.2F, 1.0F, false);
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
        tag.putInt("EatDelay", eatDelay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        xp = Mth.clamp(tag.getInt("XP"), 0, MAX_XP);
        eatDelay = Math.max(0, tag.getInt("EatDelay"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
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
