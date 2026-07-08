package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.eldritch.TC4EldritchLockBossSpawner;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stage273-282: TC4 TileEldritchLock parity tick. count=-1 idle, pump every 5 ticks, boss spawn after 100. */
public class EldritchLockBlockEntity extends BlockEntity {
    public static final String COUNT_TAG = "count";
    private int count = -1;

    public EldritchLockBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_LOCK_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean active() { return count != -1; }

    public void activate() {
        if (count == -1) {
            count = 0;
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchLockBlockEntity be) {
        if (!(level instanceof ServerLevel server) || be.count == -1) return;
        be.count++;
        if (be.count % 5 == 0) {
            server.playSound(null, pos, TC4Sounds.event("pump"), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (be.count > 100) {
            server.playSound(null, pos, TC4Sounds.event("ice"), SoundSource.BLOCKS, 1.0F, 1.0F);
            TC4EldritchLockBossSpawner.spawnFromLock(server, pos);
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
                if (server.getBlockState(p).is(ThaumcraftMod.ELDRITCH_NOTHING.get())) {
                    ThaumcraftNetwork.sendBlockZap(server, pos, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, p.getX()+0.5D, p.getY()+0.5D, p.getZ()+0.5D);
                    server.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            server.removeBlock(pos, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COUNT_TAG, count);
        tag.putString("TC4Tile", "TileEldritchLock");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        count = tag.contains(COUNT_TAG) ? tag.getInt(COUNT_TAG) : -1;
    }
}
