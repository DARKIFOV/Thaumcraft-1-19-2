package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 parity target: Nitor is not a full glass cube. It is an airy magical flame
 * block placed by the Nitor item and used as a permanent light source.
 */
public class NitorLightBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4.0D, 1.0D, 4.0D, 12.0D, 13.0D, 12.0D);

    public NitorLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            double y = pos.getY() + 0.55D + random.nextDouble() * 0.25D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.01D, 0.0D);
        }
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D;
            double y = pos.getY() + 0.65D + random.nextDouble() * 0.25D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.45D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.012D, 0.0D);
        }
    }
}
