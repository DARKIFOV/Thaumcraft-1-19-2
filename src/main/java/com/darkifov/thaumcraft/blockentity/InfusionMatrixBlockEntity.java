package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.infusion.InfusionAltarStructure;
import com.darkifov.thaumcraft.infusion.InfusionProcessHelper;
import com.darkifov.thaumcraft.infusion.InfusionMatrixAuxiliaryHelper;
import com.darkifov.thaumcraft.infusion.MatrixAuxiliaryReport;
import com.darkifov.thaumcraft.infusion.InfusionRecipe;
import com.darkifov.thaumcraft.infusion.InfusionRecipes;
import com.darkifov.thaumcraft.infusion.InfusionStructureReport;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public class InfusionMatrixBlockEntity extends BlockEntity {
    private boolean active = false;
    private int progress = 0;
    private int duration = 0;
    private ResourceLocation recipeId = null;
    private UUID ownerId = null;
    private String ownerName = "";

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

        int pct = duration <= 0 ? 0 : Math.min(100, progress * 100 / duration);

        return Component.literal("Infusion active: " + pct + "% | Recipe: " + recipeId + " | Owner: " + ownerName)
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
            player.displayClientMessage(Component.literal("Place a catalyst on the center pedestal below the matrix.").withStyle(ChatFormatting.RED), false);
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

        active = true;
        progress = 0;
        int baseDuration = ThaumcraftConfig.INFUSION_BASE_DURATION.get()
                + recipe.components().size() * ThaumcraftConfig.INFUSION_COMPONENT_DURATION.get()
                + recipe.instability() * ThaumcraftConfig.INFUSION_INSTABILITY_DURATION.get()
                + report.instabilityPenalty() * ThaumcraftConfig.INFUSION_SYMMETRY_PENALTY_DURATION.get();
        duration = InfusionProcessHelper.acceleratedDuration(baseDuration, report);
        recipeId = recipe.id();
        ownerId = player.getUUID();
        ownerName = player.getName().getString();

        player.displayClientMessage(
                Component.literal("Infusion started: ")
                        .append(catalystPedestal.stored().getHoverName())
                        .append(Component.literal(" | Duration: " + duration + " ticks | Speed: x" + report.speedMultiplier() + " | Stabilization: " + report.matrixStabilizationPercent() + "% | Stability: " + report.stabilityScore()).withStyle(ChatFormatting.LIGHT_PURPLE)),
                false
        );

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
        recipeId = null;

        player.displayClientMessage(Component.literal("Infusion cancelled. Components were not consumed.").withStyle(ChatFormatting.YELLOW), false);
        setChangedAndSync();
    }

    private void tickServer() {
        if (level == null || !active) {
            return;
        }

        InfusionRecipe recipe = recipeId == null ? null : InfusionRecipes.findById(recipeId);

        if (recipe == null) {
            active = false;
            progress = 0;
            setChangedAndSync();
            return;
        }

        ArcanePedestalBlockEntity catalystPedestal = InfusionProcessHelper.findCatalystPedestal(level, worldPosition);
        InfusionStructureReport report = InfusionAltarStructure.analyze(level, worldPosition, catalystPedestal);
        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        Player owner = getOwner();

        progress += auxiliary.speedMultiplier();

        if (owner != null && progress % 40 == 0) {
            int pct = duration <= 0 ? 0 : Math.min(100, progress * 100 / duration);
            owner.displayClientMessage(Component.literal("Infusion progress: " + pct + "% | Speed: x" + report.speedMultiplier() + " | Stabilization: " + report.matrixStabilizationPercent() + "%").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }

        if (level instanceof ServerLevel serverLevel) {
            InfusionProcessHelper.spawnProcessParticles(serverLevel, worldPosition, report, progress, duration);
        }

        if (level.getGameTime() % 45L == 0L) {
            InfusionProcessHelper.instabilityEvent(level, worldPosition, owner, recipe, report, auxiliary.effectiveStabilizers());
        }

        if (progress >= duration) {
            finishInfusion(owner, recipe, catalystPedestal, report);
        }

        setChangedAndSync();
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

        if (!InfusionProcessHelper.hasComponents(report.componentPedestals(), recipe)) {
            failInfusion(owner, "Required components are missing.");
            return;
        }

        List<EssentiaJarBlockEntity> jars = InfusionProcessHelper.findJars(level, worldPosition);

        if (!InfusionProcessHelper.hasAspects(jars, recipe)) {
            failInfusion(owner, "Required essentia is missing.");
            return;
        }

        InfusionProcessHelper.consumeComponents(report.componentPedestals(), recipe);
        InfusionProcessHelper.consumeAspects(jars, recipe);

        ItemStack result = recipe.result();

        if (result.isEmpty()) {
            failInfusion(owner, "Result item is missing.");
            return;
        }

        catalystPedestal.setStored(result);

        MatrixAuxiliaryReport auxiliary = InfusionMatrixAuxiliaryHelper.analyze(level, worldPosition, catalystPedestal, recipe);
        int instability = InfusionProcessHelper.calculatedInstability(recipe, report, auxiliary.effectiveStabilizers());

        if (instability > 0 && owner != null) {
            PlayerThaumData.addWarp(owner, Math.max(1, instability / 2));

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.95D, worldPosition.getZ() + 0.5D, 60, 0.6D, 0.6D, 0.6D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 100, 1.4D, 0.4D, 1.4D, 0.08D);
        }

        if (owner != null) {
            owner.displayClientMessage(Component.literal("Infusion complete: ").append(result.getHoverName()).withStyle(ChatFormatting.GOLD), false);
        }

        active = false;
        progress = 0;
        duration = 0;
        recipeId = null;
    }

    private void failInfusion(Player owner, String reason) {
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

        active = false;
        progress = 0;
        duration = 0;
        recipeId = null;
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

        if (recipeId != null) {
            tag.putString("RecipeId", recipeId.toString());
        }

        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }

        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        active = tag.getBoolean("Active");
        progress = tag.getInt("Progress");
        duration = tag.getInt("Duration");
        recipeId = tag.contains("RecipeId") ? new ResourceLocation(tag.getString("RecipeId")) : null;
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.getString("OwnerName");
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
