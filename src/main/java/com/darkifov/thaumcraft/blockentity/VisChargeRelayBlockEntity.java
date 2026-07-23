package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact TileMagicWorkbenchCharger adapter: a relay child that feeds 5 cv per aspect per tick. */
public class VisChargeRelayBlockEntity extends VisRelayBlockEntity {
    private static final int MAX_TRANSFER_CENTIVIS = 5;

    public VisChargeRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.VIS_CHARGE_RELAY_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VisChargeRelayBlockEntity charger) {
        VisRelayBlockEntity.serverTick(level, pos, state, charger);
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockEntity below = level.getBlockEntity(pos.below());
        if (!(below instanceof ArcaneWorkbenchBlockEntity workbench)) return;
        ItemStack wand = workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND);
        if (!(wand.getItem() instanceof WandItem wandItem) || WandItem.isStaffStack(wand)
                || WandItem.hasInfiniteVis(wand)) return;

        int capacity = wandItem.stackVisCapacity(wand);
        boolean changed = false;
        for (Aspect aspect : WandItem.primalVisAspects()) {
            int requested = Math.min(MAX_TRANSFER_CENTIVIS,
                    Math.max(0, capacity - WandItem.getVis(wand, aspect)));
            if (requested <= 0) continue;
            int drained = AuraVisRelayNetwork.drainFromRelay(serverLevel, pos, aspect, requested);
            if (drained > 0) {
                WandItem.addRealVis(wand, aspect, drained);
                changed = true;
            }
        }
        if (changed) workbench.setChanged();
    }
}
