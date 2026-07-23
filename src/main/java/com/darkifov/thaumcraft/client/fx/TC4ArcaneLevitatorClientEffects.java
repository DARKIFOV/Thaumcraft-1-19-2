package com.darkifov.thaumcraft.client.fx;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Client-only implementation of ClientProxy.sparkle(size=1,type=3,gravity=-0.3). */
public final class TC4ArcaneLevitatorClientEffects {
    private TC4ArcaneLevitatorClientEffects() {
    }

    public static void spawn(Level level, double x, double y, double z, RandomSource random) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != level) {
            return;
        }
        ParticleStatus status = minecraft.options.particles().get();
        int threshold = switch (status) {
            case ALL -> TC4ArcaneLevitatorParity.PARTICLE_ALL_THRESHOLD;
            case DECREASED -> TC4ArcaneLevitatorParity.PARTICLE_DECREASED_THRESHOLD;
            case MINIMAL -> TC4ArcaneLevitatorParity.PARTICLE_MINIMAL_THRESHOLD;
        };
        if (random.nextInt(TC4ArcaneLevitatorParity.PARTICLE_GATE_DENOMINATOR) >= threshold) {
            return;
        }
        // TC4 type-3 sparkle consumes its green-channel RNG after the density gate.
        double greenRandomUnit = random.nextFloat();
        minecraft.particleEngine.createParticle(ThaumcraftMod.ARCANE_LEVITATOR_SPARKLE_PARTICLE.get(),
                x, y, z, greenRandomUnit, 0.0D, 0.0D);
    }
}
