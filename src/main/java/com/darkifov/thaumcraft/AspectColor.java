package com.darkifov.thaumcraft;

public final class AspectColor {
    private AspectColor() {
    }

    public static int rgb(Aspect aspect) {
        return aspect == null ? 0xFFFFFF : aspect.colorValue() & 0xFFFFFF;
    }

    public static int argb(Aspect aspect) {
        return argb(aspect, 255);
    }

    public static int argb(Aspect aspect, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return (safeAlpha << 24) | rgb(aspect);
    }

    public static int dim(Aspect aspect, int alpha, float factor) {
        int rgb = rgb(aspect);
        int r = Math.max(0, Math.min(255, (int) (((rgb >> 16) & 255) * factor)));
        int g = Math.max(0, Math.min(255, (int) (((rgb >> 8) & 255) * factor)));
        int b = Math.max(0, Math.min(255, (int) ((rgb & 255) * factor)));
        return (Math.max(0, Math.min(255, alpha)) << 24) | (r << 16) | (g << 8) | b;
    }

    public static int mix(Aspect first, Aspect second, int alpha) {
        int a = rgb(first);
        int b = rgb(second);

        int r = (((a >> 16) & 255) + ((b >> 16) & 255)) / 2;
        int g = (((a >> 8) & 255) + ((b >> 8) & 255)) / 2;
        int blue = ((a & 255) + (b & 255)) / 2;

        return (Math.max(0, Math.min(255, alpha)) << 24) | (r << 16) | (g << 8) | blue;
    }

    public static int readableText(Aspect aspect) {
        int rgb = rgb(aspect);
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        int luminance = (r * 299 + g * 587 + b * 114) / 1000;
        return luminance > 145 ? 0xFF1A1208 : 0xFFFFFFFF;
    }

    public static String hex(Aspect aspect) {
        return String.format("#%06X", rgb(aspect));
    }
}
