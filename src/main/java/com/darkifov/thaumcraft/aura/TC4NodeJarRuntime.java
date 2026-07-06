package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.block.NodeStabilizerBlock;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Stage132 helper for TC4 Node-in-a-Jar parity.
 *
 * TC4 does not move nodes for free: jarring a node damages / weakens it. This
 * runtime stores the original profile but applies a preservation penalty unless
 * an advanced stabilizer is active nearby.
 */
public final class TC4NodeJarRuntime {
    public static final String TAG_NODE_JAR = "TC4NodeJar";
    private static final int NORMAL_PRESERVATION_PERCENT = 75;
    private static final int ADVANCED_PRESERVATION_PERCENT = 92;

    private TC4NodeJarRuntime() {
    }

    public static boolean hasNode(CompoundTag itemTag) {
        return itemTag != null && itemTag.contains(TAG_NODE_JAR);
    }

    public static CompoundTag capture(Level level, AuraNodeBlockEntity node) {
        CompoundTag nodeTag = new CompoundTag();
        boolean advanced = level != null && NodeStabilizerBlock.stabilizerStrength(level, node.getBlockPos()) >= 2;
        int percent = advanced ? ADVANCED_PRESERVATION_PERCENT : NORMAL_PRESERVATION_PERCENT;

        AspectList jarAspects = scaled(node.aspects(), percent);
        AspectList jarBaseAspects = scaled(node.baseAspects(), percent);
        if (jarAspects.isEmpty()) {
            jarAspects.add(Aspect.AER, 1);
        }
        if (jarBaseAspects.isEmpty()) {
            jarBaseAspects.addAll(jarAspects);
        }

        nodeTag.putString("NodeType", node.nodeType());
        nodeTag.putString("NodeModifier", advanced ? node.nodeModifier() : weakenModifier(node.nodeModifier()));
        nodeTag.putInt("Stability", Math.max(15, node.stability() - (advanced ? 6 : 18)));
        nodeTag.putBoolean("Scanned", node.scanned());
        nodeTag.putBoolean("Energized", false);
        nodeTag.putInt("PreservationPercent", percent);
        nodeTag.put("Aspects", jarAspects.save());
        nodeTag.put("BaseAspects", jarBaseAspects.save());
        return nodeTag;
    }

    public static AspectList scaled(AspectList source, int percent) {
        AspectList result = new AspectList();
        for (var entry : source.entries().entrySet()) {
            int amount = Math.max(1, Math.round(entry.getValue() * (percent / 100.0F)));
            result.add(entry.getKey(), amount);
        }
        return result;
    }

    private static String weakenModifier(String original) {
        AuraNodeModifier modifier = AuraNodeModifier.fromName(original);
        if (modifier == AuraNodeModifier.BRIGHT) {
            return AuraNodeModifier.NORMAL.name();
        }
        if (modifier == AuraNodeModifier.NORMAL || modifier == AuraNodeModifier.PALE) {
            return AuraNodeModifier.PALE.name();
        }
        return AuraNodeModifier.FADING.name();
    }
}
