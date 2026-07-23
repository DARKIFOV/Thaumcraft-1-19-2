package com.darkifov.thaumcraft.client.fx;

import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Exact four-frame FXSparkle path used by TC4's Arcane Levitator. */
public final class TC4ArcaneLevitatorSparkleParticle extends TextureSheetParticle {
    private static final double VELOCITY_DAMPING = 0.9080000019073486D;
    private static final int LAST_SPRITE_INDEX = 3;
    private final SpriteSet sprites;

    private TC4ArcaneLevitatorSparkleParticle(ClientLevel level, double x, double y, double z,
                                              double ignoredXSpeed, double ignoredYSpeed, double ignoredZSpeed,
                                              SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.rCol = TC4ArcaneLevitatorParity.particleRed();
        this.gCol = TC4ArcaneLevitatorParity.particleGreen((float) ignoredXSpeed);
        this.bCol = TC4ArcaneLevitatorParity.particleBlue();
        this.alpha = 0.75F;
        this.gravity = TC4ArcaneLevitatorParity.PARTICLE_GRAVITY;
        this.hasPhysics = false;
        this.setSize(0.01F, 0.01F);
        this.quadSize *= TC4ArcaneLevitatorParity.PARTICLE_SIZE;
        this.lifetime = TC4ArcaneLevitatorParity.PARTICLE_LIFETIME;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        updateSprite();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= VELOCITY_DAMPING;
        this.yd *= VELOCITY_DAMPING;
        this.zd *= VELOCITY_DAMPING;
        updateSprite();
    }

    private void updateSprite() {
        int frame = Math.min(LAST_SPRITE_INDEX,
                Math.max(0, this.age / TC4ArcaneLevitatorParity.PARTICLE_MULTIPLIER));
        this.setSprite(this.sprites.get(frame, LAST_SPRITE_INDEX));
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
            return new TC4ArcaneLevitatorSparkleParticle(level, x, y, z,
                    xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
