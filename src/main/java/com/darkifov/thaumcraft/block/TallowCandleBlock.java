package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.blockentity.TallowCandleBlockEntity;
import com.darkifov.thaumcraft.infusion.InfusionStabilizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * TC4 tallow candle parity block. Metadata colours from 1.7.10 are represented
 * as separate registry entries in 1.19.2, while placement, particles and
 * infusion-stabilizer behavior are shared by this class.
 */
public class TallowCandleBlock extends BaseEntityBlock implements InfusionStabilizer {
    private static final VoxelShape SHAPE = Block.box(
            TC4TallowCandleParity.BODY_MIN * 16.0D, 0.0D, TC4TallowCandleParity.BODY_MIN * 16.0D,
            TC4TallowCandleParity.BODY_MAX * 16.0D, TC4TallowCandleParity.BODY_HEIGHT * 16.0D,
            TC4TallowCandleParity.BODY_MAX * 16.0D);

    public TallowCandleBlock(Properties properties) {
        super(properties);
    }


    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TallowCandleBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** TC4 BlockCandle#getCollisionBoundingBoxFromPool returned null. */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    /** TC4 BlockCandle.randomDisplayTick: one smoke and one flame particle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + TC4TallowCandleParity.PARTICLE_XZ_OFFSET;
        double y = pos.getY() + TC4TallowCandleParity.PARTICLE_Y_OFFSET;
        double z = pos.getZ() + TC4TallowCandleParity.PARTICLE_XZ_OFFSET;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
