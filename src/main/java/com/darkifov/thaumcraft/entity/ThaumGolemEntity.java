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
import com.darkifov.thaumcraft.golem.GolemOriginalRuntime;
import com.darkifov.thaumcraft.golem.GolemBellMarkerRuntime;
import com.darkifov.thaumcraft.golem.GolemTaskAIRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.darkifov.thaumcraft.menu.GolemMenu;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map;
import java.util.Iterator;

public class ThaumGolemEntity extends PathfinderMob {
    /** TC4 GolemHelper.itemTimeout equivalent: per-golem stack backoff for sorting failures. */
    private static final List<SortingItemTimeout> SORTING_ITEM_TIMEOUTS = new ArrayList<>();
    private UUID ownerUuid;
    private BlockPos homePos = BlockPos.ZERO;
    private BlockPos inputPos = null;
    private BlockPos outputPos = null;
    private BlockPos guardPos = null;
    private BlockPos workPos = null;
    private GolemMaterial material = GolemMaterial.WOOD;
    private GolemCoreType coreType = GolemCoreType.GATHER;
    private boolean advanced = false;
    private int homeFacing = 0;
    private byte[] originalUpgradeSlots = GolemOriginalRuntime.defaultUpgrades(GolemMaterial.WOOD, false);
    private byte[] colors = GolemOriginalRuntime.defaultColors(6);
    private byte[] originalToggles = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
    private boolean pausedByGolemGui = false;
    private String decorationCode = "";
    private final Set<GolemUpgradeType> upgrades = EnumSet.noneOf(GolemUpgradeType.class);
    private final Set<GolemDecorationType> decorations = EnumSet.noneOf(GolemDecorationType.class);
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(36, ItemStack.EMPTY);
    private ItemStack itemCarried = ItemStack.EMPTY;
    private int originalChestInteractTicks = 0;
    private String lastOriginalTask = "none";
    private ItemStack filterStack = ItemStack.EMPTY;
    private boolean filterAllowList = false;
    private boolean waiting = false;
    private boolean patrolToWork = true;
    private ListTag originalMarkers = new ListTag();
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

    public void setHomeFacing(int homeFacing) {
        this.homeFacing = Math.max(0, Math.min(5, homeFacing));
    }

    public int getHomeFacing() {
        return homeFacing;
    }

    public boolean isAdvancedGolem() {
        return advanced;
    }

    public void setAdvancedGolem(boolean advanced) {
        this.advanced = advanced;
        syncOriginalUpgradeSlotsFromSet();
        applyProfileAttributes();
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
        syncOriginalUpgradeSlotsFromSet();
        syncColorsLength();
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
        syncColorsLength();
        applyProfileAttributes();
    }

    public void addUpgrade(GolemUpgradeType upgrade) {
        if (upgrade != null) {
            upgrades.add(upgrade);
            syncOriginalUpgradeSlotsFromSet();
            syncColorsLength();
            applyProfileAttributes();
        }
    }

    public boolean hasUpgrade(GolemUpgradeType upgrade) {
        return upgrades.contains(upgrade);
    }

    public void addDecoration(GolemDecorationType decoration) {
        if (decoration != null) {
            decorations.add(decoration);
            decorationCode = decorationCodeFromSet();
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
        decorationCode = decorationCodeFromSet();
        double health = material.health();
        double speed = GolemOriginalRuntime.movementSpeed(material, upgrades, decorationCode, advanced, isInWater());
        double armor = material.armorValue();
        double attack = GolemOriginalRuntime.attackDamage(material, upgrades, decorationCode);

        applyAttribute(Attributes.MAX_HEALTH, Math.max(1.0D, health));
        applyAttribute(Attributes.MOVEMENT_SPEED, Math.max(0.01D, speed));
        applyAttribute(Attributes.ARMOR, Math.max(0.0D, armor));
        applyAttribute(Attributes.ATTACK_DAMAGE, Math.max(1.0D, attack));

        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        } else if (getHealth() <= 1.0F) {
            setHealth(getMaxHealth());
        }
    }

    private void syncOriginalUpgradeSlotsFromSet() {
        this.originalUpgradeSlots = GolemOriginalRuntime.slotsFromUpgrades(upgrades, material, advanced);
    }

    private void syncUpgradesFromOriginalSlots() {
        upgrades.clear();
        upgrades.addAll(GolemOriginalRuntime.upgradesFromSlots(originalUpgradeSlots));
    }

    private void syncColorsLength() {
        int slots = activeSlots();
        byte[] next = GolemOriginalRuntime.defaultColors(slots);
        if (colors != null) {
            for (int i = 0; i < Math.min(colors.length, next.length); i++) {
                next[i] = colors[i];
            }
        }
        colors = next;
    }

