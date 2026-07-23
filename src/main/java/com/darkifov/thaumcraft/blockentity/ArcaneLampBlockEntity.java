package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneLampBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneLampParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Full server-authoritative port of TC4 TileArcaneLamp. */
public final class ArcaneLampBlockEntity extends BlockEntity {
    private Direction loadedFacing = Direction.DOWN;
    private boolean hasLoadedFacing;

    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_LAMP_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity lamp) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        RandomSource random = serverLevel.getRandom();
        int x = pos.getX() + TC4ArcaneLampParity.triangularOffset(random.nextInt(16), random.nextInt(16));
        int sampledY = pos.getY() + TC4ArcaneLampParity.triangularOffset(random.nextInt(16), random.nextInt(16));
        int z = pos.getZ() + TC4ArcaneLampParity.triangularOffset(random.nextInt(16), random.nextInt(16));
        int y = TC4ArcaneLampParity.clampSampledY(sampledY,
                serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), serverLevel.getMinBuildHeight());
        BlockPos target = new BlockPos(x, y, z);
        BlockState targetState = TC4ArcaneLampParity.insideBuildHeight(y, serverLevel.getMinBuildHeight(), serverLevel.getMaxBuildHeight())
                ? serverLevel.getBlockState(target) : null;
        if (!TC4ArcaneLampParity.shouldPlaceLampLight(targetState != null && targetState.isAir(),
                targetState != null && targetState.is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get()),
                targetState == null ? 15 : serverLevel.getMaxLocalRawBrightness(target), targetState != null)) return;
        serverLevel.setBlock(target, ThaumcraftMod.ARCANE_LAMP_LIGHT.get().defaultBlockState(), 3);
    }

    /** TC4 removes every metadata-3 airy marker in the local 31x31x31 cube. */
    public void removeLights() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -TC4ArcaneLampParity.LIGHT_RADIUS; dx <= TC4ArcaneLampParity.LIGHT_RADIUS; dx++) {
            for (int dy = -TC4ArcaneLampParity.LIGHT_RADIUS; dy <= TC4ArcaneLampParity.LIGHT_RADIUS; dy++) {
                int y = worldPosition.getY() + dy;
                if (!TC4ArcaneLampParity.insideBuildHeight(y, serverLevel.getMinBuildHeight(), serverLevel.getMaxBuildHeight())) continue;
                for (int dz = -TC4ArcaneLampParity.LIGHT_RADIUS; dz <= TC4ArcaneLampParity.LIGHT_RADIUS; dz++) {
                    cursor.set(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                    if (serverLevel.getBlockState(cursor).is(ThaumcraftMod.ARCANE_LAMP_LIGHT.get())) {
                        serverLevel.removeBlock(cursor, false);
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        Direction facing = getBlockState().hasProperty(ArcaneLampBlock.FACING)
                ? getBlockState().getValue(ArcaneLampBlock.FACING) : Direction.DOWN;
        tag.putInt("orientation", facing.get3DDataValue());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("orientation", Tag.TAG_INT)) {
            loadedFacing = Direction.from3DDataValue(tag.getInt("orientation"));
            hasLoadedFacing = true;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (hasLoadedFacing && level != null && getBlockState().hasProperty(ArcaneLampBlock.FACING)
                && getBlockState().getValue(ArcaneLampBlock.FACING) != loadedFacing) {
            level.setBlock(worldPosition, getBlockState().setValue(ArcaneLampBlock.FACING, loadedFacing), 3);
        }
        hasLoadedFacing = false;
    }
}
