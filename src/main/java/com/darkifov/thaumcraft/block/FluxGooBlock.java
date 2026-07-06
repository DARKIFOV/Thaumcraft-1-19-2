package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Stage126: TC4-style flux goo as a real 1.19.2 block.
 *
 * Original TC4 uses flux goo/gas as world spill from unstable alchemy.  This is not a
 * cosmetic marker: it hurts/poisons living things, can taint nearby soil and can evaporate
 * into flux gas.  The exact 1.7.10 block ids are not reused directly; the behavior is
 * ported into a normal Forge 1.19.2 block.
 */
public class FluxGooBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);

    public FluxGooBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && living.tickCount % 20 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(7) == 0) {
            BlockPos target = pos.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
            BlockState targetState = level.getBlockState(target);
            if (targetState.is(Blocks.DIRT) || targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.COARSE_DIRT)
                    || targetState.is(Blocks.ROOTED_DIRT) || targetState.is(Blocks.MUD)) {
                level.setBlock(target, ThaumcraftMod.TAINTED_SOIL.get().defaultBlockState(), 3);
            }
        }

        if (random.nextInt(9) == 0 && level.isEmptyBlock(pos.above())) {
            level.setBlock(pos.above(), ThaumcraftMod.FLUX_GAS.get().defaultBlockState(), 3);
            level.playSound(null, pos, TC4Sounds.event("spill"), SoundSource.BLOCKS, 0.30F, 0.75F + random.nextFloat() * 0.2F);
        }

        if (random.nextInt(18) == 0) {
            level.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.22D, pos.getZ() + 0.5D, 4, 0.35D, 0.05D, 0.35D, 0.01D);
        }
    }
}