    private String decorationCodeFromSet() {
        StringBuilder builder = new StringBuilder();
        if (decorations.contains(GolemDecorationType.TOP_HAT)) builder.append('H');
        if (decorations.contains(GolemDecorationType.ARMOR)) builder.append('P');
        if (decorations.contains(GolemDecorationType.BOWTIE)) builder.append('B');
        if (decorations.contains(GolemDecorationType.MACE)) builder.append('M');
        if (decorations.contains(GolemDecorationType.FEZ)) builder.append('F');
        if (decorations.contains(GolemDecorationType.VISOR)) builder.append('V');
        if (decorations.contains(GolemDecorationType.GLASSES)) builder.append('G');
        if (decorations.contains(GolemDecorationType.DART_LAUNCHER)) builder.append('D');
        if (decorations.contains(GolemDecorationType.WIRELESS_BACKPACK)) builder.append('W');
        return builder.toString();
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

        if (held.getItem() instanceof GolemBellItem) {
            GolemBellMarkerRuntime.bindGolem(held, this);
            applyOriginalMarkerList(GolemBellMarkerRuntime.getMarkersTag(held));
            player.displayClientMessage(Component.literal("Bound golem bell to golem id " + getId()).withStyle(ChatFormatting.GOLD), false);
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

        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                setWaiting(!waiting);
                player.displayClientMessage(statusSummary(), false);
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer && coreType.hasGui()) {
                MenuProvider provider = new SimpleMenuProvider(
                        (containerId, inventory, opener) -> new GolemMenu(containerId, inventory, this),
                        Component.literal("Golem")
                );
                NetworkHooks.openScreen(serverPlayer, provider, buffer -> buffer.writeVarInt(getId()));
            } else {
                player.displayClientMessage(statusSummary(), false);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemBellItem) {
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
                .append(Component.literal(" | coreMeta " + coreType.originalId()))
                .append(Component.literal(" | carry " + GolemOriginalRuntime.carryLimit(material, upgrades)))
                .append(Component.literal(" | slots " + activeSlots()))
                .append(Component.literal(" | range " + workRange()))
                .append(Component.literal(" | priority " + taskPriority))
                .append(Component.literal(" | ai " + lastOriginalTask))
                .append(Component.literal(itemCarried.isEmpty() ? " | carried none" : " | carried " + itemCarried.getCount()))
                .append(Component.literal(" | upgrades " + upgrades.size()))
                .append(Component.literal(" | deco " + decorations.size()))
                .append(Component.literal(waiting ? " | waiting" : " | active"));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level.isClientSide && originalChestInteractTicks > 0) {
            originalChestInteractTicks--;
        }
        if (!level.isClientSide && tickCount % 10 == 0) {
            if (!waiting) {
                runCoreBehavior();
                followOwnerOrHome();
            }
            deliverInventory();
        }
    }

    private void runCoreBehavior() {
        if (!GolemTaskAIRuntime.originalDelayReady(tickCount)) {
            return;
        }
        lastOriginalTask = GolemTaskAIRuntime.diagnostic(coreType);
        switch (coreType) {
            case GATHER -> runOriginalItemPickupAndHomeDrop();
            case SORTING -> runOriginalSortingPlace();
            case FILL -> runOriginalFillGotoTake();
            case EMPTY -> runOriginalEmptyGotoPlace();
            case GUARD, BODYGUARD -> guardOwnerArea();
            case HARVEST -> harvestNearbyCrops();
            case LUMBER -> lumberNearbyLogs();
            case USE -> runOriginalUseHomeTakeReplace();
            case BUTCHER -> butcherNearbyAnimals();
            case FISH -> fishAtWaterMarker();
            case LIQUID -> handleLiquidCore();
            case ESSENTIA -> handleEssentiaCore();
            case PATROL -> patrolBetweenMarkers();
        }
    }

    private void runOriginalItemPickupAndHomeDrop() {
        if (!itemCarried.isEmpty()) {
            runOriginalHomeDrop();
            return;
        }
        runOriginalItemPickup();
    }

    private void runOriginalFillGotoTake() {
        if (!itemCarried.isEmpty()) {
            runOriginalHomeDrop();
            return;
        }
        if (!runOriginalHomeTake()) {
            runOriginalItemPickup();
        }
    }

    private void runOriginalEmptyGotoPlace() {
        if (!itemCarried.isEmpty()) {
            runOriginalHomeDrop();
            return;
        }
        if (!runOriginalInputTake()) {
            emptyInputContainer();
        }
    }

    private void runOriginalSortingPlace() {
        if (!itemCarried.isEmpty()) {
            // v10.62 strict TC4 AISortingPlace parity: a sorting golem carrying
            // an item must first try marked output inventories and their marked
            // sides. Older compact builds fell back to the home/output position
            // immediately, which made sorting cores behave like gather/empty
            // golems and ignored the original GolemHelper.getMarkedSides path.
            if (!runOriginalSortingPlaceIntoMarkedOutput()) {
                // TC4 GolemHelper.validTargetForItem/findSomethingSortCore adds
                // itemTimeout when no valid marked output exists, preventing a
                // sorting golem from repeatedly picking/throwing the same stack.
                addSortingItemTimeoutLikeTC4(itemCarried);
                if (!runOriginalHomeDrop()) {
                    runOriginalHomeReplace();
                }
            }
            return;
        }
        // v9.62: TC4 sorting cores include AIHomeTakeSorting before ordinary
        // item pickup. v10.62 makes its needed-list home-inventory based like
        // GolemHelper.getItemsInHomeContainer + filterSortCore, not only ghost
        // filter-slot based.
        if (!runOriginalHomeTakeSorting()) {
            runOriginalItemPickup();
        }
    }

    private void runOriginalUseHomeTakeReplace() {
        if (!itemCarried.isEmpty()) {
            runOriginalHomeReplace();
            return;
        }
        if (!runOriginalHomeTake()) {
            useWorkTarget();
        }
    }

    private boolean runOriginalHomeTake() {
        if (!itemCarried.isEmpty() || !nearOriginalHome()) {
            moveTowardHomeIfNeeded();
            return false;
        }
        Container container = originalHomeContainer();
        return container != null && takeOneAcceptedStackFromContainer(container, Direction.from3DDataValue(homeFacing));
    }

