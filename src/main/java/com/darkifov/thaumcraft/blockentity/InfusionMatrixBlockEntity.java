package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.recipe.TC4RecipeRequirementIndex;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionMatrixAuxiliaryHelper;
import com.darkifov.thaumcraft.infusion.InfusionProcessHelper;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.infusion.InfusionStructureReport;
import com.darkifov.thaumcraft.infusion.MatrixAuxiliaryReport;
import com.darkifov.thaumcraft.infusion.TC4InfusionRuntime;
import com.darkifov.thaumcraft.infusion.TC4InfusionCraftCycleParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionStabilityParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionFailureParity;
import com.darkifov.thaumcraft.infusion.TC4InfusionEnchantmentAdapter;
import com.darkifov.thaumcraft.infusion.TC4InfusionRunicAugmentAdapter;
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
import net.minecraft.server.level.ServerPlayer;
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

public class InfusionMatrixBlockEntity extends BlockEntity {
    private boolean active = false;
    private boolean crafting = false;
    private boolean checkSurroundings = true;
    private int count = 0;
    private int craftCount = 0;
    private int symmetry = 0;
    private int progress = 0;
    private int duration = 0;
    private int cycleDelay = 0;
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

    public float updateAndGetRenderStartUp() {
        if (active && renderStartUp != 1.0F) {
            renderStartUp += Math.max(renderStartUp / 10.0F, 0.001F);
            if (renderStartUp > 0.999F) {
                renderStartUp = 1.0F;
            }
        }
        if (!active && renderStartUp > 0.0F) {
            renderStartUp -= renderStartUp / 10.0F;
            if (renderStartUp < 0.001F) {
                renderStartUp = 0.0F;
            }
        }
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

    public boolean onWandRightClick(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (active && !crafting) {
            return startInfusion(player);
        }

        if (!active) {
            return activateMatrix(player);
        }

        player.displayClientMessage(statusComponent(), false);
        return false;
    }

    private boolean activateMatrix(Player player) {
        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);

        if (!report.strictTc4Location()) {
            player.displayClientMessage(Component.literal("TC4 validLocation failed: center pedestal and four infusion pillars are required.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(report.summary(), false);
            return false;
        }

        active = true;
        crafting = false;
        checkSurroundings = true;
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

        player.displayClientMessage(Component.literal("Infusion Matrix activated. Use the wand again to begin the TC4 craft cycle.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        player.displayClientMessage(report.summary(), false);
        setChangedAndSync();
        return true;
    }

    public boolean startInfusion(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (!active) {
            player.displayClientMessage(Component.literal("First wand use activates the TC4 matrix; second wand use starts crafting.").withStyle(ChatFormatting.GRAY), false);
            return activateMatrix(player);
        }

        if (crafting) {
            player.displayClientMessage(statusComponent(), false);
            return false;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            player.displayClientMessage(Component.literal("Place a catalyst on the TC4 center pedestal exactly two blocks below the matrix.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!report.strictTc4Location()) {
            player.displayClientMessage(Component.literal("The infusion structure no longer matches TC4 validLocation.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(report.summary(), false);
            deactivateMatrix();
            return false;
        }

        if (!report.valid()) {
            player.displayClientMessage(Component.literal("TC4 matrix is active, but no component pedestals are ready.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(report.summary(), false);
            return false;
        }

        InfusionRecipe recipe = findMatchingOriginalInfusionRecipe(catalystPedestal.stored(), report.componentPedestals());

        if (recipe == null) {
            player.displayClientMessage(Component.literal("No infusion recipe for catalyst: ").append(catalystPedestal.stored().getHoverName()).withStyle(ChatFormatting.RED), false);
            return false;
        }

        String requiredResearch = TC4RecipeRequirementIndex.requiredResearchForRuntimeRecipe(recipe.tc4Key(), recipe.research());
        if (!requiredResearch.isBlank() && !PlayerThaumData.hasResearch(player, requiredResearch)) {
            player.displayClientMessage(Component.literal("Research locked: " + requiredResearch).withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!InfusionProcessHelper.hasComponents(report.componentPedestals(), recipe, catalystPedestal.stored())) {
            player.displayClientMessage(Component.literal("Missing infusion components: " + InfusionProcessHelper.componentText(recipe, catalystPedestal.stored())).withStyle(ChatFormatting.RED), false);
            return false;
        }

        EnumMap<Aspect, Integer> requiredAspects = recipe.isInfusionEnchantment()
                ? TC4InfusionEnchantmentAdapter.scaledAspects(recipe, catalystPedestal.stored())
                : recipe.aspectCostFor(catalystPedestal.stored());

        List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, worldPosition);

        if (!InfusionProcessHelper.hasAspects(jars, requiredAspects)) {
            player.displayClientMessage(Component.literal("Missing essentia: " + InfusionProcessHelper.aspectText(recipe, catalystPedestal.stored())).withStyle(ChatFormatting.RED), false);
            return false;
        }

        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        active = true;
        crafting = true;
        checkSurroundings = false;
        progress = 0;
        cycleDelay = 0;
        List<ResourceLocation> requiredComponents = TC4InfusionRuntime.orderedComponentPullList(recipe, catalystPedestal.stored());
        List<InfusionRecipe.ComponentSpec> requiredComponentSpecs = TC4InfusionRuntime.orderedComponentSpecList(recipe, catalystPedestal.stored());
        duration = TC4InfusionRuntime.estimateDuration(recipe, report, requiredAspects, requiredComponents, recipe.instabilityFor(catalystPedestal.stored()));
        symmetry = report.originalSymmetryPenalty();
        recipeType = recipe.recipeType();
        recipeXP = recipe.isInfusionEnchantment() ? TC4InfusionEnchantmentAdapter.calcXp(recipe, catalystPedestal.stored()) : 0;
        recipeInstability = recipe.isInfusionEnchantment() ? TC4InfusionEnchantmentAdapter.calcInstability(recipe, catalystPedestal.stored()) : recipe.instabilityFor(catalystPedestal.stored());
        currentInstability = TC4InfusionRuntime.clampInstability(symmetry + recipeInstability + auxiliary.unpairedInstabilityPenalty() - auxiliary.effectiveStabilizers());
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

        if (TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES) {
            player.displayClientMessage(
                    Component.literal("Infusion started: ")
                            .append(catalystPedestal.stored().getHoverName())
                            .append(Component.literal(" | TC4 craftCycle | Essentia: " + TC4InfusionRuntime.totalPendingEssentia(pendingAspects) + " | Components: " + pendingComponents.size() + " | XP: " + recipeXP + " | Symmetry: " + symmetry + " | Instability: " + currentInstability).withStyle(ChatFormatting.LIGHT_PURPLE)),
                    false
            );
        }

        level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_MATRIX_START), SoundSource.BLOCKS, 0.5F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.85D, worldPosition.getZ() + 0.5D, 30, 0.4D, 0.4D, 0.4D, 0.02D);
        }

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
        if (!active) {
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

        if (!active) {
            return;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);

        if (checkSurroundings || count % (crafting ? 20 : 100) == 0) {
            checkSurroundings = false;
            symmetry = report.originalSymmetryPenalty();

            if (!report.strictTc4Location()) {
                deactivateMatrix();
                setChangedAndSync();
                return;
            }
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
        updateRunningStabilitySnapshot(report, auxiliary);

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            failInfusion(owner, "Catalyst pedestal is missing.");
            return;
        }

        if (!lockedCatalystStackStillMatches(recipe, catalystPedestal.stored())) {
            // TC4 stores recipeInput at craft start and compares against that exact stack during craftCycle.
            // Keep NBT/damage-sensitive catalyst locks so mid-craft tag swaps do not silently finish.
            InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers(), Math.max(1, currentInstability));
            failInfusion(owner, "Catalyst changed during infusion.");
            return;
        }

        if (!report.valid()) {
            InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers(), Math.max(1, currentInstability));
            failInfusion(owner, "Infusion structure became invalid.");
            return;
        }

        if (!componentsHaveStartedTravelling(recipe, catalystPedestal.stored())
                && !lockedRecipeStillMatchesCurrentPedestals(recipe, catalystPedestal.stored(), report.componentPedestals())) {
            // Stage343-362: original TC4 recipe selection is locked to the
            // catalyst plus component pedestals at start. Do not silently drift
            // to another recipe sharing the same catalyst before the first
            // pedestal item has been pulled.
            InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers(), Math.max(1, currentInstability));
            failInfusion(owner, "Infusion recipe lock no longer matches catalyst and component pedestals.");
            return;
        }

        craftCount++;
        progress += Math.max(1, auxiliary.speedMultiplier());

        if (owner != null && progress % 40 == 0) {
            owner.displayClientMessage(statusComponent(), true);
        }

        if (level instanceof ServerLevel serverLevel) {
            InfusionProcessHelper.spawnProcessParticles(serverLevel, worldPosition, report, progress, duration);
        }

        if (cycleDelay > 0) {
            cycleDelay--;
            setChangedAndSync();
            return;
        }

        // TC4 rolls instability inside craftCycle, not every render/progress tick.
        // A triggered event consumes this cycle and delays normal drain/pull work.
        if (currentInstability > 0 && InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers(), currentInstability)) {
            cycleDelay = TC4InfusionCraftCycleParity.CRAFT_CYCLE_DELAY;
            setChangedAndSync();
            return;
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

    private void updateRunningStabilitySnapshot(InfusionStructureReport report, MatrixAuxiliaryReport auxiliary) {
        if (report == null || auxiliary == null) {
            return;
        }
        String signature = auxiliary.stabilizerSignature() == null ? "" : auxiliary.stabilizerSignature();
        int currentSymmetry = report.originalSymmetryPenalty();
        boolean changed = !signature.equals(lastStabilizerSignature)
                || currentSymmetry != lastSymmetryPenalty
                || auxiliary.symmetricStabilizers() != lastStabilizerPairs
                || auxiliary.unpairedStabilizers() != lastUnpairedStabilizers;
        if (!changed) {
            return;
        }

        // TC4 re-checks altar symmetry during craftCycle.  Do not silently lower
        // instability mid-craft when a player adds stabilizers; only raise/keep it
        // if symmetry/stabilizers become worse or move, preserving original risk.
        int recalculated = TC4InfusionRuntime.clampInstability(currentSymmetry + recipeInstability + auxiliary.unpairedInstabilityPenalty() - auxiliary.effectiveStabilizers());
        currentInstability = Math.max(currentInstability, recalculated);
        rememberInfusionStabilitySnapshot(report, auxiliary);
    }

    private boolean drainEnchantmentXp(InfusionRecipe recipe, Player owner) {
        if (!recipe.isInfusionEnchantment() || recipeXP <= 0) {
            return false;
        }

        List<Player> targets = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(10.0D, 10.0D, 10.0D));
        for (Player target : targets) {
            if (target.experienceLevel > 0 || target.getAbilities().instabuild) {
                if (!target.getAbilities().instabuild) {
                    target.giveExperienceLevels(-1);
                    target.hurt(DamageSource.MAGIC, level.random.nextInt(2));
                }
                recipeXP -= 1;
                cycleDelay = TC4InfusionCraftCycleParity.ENCHANTMENT_XP_DELAY;
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

        if (!pendingAspects.isEmpty() && level.random.nextInt(3) == 0) {
            Aspect extra = InfusionProcessHelper.firstPendingAspect(pendingAspects);
            if (extra != null) {
                pendingAspects.put(extra, pendingAspects.getOrDefault(extra, 0) + 1);
            }
            if (level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipeInstability)) == 0) {
                currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
            }
        }

        if (TC4InfusionCraftCycleParity.SHOW_WAITING_DEBUG_MESSAGES && owner != null && level.getGameTime() % 60L == 0L) {
            owner.displayClientMessage(Component.literal("Infusion enchantment is waiting for XP levels: " + recipeXP).withStyle(ChatFormatting.DARK_PURPLE), true);
        }
        cycleDelay = TC4InfusionCraftCycleParity.ENCHANTMENT_XP_DELAY;
        return true;
    }

    private boolean drainNextEssentia(InfusionRecipe recipe, Player owner) {
        Aspect aspect = InfusionProcessHelper.firstPendingAspect(pendingAspects);

        if (aspect == null) {
            return false;
        }

        List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, worldPosition);

        BlockPos essentiaSource = InfusionProcessHelper.consumeOneAspectSource(jars, aspect);

        if (essentiaSource != null) {
            int left = pendingAspects.getOrDefault(aspect, 0) - 1;

            if (left <= 0) {
                pendingAspects.remove(aspect);
            } else {
                pendingAspects.put(aspect, left);
            }

            cycleDelay = TC4InfusionCraftCycleParity.CRAFT_CYCLE_DELAY;
            level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_ESSENTIA_DRAIN), SoundSource.BLOCKS, 0.35F, 0.85F + level.random.nextFloat() * 0.25F);

            if (level instanceof ServerLevel serverLevel) {
                InfusionProcessHelper.spawnSourceParticles(serverLevel, essentiaSource, worldPosition, false);
            }

            return true;
        }

        if (level.random.nextInt(TC4InfusionRuntime.failedEssentiaRollBound(recipeInstability)) == 0) {
            currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
        }

        if (TC4InfusionCraftCycleParity.SHOW_WAITING_DEBUG_MESSAGES && owner != null && level.getGameTime() % 60L == 0L) {
            owner.displayClientMessage(Component.literal("Infusion is waiting for essentia: " + InfusionProcessHelper.pendingAspectText(pendingAspects)).withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        cycleDelay = TC4InfusionCraftCycleParity.WAITING_RETRY_DELAY;
        return true;
    }

    private boolean pullNextComponent(InfusionRecipe recipe, InfusionStructureReport report, Player owner) {
        if (pendingComponents.isEmpty()) {
            return false;
        }

        if (travellingComponent != null) {
            if (consumeTravellingComponentFromLockedPedestal(recipe)) {
                removeTravellingComponentFromPending();
                travellingComponent = null;
                travellingComponentSource = null;
                travellingComponentSnapshot = ItemStack.EMPTY;
                travellingComponentIndex = -1;
                cycleDelay = TC4InfusionCraftCycleParity.CRAFT_CYCLE_DELAY;
                level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_COMPONENT_PULL), SoundSource.BLOCKS, 0.45F, 0.9F + level.random.nextFloat() * 0.2F);
                return true;
            }

            // Stage363-382: original craftCycle sources a concrete pedestal before it consumes the item.
            // Do not silently consume a duplicate component from a different pedestal if the source was moved.
            currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
            travellingComponent = null;
            travellingComponentSource = null;
            travellingComponentSnapshot = ItemStack.EMPTY;
            travellingComponentIndex = -1;
            cycleDelay = TC4InfusionCraftCycleParity.CRAFT_CYCLE_DELAY;
            return true;
        }

        for (int i = 0; i < pendingComponents.size(); i++) {
            ResourceLocation componentId = pendingComponents.get(i);
            InfusionRecipe.ComponentSpec componentSpec = pendingComponentSpecAt(i, componentId);
            ArcanePedestalBlockEntity pedestal = InfusionProcessHelper.findComponentPedestal(report.componentPedestals(), componentSpec, recipe);

            if (pedestal != null) {
                if (level instanceof ServerLevel serverLevel) {
                    InfusionProcessHelper.spawnSourceParticles(serverLevel, pedestal.getBlockPos(), worldPosition, true);
                }

                travellingComponent = componentId;
                travellingComponentSource = pedestal.getBlockPos();
                travellingComponentSnapshot = pedestal.stored().copy();
                travellingComponentSnapshot.setCount(1);
                travellingComponentIndex = i;
                cycleDelay = TC4InfusionCraftCycleParity.ITEM_PULL_DELAY;
                return true;
            }
        }

        if (!pendingAspects.isEmpty() && level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipeInstability)) == 0) {
            Aspect extra = InfusionProcessHelper.firstPendingAspect(pendingAspects);

            if (extra != null) {
                pendingAspects.put(extra, pendingAspects.getOrDefault(extra, 0) + 1);
            }
        }

        if (level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipeInstability)) == 0) {
            currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
        }

        if (TC4InfusionCraftCycleParity.SHOW_WAITING_DEBUG_MESSAGES && owner != null && level.getGameTime() % 60L == 0L) {
            owner.displayClientMessage(Component.literal("Infusion is waiting for components: " + InfusionProcessHelper.pendingComponentText(pendingComponents)).withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        cycleDelay = TC4InfusionCraftCycleParity.WAITING_RETRY_DELAY;
        return true;
    }

    private boolean consumeTravellingComponentFromLockedPedestal(InfusionRecipe recipe) {
        if (level == null || travellingComponent == null || travellingComponentSource == null) {
            return false;
        }
        if (!(level.getBlockEntity(travellingComponentSource) instanceof ArcanePedestalBlockEntity pedestal)) {
            return false;
        }
        InfusionRecipe.ComponentSpec lockedSpec = pendingComponentSpecAt(travellingComponentIndex, travellingComponent);
        if (!recipe.componentMatches(pedestal.stored(), lockedSpec)) {
            return false;
        }
        if (!travellingComponentSnapshot.isEmpty() && !componentStackStillMatchesLockedSource(pedestal.stored(), travellingComponentSnapshot)) {
            return false;
        }
        InfusionProcessHelper.consumePedestalComponentPreservingContainer(level, pedestal);
        return true;
    }

    private void removeTravellingComponentFromPending() {
        if (travellingComponent == null) {
            return;
        }
        if (travellingComponentIndex >= 0
                && travellingComponentIndex < pendingComponents.size()
                && travellingComponent.equals(pendingComponents.get(travellingComponentIndex))) {
            pendingComponents.remove(travellingComponentIndex);
            removePendingComponentSpecAt(travellingComponentIndex);
            return;
        }
        for (int i = 0; i < pendingComponents.size(); i++) {
            if (travellingComponent.equals(pendingComponents.get(i))) {
                pendingComponents.remove(i);
                removePendingComponentSpecAt(i);
                return;
            }
        }
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

    private boolean componentStackStillMatchesLockedSource(ItemStack current, ItemStack locked) {
        if (current == null || current.isEmpty() || locked == null || locked.isEmpty()) {
            return false;
        }
        // v8.22: TC4 craftCycle first chooses a concrete source pedestal, then later consumes that source.
        // Keep the selected stack identity (item + damage/NBT, count ignored) so a same-id replacement
        // or NBT/meta swap cannot satisfy the already travelling component.
        return ItemStack.isSameItemSameTags(current, locked);
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
            catalystPedestal.setStored(result);
        } else if (recipe.isRunicAugmentRecipe()) {
            if (!TC4InfusionRunicAugmentAdapter.applyOutput(catalystPedestal.stored())) {
                failInfusion(owner, "Runic augmentation output is missing or incompatible.");
                return;
            }
            result = catalystPedestal.stored().copy();
            catalystPedestal.setStored(result);
        } else if (recipe.hasNbtOutput()) {
            ItemStack central = catalystPedestal.stored().copy();
            central.addTagElement(recipe.outputNbtLabel(), recipe.outputNbt());
            result = central;
            catalystPedestal.setStored(result);
        } else {
            if (result.isEmpty()) {
                failInfusion(owner, "Result item is missing.");
                return;
            }
            catalystPedestal.setStored(result);
        }

        if (currentInstability > 0 && owner != null) {
            PlayerThaumData.addWarp(owner, Math.max(1, currentInstability / 2));

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_FINISH), SoundSource.BLOCKS, 0.5F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.95D, worldPosition.getZ() + 0.5D, 60, 0.6D, 0.6D, 0.6D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 100, 1.4D, 0.4D, 1.4D, 0.08D);
        }

        if (TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES && owner != null) {
            owner.displayClientMessage(Component.literal("Infusion complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        }

        clearCraftingState(true);
    }

    private void failInfusion(Player owner, String reason) {
        lastFailureReason = reason == null ? "" : reason;
        lastFailureInstability = Math.max(0, currentInstability);
        lastFailureTravellingComponent = travellingComponent;
        lastFailureTravellingSource = travellingComponentSource;
        lastFailureTravellingSnapshot = travellingComponentSnapshot.isEmpty() ? ItemStack.EMPTY : travellingComponentSnapshot.copy();

        InfusionRecipe failureRecipe = recipeId == null ? null : InfusionRecipes.findById(recipeId);
        ArcanePedestalBlockEntity catalystPedestal = level == null ? null : InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport failureReport = level == null ? null : InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);
        TC4InfusionFailureParity.applyTerminalFailure(level, worldPosition, owner, failureRecipe, failureReport, currentInstability, lastFailureReason);

        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event(TC4InfusionCraftCycleParity.SOUND_FAIL), SoundSource.BLOCKS, 0.9F, 0.9F);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 70, 1.2D, 0.4D, 1.2D, 0.06D);
        }

        if (owner != null) {
            PlayerThaumData.addWarp(owner, 1);
            if (TC4InfusionCraftCycleParity.SHOW_START_COMPLETE_DEBUG_MESSAGES) {
                owner.displayClientMessage(Component.literal("Infusion failed: " + reason).withStyle(ChatFormatting.RED), false);
            }

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        clearCraftingState(true, false);
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
        cycleDelay = 0;
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
    }

    private Player getOwner() {
        if (!(level instanceof ServerLevel serverLevel) || ownerId == null) {
            return null;
        }

        return serverLevel.getServer().getPlayerList().getPlayer(ownerId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        matrix.tickServer();
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Active", active);
        tag.putBoolean("Crafting", crafting);
        tag.putBoolean("CheckSurroundings", checkSurroundings);
        tag.putInt("Count", count);
        tag.putInt("CraftCount", craftCount);
        tag.putInt("Symmetry", symmetry);
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putInt("CycleDelay", cycleDelay);
        tag.putInt("CurrentInstability", currentInstability);
        tag.putInt("RecipeInstability", recipeInstability);
        tag.putInt("RecipeXP", recipeXP);
        tag.putInt("RecipeType", recipeType);

        // Original TC4 TileInfusionMatrix NBT names retained for save/debug parity.
        tag.putBoolean("active", active);
        tag.putBoolean("crafting", crafting);
        tag.putShort("instability", (short) currentInstability);
        tag.putInt("recipeinst", recipeInstability);
        tag.putInt("recipetype", recipeType);
        tag.putInt("recipexp", recipeXP);
        tag.putString("recipeplayer", ownerName == null ? "" : ownerName);
        tag.putString(TC4InfusionStabilityParity.NBT_RECIPE_STABILIZERS, lastStabilizerSignature == null ? "" : lastStabilizerSignature);
        tag.putInt(TC4InfusionStabilityParity.NBT_RECIPE_SYMMETRY, lastSymmetryPenalty);
        tag.putInt(TC4InfusionStabilityParity.NBT_RECIPE_STABILIZER_PAIRS, lastStabilizerPairs);
        tag.putInt(TC4InfusionStabilityParity.NBT_RECIPE_UNPAIRED_STABILIZERS, lastUnpairedStabilizers);
        tag.putString(TC4InfusionFailureParity.NBT_LAST_FAILURE_REASON, lastFailureReason == null ? "" : lastFailureReason);
        tag.putInt(TC4InfusionFailureParity.NBT_LAST_FAILURE_INSTABILITY, lastFailureInstability);
        if (lastFailureTravellingComponent != null) {
            tag.putString("recipefailurecomponent", lastFailureTravellingComponent.toString());
        }
        if (lastFailureTravellingSource != null) {
            tag.putInt("recipefailureSourceX", lastFailureTravellingSource.getX());
            tag.putInt("recipefailureSourceY", lastFailureTravellingSource.getY());
            tag.putInt("recipefailureSourceZ", lastFailureTravellingSource.getZ());
        }
        if (!lastFailureTravellingSnapshot.isEmpty()) {
            tag.put("recipefailureSourceStack", lastFailureTravellingSnapshot.save(new CompoundTag()));
        }

        if (recipeId != null) {
            tag.putString("RecipeId", recipeId.toString());
        }

        if (travellingComponent != null) {
            tag.putString("TravellingComponent", travellingComponent.toString());
        }

        if (travellingComponentSource != null) {
            tag.putInt("TravellingComponentSourceX", travellingComponentSource.getX());
            tag.putInt("TravellingComponentSourceY", travellingComponentSource.getY());
            tag.putInt("TravellingComponentSourceZ", travellingComponentSource.getZ());
            tag.putString("sourcePedestal", travellingComponentSource.getX() + "," + travellingComponentSource.getY() + "," + travellingComponentSource.getZ());
        }
        if (!travellingComponentSnapshot.isEmpty()) {
            tag.put("TravellingComponentSnapshot", travellingComponentSnapshot.save(new CompoundTag()));
            tag.put("sourceStack", travellingComponentSnapshot.save(new CompoundTag()));
        }
        tag.putInt("TravellingComponentIndex", travellingComponentIndex);

        if (lockedCatalystId != null) {
            tag.putString("LockedCatalystId", lockedCatalystId.toString());
            // Original TC4 debug parity: recipe object/catalyst are fixed once craftCycle starts.
            tag.putString("recipeobject", lockedCatalystId.toString());
        }
        if (!lockedCatalystSnapshot.isEmpty()) {
            tag.put("LockedCatalystSnapshot", lockedCatalystSnapshot.save(new CompoundTag()));
            // TC4 TileInfusionMatrix writes recipeinput as the exact ItemStack copy.
            tag.put("recipeinput", lockedCatalystSnapshot.save(new CompoundTag()));
        }

        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }

        tag.putString("OwnerName", ownerName);

        CompoundTag aspects = new CompoundTag();
        for (Map.Entry<Aspect, Integer> entry : pendingAspects.entrySet()) {
            aspects.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("PendingAspects", aspects);
        tag.putString("PendingComponents", TC4InfusionRuntime.serializeComponents(pendingComponents));
        tag.putString("PendingComponentSpecs", TC4InfusionRuntime.serializeComponentSpecs(pendingComponentSpecs));
        ListTag pendingSpecList = new ListTag();
        for (InfusionRecipe.ComponentSpec spec : pendingComponentSpecs) {
            if (spec == null || spec.itemId() == null) {
                continue;
            }
            CompoundTag specTag = new CompoundTag();
            specTag.putString("id", spec.itemId().toString());
            specTag.putInt("damage", spec.damage());
            if (spec.tag() != null && !spec.tag().isEmpty()) {
                specTag.put("nbt", spec.tag().copy());
            }
            pendingSpecList.add(specTag);
        }
        tag.put("PendingComponentSpecList", pendingSpecList);
        // Stage683-702: keep original-style recipe snapshot fields alongside the
        // typed Forge 1.19.2 fields.  These are not new mechanics; they preserve
        // the TC4 craftCycle state for debugging/save migration without allowing
        // catalyst-only recipe drift.
        tag.putString("recipeingredients", TC4InfusionRuntime.serializeComponents(pendingComponents));
        tag.putString("recipeessentia", serializePendingAspects());
        tag.putInt("recipeinstability", recipeInstability);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.contains("Active") ? tag.getBoolean("Active") : tag.getBoolean("active");
        crafting = tag.contains("Crafting") ? tag.getBoolean("Crafting") : tag.getBoolean("crafting");
        checkSurroundings = !tag.contains("CheckSurroundings") || tag.getBoolean("CheckSurroundings");
        count = tag.getInt("Count");
        craftCount = tag.getInt("CraftCount");
        symmetry = tag.getInt("Symmetry");
        progress = tag.getInt("Progress");
        duration = tag.getInt("Duration");
        cycleDelay = tag.getInt("CycleDelay");
        currentInstability = tag.contains("CurrentInstability") ? tag.getInt("CurrentInstability") : tag.getShort("instability");
        recipeInstability = tag.contains("RecipeInstability") ? tag.getInt("RecipeInstability") : tag.getInt("recipeinst");
        recipeXP = tag.contains("RecipeXP") ? tag.getInt("RecipeXP") : tag.getInt("recipexp");
        recipeType = tag.contains("RecipeType") ? tag.getInt("RecipeType") : tag.getInt("recipetype");
        recipeId = tag.contains("RecipeId") ? new ResourceLocation(tag.getString("RecipeId")) : null;
        travellingComponent = tag.contains("TravellingComponent") && !tag.getString("TravellingComponent").isEmpty() ? new ResourceLocation(tag.getString("TravellingComponent")) : null;
        if (tag.contains("TravellingComponentSourceX") && tag.contains("TravellingComponentSourceY") && tag.contains("TravellingComponentSourceZ")) {
            travellingComponentSource = new BlockPos(tag.getInt("TravellingComponentSourceX"), tag.getInt("TravellingComponentSourceY"), tag.getInt("TravellingComponentSourceZ"));
        } else {
            travellingComponentSource = null;
        }
        if (tag.contains("TravellingComponentSnapshot")) {
            travellingComponentSnapshot = ItemStack.of(tag.getCompound("TravellingComponentSnapshot"));
        } else if (tag.contains("sourceStack")) {
            travellingComponentSnapshot = ItemStack.of(tag.getCompound("sourceStack"));
        } else {
            travellingComponentSnapshot = ItemStack.EMPTY;
        }
        travellingComponentIndex = tag.contains("TravellingComponentIndex") ? tag.getInt("TravellingComponentIndex") : -1;
        String catalystId = tag.contains("LockedCatalystId") ? tag.getString("LockedCatalystId") : tag.getString("recipeobject");
        lockedCatalystId = catalystId == null || catalystId.isEmpty() ? null : new ResourceLocation(catalystId);
        if (tag.contains("LockedCatalystSnapshot")) {
            lockedCatalystSnapshot = ItemStack.of(tag.getCompound("LockedCatalystSnapshot"));
        } else if (tag.contains("recipeinput")) {
            lockedCatalystSnapshot = ItemStack.of(tag.getCompound("recipeinput"));
        } else {
            lockedCatalystSnapshot = ItemStack.EMPTY;
        }
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : tag.getString("recipeplayer");
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
        lastFailureTravellingSnapshot = tag.contains("recipefailureSourceStack") ? ItemStack.of(tag.getCompound("recipefailureSourceStack")) : ItemStack.EMPTY;
        pendingAspects.clear();
        pendingComponents.clear();
        pendingComponentSpecs.clear();

        if (tag.contains("PendingAspects")) {
            CompoundTag aspects = tag.getCompound("PendingAspects");
            for (Aspect aspect : Aspect.values()) {
                int amount = aspects.getInt(aspect.name());
                if (amount > 0) {
                    pendingAspects.put(aspect, amount);
                }
            }
        }

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
