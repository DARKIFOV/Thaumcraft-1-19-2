package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.infusion.InfusionStabilizer;
import net.minecraft.world.level.block.Block;

/**
 * TC4 BlockCrystal parity shell.
 *
 * <p>The original crystal-cluster block implemented IInfusionStabiliser. The
 * 1.19.2 port uses separate registry blocks for the six primal colours and the
 * balanced cluster, but all of them must retain the same infusion marker.</p>
 */
public class CrystalClusterBlock extends Block implements InfusionStabilizer {
    public CrystalClusterBlock(Properties properties) {
        super(properties);
    }
}
