package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.ArcaneSpaMenu;
import com.darkifov.thaumcraft.block.TC4ArcaneSpaParity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TC4 TileSpa port: 5000 mB tank, one Bath Salts slot, 40-tick output cadence,
 * redstone disable and source expansion over the 5x5 layer above the block.
 *
 * <p>v11.64.23 closes the original filled-container interaction, root-level
 * TC4 NBT layout, migration from the earlier port keys, side automation and
 * exact source-expansion contract. The menu synchronises mode, tank amount
 * and fluid registry id while the salt stack uses the normal slot protocol.</p>
 */
public class ArcaneSpaBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = TC4ArcaneSpaParity.CAPACITY_MB;
    public static final int CHECK_INTERVAL = TC4ArcaneSpaParity.CHECK_INTERVAL_TICKS;
    private static final int BUCKET = TC4ArcaneSpaParity.BUCKET_MB;

    private boolean mixing = true;
    /** TC4 used an independent, non-persistent cadence for every spa. */
    private int counter;
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            markAndSync();
        }
    };
    private final ItemStackHandler salts = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.is(ThaumcraftMod.BATH_SALTS.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            markAndSync();
        }
    };

    /** Values consumed by {@link ArcaneSpaMenu}: mix mode, amount, fluid id. */
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mixing ? 1 : 0;
                case 1 -> tank.getFluidAmount();
                case 2 -> Math.max(0, Registry.FLUID.getId(tank.getFluid().getFluid()));
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                mixing = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> salts);

    public ArcaneSpaBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ARCANE_SPA_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneSpaBlockEntity spa) {
        if (!TC4ArcaneSpaParity.shouldRunCycle(spa.counter++) || level.hasNeighborSignal(pos)) {
            return;
        }
        spa.tryDispense(level, pos);
    }

    private void tryDispense(Level level, BlockPos pos) {
        FluidStack stored = tank.getFluid();
        if (stored.getAmount() < BUCKET) {
            return;
        }

        Fluid targetFluid;
        if (mixing) {
            if (salts.getStackInSlot(0).isEmpty() || !isVanillaWater(stored.getFluid())) {
                return;
            }
            targetFluid = ThaumcraftMod.PURIFYING_FLUID.get();
        } else {
            targetFluid = stored.getFluid();
            BlockState candidate = targetFluid.defaultFluidState().createLegacyBlock();
            if (candidate.isAir()) {
                return;
            }
        }

        if (level.dimensionType().ultraWarm() && isVanillaWater(targetFluid)) {
            return;
        }

        BlockPos output = findOutput(level, pos.above(), targetFluid);
        if (output == null) {
            return;
        }

        BlockState outputState = targetFluid.defaultFluidState().createLegacyBlock();

        // TileSpa consumed first and ignored the boolean result of setBlock.
        // This intentionally preserves the rare event-cancel/failed-placement loss contract.
        tank.drain(BUCKET, IFluidHandler.FluidAction.EXECUTE);
        if (mixing) {
            salts.extractItem(0, 1, false);
        }
        markAndSync();
        level.setBlock(output, outputState, 3);
    }

    @Nullable
    private static BlockPos findOutput(Level level, BlockPos center, Fluid targetFluid) {
        if (isTargetSource(level.getFluidState(center), targetFluid)) {
            for (int x = -TC4ArcaneSpaParity.OUTPUT_RADIUS; x <= TC4ArcaneSpaParity.OUTPUT_RADIUS; x++) {
                for (int z = -TC4ArcaneSpaParity.OUTPUT_RADIUS; z <= TC4ArcaneSpaParity.OUTPUT_RADIUS; z++) {
                    BlockPos candidate = center.offset(x, 0, z);
                    if (canPlaceAt(level, candidate)
                            && !isTargetSource(level.getFluidState(candidate), targetFluid)
                            && touchesTargetSource(level, candidate, targetFluid)) {
                        return candidate;
                    }
                }
            }
            return null;
        }
        return canPlaceAt(level, center) ? center : null;
    }

    private static boolean canPlaceAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockPos below = pos.below();
        boolean replaceable = state.isAir() || state.getMaterial().isReplaceable();
        boolean support = level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        return TC4ArcaneSpaParity.canPlaceOutput(support, replaceable, false);
    }

    private static boolean touchesTargetSource(Level level, BlockPos pos, Fluid targetFluid) {
        for (Direction direction : Direction.values()) {
            if (isTargetSource(level.getFluidState(pos.relative(direction)), targetFluid)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTargetSource(FluidState state, Fluid targetFluid) {
        return state.isSource() && state.getType().isSame(targetFluid);
    }

    private static boolean isVanillaWater(Fluid fluid) {
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    public boolean toggleMixing() {
        mixing = !mixing;
        markAndSync();
        return mixing;
    }

    public boolean isMixing() {
        return mixing;
    }

    public IItemHandler saltsHandler() {
        return salts;
    }

    public ContainerData menuData() {
        return menuData;
    }

    public boolean insertBathSalts(ItemStack held, boolean creative) {
        if (held.isEmpty() || !held.is(ThaumcraftMod.BATH_SALTS.get())) {
            return false;
        }
        ItemStack one = held.copy();
        one.setCount(1);
        ItemStack remainder = salts.insertItem(0, one, false);
        if (!remainder.isEmpty()) {
            return false;
        }
        if (!creative) {
            held.shrink(1);
        }
        return true;
    }

    public ItemStack removeBathSalts() {
        return salts.extractItem(0, 1, false);
    }

    public ItemStack removeAllBathSalts() {
        return salts.extractItem(0, salts.getSlotLimit(0), false);
    }

    public boolean canAcceptFilledContainer(FluidStack resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }
        FluidStack stored = tank.getFluid();
        boolean emptyOrSame = stored.isEmpty() || stored.isFluidEqual(resource);
        return TC4ArcaneSpaParity.canAcceptFilledContainer(tank.getFluidAmount(), emptyOrSame);
    }

    /**
     * TC4's direct block-use path filled as much as possible, but consumed the
     * complete filled container even on a partial final fill. Container
     * consumption itself is handled by ArcaneSpaBlock after this succeeds.
     */
    public int fillFromHeldContainer(FluidStack resource) {
        if (!canAcceptFilledContainer(resource)) {
            return 0;
        }
        int expected = TC4ArcaneSpaParity.acceptedFluidAmount(tank.getFluidAmount(), resource.getAmount());
        int filled = tank.fill(resource, IFluidHandler.FluidAction.EXECUTE);
        return Math.min(expected, filled);
    }

    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    public FluidStack getFluidCopy() {
        return tank.getFluid().copy();
    }

    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= TC4ArcaneSpaParity.BLOCK_USE_DISTANCE_SQUARED;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumcraft.arcane_spa");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArcaneSpaMenu(id, inventory, this, menuData);
    }

    private void markAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // Exact TC4 TileSpa keys: lowercase mix, FluidStack at root and Items list at root.
        tag.putBoolean(TC4ArcaneSpaParity.NBT_MIX, mixing);
        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty()) {
            fluid.writeToNBT(tag);
        }

        ListTag items = new ListTag();
        ItemStack saltStack = salts.getStackInSlot(0);
        if (!saltStack.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) 0);
            saltStack.save(itemTag);
            items.add(itemTag);
        }
        tag.put(TC4ArcaneSpaParity.NBT_ITEMS, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(TC4ArcaneSpaParity.NBT_MIX, Tag.TAG_BYTE)) {
            mixing = tag.getBoolean(TC4ArcaneSpaParity.NBT_MIX);
        } else if (tag.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_MIX, Tag.TAG_BYTE)) {
            // Migration from v11.63.24-v11.64.22 saves.
            mixing = tag.getBoolean(TC4ArcaneSpaParity.LEGACY_PORT_NBT_MIX);
        } else {
            // NBTTagCompound#getBoolean returned false for a missing TC4 key.
            mixing = false;
        }

        FluidStack loadedFluid = FluidStack.loadFluidStackFromNBT(tag);
        if (!loadedFluid.isEmpty()) {
            tank.setFluid(loadedFluid);
        } else if (tag.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_TANK, Tag.TAG_COMPOUND)) {
            tank.readFromNBT(tag.getCompound(TC4ArcaneSpaParity.LEGACY_PORT_NBT_TANK));
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }

        ItemStack loadedSalt = ItemStack.EMPTY;
        if (tag.contains(TC4ArcaneSpaParity.NBT_ITEMS, Tag.TAG_LIST)) {
            ListTag items = tag.getList(TC4ArcaneSpaParity.NBT_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot == 0) {
                    loadedSalt = ItemStack.of(itemTag);
                    break;
                }
            }
        } else if (tag.contains(TC4ArcaneSpaParity.LEGACY_PORT_NBT_SALTS, Tag.TAG_COMPOUND)) {
            CompoundTag legacy = tag.getCompound(TC4ArcaneSpaParity.LEGACY_PORT_NBT_SALTS);
            ListTag items = legacy.getList("Items", Tag.TAG_COMPOUND);
            if (!items.isEmpty()) {
                loadedSalt = ItemStack.of(items.getCompound(0));
            }
        }
        salts.setStackInSlot(0, loadedSalt);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        int sideOrdinal = side == null ? -1 : side.get3DDataValue();
        if (TC4ArcaneSpaParity.exposesAutomationSide(sideOrdinal)
                && capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        if (TC4ArcaneSpaParity.exposesAutomationSide(sideOrdinal)
                && capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidCapability = LazyOptional.of(() -> tank);
        itemCapability = LazyOptional.of(() -> salts);
    }
}
