package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.menu.ArcaneSpaMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
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
 * <p>v11.62.69 also exposes the original ContainerSpa/GuiSpa contract. The
 * menu synchronises the mode, tank amount and fluid registry id while the
 * actual salt stack is handled by the ordinary menu-slot protocol.</p>
 */
public class ArcaneSpaBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 5000;
    public static final int CHECK_INTERVAL = 40;
    private static final int BUCKET = 1000;

    private boolean mixing = true;
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
        if (level.hasNeighborSignal(pos) || level.getGameTime() % CHECK_INTERVAL != 0L) {
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
        if (!level.setBlock(output, outputState, 3)) {
            return;
        }

        tank.drain(BUCKET, IFluidHandler.FluidAction.EXECUTE);
        if (mixing) {
            salts.extractItem(0, 1, false);
        }
        markAndSync();
    }

    @Nullable
    private static BlockPos findOutput(Level level, BlockPos center, Fluid targetFluid) {
        if (isTargetSource(level.getFluidState(center), targetFluid)) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
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
        return (state.isAir() || state.getMaterial().isReplaceable())
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean touchesTargetSource(Level level, BlockPos pos, Fluid targetFluid) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
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
                worldPosition.getZ() + 0.5D) <= 64.0D;
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
        tag.putBoolean("Mix", mixing);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Salts", salts.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mixing = !tag.contains("Mix") || tag.getBoolean("Mix");
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        if (tag.contains("Salts")) {
            salts.deserializeNBT(tag.getCompound("Salts"));
        }
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
        if (side != Direction.UP && capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        if (side != Direction.UP && capability == ForgeCapabilities.ITEM_HANDLER) {
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
