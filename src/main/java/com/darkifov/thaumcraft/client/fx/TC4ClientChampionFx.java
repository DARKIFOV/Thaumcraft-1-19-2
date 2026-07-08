package com.darkifov.thaumcraft.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Client-side Stage214 approximation of every TC4 ChampionMod*.showFX branch.
 * The color/timing choices follow the original effects while using vanilla 1.19.2 particles.
 */
public final class TC4ClientChampionFx {
    private TC4ClientChampionFx() {
    }

    public static void showFX(int entityId, int mod) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mod < 0 || mod > 12) {
            return;
        }
        Entity boss = level.getEntity(entityId);
        if (boss == null || !boss.isAlive()) {
            return;
        }

        switch (mod) {
            case 0 -> showBold(level, boss);
            case 1 -> showSpined(level, boss);
            case 2 -> showArmored(level, boss);
            case 3 -> showMighty(level, boss);
            case 4 -> showGrim(level, boss);
            case 5 -> showWarded(level, boss);
            case 6 -> showWarp(level, boss);
            case 7 -> showUndying(level, boss);
            case 8 -> showFiery(level, boss);
            case 9 -> showSickly(level, boss);
            case 10 -> showVenomous(level, boss);
            case 11 -> showVampiric(level, boss);
            case 12 -> showInfested(level, boss);
            default -> { }
        }
    }

    private static void showBold(Level level, Entity boss) {
        if (level.random.nextBoolean()) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.ELECTRIC_SPARK, 1, 0.02D);
    }

    private static void showSpined(Level level, Entity boss) {
        if (level.random.nextInt(3) != 0) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.CRIT, 1, 0.01D);
    }

    private static void showArmored(Level level, Entity boss) {
        if (level.random.nextInt(4) != 0) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.POOF, 1, 0.0D);
    }

    private static void showMighty(Level level, Entity boss) {
        if (level.random.nextInt(4) != 0) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.ENCHANTED_HIT, 1, 0.01D);
    }

    private static void showGrim(Level level, Entity boss) {
        if (level.random.nextBoolean()) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.SOUL, 1, -0.02D);
    }

    private static void showWarded(Level level, Entity boss) {
        if (level.random.nextBoolean()) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.ENCHANT, 1, 0.0D);
    }

    private static void showWarp(Level level, Entity boss) {
        if (level.random.nextBoolean()) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.PORTAL, 2, 0.0D);
    }

    private static void showUndying(Level level, Entity boss) {
        if (level.random.nextInt(5) != 0) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.HEART, 1, 0.01D);
    }

    private static void showFiery(Level level, Entity boss) {
        sprinkle(level, boss, ParticleTypes.FLAME, 1, 0.03D);
    }

    private static void showSickly(Level level, Entity boss) {
        if (level.random.nextFloat() > 0.35F) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.WITCH, 1, 0.0D);
    }

    private static void showVenomous(Level level, Entity boss) {
        if (level.random.nextFloat() > 0.35F) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.WITCH, 1, 0.02D);
    }

    private static void showVampiric(Level level, Entity boss) {
        if (level.random.nextFloat() > 0.2F) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.DAMAGE_INDICATOR, 1, 0.0D);
    }

    private static void showInfested(Level level, Entity boss) {
        if (level.random.nextBoolean()) {
            return;
        }
        sprinkle(level, boss, ParticleTypes.SMOKE, 2, 0.01D);
    }

    private static void sprinkle(Level level, Entity boss, ParticleOptions particle, int count, double ySpeed) {
        for (int i = 0; i < count; i++) {
            double x = boss.getBoundingBox().minX + level.random.nextDouble() * boss.getBbWidth();
            double y = boss.getBoundingBox().minY + level.random.nextDouble() * boss.getBbHeight();
            double z = boss.getBoundingBox().minZ + level.random.nextDouble() * boss.getBbWidth();
            level.addParticle(particle, x, y, z, 0.0D, ySpeed, 0.0D);
        }
    }
}
