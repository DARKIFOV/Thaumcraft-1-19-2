package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.damage.TC4DamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

/** Direct 1.19.2 adapter for TC4 BlockFluidDeath. */
public final class LiquidDeathBlock extends LiquidBlock {
    public LiquidDeathBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        // TC4 used a four-quanta finite fluid and dealt metadata + 1 damage.
        // Modern LiquidBlock source LEVEL is zero, so preserve the observable
        // 4/3/2/1 damage ladder across the first four flow depths.
        int flowDepth = Math.min(3, state.getValue(LEVEL));
        living.hurt(TC4DamageSources.DISSOLVE, 4.0F - flowDepth);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int flowDepth = Math.min(3, state.getValue(LEVEL));
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.1D + 0.12D * flowDepth;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0D, 0.01D, 0.0D);
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.WITCH, x, y + 0.05D, z, 0.0D, 0.015D, 0.0D);
        }
        if (random.nextInt(50) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
