package com.darkifov.thaumcraft.client.fx;

import com.darkifov.thaumcraft.warp.TC4BathSaltsParity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Modern rendering of TC4's FXBubble used by purifying fluid.
 *
 * <p>The original uses particle-sheet frames 16, 17 and 18, full-bright
 * translucent rendering, a 0.25 alpha, a tiny random scale, weak upward
 * acceleration and 0.85 velocity damping.</p>
 */
public final class TC4PurifyingBubbleParticle extends TextureSheetParticle {
    private static final double UPWARD_ACCELERATION = 0.002D;
    private static final double HORIZONTAL_RANDOM_ACCELERATION = 0.01D;
    private static final double VELOCITY_DAMPING = 0.8500000238418579D;
    private static final int LAST_SPRITE_INDEX = 2;

    private final SpriteSet sprites;

    private TC4PurifyingBubbleParticle(ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.rCol = TC4BathSaltsParity.BUBBLE_RED;
        this.gCol = TC4BathSaltsParity.BUBBLE_GREEN;
        this.bCol = TC4BathSaltsParity.BUBBLE_BLUE;
        this.alpha = TC4BathSaltsParity.BUBBLE_ALPHA;
        this.hasPhysics = false;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.3F + 0.2F;
        this.xd = xSpeed * 0.20000000298023224D + (this.random.nextDouble() * 2.0D - 1.0D) * 0.02D;
        this.yd = ySpeed * 0.20000000298023224D + this.random.nextDouble() * 0.02D;
        this.zd = zSpeed * 0.20000000298023224D + (this.random.nextDouble() * 2.0D - 1.0D) * 0.02D;
        this.lifetime = TC4BathSaltsParity.bubbleLifetime(this.random.nextDouble());
        this.setSprite(this.sprites.get(0, LAST_SPRITE_INDEX));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.yd += UPWARD_ACCELERATION;
        this.xd += (this.random.nextFloat() - this.random.nextFloat()) * HORIZONTAL_RANDOM_ACCELERATION;
        this.zd += (this.random.nextFloat() - this.random.nextFloat()) * HORIZONTAL_RANDOM_ACCELERATION;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= VELOCITY_DAMPING;
        this.yd *= VELOCITY_DAMPING;
        this.zd *= VELOCITY_DAMPING;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        int remaining = this.lifetime - this.age;
        int sprite = remaining <= 1 ? 2 : remaining <= 2 ? 1 : 0;
        this.setSprite(this.sprites.get(sprite, LAST_SPRITE_INDEX));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
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
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new TC4PurifyingBubbleParticle(level, x, y, z,
                    xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
