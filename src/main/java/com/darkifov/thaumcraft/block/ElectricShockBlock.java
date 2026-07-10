package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Invisible, replaceable electrical residue left by the original TC4 Earth Shock orb.
 * Mirrors BlockAiry metadata 10: short-lived random-tick lifetime, 1-2 lightning
 * damage on contact, horizontal slowdown, pale sparks and the jacobs crackle.
 */
public class ElectricShockBlock extends Block {
    public ElectricShockBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.hurt(net.minecraft.world.damagesource.DamageSource.LIGHTNING_BOLT, 1.0F + level.random.nextInt(2));
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8D, 1.0D, 0.8D));
        if (!level.isClientSide && level.random.nextInt(100) == 0) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        float height = random.nextFloat() * 0.33F;
        level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                pos.getX() + random.nextFloat(),
                pos.getY() + 0.1515F + height / 2.0F,
                pos.getZ() + random.nextFloat(),
                0.0D, 0.01D, 0.0D);
        if (random.nextInt(50) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TC4Sounds.event("jacobs"),
                    SoundSource.BLOCKS, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F, false);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
