package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.porting.TC4LegacyDuplicateItemMigrator;
import com.darkifov.thaumcraft.porting.TC4LegacyStackMigrationTarget;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionInstabilityEvents;
import com.darkifov.thaumcraft.infusion.InfusionMatrixAuxiliaryHelper;
import com.darkifov.thaumcraft.infusion.InfusionProcessHelper;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.infusion.InfusionStructureReport;
import com.darkifov.thaumcraft.infusion.MatrixAuxiliaryReport;
import com.darkifov.thaumcraft.infusion.TC4InfusionRuntime;
import com.darkifov.thaumcraft.infusion.TC4InfusionShortageInstabilityParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionCraftCycleParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionStabilityParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionFailureParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionEnchantmentAdapter;
import com.darkifov.thaumcraft.infusion.TC4InfusionRunicAugmentAdapter;
import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InfusionMatrixBlockEntity extends BlockEntity implements TC4LegacyStackMigrationTarget {
    private boolean active = false;
    private boolean crafting = false;
    private boolean checkSurroundings = true;
    private int count = 0;
    private int craftCount = 0;
    private int symmetry = 0;
    private int progress = 0;
    private int duration = 0;
    private int countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
    private int itemCount = 0;
    private int currentInstability = 0;
    private int recipeInstability = 0;
    private int recipeXP = 0;
    private int recipeType = 0;
    private float renderStartUp = 0.0F;
    private ResourceLocation recipeId = null;
    private ResourceLocation travellingComponent = null;
    private BlockPos travellingComponentSource = null;
    private ItemStack travellingComponentSnapshot = ItemStack.EMPTY;
    private int travellingComponentIndex = -1;
    private ResourceLocation lockedCatalystId = null;
    private ItemStack lockedCatalystSnapshot = ItemStack.EMPTY;
    private String lastStabilizerSignature = "";
    private int lastSymmetryPenalty = 0;
    private int lastStabilizerPairs = 0;
    private int lastUnpairedStabilizers = 0;
    private String lastFailureReason = "";
    private int lastFailureInstability = 0;
    private ResourceLocation lastFailureTravellingComponent = null;
    private BlockPos lastFailureTravellingSource = null;
    private ItemStack lastFailureTravellingSnapshot = ItemStack.EMPTY;
    private UUID ownerId = null;
    private String ownerName = "";
    private final EnumMap<Aspect, Integer> pendingAspects = new EnumMap<>(Aspect.class);
    private final List<ResourceLocation> pendingComponents = new ArrayList<>();
    private final List<InfusionRecipe.ComponentSpec> pendingComponentSpecs = new ArrayList<>();
    /** TileInfusionMatrix.pedestals: refreshed only by getSurroundings/checkSurroundings. */
    private final List<BlockPos> cachedPedestalPositions = new ArrayList<>();
    /** TC4 parity: locked snapshot of original recipe aspect types (names only, not amounts).
     * The original AspectList kept zero-value keys around after visSize hit 0, so
     * component/XP shortage could randomly pick an aspect from the full original set.
     * This list preserves that ability without changing isEmpty() semantics everywhere. */
    private final List<Aspect> lockedRecipeAspectTypes = new ArrayList<>();

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean active() {
        return active;
    }

    public boolean crafting() {
        return crafting;
    }

    public int craftCount() {
        return craftCount;
    }

    public int currentInstability() {
        return currentInstability;
    }

    public int recipeXP() {
        return recipeXP;
    }

    public int recipeType() {
        return recipeType;
    }

    /** Client-ticked TC4 startUp value; renderers must never mutate block-entity state. */
    public float renderStartUp() {
        return renderStartUp;
    }

    public int symmetry() {
        return symmetry;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public ResourceLocation activeRecipeId() {
        return recipeId;
    }

    public ResourceLocation lockedCatalystId() {
        return lockedCatalystId;
    }

    public int pendingEssentiaAmount() {
        return TC4InfusionRuntime.totalPendingEssentia(pendingAspects);
    }

    public int pendingComponentAmount() {
        return pendingComponents.size();
    }

    public Component statusComponent() {
        if (!active) {
            return Component.literal("Infusion Matrix idle.").withStyle(ChatFormatting.GRAY);
        }

        if (!crafting) {
            return Component.literal("Infusion Matrix active. Use a wand again to start TC4 crafting.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        int pct = duration <= 0 ? 0 : Math.min(99, progress * 100 / duration);
        int pendingEssentia = TC4InfusionRuntime.totalPendingEssentia(pendingAspects);

        return Component.literal("Infusion active: " + pct + "% | Recipe: " + recipeId + " | Essentia left: " + pendingEssentia + " | Components left: " + pendingComponents.size() + " | Instability: " + currentInstability + " | Owner: " + ownerName)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    /** Called only by the exact WandManager-style 3x3x3 conversion path. */
    public void activateFromMultiblock() {
        if (level == null || level.isClientSide) return;
        active = true;
        crafting = false;
        checkSurroundings = true;
        count = 0;
        craftCount = 0;
        countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
        itemCount = 0;
        setChangedAndSync();
    }

    public boolean onWandRightClick(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (!active && crafting) {
            return reactivatePausedInfusion(player);
        }

        if (active && !crafting) {
            return startInfusion(player);
        }

        if (!active) {
            return activateMatrix(player);
        }

        return false;
    }

    private InfusionStructureReport refreshSurroundings(ArcanePedestalBlockEntity catalystPedestal) {
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);
        cachedPedestalPositions.clear();
        cachedPedestalPositions.addAll(report.pedestalPositions());
        symmetry = report.originalSymmetryPenalty();
        checkSurroundings = false;
        return report;
    }

    private InfusionStructureReport surroundingsSnapshot(ArcanePedestalBlockEntity catalystPedestal) {
        return InfusionAltarStructure.snapshot(level, worldPosition, catalystPedestal,
                cachedPedestalPositions, symmetry);
    }

    private boolean reactivatePausedInfusion(Player player) {
        if (level == null || level.isClientSide || !crafting) {
            return false;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);
        if (!report.strictTc4Location()) {
            return false;
        }

        active = true;
        checkSurroundings = true;
        setChangedAndSync();
        return true;
    }

    private boolean activateMatrix(Player player) {
        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);

        if (!report.strictTc4Location()) {
            return false;
        }

        active = true;
        crafting = false;
        cachedPedestalPositions.clear();
        cachedPedestalPositions.addAll(report.pedestalPositions());
        checkSurroundings = false;
        symmetry = report.originalSymmetryPenalty();
        progress = 0;
        duration = 0;
        currentInstability = 0;
        recipeInstability = 0;
        recipeXP = 0;
        recipeType = 0;
        recipeId = null;
        travellingComponent = null;
        travellingComponentSource = null;
        travellingComponentSnapshot = ItemStack.EMPTY;
        travellingComponentIndex = -1;
        lockedCatalystId = null;
        lockedCatalystSnapshot = ItemStack.EMPTY;
        lastStabilizerSignature = "";
        lastSymmetryPenalty = 0;
        lastStabilizerPairs = 0;
        lastUnpairedStabilizers = 0;
        lastFailureReason = "";
        lastFailureInstability = 0;
        lastFailureTravellingComponent = null;
        lastFailureTravellingSource = null;
        lastFailureTravellingSnapshot = ItemStack.EMPTY;
        pendingAspects.clear();
        pendingComponents.clear();
        pendingComponentSpecs.clear();
        lockedRecipeAspectTypes.clear();

        setChangedAndSync();
        return true;
    }

    public boolean startInfusion(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (!active) {
            return activateMatrix(player);
        }

        if (crafting) {
            return false;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        // Original craftingStart begins with an explicit getSurroundings call.
        InfusionStructureReport report = refreshSurroundings(catalystPedestal);

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            return false;
        }

        if (!report.strictTc4Location()) {
            deactivateMatrix();
            return false;
        }

        if (!report.valid()) {
            return false;
        }

        InfusionRecipe recipe = findMatchingOriginalInfusionRecipe(catalystPedestal.stored(), report.componentPedestals());

        if (recipe == null) {
            return false;
        }

        String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research());
        if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
            return false;
        }

        if (!InfusionProcessHelper.hasComponents(report.componentPedestals(), recipe, catalystPedestal.stored())) {
            return false;
        }

        EnumMap<Aspect, Integer> requiredAspects = recipe.isInfusionEnchantment()
                ? TC4InfusionEnchantmentAdapter.scaledAspects(recipe, catalystPedestal.stored())
                : recipe.aspectCostFor(catalystPedestal.stored());

        // TC4 craftingStart locks the recipe and begins immediately; it does not
        // require the full essentia bill to be present beforehand. craftCycle then
        // waits and retries one aspect at a time, allowing jars/tubes/golems to feed
        // a running altar. The old preflight check incorrectly blocked that behavior.
        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        active = true;
        crafting = true;
        checkSurroundings = false;
        progress = 0;
        countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
        itemCount = 0;
        // Stage210 audit marker: recipe.componentsFor(catalystPedestal.stored())
        List<ResourceLocation> requiredComponents = TC4InfusionRuntime.orderedComponentPullList(recipe, catalystPedestal.stored());
        List<InfusionRecipe.ComponentSpec> requiredComponentSpecs = TC4InfusionRuntime.orderedComponentSpecList(recipe, catalystPedestal.stored());
        duration = TC4InfusionRuntime.estimateDuration(recipe, report, requiredAspects, requiredComponents, recipe.instabilityFor(catalystPedestal.stored()));
        symmetry = report.originalSymmetryPenalty();
        recipeType = recipe.recipeType();
        recipeXP = recipe.isInfusionEnchantment() ? TC4InfusionEnchantmentAdapter.calcXp(recipe, catalystPedestal.stored()) : 0;
        recipeInstability = recipe.isInfusionEnchantment() ? TC4InfusionEnchantmentAdapter.calcInstability(recipe, catalystPedestal.stored()) : recipe.instabilityFor(catalystPedestal.stored());
        // getSurroundings has already folded pedestal and stabilizer symmetry into
        // symmetry. Applying auxiliary stabilizers again double-counted them.
        currentInstability = TC4InfusionRuntime.initialInstability(symmetry, recipeInstability);
        rememberInfusionStabilitySnapshot(report, auxiliary);
        recipeId = recipe.id();
        travellingComponent = null;
        travellingComponentSource = null;
        travellingComponentSnapshot = ItemStack.EMPTY;
        travellingComponentIndex = -1;
        lockedCatalystId = itemIdFor(catalystPedestal.stored());
        lockedCatalystSnapshot = catalystPedestal.stored().copy();
        ownerId = player.getUUID();
        ownerName = player.getName().getString();
        pendingAspects.clear();
        pendingComponents.clear();
        pendingComponentSpecs.clear();

        for (Map.Entry<Aspect, Integer> entry : requiredAspects.entrySet()) {
            if (entry.getValue() > 0) {
                pendingAspects.put(entry.getKey(), entry.getValue());
            }
        }

        pendingComponents.addAll(requiredComponents);
        pendingComponentSpecs.addAll(requiredComponentSpecs);

        lockedRecipeAspectTypes.clear();
        lockedRecipeAspectTypes.addAll(requiredAspects.keySet());

        if (TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES) {
            player.displayClientMessage(
                    Component.literal("Infusion started: ")
                            .append(catalystPedestal.stored().getHoverName())
                            .append(Component.literal(" | TC4 craftCycle | Essentia: " + TC4InfusionRuntime.totalPendingEssentia(pendingAspects) + " | Components: " + pendingComponents.size() + " | XP: " + recipeXP + " | Symmetry: " + symmetry + " | Instability: " + currentInstability).withStyle(ChatFormatting.LIGHT_PURPLE)),
                    false
            );
        }

        level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_MATRIX_START),
                SoundSource.BLOCKS, 0.5F, 1.0F);

        setChangedAndSync();
        return true;
    }

    /**
     * Stage153 strict TC4 parity: the original 1.7.10 infusion registry can
     * contain multiple recipes with the same catalyst ItemStack. The selected
     * recipe is determined by the catalyst plus the surrounding component
     * pedestals, not by the catalyst alone. Keeping the old catalyst-only lookup
     * caused newly materialized original recipes such as CoreLumber/CoreFishing
     * and fortress/void robe upgrades to resolve to the wrong recipe.
     */
    private InfusionRecipe findMatchingOriginalInfusionRecipe(ItemStack catalyst, List<ArcanePedestalBlockEntity> componentPedestals) {
        for (InfusionRecipe recipe : InfusionRecipes.recipes()) {
            if (!recipe.catalystMatches(catalyst)) {
                continue;
            }

            boolean componentsMatch = recipe.isRunicAugmentRecipe()
                    ? InfusionProcessHelper.hasComponents(componentPedestals, recipe, catalyst)
                    : InfusionProcessHelper.hasComponents(componentPedestals, recipe);

            if (componentsMatch) {
                return recipe;
            }
        }

        // Stage342 GitHub hotfix: do not fall back to catalyst-only lookup.
        // Original TC4 infusion selection requires the component pedestals too.
        return null;
    }

    private boolean componentsHaveStartedTravelling(InfusionRecipe recipe, ItemStack catalyst) {
        return pendingComponents.size() < TC4InfusionRuntime.orderedComponentPullList(recipe, catalyst).size();
    }

    private boolean lockedRecipeStillMatchesCurrentPedestals(InfusionRecipe recipe, ItemStack catalyst, List<ArcanePedestalBlockEntity> componentPedestals) {
        if (!lockedCatalystStackStillMatches(recipe, catalyst)) {
            return false;
        }
        return findMatchingOriginalInfusionRecipe(catalyst, componentPedestals) == recipe;
    }

    private boolean lockedCatalystStackStillMatches(InfusionRecipe recipe, ItemStack currentCatalyst) {
        if (currentCatalyst == null || currentCatalyst.isEmpty()) {
            return false;
        }
        ResourceLocation currentCatalystId = itemIdFor(currentCatalyst);
        if (lockedCatalystId != null && !lockedCatalystId.equals(currentCatalystId)) {
            return false;
        }
        if (lockedCatalystSnapshot.isEmpty()) {
            return recipe != null && recipe.catalystMatches(currentCatalyst);
        }
        return TC4InfusionRuntime.sameCraftingCatalyst(currentCatalyst, lockedCatalystSnapshot);
    }

    private ResourceLocation itemIdFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    public void cancelInfusion(Player player) {
        if (!active && !crafting) {
            player.displayClientMessage(Component.literal("No active infusion.").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        deactivateMatrix();

        player.displayClientMessage(Component.literal("Infusion cancelled. Already drained TC4 essentia/components are not restored.").withStyle(ChatFormatting.YELLOW), false);
        setChangedAndSync();
    }

    private void tickServer() {
        if (level == null) {
            return;
        }

        count++;
        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        boolean rescanned = checkSurroundings;
        // TileInfusionMatrix caches pedestal coordinates. Between explicit
        // getSurroundings calls newly placed pedestals are not considered.
        InfusionStructureReport report = rescanned
                ? refreshSurroundings(catalystPedestal)
                : surroundingsSnapshot(catalystPedestal);

        if (!active) {
            return;
        }

        int validityInterval = crafting
                ? TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_CRAFTING_INTERVAL
                : TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_IDLE_INTERVAL;
        if (count % validityInterval == 0 && !report.strictTc4Location()) {
            pauseForInvalidStructure();
            setChangedAndSync();
            return;
        }

        if (!crafting) {
            return;
        }

        InfusionRecipe recipe = recipeId == null ? null : InfusionRecipes.findById(recipeId);
        if (recipe == null) {
            clearCraftingState(true);
            setChangedAndSync();
            return;
        }

        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        Player owner = getOwner();
        // craftCount, infuser loop sounds, runes and startup tween are strictly
        // client-side in TC4 doEffects(). No non-original accelerator changes
        // the production craftCycle cadence.
        progress++;

        // Exact TileInfusionMatrix scheduler: craftCycle runs only when the
        // global tile count is divisible by countDelay (normally 10, XP stage 20).
        if (!TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(count, countDelay)) {
            return;
        }

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()
                || !lockedCatalystStackStillMatches(recipe, catalystPedestal.stored())) {
            // TileInfusionMatrix#craftCycle enters the weighted 21-slot table
            // unconditionally for an invalid catalyst. It does not roll the
            // normal nextInt(500) instability gate a second time.
            InfusionInstabilityEvents.triggerWeightedEvent(level, worldPosition, owner, recipe, report,
                    currentInstability);
            cancelAfterInvalidCatalyst(owner, catalystPedestal == null || catalystPedestal.stored().isEmpty()
                    ? "Catalyst was removed during infusion." : "Catalyst changed during infusion.");
            return;
        }

        // A valid catalyst still rolls the original weighted instability table
        // before XP, essentia or component work. A triggered event consumes the cycle.
        if (currentInstability > 0
                && InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report,
                auxiliary.effectiveStabilizers(), currentInstability)) {
            setChangedAndSync();
            return;
        }

        if (recipeType == 1 && recipeXP == 0) {
            countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
        }
        if (drainEnchantmentXp(recipe, owner)) {
            setChangedAndSync();
            return;
        }
        if (drainNextEssentia(recipe, owner)) {
            setChangedAndSync();
            return;
        }
        if (pullNextComponent(recipe, report, owner)) {
            setChangedAndSync();
            return;
        }
        if (pendingAspects.isEmpty() && pendingComponents.isEmpty()) {
            finishInfusion(owner, recipe, catalystPedestal, report);
        }
        setChangedAndSync();
    }

    private void rememberInfusionStabilitySnapshot(InfusionStructureReport report, MatrixAuxiliaryReport auxiliary) {
        lastStabilizerSignature = auxiliary.stabilizerSignature() == null ? "" : auxiliary.stabilizerSignature();
        lastSymmetryPenalty = report == null ? 0 : report.originalSymmetryPenalty();
        lastStabilizerPairs = auxiliary.symmetricStabilizers();
        lastUnpairedStabilizers = auxiliary.unpairedStabilizers();
    }

    private boolean drainEnchantmentXp(InfusionRecipe recipe, Player owner) {
        if (!recipe.isInfusionEnchantment() || recipeXP <= 0) {
            return false;
        }

        List<Player> targets = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(10.0D, 10.0D, 10.0D));
        for (Player target : targets) {
            if (target.experienceLevel > 0) {
                target.giveExperienceLevels(-1);
                target.hurt(DamageSource.MAGIC, level.random.nextInt(2));
                recipeXP -= 1;
                countDelay = TC4InfusionAltarFullClosureParity.ENCHANTMENT_XP_CYCLE_INTERVAL;
                level.playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 2.0F + level.random.nextFloat() * 0.4F);
                if (level instanceof ServerLevel serverLevel) {
                    ThaumcraftNetwork.sendInfusionSourceFromEntity(serverLevel, worldPosition, target.getId());
                    InfusionProcessHelper.spawnParticleBeam(serverLevel,
                            target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                            worldPosition.getX() + 0.5D, worldPosition.getY() + 0.7D, worldPosition.getZ() + 0.5D,
                            ParticleTypes.ENCHANT, 18);
                }
                return true;
            }
        }

        if (!lockedRecipeAspectTypes.isEmpty() && level.random.nextInt(TC4InfusionShortageInstabilityParity.XP_REFUND_GATE_BOUND) == 0) {
            Aspect extra = lockedRecipeAspectTypes.get(level.random.nextInt(lockedRecipeAspectTypes.size()));
            if (extra != null) {
                pendingAspects.merge(extra, 1, Integer::sum);
            }
            if (level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipeInstability)) == 0) {
                currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
            }
        }

        countDelay = TC4InfusionAltarFullClosureParity.ENCHANTMENT_XP_CYCLE_INTERVAL;
        return true;
    }

    private boolean drainNextEssentia(InfusionRecipe recipe, Player owner) {
        if (pendingAspects.isEmpty()) {
            return false;
        }

        // Stage11.63.64 TC4 parity: TileInfusionMatrix.craftCycle tries every
        // pending aspect in the same tick (not just the first one) and only
        // stops as soon as one aspect actually drains from a jar, reservoir,
        // alembic, tube or device. Each aspect that fails to drain still rolls
        // its own independent instability-creep chance before the next aspect
        // is attempted.
        for (Aspect aspect : new ArrayList<>(pendingAspects.keySet())) {
            int pending = pendingAspects.getOrDefault(aspect, 0);

            if (pending <= 0) {
                continue;
            }

            BlockPos essentiaSource = InfusionProcessHelper.consumeOneAspectSource(level, worldPosition, aspect);

            if (essentiaSource != null) {
                int left = pending - 1;

                if (left <= 0) {
                    pendingAspects.remove(aspect);
                } else {
                    pendingAspects.put(aspect, left);
                }

                if (level instanceof ServerLevel serverLevel) {
                    InfusionProcessHelper.spawnSourceParticles(serverLevel, essentiaSource, worldPosition, false);
                }

                return true;
            }

            if (level.random.nextInt(TC4InfusionRuntime.failedEssentiaRollBound(recipeInstability)) == 0) {
                currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
            }
        }

        // Original TC4 forces an immediate surroundings re-scan once every
        // pending aspect has failed to drain this cycle (TileInfusionMatrix
        // line 447: this.checkSurroundings = true;). Component shortage does
        // not do this -- only essentia shortage does.
        checkSurroundings = true;
        return true;
    }

    private boolean pullNextComponent(InfusionRecipe recipe, InfusionStructureReport report, Player owner) {
        if (pendingComponents.isEmpty()) {
            return false;
        }

        // Exact TC4 itemCount lifecycle. The matrix does not lock a source
        // pedestal or stack for the full travel animation. Every craftCycle it
        // searches the current pedestal list again; after five later cycles it
        // consumes whichever matching pedestal is found on that cycle.
        for (int i = 0; i < pendingComponents.size(); i++) {
            ResourceLocation componentId = pendingComponents.get(i);
            InfusionRecipe.ComponentSpec componentSpec = pendingComponentSpecAt(i, componentId);
            ArcanePedestalBlockEntity pedestal = InfusionProcessHelper.findComponentPedestal(
                    report.componentPedestals(), componentSpec, recipe, worldPosition);

            if (pedestal != null) {
                if (itemCount == 0) {
                    itemCount = TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES;
                    travellingComponent = componentId;
                    travellingComponentSource = pedestal.getBlockPos();
                    travellingComponentSnapshot = pedestal.stored().copy();
                    travellingComponentSnapshot.setCount(1);
                    travellingComponentIndex = i;
                    if (level instanceof ServerLevel serverLevel) {
                        InfusionProcessHelper.spawnSourceParticles(serverLevel, pedestal.getBlockPos(), worldPosition, true);
                    }
                    return true;
                }

                if (itemCount-- <= 1) {
                    InfusionProcessHelper.consumePedestalComponentPreservingContainer(level, pedestal);
                    pendingComponents.remove(i);
                    removePendingComponentSpecAt(i);
                    itemCount = 0;
                    travellingComponent = null;
                    travellingComponentSource = null;
                    travellingComponentSnapshot = ItemStack.EMPTY;
                    travellingComponentIndex = -1;
                }
                return true;
            }

            // Original shortage gate is rolled once per unmatched ingredient
            // index, and the instability roll is nested inside that gate.
            if (!lockedRecipeAspectTypes.isEmpty()
                    && level.random.nextInt(TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(i)) == 0) {
                Aspect extra = lockedRecipeAspectTypes.get(level.random.nextInt(lockedRecipeAspectTypes.size()));
                if (extra != null) {
                    pendingAspects.merge(extra, 1, Integer::sum);
                }
                if (level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipeInstability)) == 0) {
                    currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
                }
            }
        }
        return true;
    }

    private InfusionRecipe.ComponentSpec pendingComponentSpecAt(int index, ResourceLocation fallbackId) {
        if (index >= 0 && index < pendingComponentSpecs.size()) {
            InfusionRecipe.ComponentSpec spec = pendingComponentSpecs.get(index);
            if (spec != null && spec.itemId() != null) {
                return spec;
            }
        }
        return new InfusionRecipe.ComponentSpec(fallbackId, com.darkifov.thaumcraft.infusion.TC4InfusionItemMatcher.ANY_DAMAGE, null);
    }

    private void removePendingComponentSpecAt(int index) {
        if (index >= 0 && index < pendingComponentSpecs.size()) {
            pendingComponentSpecs.remove(index);
        }
    }

    private void finishInfusion(Player owner, InfusionRecipe recipe, ArcanePedestalBlockEntity catalystPedestal, InfusionStructureReport report) {
        if (level == null) {
            return;
        }

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            failInfusion(owner, "Catalyst pedestal is missing.");
            return;
        }

        if (!report.valid()) {
            failInfusion(owner, "Infusion structure became invalid.");
            return;
        }

        ItemStack result = recipe.result();

        if (recipe.isInfusionEnchantment()) {
            if (!TC4InfusionEnchantmentAdapter.applyOutput(recipe, catalystPedestal.stored())) {
                failInfusion(owner, "Enchantment output is missing or incompatible.");
                return;
            }
            result = catalystPedestal.stored().copy();
            catalystPedestal.setStoredFromInfusion(result);
        } else if (recipe.isRunicAugmentRecipe()) {
            if (!TC4InfusionRunicAugmentAdapter.applyOutput(catalystPedestal.stored())) {
                failInfusion(owner, "Runic augmentation output is missing or incompatible.");
                return;
            }
            result = catalystPedestal.stored().copy();
            catalystPedestal.setStoredFromInfusion(result);
        } else if (recipe.hasNbtOutput()) {
            ItemStack central = catalystPedestal.stored().copy();
            central.addTagElement(recipe.outputNbtLabel(), recipe.outputNbt());
            result = central;
            catalystPedestal.setStoredFromInfusion(result);
        } else {
            if (result.isEmpty()) {
                failInfusion(owner, "Result item is missing.");
                return;
            }
            catalystPedestal.setStoredFromInfusion(result);
        }

        catalystPedestal.sendOriginalBlockEvent(12);

        if (TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES && owner != null) {
            owner.displayClientMessage(Component.literal("Infusion complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        }

        clearCraftingState(true);
    }

    private void cancelAfterInvalidCatalyst(Player owner, String reason) {
        lastFailureReason = reason == null ? "" : reason;
        lastFailureInstability = Math.max(0, currentInstability);
        lastFailureTravellingComponent = travellingComponent;
        lastFailureTravellingSource = travellingComponentSource;
        lastFailureTravellingSnapshot = travellingComponentSnapshot.isEmpty()
                ? ItemStack.EMPTY : travellingComponentSnapshot.copy();

        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_FAIL),
                    SoundSource.BLOCKS, 0.9F, 0.9F);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D,
                    70, 1.2D, 0.4D, 1.2D, 0.06D);
        }
        if (owner != null && TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES) {
            owner.displayClientMessage(Component.literal("Infusion cancelled: " + reason)
                    .withStyle(ChatFormatting.RED), false);
        }

        // No terminal failure helper and no unconditional Warp: the single
        // weighted instability event above is the entire original consequence.
        clearCraftingState(true, false);
    }

    private void failInfusion(Player owner, String reason) {
        lastFailureReason = reason == null ? "" : reason;
        lastFailureInstability = Math.max(0, currentInstability);
        lastFailureTravellingComponent = travellingComponent;
        lastFailureTravellingSource = travellingComponentSource;
        lastFailureTravellingSnapshot = travellingComponentSnapshot.isEmpty()
                ? ItemStack.EMPTY : travellingComponentSnapshot.copy();

        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_FAIL),
                    SoundSource.BLOCKS, 0.9F, 0.9F);
        }
        if (owner != null && TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES) {
            owner.displayClientMessage(Component.literal("Infusion cancelled: " + reason)
                    .withStyle(ChatFormatting.RED), false);
        }
        // Original TileInfusionMatrix has no generic terminal-failure Warp path.
        clearCraftingState(true, false);
    }

    private void pauseForInvalidStructure() {
        // Original TileInfusionMatrix.updateEntity only cleared active when
        // validLocation failed.  Keep the locked recipe and all partially drained
        // state so rebuilding the altar and using a wand resumes the same craft.
        active = false;
        checkSurroundings = true;
    }

    private void deactivateMatrix() {
        active = false;
        clearCraftingState(false);
    }

    private void clearCraftingState(boolean keepActive) {
        clearCraftingState(keepActive, true);
    }

    private void clearCraftingState(boolean keepActive, boolean clearLastFailure) {
        active = keepActive && active;
        crafting = false;
        checkSurroundings = true;
        progress = 0;
        duration = 0;
        countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
        itemCount = 0;
        currentInstability = 0;
        recipeInstability = 0;
        recipeXP = 0;
        recipeType = 0;
        recipeId = null;
        travellingComponent = null;
        travellingComponentSource = null;
        travellingComponentSnapshot = ItemStack.EMPTY;
        travellingComponentIndex = -1;
        lockedCatalystId = null;
        lockedCatalystSnapshot = ItemStack.EMPTY;
        lastStabilizerSignature = "";
        lastSymmetryPenalty = 0;
        lastStabilizerPairs = 0;
        lastUnpairedStabilizers = 0;
        if (clearLastFailure) {
            lastFailureReason = "";
            lastFailureInstability = 0;
            lastFailureTravellingComponent = null;
            lastFailureTravellingSource = null;
            lastFailureTravellingSnapshot = ItemStack.EMPTY;
        }
        pendingAspects.clear();
        pendingComponents.clear();
        pendingComponentSpecs.clear();
        lockedRecipeAspectTypes.clear();
    }

    private Player getOwner() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (ownerId != null) {
            Player byId = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            if (byId != null) return byId;
        }
        return ownerName == null || ownerName.isBlank()
                ? null
                : serverLevel.getServer().getPlayerList().getPlayerByName(ownerName);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        matrix.tickServer();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        matrix.count++;
        if (matrix.crafting) {
            if (matrix.craftCount == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TC4Sounds.event("infuserstart"),
                        SoundSource.BLOCKS, 0.5F, 1.0F, false);
            } else if (matrix.craftCount % 65 == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TC4Sounds.event("infuser"),
                        SoundSource.BLOCKS, 0.5F, 1.0F, false);
            }
            matrix.craftCount++;
            double x = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.9D;
            double y = pos.getY() - 1.45D + level.random.nextDouble() * 0.35D;
            double z = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.9D;
            level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0D, -0.03D, 0.0D);
        } else if (matrix.craftCount > 0) {
            matrix.craftCount -= 2;
            if (matrix.craftCount < 0) matrix.craftCount = 0;
            if (matrix.craftCount > 50) matrix.craftCount = 50;
        }

        if (matrix.active && matrix.renderStartUp != 1.0F) {
            if (matrix.renderStartUp < 1.0F) {
                matrix.renderStartUp += Math.max(matrix.renderStartUp / 10.0F, 0.001F);
            }
            if (matrix.renderStartUp > 0.999F) matrix.renderStartUp = 1.0F;
        }
        if (!matrix.active && matrix.renderStartUp > 0.0F) {
            matrix.renderStartUp -= matrix.renderStartUp / 10.0F;
            if (matrix.renderStartUp < 0.001F) matrix.renderStartUp = 0.0F;
        }

        if (matrix.crafting && matrix.currentInstability > 0
                && level.random.nextInt(200) <= matrix.currentInstability) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D),
                    pos.getY() + 0.5D + (level.random.nextDouble() - 0.5D),
                    pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D),
                    0.0D, 0.0D, 0.0D);
        }
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int migrateLegacyStacks() {
        int changed = 0;
        TC4LegacyDuplicateItemMigrator.MigrationResult travelling =
                TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus(travellingComponentSnapshot);
        if (travelling.changed()) {
            travellingComponentSnapshot = travelling.stack();
            changed += travelling.changedStacks();
        }
        TC4LegacyDuplicateItemMigrator.MigrationResult catalyst =
                TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus(lockedCatalystSnapshot);
        if (catalyst.changed()) {
            lockedCatalystSnapshot = catalyst.stack();
            changed += catalyst.changedStacks();
        }
        TC4LegacyDuplicateItemMigrator.MigrationResult failure =
                TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus(lastFailureTravellingSnapshot);
        if (failure.changed()) {
            lastFailureTravellingSnapshot = failure.stack();
            changed += failure.changedStacks();
        }
        if (changed > 0) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return changed;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 1.0D, worldPosition.getY() - 1.0D, worldPosition.getZ() - 1.0D,
                worldPosition.getX() + 1.0D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 1.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // Canonical TC4 TileInfusionMatrix persistence. Runtime counters,
        // cached surroundings, source FX and diagnostics are deliberately
        // transient, exactly as in the original tile.
        tag.putBoolean("active", active);
        tag.putBoolean("crafting", crafting);
        tag.putShort("instability", (short) currentInstability);

        ListTag aspects = new ListTag();
        List<Aspect> aspectTypes = lockedRecipeAspectTypes.isEmpty()
                ? new ArrayList<>(pendingAspects.keySet())
                : lockedRecipeAspectTypes;
        for (Aspect aspect : aspectTypes) {
            if (aspect == null) {
                continue;
            }
            CompoundTag aspectTag = new CompoundTag();
            aspectTag.putString("key", aspect.id());
            // AspectList keeps zero-valued keys after draining; this is used by
            // the original component/XP shortage refund path after reload.
            aspectTag.putInt("amount", Math.max(0, pendingAspects.getOrDefault(aspect, 0)));
            aspects.add(aspectTag);
        }
        tag.put("Aspects", aspects);

        ListTag recipeInputs = new ListTag();
        for (int i = 0; i < pendingComponents.size(); i++) {
            ResourceLocation componentId = pendingComponents.get(i);
            InfusionRecipe.ComponentSpec spec = pendingComponentSpecAt(i, componentId);
            ItemStack ingredient = stackForComponentSpec(spec);
            if (ingredient.isEmpty()) {
                continue;
            }
            CompoundTag ingredientTag = ingredient.save(new CompoundTag());
            ingredientTag.putByte("item", (byte) i);
            recipeInputs.add(ingredientTag);
        }
        tag.put("recipein", recipeInputs);

        InfusionRecipe recipe = recipeId == null ? null : InfusionRecipes.findById(recipeId);
        if (recipe != null) {
            if (recipe.hasNbtOutput()) {
                tag.putString("rotype", recipe.outputNbtLabel());
                tag.put("recipeout", recipe.outputNbt());
            } else {
                ItemStack output = recipe.isInfusionEnchantment() || recipe.isRunicAugmentRecipe()
                        ? lockedCatalystSnapshot.copy()
                        : recipe.result();
                if (!output.isEmpty()) {
                    tag.putString("rotype", "@");
                    tag.put("recipeout", output.save(new CompoundTag()));
                }
            }
        }

        if (!lockedCatalystSnapshot.isEmpty()) {
            tag.put("recipeinput", lockedCatalystSnapshot.save(new CompoundTag()));
        }
        tag.putInt("recipeinst", recipeInstability);
        tag.putInt("recipetype", recipeType);
        tag.putInt("recipexp", recipeXP);
        tag.putString("recipeplayer", ownerName == null ? "" : ownerName);

        // A registry id has no TC4 equivalent, but is the minimum modern bridge
        // required to reconstruct the registered 1.19.2 recipe object.
        if (recipeId != null) {
            tag.putString("recipeid", recipeId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.contains("Active") ? tag.getBoolean("Active") : tag.getBoolean("active");
        crafting = tag.contains("Crafting") ? tag.getBoolean("Crafting") : tag.getBoolean("crafting");
        boolean clientPacket = level != null && level.isClientSide;
        if (!clientPacket) {
            // Original field initializers after chunk/world reload.
            checkSurroundings = true;
            count = 0;
            craftCount = 0;
            symmetry = 0;
            progress = 0;
            duration = 0;
        }
        currentInstability = tag.contains("CurrentInstability") ? tag.getInt("CurrentInstability") : tag.getShort("instability");
        recipeInstability = tag.contains("RecipeInstability") ? tag.getInt("RecipeInstability") : tag.getInt("recipeinst");
        recipeXP = tag.contains("RecipeXP") ? tag.getInt("RecipeXP") : tag.getInt("recipexp");
        recipeType = tag.contains("RecipeType") ? tag.getInt("RecipeType") : tag.getInt("recipetype");
        countDelay = TC4InfusionAltarFullClosureParity.CRAFT_CYCLE_INTERVAL;
        itemCount = 0;
        String savedRecipeId = tag.contains("recipeid") ? tag.getString("recipeid") : tag.getString("RecipeId");
        recipeId = savedRecipeId == null || savedRecipeId.isBlank() ? null : new ResourceLocation(savedRecipeId);

        // itemCount/source-FX are transient in TC4. Old port-only fields are
        // intentionally discarded during one-time migration.
        travellingComponent = null;
        travellingComponentSource = null;
        travellingComponentSnapshot = ItemStack.EMPTY;
        travellingComponentIndex = -1;

        if (tag.contains("recipeinput")) {
            lockedCatalystSnapshot = ItemStack.of(tag.getCompound("recipeinput"));
        } else if (tag.contains("LockedCatalystSnapshot")) {
            lockedCatalystSnapshot = ItemStack.of(tag.getCompound("LockedCatalystSnapshot"));
        } else {
            lockedCatalystSnapshot = ItemStack.EMPTY;
        }
        String catalystId = tag.contains("LockedCatalystId") ? tag.getString("LockedCatalystId") : "";
        lockedCatalystId = catalystId == null || catalystId.isBlank()
                ? itemIdFor(lockedCatalystSnapshot)
                : new ResourceLocation(catalystId);

        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : tag.getString("recipeplayer");

        // Read-only migration of former diagnostic fields. New saves no longer
        // persist these non-original values.
        lastStabilizerSignature = tag.getString(TC4InfusionStabilityParity.NBT_RECIPE_STABILIZERS);
        lastSymmetryPenalty = tag.getInt(TC4InfusionStabilityParity.NBT_RECIPE_SYMMETRY);
        lastStabilizerPairs = tag.getInt(TC4InfusionStabilityParity.NBT_RECIPE_STABILIZER_PAIRS);
        lastUnpairedStabilizers = tag.getInt(TC4InfusionStabilityParity.NBT_RECIPE_UNPAIRED_STABILIZERS);
        lastFailureReason = tag.getString(TC4InfusionFailureParity.NBT_LAST_FAILURE_REASON);
        lastFailureInstability = tag.getInt(TC4InfusionFailureParity.NBT_LAST_FAILURE_INSTABILITY);
        String failedComponent = tag.getString("recipefailurecomponent");
        lastFailureTravellingComponent = failedComponent == null || failedComponent.isBlank() ? null : new ResourceLocation(failedComponent);
        if (tag.contains("recipefailureSourceX") && tag.contains("recipefailureSourceY") && tag.contains("recipefailureSourceZ")) {
            lastFailureTravellingSource = new BlockPos(tag.getInt("recipefailureSourceX"), tag.getInt("recipefailureSourceY"), tag.getInt("recipefailureSourceZ"));
        } else {
            lastFailureTravellingSource = null;
        }
        lastFailureTravellingSnapshot = tag.contains("recipefailureSourceStack")
                ? ItemStack.of(tag.getCompound("recipefailureSourceStack"))
                : ItemStack.EMPTY;

        pendingAspects.clear();
        pendingComponents.clear();
        pendingComponentSpecs.clear();
        lockedRecipeAspectTypes.clear();

        if (tag.contains("Aspects")) {
            ListTag aspectList = tag.getList("Aspects", 10);
            for (int i = 0; i < aspectList.size(); i++) {
                CompoundTag aspectTag = aspectList.getCompound(i);
                Aspect aspect = Aspect.byId(aspectTag.getString("key"));
                if (aspect == null) {
                    continue;
                }
                if (!lockedRecipeAspectTypes.contains(aspect)) {
                    lockedRecipeAspectTypes.add(aspect);
                }
                int amount = aspectTag.getInt("amount");
                if (amount > 0) {
                    pendingAspects.put(aspect, amount);
                }
            }
        } else if (tag.contains("PendingAspects")) {
            // Migration from pre-v11.64.34 port saves.
            CompoundTag oldAspects = tag.getCompound("PendingAspects");
            for (Aspect aspect : Aspect.values()) {
                int amount = oldAspects.getInt(aspect.name());
                if (amount > 0) {
                    pendingAspects.put(aspect, amount);
                    lockedRecipeAspectTypes.add(aspect);
                }
            }
        }

        if (tag.contains("recipein")) {
            ListTag recipeInputs = tag.getList("recipein", 10);
            for (int i = 0; i < recipeInputs.size(); i++) {
                ItemStack ingredient = ItemStack.of(recipeInputs.getCompound(i));
                ResourceLocation id = itemIdFor(ingredient);
                if (ingredient.isEmpty() || id == null) {
                    continue;
                }
                pendingComponents.add(id);
                pendingComponentSpecs.add(new InfusionRecipe.ComponentSpec(
                        id,
                        ingredient.hasTag() && ingredient.getTag().contains("Damage")
                                ? ingredient.getDamageValue()
                                : com.darkifov.thaumcraft.infusion.TC4InfusionItemMatcher.ANY_DAMAGE,
                        ingredient.hasTag() ? ingredient.getTag().copy() : null));
            }
        } else {
            String componentString = tag.getString("PendingComponents");
            if (componentString.isEmpty()) {
                componentString = tag.getString("recipeingredients");
            }
            if (!componentString.isEmpty()) {
                for (String part : componentString.split("\\|")) {
                    if (part.isEmpty()) {
                        continue;
                    }
                    try {
                        pendingComponents.add(new ResourceLocation(part));
                    } catch (Exception ignored) {
                    }
                }
            }
            restorePendingComponentSpecs(tag);
        }

        if (lockedRecipeAspectTypes.isEmpty() && tag.contains("LockedRecipeAspectTypes")) {
            ListTag oldLockedAspects = tag.getList("LockedRecipeAspectTypes", 10);
            for (int i = 0; i < oldLockedAspects.size(); i++) {
                Aspect aspect = Aspect.byId(oldLockedAspects.getCompound(i).getString("aspect"));
                if (aspect != null && !lockedRecipeAspectTypes.contains(aspect)) {
                    lockedRecipeAspectTypes.add(aspect);
                }
            }
        }
    }

    private ItemStack stackForComponentSpec(InfusionRecipe.ComponentSpec spec) {
        if (spec == null || spec.itemId() == null) {
            return ItemStack.EMPTY;
        }
        net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(spec.itemId());
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (spec.damage() >= 0) {
            stack.setDamageValue(spec.damage());
        }
        if (spec.tag() != null && !spec.tag().isEmpty()) {
            stack.setTag(spec.tag().copy());
        }
        return stack;
    }

    private void restorePendingComponentSpecs(CompoundTag tag) {
        pendingComponentSpecs.clear();
        if (tag.contains("PendingComponentSpecList")) {
            ListTag specs = tag.getList("PendingComponentSpecList", 10);
            for (int i = 0; i < specs.size(); i++) {
                CompoundTag specTag = specs.getCompound(i);
                try {
                    ResourceLocation id = new ResourceLocation(specTag.getString("id"));
                    int damage = specTag.contains("damage") ? specTag.getInt("damage") : com.darkifov.thaumcraft.infusion.TC4InfusionItemMatcher.ANY_DAMAGE;
                    CompoundTag nbt = specTag.contains("nbt") ? specTag.getCompound("nbt") : null;
                    pendingComponentSpecs.add(new InfusionRecipe.ComponentSpec(id, damage, nbt));
                } catch (Exception ignored) {
                }
            }
        } else {
            String serialized = tag.getString("PendingComponentSpecs");
            if (serialized != null && !serialized.isBlank()) {
                for (String part : serialized.split("\\|")) {
                    String[] pieces = part.split("@", 3);
                    if (pieces.length == 0 || pieces[0].isBlank()) {
                        continue;
                    }
                    try {
                        ResourceLocation id = new ResourceLocation(pieces[0]);
                        int damage = pieces.length > 1 && !pieces[1].isBlank()
                                ? Integer.parseInt(pieces[1])
                                : com.darkifov.thaumcraft.infusion.TC4InfusionItemMatcher.ANY_DAMAGE;
                        pendingComponentSpecs.add(new InfusionRecipe.ComponentSpec(id, damage, null));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        while (pendingComponentSpecs.size() < pendingComponents.size()) {
            ResourceLocation id = pendingComponents.get(pendingComponentSpecs.size());
            pendingComponentSpecs.add(new InfusionRecipe.ComponentSpec(id, com.darkifov.thaumcraft.infusion.TC4InfusionItemMatcher.ANY_DAMAGE, null));
        }
        while (pendingComponentSpecs.size() > pendingComponents.size()) {
            pendingComponentSpecs.remove(pendingComponentSpecs.size() - 1);
        }
    }


    private String serializePendingAspects() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Aspect, Integer> entry : pendingAspects.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(entry.getKey().id()).append(':').append(entry.getValue());
        }
        return builder.toString();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
