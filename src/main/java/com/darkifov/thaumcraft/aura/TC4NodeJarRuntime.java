package com.darkifov.thaumcraft.aura;

import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Exact TC4 node profile capture rules used by the Node in a Jar ritual. */
public final class TC4NodeJarRuntime {
    public static final String TAG_NODE_JAR = "TC4NodeJar";
    public static final float MODIFIER_DAMAGE_CHANCE = 0.75F;
    public static final int ORIGINAL_PRESERVATION_PERCENT = 100;

    private TC4NodeJarRuntime() {
    }

    public static boolean hasNode(CompoundTag itemTag) {
        return itemTag != null && itemTag.contains(TAG_NODE_JAR);
    }

    /**
     * WandManager#replaceNodeJar copies current/base aspects exactly and only
     * has a 75% chance to weaken the modifier by one step. No aspect scaling is
     * performed by TC4, so early rebuild PreservationPercent tags are retained
     * only as migration metadata and new captures write 100.
     */
    public static CompoundTag capture(Level level, AuraNodeBlockEntity node) {
        RandomSource random = level == null ? RandomSource.create() : level.random;
        CompoundTag nodeTag = node.saveNodeJarTag();
        nodeTag.putBoolean("Energized", false);
        nodeTag.putBoolean("Jarred", true);
        nodeTag.putInt("PreservationPercent", ORIGINAL_PRESERVATION_PERCENT);
        if (random.nextFloat() < MODIFIER_DAMAGE_CHANCE) {
            nodeTag.putString("NodeModifier", weakenModifier(nodeTag.getString("NodeModifier")));
        }
        return nodeTag;
    }

    static String weakenModifier(String original) {
        AuraNodeModifier modifier = AuraNodeModifier.fromName(original);
        return switch (modifier) {
            case BRIGHT -> AuraNodeModifier.NORMAL.name();
            case NORMAL -> AuraNodeModifier.PALE.name();
            case PALE -> AuraNodeModifier.FADING.name();
            case FADING -> AuraNodeModifier.FADING.name();
        };
    }
}
