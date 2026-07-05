package com.darkifov.thaumcraft;

import java.util.Map;

public final class AspectVisuals {
    private AspectVisuals() {
    }

    public static Aspect dominant(AspectList list) {
        if (list == null || list.entries().isEmpty()) {
            return Aspect.AURAM;
        }

        Aspect best = Aspect.AURAM;
        int amount = -1;

        for (Map.Entry<Aspect, Integer> entry : list.entries().entrySet()) {
            if (entry.getValue() > amount) {
                best = entry.getKey();
                amount = entry.getValue();
            }
        }

        return best;
    }

    public static int dominantColor(AspectList list, int alpha) {
        return AspectColor.argb(dominant(list), alpha);
    }

    public static int blendedColor(AspectList list, int alpha) {
        if (list == null || list.entries().isEmpty()) {
            return AspectColor.argb(Aspect.AURAM, alpha);
        }

        int total = Math.max(1, list.totalAmount());
        int r = 0;
        int g = 0;
        int b = 0;

        for (Map.Entry<Aspect, Integer> entry : list.entries().entrySet()) {
            int rgb = AspectColor.rgb(entry.getKey());
            int amount = Math.max(0, entry.getValue());
            r += ((rgb >> 16) & 255) * amount;
            g += ((rgb >> 8) & 255) * amount;
            b += (rgb & 255) * amount;
        }

        r /= total;
        g /= total;
        b /= total;

        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return (safeAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    public static float fillRatio(AspectList list, int capacity) {
        if (list == null || capacity <= 0) {
            return 0.0F;
        }

        return Math.max(0.0F, Math.min(1.0F, list.totalAmount() / (float) capacity));
    }
}
