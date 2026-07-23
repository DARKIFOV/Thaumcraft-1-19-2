package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.BellowsBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact TC4 TileBellows behavior with modern block-state orientation. */
public final class BellowsBlockEntity extends BlockEntity {
    private float inflation = TC4ArcaneBellowsParity.MAX_INFLATION;
    private boolean expanding;
    private boolean firstRun = true;
    private boolean onVanillaFurnace;
    private int delay;
    private int loadedLegacyOrientation = -1;

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.BELLOWS_BLOCK_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        if (bellows.firstRun) {
            bellows.inflation = TC4ArcaneBellowsParity.initialInflation(level.random.nextFloat());
            bellows.firstRun = false;
        }

        TC4ArcaneBellowsParity.AnimationStep step =
                TC4ArcaneBellowsParity.animationStep(bellows.inflation, bellows.expanding);
        bellows.inflation = step.inflation();
        bellows.expanding = step.expanding();
        if (step.playSound()) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS,
                    TC4ArcaneBellowsParity.SOUND_VOLUME,
                    TC4ArcaneBellowsParity.soundPitch(level.random.nextFloat(), level.random.nextFloat()),
                    false);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (!bellows.onVanillaFurnace || level.hasNeighborSignal(pos)) {
            return;
        }
        bellows.delay++;
        if (bellows.delay < TC4ArcaneBellowsParity.VANILLA_FURNACE_DELAY_TICKS) {
            return;
        }
        bellows.delay = 0;
        bellows.accelerateVanillaFurnace();
    }

    public float inflation() {
        return inflation;
    }

    public boolean expanding() {
        return expanding;
    }

    public int delay() {
        return delay;
    }

    public boolean isOnVanillaFurnace() {
        return onVanillaFurnace;
    }

    /** Mirrors BlockWoodenDeviceItem's placement-time furnace/lit-furnace detection. */
    public void refreshAttachment() {
        if (level == null) {
            return;
        }
        Direction facing = facing();
        onVanillaFurnace = level.getBlockState(worldPosition.relative(facing)).is(Blocks.FURNACE);
        setChanged();
    }

    /**
     * TileBellows used only X/Z offsets while ticking a vanilla furnace. This
     * intentionally leaves vertical orientations targeting the bellows block itself.
     */
    public BlockPos legacyVanillaFurnacePos() {
        TC4ArcaneBellowsParity.Offset offset =
                TC4ArcaneBellowsParity.legacyVanillaFurnaceOffset(facing().get3DDataValue());
        return worldPosition.offset(offset.x(), offset.y(), offset.z());
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(BellowsBlock.FACING) ? state.getValue(BellowsBlock.FACING) : Direction.NORTH;
    }

    private void accelerateVanillaFurnace() {
        if (level == null) {
            return;
        }
        BlockPos furnacePos = legacyVanillaFurnacePos();
        if (!level.getBlockState(furnacePos).is(Blocks.FURNACE)
                || !(level.getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity furnace)) {
            return;
        }

        CompoundTag tag = furnace.saveWithoutMetadata();
        int cookTime = tag.getShort("CookTime");
        if (TC4ArcaneBellowsParity.canAdvanceVanillaFurnace(cookTime)) {
            tag.putShort("CookTime", (short) (cookTime + 1));
            furnace.load(tag);
            furnace.setChanged();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && loadedLegacyOrientation >= 0 && loadedLegacyOrientation < Direction.values().length
                && getBlockState().hasProperty(BellowsBlock.FACING)) {
            Direction legacy = Direction.from3DDataValue(loadedLegacyOrientation);
            if (getBlockState().getValue(BellowsBlock.FACING) != legacy) {
                level.setBlock(worldPosition, getBlockState().setValue(BellowsBlock.FACING, legacy), Block.UPDATE_CLIENTS);
            }
        }
        loadedLegacyOrientation = -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("orientation", (byte) facing().get3DDataValue());
        tag.putBoolean("onVanillaFurnace", onVanillaFurnace);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("orientation", Tag.TAG_BYTE)) {
            loadedLegacyOrientation = Byte.toUnsignedInt(tag.getByte("orientation"));
        }
        onVanillaFurnace = tag.contains("onVanillaFurnace", Tag.TAG_BYTE)
                ? tag.getBoolean("onVanillaFurnace")
                : tag.getBoolean("OnVanillaFurnace");
        // delay, inflation, direction and first-run are transient in TileBellows.
        delay = 0;
        inflation = TC4ArcaneBellowsParity.MAX_INFLATION;
        expanding = false;
        firstRun = true;
    }
}
