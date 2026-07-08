package com.darkifov.thaumcraft.client.fx;

import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Client bridge for Stage216 boss/orb FX packets mirroring TC4 entity status bytes. */
public final class TC4ClientEldritchBossFx {
    public static final int ELDRITCH_ORB_BURST = 116;
    public static final int WARDEN_ARM_LEFT = 15;
    public static final int WARDEN_ARM_RIGHT = 16;
    public static final int WARDEN_FRENZY_RING = 17;
    public static final int GOLEM_SPAWN = 18;
    public static final int GOLEM_HEADLESS_ARC = 19;

    private TC4ClientEldritchBossFx() {
    }

    public static void show(int type, int entityId, double x, double y, double z, int bx, int by, int bz) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        Entity entity = entityId >= 0 ? level.getEntity(entityId) : null;
        double px = entity == null ? x : entity.getX();
        double py = entity == null ? y : entity.getY();
        double pz = entity == null ? z : entity.getZ();

        switch (type) {
            case ELDRITCH_ORB_BURST -> eldritchOrbBurst(level, px, py, pz);
            case WARDEN_ARM_LEFT, WARDEN_ARM_RIGHT -> wardenArmLift(level, entity, type == WARDEN_ARM_LEFT);
            case WARDEN_FRENZY_RING -> frenzyRing(level, px, py, pz, bx, by, bz);
            case GOLEM_SPAWN -> spawnSmoke(level, px, py, pz, entity == null ? 3.5F : entity.getBbHeight());
            case GOLEM_HEADLESS_ARC -> golemArc(level, entity, px, py, pz, bx, by, bz);
            default -> { }
        }
    }

    private static void eldritchOrbBurst(Level level, double x, double y, double z) {
        for (int i = 0; i < 30; i++) {
            double fx = (level.random.nextDouble() - level.random.nextDouble()) * 0.3D;
            double fy = (level.random.nextDouble() - level.random.nextDouble()) * 0.3D;
            double fz = (level.random.nextDouble() - level.random.nextDouble()) * 0.3D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x + fx, y + fy, z + fz, fx * 0.16D, fy * 0.16D, fz * 0.16D);
            level.addParticle(ParticleTypes.WITCH, x + fx, y + fy, z + fz, fx * 0.08D, fy * 0.08D, fz * 0.08D);
        }
    }

    private static void wardenArmLift(Level level, Entity entity, boolean left) {
        if (entity == null) {
            return;
        }
        if (entity instanceof EldritchWardenEntity warden) {
            warden.clientArmLift(left);
        }
        double side = left ? -0.55D : 0.55D;
        double yaw = Math.toRadians(entity.getYRot());
        double sx = entity.getX() + Math.cos(yaw) * side;
        double sz = entity.getZ() + Math.sin(yaw) * side;
        for (int i = 0; i < 16; i++) {
            level.addParticle(ParticleTypes.DRAGON_BREATH, sx, entity.getY() + entity.getBbHeight() * 0.65D, sz, 0.0D, 0.03D, 0.0D);
        }
    }

    private static void frenzyRing(Level level, double x, double y, double z, int bx, int by, int bz) {
        BlockPos pos = new BlockPos(bx, by, bz);
        for (int i = 0; i < 12; i++) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, (level.random.nextDouble() - 0.5D) * 0.04D, 0.08D, (level.random.nextDouble() - 0.5D) * 0.04D);
        }
        level.playLocalSound(x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.2F, 1.8F, false);
    }

    private static void spawnSmoke(Level level, double x, double y, double z, float height) {
        float he = Math.max(1.0F, height);
        for (int i = 0; i < 33; i++) {
            double a = Math.toRadians(level.random.nextInt(360));
            double r = 0.25D + level.random.nextDouble() * he * 0.5D;
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + Math.cos(a) * r, y + he * 0.5D, z + Math.sin(a) * r, 0.0D, 0.03D, 0.0D);
        }
    }

    private static void golemArc(Level level, Entity entity, double x, double y, double z, int bx, int by, int bz) {
        if (entity instanceof EldritchGolemEntity golem) {
            golem.clientStartArc(new BlockPos(bx, by, bz));
        }
        double ex = bx + 0.5D;
        double ey = by + 1.0D;
        double ez = bz + 0.5D;
        for (int i = 0; i < 16; i++) {
            double t = i / 15.0D;
            double ox = (level.random.nextDouble() - 0.5D) * 0.25D;
            double oy = (level.random.nextDouble() - 0.5D) * 0.25D;
            double oz = (level.random.nextDouble() - 0.5D) * 0.25D;
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x + (ex - x) * t + ox, y + (ey - y) * t + oy, z + (ez - z) * t + oz, 0.0D, 0.0D, 0.0D);
        }
        level.playLocalSound(x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 0.35F, 1.25F, false);
    }
}
