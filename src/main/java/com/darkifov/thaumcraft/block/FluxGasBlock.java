package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Stage126: TC4-style flux gas as a real 1.19.2 block.
 * Stage209 narrows this toward TC4 BlockFluxGas: occasional contact effect,
 * slow drift, and finite dissipation instead of a permanent world filler.
 */
public class FluxGasBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.empty();

    public FluxGasBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && living.tickCount % 20 == 0 && level.random.nextInt(10) == 0) {
            if (level.random.nextBoolean()) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0, true, true));
            } else {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, true));
            }
            if (level.random.nextBoolean()) {
                level.removeBlock(pos, false);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            level.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.45D, 0.35D, 0.45D, 0.01D);
        }

        if (random.nextInt(7) == 0) {
            BlockPos drift = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (level.isEmptyBlock(drift)) {
                level.setBlock(drift, state, 3);
            }
        }

        if (random.nextInt(20) == 0) {
            BlockPos below = pos.below();
            if (level.isEmptyBlock(below)) {
                level.setBlock(below, ThaumcraftMod.FLUX_GOO.get().defaultBlockState(), 3);
            }
        }

        if (random.nextInt(18) == 0) {
            level.removeBlock(pos, false);
        }
    }
}
