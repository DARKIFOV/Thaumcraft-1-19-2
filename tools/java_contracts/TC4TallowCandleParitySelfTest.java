import com.darkifov.thaumcraft.block.TC4TallowCandleParity;

import java.util.List;

public final class TC4TallowCandleParitySelfTest {
    private TC4TallowCandleParitySelfTest() {
    }

    public static void main(String[] args) {
        require("11.64.17".equals(TC4TallowCandleParity.CONTRACT_VERSION), "contract version");
        int[] colors = {
                0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3,
                0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
                0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192,
                0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B
        };
        String[] paths = {
                "tallow_candle", "tallow_candle_orange", "tallow_candle_magenta",
                "tallow_candle_light_blue", "tallow_candle_yellow", "tallow_candle_lime",
                "tallow_candle_pink", "tallow_candle_gray", "tallow_candle_light_gray",
                "tallow_candle_cyan", "tallow_candle_purple", "tallow_candle_blue",
                "tallow_candle_brown", "tallow_candle_green", "tallow_candle_red",
                "tallow_candle_black"
        };
        for (int i = 0; i < colors.length; i++) {
            require(TC4TallowCandleParity.color(i) == colors[i], "color " + i);
            require(TC4TallowCandleParity.registryPath(i).equals(paths[i]), "path " + i);
            require(TC4TallowCandleParity.legacyMetadata(paths[i]) == i, "round trip " + i);
            require(TC4TallowCandleParity.isOriginalArcanePechOfferColor(i) == (i < 15),
                    "Pech offer bound " + i);
        }
        require(TC4TallowCandleParity.BLOCK_LIGHT_LEVEL == 14, "light level");
        require(TC4TallowCandleParity.CRAFT_RESULT_COUNT == 3, "craft count");
        require(TC4TallowCandleParity.worldSeed(7, 11, -3) == -26, "world seed formula");

        List<TC4TallowCandleParity.Drip> first = TC4TallowCandleParity.drips(7, 11, -3);
        List<TC4TallowCandleParity.Drip> second = TC4TallowCandleParity.drips(7, 11, -3);
        require(first.equals(second), "drip determinism");
        require(first.size() >= 1 && first.size() <= 5, "drip count");
        for (int i = 0; i < first.size(); i++) {
            TC4TallowCandleParity.Drip drip = first.get(i);
            require(drip.index() == i, "drip index");
            require(drip.xAxis() == ((i & 1) == 0), "alternating axis");
            require(drip.location() >= 2 && drip.location() <= 3, "drip location");
            require(drip.heightPixels() >= 1 && drip.heightPixels() <= 3, "drip height");
        }

        boolean rejected = false;
        try {
            TC4TallowCandleParity.color(16);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, "invalid metadata rejection");
        System.out.println("TC4TallowCandleParitySelfTest PASS");
    }

    private static void require(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
    }
}
