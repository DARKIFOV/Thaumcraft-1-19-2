package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.TC4WandPedestalBlock;
import com.darkifov.thaumcraft.block.WandItem;
import com.darkifov.thaumcraft.item.simple.TC4VisAmuletItem;
import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandComponentData;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Server-authoritative port of TC4 TileWandPedestal. */
public final class TC4WandPedestalBlockEntity extends BlockEntity {
    private static final int NODE_RADIUS = 8;
    private static final int CHARGE_INTERVAL = 5;
    private static final int RESCAN_INTERVAL = 100;
    private static final int CENTIVIS_PER_NODE_POINT = 100;
    private static final int DRAIN_VISUAL_TICKS = CHARGE_INTERVAL + 2;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return TC4WandPedestalBlock.accepts(stack);
        }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) { markAndSync(); }
    };
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inventory);
    private final List<BlockPos> nodes = new ArrayList<>();
    private int chargeCounter;
    private int rescanCounter;
    @Nullable private BlockPos drainSource;
    private int drainColor = 0xFFFFFF;
    private long drainStartedAt = Long.MIN_VALUE;

    public TC4WandPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.TC4_WAND_PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TC4WandPedestalBlockEntity pedestal) {
        if (++pedestal.rescanCounter >= RESCAN_INTERVAL || pedestal.nodes.isEmpty()) {
            pedestal.rescanCounter = 0;
            pedestal.rescanNodes();
        }
        if (++pedestal.chargeCounter >= CHARGE_INTERVAL) {
            pedestal.chargeCounter = 0;
            pedestal.chargeOneVis();
        }
    }

    public ItemStack stored() { return inventory.getStackInSlot(0); }
    public void setStored(ItemStack stack) {
        ItemStack one = stack.copy();
        one.setCount(Math.min(1, one.getCount()));
        inventory.setStackInSlot(0, one);
    }
    public ItemStack removeStored() { return inventory.extractItem(0, 1, false); }

    private void rescanNodes() {
        nodes.clear();
        if (level == null) return;
        for (BlockPos candidate : BlockPos.betweenClosed(worldPosition.offset(-NODE_RADIUS, -NODE_RADIUS, -NODE_RADIUS),
                worldPosition.offset(NODE_RADIUS, NODE_RADIUS, NODE_RADIUS))) {
            if (level.getBlockEntity(candidate) instanceof AuraNodeBlockEntity node && !node.isJarredNode()) {
                nodes.add(candidate.immutable());
            }
        }
    }

    private void chargeOneVis() {
        if (level == null) return;
        ItemStack target = stored();
        if (!TC4WandPedestalBlock.accepts(target)) return;
        int minimum = minimumNodeReserve(target);

        for (BlockPos nodePos : new ArrayList<>(nodes)) {
            if (!(level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) || node.isJarredNode()) {
                nodes.remove(nodePos);
                continue;
            }
            for (Aspect primal : WandItem.primalVisAspects()) {
                if (hasRoom(target, primal) && node.aspects().get(primal) > minimum
                        && node.drainForPedestal(primal, 1) == 1) {
                    addOneVis(target, primal);
                    recordDrain(nodePos, primal);
                    return;
                }
            }
        }

        if (!level.getBlockState(worldPosition.above()).is(ThaumcraftMod.TC4_WAND_PEDESTAL_FOCUS.get())) return;
        for (BlockPos nodePos : new ArrayList<>(nodes)) {
            if (!(level.getBlockEntity(nodePos) instanceof AuraNodeBlockEntity node) || node.isJarredNode()) continue;
            for (Map.Entry<Aspect, Integer> entry : node.aspects().entries().entrySet()) {
                Aspect compound = entry.getKey();
                if (compound.isPrimal() || entry.getValue() <= minimum) continue;
                Aspect available = firstPrimalWithRoom(target, compound);
                if (available != null && node.drainForPedestal(compound, 1) == 1) {
                    addOneVis(target, available);
                    recordDrain(nodePos, compound);
                    return;
                }
            }
        }
    }

    private static int minimumNodeReserve(ItemStack target) {
        if (target.getItem() instanceof WandItem) {
            WandComponentData data = WandComponentData.from(target);
            return data.rod() == WandRodType.WOOD || data.cap() == WandCapType.IRON ? 0 : 1;
        }
        return 1;
    }

    private static Aspect firstPrimalWithRoom(ItemStack target, Aspect aspect) {
        if (aspect == null) return null;
        if (aspect.isPrimal()) return hasRoom(target, aspect) ? aspect : null;
        Aspect first = firstPrimalWithRoom(target, aspect.firstComponent());
        return first != null ? first : firstPrimalWithRoom(target, aspect.secondComponent());
    }

    private static boolean hasRoom(ItemStack target, Aspect aspect) {
        if (target.getItem() instanceof WandItem wand) {
            return WandItem.getVis(target, aspect) < wand.stackVisCapacity(target);
        }
        if (target.getItem() instanceof TC4VisAmuletItem amulet) {
            return amulet.getVis(target, aspect) < amulet.capacity();
        }
        return false;
    }

    private static void addOneVis(ItemStack target, Aspect aspect) {
        if (target.getItem() instanceof WandItem) {
            WandItem.addRealVis(target, aspect, CENTIVIS_PER_NODE_POINT);
        } else if (target.getItem() instanceof TC4VisAmuletItem amulet) {
            amulet.addRealVis(target, aspect, CENTIVIS_PER_NODE_POINT);
        }
    }

    private void recordDrain(BlockPos source, Aspect drainedAspect) {
        drainSource = source.immutable();
        drainColor = drainedAspect == null ? 0xFFFFFF : drainedAspect.nativeColor();
        drainStartedAt = level == null ? 0L : level.getGameTime();
        markAndSync();
    }

    @Nullable public BlockPos drainSource() { return drainSource; }
    public int drainColor() { return drainColor; }
    public boolean isDrainVisualActive() {
        if (level == null || drainSource == null) return false;
        long age = level.getGameTime() - drainStartedAt;
        return age >= 0L && age <= DRAIN_VISUAL_TICKS;
    }

    /** TC4 expanded the pedestal TESR bounds so the node link is not culled at the block edge. */
    @Override public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.0D);
    }

    public int comparatorSignal() {
        ItemStack target = stored();
        if (!TC4WandPedestalBlock.accepts(target)) return 0;
        long current = 0;
        long maximum = 0;
        for (Aspect aspect : WandItem.primalVisAspects()) {
            if (target.getItem() instanceof WandItem wand) {
                current += WandItem.getVis(target, aspect);
                maximum += wand.stackVisCapacity(target);
            } else if (target.getItem() instanceof TC4VisAmuletItem amulet) {
                current += amulet.getVis(target, aspect);
                maximum += amulet.capacity();
            }
        }
        return maximum <= 0 ? 0 : Math.min(15, 1 + (int)Math.floor(14.0D * current / maximum));
    }

    public void markAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("ChargeCounter", chargeCounter);
        tag.putInt("RescanCounter", rescanCounter);
        if (drainSource != null) tag.putLong("DrainSource", drainSource.asLong());
        tag.putInt("DrainColor", drainColor);
        tag.putLong("DrainStartedAt", drainStartedAt);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        chargeCounter = tag.getInt("ChargeCounter");
        rescanCounter = tag.getInt("RescanCounter");
        drainSource = tag.contains("DrainSource") ? BlockPos.of(tag.getLong("DrainSource")) : null;
        drainColor = tag.contains("DrainColor") ? tag.getInt("DrainColor") : 0xFFFFFF;
        drainStartedAt = tag.contains("DrainStartedAt") ? tag.getLong("DrainStartedAt") : Long.MIN_VALUE;
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    @Nonnull @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); }
    @Override public void reviveCaps() { super.reviveCaps(); itemCapability = LazyOptional.of(() -> inventory); }
}
