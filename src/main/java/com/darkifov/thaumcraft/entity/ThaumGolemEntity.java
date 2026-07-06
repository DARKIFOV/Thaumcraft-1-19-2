package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.GolemBellItem;
import com.darkifov.thaumcraft.block.GolemCoreItem;
import com.darkifov.thaumcraft.block.EssentiaPhialItem;
import com.darkifov.thaumcraft.block.GolemDecorationItem;
import com.darkifov.thaumcraft.block.GolemFilterItem;
import com.darkifov.thaumcraft.block.GolemTaskMarkerItem;
import com.darkifov.thaumcraft.block.GolemUpgradeItem;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ThaumGolemEntity extends PathfinderMob {
    private UUID ownerUuid;
    private BlockPos homePos = BlockPos.ZERO;
    private BlockPos inputPos = null;
    private BlockPos outputPos = null;
    private BlockPos guardPos = null;
    private BlockPos workPos = null;
    private GolemMaterial material = GolemMaterial.WOOD;
    private GolemCoreType coreType = GolemCoreType.GATHER;
    private final Set<GolemUpgradeType> upgrades = EnumSet.noneOf(GolemUpgradeType.class);
    private final Set<GolemDecorationType> decorations = EnumSet.noneOf(GolemDecorationType.class);
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(36, ItemStack.EMPTY);
    private ItemStack filterStack = ItemStack.EMPTY;
    private boolean filterAllowList = false;
    private boolean waiting = false;
    private boolean patrolToWork = true;
    private int taskRadius = 8;
    private int taskPriority = 0;

    public ThaumGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
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

    public void setTaskMarker(GolemMarkerMode mode, BlockPos pos) {
        setTaskMarker(mode, pos, taskRadius, taskPriority);
    }

    public void setTaskMarker(GolemMarkerMode mode, BlockPos pos, int radius, int priority) {
        if (mode == null || pos == null) {
            return;
        }
        switch (mode) {
            case HOME -> setHomePos(pos);
            case INPUT -> inputPos = pos.immutable();
            case OUTPUT -> outputPos = pos.immutable();
            case GUARD -> guardPos = pos.immutable();
            case WORK -> workPos = pos.immutable();
        }
        this.taskRadius = Math.max(1, Math.min(32, radius));
        this.taskPriority = Math.max(0, Math.min(9, priority));
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        getNavigation().stop();
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setGolemProfile(GolemMaterial material, GolemCoreType coreType) {
        this.material = material == null ? GolemMaterial.WOOD : material;
        this.coreType = coreType == null ? GolemCoreType.GATHER : coreType;
        applyProfileAttributes();
    }

    public GolemMaterial getGolemMaterial() {
        return material;
    }

    public GolemCoreType getCoreType() {
        return coreType;
    }

    public void setCoreType(GolemCoreType coreType) {
        this.coreType = coreType == null ? GolemCoreType.GATHER : coreType;
    }

    public void addUpgrade(GolemUpgradeType upgrade) {
        if (upgrade != null) {
            upgrades.add(upgrade);
            applyProfileAttributes();
        }
    }

    public boolean hasUpgrade(GolemUpgradeType upgrade) {
        return upgrades.contains(upgrade);
    }

    public void addDecoration(GolemDecorationType decoration) {
        if (decoration != null) {
            decorations.add(decoration);
            applyProfileAttributes();
        }
    }

    public boolean hasDecoration(GolemDecorationType decoration) {
        return decorations.contains(decoration);
    }

    public int decorationCount() {
        return decorations.size();
    }

    public void setFilter(ItemStack stack, boolean allowList) {
        this.filterStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!this.filterStack.isEmpty()) {
            this.filterStack.setCount(1);
        }
        this.filterAllowList = allowList;
    }

    public void loadGolemConfiguration(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        for (String part : tag.getString(GolemCoreItem.TAG_UPGRADES).split(",")) {
            if (!part.isBlank()) {
                upgrades.add(GolemUpgradeType.byName(part));
            }
        }
        for (String part : tag.getString(GolemCoreItem.TAG_DECORATIONS).split(",")) {
            if (!part.isBlank()) {
                decorations.add(GolemDecorationType.byName(part));
            }
        }
        if (tag.contains(GolemCoreItem.TAG_FILTER)) {
            setFilter(ItemStack.of(tag.getCompound(GolemCoreItem.TAG_FILTER)), tag.getBoolean(GolemCoreItem.TAG_FILTER_ALLOW));
        }
        readMarker(tag, GolemMarkerMode.HOME);
        readMarker(tag, GolemMarkerMode.INPUT);
        readMarker(tag, GolemMarkerMode.OUTPUT);
        readMarker(tag, GolemMarkerMode.GUARD);
        readMarker(tag, GolemMarkerMode.WORK);
        applyProfileAttributes();
    }

    private void readMarker(CompoundTag tag, GolemMarkerMode mode) {
        String prefix = GolemTaskMarkerItem.markerPrefix(mode);
        if (!tag.getBoolean(prefix + "Has")) {
            return;
        }
        int radius = tag.contains(prefix + "Radius") ? tag.getInt(prefix + "Radius") : taskRadius;
        int priority = tag.contains(prefix + "Priority") ? tag.getInt(prefix + "Priority") : taskPriority;
        setTaskMarker(mode, new BlockPos(tag.getInt(prefix + "X"), tag.getInt(prefix + "Y"), tag.getInt(prefix + "Z")), radius, priority);
    }

    private void applyProfileAttributes() {
        double health = material.maxHealth();
        double speed = material.movementSpeed();
        double armor = material.armor();
        double attack = material == GolemMaterial.STRAW ? 1.0D : 2.0D + material.armor() * 0.25D;

        if (hasUpgrade(GolemUpgradeType.EARTH)) {
            health += 8.0D;
            armor += 2.0D;
            speed -= 0.015D;
        }
        if (hasUpgrade(GolemUpgradeType.AIR)) {
            speed += 0.06D;
        }
        if (hasDecoration(GolemDecorationType.WIRELESS_BACKPACK)) {
            speed += 0.015D;
        }
        if (hasUpgrade(GolemUpgradeType.FIRE)) {
            attack += 2.0D;
        }
        if (hasDecoration(GolemDecorationType.MACE)) {
            attack += 1.5D;
        }
        if (hasDecoration(GolemDecorationType.ARMOR)) {
            armor += 1.5D;
        }
        if (hasUpgrade(GolemUpgradeType.ENTROPY)) {
            attack += 1.0D;
            armor -= 1.0D;
        }

        applyAttribute(Attributes.MAX_HEALTH, Math.max(6.0D, health));
        applyAttribute(Attributes.MOVEMENT_SPEED, Math.max(0.08D, speed));
        applyAttribute(Attributes.ARMOR, Math.max(0.0D, armor));
        applyAttribute(Attributes.ATTACK_DAMAGE, Math.max(1.0D, attack));

        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        } else if (getHealth() <= 1.0F) {
            setHealth(getMaxHealth());
        }
    }

    private void applyAttribute(Attribute attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (ownerUuid != null && !player.getUUID().equals(ownerUuid) && !player.getAbilities().instabuild) {
            player.displayClientMessage(Component.literal("This golem is bound to another thaumaturge.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemTaskMarkerItem) {
            BlockPos markerPos = GolemTaskMarkerItem.getPosition(held);
            GolemMarkerMode mode = GolemTaskMarkerItem.getMode(held);
            if (markerPos == null) {
                markerPos = blockPosition();
            }
            setTaskMarker(mode, markerPos, GolemTaskMarkerItem.getRadius(held), GolemTaskMarkerItem.getPriority(held));
            player.displayClientMessage(Component.literal("Assigned ").append(mode.displayName()).append(Component.literal(" marker to golem at " + markerPos.toShortString())), false);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemFilterItem) {
            ItemStack filter = GolemFilterItem.getFilterStack(held);
            setFilter(filter, GolemFilterItem.isAllowList(held));
            player.displayClientMessage(filter.isEmpty()
                    ? Component.literal("Cleared golem filter.").withStyle(ChatFormatting.GRAY)
                    : Component.literal("Applied golem filter: ").append(filter.getHoverName()).withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemUpgradeItem upgradeItem) {
            addUpgrade(upgradeItem.getUpgradeType());
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.literal("Installed golem upgrade: ").append(upgradeItem.getUpgradeType().displayName()), false);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemDecorationItem decorationItem) {
            addDecoration(decorationItem.getDecorationType());
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.literal("Installed golem decoration: ").append(decorationItem.getDecorationType().displayName()), false);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemCoreItem) {
            CompoundTag tag = held.getOrCreateTag();
            setGolemProfile(GolemMaterial.byName(tag.getString(GolemCoreItem.TAG_MATERIAL)), GolemCoreType.byName(tag.getString(GolemCoreItem.TAG_CORE)));
            loadGolemConfiguration(tag);
            player.displayClientMessage(Component.literal("Rebuilt golem profile: ").append(material.displayName()).append(Component.literal(" / ")).append(coreType.displayName()), false);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemBellItem || held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                setWaiting(!waiting);
            }
            player.displayClientMessage(statusSummary(), false);
            return InteractionResult.CONSUME;
        }

        return super.mobInteract(player, hand);
    }

    public Component statusSummary() {
        return Component.literal("Golem ")
                .append(material.displayName())
                .append(Component.literal(" / "))
                .append(coreType.displayName())
                .append(Component.literal(" | slots " + activeSlots()))
                .append(Component.literal(" | range " + workRange()))
                .append(Component.literal(" | priority " + taskPriority))
                .append(Component.literal(" | upgrades " + upgrades.size()))
                .append(Component.literal(" | deco " + decorations.size()))
                .append(Component.literal(waiting ? " | waiting" : " | active"));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level.isClientSide && tickCount % 10 == 0) {
            if (!waiting) {
                runCoreBehavior();
                followOwnerOrHome();
            }
            deliverInventory();
        }
    }

    private void runCoreBehavior() {
        switch (coreType) {
            case GATHER, SORTING -> pickupNearbyItems();
            case FILL -> fillOutputContainer();
            case EMPTY -> emptyInputContainer();
            case GUARD, BODYGUARD -> guardOwnerArea();
            case HARVEST -> harvestNearbyCrops();
            case LUMBER -> lumberNearbyLogs();
            case USE -> useWorkTarget();
            case BUTCHER -> butcherNearbyAnimals();
            case FISH -> fishAtWaterMarker();
            case LIQUID -> handleLiquidCore();
            case ESSENTIA -> handleEssentiaCore();
            case PATROL -> patrolBetweenMarkers();
        }
    }

    private int workRange() {
        int range = Math.max(5, taskRadius);
        if (hasNearbyCollectSeal()) {
            range = Math.max(range, 12);
        }
        if (hasUpgrade(GolemUpgradeType.AIR)) {
            range += 4;
        }
        if (hasUpgrade(GolemUpgradeType.ORDER)) {
            range += 2;
        }
        if (hasDecoration(GolemDecorationType.VISOR) || hasDecoration(GolemDecorationType.GLASSES)) {
            range += 1;
        }
        return range;
    }

    private void pickupNearbyItems() {
        int radius = workRange();

        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                getBoundingBox().inflate(radius),
                item -> item.isAlive() && !item.getItem().isEmpty() && acceptsItem(item.getItem())
        );

        if (items.isEmpty()) {
            return;
        }

        items.sort(Comparator.comparingDouble(this::distanceToSqr));
        ItemEntity target = items.get(0);

        if (distanceToSqr(target) > 2.25D) {
            getNavigation().moveTo(target, hasUpgrade(GolemUpgradeType.AIR) ? 1.25D : 1.1D);
            return;
        }

        ItemStack remaining = addToInventory(target.getItem().copy());

        if (remaining.isEmpty()) {
            target.discard();
        } else {
            target.setItem(remaining);
        }
    }

    private boolean acceptsItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (filterStack.isEmpty()) {
            return true;
        }
        if (hasDecoration(GolemDecorationType.WIRELESS_BACKPACK) && stack.getItem() == Items.ENDER_PEARL) {
            return true;
        }
        boolean same = ItemStack.isSameItemSameTags(filterStack, stack);
        return filterAllowList ? same : !same;
    }

    private void guardOwnerArea() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Player owner = ownerUuid == null ? null : serverLevel.getPlayerByUUID(ownerUuid);
        BlockPos center = guardPos != null ? guardPos : owner != null ? owner.blockPosition() : homePos;
        int range = hasUpgrade(GolemUpgradeType.AIR) ? 16 : 10;
        List<Mob> targets = level.getEntitiesOfClass(
                Mob.class,
                getBoundingBox().inflate(range),
                mob -> mob.isAlive() && mob instanceof Enemy && mob.distanceToSqr(center.getX(), center.getY(), center.getZ()) < range * range
        );

        if (targets.isEmpty() && coreType == GolemCoreType.BUTCHER) {
            butcherNearbyAnimals();
            return;
        }

        if (targets.isEmpty()) {
            return;
        }

        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        Mob target = targets.get(0);
        setTarget(target);

        if (distanceToSqr(target) > 4.0D) {
            getNavigation().moveTo(target, hasUpgrade(GolemUpgradeType.AIR) ? 1.25D : 1.15D);
        } else {
            if (hasUpgrade(GolemUpgradeType.FIRE)) {
                target.setSecondsOnFire(3);
            }
            doHurtTarget(target);
        }
    }

    private void butcherNearbyAnimals() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<Animal> targets = level.getEntitiesOfClass(
                Animal.class,
                getBoundingBox().inflate(workRange()),
                animal -> animal.isAlive() && !animal.isBaby()
        );
        if (targets.isEmpty()) {
            return;
        }
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        LivingEntity target = targets.get(0);
        if (distanceToSqr(target) > 4.0D) {
            getNavigation().moveTo(target, 1.0D);
        } else {
            target.hurt(DamageSource.mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
    }

    private void harvestNearbyCrops() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = workPos == null ? blockPosition() : workPos;
        int range = workRange();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 2, range))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                if (distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > 3.0D) {
                    getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, hasUpgrade(GolemUpgradeType.AIR) ? 1.1D : 0.95D);
                    return;
                }
                List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), this, ItemStack.EMPTY);
                level.setBlock(pos, crop.getStateForAge(0), 3);
                for (ItemStack drop : drops) {
                    if (!acceptsItem(drop) && hasUpgrade(GolemUpgradeType.ORDER)) {
                        continue;
                    }
                    ItemStack remaining = addToInventory(drop.copy());
                    if (!remaining.isEmpty()) {
                        spawnAtLocation(remaining);
                    }
                }
                return;
            }
        }
    }

    private void lumberNearbyLogs() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos center = workPos == null ? blockPosition() : workPos;
        int range = workRange();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 6, range))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS)) {
                if (distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > 3.0D) {
                    getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, hasUpgrade(GolemUpgradeType.AIR) ? 1.05D : 0.90D);
                    return;
                }
                List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), this, ItemStack.EMPTY);
                level.destroyBlock(pos, false, this);
                for (ItemStack drop : drops) {
                    ItemStack remaining = addToInventory(drop.copy());
                    if (!remaining.isEmpty()) {
                        spawnAtLocation(remaining);
                    }
                }
                if (!hasUpgrade(GolemUpgradeType.FIRE)) {
                    return;
                }
            }
        }
    }

    private void fishAtWaterMarker() {
        if (workPos == null) {
            patrolBetweenMarkers();
            return;
        }
        if (distanceToSqr(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D) > 5.0D) {
            getNavigation().moveTo(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D, 0.9D);
            return;
        }
        if (tickCount % (hasUpgrade(GolemUpgradeType.WATER) ? 80 : 140) == 0 && addToInventory(new ItemStack(Items.COD)).isEmpty()) {
            level.broadcastEntityEvent(this, (byte) 7);
        }
    }

    private void patrolBetweenMarkers() {
        BlockPos a = homePos;
        BlockPos b = workPos;
        if (a == null || a.equals(BlockPos.ZERO) || b == null) {
            followOwnerOrHome();
            return;
        }
        BlockPos target = patrolToWork ? b : a;
        if (distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D) < 4.0D) {
            patrolToWork = !patrolToWork;
        } else {
            getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 0.85D);
        }
    }


    private void useWorkTarget() {
        if (workPos == null) {
            patrolBetweenMarkers();
            return;
        }
        if (distanceToSqr(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D) > 4.0D) {
            getNavigation().moveTo(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D, hasUpgrade(GolemUpgradeType.AIR) ? 1.05D : 0.85D);
            return;
        }
        BlockState state = level.getBlockState(workPos);
        if (state.isAir()) {
            int slot = findInventoryBlockItemSlot();
            if (slot >= 0 && inventory.get(slot).getItem() instanceof BlockItem blockItem) {
                level.setBlock(workPos, blockItem.getBlock().defaultBlockState(), 3);
                inventory.get(slot).shrink(1);
                if (inventory.get(slot).isEmpty()) {
                    inventory.set(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private void handleLiquidCore() {
        if (workPos == null) {
            patrolBetweenMarkers();
            return;
        }
        if (distanceToSqr(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D) > 5.0D) {
            getNavigation().moveTo(workPos.getX() + 0.5D, workPos.getY(), workPos.getZ() + 0.5D, 0.85D);
            return;
        }
        BlockState state = level.getBlockState(workPos);
        int emptyBucketSlot = findInventoryItemSlot(new ItemStack(Items.BUCKET));
        if (emptyBucketSlot >= 0 && state.is(Blocks.WATER)) {
            replaceInventoryOne(emptyBucketSlot, new ItemStack(Items.WATER_BUCKET));
            level.setBlock(workPos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (emptyBucketSlot >= 0 && state.is(Blocks.LAVA)) {
            replaceInventoryOne(emptyBucketSlot, new ItemStack(Items.LAVA_BUCKET));
            level.setBlock(workPos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        BlockPos out = outputPos == null ? workPos : outputPos;
        if (level.getBlockState(out).isAir()) {
            int waterSlot = findInventoryItemSlot(new ItemStack(Items.WATER_BUCKET));
            if (waterSlot >= 0) {
                level.setBlock(out, Blocks.WATER.defaultBlockState(), 3);
                replaceInventoryOne(waterSlot, new ItemStack(Items.BUCKET));
                return;
            }
            int lavaSlot = findInventoryItemSlot(new ItemStack(Items.LAVA_BUCKET));
            if (lavaSlot >= 0) {
                level.setBlock(out, Blocks.LAVA.defaultBlockState(), 3);
                replaceInventoryOne(lavaSlot, new ItemStack(Items.BUCKET));
            }
        }
    }

    private void handleEssentiaCore() {
        BlockPos input = inputPos == null ? workPos : inputPos;
        BlockPos output = outputPos == null ? homePos : outputPos;
        EssentiaJarBlockEntity source = findNearbyEssentiaJar(input, 2, true);
        EssentiaJarBlockEntity target = findNearbyEssentiaJar(output, 2, false);

        if (source != null) {
            int emptyPhialSlot = findEmptyPhialSlot();
            Aspect aspect = source.storedAspect();
            if (emptyPhialSlot >= 0 && aspect != null && source.amount() >= 8) {
                ItemStack phial = inventory.get(emptyPhialSlot).copy();
                phial.setCount(1);
                EssentiaPhialItem.setEssentia(phial, aspect, 8);
                source.aspects().remove(aspect, 8);
                source.setChangedAndSync();
                replaceInventoryOne(emptyPhialSlot, phial);
                return;
            }
        }

        if (target != null) {
            int filledPhialSlot = findFilledPhialSlot();
            if (filledPhialSlot >= 0) {
                ItemStack phial = inventory.get(filledPhialSlot);
                Aspect aspect = EssentiaPhialItem.getAspect(phial);
                int amount = EssentiaPhialItem.getAmount(phial);
                int accepted = target.acceptFromTube(aspect, amount, false);
                if (accepted > 0) {
                    ItemStack empty = new ItemStack(ThaumcraftMod.ESSENTIA_PHIAL.get());
                    replaceInventoryOne(filledPhialSlot, empty);
                }
            }
        }
    }

    private void emptyInputContainer() {
        BlockPos center = inputPos == null ? homePos : inputPos;
        Container container = findNearbyContainer(center, 2);
        if (container == null) {
            pickupNearbyItems();
            return;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && acceptsItem(stored)) {
                ItemStack copy = stored.copy();
                copy.setCount(1);
                ItemStack remaining = addToInventory(copy);
                if (remaining.isEmpty()) {
                    stored.shrink(1);
                    container.setChanged();
                }
                return;
            }
        }
    }

    private void fillOutputContainer() {
        if (!deliverInventoryToNearbyContainer()) {
            pickupNearbyItems();
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

    private int activeSlots() {
        int slots = material.inventorySlots();
        if (hasUpgrade(GolemUpgradeType.EARTH)) {
            slots += 6;
        }
        if (hasDecoration(GolemDecorationType.WIRELESS_BACKPACK)) {
            slots += 9;
        }
        if (hasUpgrade(GolemUpgradeType.ORDER)) {
            slots += 3;
        }
        return Math.min(inventory.size(), slots);
    }


    private int findInventoryBlockItemSlot() {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private int findInventoryItemSlot(ItemStack sample) {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, sample)) {
                return i;
            }
        }
        return -1;
    }

    private int findEmptyPhialSlot() {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof EssentiaPhialItem && !EssentiaPhialItem.isFilled(stack)) {
                return i;
            }
        }
        return -1;
    }

    private int findFilledPhialSlot() {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof EssentiaPhialItem && EssentiaPhialItem.isFilled(stack)) {
                return i;
            }
        }
        return -1;
    }

    private void replaceInventoryOne(int slot, ItemStack replacement) {
        ItemStack stack = inventory.get(slot);
        stack.shrink(1);
        if (stack.isEmpty()) {
            inventory.set(slot, replacement.copy());
        } else {
            ItemStack remaining = addToInventory(replacement.copy());
            if (!remaining.isEmpty()) {
                spawnAtLocation(remaining);
            }
        }
    }

    private EssentiaJarBlockEntity findNearbyEssentiaJar(BlockPos center, int radius, boolean requireContents) {
        if (center == null) {
            return null;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EssentiaJarBlockEntity jar) {
                if (!requireContents || jar.amount() > 0) {
                    return jar;
                }
            }
        }
        return null;
    }

    private ItemStack addToInventory(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < activeSlots(); i++) {
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

        for (int i = 0; i < activeSlots(); i++) {
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

    private void deliverInventory() {
        if (!(level instanceof ServerLevel serverLevel) || tickCount % 20 != 0) {
            return;
        }

        boolean deliveredToContainer = deliverInventoryToNearbyContainer();
        if (!deliveredToContainer && !hasUpgrade(GolemUpgradeType.ORDER)) {
            deliverInventoryToOwner(serverLevel);
        }
    }

    private boolean deliverInventoryToNearbyContainer() {
        BlockPos center = outputPos != null ? outputPos : homePos == null || homePos.equals(BlockPos.ZERO) ? blockPosition() : homePos;
        Container exact = outputPos == null ? null : findNearbyContainer(outputPos, 1);
        if (exact != null) {
            return deliverInventoryToContainer(exact);
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -2, -3), center.offset(3, 2, 3))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container && deliverInventoryToContainer(container)) {
                return true;
            }
        }
        return false;
    }

    private boolean deliverInventoryToContainer(Container container) {
        boolean movedAny = false;
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && (coreType != GolemCoreType.SORTING || acceptsItem(stack) || hasUpgrade(GolemUpgradeType.ORDER))) {
                ItemStack remainder = insertIntoContainer(container, stack.copy());
                if (remainder.getCount() != stack.getCount()) {
                    inventory.set(i, remainder);
                    movedAny = true;
                }
            }
        }
        return movedAny;
    }

    private Container findNearbyContainer(BlockPos center, int radius) {
        if (center == null) {
            return null;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                return container;
            }
        }
        return null;
    }

    private ItemStack insertIntoContainer(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, stack)) {
                int space = Math.min(stored.getMaxStackSize(), container.getMaxStackSize()) - stored.getCount();
                if (space > 0) {
                    int move = Math.min(space, stack.getCount());
                    stored.grow(move);
                    stack.shrink(move);
                    container.setChanged();
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (stored.isEmpty()) {
                ItemStack copy = stack.copy();
                int move = Math.min(container.getMaxStackSize(), stack.getCount());
                copy.setCount(move);
                container.setItem(slot, copy);
                stack.shrink(move);
                container.setChanged();
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    private void deliverInventoryToOwner(ServerLevel serverLevel) {
        if (ownerUuid == null) {
            return;
        }

        Player owner = serverLevel.getPlayerByUUID(ownerUuid);

        if (owner == null || distanceToSqr(owner) > 64.0D) {
            return;
        }

        for (int i = 0; i < activeSlots(); i++) {
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

        if (owner != null && coreType != GolemCoreType.GUARD && coreType != GolemCoreType.BODYGUARD && distanceToSqr(owner) > 196.0D) {
            getNavigation().moveTo(owner, 1.0D);
            return;
        }

        if (homePos != null && !homePos.equals(BlockPos.ZERO) && distanceToSqr(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D) > 100.0D) {
            getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 0.8D);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }

        tag.putString("GolemMaterial", material.id());
        tag.putString("GolemCore", coreType.id());
        tag.putBoolean("Waiting", waiting);
        tag.putInt("TaskRadius", taskRadius);
        tag.putInt("TaskPriority", taskPriority);
        tag.putInt("HomeX", homePos.getX());
        tag.putInt("HomeY", homePos.getY());
        tag.putInt("HomeZ", homePos.getZ());
        writeNullablePos(tag, "Input", inputPos);
        writeNullablePos(tag, "Output", outputPos);
        writeNullablePos(tag, "Guard", guardPos);
        writeNullablePos(tag, "Work", workPos);
        tag.putString("GolemUpgrades", upgradesToString());
        tag.putString("GolemDecorations", decorationsToString());
        tag.putBoolean("FilterAllow", filterAllowList);
        if (!filterStack.isEmpty()) {
            CompoundTag filterTag = new CompoundTag();
            filterStack.save(filterTag);
            tag.put("FilterStack", filterTag);
        }

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
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
        }

        material = GolemMaterial.byName(tag.getString("GolemMaterial"));
        coreType = GolemCoreType.byName(tag.getString("GolemCore"));
        waiting = tag.getBoolean("Waiting");
        taskRadius = tag.contains("TaskRadius") ? Math.max(1, Math.min(32, tag.getInt("TaskRadius"))) : 8;
        taskPriority = tag.contains("TaskPriority") ? Math.max(0, Math.min(9, tag.getInt("TaskPriority"))) : 0;
        homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        inputPos = readNullablePos(tag, "Input");
        outputPos = readNullablePos(tag, "Output");
        guardPos = readNullablePos(tag, "Guard");
        workPos = readNullablePos(tag, "Work");
        upgrades.clear();
        for (String part : tag.getString("GolemUpgrades").split(",")) {
            if (!part.isBlank()) {
                upgrades.add(GolemUpgradeType.byName(part));
            }
        }
        decorations.clear();
        for (String part : tag.getString("GolemDecorations").split(",")) {
            if (!part.isBlank()) {
                decorations.add(GolemDecorationType.byName(part));
            }
        }
        filterAllowList = tag.getBoolean("FilterAllow");
        filterStack = tag.contains("FilterStack") ? ItemStack.of(tag.getCompound("FilterStack")) : ItemStack.EMPTY;
        applyProfileAttributes();

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

    private String upgradesToString() {
        StringBuilder builder = new StringBuilder();
        for (GolemUpgradeType upgrade : upgrades) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(upgrade.id());
        }
        return builder.toString();
    }

    private String decorationsToString() {
        StringBuilder builder = new StringBuilder();
        for (GolemDecorationType decoration : decorations) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(decoration.id());
        }
        return builder.toString();
    }

    private void writeNullablePos(CompoundTag tag, String key, BlockPos pos) {
        if (pos != null) {
            tag.putBoolean(key + "Has", true);
            tag.putInt(key + "X", pos.getX());
            tag.putInt(key + "Y", pos.getY());
            tag.putInt(key + "Z", pos.getZ());
        }
    }

    private BlockPos readNullablePos(CompoundTag tag, String key) {
        if (!tag.getBoolean(key + "Has")) {
            return null;
        }
        return new BlockPos(tag.getInt(key + "X"), tag.getInt(key + "Y"), tag.getInt(key + "Z"));
    }

    @Override
    public Component getName() {
        return Component.literal("Thaumic Golem [" + material.id() + " / " + coreType.id() + "]");
    }
}
