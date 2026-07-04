package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ThaumGolemEntity extends PathfinderMob {
    private UUID ownerUuid;
    private BlockPos homePos = BlockPos.ZERO;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);

    public ThaumGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.85D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setHomePos(BlockPos homePos) {
        this.homePos = homePos == null ? BlockPos.ZERO : homePos.immutable();
    }

    public BlockPos getHomePos() {
        return homePos;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level.isClientSide && tickCount % 10 == 0) {
            deliverInventoryToOwner();
            pickupNearbyItems();
            followOwnerOrHome();
        }
    }

    private void pickupNearbyItems() {
        int radius = hasNearbyCollectSeal() ? 12 : 5;

        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                getBoundingBox().inflate(radius),
                item -> item.isAlive() && !item.getItem().isEmpty()
        );

        if (items.isEmpty()) {
            return;
        }

        items.sort(Comparator.comparingDouble(this::distanceToSqr));
        ItemEntity target = items.get(0);

        if (distanceToSqr(target) > 2.25D) {
            getNavigation().moveTo(target, 1.1D);
            return;
        }

        ItemStack remaining = addToInventory(target.getItem().copy());

        if (remaining.isEmpty()) {
            target.discard();
        } else {
            target.setItem(remaining);
        }
    }

    private boolean hasNearbyCollectSeal() {
        BlockPos center = blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -3, -8), center.offset(8, 3, 8))) {
            if (level.getBlockState(pos).is(ThaumcraftMod.GOLEM_SEAL_COLLECT_BLOCK.get())) {
                return true;
            }
        }

        return false;
    }

    private ItemStack addToInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stored = inventory.get(i);

            if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, stack)) {
                int space = stored.getMaxStackSize() - stored.getCount();

                if (space > 0) {
                    int move = Math.min(space, stack.getCount());
                    stored.grow(move);
                    stack.shrink(move);

                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                ItemStack copy = stack.copy();
                int move = Math.min(copy.getMaxStackSize(), stack.getCount());
                copy.setCount(move);
                inventory.set(i, copy);
                stack.shrink(move);

                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return stack;
    }

    private void deliverInventoryToOwner() {
        if (!(level instanceof ServerLevel serverLevel) || ownerUuid == null) {
            return;
        }

        Player owner = serverLevel.getPlayerByUUID(ownerUuid);

        if (owner == null || distanceToSqr(owner) > 64.0D) {
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);

            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();

                if (owner.getInventory().add(copy)) {
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private void followOwnerOrHome() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Player owner = ownerUuid == null ? null : serverLevel.getPlayerByUUID(ownerUuid);

        if (owner != null && distanceToSqr(owner) > 144.0D) {
            getNavigation().moveTo(owner, 1.0D);
            return;
        }

        if (homePos != null && !homePos.equals(BlockPos.ZERO) && distanceToSqr(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D) > 100.0D) {
            getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 0.8D);
        }
    }

    @Override
    protected boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }

        tag.putInt("HomeX", homePos.getX());
        tag.putInt("HomeY", homePos.getY());
        tag.putInt("HomeZ", homePos.getZ());

        ListTag list = new ListTag();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);

            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }

        tag.put("Inventory", list);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
        }

        homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));

        inventory.clear();

        ListTag list = tag.getList("Inventory", 10);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;

            if (slot >= 0 && slot < inventory.size()) {
                inventory.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public Component getName() {
        return Component.literal("Thaumic Golem");
    }
}
