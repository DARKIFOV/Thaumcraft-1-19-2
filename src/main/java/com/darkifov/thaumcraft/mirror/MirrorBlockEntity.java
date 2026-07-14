package com.darkifov.thaumcraft.mirror;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.MirrorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Runtime port of TC4 TileMirror, including its delayed output queue and instability. */
public final class MirrorBlockEntity extends AbstractMirrorBlockEntity implements WorldlyContainer {
    private static final int[] SLOT = new int[]{0};
    private final List<ItemStack> outputStacks = new ArrayList<>();
    private int instability;
    private int mirrorTicks;
    private final IItemHandler insertionHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || level == null || level.isClientSide) {
                return stack;
            }
            if (simulate) {
                return ItemStack.EMPTY;
            }
            return routeInsertedStack(stack.copy()) ? ItemStack.EMPTY : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 && !stack.isEmpty();
        }
    };
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> insertionHandler);

    public MirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.MIRROR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected boolean acceptsPeer(AbstractMirrorBlockEntity peer) {
        return peer instanceof MirrorBlockEntity;
    }

    public int instability() {
        return instability;
    }

    public int queuedStackCount() {
        return outputStacks.size();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        mirror.mirrorTicks++;
        int tickRate = mirror.instability / 50;
        if (tickRate == 0 || mirror.mirrorTicks % Math.max(1, tickRate * tickRate) == 0) {
            mirror.ejectOne();
        }
        mirror.checkInstability();
        mirror.tickLinkLifecycle();
    }

    public boolean transport(ItemEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || entity.isRemoved() || entity.hasPickUpDelay()
                || !isLinkValid() || link == null) {
            return false;
        }
        ServerLevel targetLevel = link.resolveLevel(serverLevel);
        if (targetLevel == null || !targetLevel.hasChunkAt(link.pos())) {
            return false;
        }
        BlockEntity target = targetLevel.getBlockEntity(link.pos());
        if (!(target instanceof MirrorBlockEntity targetMirror)) {
            return false;
        }
        ItemStack transported = entity.getItem().copy();
        targetMirror.addStack(transported);
        addInstability(null, transported.getCount());
        entity.discard();
        setChangedAndSync();
        targetMirror.setChangedAndSync();
        level.playSound(null, worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.1F, 1.0F);
        return true;
    }

    public void addStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            outputStacks.add(stack.copy());
            setChangedAndSync();
        }
    }

    /** Hand mirrors bypass the queue, matching ItemHandMirror.transport in TC4. */
    public boolean spawnDirect(ItemStack stack) {
        return spawnOutFront(stack.copy());
    }

    private void ejectOne() {
        if (level == null || level.isClientSide || outputStacks.isEmpty() || mirrorTicks <= 20) {
            return;
        }
        int index = level.random.nextInt(outputStacks.size());
        ItemStack queued = outputStacks.get(index);
        if (queued.isEmpty()) {
            outputStacks.remove(index);
            setChangedAndSync();
            return;
        }
        ItemStack single = queued.copy();
        single.setCount(1);
        if (!spawnOutFront(single)) {
            return;
        }
        queued.shrink(1);
        addInstability(null, 1);
        if (queued.isEmpty()) {
            outputStacks.remove(index);
        }
        setChangedAndSync();
    }

    private boolean spawnOutFront(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return false;
        }
        Direction facing = getBlockState().hasProperty(MirrorBlock.FACING)
                ? getBlockState().getValue(MirrorBlock.FACING) : Direction.NORTH;
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 center = Vec3.atCenterOf(worldPosition).subtract(normal.scale(0.3D));
        ItemEntity entity = new ItemEntity(serverLevel, center.x, center.y, center.z, stack);
        entity.setDeltaMovement(normal.scale(0.15D));
        entity.setPickUpDelay(20);
        if (!serverLevel.addFreshEntity(entity)) {
            return false;
        }
        serverLevel.playSound(null, worldPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.1F, 1.0F);
        return true;
    }

    private void checkInstability() {
        if (!(level instanceof ServerLevel serverLevel) || instability <= 0) {
            return;
        }
        if (mirrorTicks % 20 == 0) {
            instability = Math.max(0, instability - 1);
            setChangedAndSync();
        }
        if (instability <= 0) {
            return;
        }
        int drained = AuraVisRelayNetwork.drainMachineVis(serverLevel, worldPosition, Aspect.ORDO, 100);
        if (drained >= 100) {
            addInstability(serverLevel, -1);
        }
    }

    private void addInstability(@Nullable ServerLevel sourceLevel, int amount) {
        instability = Math.max(0, instability + amount);
        setChangedAndSync();
        if (sourceLevel == null || link == null) {
            return;
        }
        ServerLevel targetLevel = link.resolveLevel(sourceLevel);
        if (targetLevel == null || !targetLevel.hasChunkAt(link.pos())) {
            return;
        }
        BlockEntity target = targetLevel.getBlockEntity(link.pos());
        if (target instanceof MirrorBlockEntity mirror) {
            mirror.instability = Math.max(0, mirror.instability + amount);
            mirror.setChangedAndSync();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("instability", instability);
        ListTag items = new ListTag();
        for (int i = 0; i < outputStacks.size(); i++) {
            ItemStack stack = outputStacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) i);
            stack.save(itemTag);
            items.add(itemTag);
        }
        tag.put("Items", items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        instability = Math.max(0, tag.getInt("instability"));
        outputStacks.clear();
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = ItemStack.of(items.getCompound(i));
            if (!stack.isEmpty()) {
                outputStacks.add(stack);
            }
        }
    }

    // TC4 exposed a virtual one-slot inventory so hoppers could send stacks through a linked mirror.
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return true; }
    @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0 && !stack.isEmpty()) {
            routeInsertedStack(stack.copy());
        }
    }

    private boolean routeInsertedStack(ItemStack stack) {
        if (stack.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (isLinkValid() && link != null) {
            ServerLevel targetLevel = link.resolveLevel(serverLevel);
            if (targetLevel != null && targetLevel.hasChunkAt(link.pos())
                    && targetLevel.getBlockEntity(link.pos()) instanceof MirrorBlockEntity target) {
                target.addStack(stack.copy());
                addInstability(null, stack.getCount());
                return true;
            }
        }
        return spawnOutFront(stack.copy());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> insertionHandler);
    }

    @Override public boolean stillValid(Player player) { return false; }
    @Override public void clearContent() { outputStacks.clear(); setChangedAndSync(); }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) { return !stack.isEmpty(); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
}
