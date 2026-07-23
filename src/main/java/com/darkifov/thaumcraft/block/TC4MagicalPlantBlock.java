package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * De-metadata'd TC4 {@code BlockCustomPlant} variants 2..5.
 *
 * <p>Shimmerleaf and Cinderpearl retain their light and ambient particles,
 * Vishroom applies the original ten-second confusion effect, and Ethereal
 * Bloom performs a conservative server-side purification pass in the original
 * eight-block radius. TC4 changed biome data; the 1.19.2 rebuild instead
 * removes the rebuild's explicit taint blocks because biome mutation is no
 * longer a safe or data-pack-friendly persistence mechanism.</p>
 */
public final class TC4MagicalPlantBlock extends BushBlock {
    public enum Kind {
        SHIMMERLEAF,
        CINDERPEARL,
        ETHEREAL_BLOOM,
        VISHROOM
    }

    private static final VoxelShape SHAPE = Block.box(1.6D, 0.0D, 1.6D, 14.4D, 12.8D, 14.4D);
    private final Kind kind;

    public TC4MagicalPlantBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind == null ? Kind.SHIMMERLEAF : kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        BlockState support = level.getBlockState(supportPos);
        return !support.isAir() && support.isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && kind == Kind.VISHROOM && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, true));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && kind == Kind.ETHEREAL_BLOOM && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.ETHEREAL_BLOOM) {
            purifyOneTaintBlock(level, pos, random);
            level.scheduleTick(pos, this, 20);
        }
    }

    private static void purifyOneTaintBlock(ServerLevel level, BlockPos origin, RandomSource random) {
        int dx = random.nextInt(8) - random.nextInt(8);
        int dz = random.nextInt(8) - random.nextInt(8);
        if (dx * dx + dz * dz > 81) return;

        for (int dy = 2; dy >= -2; dy--) {
            BlockPos target = origin.offset(dx, dy, dz);
            if (!level.hasChunkAt(target)) continue;
            BlockState targetState = level.getBlockState(target);

            if (targetState.is(ThaumcraftMod.TAINTED_SOIL.get())
                    || targetState.is(ThaumcraftMod.TAINT_SOIL.get())) {
                level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        target.getX() + 0.5D, target.getY() + 0.7D, target.getZ() + 0.5D,
                        4, 0.25D, 0.25D, 0.25D, 0.01D);
                return;
            }

            if (targetState.is(ThaumcraftMod.TAINT_CRUST.get())
                    || targetState.is(ThaumcraftMod.FLESH_BLOCK.get())
                    || targetState.is(ThaumcraftMod.TAINT_FIBRES.get())
                    || targetState.is(ThaumcraftMod.FLUX_GOO.get())
                    || targetState.is(ThaumcraftMod.FLUX_GAS.get())) {
                level.removeBlock(target, false);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
                        4, 0.25D, 0.25D, 0.25D, 0.01D);
                return;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (kind == Kind.SHIMMERLEAF && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.1D,
                    pos.getY() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.15D,
                    pos.getZ() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.1D,
                    0.0D, 0.005D, 0.0D);
        } else if (kind == Kind.CINDERPEARL && random.nextBoolean()) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.1D;
            double y = pos.getY() + 0.6D + (random.nextDouble() - random.nextDouble()) * 0.1D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.1D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        } else if (kind == Kind.VISHROOM && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.4D,
                    pos.getY() + 0.3D,
                    pos.getZ() + 0.5D + (random.nextDouble() - random.nextDouble()) * 0.4D,
                    0.0D, 0.015D, 0.0D);
        } else if (kind == Kind.ETHEREAL_BLOOM && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D,
                    pos.getY() + 0.6D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D,
                    0.0D, 0.01D, 0.0D);
        }
    }
}
