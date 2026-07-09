package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import com.darkifov.thaumcraft.essentia.TC4DistillationRuntime;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemicalFurnaceBlockEntity extends BlockEntity {
    public static final int CAPACITY = 128;

    private final AspectList aspects = new AspectList();
    private final AspectList pendingAspects = new AspectList();

    private int fuelTime = 0;
    private int burnProgress = 0;
    private int burnDuration = 0;

    public AlchemicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ALCHEMICAL_FURNACE_BLOCK_ENTITY.get(), pos, state);
    }

    public AspectList aspects() {
        return aspects;
    }

    public AspectList pendingAspects() {
        return pendingAspects;
    }

    public int fuelTime() {
        return fuelTime;
    }

    public int burnProgress() {
        return burnProgress;
    }

    public int burnDuration() {
        return burnDuration;
    }

    public boolean active() {
        return burnDuration > 0 && !pendingAspects.isEmpty();
    }

    public int space() {
        return Math.max(0, CAPACITY - aspects.totalAmount());
    }

    public boolean canAccept(AspectList incoming) {
        return incoming != null && !incoming.isEmpty() && incoming.totalAmount() <= space();
    }

    public int addFuel(int amount) {
        if (amount <= 0) {
            return 0;
        }

        fuelTime = Math.min(6000, fuelTime + amount);
        setChangedAndSync();
        return amount;
    }

    public boolean startBurn(AspectList incoming) {
        if (incoming == null || incoming.isEmpty() || active() || !canAccept(incoming)) {
            return false;
        }

        pendingAspects.clear();
        pendingAspects.addAll(incoming);
        burnProgress = 0;
        burnDuration = 100 + incoming.totalAmount() * 10;
        setChangedAndSync();
        return true;
    }

    private void finishBurn() {
        addAllLimited(pendingAspects);
        pendingAspects.clear();
        burnProgress = 0;
        burnDuration = 0;
        setChangedAndSync();
    }

    public int addAllLimited(AspectList incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return 0;
        }

        int moved = 0;

        for (java.util.Map.Entry<Aspect, Integer> entry : incoming.entries().entrySet()) {
            if (space() <= 0) {
                break;
            }

            int amount = Math.min(entry.getValue(), space());
            aspects.add(entry.getKey(), amount);
            moved += amount;
        }

        if (moved > 0) {
            setChangedAndSync();
        }

        return moved;
    }

    public Aspect firstAspect() {
        return aspects.firstAspect();
    }

    public int removeUpTo(Aspect aspect, int amount) {
        int removed = aspects.removeUpTo(aspect, amount);

        if (removed > 0) {
            setChangedAndSync();
        }

        return removed;
    }

    private void tickServer() {
        if (!active()) {
            return;
        }

        if (fuelTime <= 0) {
            return;
        }

        fuelTime--;
        burnProgress++;

        if (burnProgress >= burnDuration) {
            finishBurn();
        } else {
            setChangedAndSync();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemicalFurnaceBlockEntity furnace) {
        furnace.tickServer();
        TC4DistillationRuntime.tickFurnaceToAlembics(level, pos, furnace);
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Aspects", aspects.save());
        tag.put("PendingAspects", pendingAspects.save());
        tag.putInt("FuelTime", fuelTime);
        tag.putInt("BurnProgress", burnProgress);
        tag.putInt("BurnDuration", burnDuration);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        aspects.clear();
        pendingAspects.clear();

        if (tag.contains("Aspects")) {
            aspects.load(tag.getCompound("Aspects"));
        }

        if (tag.contains("PendingAspects")) {
            pendingAspects.load(tag.getCompound("PendingAspects"));
        }

        fuelTime = tag.getInt("FuelTime");
        burnProgress = tag.getInt("BurnProgress");
        burnDuration = tag.getInt("BurnDuration");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
