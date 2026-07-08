package com.darkifov.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Client-side 1.19.2 approximation of TC4 FXShieldRunes spawned by PacketFXShield. */
public final class TC4ClientRunicShieldFx {
    private TC4ClientRunicShieldFx() {
    }

    public static void shield(int sourceId, int targetId) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        Entity source = level.getEntity(sourceId);
        if (source == null) {
            return;
        }

        double yaw = 0.0D;
        double pitch = Math.PI / 2.0D;
        if (targetId >= 0) {
            Entity target = level.getEntity(targetId);
            if (target != null) {
                double dx = source.getX() - target.getX();
                double dy = source.getY(0.5D) - target.getY(0.5D);
                double dz = source.getZ() - target.getZ();
                yaw = Math.atan2(dz, dx) - Math.PI / 2.0D;
                pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
            }
            ring(level, source, yaw, pitch);
        } else if (targetId == -1) {
            ring(level, source, 0.0D, Math.PI / 2.0D);
            ring(level, source, 0.0D, Math.PI * 1.5D);
        } else if (targetId == -2) {
            ring(level, source, 0.0D, Math.PI * 1.5D);
        } else if (targetId == -3) {
            ring(level, source, 0.0D, Math.PI / 2.0D);
        }
    }

    private static void ring(Level level, Entity source, double yaw, double pitch) {
        double cx = source.getX();
        double cy = source.getY() + source.getBbHeight() * 0.55D;
        double cz = source.getZ();
        double radius = Math.max(0.65D, source.getBbWidth() * 0.9D);

        for (int i = 0; i < 32; i++) {
            double angle = Math.PI * 2.0D * i / 32.0D;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            double z = 0.0D;

            double py = y * Math.cos(pitch) - z * Math.sin(pitch);
            double pz = y * Math.sin(pitch) + z * Math.cos(pitch);
            double px = x;

            double rx = px * Math.cos(yaw) - pz * Math.sin(yaw);
            double rz = px * Math.sin(yaw) + pz * Math.cos(yaw);

            level.addParticle(ParticleTypes.ENCHANT, cx + rx, cy + py, cz + rz, 0.0D, 0.0D, 0.0D);
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, cx + rx, cy + py, cz + rz, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
