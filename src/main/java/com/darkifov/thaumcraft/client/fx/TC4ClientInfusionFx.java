package com.darkifov.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Client-side renderer for TC4 PacketFXInfusionSource and PacketFXBlockZap adapters. */
public final class TC4ClientInfusionFx {
    private TC4ClientInfusionFx() {
    }

    public static void infusionSource(BlockPos matrixPos, int dx, int dy, int dz, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }

        double ex = matrixPos.getX() + 0.5D;
        double ey = matrixPos.getY() + 0.7D;
        double ez = matrixPos.getZ() + 0.5D;
        double sx;
        double sy;
        double sz;
        ParticleOptions particle = ParticleTypes.WITCH;

        if (entityId != 0) {
            Entity entity = level.getEntity(entityId);
            if (entity == null) {
                return;
            }
            sx = entity.getX();
            sy = entity.getY() + entity.getBbHeight() * 0.5D;
            sz = entity.getZ();
            particle = ParticleTypes.ENCHANT;
        } else {
            sx = matrixPos.getX() - dx + 0.5D;
            sy = matrixPos.getY() - dy + 1.15D;
            sz = matrixPos.getZ() - dz + 0.5D;
        }

        particleLine(level, sx, sy, sz, ex, ey, ez, particle, entityId == 0 ? 22 : 18);
    }

    public static void blockZap(double sx, double sy, double sz, double ex, double ey, double ez) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }
        particleLine(level, sx, sy, sz, ex, ey, ez, ParticleTypes.ELECTRIC_SPARK, 24);
    }

    private static void particleLine(Level level, double sx, double sy, double sz, double ex, double ey, double ez, ParticleOptions particle, int points) {
        for (int i = 0; i <= points; i++) {
            double t = points <= 0 ? 0.0D : (double) i / (double) points;
            double x = sx + (ex - sx) * t + (level.random.nextDouble() - 0.5D) * 0.04D;
            double y = sy + (ey - sy) * t + (level.random.nextDouble() - 0.5D) * 0.04D;
            double z = sz + (ez - sz) * t + (level.random.nextDouble() - 0.5D) * 0.04D;
            level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
