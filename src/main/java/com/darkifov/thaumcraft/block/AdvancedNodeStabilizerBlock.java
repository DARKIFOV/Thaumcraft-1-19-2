package com.darkifov.thaumcraft.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Stage132: advanced variant of the TC4 node stabilizer.
 *
 * In TC4 the advanced stabilizer is used when a player wants to lock down more
 * dangerous nodes before moving or energizing them. The 1.19.2 port keeps the
 * visible behaviour simple but makes the runtime effect different from the
 * normal stabilizer: larger scan radius, stronger stability recovery and
 * permission for hungry/unstable node preservation.
 */
public class AdvancedNodeStabilizerBlock extends NodeStabilizerBlock {
    public AdvancedNodeStabilizerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
