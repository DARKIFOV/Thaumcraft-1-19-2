package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client animation state for the TC4 node stabilizer.
 *
 * <p>The original TileNodeStabilizer only extends its four pistons when a real
 * aura node is located exactly one block above it and the stabilizer is not
 * receiving redstone power. Its animation counter moves between 0 and 37.</p>
 */
public final class NodeStabilizerBlockEntity extends BlockEntity {
    public static final int MAX_EXTENSION_TICKS = 37;

    private float previousExtensionTicks;
    private float extensionTicks;

    public NodeStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.NODE_STABILIZER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, NodeStabilizerBlockEntity stabilizer) {
        stabilizer.previousExtensionTicks = stabilizer.extensionTicks;
        boolean hasNode = level.getBlockEntity(pos.above()) instanceof AuraNodeBlockEntity;
        boolean powered = level.hasNeighborSignal(pos);
        if (hasNode && !powered) {
            stabilizer.extensionTicks = Math.min(MAX_EXTENSION_TICKS, stabilizer.extensionTicks + 1.0F);
        } else {
            stabilizer.extensionTicks = Math.max(0.0F, stabilizer.extensionTicks - 1.0F);
        }
    }

    /** Returns the original TC4 animation counter, interpolated in the 0..37 range. */
    public float extensionTicks(float partialTick) {
        return Mth.lerp(partialTick, previousExtensionTicks, extensionTicks);
    }

    /** Returns a normalized 0..1 extension value for field effects and diagnostics. */
    public float extension(float partialTick) {
        return extensionTicks(partialTick) / MAX_EXTENSION_TICKS;
    }

    public int lockLevel() {
        return getBlockState().is(ThaumcraftMod.ADVANCED_NODE_STABILIZER.get()) ? 2 : 1;
    }
}
