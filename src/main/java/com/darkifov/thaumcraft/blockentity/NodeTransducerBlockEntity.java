package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client animation state for TC4's TileNodeConverter (Node Transducer).
 * The original counter extends from 0 to 50 and drives the shared
 * node_stabilizer.obj lock/piston mesh.
 */
public final class NodeTransducerBlockEntity extends BlockEntity {
    public static final int MAX_EXTENSION_TICKS = 50;

    private float previousExtensionTicks;
    private float extensionTicks;
    private int status;

    public NodeTransducerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.NODE_TRANSDUCER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
                                  NodeTransducerBlockEntity transducer) {
        transducer.previousExtensionTicks = transducer.extensionTicks;
        // Original TC4 stack: transducer above, aura node directly below, stabilizer two blocks below.
        boolean hasNode = level.getBlockEntity(pos.below()) instanceof AuraNodeBlockEntity;
        boolean powered = level.hasNeighborSignal(pos);

        if (hasNode && powered) {
            transducer.extensionTicks = Math.min(MAX_EXTENSION_TICKS, transducer.extensionTicks + 1.0F);
            transducer.status = 1; // original orange charging state
        } else {
            transducer.extensionTicks = Math.max(0.0F, transducer.extensionTicks - 1.0F);
            transducer.status = hasNode ? 0 : 2; // green ready / red missing target
        }
    }

    public float extensionTicks(float partialTick) {
        return Mth.lerp(partialTick, previousExtensionTicks, extensionTicks);
    }

    /** Original TileNodeConverter uses min(50,count)/137 as piston offset. */
    public float pistonOffset(float partialTick) {
        return Math.min(MAX_EXTENSION_TICKS, extensionTicks(partialTick)) / 137.0F;
    }

    public int status() {
        return status;
    }
}