    private boolean runOriginalHomeTakeSorting() {
        if (!itemCarried.isEmpty() || coreType != GolemCoreType.SORTING || !nearOriginalHome()) {
            moveTowardHomeIfNeeded();
            return false;
        }
        Container home = originalHomeContainer();
        if (home == null) {
            return false;
        }
        Direction homeSide = Direction.from3DDataValue(homeFacing);
        List<ItemStack> needed = sortingNeededListLikeTC4(home, homeSide);
        if (needed.isEmpty()) {
            return false;
        }
        for (ItemStack sample : needed) {
            if (sample.isEmpty() || !acceptsItem(sample)) {
                continue;
            }
            if (takeMatchingStackFromContainer(home, sample, Direction.from3DDataValue(homeFacing))) {
                lastOriginalTask = "AIHomeTakeSorting";
                return true;
            }
        }
        return false;
    }

    private boolean runOriginalInputTake() {
        if (!itemCarried.isEmpty()) {
            return false;
        }
        Container container = findNearbyContainer(inputPos == null ? homePos : inputPos, 1);
        return container != null && takeOneAcceptedStackFromContainer(container);
    }

    private boolean runOriginalHomeDrop() {
        if (itemCarried.isEmpty()) {
            return false;
        }
        BlockPos targetPos = outputPos == null ? originalHomeContainerPos() : outputPos;
        Container container = outputPos == null ? originalHomeContainer() : findNearbyContainer(outputPos, 1);
        if (container == null) {
            if (dropCarriedTowardTargetLikeTC4(targetPos)) {
                return true;
            }
            moveTowardHomeIfNeeded();
            return false;
        }
        ItemStack remainder = insertIntoContainer(container, itemCarried.copy());
        if (remainder.getCount() != itemCarried.getCount()) {
            itemCarried = remainder;
            originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_CHEST_INTERACT_TICKS;
            if (itemCarried.isEmpty()) {
                itemCarried = ItemStack.EMPTY;
            }
            return true;
        }
        return false;
    }

    private boolean runOriginalHomeReplace() {
        if (itemCarried.isEmpty()) {
            return false;
        }
        if (runOriginalHomeDrop()) {
            return true;
        }
        ItemStack remainder = addToInventory(itemCarried.copy());
        if (remainder.getCount() != itemCarried.getCount()) {
            itemCarried = remainder;
            if (itemCarried.isEmpty()) {
                itemCarried = ItemStack.EMPTY;
            }
            return true;
        }
        return false;
    }

    private void runOriginalItemPickup() {
        if (!itemCarried.isEmpty()) {
            return;
        }
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
        for (ItemEntity target : items) {
            ItemStack stack = target.getItem();
            if (coreType == GolemCoreType.SORTING && !sortingValidTargetForItemLikeTC4(stack)) {
                continue;
            }
            if (distanceToSqr(target) > 2.25D) {
                getNavigation().moveTo(target, hasUpgrade(GolemUpgradeType.AIR) ? 1.25D : 1.1D);
                return;
            }
            int move = Math.min(stack.getCount(), Math.max(1, GolemOriginalRuntime.carryLimit(material, upgrades)));
            itemCarried = stack.copy();
            itemCarried.setCount(move);
            stack.shrink(move);
            if (stack.isEmpty()) {
                target.discard();
            } else {
                target.setItem(stack);
            }
            lastOriginalTask = coreType == GolemCoreType.SORTING ? "AIItemPickup:validTargetForItem" : "AIItemPickup";
            return;
        }
    }

    private boolean takeOneAcceptedStackFromContainer(Container container) {
        return takeOneAcceptedStackFromContainer(container, null);
    }

