package com.darkifov.thaumcraft.client.fx;

import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Alpha-aware modern equivalent of the original EntitySpellParticleFX emitted
 * above a full Brain in a Jar. The eight vanilla generic frames are the modern
 * spell-particle atlas equivalent; TC4's exact alpha and RGB contract is kept.
 */
public final class TC4BrainJarFullParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private TC4BrainJarFullParticle(ClientLevel level, double x, double y, double z,
                                    double red, double green, double blue,
                                    SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.rCol = (float) red;
        this.gCol = (float) green;
        this.bCol = (float) blue;
        this.alpha = TC4BrainJarParity.FULL_PARTICLE_ALPHA;
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        this.xd = (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
        this.yd = (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
        this.zd = (this.random.nextDouble() * 2.0D - 1.0D) * 0.05D;
        double speed = (this.random.nextDouble() + this.random.nextDouble() + 1.0D) * 0.15D;
        double length = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        if (length != 0.0D) {
            this.xd = this.xd / length * speed * 0.4D;
            this.yd = this.yd / length * speed * 0.4D + 0.1D;
            this.zd = this.zd / length * speed * 0.4D;
        }
        this.quadSize *= 0.75F;
        this.quadSize *= this.random.nextFloat() * 0.5F + 0.5F;
        this.lifetime = (int) (8.0D / (this.random.nextDouble() * 0.8D + 0.2D));
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double red, double green, double blue) {
            return new TC4BrainJarFullParticle(level, x, y, z, red, green, blue, sprites);
        }
    }
}
