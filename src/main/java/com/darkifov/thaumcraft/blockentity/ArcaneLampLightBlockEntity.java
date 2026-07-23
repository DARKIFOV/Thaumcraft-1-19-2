package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Migration-only carrier for early rebuild saves.
 *
 * <p>TC4's TileArcaneLampLight class was never registered or bound to blockAiry
 * metadata 3. New light markers intentionally have no BlockEntity and no
 * self-cleanup ticker; keeping this type avoids hard failures when old port NBT
 * is encountered.</p>
 */
@Deprecated
public final class ArcaneLampLightBlockEntity extends BlockEntity {
    public ArcaneLampLightBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_LAMP_LIGHT_BLOCK_ENTITY.get(), pos, state);
    }
}
