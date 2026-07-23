package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.ArcaneLevitatorBlock;
import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Direct behavioral port of TC4 TileLifter. All fields are transient, as in the original. */
public final class ArcaneLevitatorBlockEntity extends BlockEntity {
    private int counter;
    private int rangeAbove;
    private boolean requiresUpdate = true;
    private boolean lastPowerState;

    public ArcaneLevitatorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_LEVITATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  ArcaneLevitatorBlockEntity levitator) {
        levitator.counter++;
        if (levitator.requiresUpdate
                || levitator.counter % TC4ArcaneLevitatorParity.REFRESH_INTERVAL_TICKS == 0) {
            levitator.lastPowerState = isPowered(level, pos);
            levitator.requiresUpdate = false;
            levitator.rangeAbove = computeRangeAbove(level, pos);
            if (state.hasProperty(ArcaneLevitatorBlock.POWERED)
                    && state.getValue(ArcaneLevitatorBlock.POWERED) != levitator.lastPowerState) {
                level.setBlock(pos, state.setValue(ArcaneLevitatorBlock.POWERED, levitator.lastPowerState),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
        }

        if (levitator.rangeAbove <= 0 || isPowered(level, pos)) {
            return;
        }

        AABB column = new AABB(pos.getX(), pos.getY() + 1.0D, pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.0D + levitator.rangeAbove, pos.getZ() + 1.0D);
        List<Entity> targets = level.getEntitiesOfClass(Entity.class, column,
                ArcaneLevitatorBlockEntity::canLift);
        for (Entity entity : targets) {
            Vec3 movement = entity.getDeltaMovement();
            double y = TC4ArcaneLevitatorParity.nextVerticalVelocity(
                    movement.y, entity.isShiftKeyDown());
            entity.setDeltaMovement(movement.x, y, movement.z);
            entity.fallDistance = 0.0F;
        }
    }

    private static boolean canLift(Entity entity) {
        return TC4ArcaneLevitatorParity.admitsEntity(
                entity instanceof ItemEntity,
                entity.isPushable(),
                entity instanceof AbstractHorse);
    }

    public static boolean isPowered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
    }

    public static int computeRangeAbove(Level level, BlockPos pos) {
        int lowerLevitators = 0;
        while (pos.getY() - 1 - lowerLevitators >= level.getMinBuildHeight()) {
            BlockPos below = pos.below(1 + lowerLevitators);
            if (!(level.getBlockState(below).getBlock() instanceof ArcaneLevitatorBlock)
                    || !TC4ArcaneLevitatorParity.lowerSegmentContributes(level.hasNeighborSignal(below))) {
                break;
            }
            lowerLevitators++;
        }

        int maximum = TC4ArcaneLevitatorParity.stackedMaximumRange(lowerLevitators);
        int range = 0;
        while (range < maximum && pos.getY() + 1 + range < level.getMaxBuildHeight()) {
            BlockPos above = pos.above(1 + range);
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.isSolidRender(level, above)) {
                break;
            }
            range++;
        }
        return range;
    }

    public void requestRangeUpdate() {
        requiresUpdate = true;
    }

    public int rangeAbove() {
        return rangeAbove;
    }

    public boolean lastPowerState() {
        return lastPowerState;
    }

    public boolean requiresUpdate() {
        return requiresUpdate;
    }
}
