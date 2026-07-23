package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.warp.TC4BathSaltsParity;
import net.minecraft.core.BlockPos;
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
        if (player.hasEffect(ThaumcraftMod.WARP_WARD.get())) {
            return;
        }

        int permanentWarp = PlayerThaumData.getWarpPerm(player);
        int duration = TC4BathSaltsParity.wardDurationTicks(permanentWarp);
        player.addEffect(new MobEffectInstance(ThaumcraftMod.WARP_WARD.get(), duration, 0, true, true));
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int legacyMetadata = state.getValue(LEVEL);
        double bubbleX = pos.getX() + random.nextFloat();
        double bubbleY = pos.getY() + TC4BathSaltsParity.bubbleYOffset(legacyMetadata);
        double bubbleZ = pos.getZ() + random.nextFloat();
        level.addParticle(ThaumcraftMod.PURIFYING_BUBBLE_PARTICLE.get(),
                bubbleX, bubbleY, bubbleZ, 0.0D, 0.0D, 0.0D);

        if (TC4BathSaltsParity.playsPopSound(
                random.nextInt(TC4BathSaltsParity.POP_SOUND_CHANCE_BOUND))) {
            double soundX = pos.getX() + random.nextFloat();
            double soundY = pos.getY() + TC4BathSaltsParity.POP_SOUND_Y_OFFSET;
            double soundZ = pos.getZ() + random.nextFloat();
            level.playLocalSound(soundX, soundY, soundZ, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    TC4BathSaltsParity.popVolume(random.nextFloat()),
                    TC4BathSaltsParity.popPitch(random.nextFloat()), false);
        }

        // TC4 invoked the superclass after its own bubble/sound RNG sequence.
        super.animateTick(state, level, pos, random);
    }

}
