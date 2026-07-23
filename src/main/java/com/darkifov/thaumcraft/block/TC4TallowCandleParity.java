package com.darkifov.thaumcraft.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Dependency-free TC4 4.2.3.5 contract for the complete Tallow Candle family.
 *
 * <p>The values come from {@code BlockCandle}, {@code BlockCandleItem},
 * {@code BlockCandleRenderer}, {@code Utils.colors}, {@code ConfigAspects},
 * {@code ConfigRecipes}, {@code ConfigResearch} and the arcane Pech offer list.
 * Production render and block registration use this class directly.</p>
 */
public final class TC4TallowCandleParity {
    public static final String CONTRACT_VERSION = "11.64.17";

    public static final int COLOR_COUNT = 16;
    public static final int ARCANE_PECH_OFFER_COLOR_COUNT = 15; // original loop is a < 15

    public static final float BLOCK_HARDNESS = 0.1F;
    public static final int BLOCK_LIGHT_LEVEL = 14; // setLightLevel(0.95F) -> floor(15 * 0.95)

    public static final double BODY_MIN = 6.0D / 16.0D;
    public static final double BODY_MAX = 10.0D / 16.0D;
    public static final double BODY_HEIGHT = 8.0D / 16.0D;
    public static final double WICK_MIN = 0.475D;
    public static final double WICK_MAX = 0.525D;
    public static final double WICK_BOTTOM = 8.0D / 16.0D;
    public static final double WICK_TOP = 10.0D / 16.0D;
    public static final double PARTICLE_XZ_OFFSET = 0.5D;
    public static final double PARTICLE_Y_OFFSET = 0.7D;

    public static final int DRIP_COUNT_BASE = 1;
    public static final int DRIP_COUNT_BOUND = 5;
    public static final int DRIP_LOCATION_BASE = 2;
    public static final int DRIP_LOCATION_BOUND = 2;
    public static final int DRIP_HEIGHT_BASE = 1;
    public static final int DRIP_HEIGHT_BOUND = 3;

    public static final int CRAFT_RESULT_COUNT = 3;
    public static final int OBJECT_LUX = 2;
    public static final int OBJECT_CORPUS = 1;
    public static final int OBJECT_PRAECANTATIO = 1;
    public static final int RESEARCH_CORPUS = 3;
    public static final int RESEARCH_PRAECANTATIO = 1;
    public static final int RESEARCH_X = -2;
    public static final int RESEARCH_Y = 0;
    public static final int RESEARCH_COMPLEXITY = 1;

    /** Exact {@code thaumcraft.common.lib.utils.Utils.colors} metadata order. */
    private static final int[] COLORS = {
            0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3,
            0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
            0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192,
            0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B
    };

    private static final String[] REGISTRY_PATHS = {
            "tallow_candle", "tallow_candle_orange", "tallow_candle_magenta",
            "tallow_candle_light_blue", "tallow_candle_yellow", "tallow_candle_lime",
            "tallow_candle_pink", "tallow_candle_gray", "tallow_candle_light_gray",
            "tallow_candle_cyan", "tallow_candle_purple", "tallow_candle_blue",
            "tallow_candle_brown", "tallow_candle_green", "tallow_candle_red",
            "tallow_candle_black"
    };

    private TC4TallowCandleParity() {
    }

    public static int color(int legacyMetadata) {
        if (legacyMetadata < 0 || legacyMetadata >= COLOR_COUNT) {
            throw new IllegalArgumentException("legacyMetadata must be in [0, 15]");
        }
        return COLORS[legacyMetadata];
    }

    public static float red(int legacyMetadata) {
        return ((color(legacyMetadata) >>> 16) & 0xFF) / 255.0F;
    }

    public static float green(int legacyMetadata) {
        return ((color(legacyMetadata) >>> 8) & 0xFF) / 255.0F;
    }

    public static float blue(int legacyMetadata) {
        return (color(legacyMetadata) & 0xFF) / 255.0F;
    }

    public static int legacyMetadata(String registryPath) {
        if (registryPath == null) {
            return 0;
        }
        for (int index = 0; index < REGISTRY_PATHS.length; index++) {
            if (REGISTRY_PATHS[index].equals(registryPath)) {
                return index;
            }
        }
        return 0;
    }

    public static String registryPath(int legacyMetadata) {
        if (legacyMetadata < 0 || legacyMetadata >= COLOR_COUNT) {
            throw new IllegalArgumentException("legacyMetadata must be in [0, 15]");
        }
        return REGISTRY_PATHS[legacyMetadata];
    }

    /** Preserves Java int overflow in the original expression {@code x + y * z}. */
    public static int worldSeed(int x, int y, int z) {
        return x + y * z;
    }

    /** Exact original sequence and bounds for coordinate-seeded wax drips. */
    public static List<Drip> drips(int x, int y, int z) {
        Random random = new Random(worldSeed(x, y, z));
        int count = DRIP_COUNT_BASE + random.nextInt(DRIP_COUNT_BOUND);
        List<Drip> result = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            boolean side = random.nextBoolean();
            int location = DRIP_LOCATION_BASE + random.nextInt(DRIP_LOCATION_BOUND);
            int heightPixels = DRIP_HEIGHT_BASE + random.nextInt(DRIP_HEIGHT_BOUND);
            result.add(new Drip(index, side, location, heightPixels));
        }
        return Collections.unmodifiableList(result);
    }

    public static boolean isOriginalArcanePechOfferColor(int legacyMetadata) {
        return legacyMetadata >= 0 && legacyMetadata < ARCANE_PECH_OFFER_COLOR_COUNT;
    }

    public record Drip(int index, boolean side, int location, int heightPixels) {
        public boolean xAxis() {
            return (index & 1) == 0;
        }
    }
}
