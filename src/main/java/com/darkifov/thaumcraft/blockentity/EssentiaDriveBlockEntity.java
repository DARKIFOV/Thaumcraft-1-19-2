package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaCellItem;
import com.darkifov.thaumcraft.block.EssentiaUpgradeCardItem;
import com.darkifov.thaumcraft.menu.EssentiaDriveMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

public class EssentiaDriveBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CELL_SLOTS = 10;
    public static final int UPGRADE_START = 10;
    public static final int UPGRADE_END = 13;
    public static final int SIZE = 14;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public EssentiaDriveBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ESSENTIA_DRIVE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Essentia Drive");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EssentiaDriveMenu(containerId, inventory, this);
    }

    public Map<Aspect, Integer> totals() {
        Map<Aspect, Integer> totals = new EnumMap<>(Aspect.class);

        for (int i = 0; i < CELL_SLOTS; i++) {
            ItemStack stack = items.get(i);

            if (stack.getItem() instanceof EssentiaCellItem) {
                Aspect aspect = EssentiaCellItem.getAspect(stack);
                int amount = EssentiaCellItem.getAmount(stack);

                if (aspect != null && amount > 0) {
                    totals.merge(aspect, amount, Integer::sum);
                }
            }
        }

        return totals;
    }

    public int amountOf(Aspect aspect) {
        return totals().getOrDefault(aspect, 0);
    }

    public int insertEssentia(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }

        int moved = 0;

        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < CELL_SLOTS && moved < amount; i++) {
                ItemStack stack = items.get(i);

                if (!(stack.getItem() instanceof EssentiaCellItem cellItem)) {
                    continue;
                }

                Aspect partition = EssentiaCellItem.getPartitionAspect(stack);
                if (partition != null && partition != aspect) {
                    continue;
                }

                Aspect storedAspect = EssentiaCellItem.getAspect(stack);
                int current = EssentiaCellItem.getAmount(stack);

                if (pass == 0 && storedAspect != aspect) {
                    continue;
                }

                if (pass == 1 && storedAspect != null && storedAspect != aspect) {
                    continue;
                }

                int space = Math.max(0, cellItem.capacity() - current);
                int add = Math.min(space, amount - moved);

                if (add > 0) {
                    EssentiaCellItem.setEssentia(stack, aspect, current + add);
                    moved += add;
                }
            }
        }

        if (moved > 0) {
            setChangedAndSync();
        }

        return moved;
    }

    public int extractEssentia(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return 0;
        }

        int moved = 0;

        for (int i = 0; i < CELL_SLOTS && moved < amount; i++) {
            ItemStack stack = items.get(i);

            if (!(stack.getItem() instanceof EssentiaCellItem)) {
                continue;
            }

            Aspect storedAspect = EssentiaCellItem.getAspect(stack);
            int current = EssentiaCellItem.getAmount(stack);

            if (storedAspect != aspect || current <= 0) {
                continue;
            }

            int take = Math.min(current, amount - moved);
            EssentiaCellItem.setEssentia(stack, aspect, current - take);
            moved += take;
        }

        if (moved > 0) {
            setChangedAndSync();
        }

        return moved;
    }

    public MovedEssentia extractAnyEssentia(int amount) {
        if (amount <= 0) {
            return MovedEssentia.empty();
        }

        for (Aspect aspect : Aspect.values()) {
            int available = amountOf(aspect);

            if (available > 0) {
                int moved = extractEssentia(aspect, Math.min(amount, available));
                return new MovedEssentia(aspect, moved);
            }
        }

        return MovedEssentia.empty();
    }

    public int cellCount() {
        int count = 0;

        for (int i = 0; i < CELL_SLOTS; i++) {
            if (items.get(i).getItem() instanceof EssentiaCellItem) {
                count++;
            }
        }

        return count;
    }

    public int totalCapacity() {
        int capacity = 0;

        for (int i = 0; i < CELL_SLOTS; i++) {
            ItemStack stack = items.get(i);

            if (stack.getItem() instanceof EssentiaCellItem cellItem) {
                capacity += cellItem.capacity();
            }
        }

        return capacity;
    }

    public int storedAmount() {
        int amount = 0;

        for (int i = 0; i < CELL_SLOTS; i++) {
            ItemStack stack = items.get(i);

            if (stack.getItem() instanceof EssentiaCellItem) {
                amount += EssentiaCellItem.getAmount(stack);
            }
        }

        return amount;
    }

    public int transferLimit() {
        int limit = 64;

        for (int i = UPGRADE_START; i <= UPGRADE_END; i++) {
            ItemStack stack = items.get(i);

            if (stack.getItem() instanceof EssentiaUpgradeCardItem card) {
                limit += card.transferBonus() * stack.getCount();
            }
        }

        return Math.min(2048, limit);
    }

    public int scanBonus() {
        int bonus = 1;

        for (int i = UPGRADE_START; i <= UPGRADE_END; i++) {
            ItemStack stack = items.get(i);

            if (stack.getItem() instanceof EssentiaUpgradeCardItem card) {
                bonus += card.scanBonus() * stack.getCount();
            }
        }

        return Math.min(32, bonus);
    }

    public void sendStatus(Player player) {
        player.displayClientMessage(Component.literal("Essentia Drive | cells: " + cellCount() + "/" + CELL_SLOTS + " | stored: " + storedAmount() + "/" + totalCapacity()).withStyle(ChatFormatting.AQUA), false);
        player.displayClientMessage(Component.literal("Transfer limit: " + transferLimit() + " | scan bonus: x" + scanBonus()).withStyle(ChatFormatting.LIGHT_PURPLE), false);

        Map<Aspect, Integer> totals = totals();

        if (totals.isEmpty()) {
            player.displayClientMessage(Component.literal("Drive пустой.").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        for (Map.Entry<Aspect, Integer> entry : totals.entrySet()) {
            player.displayClientMessage(Component.literal(entry.getKey().displayName() + ": " + entry.getValue()).withStyle(entry.getKey().color()), false);
        }
    }

    public boolean isCellSlot(int slot) {
        return slot >= 0 && slot < CELL_SLOTS;
    }

    public boolean isUpgradeSlot(int slot) {
        return slot >= UPGRADE_START && slot <= UPGRADE_END;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);

        if (!result.isEmpty()) {
            setChangedAndSync();
        }

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (isCellSlot(slot)) {
            return stack.getItem() instanceof EssentiaCellItem;
        }

        if (isUpgradeSlot(slot)) {
            return stack.getItem() instanceof EssentiaUpgradeCardItem;
        }

        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        ContainerHelper.loadAllItems(tag, items);
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static class MovedEssentia {
        public final Aspect aspect;
        public final int amount;

        public MovedEssentia(Aspect aspect, int amount) {
            this.aspect = aspect;
            this.amount = amount;
        }

        public static MovedEssentia empty() {
            return new MovedEssentia(null, 0);
        }
    }
}
