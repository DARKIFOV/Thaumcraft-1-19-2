package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class InfusionMatrixAuxiliaryHelper {
    public static final int AUXILIARY_RADIUS = 8;

    private InfusionMatrixAuxiliaryHelper() {
    }

    public static MatrixAuxiliaryReport analyze(Level level, BlockPos matrixPos, ArcanePedestalBlockEntity catalystPedestal, InfusionRecipe recipe) {
        BlockPos center = catalystPedestal == null ? matrixPos.below(2) : catalystPedestal.getBlockPos();

        int accelerators = 0;

        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-AUXILIARY_RADIUS, -2, -AUXILIARY_RADIUS), center.offset(AUXILIARY_RADIUS, 3, AUXILIARY_RADIUS))) {
            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.MATRIX_ACCELERATOR.get())) {
                accelerators++;
            }
        }

        // Stage723-742: stabilizer parity is now delegated to the TC4 symmetry
        // bridge so matrix runtime, structure analysis and NBT snapshots all agree.
        // v7.82: TC4 TileInfusionMatrix#getSurroundings mirrors stabilizers around the matrix block itself, not the catalyst pedestal.
        TC4InfusionStabilityParity.StabilitySnapshot stability = TC4InfusionStabilityParity.scan(level, matrixPos);

        boolean aspectPowered = false;

        if (recipe != null) {
            List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, matrixPos);
            aspectPowered = InfusionProcessHelper.hasAspects(jars, recipe.aspectCostFor(catalystPedestal == null ? net.minecraft.world.item.ItemStack.EMPTY : catalystPedestal.stored()));
        }

        return new MatrixAuxiliaryReport(
                Math.min(4, accelerators),
                stability.positions().size(),
                stability.effectivePairs(),
                aspectPowered,
                stability.unpaired(),
                stability.signature()
        );
    }
}
