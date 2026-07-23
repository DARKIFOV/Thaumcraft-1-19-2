package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.taint.TaintSpreadRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Vector3f;

public class TaintBlock extends Block {
    public enum Variant { CRUST, SOIL, FLESH }
    private final Variant variant;

    public TaintBlock(Properties properties, Variant variant) { super(properties); this.variant = variant; }
    public Variant variant() { return variant; }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintSpreadRuntime.randomTick(level, pos, random, variant);
    }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (variant == Variant.FLESH || level.isClientSide || !(entity instanceof LivingEntity living)
                || living.isInvertedHealAndHarm()) return;
        int chance = living instanceof Player ? 100 : 20;
        int duration = living instanceof Player ? 80 : 160;
        if (level.random.nextInt(chance) == 0) living.addEffect(new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), duration, 0, false, false));
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (variant == Variant.CRUST && level.isEmptyBlock(pos.below()) && random.nextInt(10) == 0) {
            level.addParticle(new DustParticleOptions(new Vector3f(0.3F, 0.1F, 0.8F), 0.8F),
                    pos.getX() + 0.1D + random.nextDouble() * 0.8D, pos.getY(),
                    pos.getZ() + 0.1D + random.nextDouble() * 0.8D, 0.0D, -0.02D, 0.0D);
        }
    }
}
