package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Forge runtime port of TC4 EntityTravelingTrunk.
 *
 * <p>It keeps a 27-slot inventory, follows its owner, can be ordered to stay,
 * heals from food, animates its lid, attracts items for the collector upgrade,
 * serializes its inventory/upgrade and drops both contents and its spawner.</p>
 */
public final class TravelingTrunkEntity extends TamableAnimal implements Container, MenuProvider {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TravelingTrunkEntity.class, EntityDataSerializers.INT);
    private static final int SIZE = 27;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    private int viewers;
    public float lidOld;
    public float lid;

    public TravelingTrunkEntity(EntityType<? extends TravelingTrunkEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 75.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OPEN, false);
        entityData.define(UPGRADE, -1);
        entityData.define(ANGER, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.15D, 5.0F, 2.0F, false));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        lidOld = lid;
        float target = isOpen() ? 0.5F : (isOnGround() || isInWater() ? 0.0F : 0.2F);
        float speed = isOpen() ? 0.035F : 0.10F;
        lid += Math.signum(target - lid) * Math.min(Math.abs(target - lid), speed);

        if (!level.isClientSide) {
            int anger = getAnger();
            if (anger > 0) setAnger(anger - 1);
            if (getHealth() < getMaxHealth() && (getUpgrade() == 3 || tickCount % 50 == 0)) {
                heal(1.0F);
            }
            if (getUpgrade() == 5) {
                pullItems();
            }
        }
    }

    public float getLid(float partialTick) {
        return lidOld + (lid - lidOld) * partialTick;
    }

    public boolean isOpen() {
        return entityData.get(OPEN);
    }

    private void setOpen(boolean open) {
        if (entityData.get(OPEN) != open) {
            entityData.set(OPEN, open);
            level.playSound(null, blockPosition(), open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                    SoundSource.NEUTRAL, 0.35F, 0.95F + random.nextFloat() * 0.1F);
        }
    }

    public int getUpgrade() {
        return entityData.get(UPGRADE);
    }

    public void setUpgrade(int upgrade) {
        entityData.set(UPGRADE, Math.max(-1, Math.min(5, upgrade)));
    }

    public int getAnger() {
        return entityData.get(ANGER);
    }

    private void setAnger(int ticks) {
        entityData.set(ANGER, Math.max(0, ticks));
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source == net.minecraft.world.damagesource.DamageSource.IN_WALL || getUpgrade() == 3) {
            return false;
        }
        setAnger(80);
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEdible() && getHealth() < getMaxHealth()) {
            if (!level.isClientSide) {
                heal(Math.max(1.0F, held.getFoodProperties(player).getNutrition()));
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.55F, 0.9F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!isTame()) {
            if (!level.isClientSide) {
                tame(player);
                setOrderedToSit(false);
                level.broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isOwnedBy(player) && player.isShiftKeyDown()) {
            if (!level.isClientSide) setOrderedToSit(!isOrderedToSit());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void pullItems() {
        AABB pullBox = getBoundingBox().inflate(3.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, pullBox, ItemEntity::isAlive)) {
            double dx = getX() - item.getX();
            double dy = getY() + getBbHeight() * 0.8D - item.getY();
            double dz = getZ() - item.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0.001D) {
                item.setDeltaMovement(item.getDeltaMovement().add(dx / distance * 0.075D, dy / distance * 0.075D, dz / distance * 0.075D));
            }
            if (distance < 1.0D) {
                ItemStack remainder = addItem(item.getItem().copy());
                if (remainder.getCount() != item.getItem().getCount()) {
                    item.setItem(remainder);
                    if (remainder.isEmpty()) item.discard();
                    setOpen(true);
                }
            }
        }
    }

    private ItemStack addItem(ItemStack input) {
        ItemStack remainder = input.copy();
        for (int i = 0; i < items.size() && !remainder.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                items.set(i, remainder.copy());
                remainder = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameTags(slot, remainder) && slot.getCount() < slot.getMaxStackSize()) {
                int move = Math.min(remainder.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(move);
                remainder.shrink(move);
            }
        }
        setChanged();
        return remainder;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : Component.translatable("item.thaumcraft.tc4_travel_trunk");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return ChestMenu.threeRows(containerId, playerInventory, this);
    }

    @Override public int getContainerSize() { return SIZE; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
    @Override public void setChanged() { }
    @Override public boolean stillValid(Player player) { return isAlive() && distanceToSqr(player) <= 64.0D; }
    @Override public void clearContent() {
        for (int slot = 0; slot < items.size(); slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }
    @Override public void startOpen(Player player) { if (!player.isSpectator()) { viewers++; setOpen(true); } }
    @Override public void stopOpen(Player player) { if (!player.isSpectator()) { viewers = Math.max(0, viewers - 1); setOpen(viewers > 0); } }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Upgrade", getUpgrade());
        tag.putBoolean("Open", isOpen());
        tag.putInt("Anger", getAnger());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ContainerHelper.loadAllItems(tag, items);
        setUpgrade(tag.getInt("Upgrade"));
        entityData.set(OPEN, tag.getBoolean("Open"));
        setAnger(tag.getInt("Anger"));
    }

    @Override
    protected void dropAllDeathLoot(net.minecraft.world.damagesource.DamageSource source) {
        super.dropAllDeathLoot(source);
        if (!level.isClientSide) {
            Containers.dropContents(level, this, this);
            ItemStack spawner = new ItemStack(ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_travel_trunk").get());
            if (getUpgrade() >= 0) spawner.getOrCreateTag().putInt("Upgrade", getUpgrade());
            if (hasCustomName()) spawner.setHoverName(getCustomName());
            spawnAtLocation(spawner);
        }
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
