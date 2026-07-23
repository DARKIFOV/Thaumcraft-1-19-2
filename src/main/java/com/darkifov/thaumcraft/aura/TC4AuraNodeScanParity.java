package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage723-742: original-style node scan text/data bridge for Thaumometer.
 *
 * This does not add new scan rewards or research gates.  It only centralizes the
 * TC4 node type/modifier/aspect formatting so Thaumometer, AuraNodeScan and HUD
 * adapters stop drifting into different modern debug strings.
 */
public final class TC4AuraNodeScanParity {
    public static final String ORIGINAL_AURA_NODE_SCAN_KEY = "ConfigBlocks.blockAiry, 1, 0";
    public static final String LEGACY_OBJECT_ID = "thaumcraft:aura_node";
    public static final int THAUMOMETER_USE_DURATION_TICKS = TC4ThaumometerParity.USE_DURATION_TICKS;
    public static final double THAUMOMETER_SCAN_RANGE = TC4ThaumometerParity.ENTITY_SCAN_RANGE;
    public static final double THAUMOMETER_ENTITY_EXPAND = TC4ThaumometerParity.ENTITY_TARGET_EXPAND;

    private TC4AuraNodeScanParity() {
    }

    public static Component header(AuraNodeBlockEntity node, boolean firstScan) {
        AuraNodeType type = AuraNodeType.fromName(node.nodeType());
        AuraNodeModifier modifier = AuraNodeModifier.fromName(node.nodeModifier());
        return Component.translatable(firstScan ? "thaumcraft.scan.node.new" : "thaumcraft.scan.node.known")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(modifier.translationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" "))
                .append(Component.translatable(type.translationKey()).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    }

    public static Component visLine(AuraNodeBlockEntity node) {
        MutableComponent line = Component.translatable("thaumcraft.scan.node.vis",
                        node.aspects().totalAmount(), node.baseAspects().totalAmount(), node.stability())
                .withStyle(ChatFormatting.GOLD);
        if (node.isStabilized()) {
            line.append(Component.literal(" | "))
                    .append(Component.translatable("thaumcraft.scan.node.stabilized").withStyle(ChatFormatting.GREEN));
        }
        if (node.isEnergized()) {
            line.append(Component.literal(" | "))
                    .append(Component.translatable("thaumcraft.scan.node.energized").withStyle(ChatFormatting.AQUA));
        }
        return line;
    }

    public static Component aspectLine(AuraNodeBlockEntity node) {
        List<AspectStack> stacks = sortedAspects(node);
        MutableComponent result = Component.translatable("thaumcraft.scan.aspects").withStyle(ChatFormatting.GRAY);
        if (stacks.isEmpty()) {
            return result.append(Component.translatable("thaumcraft.aspect.none").withStyle(ChatFormatting.DARK_GRAY));
        }
        boolean first = true;
        for (AspectStack stack : stacks) {
            if (!first) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            result.append(Component.translatable("thaumcraft.aspect.amount",
                            Component.translatable("aspect.thaumcraft." + stack.aspect().id()), stack.amount())
                    .withStyle(style -> style.withColor(stack.aspect().textColor())));
            first = false;
        }
        return result;
    }

    public static Component compactLine(AuraNodeBlockEntity node) {
        AuraNodeType type = AuraNodeType.fromName(node.nodeType());
        AuraNodeModifier modifier = AuraNodeModifier.fromName(node.nodeModifier());
        return Component.translatable("thaumcraft.scan.node.compact",
                        Component.translatable(modifier.translationKey()),
                        Component.translatable(type.translationKey()),
                        node.aspects().totalAmount(), node.baseAspects().totalAmount())
                .withStyle(ChatFormatting.AQUA);
    }

    public static List<AspectStack> sortedAspects(AuraNodeBlockEntity node) {
        List<AspectStack> stacks = new ArrayList<>(node.aspects().all());
        // v8.22: scan text and revealer HUD now use the same stable TC4-style ordering:
        // strongest aspects first, legacy aspect id as a deterministic tie-breaker.
        stacks.sort(Comparator.comparingInt(AspectStack::amount).reversed().thenComparing(stack -> stack.aspect().id()));
        return stacks;
    }


    /**
     * TC4 ScanManager.generateNodeAspects parity.  Scanning an aura node never
     * awards the node's entire live vis pool: each contained aspect contributes
     * max(4, amount / 10), followed by the original node-type bonus.
     */
    public static AspectList scanRewardAspects(AuraNodeBlockEntity node) {
        AspectList rewards = new AspectList();
        if (node == null) {
            return rewards;
        }

        for (java.util.Map.Entry<Aspect, Integer> entry : node.aspects().entries().entrySet()) {
            rewards.add(entry.getKey(), Math.max(4, entry.getValue() / 10));
        }

        switch (node.typedNodeType()) {
            case UNSTABLE -> rewards.add(Aspect.PERDITIO, 4);
            case HUNGRY -> rewards.add(Aspect.FAMES, 4);
            case TAINTED -> rewards.add(Aspect.VITIUM, 4);
            case PURE -> rewards.add(Aspect.SANO, 2).add(Aspect.ORDO, 2);
            case DARK -> rewards.add(Aspect.MORTUUS, 2).add(Aspect.TENEBRAE, 2);
            default -> {
            }
        }
        return rewards;
    }

    public static boolean isWithinScanRange(Player player, BlockPos pos) {
        if (player == null || pos == null) {
            return false;
        }
        double reach = THAUMOMETER_SCAN_RANGE + 0.75D;
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= reach * reach;
    }
}
