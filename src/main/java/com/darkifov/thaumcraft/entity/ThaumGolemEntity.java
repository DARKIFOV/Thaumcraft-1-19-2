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
import com.darkifov.thaumcraft.blockentity.EssentiaReservoirBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlembicBlockEntity;
import com.darkifov.thaumcraft.blockentity.AlchemicalCentrifugeBlockEntity;
import com.darkifov.thaumcraft.blockentity.ThaumatoriumBlockEntity;
import com.darkifov.thaumcraft.golem.GolemCoreType;
import com.darkifov.thaumcraft.golem.GolemDecorationType;
import com.darkifov.thaumcraft.golem.GolemMarkerMode;
import com.darkifov.thaumcraft.golem.GolemMaterial;
import com.darkifov.thaumcraft.golem.GolemUpgradeType;
import com.darkifov.thaumcraft.golem.GolemOriginalRuntime;
import com.darkifov.thaumcraft.golem.GolemBellMarkerRuntime;
import com.darkifov.thaumcraft.golem.GolemTaskAIRuntime;
import com.darkifov.thaumcraft.golem.GolemItemHandlerContainerAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.darkifov.thaumcraft.menu.GolemMenu;
import com.darkifov.thaumcraft.item.TC4GolemCoreComponentItem;
import com.darkifov.thaumcraft.porting.TC4ResearchItems;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

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
import java.util.ArrayDeque;
import java.util.Deque;

