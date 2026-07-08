package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stage273-282: 1.19.2 runtime mirror for TC4 TileEldritchCap / blockEldritch meta 3. */
public class EldritchCapBlockEntity extends BlockEntity {
    private int ticks;

    public EldritchCapBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_CAP_BLOCK_ENTITY.get(), pos, state);
    }

    public int ticks() { return ticks; }

    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, EldritchCapBlockEntity be) {
        be.ticks++;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ticks", ticks);
        tag.putString("TC4Tile", "TileEldritchCap");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ticks = tag.getInt("ticks");
    }
}
