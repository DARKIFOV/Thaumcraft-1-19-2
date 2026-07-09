package com.darkifov.thaumcraft.infusion;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.ArcanePedestalBlockEntity;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InfusionProcessHelper {
    public static final int RADIUS = TC4InfusionRuntime.ESSENTIA_DRAIN_RANGE;

    private InfusionProcessHelper() {
    }

    public static BlockPos findNearbyMatrix(Level level, BlockPos sourcePos, int radius) {
        for (BlockPos scan : BlockPos.betweenClosed(sourcePos.offset(-radius, -radius, -radius), sourcePos.offset(radius, radius, radius))) {
            if (level.getBlockState(scan).is(ThaumcraftMod.INFUSION_MATRIX.get())) {
                return scan.immutable();
            }
        }

        return null;
    }

    public static int acceleratedDuration(int baseDuration, InfusionStructureReport report) {
        int multiplier = Math.max(1, report.speedMultiplier());
        return Math.max(40, baseDuration / multiplier);
    }

    public static int acceleratorExtraInstability(InfusionStructureReport report) {
        return Math.max(0, Math.min(4, report.matrixAccelerators()) - Math.min(4, report.matrixStabilizers()));
    }

    public static ArcanePedestalBlockEntity findCatalystPedestal(Level level, BlockPos matrixPos) {
        // TC4 validLocation checks the center pedestal exactly at matrixY - 2.
        if (level.getBlockEntity(matrixPos.below(2)) instanceof ArcanePedestalBlockEntity pedestal) {
            return pedestal;
        }

        return null;
    }

    public static List<EssentiaJarBlockEntity> findJars(Level level, BlockPos center) {
        List<EssentiaJarBlockEntity> result = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -3, -RADIUS), center.offset(RADIUS, 3, RADIUS))) {
            if (level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar && !jar.aspects().isEmpty()) {
                result.add(jar);
            }
        }

        return result;
    }

    public static boolean hasComponents(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe recipe) {
        return hasComponents(pedestals, recipe, ItemStack.EMPTY);
    }

    public static boolean hasComponents(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe recipe, ItemStack catalyst) {
        List<ItemStack> available = new ArrayList<>();
        for (ArcanePedestalBlockEntity pedestal : pedestals) {
            if (!pedestal.stored().isEmpty()) {
                available.add(pedestal.stored().copy());
            }
        }

        for (InfusionRecipe.ComponentSpec componentSpec : recipe.componentSpecsFor(catalyst)) {
            boolean matched = false;
            for (int i = 0; i < available.size(); i++) {
                if (recipe.componentMatches(available.get(i), componentSpec)) {
                    available.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return available.isEmpty() || recipe.isInfusionEnchantment();
    }

    public static void consumeComponents(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe recipe) {
        List<ResourceLocation> remaining = new ArrayList<>(recipe.components());

        for (ArcanePedestalBlockEntity pedestal : pedestals) {
            if (remaining.isEmpty()) {
                break;
            }

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(pedestal.stored().getItem());

            if (id != null && remaining.remove(id)) {
                pedestal.setStored(ItemStack.EMPTY);
            }
        }
    }

    public static boolean hasAspects(List<EssentiaJarBlockEntity> jars, InfusionRecipe recipe) {
        return hasAspects(jars, recipe.aspectCost());
    }

    public static boolean hasAspects(List<EssentiaJarBlockEntity> jars, Map<Aspect, Integer> requiredAspects) {
        AspectList total = new AspectList();

        for (EssentiaJarBlockEntity jar : jars) {
            total.addAll(jar.aspects());
        }

        for (Map.Entry<Aspect, Integer> entry : requiredAspects.entrySet()) {
            if (total.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    public static void consumeAspects(List<EssentiaJarBlockEntity> jars, InfusionRecipe recipe) {
        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            Aspect aspect = entry.getKey();
            int remaining = entry.getValue();

            for (EssentiaJarBlockEntity jar : jars) {
                if (remaining <= 0) {
                    break;
                }

                int removed = jar.aspects().removeUpTo(aspect, remaining);

                if (removed > 0) {
                    jar.setChangedAndSync();
                    remaining -= removed;
                }
            }
        }
    }


    public static boolean consumeOneAspect(List<EssentiaJarBlockEntity> jars, Aspect aspect) {
        return consumeOneAspectSource(jars, aspect) != null;
    }

    public static BlockPos consumeOneAspectSource(List<EssentiaJarBlockEntity> jars, Aspect aspect) {
        return consumeOneAspectSource(jars, aspect, null);
    }

    public static BlockPos consumeOneAspectSource(List<EssentiaJarBlockEntity> jars, Aspect aspect, BlockPos matrixPos) {
        if (aspect == null) {
            return null;
        }

        List<EssentiaJarBlockEntity> ordered = matrixPos == null ? jars : nearestJarOrder(jars, matrixPos);
        for (EssentiaJarBlockEntity jar : ordered) {
            int removed = jar.aspects().removeUpTo(aspect, 1);

            if (removed > 0) {
                jar.setChangedAndSync();
                return jar.getBlockPos();
            }
        }

        return null;
    }

    private static List<EssentiaJarBlockEntity> nearestJarOrder(List<EssentiaJarBlockEntity> jars, BlockPos matrixPos) {
        List<EssentiaJarBlockEntity> ordered = new ArrayList<>(jars);
        // v11.62: TC4 craftCycle source selection should not depend on BlockPos.betweenClosed iteration order.
        // Prefer the closest valid jar/source to the matrix, then use coordinates only as a deterministic tie-breaker.
        ordered.sort(Comparator
                .comparingDouble((EssentiaJarBlockEntity jar) -> jar.getBlockPos().distSqr(matrixPos))
                .thenComparingInt(jar -> jar.getBlockPos().getY())
                .thenComparingInt(jar -> jar.getBlockPos().getX())
                .thenComparingInt(jar -> jar.getBlockPos().getZ()));
        return ordered;
    }

    public static ArcanePedestalBlockEntity findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId) {
        return findComponentPedestal(pedestals, componentId, null);
    }

    public static ArcanePedestalBlockEntity findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId, InfusionRecipe recipe) {
        if (componentId == null) {
            return null;
        }

        for (ArcanePedestalBlockEntity pedestal : pedestals) {
            if (recipe == null) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(pedestal.stored().getItem());
                if (componentId.equals(id)) {
                    return pedestal;
                }
            } else if (recipe.componentMatches(pedestal.stored(), componentId)) {
                return pedestal;
            }
        }

        return null;
    }

    public static ArcanePedestalBlockEntity findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe.ComponentSpec componentSpec, InfusionRecipe recipe) {
        return findComponentPedestal(pedestals, componentSpec, recipe, null);
    }

    public static ArcanePedestalBlockEntity findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe.ComponentSpec componentSpec, InfusionRecipe recipe, BlockPos matrixPos) {
        if (componentSpec == null || componentSpec.itemId() == null) {
            return null;
        }
        List<ArcanePedestalBlockEntity> ordered = matrixPos == null ? pedestals : nearestPedestalOrder(pedestals, matrixPos);
        for (ArcanePedestalBlockEntity pedestal : ordered) {
            if (recipe == null) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(pedestal.stored().getItem());
                if (componentSpec.itemId().equals(id)) {
                    return pedestal;
                }
            } else if (recipe.componentMatches(pedestal.stored(), componentSpec)) {
                return pedestal;
            }
        }
        return null;
    }

    private static List<ArcanePedestalBlockEntity> nearestPedestalOrder(List<ArcanePedestalBlockEntity> pedestals, BlockPos matrixPos) {
        List<ArcanePedestalBlockEntity> ordered = new ArrayList<>(pedestals);
        // v11.62: lock the concrete component source in a stable nearest-source order before ITEM_PULL_DELAY.
        ordered.sort(Comparator
                .comparingDouble((ArcanePedestalBlockEntity pedestal) -> pedestal.getBlockPos().distSqr(matrixPos))
                .thenComparingInt(pedestal -> pedestal.getBlockPos().getY())
                .thenComparingInt(pedestal -> pedestal.getBlockPos().getX())
                .thenComparingInt(pedestal -> pedestal.getBlockPos().getZ()));
        return ordered;
    }

    public static boolean consumeSingleComponent(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId) {
        return consumeSingleComponent(pedestals, componentId, null);
    }

    public static boolean consumeSingleComponent(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId, InfusionRecipe recipe) {
        ArcanePedestalBlockEntity pedestal = findComponentPedestal(pedestals, componentId, recipe);

        if (pedestal == null) {
            return false;
        }

        consumePedestalComponentPreservingContainer(pedestal.getLevel(), pedestal);

        return true;
    }

    public static void consumePedestalComponentPreservingContainer(Level level, ArcanePedestalBlockEntity pedestal) {
        if (pedestal == null || pedestal.stored().isEmpty()) {
            return;
        }
        ItemStack stack = pedestal.stored().copy();
        ItemStack container = stack.getCraftingRemainingItem();
        stack.shrink(1);

        if (stack.isEmpty()) {
            pedestal.setStored(container.isEmpty() ? ItemStack.EMPTY : container.copy());
            return;
        }

        pedestal.setStored(stack);
        if (!container.isEmpty() && level != null) {
            BlockPos p = pedestal.getBlockPos();
            Containers.dropItemStack(level, p.getX() + 0.5D, p.getY() + 1.1D, p.getZ() + 0.5D, container.copy());
        }
    }

    public static Aspect firstPendingAspect(Map<Aspect, Integer> pendingAspects) {
        for (Map.Entry<Aspect, Integer> entry : pendingAspects.entrySet()) {
            if (entry.getValue() > 0) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static String pendingAspectText(Map<Aspect, Integer> pendingAspects) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Aspect, Integer> entry : pendingAspects.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(entry.getKey().displayName()).append(" ").append(entry.getValue());
        }

        return builder.length() == 0 ? "none" : builder.toString();
    }

    public static String pendingComponentText(List<ResourceLocation> pendingComponents) {
        StringBuilder builder = new StringBuilder();

        for (ResourceLocation id : pendingComponents) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(id);
        }

        return builder.length() == 0 ? "none" : builder.toString();
    }

    public static void spawnSourceParticles(ServerLevel level, BlockPos source, BlockPos matrixPos, boolean item) {
        // Stage209: server particle fallback plus TC4 PacketFXInfusionSource client arc.
        ThaumcraftNetwork.sendInfusionSource(level, matrixPos, source, 0);
        spawnParticleLine(level,
                source.getX() + 0.5D, source.getY() + 1.15D, source.getZ() + 0.5D,
                matrixPos.getX() + 0.5D, matrixPos.getY() + 0.7D, matrixPos.getZ() + 0.5D,
                item ? ParticleTypes.WITCH : ParticleTypes.PORTAL,
                item ? 14 : 10);
    }

    public static int calculatedInstability(InfusionRecipe recipe, InfusionStructureReport report) {
        return Math.max(0, recipe.instability()
                + report.instabilityPenalty()
                + acceleratorExtraInstability(report)
                - Math.max(0, report.stabilityScore()) / 5);
    }

    public static int calculatedInstability(InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers) {
        return Math.max(0, calculatedInstability(recipe, report) - Math.max(0, matrixStabilizers));
    }

    public static void spawnProcessParticles(ServerLevel level, BlockPos matrixPos, InfusionStructureReport report, int progress, int duration) {
        double pct = duration <= 0 ? 0.0D : (double) progress / (double) duration;

        level.sendParticles(
                ParticleTypes.ENCHANT,
                matrixPos.getX() + 0.5D,
                matrixPos.getY() + 0.65D,
                matrixPos.getZ() + 0.5D,
                8 + (int) (pct * 12),
                0.55D + pct,
                0.25D,
                0.55D + pct,
                0.04D
        );

        if (progress % 8 == 0) {
            for (ArcanePedestalBlockEntity pedestal : report.componentPedestals()) {
                BlockPos p = pedestal.getBlockPos();
                spawnParticleLine(level, p.getX() + 0.5D, p.getY() + 1.25D, p.getZ() + 0.5D,
                        matrixPos.getX() + 0.5D, matrixPos.getY() + 0.7D, matrixPos.getZ() + 0.5D,
                        ParticleTypes.WITCH, 10);
            }
        }

        if (progress % 12 == 0) {
            for (EssentiaJarBlockEntity jar : findJars(level, matrixPos)) {
                BlockPos p = jar.getBlockPos();
                spawnParticleLine(level, p.getX() + 0.5D, p.getY() + 1.15D, p.getZ() + 0.5D,
                        matrixPos.getX() + 0.5D, matrixPos.getY() + 0.7D, matrixPos.getZ() + 0.5D,
                        ParticleTypes.PORTAL, 8);
            }
        }

        if (progress > duration * 0.75D) {
            level.sendParticles(ParticleTypes.END_ROD, matrixPos.getX() + 0.5D, matrixPos.getY() + 0.9D, matrixPos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.01D);
        }

        // Stage703-722: do not add non-TC4 happy-villager progress markers.
        // Original craftCycle feedback is handled by the matrix swirl plus
        // source-to-matrix FX packets for jars/pedestals/entities.
    }

    private static void spawnParticleLine(ServerLevel level, double sx, double sy, double sz,
                                          double ex, double ey, double ez,
                                          net.minecraft.core.particles.ParticleOptions particle,
                                          int points) {
        for (int i = 0; i <= points; i++) {
            double t = points <= 0 ? 0.0D : (double) i / (double) points;
            double x = sx + (ex - sx) * t;
            double y = sy + (ey - sy) * t;
            double z = sz + (ez - sz) * t;
            level.sendParticles(particle, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    public static void spawnParticleBeam(ServerLevel level, double sx, double sy, double sz,
                                         double ex, double ey, double ez,
                                         net.minecraft.core.particles.ParticleOptions particle,
                                         int points) {
        spawnParticleLine(level, sx, sy, sz, ex, ey, ez, particle, points);
    }

    public static boolean instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report) {
        return instabilityEvent(level, matrixPos, player, recipe, report, 0);
    }

    public static boolean instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers) {
        int instability = calculatedInstability(recipe, report, matrixStabilizers);
        return instabilityEvent(level, matrixPos, player, recipe, report, matrixStabilizers, instability);
    }

    public static boolean instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers, int explicitInstability) {
        return InfusionInstabilityEvents.maybeTrigger(level, matrixPos, player, recipe, report, explicitInstability);
    }

    public static String componentText(InfusionRecipe recipe) {
        return componentText(recipe, ItemStack.EMPTY);
    }

    public static String componentText(InfusionRecipe recipe, ItemStack catalyst) {
        StringBuilder builder = new StringBuilder();

        for (ResourceLocation id : recipe.componentsFor(catalyst)) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(id);
        }

        return builder.toString();
    }

    public static String aspectText(InfusionRecipe recipe) {
        return aspectText(recipe, ItemStack.EMPTY);
    }

    public static String aspectText(InfusionRecipe recipe, ItemStack catalyst) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCostFor(catalyst).entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(entry.getKey().displayName()).append(" ").append(entry.getValue());
        }

        return builder.toString();
    }
}
