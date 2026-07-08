package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InfusionMatrixAuxiliaryHelper {
    public static final int AUXILIARY_RADIUS = 8;

    private InfusionMatrixAuxiliaryHelper() {
    }

    public static MatrixAuxiliaryReport analyze(Level level, BlockPos matrixPos, ArcanePedestalBlockEntity catalystPedestal, InfusionRecipe recipe) {
        BlockPos center = catalystPedestal == null ? matrixPos.below(2) : catalystPedestal.getBlockPos();

        int accelerators = 0;
        Set<BlockPos> stabilizers = new HashSet<>();

        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-AUXILIARY_RADIUS, -2, -AUXILIARY_RADIUS), center.offset(AUXILIARY_RADIUS, 3, AUXILIARY_RADIUS))) {
            BlockState state = level.getBlockState(scan);

            if (state.is(ThaumcraftMod.MATRIX_ACCELERATOR.get())) {
                accelerators++;
            }

            if (state.is(ThaumcraftMod.MATRIX_STABILIZER.get())) {
                stabilizers.add(scan.immutable());
            }
        }

        int symmetricStabilizers = 0;

        for (BlockPos stabilizer : stabilizers) {
            int dx = stabilizer.getX() - center.getX();
            int dy = stabilizer.getY() - center.getY();
            int dz = stabilizer.getZ() - center.getZ();

            if (dx == 0 && dz == 0) {
                continue;
            }

            BlockPos mirror = new BlockPos(center.getX() - dx, center.getY() + dy, center.getZ() - dz);

            if (stabilizers.contains(mirror)) {
                symmetricStabilizers++;
            }
        }

        boolean aspectPowered = false;

        if (recipe != null) {
            List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, matrixPos);
            aspectPowered = InfusionProcessHelper.hasAspects(jars, recipe.aspectCostFor(catalystPedestal == null ? net.minecraft.world.item.ItemStack.EMPTY : catalystPedestal.stored()));
        }

        return new MatrixAuxiliaryReport(
                Math.min(4, accelerators),
                stabilizers.size(),
                Math.min(4, symmetricStabilizers),
                aspectPowered
        );
    }
}