public class ThaumGolemEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_MATERIAL =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CORE =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAGS =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_DECORATIONS =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_UPGRADES =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_COLORS =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_TOGGLES =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED =
            SynchedEntityData.defineId(ThaumGolemEntity.class, EntityDataSerializers.ITEM_STACK);
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
    private final Set<GolemDecorationType> decorations = EnumSet.noneOf(GolemDecorationType.class);
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(36, ItemStack.EMPTY);
    private ItemStack itemCarried = ItemStack.EMPTY;
    /** TC4 EntityGolemBase carried fluid is separate from the ghost filter inventory. */
    private ResourceLocation carriedFluidId = null;
    private int carriedFluidAmount = 0;
    /** TC4 essentia core carries a raw aspect/amount, not filled phials in ghost slots. */
    private Aspect carriedEssentia = null;
    private int carriedEssentiaAmount = 0;
    private int fishingCooldown = 300;
    private int originalChestInteractTicks = 0;
    private String lastOriginalTask = "none";
    private ItemStack filterStack = ItemStack.EMPTY;
    private boolean filterAllowList = false;
    private boolean waiting = false;
    private boolean patrolToWork = true;
    private ListTag originalMarkers = new ListTag();
    private int taskRadius = 8;
    private int taskPriority = 0;
    private int regenerationTimer = 0;
    private int combatCooldown = 0;
    private int rangedAttackCooldown = 0;
    /** TC4-style task retry/backoff: prevents an unreachable marker from monopolising every AI pass. */
    private int taskBackoffTicks = 0;
    private int schedulerStuckTicks = 0;
    private Vec3 schedulerLastPos = Vec3.ZERO;

    public ThaumGolemEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MATERIAL, GolemMaterial.WOOD.ordinal());
        entityData.define(DATA_CORE, GolemCoreType.GATHER.originalId());
        entityData.define(DATA_FLAGS, 0);
        entityData.define(DATA_DECORATIONS, "");
        entityData.define(DATA_UPGRADES, "f");
        entityData.define(DATA_COLORS, "");
        entityData.define(DATA_TOGGLES, "00000000");
        entityData.define(DATA_CARRIED, ItemStack.EMPTY);
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
        return level != null && level.isClientSide ? (entityData.get(DATA_FLAGS) & 1) != 0 : advanced;
    }

    public void setAdvancedGolem(boolean advanced) {
        this.advanced = advanced;
        this.originalUpgradeSlots = GolemOriginalRuntime.normalizeUpgradeSlots(originalUpgradeSlots, material, advanced);
        syncColorsLength();
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
        syncClientState();
    }

    public boolean isWaiting() {
        return level != null && level.isClientSide ? (entityData.get(DATA_FLAGS) & 2) != 0 : waiting;
    }

    public void setGolemProfile(GolemMaterial material, GolemCoreType coreType) {
        this.material = material == null ? GolemMaterial.WOOD : material;
        this.coreType = coreType == null ? GolemCoreType.GATHER : coreType;
        this.originalUpgradeSlots = GolemOriginalRuntime.normalizeUpgradeSlots(originalUpgradeSlots, this.material, advanced);
        syncColorsLength();
        applyProfileAttributes();
    }

    public GolemMaterial getGolemMaterial() {
        if (level.isClientSide) {
            int ordinal = entityData.get(DATA_MATERIAL);
            return ordinal >= 0 && ordinal < GolemMaterial.values().length
                    ? GolemMaterial.values()[ordinal] : GolemMaterial.WOOD;
        }
        return material;
    }

    public GolemCoreType getCoreType() {
        return level.isClientSide ? GolemCoreType.byOriginalId(entityData.get(DATA_CORE)) : coreType;
    }

    public void setCoreType(GolemCoreType coreType) {
        this.coreType = coreType == null ? GolemCoreType.GATHER : coreType;
        syncColorsLength();
        applyProfileAttributes();
    }

    public boolean addUpgrade(GolemUpgradeType upgrade) {
        if (!GolemOriginalRuntime.installUpgrade(originalUpgradeSlots, upgrade)) {
            return false;
        }
        syncColorsLength();
        applyProfileAttributes();
        return true;
    }

    public boolean hasUpgrade(GolemUpgradeType upgrade) {
        return getUpgradeAmount(upgrade) > 0;
    }

    public int getUpgradeAmount(GolemUpgradeType upgrade) {
        if (!level.isClientSide) {
            return GolemOriginalRuntime.upgradeAmount(originalUpgradeSlots, upgrade);
        }
        int count = 0;
        char wanted = Character.forDigit(upgrade.originalId(), 16);
        for (char value : entityData.get(DATA_UPGRADES).toCharArray()) {
            if (value == wanted) {
                count++;
            }
        }
        return count;
    }

    public int getUpgradeSlotCount() {
        return originalUpgradeSlots.length;
    }

    public int getOccupiedUpgradeSlotCount() {
        return GolemOriginalRuntime.occupiedUpgradeSlots(originalUpgradeSlots);
    }

    public byte getUpgradeSlot(int slot) {
        return slot >= 0 && slot < originalUpgradeSlots.length ? originalUpgradeSlots[slot] : (byte) -1;
    }

    public GolemUpgradeType removeUpgradeFromSlot(int slot) {
        GolemUpgradeType removed = GolemOriginalRuntime.removeUpgrade(originalUpgradeSlots, slot);
        if (removed != null) {
            syncColorsLength();
            applyProfileAttributes();
        }
        return removed;
    }

    public boolean addDecoration(GolemDecorationType decoration) {
        if (decoration == null || decorations.contains(decoration)) {
            return false;
        }
        // TC4 mutually exclusive decoration pairs.
        if ((decoration == GolemDecorationType.FEZ || decoration == GolemDecorationType.TOP_HAT)
                && (decorations.contains(GolemDecorationType.FEZ) || decorations.contains(GolemDecorationType.TOP_HAT))) {
            return false;
        }
        if ((decoration == GolemDecorationType.GLASSES || decoration == GolemDecorationType.VISOR)
                && (decorations.contains(GolemDecorationType.GLASSES) || decorations.contains(GolemDecorationType.VISOR))) {
            return false;
        }
        if ((decoration == GolemDecorationType.BOWTIE || decoration == GolemDecorationType.ARMOR)
                && (decorations.contains(GolemDecorationType.BOWTIE) || decorations.contains(GolemDecorationType.ARMOR))) {
            return false;
        }
        decorations.add(decoration);
        decorationCode = decorationCodeFromSet();
        applyProfileAttributes();
        return true;
    }

    public boolean hasDecoration(GolemDecorationType decoration) {
        if (!level.isClientSide) {
            return decorations.contains(decoration);
        }
        String code = entityData.get(DATA_DECORATIONS);
        return switch (decoration) {
            case TOP_HAT -> code.contains("H");
            case ARMOR -> code.contains("P");
            case BOWTIE -> code.contains("B");
            case MACE -> code.contains("M");
            case FEZ -> code.contains("F");
            case VISOR -> code.contains("V");
            case GLASSES -> code.contains("G");
            case DART_LAUNCHER -> code.contains("D") || code.contains("R");
            case WIRELESS_BACKPACK -> code.contains("W");
        };
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
        if (tag.contains(GolemOriginalRuntime.NBT_UPGRADES)) {
            originalUpgradeSlots = GolemOriginalRuntime.normalizeUpgradeSlots(
                    tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES), material, advanced);
        } else {
            originalUpgradeSlots = GolemOriginalRuntime.defaultUpgrades(material, advanced);
            for (String part : tag.getString(GolemCoreItem.TAG_UPGRADES).split(",")) {
                if (!part.isBlank()) {
                    GolemOriginalRuntime.installUpgrade(originalUpgradeSlots, GolemUpgradeType.byName(part));
                }
            }
        }
        decorations.clear();
        if (tag.contains(GolemOriginalRuntime.NBT_DECORATION)) {
            loadDecorationCode(tag.getString(GolemOriginalRuntime.NBT_DECORATION));
        }
        for (String part : tag.getString(GolemCoreItem.TAG_DECORATIONS).split(",")) {
            if (!part.isBlank()) {
                addDecoration(GolemDecorationType.byName(part));
            }
        }
        if (tag.contains(GolemCoreItem.TAG_FILTER)) {
            setFilter(ItemStack.of(tag.getCompound(GolemCoreItem.TAG_FILTER)), tag.getBoolean(GolemCoreItem.TAG_FILTER_ALLOW));
        }
        if (tag.contains(GolemOriginalRuntime.NBT_COLORS)) {
            colors = tag.getByteArray(GolemOriginalRuntime.NBT_COLORS);
        }
        if (tag.contains(GolemOriginalRuntime.NBT_TOGGLES)) {
            originalToggles = tag.getByteArray(GolemOriginalRuntime.NBT_TOGGLES);
            if (originalToggles.length < 8) {
                originalToggles = java.util.Arrays.copyOf(originalToggles, 8);
            }
        }
        if (tag.contains(GolemOriginalRuntime.NBT_MARKERS)) {
            applyOriginalMarkerList(tag.getList(GolemOriginalRuntime.NBT_MARKERS, 10));
        }
        if (tag.contains(GolemOriginalRuntime.NBT_ITEM_CARRIED)) {
            itemCarried = ItemStack.of(tag.getCompound(GolemOriginalRuntime.NBT_ITEM_CARRIED));
        }
        if (tag.contains("FluidCarried") && tag.contains("FluidCarriedAmount")) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("FluidCarried"));
            carriedFluidId = parsed;
            carriedFluidAmount = Math.max(0, tag.getInt("FluidCarriedAmount"));
            if (carriedFluidAmount <= 0) clearCarriedFluid();
        }
        carriedEssentia = Aspect.byId(tag.getString("EssentiaCarried"));
        carriedEssentiaAmount = Math.max(0, tag.getInt("EssentiaCarriedAmount"));
        if (carriedEssentiaAmount <= 0) carriedEssentia = null;
        fishingCooldown = tag.contains("FishingCooldown") ? Math.max(0, tag.getInt("FishingCooldown")) : 300;
        if (tag.contains(GolemOriginalRuntime.NBT_INVENTORY)) {
            ListTag stored = tag.getList(GolemOriginalRuntime.NBT_INVENTORY, 10);
            for (int i = 0; i < stored.size(); i++) {
                CompoundTag itemTag = stored.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < inventory.size()) {
                    inventory.set(slot, ItemStack.of(itemTag));
                }
            }
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
        double health = GolemOriginalRuntime.maxHealth(material, decorationCode);
        double speed = GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater());
        double armor = material.armorValue()
                + (decorations.contains(GolemDecorationType.VISOR) ? 1 : 0)
                + (decorations.contains(GolemDecorationType.ARMOR) ? 4 : 0);
        armor = Math.min(20.0D, armor);
        double attack = GolemOriginalRuntime.attackDamage(material, originalUpgradeSlots, decorationCode);

        applyAttribute(Attributes.MAX_HEALTH, Math.max(1.0D, health));
        applyAttribute(Attributes.MOVEMENT_SPEED, Math.max(0.01D, speed));
        applyAttribute(Attributes.ARMOR, Math.max(0.0D, armor));
        applyAttribute(Attributes.ATTACK_DAMAGE, Math.max(1.0D, attack));

        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        } else if (getHealth() <= 1.0F) {
            setHealth(getMaxHealth());
        }
        syncClientState();
    }

    private void syncClientState() {
        if (level == null || level.isClientSide) {
            return;
        }
        entityData.set(DATA_MATERIAL, material.ordinal());
        entityData.set(DATA_CORE, coreType.originalId());
        entityData.set(DATA_FLAGS, (advanced ? 1 : 0) | (waiting ? 2 : 0) | (pausedByGolemGui ? 4 : 0));
        entityData.set(DATA_DECORATIONS, decorationCodeFromSet());
        entityData.set(DATA_UPGRADES, GolemOriginalRuntime.upgradeSlotString(originalUpgradeSlots));
        entityData.set(DATA_COLORS, GolemOriginalRuntime.colorSlotString(colors));
        StringBuilder toggles = new StringBuilder();
        for (byte toggle : originalToggles) {
            toggles.append(toggle == 0 ? '0' : '1');
        }
        entityData.set(DATA_TOGGLES, toggles.toString());
        entityData.set(DATA_CARRIED, carriedDisplayStack());
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
        if (decorations.contains(GolemDecorationType.DART_LAUNCHER)) builder.append('R');
        if (decorations.contains(GolemDecorationType.WIRELESS_BACKPACK)) builder.append('W');
        return builder.toString();
    }

    private void loadDecorationCode(String code) {
        if (code == null) {
            return;
        }
        for (char value : code.toCharArray()) {
            GolemDecorationType type = switch (value) {
                case 'H' -> GolemDecorationType.TOP_HAT;
                case 'P' -> GolemDecorationType.ARMOR;
                case 'B' -> GolemDecorationType.BOWTIE;
                case 'M' -> GolemDecorationType.MACE;
                case 'F' -> GolemDecorationType.FEZ;
                case 'V' -> GolemDecorationType.VISOR;
                case 'G' -> GolemDecorationType.GLASSES;
                case 'D', 'R' -> GolemDecorationType.DART_LAUNCHER;
                case 'W' -> GolemDecorationType.WIRELESS_BACKPACK;
                default -> null;
            };
            if (type != null) {
                addDecoration(type);
            }
        }
        decorationCode = decorationCodeFromSet();
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

        // EntityGolemBase customInteraction: sugar heals five health and
        // grants 600 ticks of speed, with the original heart/eating feedback.
        if (held.is(Items.SUGAR) && getHealth() < getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            heal(5.0F);
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, getX(), getY() + 0.7D, getZ(), 3,
                        getBbWidth() * 0.4D, getBbHeight() * 0.25D, getBbWidth() * 0.4D, 0.02D);
            }
            level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EAT,
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.3F, 1.0F);
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
            if (addUpgrade(upgradeItem.getUpgradeType())) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F, 1.0F);
                player.displayClientMessage(Component.literal("Installed golem upgrade: ")
                        .append(upgradeItem.getUpgradeType().displayName())
                        .append(Component.literal(" (" + getUpgradeAmount(upgradeItem.getUpgradeType()) + "/2)")), false);
            } else {
                player.displayClientMessage(Component.literal("No free upgrade slot, or this upgrade is already installed twice.")
                        .withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof GolemDecorationItem decorationItem) {
            if (addDecoration(decorationItem.getDecorationType())) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE,
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.7F, 1.1F);
                player.displayClientMessage(Component.literal("Installed golem decoration: ")
                        .append(decorationItem.getDecorationType().displayName()), false);
            } else {
                player.displayClientMessage(Component.literal("That decoration conflicts with one already installed.")
                        .withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof TC4GolemCoreComponentItem coreItem) {
            if (coreItem.coreType() == GolemCoreType.BLANK) {
                return InteractionResult.PASS;
            }
            if (coreType != GolemCoreType.BLANK) {
                player.displayClientMessage(Component.literal("This golem already has a functional core.")
                        .withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }
            setCoreType(coreItem.coreType());
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.1F);
            player.displayClientMessage(Component.literal("Installed golem core: ").append(coreType.displayName()), false);
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
                .append(Component.literal(" | carry " + GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots)))
                .append(Component.literal(" | slots " + activeSlots()))
                .append(Component.literal(" | range " + workRange()))
                .append(Component.literal(" | priority " + taskPriority))
                .append(Component.literal(" | ai " + lastOriginalTask))
                .append(Component.literal(itemCarried.isEmpty() ? " | carried none" : " | carried " + itemCarried.getCount()))
                .append(Component.literal(" | upgrades " + GolemOriginalRuntime.upgradeDescription(originalUpgradeSlots)))
                .append(Component.literal(" | deco " + decorations.size()))
                .append(Component.literal(waiting ? " | waiting" : " | active"));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level.isClientSide) {
            return;
        }
        if (originalChestInteractTicks > 0) {
            originalChestInteractTicks--;
        }
        if (combatCooldown > 0) {
            combatCooldown--;
        }
        if (rangedAttackCooldown > 0) {
            rangedAttackCooldown--;
        }
        if (coreType == GolemCoreType.FISH && fishingCooldown > 0) {
            fishingCooldown--;
        }

        // EntityGolemBase regenerates one health after its material delay.
        if (regenerationTimer > 0) {
            regenerationTimer--;
        } else {
            regenerationTimer = GolemOriginalRuntime.regenDelay(material, decorationCode);
            if (getHealth() < getMaxHealth()) {
                heal(1.0F);
                level.broadcastEntityEvent(this, (byte) 5);
            }
        }

        // TC4 golem task adapters use a five-tick evaluation delay.
        if (tickCount % GolemTaskAIRuntime.ORIGINAL_GOLEM_DELAY_TICKS == 0) {
            syncClientState();
            updateTaskSchedulerLikeTC4();
            if (taskBackoffTicks > 0) {
                taskBackoffTicks = Math.max(0, taskBackoffTicks - GolemTaskAIRuntime.ORIGINAL_GOLEM_DELAY_TICKS);
            } else if (!waiting && !pausedByGolemGui) {
                runCoreBehavior();
                returnHomeLikeTC4();
            } else {
                getNavigation().stop();
            }
        }
        // Inventory slots in ContainerGolem are TC4 SlotGhost filters. They must
        // never be delivered, consumed, or treated as real golem storage.
    }

    private void updateTaskSchedulerLikeTC4() {
        Vec3 current = position();
        if (schedulerLastPos == Vec3.ZERO) {
            schedulerLastPos = current;
            return;
        }
        if (getNavigation().isDone() || waiting || pausedByGolemGui) {
            schedulerStuckTicks = 0;
            schedulerLastPos = current;
            return;
        }
        double moved = current.distanceToSqr(schedulerLastPos);
        if (moved < 0.0025D) {
            schedulerStuckTicks += GolemTaskAIRuntime.ORIGINAL_GOLEM_DELAY_TICKS;
        } else {
            schedulerStuckTicks = 0;
        }
        schedulerLastPos = current;
        if (schedulerStuckTicks == 40) {
            getNavigation().stop();
            taskBackoffTicks = Math.max(taskBackoffTicks, 10);
            lastOriginalTask = "scheduler:short-backoff";
        } else if (schedulerStuckTicks >= 100) {
            getNavigation().stop();
            taskBackoffTicks = Math.max(taskBackoffTicks, 20);
            lastOriginalTask = "scheduler:stuck-recovery";
        }
    }

    private void runCoreBehavior() {
        if (!GolemTaskAIRuntime.originalDelayReady(tickCount)) {
            return;
        }
        lastOriginalTask = GolemTaskAIRuntime.diagnostic(coreType);
        switch (coreType) {
            case BLANK -> getNavigation().stop();
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
        // ContainerGolem inventory is ghost filters. If the home cannot accept
        // the carried stack the original golem keeps holding it; it must not be
        // hidden inside the GUI filter slots.
        return runOriginalHomeDrop();
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
            int move = Math.min(stack.getCount(), Math.max(1, GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots)));
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
                int move = Math.min(stored.getCount(), Math.max(1, GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots)));
                ItemStack extracted = container.removeItem(slot, move);
                if (extracted.isEmpty()) {
                    continue;
                }
                itemCarried = extracted.copy();
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
                int move = Math.min(stored.getCount(), Math.min(Math.max(1, sample.getCount()), Math.max(1, GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots))));
                ItemStack extracted = container.removeItem(slot, move);
                if (extracted.isEmpty()) {
                    continue;
                }
                itemCarried = extracted.copy();
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
        int carryLimit = Math.max(1, GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots));
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
            if (!GolemBellMarkerRuntime.markerMatchesLevel(marker, level)
                    || !markerMatchesGolemColorLikeTC4(marker.color(), color)) {
                continue;
            }
            BlockPos markerPos = new BlockPos(marker.x(), marker.y(), marker.z());
            Direction side = Direction.from3DDataValue(marker.side() & 255);
            Container container = containerAt(markerPos, side);
            // v10.22 strict GolemHelper parity: markers point at the inventory
            // block itself. The marker side is an access side for sided inventories;
            // do not also treat markerPos.relative(side) as a second output chest.
            if (container != null) {
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
            Container neighbourContainer = containerAt(neighbourPos, side);
            if (neighbourContainer != null) {
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
        Container container = containerAt(pos, null);
        if (container != null) {
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
        // EntityGolemBase uses toggle 5/6/7 for ore dictionary, damage and
        // NBT matching. Forge item tags are the 1.19.2 replacement for the old
        // OreDictionary relation.
        boolean checkTags = originalToggleEnabled(5) && getUpgradeAmount(GolemUpgradeType.ENTROPY) > 0;
        boolean ignoreDamage = originalToggleEnabled(6) && getUpgradeAmount(GolemUpgradeType.ENTROPY) > 0;
        boolean ignoreNbt = originalToggleEnabled(7) && getUpgradeAmount(GolemUpgradeType.ENTROPY) > 0;
        boolean sameItem = stored.getItem() == sample.getItem();
        if (!sameItem && checkTags) {
            sameItem = ForgeRegistries.ITEMS.tags().getReverseTag(stored.getItem())
                    .map(reverse -> reverse.getTagKeys().anyMatch(sample::is))
                    .orElse(false);
        }
        if (!sameItem) {
            return false;
        }
        if (!ignoreDamage && (stored.isDamageableItem() || sample.isDamageableItem())
                && stored.getDamageValue() != sample.getDamageValue()) {
            return false;
        }
        return ignoreNbt || java.util.Objects.equals(stored.getTag(), sample.getTag());
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
        return containerAt(chestPos, Direction.from3DDataValue(homeFacing));
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
        return Math.max(1, (int) Math.ceil(GolemOriginalRuntime.workRange(
                originalUpgradeSlots, decorationCode, advanced)));
    }

    private void pickupNearbyItems() {
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
        ItemEntity target = items.get(0);
        if (distanceToSqr(target) > 2.25D) {
            getNavigation().moveTo(target, hasUpgrade(GolemUpgradeType.AIR) ? 1.25D : 1.1D);
            return;
        }
        ItemStack source = target.getItem();
        int move = Math.min(source.getCount(), GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots));
        itemCarried = source.copy();
        itemCarried.setCount(move);
        source.shrink(move);
        if (source.isEmpty()) {
            target.discard();
        } else {
            target.setItem(source);
        }
        syncClientState();
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
        int range = workRange();
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(center).inflate(range, 4.0D, range),
                candidate -> isValidGuardTarget(candidate, owner, center, range, serverLevel)
        );
        if (targets.isEmpty()) {
            setTarget(null);
            return;
        }

        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        LivingEntity target = targets.get(0);
        if (target instanceof Mob mob) {
            setTarget(mob);
        }
        double distanceSq = distanceToSqr(target);
        if (tryDartAttackLikeTC4(target, distanceSq)) {
            getNavigation().moveTo(target, GolemOriginalRuntime.movementSpeed(
                    material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
        } else if (distanceSq > 4.0D) {
            getNavigation().moveTo(target, GolemOriginalRuntime.movementSpeed(
                    material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
        } else {
            doHurtTarget(target);
        }
    }

    private boolean tryDartAttackLikeTC4(LivingEntity target, double distanceSq) {
        if (!hasDecoration(GolemDecorationType.DART_LAUNCHER) || distanceSq < 9.0D || !hasLineOfSight(target)) {
            return false;
        }
        float rangedLimit = workRange() * 0.8F;
        if (distanceSq > rangedLimit * rangedLimit) {
            return false;
        }
        if (rangedAttackCooldown > 0) {
            return true;
        }

        Arrow dart = new Arrow(level, this);
        dart.setPos(getX(), getEyeY() - 0.1D, getZ());
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getEyeY() - 0.7D - dart.getY();
        float inaccuracy = 7.0F - getUpgradeAmount(GolemUpgradeType.WATER) * 1.75F;
        dart.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F, Math.max(0.0F, inaccuracy));
        dart.setBaseDamage(getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4D);
        dart.pickup = AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(dart);
        level.playSound(null, blockPosition(), TC4Sounds.event("golemironshoot"),
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F,
                1.0F / (getRandom().nextFloat() * 0.4F + 0.6F));
        // EntityGolemBase#getAttackSpeed: 20 ticks, or 18 for advanced golems.
        rangedAttackCooldown = Math.max(1, 20 - (advanced ? 2 : 0));
        return true;
    }

    /**
     * Direct 1.19.2 adaptation of EntityGolemBase.isValidTarget. Guard toggles
     * intentionally use original slots 1..4; slot zero belongs to other cores.
     */
    private boolean isValidGuardTarget(LivingEntity target, Player owner, BlockPos center, int range, ServerLevel serverLevel) {
        if (!target.isAlive() || target == this || target instanceof ThaumGolemEntity) {
            return false;
        }
        if (target.distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) > range * range) {
            return false;
        }
        if (owner != null && target.getUUID().equals(owner.getUUID())) {
            return false;
        }

        int order = getUpgradeAmount(GolemUpgradeType.ORDER);
        if (target instanceof Creeper) {
            return order > 0 && getGolemToggle(4) == 0;
        }
        if (target instanceof Enemy) {
            return getGolemToggle(1) == 0;
        }
        if (target instanceof Animal animal) {
            if (order <= 0 || getGolemToggle(2) != 0 || animal.isBaby()) {
                return false;
            }
            return !(animal instanceof TamableAnimal tamable) || !tamable.isTame();
        }
        if (target instanceof Player player) {
            return order > 0
                    && getGolemToggle(3) == 0
                    && !player.isCreative()
                    && !player.isSpectator()
                    && serverLevel.getServer().isPvpAllowed();
        }
        return false;
    }

    private void butcherNearbyAnimals() {
        List<Animal> targets = level.getEntitiesOfClass(
                Animal.class,
                getBoundingBox().inflate(workRange(), 4.0D, workRange()),
                animal -> animal.isAlive()
                        && !animal.isBaby()
                        && (!(animal instanceof TamableAnimal tamable) || !tamable.isTame())
                        && distanceToSqr(animal) <= workRange() * workRange()
        );
        if (targets.isEmpty()) {
            setTarget(null);
            return;
        }
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        Animal target = targets.get(0);
        setTarget(target);
        double distanceSq = distanceToSqr(target);
        if (tryDartAttackLikeTC4(target, distanceSq)) {
            getNavigation().moveTo(target, GolemOriginalRuntime.movementSpeed(
                    material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
        } else if (distanceSq > 4.0D) {
            getNavigation().moveTo(target, GolemOriginalRuntime.movementSpeed(
                    material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
        } else {
            doHurtTarget(target);
        }
    }

    private void harvestNearbyCrops() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos center = workPos == null ? homePos : workPos;
        int range = Math.max(1, (int) Math.ceil(workRange() / 4.0D));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -3, -range), center.offset(range, 3, range))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) {
                continue;
            }
            if (distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) > 3.0D) {
                getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                        GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
                return;
            }
            if (!mayBreakAsOwner(serverLevel, pos)) {
                continue;
            }
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), this, ItemStack.EMPTY);
            boolean replanted = false;
            if (getUpgradeAmount(GolemUpgradeType.ORDER) > 0) {
                ItemStack seed = seedForCrop(state);
                if (!seed.isEmpty() && consumeOneMatchingDrop(drops, seed)) {
                    level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
                    replanted = true;
                }
            }
            if (!replanted) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            spawnDropsAt(pos, drops);
            level.levelEvent(2001, pos, Block.getId(state));
            lastOriginalTask = replanted ? "AIHarvestCrops:replant" : "AIHarvestCrops:harvest";
            return;
        }
    }

    private ItemStack seedForCrop(BlockState state) {
        if (state.is(Blocks.WHEAT)) return new ItemStack(Items.WHEAT_SEEDS);
        if (state.is(Blocks.CARROTS)) return new ItemStack(Items.CARROT);
        if (state.is(Blocks.POTATOES)) return new ItemStack(Items.POTATO);
        if (state.is(Blocks.BEETROOTS)) return new ItemStack(Items.BEETROOT_SEEDS);
        return ItemStack.EMPTY;
    }

    private boolean consumeOneMatchingDrop(List<ItemStack> drops, ItemStack sample) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (!drop.isEmpty() && ItemStack.isSameItemSameTags(drop, sample)) {
                drop.shrink(1);
                if (drop.isEmpty()) drops.remove(i);
                return true;
            }
        }
        return false;
    }

    private void lumberNearbyLogs() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos center = workPos == null ? homePos : workPos;
        int range = Math.max(1, (int) Math.ceil(workRange() / 3.0D));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 8, range))) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.LOGS)) {
                continue;
            }
            BlockPos target = findFurthestConnectedLogLikeTC4(pos, state.getBlock(), 128);
            if (distanceToSqr(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D) > 3.0D) {
                getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                        GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
                return;
            }
            if (!mayBreakAsOwner(serverLevel, target)) {
                continue;
            }
            BlockState targetState = level.getBlockState(target);
            List<ItemStack> drops = Block.getDrops(targetState, serverLevel, target, level.getBlockEntity(target), this, ItemStack.EMPTY);
            level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            spawnDropsAt(target, drops);
            level.levelEvent(2001, target, Block.getId(targetState));
            lastOriginalTask = "AIHarvestLogs:furthest";
            return;
        }
    }

    private BlockPos findFurthestConnectedLogLikeTC4(BlockPos start, Block block, int maxVisited) {
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start.immutable());
        BlockPos furthest = start.immutable();
        double furthestDistance = -1.0D;
        while (!queue.isEmpty() && visited.size() < maxVisited) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current) || level.getBlockState(current).getBlock() != block) continue;
            double distance = current.distSqr(start);
            if (distance > furthestDistance || (distance == furthestDistance && current.getY() > furthest.getY())) {
                furthestDistance = distance;
                furthest = current.immutable();
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next) && level.getBlockState(next).getBlock() == block) queue.addLast(next.immutable());
            }
        }
        return furthest;
    }

    private void fishAtWaterMarker() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos water = findFishingWater();
        if (water == null) {
            patrolBetweenMarkers();
            return;
        }
        if (distanceToSqr(water.getX() + 0.5D, water.getY() + 0.5D, water.getZ() + 0.5D) > 9.0D) {
            getNavigation().moveTo(water.getX() + 0.5D, water.getY() + 1.0D, water.getZ() + 0.5D,
                    GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
            return;
        }
        if (fishingCooldown > 0) return;
        int catches = 1;
        int air = getUpgradeAmount(GolemUpgradeType.AIR);
        if (air > 0 && random.nextInt(10) < air) catches++;
        for (int i = 0; i < catches; i++) {
            ItemStack catchStack = rollFishingCatchLikeTC4();
            if (getUpgradeAmount(GolemUpgradeType.FIRE) > 0) catchStack = cookFishingCatch(catchStack);
            ItemEntity entity = new ItemEntity(serverLevel, water.getX() + 0.5D, water.getY() + 1.0D, water.getZ() + 0.5D, catchStack);
            double dx = getX() - entity.getX();
            double dz = getZ() - entity.getZ();
            entity.setDeltaMovement(dx * 0.1D, 0.25D, dz * 0.1D);
            entity.setPickUpDelay(20);
            if (getUpgradeAmount(GolemUpgradeType.FIRE) > 0) entity.setSecondsOnFire(2);
            serverLevel.addFreshEntity(entity);
        }
        fishingCooldown = 300 + random.nextInt(200);
        level.broadcastEntityEvent(this, (byte) 7);
        lastOriginalTask = "AIFishing:catch";
    }

    private BlockPos findFishingWater() {
        BlockPos center = workPos == null ? homePos : workPos;
        int range = Math.max(1, (int) Math.ceil(workRange() / 2.0D));
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 2, range))) {
            if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource() || !level.getBlockState(pos.above()).isAir()) continue;
            double distance = distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private ItemStack rollFishingCatchLikeTC4() {
        int entropy = getUpgradeAmount(GolemUpgradeType.ENTROPY);
        int order = getUpgradeAmount(GolemUpgradeType.ORDER);
        float junkChance = Math.max(0.0F, 0.10F - entropy * 0.025F);
        float treasureChance = 0.05F + order * 0.0125F;
        float roll = random.nextFloat();
        if (roll < treasureChance) {
            ItemStack[] treasure = {new ItemStack(Items.NAME_TAG), new ItemStack(Items.SADDLE), new ItemStack(Items.NAUTILUS_SHELL), new ItemStack(Items.BOW), new ItemStack(Items.FISHING_ROD), new ItemStack(Items.ENCHANTED_BOOK)};
            return treasure[random.nextInt(treasure.length)].copy();
        }
        if (roll < treasureChance + junkChance) {
            ItemStack[] junk = {new ItemStack(Items.LEATHER_BOOTS), new ItemStack(Items.LEATHER), new ItemStack(Items.BONE), new ItemStack(Items.INK_SAC), new ItemStack(Items.STRING), new ItemStack(Items.BOWL), new ItemStack(Items.STICK), new ItemStack(Items.TRIPWIRE_HOOK), new ItemStack(Items.LILY_PAD), new ItemStack(Items.ROTTEN_FLESH)};
            return junk[random.nextInt(junk.length)].copy();
        }
        int fish = random.nextInt(100);
        if (fish < 60) return new ItemStack(Items.COD);
        if (fish < 85) return new ItemStack(Items.SALMON);
        if (fish < 87) return new ItemStack(Items.TROPICAL_FISH);
        return new ItemStack(Items.PUFFERFISH);
    }

    private ItemStack cookFishingCatch(ItemStack stack) {
        if (stack.is(Items.COD)) return new ItemStack(Items.COOKED_COD, stack.getCount());
        if (stack.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON, stack.getCount());
        return stack;
    }

    private void patrolBetweenMarkers() {
        BlockPos a = homePos;
        BlockPos b = workPos;
        if (a == null || a.equals(BlockPos.ZERO) || b == null) {
            returnHomeLikeTC4();
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
        if (!(level instanceof ServerLevel serverLevel) || ownerUuid == null) {
            return;
        }
        if (itemCarried.isEmpty()) {
            // Original AIUseItem reads only EntityGolemBase.itemCarried. The
            // ContainerGolem slots are SlotGhost filters and are never consumed.
            moveTowardHomeIfNeeded();
            return;
        }

        UseMarkerTarget target = findUseMarkerTargetLikeTC4();
        if (target == null) {
            patrolBetweenMarkers();
            return;
        }
        BlockPos targetPos = target.pos();
        if (distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D) > 4.0D) {
            getNavigation().moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                    GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
            return;
        }

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || !serverLevel.mayInteract(owner, targetPos)) {
            return;
        }
        performUseActionLikeTC4(serverLevel, owner, target);
    }

    /**
     * Direct marker-selection adaptation of AIUseItem.findSomething(). Toggle 0
     * chooses existing blocks versus empty spaces; Order-coloured ghost filters
     * restrict which bell markers can be used for the carried stack.
     */
    private UseMarkerTarget findUseMarkerTargetLikeTC4() {
        List<Integer> matchingColors = colorsMatchingStackLikeTC4(itemCarried);
        boolean requireEmpty = originalToggleEnabled(0);
        UseMarkerTarget closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (int color : matchingColors) {
            for (GolemBellMarkerRuntime.Marker marker : GolemBellMarkerRuntime.readMarkers(originalMarkers)) {
                if (!GolemBellMarkerRuntime.markerMatchesLevel(marker, level)) {
                    continue;
                }
                if (color != -1 && marker.color() != -1 && marker.color() != (byte) color) {
                    continue;
                }
                BlockPos pos = new BlockPos(marker.x(), marker.y(), marker.z());
                boolean empty = level.getBlockState(pos).isAir();
                if (empty != requireEmpty) {
                    continue;
                }
                Direction side = Direction.from3DDataValue(marker.side() & 255);
                if (!level.getBlockState(pos.relative(side.getOpposite())).isAir()) {
                    continue;
                }
                double distance = distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = new UseMarkerTarget(pos.immutable(), side, marker.color());
                }
            }
        }
        if (closest == null && workPos != null) {
            BlockState state = level.getBlockState(workPos);
            if (state.isAir() == requireEmpty) {
                closest = new UseMarkerTarget(workPos.immutable(), Direction.from3DDataValue(homeFacing), (byte) -1);
            }
        }
        return closest;
    }

    /**
     * Forge 1.19.2 counterpart of ItemInWorldManager used by TC4 AIUseItem. A
     * stable fake player with the owner's UUID makes claim/protection hooks see
     * the correct thaumaturge. Toggle 1 selects right/left click and toggle 2
     * controls sneaking. Any extra result stacks are dropped beside the golem,
     * while the mutated main-hand stack becomes itemCarried again.
     */
    private void performUseActionLikeTC4(ServerLevel serverLevel, ServerPlayer owner, UseMarkerTarget target) {
        GameProfile profile = new GameProfile(ownerUuid, "FakeThaumcraftGolem");
        FakePlayer fake = FakePlayerFactory.get(serverLevel, profile);
        fake.getInventory().clearContent();
        fake.setPos(getX(), getY(), getZ());
        fake.setYRot(getYRot());
        fake.setXRot(getXRot());
        fake.setShiftKeyDown(originalToggleEnabled(2));
        fake.setItemInHand(InteractionHand.MAIN_HAND, itemCarried.copy());

        BlockPos pos = target.pos();
        Direction side = target.side();
        Vec3 hitLocation = Vec3.atCenterOf(pos).add(
                side.getStepX() * 0.5D, side.getStepY() * 0.5D, side.getStepZ() * 0.5D);
        BlockHitResult hit = new BlockHitResult(hitLocation, side, pos, false);
        boolean acted = false;
        try {
            if (originalToggleEnabled(1)) {
                // Left-click mode mirrors ItemInWorldManager.tryHarvestBlock.
                if (!serverLevel.getBlockState(pos).isAir() && mayBreakAsOwner(serverLevel, pos)) {
                    acted = fake.gameMode.destroyBlock(pos);
                }
            } else {
                BlockState state = serverLevel.getBlockState(pos);
                InteractionResult result = state.use(serverLevel, pos, fake, InteractionHand.MAIN_HAND, hit);
                if (!result.consumesAction()) {
                    UseOnContext context = new UseOnContext(fake, InteractionHand.MAIN_HAND, hit);
                    result = fake.getMainHandItem().useOn(context);
                }
                acted = result.consumesAction();
            }
        } catch (RuntimeException ignored) {
            // A misbehaving third-party item/block must not corrupt the golem or
            // consume its ghost filters. The carried stack is restored below.
        }

        itemCarried = fake.getMainHandItem().copy();
        if (itemCarried.isEmpty()) {
            itemCarried = ItemStack.EMPTY;
        }
        for (int slot = 0; slot < fake.getInventory().getContainerSize(); slot++) {
            if (slot == fake.getInventory().selected) {
                continue;
            }
            ItemStack extra = fake.getInventory().getItem(slot);
            if (!extra.isEmpty()) {
                spawnAtLocation(extra.copy(), 0.2F);
                fake.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
        fake.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        fake.setShiftKeyDown(false);
        syncClientState();
        if (acted) {
            lastOriginalTask = originalToggleEnabled(1) ? "AIUseItem:left-click" : "AIUseItem:right-click";
        }
    }

    private record UseMarkerTarget(BlockPos pos, Direction side, byte color) {
    }

    private void handleLiquidCore() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int capacity = GolemOriginalRuntime.fluidCarryLimit(material, originalUpgradeSlots);
        if (carriedFluidAmount > 0 && carriedFluidId != null) {
            BlockPos out = outputPos == null ? homePos : outputPos;
            FluidTarget tankTarget = findNearbyFluidTarget(out, 2, carriedFluidId);
            BlockPos destination = tankTarget == null ? out : tankTarget.pos();
            if (destination != null && distanceToSqr(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D) > 5.0D) {
                getNavigation().moveTo(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D,
                        GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
                return;
            }
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(carriedFluidId);
            if (tankTarget != null && fluid != null && fluid != Fluids.EMPTY) {
                int accepted = tankTarget.handler().fill(new FluidStack(fluid, carriedFluidAmount), IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0) {
                    carriedFluidAmount -= accepted;
                    if (carriedFluidAmount <= 0) clearCarriedFluid();
                    syncClientState();
                    lastOriginalTask = "AILiquidEmpty:capability";
                    return;
                }
            }
            if (out != null && level.getBlockState(out).canBeReplaced() && carriedFluidAmount >= 1000
                    && (fluid == Fluids.WATER || fluid == Fluids.LAVA)) {
                level.setBlock(out, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
                carriedFluidAmount -= 1000;
                if (carriedFluidAmount <= 0) clearCarriedFluid();
                syncClientState();
                lastOriginalTask = "AILiquidEmpty:place";
            }
            return;
        }

        BlockPos inputCenter = inputPos == null ? (workPos == null ? homePos : workPos) : inputPos;
        FluidSource tankSource = findNearbyFluidSource(inputCenter, 2, capacity);
        if (tankSource != null) {
            if (distanceToSqr(tankSource.pos().getX() + 0.5D, tankSource.pos().getY(), tankSource.pos().getZ() + 0.5D) > 5.0D) {
                getNavigation().moveTo(tankSource.pos().getX() + 0.5D, tankSource.pos().getY(), tankSource.pos().getZ() + 0.5D,
                        GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
                return;
            }
            FluidStack drained = tankSource.handler().drain(tankSource.preview(), IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty() && drained.getAmount() > 0) {
                ResourceLocation id = ForgeRegistries.FLUIDS.getKey(drained.getFluid());
                if (id != null) {
                    carriedFluidId = id;
                    carriedFluidAmount = Math.min(capacity, drained.getAmount());
                    syncClientState();
                    lastOriginalTask = "AILiquidGather:capability";
                    return;
                }
            }
        }

        BlockPos source = findLiquidSource();
        if (source == null) {
            patrolBetweenMarkers();
            return;
        }
        if (distanceToSqr(source.getX() + 0.5D, source.getY(), source.getZ() + 0.5D) > 5.0D) {
            getNavigation().moveTo(source.getX() + 0.5D, source.getY(), source.getZ() + 0.5D,
                    GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
            return;
        }
        if (!mayBreakAsOwner(serverLevel, source)) return;
        Fluid fluid = level.getFluidState(source).getType();
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null || !matchesLiquidFilter(fluid)) return;
        int accepted = Math.min(1000, capacity - carriedFluidAmount);
        if (accepted < 1000) return;
        carriedFluidId = id;
        carriedFluidAmount += 1000;
        level.setBlock(source, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        syncClientState();
        lastOriginalTask = "AILiquidGather:source";
    }

    private BlockPos findLiquidSource() {
        BlockPos center = inputPos == null ? (workPos == null ? homePos : workPos) : inputPos;
        if (center == null) return null;
        int range = Math.max(1, Math.min(workRange(), 8));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -2, -range), center.offset(range, 2, range))) {
            if (level.getFluidState(pos).isSource() && matchesLiquidFilter(level.getFluidState(pos).getType())) return pos.immutable();
        }
        return null;
    }

    private boolean matchesLiquidFilter(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) return false;
        ItemStack filter = firstGhostFilter();
        if (filter.isEmpty()) return fluid == Fluids.WATER || fluid == Fluids.LAVA;
        LazyOptional<IFluidHandler> capability = filter.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        Fluid expected = capability.map(handler -> {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                FluidStack contained = handler.getFluidInTank(tank);
                if (!contained.isEmpty()) return contained.getFluid();
            }
            return Fluids.EMPTY;
        }).orElse(Fluids.EMPTY);
        if (expected != Fluids.EMPTY) return fluid == expected;
        if (filter.is(Items.WATER_BUCKET)) return fluid == Fluids.WATER;
        if (filter.is(Items.LAVA_BUCKET)) return fluid == Fluids.LAVA;
        return true;
    }

    private IFluidHandler fluidHandlerAt(BlockPos pos, Direction side) {
        if (pos == null || level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;
        LazyOptional<IFluidHandler> capability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side);
        if (!capability.isPresent() && side != null) {
            capability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null);
        }
        return capability.orElse(null);
    }

    private FluidTarget findNearbyFluidTarget(BlockPos center, int radius, ResourceLocation fluidId) {
        if (center == null || fluidId == null) return null;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            for (Direction side : Direction.values()) {
                IFluidHandler handler = fluidHandlerAt(pos, side);
                if (handler != null && handler.fill(new FluidStack(fluid, Math.max(1, carriedFluidAmount)), IFluidHandler.FluidAction.SIMULATE) > 0) {
                    return new FluidTarget(pos.immutable(), handler);
                }
            }
        }
        return null;
    }

    private FluidSource findNearbyFluidSource(BlockPos center, int radius, int capacity) {
        if (center == null || capacity <= 0) return null;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            for (Direction side : Direction.values()) {
                IFluidHandler handler = fluidHandlerAt(pos, side);
                if (handler == null) continue;
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    FluidStack stored = handler.getFluidInTank(tank);
                    if (stored.isEmpty() || !matchesLiquidFilter(stored.getFluid())) continue;
                    FluidStack request = stored.copy();
                    request.setAmount(Math.min(capacity, stored.getAmount()));
                    FluidStack preview = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
                    if (!preview.isEmpty() && preview.getAmount() > 0) {
                        return new FluidSource(pos.immutable(), handler, preview);
                    }
                }
            }
        }
        return null;
    }

    private record FluidTarget(BlockPos pos, IFluidHandler handler) {}
    private record FluidSource(BlockPos pos, IFluidHandler handler, FluidStack preview) {}

    private void clearCarriedFluid() {
        carriedFluidId = null;
        carriedFluidAmount = 0;
    }

    private void handleEssentiaCore() {
        BlockPos input = inputPos == null ? workPos : inputPos;
        BlockPos output = outputPos == null ? homePos : outputPos;
        int capacity = GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots);
        if (carriedEssentia != null && carriedEssentiaAmount > 0) {
            GolemEssentiaEndpoint target = findNearbyEssentiaEndpoint(output, 2, carriedEssentia, false);
            if (target == null) return;
            if (distanceToSqr(target.pos().getX() + 0.5D, target.pos().getY(), target.pos().getZ() + 0.5D) > 5.0D) {
                getNavigation().moveTo(target.pos().getX() + 0.5D, target.pos().getY(), target.pos().getZ() + 0.5D,
                        GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
                return;
            }
            int accepted = target.accept(carriedEssentia, carriedEssentiaAmount);
            if (accepted > 0) {
                carriedEssentiaAmount -= accepted;
                if (carriedEssentiaAmount <= 0) {
                    carriedEssentia = null;
                    carriedEssentiaAmount = 0;
                }
                syncClientState();
                lastOriginalTask = "AIEssentiaEmpty:" + target.kind();
            }
            return;
        }
        Aspect desired = firstGhostAspectFilter();
        GolemEssentiaEndpoint source = findNearbyEssentiaEndpoint(input, 2, desired, true);
        if (source == null) return;
        Aspect aspect = desired != null ? desired : source.firstAspect();
        if (aspect == null || source.amount(aspect) <= 0) return;
        if (distanceToSqr(source.pos().getX() + 0.5D, source.pos().getY(), source.pos().getZ() + 0.5D) > 5.0D) {
            getNavigation().moveTo(source.pos().getX() + 0.5D, source.pos().getY(), source.pos().getZ() + 0.5D,
                    GolemOriginalRuntime.movementSpeed(material, originalUpgradeSlots, decorationCode, advanced, isInWater()));
            return;
        }
        int move = source.remove(aspect, Math.min(capacity, source.amount(aspect)));
        if (move > 0) {
            carriedEssentia = aspect;
            carriedEssentiaAmount = move;
            syncClientState();
            lastOriginalTask = "AIEssentiaGather:" + source.kind();
        }
    }

    private GolemEssentiaEndpoint findNearbyEssentiaEndpoint(BlockPos center, int radius, Aspect desired, boolean requireContents) {
        if (center == null) return null;
        List<GolemEssentiaEndpoint> endpoints = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            GolemEssentiaEndpoint endpoint = essentiaEndpoint(blockEntity);
            if (endpoint == null) continue;
            if (requireContents) {
                if (!endpoint.canProvide()) continue;
                Aspect sourceAspect = desired != null ? desired : endpoint.firstAspect();
                if (sourceAspect == null || endpoint.amount(sourceAspect) <= 0) continue;
            } else if (!endpoint.canReceive() || desired == null || endpoint.simulateAccept(desired, 1) <= 0) {
                continue;
            }
            endpoints.add(endpoint);
        }
        endpoints.sort(Comparator.comparingDouble(endpoint -> distanceToSqr(
                endpoint.pos().getX() + 0.5D, endpoint.pos().getY(), endpoint.pos().getZ() + 0.5D)));
        return endpoints.isEmpty() ? null : endpoints.get(0);
    }

    private GolemEssentiaEndpoint essentiaEndpoint(BlockEntity blockEntity) {
        if (blockEntity instanceof EssentiaJarBlockEntity jar) return new JarEssentiaEndpoint(jar);
        if (blockEntity instanceof EssentiaReservoirBlockEntity reservoir) return new ReservoirEssentiaEndpoint(reservoir);
        if (blockEntity instanceof AlembicBlockEntity alembic) return new AlembicEssentiaEndpoint(alembic);
        if (blockEntity instanceof AlchemicalCentrifugeBlockEntity centrifuge) return new CentrifugeEssentiaEndpoint(centrifuge);
        if (blockEntity instanceof ThaumatoriumBlockEntity thaumatorium) return new ThaumatoriumEssentiaEndpoint(thaumatorium);
        return null;
    }

    private interface GolemEssentiaEndpoint {
        BlockPos pos();
        Aspect firstAspect();
        int amount(Aspect aspect);
        int accept(Aspect aspect, int amount);
        int simulateAccept(Aspect aspect, int amount);
        int remove(Aspect aspect, int amount);
        String kind();
        default boolean canProvide() { return true; }
        default boolean canReceive() { return true; }
    }

    private record JarEssentiaEndpoint(EssentiaJarBlockEntity jar) implements GolemEssentiaEndpoint {
        public BlockPos pos() { return jar.getBlockPos(); }
        public Aspect firstAspect() { return jar.storedAspect(); }
        public int amount(Aspect aspect) { return jar.storedAspect() == aspect ? jar.amount() : 0; }
        public int accept(Aspect aspect, int amount) { return jar.acceptFromTube(aspect, amount, false); }
        public int simulateAccept(Aspect aspect, int amount) {
            return jar.canAcceptAspect(aspect) ? Math.min(amount, Math.max(0, jar.capacity() - jar.amount())) : 0;
        }
        public int remove(Aspect aspect, int amount) {
            int move = Math.min(amount, jar.amount());
            return move > 0 && jar.takeFromContainerOriginal(aspect, move) ? move : 0;
        }
        public String kind() { return "jar"; }
    }

    private record ReservoirEssentiaEndpoint(EssentiaReservoirBlockEntity reservoir) implements GolemEssentiaEndpoint {
        public BlockPos pos() { return reservoir.getBlockPos(); }
        public Aspect firstAspect() { return reservoir.firstAspect(); }
        public int amount(Aspect aspect) { return reservoir.aspects().get(aspect); }
        public int accept(Aspect aspect, int amount) { return reservoir.acceptFromTube(aspect, amount); }
        public int simulateAccept(Aspect aspect, int amount) {
            return reservoir.canAcceptAspect(aspect) ? Math.min(amount, Math.max(0, EssentiaReservoirBlockEntity.CAPACITY - reservoir.amount())) : 0;
        }
        public int remove(Aspect aspect, int amount) { return reservoir.removeEssentia(aspect, amount); }
        public String kind() { return "reservoir"; }
    }

    private record AlembicEssentiaEndpoint(AlembicBlockEntity alembic) implements GolemEssentiaEndpoint {
        public BlockPos pos() { return alembic.getBlockPos(); }
        public Aspect firstAspect() { return alembic.aspects().firstAspect(); }
        public int amount(Aspect aspect) { return alembic.aspects().get(aspect); }
        public int accept(Aspect aspect, int amount) { return alembic.addEssentia(aspect, amount); }
        public int simulateAccept(Aspect aspect, int amount) {
            return alembic.canAccept(aspect) ? Math.min(amount, alembic.spaceLeft()) : 0;
        }
        public int remove(Aspect aspect, int amount) { return alembic.removeEssentia(aspect, amount); }
        public String kind() { return "alembic"; }
    }

    private record CentrifugeEssentiaEndpoint(AlchemicalCentrifugeBlockEntity centrifuge) implements GolemEssentiaEndpoint {
        public BlockPos pos() { return centrifuge.getBlockPos(); }
        public Aspect firstAspect() { return centrifuge.outputAspect(); }
        public int amount(Aspect aspect) { return centrifuge.outputAspect() == aspect ? 1 : 0; }
        public int accept(Aspect aspect, int amount) { return centrifuge.addInput(aspect, amount, Direction.DOWN); }
        public int simulateAccept(Aspect aspect, int amount) {
            return aspect != null && !aspect.isPrimal() && centrifuge.suctionAmount(Direction.DOWN) > 0 ? Math.min(1, amount) : 0;
        }
        public int remove(Aspect aspect, int amount) { return centrifuge.takeOutput(aspect, amount, Direction.UP); }
        public String kind() { return "centrifuge"; }
        public boolean canProvide() { return centrifuge.outputAspect() != null; }
        public boolean canReceive() { return centrifuge.suctionAmount(Direction.DOWN) > 0; }
    }

    private record ThaumatoriumEssentiaEndpoint(ThaumatoriumBlockEntity thaumatorium) implements GolemEssentiaEndpoint {
        public BlockPos pos() { return thaumatorium.getBlockPos(); }
        public Aspect firstAspect() { return thaumatorium.currentSuction(); }
        public int amount(Aspect aspect) { return 0; }
        public int accept(Aspect aspect, int amount) { return thaumatorium.acceptEssentiaFromGolem(aspect, amount); }
        public int simulateAccept(Aspect aspect, int amount) { return thaumatorium.simulateEssentiaAcceptance(aspect, amount); }
        public int remove(Aspect aspect, int amount) { return 0; }
        public String kind() { return "thaumatorium"; }
        public boolean canProvide() { return false; }
    }

    private ItemStack firstGhostFilter() {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    private Aspect firstGhostAspectFilter() {
        for (int i = 0; i < activeSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof EssentiaPhialItem) {
                Aspect aspect = EssentiaPhialItem.getAspect(stack);
                if (aspect != null) return aspect;
            }
        }
        return null;
    }

    private void emptyInputContainer() {
        if (!itemCarried.isEmpty()) return;
        BlockPos center = inputPos == null ? homePos : inputPos;
        Container container = findNearbyContainer(center, 2);
        if (container == null) {
            pickupNearbyItems();
            return;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && acceptsItem(stored)) {
                int move = Math.min(stored.getCount(), GolemOriginalRuntime.carryLimit(material, originalUpgradeSlots));
                itemCarried = stored.copy();
                itemCarried.setCount(move);
                stored.shrink(move);
                if (stored.isEmpty()) container.setItem(slot, ItemStack.EMPTY);
                container.setChanged();
                syncClientState();
                return;
            }
        }
    }

    private void fillOutputContainer() {
        if (!deliverInventoryToNearbyContainer()) {
            pickupNearbyItems();
        }
    }

    private boolean mayBreakAsOwner(ServerLevel serverLevel, BlockPos pos) {
        if (ownerUuid == null) return false;
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || !serverLevel.mayInteract(owner, pos) || !owner.mayUseItemAt(pos, Direction.UP, owner.getMainHandItem())) return false;
        return ForgeHooks.onBlockBreakEvent(serverLevel, owner.gameMode.getGameModeForPlayer(), owner, pos) != -1;
    }

    private void spawnDropsAt(BlockPos pos, List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) Block.popResource(level, pos, drop);
        }
    }

    private ItemStack carriedDisplayStack() {
        if (!itemCarried.isEmpty()) return itemCarried.copy();
        if (carriedEssentia != null && carriedEssentiaAmount > 0) {
            ItemStack phial = new ItemStack(ThaumcraftMod.ESSENTIA_PHIAL.get());
            EssentiaPhialItem.setEssentia(phial, carriedEssentia, Math.min(8, carriedEssentiaAmount));
            return phial;
        }
        if (carriedFluidId != null && carriedFluidAmount > 0) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(carriedFluidId);
            if (fluid == Fluids.WATER) return new ItemStack(Items.WATER_BUCKET);
            if (fluid == Fluids.LAVA) return new ItemStack(Items.LAVA_BUCKET);
            return new ItemStack(Items.BUCKET);
        }
        return ItemStack.EMPTY;
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
        int slots = GolemOriginalRuntime.inventorySlotCount(material, coreType, originalUpgradeSlots);
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
            Container container = containerAt(pos, null);
            if (container != null && deliverInventoryToContainer(container)) {
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

    private Container containerAt(BlockPos pos, Direction side) {
        if (pos == null || level == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        if (blockEntity instanceof Container container) {
            return container;
        }
        LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        if (!capability.isPresent() && side != null) {
            capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        }
        return capability.map(GolemItemHandlerContainerAdapter::new).orElse(null);
    }

    private Container findNearbyContainer(BlockPos center, int radius) {
        if (center == null) {
            return null;
        }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            Container container = containerAt(pos, null);
            if (container != null) {
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
        if (container instanceof GolemItemHandlerContainerAdapter adapter) {
            return adapter.insertIntoSlots(stack, candidateSlots, !commit);
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
                        container.setItem(slot, stored);
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
        if (container instanceof GolemItemHandlerContainerAdapter adapter) {
            return adapter.insert(stack, false);
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stored = container.getItem(slot);
            if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, stack)) {
                int space = Math.min(stored.getMaxStackSize(), container.getMaxStackSize()) - stored.getCount();
                if (space > 0) {
                    int move = Math.min(space, stack.getCount());
                    stored.grow(move);
                    container.setItem(slot, stored);
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

    private void returnHomeLikeTC4() {
        if (homePos == null || homePos.equals(BlockPos.ZERO)) {
            return;
        }
        double distance = distanceToSqr(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D);
        double range = GolemOriginalRuntime.workRange(originalUpgradeSlots, decorationCode, advanced);

        // Original AIReturnHome teleports a golem that is at least 48 blocks
        // from home or physically stuck, then otherwise walks back inside range.
        if (distance >= 2304.0D || schedulerStuckTicks >= 100) {
            for (int dy = 1; dy >= -1; dy--) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos candidate = homePos.offset(dx, dy, dz);
                        if (level.getBlockState(candidate.below()).isSolidRender(level, candidate.below())
                                && level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()
                                && level.getBlockState(candidate.above()).getCollisionShape(level, candidate.above()).isEmpty()) {
                            teleportTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
                            getNavigation().stop();
                            schedulerStuckTicks = 0;
                            schedulerLastPos = position();
                            return;
                        }
                    }
                }
            }
        } else if (distance > range * range) {
            getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /**
     * Recreates the exact de-metadata'd body item that represents this golem's
     * original TC4 ItemGolemPlacer metadata. The internal universal core item
     * remains only as a migration fallback for old development saves.
     */
    public ItemStack createBareGolemBodyStack() {
        return TC4ResearchItems.registered("tc4_golem_" + material.id())
                .map(entry -> new ItemStack(entry.get()))
                .orElseGet(() -> new ItemStack(ThaumcraftMod.GOLEM_CORE.get()));
    }

    public byte[] getOriginalUpgradeSlotsCopy() {
        return originalUpgradeSlots.clone();
    }

    public ItemStack createGolemPlacerStack() {
        ItemStack placer = createBareGolemBodyStack();
        CompoundTag tag = placer.getOrCreateTag();
        tag.putString(GolemCoreItem.TAG_MATERIAL, material.id());
        tag.putString(GolemCoreItem.TAG_CORE, coreType.id());
        tag.putByte(GolemOriginalRuntime.NBT_GOLEM_TYPE, (byte) material.ordinal());
        tag.putByte(GolemOriginalRuntime.NBT_CORE, (byte) coreType.originalId());
        tag.putByte("core", (byte) coreType.originalId());
        tag.putBoolean(GolemOriginalRuntime.NBT_ADVANCED, advanced);
        tag.putByte(GolemOriginalRuntime.NBT_HOME_FACING, (byte) homeFacing);
        tag.putByteArray(GolemOriginalRuntime.NBT_UPGRADES, originalUpgradeSlots.clone());
        tag.putByteArray("upgrades", originalUpgradeSlots.clone());
        tag.putString(GolemCoreItem.TAG_UPGRADES, GolemOriginalRuntime.upgradeDescription(originalUpgradeSlots));
        tag.putString(GolemOriginalRuntime.NBT_DECORATION, decorationCodeFromSet());
        tag.putString("deco", decorationCodeFromSet());
        tag.putString(GolemCoreItem.TAG_DECORATIONS, decorationsToString());
        tag.putByteArray(GolemOriginalRuntime.NBT_COLORS, colors.clone());
        tag.putByteArray(GolemOriginalRuntime.NBT_TOGGLES, originalToggles.clone());
        tag.put(GolemOriginalRuntime.NBT_MARKERS, originalMarkerListSnapshot());
        tag.put("markers", originalMarkerListSnapshot());
        tag.putBoolean(GolemCoreItem.TAG_FILTER_ALLOW, filterAllowList);
        if (!filterStack.isEmpty()) {
            CompoundTag filterTag = new CompoundTag();
            filterStack.save(filterTag);
            tag.put(GolemCoreItem.TAG_FILTER, filterTag);
        }
        // Original bell dismantling stores the golem inventory in the placer,
        // while the item currently carried in its hands is dropped separately.
        ListTag inventoryTag = new ListTag();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) slot);
                stack.save(itemTag);
                inventoryTag.add(itemTag);
            }
        }
        tag.put(GolemOriginalRuntime.NBT_INVENTORY, inventoryTag);
        if (hasCustomName()) {
            placer.setHoverName(getCustomName());
        }
        return placer;
    }

    public void dropCarriedStackAfterDismantle() {
        if (!itemCarried.isEmpty()) {
            spawnAtLocation(itemCarried.copy());
            itemCarried = ItemStack.EMPTY;
        }
        // Fluid and essentia are internal carried resources in TC4; dismantling
        // clears them instead of corrupting the ghost filter inventory.
        clearCarriedFluid();
        carriedEssentia = null;
        carriedEssentiaAmount = 0;
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
        if (carriedFluidId != null && carriedFluidAmount > 0) {
            tag.putString("FluidCarried", carriedFluidId.toString());
            tag.putInt("FluidCarriedAmount", carriedFluidAmount);
        }
        if (carriedEssentia != null && carriedEssentiaAmount > 0) {
            tag.putString("EssentiaCarried", carriedEssentia.id());
            tag.putInt("EssentiaCarriedAmount", carriedEssentiaAmount);
        }
        tag.putInt("FishingCooldown", fishingCooldown);
        writeNullablePos(tag, "Input", inputPos);
        writeNullablePos(tag, "Output", outputPos);
        writeNullablePos(tag, "Guard", guardPos);
        writeNullablePos(tag, "Work", workPos);
        tag.putString("GolemUpgrades", GolemOriginalRuntime.upgradeDescription(originalUpgradeSlots));
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
        loadDecorationCode(decorationCode);
        waiting = tag.getBoolean("Waiting");
        taskRadius = tag.contains("TaskRadius") ? Math.max(1, Math.min(32, tag.getInt("TaskRadius"))) : 8;
        taskPriority = tag.contains("TaskPriority") ? Math.max(0, Math.min(9, tag.getInt("TaskPriority"))) : 0;
        homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        inputPos = readNullablePos(tag, "Input");
        outputPos = readNullablePos(tag, "Output");
        guardPos = readNullablePos(tag, "Guard");
        workPos = readNullablePos(tag, "Work");
        if (tag.contains(GolemOriginalRuntime.NBT_UPGRADES)) {
            originalUpgradeSlots = GolemOriginalRuntime.normalizeUpgradeSlots(
                    tag.getByteArray(GolemOriginalRuntime.NBT_UPGRADES), material, advanced);
        } else {
            originalUpgradeSlots = GolemOriginalRuntime.defaultUpgrades(material, advanced);
            for (String part : tag.getString("GolemUpgrades").split(",")) {
                if (!part.isBlank()) {
                    String id = part.contains("x") ? part.substring(0, part.indexOf('x')) : part;
                    int count = 1;
                    if (part.contains("x")) {
                        try {
                            count = Math.max(1, Integer.parseInt(part.substring(part.indexOf('x') + 1)));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    for (int i = 0; i < count; i++) {
                        GolemOriginalRuntime.installUpgrade(originalUpgradeSlots, GolemUpgradeType.byName(id));
                    }
                }
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
        if (tag.contains("FluidCarried") && tag.contains("FluidCarriedAmount")) {
            ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("FluidCarried"));
            carriedFluidId = parsed;
            carriedFluidAmount = Math.max(0, tag.getInt("FluidCarriedAmount"));
            if (carriedFluidAmount <= 0) clearCarriedFluid();
        }
        carriedEssentia = Aspect.byId(tag.getString("EssentiaCarried"));
        carriedEssentiaAmount = Math.max(0, tag.getInt("EssentiaCarriedAmount"));
        if (carriedEssentiaAmount <= 0) carriedEssentia = null;
        fishingCooldown = tag.contains("FishingCooldown") ? Math.max(0, tag.getInt("FishingCooldown")) : 300;
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
        syncClientState();
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
            if (!GolemBellMarkerRuntime.markerMatchesLevel(marker, level)) {
                continue;
            }
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
        if (level.isClientSide) {
            String packed = entityData.get(DATA_COLORS);
            if (slot < 0 || slot >= packed.length()) {
                return -1;
            }
            char value = packed.charAt(slot);
            return value == 'h' ? -1 : Character.digit(value, 16);
        }
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
        syncClientState();
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
        if (level.isClientSide) {
            String packed = entityData.get(DATA_TOGGLES);
            return index >= 0 && index < packed.length() && packed.charAt(index) == '1' ? (byte) 1 : 0;
        }
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
        syncClientState();
    }

    public void setPausedByGolemGui(boolean pausedByGolemGui) {
        this.pausedByGolemGui = pausedByGolemGui;
        if (pausedByGolemGui) {
            getNavigation().stop();
        }
        syncClientState();
    }

    public boolean isPausedByGolemGui() {
        return level != null && level.isClientSide ? (entityData.get(DATA_FLAGS) & 4) != 0 : pausedByGolemGui;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (combatCooldown > 0) {
            return false;
        }
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            combatCooldown = Math.max(1, 20 - (advanced ? 2 : 0));
            int fireSeconds = GolemOriginalRuntime.attackFireSeconds(originalUpgradeSlots);
            if (fireSeconds > 0) {
                target.setSecondsOnFire(fireSeconds);
            }
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (material == GolemMaterial.THAUMIUM && source.isMagic()) {
            amount *= 0.5F;
        }
        Entity attacker = source.getEntity();
        if (!level.isClientSide && attacker instanceof LivingEntity living && attacker != this) {
            float retaliation = GolemOriginalRuntime.entropyRetaliationDamage(originalUpgradeSlots, random);
            if (retaliation > 0.0F) {
                living.hurt(DamageSource.thorns(this), retaliation);
                level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.THORNS_HIT,
                        net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
        }
        waiting = false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return material.fireResist() || super.fireImmune();
    }

    public ItemStack getCarriedForDisplay() {
        return level.isClientSide ? entityData.get(DATA_CARRIED) : carriedDisplayStack();
    }

    @Override
    public Component getName() {
        if (hasCustomName() && getCustomName() != null) {
            return getCustomName();
        }
        GolemMaterial displayMaterial = getGolemMaterial();
        GolemCoreType displayCore = getCoreType();
        return Component.literal("Thaumic Golem [" + displayMaterial.id() + " / " + displayCore.id() + "]");
    }
}
