package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stage273-282: TC4 TileEldritchTrap parity tick: delay 10+rand(25), range 3, magic damage 2 and warp chance. */
public class EldritchTrapBlockEntity extends BlockEntity {
    public static final String COUNT_TAG = "count";
    private int count = 20;

    public EldritchTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_TRAP_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchTrapBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;
        if (be.count-- > 0) return;
        be.count = 10 + level.random.nextInt(25);
        Player player = server.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 3.0D, false);
        if (player != null) {
            player.hurt(DamageSource.MAGIC, 2.0F);
            if (level.random.nextBoolean()) {
                PlayerThaumData.addWarpTemporary(player, 1 + level.random.nextInt(2));
            }
            ThaumcraftNetwork.sendBlockZap(server, pos,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COUNT_TAG, count);
        tag.putString("TC4Tile", "TileEldritchTrap");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        count = tag.contains(COUNT_TAG) ? tag.getInt(COUNT_TAG) : 20;
    }
}
