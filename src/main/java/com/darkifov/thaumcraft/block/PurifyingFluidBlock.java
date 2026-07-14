package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

/** TC4 BlockFluidPure parity: a luminous, single-use source that grants Warp Ward. */
public class PurifyingFluidBlock extends LiquidBlock {
    public PurifyingFluidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!level.getFluidState(pos).isSource()) {
            return;
        }
        if (player.hasEffect(ThaumcraftMod.WARP_WARD.get()) || PlayerThaumData.hasWarpWard(player)) {
            return;
        }

        int permanentWarp = PlayerThaumData.getWarpPerm(player);
        int divisor = Math.max(1, (int) Math.floor(Math.sqrt(permanentWarp)));
        int duration = Math.min(32000, 200000 / divisor);
        player.addEffect(new MobEffectInstance(ThaumcraftMod.WARP_WARD.get(), duration, 0, true, true));
        PlayerThaumData.setWarpWardTicks(player, duration);
        level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        int levelValue = state.getValue(LEVEL);
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.125D * (8 - Math.min(7, levelValue));
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0D, 0.0D, 0.0D);
        if (random.nextInt(25) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1F + random.nextFloat() * 0.1F,
                    0.9F + random.nextFloat() * 0.15F, false);
        }
    }
}
