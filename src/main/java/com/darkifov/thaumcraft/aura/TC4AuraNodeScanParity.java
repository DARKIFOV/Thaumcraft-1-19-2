package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    public static final int THAUMOMETER_USE_DURATION_TICKS = 25;
    public static final double THAUMOMETER_SCAN_RANGE = 10.0D;
    public static final double THAUMOMETER_ENTITY_EXPAND = 0.5D;

    private TC4AuraNodeScanParity() {
    }

    public static Component header(AuraNodeBlockEntity node, boolean firstScan) {
        AuraNodeType type = AuraNodeType.fromName(node.nodeType());
        AuraNodeModifier modifier = AuraNodeModifier.fromName(node.nodeModifier());
        return Component.literal(firstScan ? "New aura node scan" : "Aura node")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" [" + modifier.displayName() + " " + type.displayName() + "]").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static Component visLine(AuraNodeBlockEntity node) {
        return Component.literal("Vis: " + node.aspects().totalAmount() + "/" + node.baseAspects().totalAmount()
                + "  Stability: " + node.stability()
                + (node.isStabilized() ? "  Stabilized" : "")
                + (node.isEnergized() ? "  Energized" : ""))
                .withStyle(ChatFormatting.GOLD);
    }

    public static Component aspectLine(AuraNodeBlockEntity node) {
        List<AspectStack> stacks = sortedAspects(node);
        if (stacks.isEmpty()) {
            return Component.literal("Aspects: none").withStyle(ChatFormatting.GRAY);
        }
        StringBuilder builder = new StringBuilder();
        for (AspectStack stack : stacks) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(stack.aspect().id()).append(' ').append(stack.amount());
        }
        return Component.literal("Aspects: " + builder).withStyle(ChatFormatting.GRAY);
    }

    public static List<AspectStack> sortedAspects(AuraNodeBlockEntity node) {
        List<AspectStack> stacks = new ArrayList<>(node.aspects().all());
        // v8.22: scan text and revealer HUD now use the same stable TC4-style ordering:
        // strongest aspects first, legacy aspect id as a deterministic tie-breaker.
        stacks.sort(Comparator.comparingInt(AspectStack::amount).reversed().thenComparing(stack -> stack.aspect().id()));
        return stacks;
    }

    public static boolean isWithinScanRange(Player player, BlockPos pos) {
        if (player == null || pos == null) {
            return false;
        }
        double reach = THAUMOMETER_SCAN_RANGE + 0.75D;
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= reach * reach;
    }
}
