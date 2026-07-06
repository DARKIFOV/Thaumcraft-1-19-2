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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InfusionProcessHelper {
    public static final int RADIUS = 4;

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
        Map<ResourceLocation, Integer> available = new HashMap<>();

        for (ArcanePedestalBlockEntity pedestal : pedestals) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(pedestal.stored().getItem());

            if (id != null) {
                available.put(id, available.getOrDefault(id, 0) + 1);
            }
        }

        Map<ResourceLocation, Integer> needed = new HashMap<>();

        for (ResourceLocation id : recipe.components()) {
            needed.put(id, needed.getOrDefault(id, 0) + 1);
        }

        for (Map.Entry<ResourceLocation, Integer> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
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
        AspectList total = new AspectList();

        for (EssentiaJarBlockEntity jar : jars) {
            total.addAll(jar.aspects());
        }

        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
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
        if (aspect == null) {
            return false;
        }

        for (EssentiaJarBlockEntity jar : jars) {
            int removed = jar.aspects().removeUpTo(aspect, 1);

            if (removed > 0) {
                jar.setChangedAndSync();
                return true;
            }
        }

        return false;
    }

    public static ArcanePedestalBlockEntity findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId) {
        if (componentId == null) {
            return null;
        }

        for (ArcanePedestalBlockEntity pedestal : pedestals) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(pedestal.stored().getItem());

            if (componentId.equals(id)) {
                return pedestal;
            }
        }

        return null;
    }

    public static boolean consumeSingleComponent(List<ArcanePedestalBlockEntity> pedestals, ResourceLocation componentId) {
        ArcanePedestalBlockEntity pedestal = findComponentPedestal(pedestals, componentId);

        if (pedestal == null) {
            return false;
        }

        ItemStack stack = pedestal.stored();
        ItemStack container = stack.getCraftingRemainingItem();

        if (!container.isEmpty()) {
            pedestal.setStored(container);
        } else {
            pedestal.setStored(ItemStack.EMPTY);
        }

        return true;
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

        if (progress % 30 == 0) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, matrixPos.getX() + 0.5D, matrixPos.getY() + 1.05D, matrixPos.getZ() + 0.5D, 2, 0.15D, 0.15D, 0.15D, 0.01D);
        }
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

    public static void instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report) {
        instabilityEvent(level, matrixPos, player, recipe, report, 0);
    }

    public static void instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers) {
        int instability = calculatedInstability(recipe, report, matrixStabilizers);
        instabilityEvent(level, matrixPos, player, recipe, report, matrixStabilizers, instability);
    }

    public static void instabilityEvent(Level level, BlockPos matrixPos, Player player, InfusionRecipe recipe, InfusionStructureReport report, int matrixStabilizers, int explicitInstability) {
        InfusionInstabilityEvents.maybeTrigger(level, matrixPos, player, recipe, report, explicitInstability);
    }

    public static String componentText(InfusionRecipe recipe) {
        StringBuilder builder = new StringBuilder();

        for (ResourceLocation id : recipe.components()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(id);
        }

        return builder.toString();
    }

    public static String aspectText(InfusionRecipe recipe) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Aspect, Integer> entry : recipe.aspectCost().entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(entry.getKey().displayName()).append(" ").append(entry.getValue());
        }

        return builder.toString();
    }
}
