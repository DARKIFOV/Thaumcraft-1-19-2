package com.darkifov.thaumcraft.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Common-side no-op bridge for the client-only TC4 sparkle gate.
 * The client installs the real spawner during client setup; dedicated servers
 * never load Minecraft client classes.
 */
public final class TC4ArcaneLevitatorEffectsBridge {
    @FunctionalInterface
    public interface SparkleSpawner {
        void spawn(Level level, double x, double y, double z, RandomSource random);
    }

    private static final SparkleSpawner NO_OP = (level, x, y, z, random) -> { };
    private static volatile SparkleSpawner spawner = NO_OP;

    private TC4ArcaneLevitatorEffectsBridge() {
    }

    public static void install(SparkleSpawner clientSpawner) {
        spawner = Objects.requireNonNull(clientSpawner, "clientSpawner");
    }

    public static void spawn(Level level, double x, double y, double z, RandomSource random) {
        spawner.spawn(level, x, y, z, random);
    }
}
