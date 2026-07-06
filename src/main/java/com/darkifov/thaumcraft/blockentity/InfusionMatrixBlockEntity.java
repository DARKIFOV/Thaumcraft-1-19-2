package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionMatrixAuxiliaryHelper;
import com.darkifov.thaumcraft.infusion.InfusionProcessHelper;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.infusion.InfusionStructureReport;
import com.darkifov.thaumcraft.infusion.MatrixAuxiliaryReport;
import com.darkifov.thaumcraft.infusion.TC4InfusionRuntime;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InfusionMatrixBlockEntity extends BlockEntity {
    private boolean active = false;
    private int progress = 0;
    private int duration = 0;
    private int cycleDelay = 0;
    private int currentInstability = 0;
    private ResourceLocation recipeId = null;
    private UUID ownerId = null;
    private String ownerName = "";
    private final EnumMap<Aspect, Integer> pendingAspects = new EnumMap<>(Aspect.class);
    private final List<ResourceLocation> pendingComponents = new ArrayList<>();

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.INFUSION_MATRIX_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean active() {
        return active;
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

    public Component statusComponent() {
        if (!active) {
            return Component.literal("Infusion Matrix idle.").withStyle(ChatFormatting.GRAY);
        }

        int pct = duration <= 0 ? 0 : Math.min(99, progress * 100 / duration);
        int pendingEssentia = TC4InfusionRuntime.totalPendingEssentia(pendingAspects);

        return Component.literal("Infusion active: " + pct + "% | Recipe: " + recipeId + " | Essentia left: " + pendingEssentia + " | Components left: " + pendingComponents.size() + " | Instability: " + currentInstability + " | Owner: " + ownerName)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public boolean startInfusion(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (active) {
            player.displayClientMessage(statusComponent(), false);
            return false;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            player.displayClientMessage(Component.literal("Place a catalyst on the TC4 center pedestal exactly two blocks below the matrix.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!report.valid()) {
            player.displayClientMessage(Component.literal("The infusion structure is too weak.").withStyle(ChatFormatting.RED), false);
            player.displayClientMessage(report.summary(), false);
            return false;
        }

        InfusionRecipe recipe = InfusionRecipes.find(catalystPedestal.stored());

        if (recipe == null) {
            player.displayClientMessage(Component.literal("No infusion recipe for catalyst: ").append(catalystPedestal.stored().getHoverName()).withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!PlayerThaumData.hasResearch(player, recipe.research())) {
            player.displayClientMessage(Component.literal("Research locked: " + recipe.research()).withStyle(ChatFormatting.RED), false);
            return false;
        }

        if (!InfusionProcessHelper.hasComponents(report.componentPedestals(), recipe)) {
            player.displayClientMessage(Component.literal("Missing infusion components: " + InfusionProcessHelper.componentText(recipe)).withStyle(ChatFormatting.RED), false);
            return false;
        }

        List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, worldPosition);

        if (!InfusionProcessHelper.hasAspects(jars, recipe)) {
            player.displayClientMessage(Component.literal("Missing essentia: " + InfusionProcessHelper.aspectText(recipe)).withStyle(ChatFormatting.RED), false);
            return false;
        }

        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        active = true;
        progress = 0;
        cycleDelay = 0;
        duration = TC4InfusionRuntime.estimateDuration(recipe, report);
        currentInstability = TC4InfusionRuntime.startingInstability(recipe, report, auxiliary.effectiveStabilizers());
        recipeId = recipe.id();
        ownerId = player.getUUID();
        ownerName = player.getName().getString();
        pendingAspects.clear();
        pendingComponents.clear();

        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            if (entry.getValue() > 0) {
                pendingAspects.put(entry.getKey(), entry.getValue());
            }
        }

        pendingComponents.addAll(recipe.components());

        player.displayClientMessage(
                Component.literal("Infusion started: ")
                        .append(catalystPedestal.stored().getHoverName())
                        .append(Component.literal(" | TC4 cycle runtime | Essentia: " + TC4InfusionRuntime.totalPendingEssentia(pendingAspects) + " | Components: " + pendingComponents.size() + " | Instability: " + currentInstability).withStyle(ChatFormatting.LIGHT_PURPLE)),
                false
        );

        level.playSound(null, worldPosition, TC4Sounds.event("infuserstart"), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.85D, worldPosition.getZ() + 0.5D, 30, 0.4D, 0.4D, 0.4D, 0.02D);
        }

        setChangedAndSync();
        return true;
    }

    public void cancelInfusion(Player player) {
        if (!active) {
            player.displayClientMessage(Component.literal("No active infusion.").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        active = false;
        progress = 0;
        duration = 0;
        cycleDelay = 0;
        currentInstability = 0;
        recipeId = null;
        pendingAspects.clear();
        pendingComponents.clear();

        player.displayClientMessage(Component.literal("Infusion cancelled. Already drained TC4 essentia/components are not restored.").withStyle(ChatFormatting.YELLOW), false);
        setChangedAndSync();
    }

    private void tickServer() {
        if (level == null || !active) {
            return;
        }

        InfusionRecipe recipe = recipeId == null ? null : InfusionRecipes.findById(recipeId);

        if (recipe == null) {
            resetInfusionState();
            setChangedAndSync();
            return;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);
        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        Player owner = getOwner();

        if (catalystPedestal == null || catalystPedestal.stored().isEmpty()) {
            failInfusion(owner, "Catalyst pedestal is missing.");
            return;
        }

        if (!recipe.catalystMatches(catalystPedestal.stored())) {
            failInfusion(owner, "Catalyst changed during infusion.");
            return;
        }

        if (!report.valid()) {
            failInfusion(owner, "Infusion structure became invalid.");
            return;
        }

        progress += Math.max(1, auxiliary.speedMultiplier());

        if (owner != null && progress % 40 == 0) {
            owner.displayClientMessage(statusComponent(), true);
        }

        if (level instanceof ServerLevel serverLevel) {
            InfusionProcessHelper.spawnProcessParticles(serverLevel, worldPosition, report, progress, duration);
        }

        if (currentInstability > 0) {
            InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers(), currentInstability);
        }

        if (cycleDelay > 0) {
            cycleDelay--;
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

    private boolean drainNextEssentia(InfusionRecipe recipe, Player owner) {
        Aspect aspect = InfusionProcessHelper.firstPendingAspect(pendingAspects);

        if (aspect == null) {
            return false;
        }

        List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, worldPosition);

        if (InfusionProcessHelper.consumeOneAspect(jars, aspect)) {
            int left = pendingAspects.getOrDefault(aspect, 0) - 1;

            if (left <= 0) {
                pendingAspects.remove(aspect);
            } else {
                pendingAspects.put(aspect, left);
            }

            cycleDelay = TC4InfusionRuntime.ESSENTIA_DRAIN_RANGE;
            level.playSound(null, worldPosition, TC4Sounds.event("infuser"), SoundSource.BLOCKS, 0.35F, 0.85F + level.random.nextFloat() * 0.25F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.PORTAL, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 12, 0.9D, 0.35D, 0.9D, 0.06D);
            }

            return true;
        }

        if (level.random.nextInt(TC4InfusionRuntime.failedEssentiaRollBound(recipe.instability())) == 0) {
            currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
        }

        if (owner != null && level.getGameTime() % 60L == 0L) {
            owner.displayClientMessage(Component.literal("Infusion is waiting for essentia: " + InfusionProcessHelper.pendingAspectText(pendingAspects)).withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        cycleDelay = 10;
        return true;
    }

    private boolean pullNextComponent(InfusionRecipe recipe, InfusionStructureReport report, Player owner) {
        if (pendingComponents.isEmpty()) {
            return false;
        }

        for (int i = 0; i < pendingComponents.size(); i++) {
            ResourceLocation componentId = pendingComponents.get(i);
            ArcanePedestalBlockEntity pedestal = InfusionProcessHelper.findComponentPedestal(report.componentPedestals(), componentId);

            if (pedestal != null) {
                if (level instanceof ServerLevel serverLevel) {
                    InfusionProcessHelper.spawnSourceParticles(serverLevel, pedestal.getBlockPos(), worldPosition, true);
                }

                if (InfusionProcessHelper.consumeSingleComponent(report.componentPedestals(), componentId)) {
                    pendingComponents.remove(i);
                    cycleDelay = TC4InfusionRuntime.ITEM_PULL_DELAY;
                    level.playSound(null, worldPosition, TC4Sounds.event("craftstart"), SoundSource.BLOCKS, 0.45F, 0.9F + level.random.nextFloat() * 0.2F);
                    return true;
                }
            }
        }

        if (!pendingAspects.isEmpty() && level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipe.instability())) == 0) {
            Aspect extra = InfusionProcessHelper.firstPendingAspect(pendingAspects);

            if (extra != null) {
                pendingAspects.put(extra, pendingAspects.getOrDefault(extra, 0) + 1);
            }
        }

        if (level.random.nextInt(TC4InfusionRuntime.failedComponentRollBound(recipe.instability())) == 0) {
            currentInstability = TC4InfusionRuntime.clampInstability(currentInstability + 1);
        }

        if (owner != null && level.getGameTime() % 60L == 0L) {
            owner.displayClientMessage(Component.literal("Infusion is waiting for components: " + InfusionProcessHelper.pendingComponentText(pendingComponents)).withStyle(ChatFormatting.DARK_PURPLE), true);
        }

        cycleDelay = 10;
        return true;
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

        if (result.isEmpty()) {
            failInfusion(owner, "Result item is missing.");
            return;
        }

        catalystPedestal.setStored(result);

        if (currentInstability > 0 && owner != null) {
            PlayerThaumData.addWarp(owner, Math.max(1, currentInstability / 2));

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        level.playSound(null, worldPosition, TC4Sounds.event("learn"), SoundSource.BLOCKS, 0.9F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.95D, worldPosition.getZ() + 0.5D, 60, 0.6D, 0.6D, 0.6D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 100, 1.4D, 0.4D, 1.4D, 0.08D);
        }

        if (owner != null) {
            owner.displayClientMessage(Component.literal("Infusion complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        }

        resetInfusionState();
    }

    private void failInfusion(Player owner, String reason) {
        if (level != null) {
            level.playSound(null, worldPosition, TC4Sounds.event("craftfail"), SoundSource.BLOCKS, 0.9F, 0.9F);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 70, 1.2D, 0.4D, 1.2D, 0.06D);
        }

        if (owner != null) {
            PlayerThaumData.addWarp(owner, 1);
            owner.displayClientMessage(Component.literal("Infusion failed: " + reason).withStyle(ChatFormatting.RED), false);

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        resetInfusionState();
    }

    private void resetInfusionState() {
        active = false;
        progress = 0;
        duration = 0;
        cycleDelay = 0;
        currentInstability = 0;
        recipeId = null;
        pendingAspects.clear();
        pendingComponents.clear();
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
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putInt("CycleDelay", cycleDelay);
        tag.putInt("CurrentInstability", currentInstability);

        if (recipeId != null) {
            tag.putString("RecipeId", recipeId.toString());
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.getBoolean("Active");
        progress = tag.getInt("Progress");
        duration = tag.getInt("Duration");
        cycleDelay = tag.getInt("CycleDelay");
        currentInstability = tag.getInt("CurrentInstability");
        recipeId = tag.contains("RecipeId") ? new ResourceLocation(tag.getString("RecipeId")) : null;
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.getString("OwnerName");
        pendingAspects.clear();
        pendingComponents.clear();

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
