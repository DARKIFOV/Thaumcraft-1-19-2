package com.darkifov.thaumcraft.client;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.AuraNodeModifier;
import com.darkifov.thaumcraft.aura.AuraNodeType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stage683-702: shared original TC4 node visual atlas ledger.
 * The 1.7.10 node renderer and revealer HUD both sample the same misc/nodes.png
 * sheet: 32 columns, 64px cells, 2048px sheet.  Keeping the mapping in one
 * place prevents the HUD and in-world renderer from drifting to different
 * type strips or cropped 32px adapter frames.
 */
public final class TC4AuraNodeHudParity {
    public static final ResourceLocation ORIGINAL_HUD = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/gui/thaumcraft_core_original/hud.png");
    public static final ResourceLocation ORIGINAL_NODES = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/nodes.png");
    public static final ResourceLocation ORIGINAL_NODE_BUBBLE = new ResourceLocation(ThaumcraftMod.MOD_ID, "textures/original/thaumcraft4/misc/node_bubble.png");
    public static final int NODE_SHEET_FRAMES = 32;
    public static final int NODE_FRAME_PIXELS = 64;
    public static final int NODE_SHEET_PIXELS = 2048;
    public static final int NODE_BUBBLE_PIXELS = 256;
    public static final float NODE_SHEET_CELL_UV = 1.0F / NODE_SHEET_FRAMES;
    public static final int HUD_RING_U = 0;
    public static final int HUD_RING_V = 0;
    public static final int HUD_RING_W = 48;
    public static final int HUD_RING_H = 48;
    public static final int HUD_BAR_U = 70;
    public static final int HUD_BAR_V = 0;
    public static final int HUD_BAR_W = 10;
    public static final int HUD_BAR_H = 43;
    public static final int HUD_NODE_SIZE = 32;
    public static final int HUD_BUBBLE_SIZE = 42;
    public static final int HUD_MAX_ASPECTS = 6;
    public static final int HUD_ASPECT_ICON_SIZE = 16;
    public static final int HUD_NODE_CENTER_OFFSET_X = -26;
    public static final int HUD_NODE_CENTER_OFFSET_Y = 1;
    public static final int HUD_ASPECT_RING_RADIUS = 17;
    public static final int HUD_ASPECT_COLUMN_X = 16;
    public static final int HUD_ASPECT_COUNT_X = 35;
    public static final int HUD_ASPECT_COLUMN_START_Y_OFFSET = -18;
    public static final int HUD_ASPECT_COLUMN_STEP = 18;
    public static final float HUD_BUBBLE_ALPHA = 0.62F;
    public static final float WORLD_BUBBLE_ALPHA = 0.42F;

    private TC4AuraNodeHudParity() {
    }

    public static int stripFor(AuraNodeType type) {
        if (type == null) {
            return 1;
        }
        return switch (type) {
            case UNSTABLE -> 6;
            case DARK -> 2;
            case TAINTED -> 5;
            case PURE -> 4;
            case HUNGRY -> 3;
            default -> 1;
        };
    }

    public static int frameOffsetFor(AuraNodeModifier modifier) {
        if (modifier == null) {
            return 0;
        }
        return switch (modifier) {
            case BRIGHT -> 5;
            case PALE -> 9;
            case FADING -> 13;
            default -> 0;
        };
    }

    public static float alphaFor(AuraNodeModifier modifier, float normalAlpha) {
        if (modifier == AuraNodeModifier.FADING) {
            return normalAlpha * 0.45F;
        }
        if (modifier == AuraNodeModifier.BRIGHT) {
            return Math.min(1.0F, normalAlpha * 1.18F);
        }
        return normalAlpha;
    }

    /**
     * v7.62: one ordering source for the revealer HUD and scan GUI.
     * TC4 presents the strongest node aspects first in the visible HUD cluster;
     * ties are stable by legacy aspect id so the column/ring do not shuffle every
     * render tick.
     */
    public static List<AspectStack> sortedAspectsForHud(Iterable<AspectStack> raw) {
        List<AspectStack> result = new ArrayList<>();
        if (raw != null) {
            for (AspectStack stack : raw) {
                if (stack != null && stack.amount() > 0) {
                    result.add(stack);
                }
            }
        }
        result.sort(Comparator.comparingInt(AspectStack::amount).reversed().thenComparing(stack -> stack.aspect().id()));
        return result;
    }

    public static int ringIconX(int centerX, int iconIndex, int visibleCount) {
        double angle = -Math.PI / 2.0D + iconIndex * (Math.PI * 2.0D / Math.max(1, visibleCount));
        return centerX + (int) Math.round(Math.cos(angle) * HUD_ASPECT_RING_RADIUS) - HUD_ASPECT_ICON_SIZE / 2;
    }

    public static int ringIconY(int centerY, int iconIndex, int visibleCount) {
        double angle = -Math.PI / 2.0D + iconIndex * (Math.PI * 2.0D / Math.max(1, visibleCount));
        return centerY + (int) Math.round(Math.sin(angle) * HUD_ASPECT_RING_RADIUS) - HUD_ASPECT_ICON_SIZE / 2;
    }

    public static int stripFor(String type) {
        if (type == null) {
            return 1;
        }
        try {
            return stripFor(AuraNodeType.valueOf(type.toUpperCase(java.util.Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return 1;
        }
    }
}
