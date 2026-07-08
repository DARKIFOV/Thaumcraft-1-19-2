package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stage273-282: ticking 1.19.2 equivalent for TC4 TileEldritchCrystal renderer anchors. */
public class EldritchCrystalBlockEntity extends BlockEntity {
    private int ticks;

    public EldritchCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_CRYSTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public int ticks() { return ticks; }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EldritchCrystalBlockEntity be) {
        be.ticks++;
        RandomSource random = level.random;
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D,
                    pos.getY() + 0.35D + random.nextDouble() * 0.75D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ticks", ticks);
        tag.putString("TC4Tile", "TileEldritchCrystal");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ticks = tag.getInt("ticks");
    }
}
