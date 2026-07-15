package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Runtime logic of TC4 TileMagicWorkbenchCharger. */
public class VisChargeRelayBlockEntity extends BlockEntity {
    private static final int MAX_TRANSFER_CENTIVIS = 500;
    private static final int PULSE_TICKS = 5;

    private Aspect pulseAspect;
    private long pulseStartGameTime = Long.MIN_VALUE;

    public VisChargeRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.VIS_CHARGE_RELAY_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VisChargeRelayBlockEntity charger) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (!(below instanceof ArcaneWorkbenchBlockEntity workbench)) {
            return;
        }
        ItemStack wand = workbench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND);
        if (!(wand.getItem() instanceof WandItem wandItem) || WandItem.isStaffStack(wand)
                || WandItem.hasInfiniteVis(wand)) {
            return;
        }

        int capacity = wandItem.stackVisCapacity(wand);
        boolean changed = false;
        for (Aspect aspect : WandItem.primalVisAspects()) {
            int room = capacity - WandItem.getVis(wand, aspect);
            int requested = Math.min(MAX_TRANSFER_CENTIVIS, Math.max(0, room));
            requested -= requested % 100;
            if (requested < 100) {
                continue;
            }
            int drained = AuraVisRelayNetwork.drainMachineVis(serverLevel, pos, aspect, requested);
            if (drained > 0) {
                WandItem.addRealVis(wand, aspect, drained);
                charger.triggerPulse(aspect);
                changed = true;
            }
        }
        if (changed) {
            workbench.setChanged();
        }
    }
    private void triggerPulse(Aspect aspect) {
        if (level == null || level.isClientSide || aspect == null) {
            return;
        }
        long now = level.getGameTime();
        if (pulseStartGameTime != Long.MIN_VALUE && now - pulseStartGameTime < PULSE_TICKS) {
            return;
        }
        pulseAspect = aspect;
        pulseStartGameTime = now;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public Aspect pulseAspect() {
        return pulseAspect;
    }

    public float pulseStrength(float partialTick) {
        if (level == null || pulseAspect == null || pulseStartGameTime == Long.MIN_VALUE) {
            return 0.0F;
        }
        float age = (level.getGameTime() + partialTick) - pulseStartGameTime;
        if (age < 0.0F || age >= PULSE_TICKS) {
            return 0.0F;
        }
        return 1.0F - age / PULSE_TICKS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("PulseAspect", pulseAspect == null ? "" : pulseAspect.id());
        tag.putLong("PulseStart", pulseStartGameTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        pulseAspect = Aspect.byId(tag.getString("PulseAspect"));
        pulseStartGameTime = tag.contains("PulseStart") ? tag.getLong("PulseStart") : Long.MIN_VALUE;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
