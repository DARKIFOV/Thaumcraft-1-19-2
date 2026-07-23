package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.InfernalFurnaceBlock;
import com.darkifov.thaumcraft.block.TC4InfernalFurnaceParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** TC4 TileArcaneFurnaceNozzle: Ignis-only suction and 600-tick acceleration credits. */
public final class InfernalFurnaceNozzleBlockEntity extends BlockEntity {
    private int drawDelay;

    public InfernalFurnaceNozzleBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.INFERNAL_FURNACE_NOZZLE_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfernalFurnaceNozzleBlockEntity nozzle) {
        if (level.isClientSide || ++nozzle.drawDelay % TC4InfernalFurnaceParity.NOZZLE_DRAW_INTERVAL_TICKS != 0) return;
        InfernalFurnaceBlockEntity furnace = nozzle.furnace();
        if (furnace == null || furnace.speedyTime() >= TC4InfernalFurnaceParity.NOZZLE_DRAW_ADMISSION_SPEED) return;
        Direction outward = state.getValue(InfernalFurnaceBlock.FACING);
        if (level.getBlockEntity(pos.relative(outward)) instanceof EssentiaTubeBlockEntity tube
                && tube.getSuctionAmount(outward.getOpposite()) < nozzle.suctionAmount(outward)
                && tube.takeEssentiaOriginal(Aspect.IGNIS, 1, outward.getOpposite()) == 1) {
            nozzle.acceptFromTube(Aspect.IGNIS, 1);
        }
    }

    public Aspect suctionType(Direction face) {
        return Aspect.IGNIS;
    }

    public int suctionAmount(Direction face) {
        InfernalFurnaceBlockEntity furnace = furnace();
        return furnace != null && furnace.speedyTime() < TC4InfernalFurnaceParity.NOZZLE_SUCTION_ADMISSION_SPEED
                ? TC4InfernalFurnaceParity.NOZZLE_SUCTION : 0;
    }

    public int acceptFromTube(Aspect aspect, int amount) {
        if (aspect != Aspect.IGNIS || amount <= 0) return 0;
        InfernalFurnaceBlockEntity furnace = furnace();
        if (furnace == null || furnace.speedyTime() >= TC4InfernalFurnaceParity.NOZZLE_DRAW_ADMISSION_SPEED) return 0;
        furnace.addSpeedyTime(TC4InfernalFurnaceParity.IGNIS_ESSENTIA_SPEED_TICKS);
        return 1;
    }

    public InfernalFurnaceBlockEntity furnace() {
        if (level == null || !getBlockState().hasProperty(InfernalFurnaceBlock.FACING)) return null;
        Direction outward = getBlockState().getValue(InfernalFurnaceBlock.FACING);
        BlockEntity be = level.getBlockEntity(worldPosition.relative(outward.getOpposite()));
        return be instanceof InfernalFurnaceBlockEntity furnace ? furnace : null;
    }
}
