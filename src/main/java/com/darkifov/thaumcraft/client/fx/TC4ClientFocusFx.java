package com.darkifov.thaumcraft.client.fx;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Stage181 client-only approximation layer for original TC4 focus FX classes.
 *
 * The 1.7.10 code used bespoke GL particles (FXBeamWand, FXLightningBolt,
 * FXWisp/sparkle and RenderGlobal.destroyBlockProgress).  These classes cannot
 * be copied verbatim into the 1.19.2 renderer, so this class preserves the same
 * source calls, constants and timings using Forge/Minecraft 1.19.2 primitives.
 * No new visual identity is introduced here: beam type, color, impact, sparkle
 * counts and lightning duration mirror the original focus methods.
 */
public final class TC4ClientFocusFx {
    private TC4ClientFocusFx() {
    }

    public static void beamCont(Player player, Vec3 target, int type, int color, boolean reverse, float endMod, int impact) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || player == null) {
            return;
        }
        Vec3 start = wandTip(player);
        Vec3 delta = target.subtract(start);
        int steps = Math.max(6, (int) (delta.length() * 8.0D));
        float[] rgb = rgb(color);
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), type == 2 ? 0.55F : 0.35F);
        RandomSource random = mc.level.random;
        double pulse = (player.tickCount % 8) / 8.0D;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            if (reverse) {
                t = 1.0D - t;
            }
            Vec3 p = start.add(delta.scale(t));
            double wobble = Mth.sin((float) ((t + pulse) * ((float)(Math.PI * 2.0D)) * 3.0D)) * 0.018D;
            mc.level.addParticle(dust, p.x + wobble, p.y, p.z - wobble, 0.0D, 0.0D, 0.0D);
            if (impact > 0 && i >= steps - 2) {
                mc.level.addParticle(ParticleTypes.CRIT, p.x, p.y, p.z, random.nextGaussian() * 0.015D, random.nextGaussian() * 0.015D, random.nextGaussian() * 0.015D);
            }
        }
        if (impact > 0) {
            int burst = Math.max(3, impact);
            for (int i = 0; i < burst; i++) {
                mc.level.addParticle(ParticleTypes.CRIT, target.x, target.y, target.z,
                        (random.nextFloat() - random.nextFloat()) * 0.07D * endMod,
                        (random.nextFloat() - random.nextFloat()) * 0.07D * endMod,
                        (random.nextFloat() - random.nextFloat()) * 0.07D * endMod);
            }
        }
    }

    public static void sparkleCloud(Vec3 pos, float spread, int count, int colorType) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        RandomSource random = mc.level.random;
        for (int i = 0; i < count; i++) {
            double x = pos.x + (random.nextFloat() - random.nextFloat()) * spread;
            double y = pos.y + (random.nextFloat() - random.nextFloat()) * spread;
            double z = pos.z + (random.nextFloat() - random.nextFloat()) * spread;
            if (colorType == 2) {
                mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.02D + random.nextFloat() * 0.02D, 0.0D);
            }
            mc.level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, 0.05D + random.nextFloat() * 0.05D, 0.0D);
        }
    }

    public static void shockLightning(Player player, Vec3 target, boolean offset) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || player == null) {
            return;
        }
        Vec3 start = wandTip(player);
        Vec3 delta = target.subtract(start);
        RandomSource random = mc.level.random;
        Vec3 previous = start;
        int segments = 6; // original FXLightningBolt(world, ..., 6, 0.5F, 8)
        for (int i = 1; i <= segments; i++) {
            double t = i / (double) segments;
            Vec3 current = start.add(delta.scale(t));
            if (i < segments) {
                current = current.add(
                        (random.nextFloat() - random.nextFloat()) * 0.22D,
                        (random.nextFloat() - random.nextFloat()) * 0.22D,
                        (random.nextFloat() - random.nextFloat()) * 0.22D);
            }
            renderSegment(previous, current, 0x99CCFF, 5);
            previous = current;
        }
    }

    public static void excavateFX(BlockPos pos, Player player, int progress) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.levelRenderer == null || player == null) {
            return;
        }
        mc.levelRenderer.destroyBlockProgress(player.getId(), pos, Math.max(0, Math.min(9, progress)));
    }

    public static void renderSegment(Vec3 start, Vec3 end, int color, int density) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float[] rgb = rgb(color);
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 0.45F);
        Vec3 delta = end.subtract(start);
        int steps = Math.max(2, density);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = start.add(delta.scale(i / (double) steps));
            mc.level.addParticle(dust, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
            if (i % 2 == 0) {
                mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static Vec3 wandTip(Player player) {
        Vec3 look = player.getViewVector(1.0F);
        double px = player.getX() - Mth.cos(player.getYRot() / 180.0F * Mth.PI) * 0.06D + look.x * 0.3D;
        double py = player.getY() + player.getEyeHeight() - 0.06D + look.y * 0.3D;
        double pz = player.getZ() - Mth.sin(player.getYRot() / 180.0F * Mth.PI) * 0.06D + look.z * 0.3D;
        return new Vec3(px, py, pz);
    }

    private static float[] rgb(int color) {
        return new float[] {
                ((color >> 16) & 255) / 255.0F,
                ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F
        };
    }
}
