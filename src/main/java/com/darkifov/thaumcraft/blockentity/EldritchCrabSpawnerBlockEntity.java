package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EldritchCrabSpawnerBlock;
import com.darkifov.thaumcraft.entity.EldritchCrabEntity;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Stage220 parity runtime for original TileEldritchCrabSpawner: count=150,
 * count==15 vent, 50+rand(50) dry reset, 150+rand(100) post-spawn reset,
 * player range 16, entity cap >5 inside 32 blocks, NBT byte `facing`.
 */
public class EldritchCrabSpawnerBlockEntity extends BlockEntity {
    public static final String COUNT_TAG = "count";
    public static final String TICKS_TAG = "ticks";
    public static final String FACING_TAG = "facing";
    public static final int ORIGINAL_START_COUNT = 150;
    public static final int ORIGINAL_WARMUP_EVENT = 15;
    public static final int ORIGINAL_MIN_RESET = 50;
    public static final int ORIGINAL_MAX_EXTRA_RESET = 50;
    public static final int ORIGINAL_POST_SPAWN_COUNT = 150;
    public static final int ORIGINAL_POST_SPAWN_EXTRA = 100;
    public static final double ORIGINAL_PLAYER_RANGE = 16.0D;
    public static final double ORIGINAL_ENTITY_RANGE = 32.0D;
    public static final int ORIGINAL_MAX_CRABS = 5;

    private int count = ORIGINAL_START_COUNT;
    private int ticks = 0;
    private int venting = 0;

    public EldritchCrabSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_CRAB_SPAWNER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchCrabSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        be.tickCommon(level.random);
        be.count--;
        if (be.count < 0) {
            be.count = ORIGINAL_MIN_RESET + level.random.nextInt(ORIGINAL_MAX_EXTRA_RESET);
            return;
        }
        if (be.count == ORIGINAL_WARMUP_EVENT && be.isActivated(serverLevel) && !be.maxEntitiesReached(serverLevel)) {
            serverLevel.blockEvent(pos, state.getBlock(), 1, 0);
            serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        if (be.count <= 0 && be.isActivated(serverLevel) && !be.maxEntitiesReached(serverLevel)) {
            be.count = ORIGINAL_POST_SPAWN_COUNT + level.random.nextInt(ORIGINAL_POST_SPAWN_EXTRA);
            be.spawnCrab(serverLevel, state);
            serverLevel.playSound(null, pos, TC4Sounds.event("gore"), SoundSource.HOSTILE, 0.5F, 1.0F);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, EldritchCrabSpawnerBlockEntity be) {
        be.tickCommon(level.random);
        if (be.venting > 0) {
            be.venting--;
            for (int i = 0; i < 3; i++) be.drawVent(level, state);
        } else if (level.random.nextInt(20) == 0) {
            be.drawVent(level, state);
        }
    }

    private void tickCommon(RandomSource random) {
        if (ticks == 0) ticks = random.nextInt(500);
        ticks++;
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) { venting = 20; return true; }
        return super.triggerEvent(id, type);
    }

    private boolean isActivated(ServerLevel level) {
        Player player = level.getNearestPlayer(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, ORIGINAL_PLAYER_RANGE, false);
        return player != null;
    }

    private boolean maxEntitiesReached(ServerLevel level) {
        return level.getEntitiesOfClass(EldritchCrabEntity.class, new AABB(worldPosition).inflate(ORIGINAL_ENTITY_RANGE)).size() > ORIGINAL_MAX_CRABS;
    }

    private void spawnCrab(ServerLevel level, BlockState state) {
        Direction dir = state.hasProperty(EldritchCrabSpawnerBlock.FACING) ? state.getValue(EldritchCrabSpawnerBlock.FACING) : Direction.NORTH;
        EldritchCrabEntity crab = ThaumcraftMod.ELDRITCH_CRAB.get().create(level);
        if (crab == null) return;
        crab.moveTo(worldPosition.getX() + dir.getStepX() + 0.5D, worldPosition.getY() + dir.getStepY() + 0.5D, worldPosition.getZ() + dir.getStepZ() + 0.5D, 0.0F, 0.0F);
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(crab.blockPosition()), MobSpawnType.SPAWNER, null, null);
        crab.setHelm(false); // original spawner always strips the helm
        crab.setDeltaMovement(dir.getStepX() * 0.2D, dir.getStepY() * 0.2D, dir.getStepZ() * 0.2D);
        level.addFreshEntity(crab);
    }

    private void drawVent(Level level, BlockState state) {
        Direction dir = state.hasProperty(EldritchCrabSpawnerBlock.FACING) ? state.getValue(EldritchCrabSpawnerBlock.FACING) : Direction.NORTH;
        RandomSource random = level.random;
        double fx = 0.15D - random.nextDouble() * 0.3D;
        double fy = 0.15D - random.nextDouble() * 0.3D;
        double fz = 0.15D - random.nextDouble() * 0.3D;
        double mx = dir.getStepX() / 3.0D + 0.1D - random.nextDouble() * 0.2D;
        double my = dir.getStepY() / 3.0D + 0.1D - random.nextDouble() * 0.2D;
        double mz = dir.getStepZ() / 3.0D + 0.1D - random.nextDouble() * 0.2D;
        level.addParticle(ParticleTypes.SMOKE,
                worldPosition.getX() + 0.5D + fx + dir.getStepX() / 2.1D,
                worldPosition.getY() + 0.5D + fy + dir.getStepY() / 2.1D,
                worldPosition.getZ() + 0.5D + fz + dir.getStepZ() / 2.1D,
                mx, my, mz);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COUNT_TAG, count);
        tag.putInt(TICKS_TAG, ticks);
        Direction dir = getBlockState().hasProperty(EldritchCrabSpawnerBlock.FACING) ? getBlockState().getValue(EldritchCrabSpawnerBlock.FACING) : Direction.NORTH;
        tag.putByte(FACING_TAG, (byte)dir.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        count = tag.contains(COUNT_TAG) ? tag.getInt(COUNT_TAG) : ORIGINAL_START_COUNT;
        ticks = tag.getInt(TICKS_TAG);
    }
}
