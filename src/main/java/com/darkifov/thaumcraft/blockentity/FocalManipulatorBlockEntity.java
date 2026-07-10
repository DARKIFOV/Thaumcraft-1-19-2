package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraVisRelayNetwork;
import com.darkifov.thaumcraft.block.WandFocusItem;
import com.darkifov.thaumcraft.menu.FocalManipulatorMenu;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.wand.WandFocusType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.DustParticleOptions;
import com.mojang.math.Vector3f;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TC4 TileFocalManipulator lifecycle port.
 *
 * Original invariants retained:
 * - one focus slot and five upgrade ranks;
 * - XP cost rank * 8;
 * - upgrade aspect base 200 cv, doubled per rank;
 * - VisNet drain capped to 100 cv per aspect every five ticks;
 * - removing/replacing the focus aborts without refunding XP or vis;
 * - successful completion writes one short id into the original "upgrade" NBT list.
 */
public class FocalManipulatorBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_FOCUS = 0;
    public static final int SIZE = 1;
    public static final int XP_MULT = 8;
    public static final int VIS_MULT = 200;
    public static final int DRAIN_INTERVAL = 5;
    public static final int MAX_DRAIN_PER_ASPECT = 100;
    public static final Aspect[] PRIMALS = {
            Aspect.AER, Aspect.TERRA, Aspect.IGNIS, Aspect.AQUA, Aspect.ORDO, Aspect.PERDITIO
    };

    public static final int DATA_INITIAL_SIZE = 0;
    public static final int DATA_REMAINING_SIZE = 1;
    public static final int DATA_UPGRADE = 2;
    public static final int DATA_RANK = 3;
    public static final int DATA_ASPECT_START = 4;
    public static final int DATA_COUNT = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final AspectList remaining = new AspectList();
    private int initialSize;
    private int selectedUpgrade = -1;
    private int rank = -1;
    private int ticks;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_INITIAL_SIZE -> initialSize;
                case DATA_REMAINING_SIZE -> remaining.totalAmount();
                case DATA_UPGRADE -> selectedUpgrade;
                case DATA_RANK -> rank;
                default -> index >= DATA_ASPECT_START && index < DATA_COUNT
                        ? remaining.get(PRIMALS[index - DATA_ASPECT_START]) : 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_INITIAL_SIZE -> initialSize = value;
                case DATA_UPGRADE -> selectedUpgrade = value;
                case DATA_RANK -> rank = value;
                default -> {
                    if (index >= DATA_ASPECT_START && index < DATA_COUNT) {
                        Aspect aspect = PRIMALS[index - DATA_ASPECT_START];
                        int old = remaining.get(aspect);
                        if (old > 0) remaining.remove(aspect, old);
                        if (value > 0) remaining.add(aspect, value);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.FOCAL_MANIPULATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public int initialSize() {
        return initialSize;
    }

    public int selectedUpgrade() {
        return selectedUpgrade;
    }

    public int activeRank() {
        return rank;
    }

    public boolean isCrafting() {
        return initialSize > 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.thaumcraft.focal_manipulator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FocalManipulatorMenu(containerId, inventory, this, dataAccess);
    }

    public boolean startCraft(int upgradeId, Player player) {
        if (level == null || level.isClientSide || isCrafting()) return false;
        ItemStack focusStack = getItem(SLOT_FOCUS);
        if (!(focusStack.getItem() instanceof WandFocusItem focusItem)) return false;

        int nextRank = FocusUpgradeRuntime.nextOpenRank(focusStack);
        if (nextRank < 1 || nextRank > FocusUpgradeRuntime.MAX_RANK) return false;
        int xp = nextRank * XP_MULT;
        // Original TC4 checks the level requirement even in creative, then skips only the deduction.
        if (player.experienceLevel < xp) return false;

        FocusUpgradeType selected = FocusUpgradeType.byId((short) upgradeId);
        WandFocusType focusType = focusItem.focusType();
        if (selected == null
                || !FocusUpgradeRuntime.isPossible(focusType, selected, nextRank)
                || !FocusUpgradeRuntime.canApplyUpgrade(focusStack, focusType, selected, nextRank, player)) {
            return false;
        }

        AspectList cost = FocusUpgradeRuntime.primalVisCost(selected, nextRank);
        if (cost.isEmpty()) return false;

        remaining.clear();
        remaining.addAll(cost);
        initialSize = remaining.totalAmount();
        selectedUpgrade = selected.id();
        rank = nextRank;
        if (!player.getAbilities().instabuild) player.giveExperienceLevels(-xp);

        level.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.25F, 1.0F);
        setChangedAndSync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FocalManipulatorBlockEntity tile) {
        tile.ticks++;
        if (tile.ticks % DRAIN_INTERVAL != 0 || !tile.isCrafting() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack focus = tile.getItem(SLOT_FOCUS);
        if (!tile.isActiveCraftStillValid(focus)) {
            tile.finish(false);
            return;
        }

        for (Aspect aspect : PRIMALS) {
            int outstanding = tile.remaining.get(aspect);
            if (outstanding <= 0) continue;
            int requestCentivis = Math.min(MAX_DRAIN_PER_ASPECT, outstanding);
            int drainedCentivis = AuraVisRelayNetwork.drainMachineVis(
                    serverLevel, tile.worldPosition, aspect, requestCentivis);
            if (drainedCentivis > 0) {
                tile.remaining.removeUpTo(aspect, Math.min(outstanding, drainedCentivis));
            }
        }

        // Original TileFocalManipulator emits restrained pink-white motes while a craft is active.
        serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.85F, 0.62F, 0.92F), 0.55F),
                tile.worldPosition.getX() + 0.5D,
                tile.worldPosition.getY() + 1.25D,
                tile.worldPosition.getZ() + 0.5D,
                1, 0.20D, 0.18D, 0.20D, 0.01D);

        if (tile.remaining.totalAmount() <= 0) {
            FocusUpgradeType type = FocusUpgradeType.byId((short) tile.selectedUpgrade);
            boolean applied = FocusUpgradeRuntime.applyUpgrade(focus, type, tile.rank);
            tile.finish(applied);
        } else {
            tile.setChangedAndSync();
        }
    }

    private boolean isActiveCraftStillValid(ItemStack focusStack) {
        if (!(focusStack.getItem() instanceof WandFocusItem focusItem)) return false;
        if (rank < 1 || rank > FocusUpgradeRuntime.MAX_RANK) return false;
        FocusUpgradeType selected = FocusUpgradeType.byId((short) selectedUpgrade);
        if (selected == null) return false;
        if (FocusUpgradeRuntime.nextOpenRank(focusStack) != rank) return false;
        WandFocusType focusType = focusItem.focusType();
        return FocusUpgradeRuntime.isPossible(focusType, selected, rank)
                && FocusUpgradeRuntime.canApplyUpgrade(focusStack, focusType, selected, rank);
    }

    private void finish(boolean success) {
        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event(success ? "wand" : "craftfail"),
                    SoundSource.BLOCKS, success ? 1.0F : 0.33F, 1.0F);
        }
        remaining.clear();
        initialSize = 0;
        selectedUpgrade = -1;
        rank = -1;
        setChangedAndSync();
    }

    private void cancelCraft() {
        if (isCrafting()) finish(false);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.get(SLOT_FOCUS).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) {
            cancelCraft();
            setChangedAndSync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) cancelCraft();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack old = items.get(slot);
        if (isCrafting() && !ItemStack.isSameItemSameTags(old, stack)) cancelCraft();
        items.set(slot, stack);
        if (stack.getCount() > 1) stack.setCount(1);
        setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_FOCUS && stack.getItem() instanceof WandFocusItem && !isCrafting();
    }

    @Override
    public void clearContent() {
        items.clear();
        cancelCraft();
        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.put("Aspects", remaining.save());
        tag.putInt("size", initialSize);
        tag.putInt("upgrade", selectedUpgrade);
        tag.putInt("rank", rank);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        remaining.clear();
        remaining.load(tag.getCompound("Aspects"));
        initialSize = tag.getInt("size");
        selectedUpgrade = tag.contains("upgrade") ? tag.getInt("upgrade") : -1;
        rank = tag.contains("rank") ? tag.getInt("rank") : -1;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }
}
