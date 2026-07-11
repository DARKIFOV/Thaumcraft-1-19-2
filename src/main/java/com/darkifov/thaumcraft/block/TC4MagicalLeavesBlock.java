package com.darkifov.thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge 1.19.2 port of TC4 BlockMagicalLeaves.
 *
 * <p>The previous rebuild registered both magical leaf types as ordinary
 * non-occluding blocks. That forced Minecraft to render every internal face of
 * a tree canopy and produced the transparent blue lattice visible in the
 * supplied Silverwood screenshot. Extending the vanilla LeavesBlock restores
 * leaf distance/persistence state, decay, neighbour face suppression and the
 * normal fancy/fast leaves pipeline while keeping the original TC4 textures.</p>
 */
public final class TC4MagicalLeavesBlock extends LeavesBlock {
    public enum Kind {
        GREATWOOD,
        SILVERWOOD
    }

    private final Kind kind;

    public TC4MagicalLeavesBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind == null ? Kind.GREATWOOD : kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // TC4 BlockMagicalLeaves: rain occasionally drips through exposed leaf
        // blocks. Use the modern particle and sturdy-face APIs.
        if (level.isRainingAt(pos.above()) && random.nextInt(15) == 1) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (!belowState.canOcclude() || !belowState.isFaceSturdy(level, below, Direction.UP)) {
                level.addParticle(ParticleTypes.DRIPPING_WATER,
                        pos.getX() + random.nextDouble(),
                        pos.getY() - 0.05D,
                        pos.getZ() + random.nextDouble(),
                        0.0D, 0.0D, 0.0D);
            }
        }

        // Original Silverwood leaves emit a very rare magical sparkle. END_ROD
        // is the closest vanilla 1.19.2 particle to the small TC4 sparkle and is
        // deliberately kept at the original 1/500 frequency.
        if (kind == Kind.SILVERWOOD && random.nextInt(500) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + random.nextDouble() - random.nextDouble(),
                    pos.getY() + 0.5D + random.nextDouble() - random.nextDouble(),
                    pos.getZ() + 0.5D + random.nextDouble() - random.nextDouble(),
                    0.0D, 0.01D, 0.0D);
        }
    }
}