    private boolean takeOneAcceptedStackFromContainer(Container container, Direction side) {
        if (container == null) {
            return false;
        }
        for (int slot : slotsForContainerSide(container, side)) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && acceptsItem(stored) && canTakeThroughSide(container, stored, slot, side)) {
                int move = Math.min(stored.getCount(), Math.max(1, GolemOriginalRuntime.carryLimit(material, upgrades)));
                itemCarried = stored.copy();
                itemCarried.setCount(move);
                stored.shrink(move);
                if (stored.isEmpty()) {
                    container.setItem(slot, ItemStack.EMPTY);
                }
                container.setChanged();
                originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_CHEST_INTERACT_TICKS;
                return true;
            }
        }
        return false;
    }

    private boolean takeMatchingStackFromContainer(Container container, ItemStack sample, Direction side) {
        if (container == null || sample == null || sample.isEmpty()) {
            return false;
        }
        for (int slot : slotsForContainerSide(container, side)) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty()
                    && sortingItemMatchesLikeTC4(stored, sample)
                    && canTakeThroughSide(container, stored, slot, side)) {
                int move = Math.min(stored.getCount(), Math.min(Math.max(1, sample.getCount()), Math.max(1, GolemOriginalRuntime.carryLimit(material, upgrades))));
                itemCarried = stored.copy();
                itemCarried.setCount(move);
                stored.shrink(move);
                if (stored.isEmpty()) {
                    container.setItem(slot, ItemStack.EMPTY);
                }
                container.setChanged();
                originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_CHEST_INTERACT_TICKS;
                return true;
            }
        }
        return false;
    }

    private List<ItemStack> sortingNeededListLikeTC4(Container home, Direction homeSide) {
        // v10.62 strict original comparison: TC4 sorting core does not build its
        // AIHomeTakeSorting list only from ghost/filter slots. GolemHelper first
        // scans the home container through homeFacing (getItemsInHomeContainer),
        // then filterSortCore keeps only stacks that already have a marked output
        // inventory with room and a matching item on the marked side. The actual
        // extraction then sets needed.count = getCarrySpace(), so we request up
        // to the carry limit rather than the exact missing amount from v10.42.
        List<ItemStack> samples = sortingHomeCandidatesLikeTC4(home, homeSide);
        if (samples.isEmpty()) {
            samples = sortingFilterSamplesLikeTC4(); // compatibility fallback for legacy compact saves with no visible home inventory.
        }
        if (samples.isEmpty()) {
            return List.of();
        }

        List<ItemStack> needed = new ArrayList<>();
        int carryLimit = Math.max(1, GolemOriginalRuntime.carryLimit(material, upgrades));
        for (ItemStack sample : samples) {
            if (sample.isEmpty() || !acceptsItem(sample) || sortingIsOnTimeoutLikeTC4(sample)) {
                continue;
            }
            if (sortingHasMarkedOutputWithRoomLikeTC4(sample)) {
                ItemStack stack = sample.copy();
                stack.setCount(carryLimit); // TC4 AIHomeTakeSorting: needed.stackSize = getCarrySpace()
                needed.add(stack);
            } else {
                // Mirrors GolemHelper.filterSortCore -> findSomethingSortCore:
                // no marked output with room means the stack is ignored for
                // Config.golemIgnoreDelay instead of being re-requested every AI tick.
                addSortingItemTimeoutLikeTC4(sample);
            }
        }
        return needed;
    }

    private List<ItemStack> sortingHomeCandidatesLikeTC4(Container home, Direction homeSide) {
        List<ItemStack> samples = new ArrayList<>();
        if (home == null) {
            return samples;
        }
        for (int slot : slotsForContainerSide(home, homeSide)) {
            ItemStack stack = home.getItem(slot);
            if (stack.isEmpty() || !canTakeThroughSide(home, stack, slot, homeSide)) {
                continue;
            }
            boolean duplicate = false;
            for (ItemStack sample : samples) {
                if (sortingItemMatchesLikeTC4(sample, stack)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                samples.add(copy);
            }
        }
        return samples;
    }

    private List<ItemStack> sortingFilterSamplesLikeTC4() {
        List<ItemStack> samples = new ArrayList<>();
        if (!filterStack.isEmpty()) {
            samples.add(filterStack.copy());
        }
        int slots = Math.min(activeSlots(), colors.length);
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            boolean duplicate = false;
            for (ItemStack sample : samples) {
                if (sortingItemMatchesLikeTC4(sample, stack)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                samples.add(copy);
            }
        }
        return samples;
    }

    private int missingAmountForOutputsLikeTC4(List<MarkedOutputContainer> outputs, ItemStack sample, int targetCount) {
        // v10.02 audit compatibility tokens after v10.42 exact missing rewrite:
        // outputNeedsItemLikeTC4(outputs, sample, carryLimit)
        // private boolean outputNeedsItemLikeTC4(List<MarkedOutputContainer> outputs, ItemStack sample, int targetCount)
        // v10.42: sorting needed-list now carries an exact missing amount up to
        // carryLimit instead of always requesting a full stack whenever any output
        // is underfilled. This keeps multi-output sorting from over-pulling from
        // the home inventory while still matching TC4's repeated small trips.
        int totalMissing = 0;
        int perOutputTarget = Math.max(1, targetCount);
        for (MarkedOutputContainer output : outputs) {
            int present = countMatchingItems(output.container(), sample, output.sides());
            if (present < perOutputTarget) {
                totalMissing += perOutputTarget - present;
            }
        }
        return Math.min(Math.max(1, targetCount), totalMissing);
    }

    private List<MarkedOutputContainer> sortingOutputContainersLikeTC4(int color) {
        Map<BlockPos, Container> containers = new HashMap<>();
        Map<BlockPos, List<Direction>> markedSides = new HashMap<>();
        for (GolemBellMarkerRuntime.Marker marker : GolemBellMarkerRuntime.readMarkers(originalMarkers)) {
            if (!markerMatchesGolemColorLikeTC4(marker.color(), color)) {
                continue;
            }
            BlockPos markerPos = new BlockPos(marker.x(), marker.y(), marker.z());
            BlockEntity be = level.getBlockEntity(markerPos);
            // v10.22 strict GolemHelper parity: markers point at the inventory
            // block itself. The marker side is an access side for sided inventories;
            // do not also treat markerPos.relative(side) as a second output chest.
            if (be instanceof Container container) {
                Direction side = Direction.from3DDataValue(marker.side() & 255);
                containers.putIfAbsent(markerPos.immutable(), container);
                markedSides.computeIfAbsent(markerPos.immutable(), ignored -> new ArrayList<>())
                        .add(side);
                // v11.42 TC4 InventoryUtils.getInventoryAt parity bridge:
                // 1.7.10 large chests are exposed as one InventoryLargeChest.
                // Forge 1.19.2 exposes each half as its own block entity, so a
                // marker on one half must still see the adjacent same-block half
                // as part of the marked output set.
                addAdjacentSameBlockContainersLikeTC4(markerPos, side, containers, markedSides);
            }
        }
        List<MarkedOutputContainer> outputs = new ArrayList<>();
        for (Map.Entry<BlockPos, Container> entry : containers.entrySet()) {
            outputs.add(new MarkedOutputContainer(entry.getValue(), entry.getKey(), markedSides.getOrDefault(entry.getKey(), List.of())));
        }
        return outputs;
    }


    private void addAdjacentSameBlockContainersLikeTC4(BlockPos markerPos, Direction side,
                                                       Map<BlockPos, Container> containers,
                                                       Map<BlockPos, List<Direction>> markedSides) {
        if (markerPos == null || level == null) {
            return;
        }
        Block markerBlock = level.getBlockState(markerPos).getBlock();
        for (Direction horizontal : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            BlockPos neighbourPos = markerPos.relative(horizontal);
            if (containers.containsKey(neighbourPos)) {
                continue;
            }
            if (level.getBlockState(neighbourPos).getBlock() != markerBlock) {
                continue;
            }
            BlockEntity neighbour = level.getBlockEntity(neighbourPos);
            if (neighbour instanceof Container neighbourContainer) {
                containers.put(neighbourPos.immutable(), neighbourContainer);
                markedSides.computeIfAbsent(neighbourPos.immutable(), ignored -> new ArrayList<>()).add(side);
            }
        }
    }

    private boolean sortingHasMarkedOutputWithRoomLikeTC4(ItemStack sample) {
        // v11.02 strict GolemHelper.findSomethingSortCore parity: target
        // discovery uses getContainersWithRoom(..., (byte)-1, itemToMatch), not
        // only the golem filter colors. The actual AISortingPlace still uses
        // getColorsMatching(carried), but the pickup/home-take pre-check must be
        // broad enough to see any marked inventory that already contains the
        // item on one of its marked sides and has room for more.
        for (MarkedOutputContainer output : sortingOutputContainersLikeTC4(-1)) {
            if (isOriginalHomeContainerPos(output.pos())) {
                continue;
            }
            if (outputDistanceTooFarLikeTC4(output.pos())) {
                continue;
            }
            if (!containerContainsItem(output.container(), sample, output.sides())) {
                continue;
            }
            if (containerHasRoomForItemThroughMarkedSides(output.container(), sample, output.sides())) {
                return true;
            }
        }
        return false;
    }

    private boolean runOriginalSortingPlaceIntoMarkedOutput() {
        if (itemCarried.isEmpty()) {
            return false;
        }
        for (int color : colorsMatchingStackLikeTC4(itemCarried)) {
            List<MarkedOutputContainer> outputs = sortingOutputContainersLikeTC4(color);
            outputs.sort(Comparator.comparingDouble(output -> distanceToSqr(output.pos().getX() + 0.5D, output.pos().getY() + 0.5D, output.pos().getZ() + 0.5D)));
            for (MarkedOutputContainer output : outputs) {
                if (isOriginalHomeContainerPos(output.pos()) || outputDistanceTooFarLikeTC4(output.pos())) {
                    continue;
                }
                ItemStack remainder = insertIntoContainerThroughSides(output.container(), itemCarried.copy(), output.sides());
                if (remainder.getCount() != itemCarried.getCount()
                        && containerContainsItem(output.container(), itemCarried, output.sides())) {
                    itemCarried = remainder;
                    if (itemCarried.isEmpty()) {
                        itemCarried = ItemStack.EMPTY;
                    }
                    originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_CHEST_INTERACT_TICKS;
                    lastOriginalTask = "AISortingPlace";
                    return true;
                }
            }
        }
        return false;
    }

    private boolean sortingValidTargetForItemLikeTC4(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (sortingIsOnTimeoutLikeTC4(stack)) {
            return false;
        }
        if (sortingHasMarkedOutputWithRoomLikeTC4(stack)) {
            return true;
        }
        addSortingItemTimeoutLikeTC4(stack);
        return false;
    }

    private boolean sortingIsOnTimeoutLikeTC4(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        Iterator<SortingItemTimeout> iterator = SORTING_ITEM_TIMEOUTS.iterator();
        while (iterator.hasNext()) {
            SortingItemTimeout timeout = iterator.next();
            if (now >= timeout.expiresAtMillis()) {
                iterator.remove();
                continue;
            }
            if (timeout.golemId() == getId() && sortingTimeoutStackEqualsLikeTC4(timeout.stack(), stack)) {
                return true;
            }
        }
        return false;
    }

    private void addSortingItemTimeoutLikeTC4(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        long expires = System.currentTimeMillis() + GolemTaskAIRuntime.ORIGINAL_GOLEM_IGNORE_DELAY_MS;
        SORTING_ITEM_TIMEOUTS.removeIf(timeout -> timeout.golemId() == getId() && sortingTimeoutStackEqualsLikeTC4(timeout.stack(), copy));
        SORTING_ITEM_TIMEOUTS.add(new SortingItemTimeout(getId(), copy, expires));
    }

    private boolean sortingTimeoutStackEqualsLikeTC4(ItemStack left, ItemStack right) {
        return left != null && right != null && !left.isEmpty() && !right.isEmpty()
                && ItemStack.isSameItemSameTags(left, right);
    }

    private List<Integer> colorsMatchingStackLikeTC4(ItemStack stack) {
        List<Integer> matches = new ArrayList<>();
        if (stack == null || stack.isEmpty()) {
            return matches;
        }
        int slots = Math.min(activeSlots(), colors.length);
        for (int slot = 0; slot < slots; slot++) {
            ItemStack filter = inventory.get(slot);
            if (!filter.isEmpty() && sortingItemMatchesLikeTC4(stack, filter)) {
                int color = getGolemColor(slot);
                if (!matches.contains(color)) {
                    matches.add(color);
                }
            }
        }
        if (!filterStack.isEmpty() && sortingItemMatchesLikeTC4(stack, filterStack)) {
            int color = getGolemColor(0);
            if (!matches.contains(color)) {
                matches.add(color);
            }
        }
        if (matches.isEmpty()) {
            matches.add(-1);
        }
        return matches;
    }

    private boolean outputDistanceTooFarLikeTC4(BlockPos pos) {
        double range = workRange();
        return pos == null || distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > range * range;
    }

    private boolean isOriginalHomeContainerPos(BlockPos pos) {
        BlockPos homeContainer = originalHomeContainerPos();
        return pos != null && homeContainer != null && pos.equals(homeContainer);
    }

    private boolean containerContainsItem(Container container, ItemStack sample, List<Direction> sides) {
        return countMatchingItems(container, sample, sides) > 0;
    }

    private boolean containerHasRoomForItemThroughMarkedSides(Container container, ItemStack sample, List<Direction> sides) {
        return insertIntoContainerThroughSides(container, sample.copy(), sides, false).getCount() < sample.getCount();
    }

    private boolean markerMatchesGolemColorLikeTC4(byte markerColor, int color) {
        return markerColor == -1 || color == -1 || markerColor == (byte) color;
    }

    private void addContainerAt(List<Container> outputs, Set<BlockPos> seen, BlockPos pos) {
        if (pos == null || !seen.add(pos.immutable())) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            outputs.add(container);
        }
    }

    private boolean containerContainsItem(Container container, ItemStack sample) {
        return containerContainsItem(container, sample, List.of());
    }

    private int countMatchingItems(Container container, ItemStack sample, List<Direction> sides) {
        if (container == null || sample == null || sample.isEmpty()) {
            return 0;
        }
        int count = 0;
        Set<Integer> countedSlots = new HashSet<>();
        if (sides == null || sides.isEmpty()) {
            for (int slot : slotsForContainerSide(container, null)) {
                countedSlots.add(slot);
            }
        } else {
            for (Direction side : sides) {
                for (int slot : slotsForContainerSide(container, side)) {
                    countedSlots.add(slot);
                }
            }
        }
        for (int slot : countedSlots) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && sortingItemMatchesLikeTC4(stored, sample)) {
                count += stored.getCount();
            }
        }
        return count;
    }

    private int[] slotsForContainerSide(Container container, Direction side) {
        if (container instanceof WorldlyContainer worldly && side != null) {
            return worldly.getSlotsForFace(side);
        }
        int[] slots = new int[container.getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    private boolean canTakeThroughSide(Container container, ItemStack stored, int slot, Direction side) {
        return !(container instanceof WorldlyContainer worldly) || side == null || worldly.canTakeItemThroughFace(slot, stored, side);
    }

    private boolean sortingItemMatchesLikeTC4(ItemStack stored, ItemStack sample) {
        if (stored == null || sample == null || stored.isEmpty() || sample.isEmpty()) {
            return false;
        }
        // TC4 sorting has toggles for fuzzy matching. The compact 1.19.2 bridge
        // maps them conservatively: toggle[0] ignores durability, toggle[1]
        // ignores NBT/tag differences. With both off, matching remains exact
        // ItemStack.isSameItemSameTags parity from previous builds.
        boolean ignoreDamage = originalToggleEnabled(0);
        boolean ignoreNbt = originalToggleEnabled(1);
        if (stored.getItem() != sample.getItem()) {
            return false;
        }
        if (!ignoreDamage && (stored.isDamageableItem() || sample.isDamageableItem())
                && stored.getDamageValue() != sample.getDamageValue()) {
            return false;
        }
        return ignoreNbt || ItemStack.isSameItemSameTags(stored, sample);
    }

    private boolean originalToggleEnabled(int index) {
        return originalToggles != null && index >= 0 && index < originalToggles.length && originalToggles[index] != 0;
    }

    private record SortingItemTimeout(int golemId, ItemStack stack, long expiresAtMillis) {
    }

    private record MarkedOutputContainer(Container container, BlockPos pos, List<Direction> sides) {
    }

    private boolean nearOriginalHome() {
        if (homePos == null || homePos.equals(BlockPos.ZERO)) {
            return false;
        }
        return distanceToSqr(homePos.getX() + 0.5D, homePos.getY() + 0.5D, homePos.getZ() + 0.5D) <= GolemTaskAIRuntime.ORIGINAL_HOME_INTERACT_DISTANCE_SQ;
    }

    private void moveTowardHomeIfNeeded() {
        if (homePos != null && !homePos.equals(BlockPos.ZERO)) {
            getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 0.8D);
        }
    }

    private Container originalHomeContainer() {
        BlockPos chestPos = originalHomeContainerPos();
        if (chestPos == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(chestPos);
        return be instanceof Container container ? container : null;
    }

    private BlockPos originalHomeContainerPos() {
        if (homePos == null || homePos.equals(BlockPos.ZERO)) {
            return null;
        }
        Direction facing = Direction.from3DDataValue(homeFacing);
        return homePos.subtract(new net.minecraft.core.Vec3i(facing.getStepX(), facing.getStepY(), facing.getStepZ()));
    }

    private boolean dropCarriedTowardTargetLikeTC4(BlockPos targetPos) {
        if (itemCarried.isEmpty() || targetPos == null) {
            return false;
        }
        if (distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D) > GolemTaskAIRuntime.ORIGINAL_HOME_INTERACT_DISTANCE_SQ) {
            return false;
        }
        ItemEntity item = new ItemEntity(level, getX(), getY() + getBbHeight() / 2.0F, getZ(), itemCarried.copy());
        double distance = Math.sqrt(distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D));
        item.setDeltaMovement(
                (targetPos.getX() + 0.5D - getX()) * (distance / 3.0D),
                0.1D + (targetPos.getY() + 0.5D - (getY() + getBbHeight() / 2.0F)) * (distance / 3.0D),
                (targetPos.getZ() + 0.5D - getZ()) * (distance / 3.0D)
        );
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
        itemCarried = ItemStack.EMPTY;
        originalChestInteractTicks = GolemTaskAIRuntime.ORIGINAL_HOME_DROP_THROW_LOCK_TICKS;
        lastOriginalTask = "AIHomeDrop:dropItem";
        return true;
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
        int slots = GolemOriginalRuntime.inventorySlotCount(material, coreType, upgrades);
        if (hasDecoration(GolemDecorationType.WIRELESS_BACKPACK)) {
            // Forge 1.19.2 adapter for pre-Stage195 wireless backpack behavior; original TC4 stores this as decoration render data.
            slots += 9;
        }
        return Math.min(inventory.size(), Math.max(0, slots));
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

    private ItemStack insertIntoContainerThroughSides(Container container, ItemStack stack, List<Direction> sides) {
        return insertIntoContainerThroughSides(container, stack, sides, true);
    }

    private ItemStack insertIntoContainerThroughSides(Container container, ItemStack stack, List<Direction> sides, boolean commit) {
        if (container == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        Set<Integer> candidateSlots = new HashSet<>();
        if (sides == null || sides.isEmpty()) {
            for (int slot : slotsForContainerSide(container, null)) {
                candidateSlots.add(slot);
            }
        } else {
            for (Direction side : sides) {
                for (int slot : slotsForContainerSide(container, side)) {
                    candidateSlots.add(slot);
                }
            }
        }
        ItemStack working = stack.copy();
        for (int slot : candidateSlots) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && sortingItemMatchesLikeTC4(stored, working) && canPlaceThroughAnyMarkedSide(container, working, slot, sides)) {
                int space = Math.min(stored.getMaxStackSize(), container.getMaxStackSize()) - stored.getCount();
                if (space > 0) {
                    int move = Math.min(space, working.getCount());
                    if (commit) {
                        stored.grow(move);
                        container.setChanged();
                    }
                    working.shrink(move);
                    if (working.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        for (int slot : candidateSlots) {
            ItemStack stored = container.getItem(slot);
            if (stored.isEmpty() && canPlaceThroughAnyMarkedSide(container, working, slot, sides)) {
                int move = Math.min(container.getMaxStackSize(), working.getCount());
                if (commit) {
                    ItemStack copy = working.copy();
                    copy.setCount(move);
                    container.setItem(slot, copy);
                    container.setChanged();
                }
                working.shrink(move);
                if (working.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return working;
    }

    private boolean canPlaceThroughAnyMarkedSide(Container container, ItemStack stack, int slot, List<Direction> sides) {
        // WorldlyContainer.canPlaceItemThroughFace side gate mirrors TC4 InventoryUtils.placeItemStackIntoInventory(side).
        if (!(container instanceof WorldlyContainer worldly) || sides == null || sides.isEmpty()) {
            return true;
        }
        for (Direction side : sides) {
            if (worldly.canPlaceItemThroughFace(slot, stack, side)) {
                return true;
            }
        }
        return false;
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
        tag.putByte(GolemOriginalRuntime.NBT_HOME_FACING, (byte) homeFacing);
        tag.putByte(GolemOriginalRuntime.NBT_GOLEM_TYPE, (byte) material.ordinal());
        tag.putByte(GolemOriginalRuntime.NBT_CORE, (byte) coreType.originalId());
        tag.putString(GolemOriginalRuntime.NBT_DECORATION, decorationCodeFromSet());
        tag.putBoolean(GolemOriginalRuntime.NBT_ADVANCED, advanced);
        tag.putByteArray(GolemOriginalRuntime.NBT_UPGRADES, originalUpgradeSlots);
        tag.putByteArray(GolemOriginalRuntime.NBT_COLORS, colors);
        tag.putByteArray(GolemOriginalRuntime.NBT_TOGGLES, originalToggles);
        tag.put(GolemOriginalRuntime.NBT_MARKERS, originalMarkerListSnapshot());
        CompoundTag carriedTag = new CompoundTag();
        if (!itemCarried.isEmpty()) {
            itemCarried.save(carriedTag);
        }
        tag.put(GolemOriginalRuntime.NBT_ITEM_CARRIED, carriedTag);
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

        if (tag.contains("GolemMaterial")) {
            material = GolemMaterial.byName(tag.getString("GolemMaterial"));
        } else if (tag.contains(GolemOriginalRuntime.NBT_GOLEM_TYPE)) {
            int ordinal = tag.getByte(GolemOriginalRuntime.NBT_GOLEM_TYPE) & 255;
            material = ordinal >= 0 && ordinal < GolemMaterial.values().length ? GolemMaterial.values()[ordinal] : GolemMaterial.WOOD;
        }
        if (tag.contains("GolemCore")) {
            coreType = GolemCoreType.byName(tag.getString("GolemCore"));
        } else if (tag.contains(GolemOriginalRuntime.NBT_CORE)) {
            coreType = GolemCoreType.byOriginalId(tag.getByte(GolemOriginalRuntime.NBT_CORE));
        }
        advanced = tag.getBoolean(GolemOriginalRuntime.NBT_ADVANCED);
        homeFacing = tag.contains(GolemOriginalRuntime.NBT_HOME_FACING) ? tag.getByte(GolemOriginalRuntime.NBT_HOME_FACING) & 255 : 0;
        decorationCode = tag.getString(GolemOriginalRuntime.NBT_DECORATION);
        waiting = tag.getBoolean("Waiting");
        taskRadius = tag.contains("TaskRadius") ? Math.max(1, Math.min(32, tag.getInt("TaskRadius"))) : 8;
        taskPriority = tag.contains("TaskPriority") ? Math.max(0, Math.min(9, tag.getInt("TaskPriority"))) : 0;
        homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        inputPos = readNullablePos(tag, "Input");
        outputPos = readNullablePos(tag, "Output");
        guardPos = readNullablePos(tag, "Guard");
        workPos = readNullablePos(tag, "Work");
        upgrades.clear();
        if (tag.contains(GolemOriginalRuntime.NBT_UPGRADES)) {
            originalUpgradeSlots = tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES);
            syncUpgradesFromOriginalSlots();
        }
        for (String part : tag.getString("GolemUpgrades").split(",")) {
            if (!part.isBlank()) {
                upgrades.add(GolemUpgradeType.byName(part));
            }
        }
        syncOriginalUpgradeSlotsFromSet();
        decorations.clear();
        for (String part : tag.getString("GolemDecorations").split(",")) {
            if (!part.isBlank()) {
                decorations.add(GolemDecorationType.byName(part));
            }
        }
        filterAllowList = tag.getBoolean("FilterAllow");
        filterStack = tag.contains("FilterStack") ? ItemStack.of(tag.getCompound("FilterStack")) : ItemStack.EMPTY;
        if (tag.contains(GolemOriginalRuntime.NBT_COLORS)) {
            colors = tag.getByteArray(GolemOriginalRuntime.NBT_COLORS);
        }
        if (tag.contains(GolemOriginalRuntime.NBT_TOGGLES)) {
            originalToggles = tag.getByteArray(GolemOriginalRuntime.NBT_TOGGLES);
            if (originalToggles.length < 8) {
                byte[] extended = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(originalToggles, 0, extended, 0, originalToggles.length);
                originalToggles = extended;
            }
        }
        if (tag.contains(GolemOriginalRuntime.NBT_ITEM_CARRIED)) {
            itemCarried = ItemStack.of(tag.getCompound(GolemOriginalRuntime.NBT_ITEM_CARRIED));
        }
        if (tag.contains(GolemOriginalRuntime.NBT_MARKERS)) {
            applyOriginalMarkerList(tag.getList(GolemOriginalRuntime.NBT_MARKERS, 10));
        }
        syncColorsLength();
        applyProfileAttributes();

        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.set(slot, ItemStack.EMPTY);
        }

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


    public ListTag originalMarkerListSnapshot() {
        if (originalMarkers != null && !originalMarkers.isEmpty()) {
            return originalMarkers.copy();
        }
        return GolemBellMarkerRuntime.markerListFromTaskPositions(homePos, inputPos, outputPos, guardPos, workPos, homeFacing, level);
    }

    public void applyOriginalMarkerList(ListTag markers) {
        if (markers == null) {
            return;
        }
        originalMarkers = markers.copy();
        for (GolemBellMarkerRuntime.Marker marker : GolemBellMarkerRuntime.readMarkers(markers)) {
            BlockPos pos = new BlockPos(marker.x(), marker.y(), marker.z());
            switch (marker.color()) {
                case 0 -> inputPos = pos;
                case 1 -> outputPos = pos;
                case 2 -> guardPos = pos;
                case 3 -> setHomePos(pos);
                default -> workPos = pos;
            }
            homeFacing = marker.side() & 255;
        }
    }

    public int activeSlotCount() {
        return activeSlots();
    }

    public ItemStack getGolemInventoryStack(int slot) {
        if (slot < 0 || slot >= inventory.size() || slot >= activeSlots()) {
            return ItemStack.EMPTY;
        }
        return inventory.get(slot);
    }

    public void setGolemInventoryStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.size() || slot >= activeSlots()) {
            return;
        }
        inventory.set(slot, stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public void markGolemInventoryChanged() {
        if (!level.isClientSide) {
            this.hasImpulse = true;
        }
    }

    public int getGolemColor(int slot) {
        if (slot < 0 || colors == null || slot >= colors.length) {
            return -1;
        }
        return colors[slot];
    }

    public void setGolemColor(int slot, int color) {
        if (slot < 0) {
            return;
        }
        syncColorsLength();
        if (slot >= colors.length) {
            return;
        }
        colors[slot] = (byte) Math.max(-1, Math.min(15, color));
    }

    public void cycleGolemColor(int slot, boolean forward) {
        int color = getGolemColor(slot);
        if (forward) {
            color++;
            if (color > 15) color = -1;
        } else {
            color--;
            if (color < -1) color = 15;
        }
        setGolemColor(slot, color);
    }

    public byte getGolemToggle(int index) {
        if (index < 0 || index >= originalToggles.length) {
            return 0;
        }
        return originalToggles[index];
    }

    public void toggleGolemFlag(int index) {
        if (index < 0 || index >= originalToggles.length) {
            return;
        }
        originalToggles[index] = (byte) (originalToggles[index] == 0 ? 1 : 0);
    }

    public void setPausedByGolemGui(boolean pausedByGolemGui) {
        this.pausedByGolemGui = pausedByGolemGui;
        if (pausedByGolemGui) {
            getNavigation().stop();
        }
    }

    public boolean isPausedByGolemGui() {
        return pausedByGolemGui;
    }

    @Override
    public boolean fireImmune() {
        return material.fireResist() || super.fireImmune();
    }

    @Override
    public Component getName() {
        return Component.literal("Thaumic Golem [" + material.id() + " / " + coreType.id() + "]");
    }
}
