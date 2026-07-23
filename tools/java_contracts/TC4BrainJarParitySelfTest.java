import com.darkifov.thaumcraft.blockentity.TC4BrainJarParity;

public final class TC4BrainJarParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void close(double actual, double expected, double eps, String message) {
        if (Math.abs(actual - expected) > eps) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        require("11.64.15".equals(TC4BrainJarParity.CONTRACT_VERSION), "contract version");
        require(TC4BrainJarParity.clampAtTickStart(2010) == 2000, "upper tick-start clamp");
        require(TC4BrainJarParity.clampAtTickStart(1999) == 1999, "normal tick-start value");
        require(TC4BrainJarParity.clampAtTickStart(-3) == -3, "original negative-value behavior");
        require(TC4BrainJarParity.mayAttract(1999, 0), "attraction below capacity");
        require(!TC4BrainJarParity.mayAttract(2000, 0), "no attraction at capacity");
        require(!TC4BrainJarParity.mayAbsorb(100, 1), "eat delay blocks absorption");
        require(TC4BrainJarParity.randomReleaseBound(0) == 1, "zero-XP nextInt bound");
        require(TC4BrainJarParity.randomReleaseBound(63) == 64, "63-XP nextInt bound");
        require(TC4BrainJarParity.randomReleaseBound(2000) == 64, "large-XP nextInt bound");
        require(TC4BrainJarParity.comparatorOutput(0) == 0, "empty comparator");
        require(TC4BrainJarParity.comparatorOutput(1) == 1, "non-empty comparator floor");
        require(TC4BrainJarParity.comparatorOutput(1000) == 8, "half-full comparator");
        require(TC4BrainJarParity.comparatorOutput(2000) == 15, "full comparator");

        TC4BrainJarParity.Pull pull = TC4BrainJarParity.pull(0.5, 0.5, 0.5, 1.5, 0.5, 0.5);
        double scaledDistance = 1.0 / 7.0;
        double strength = (1.0 - scaledDistance) * (1.0 - scaledDistance);
        close(pull.x(), -strength * 0.15, 1.0E-12, "horizontal pull");
        close(pull.y(), 0.0, 1.0E-12, "vertical pull at same height");
        close(pull.z(), 0.0, 1.0E-12, "z pull at same depth");
        require(TC4BrainJarParity.pull(0.5, 0.5, 0.5, 0.5, 0.5, 0.5)
                        == TC4BrainJarParity.Pull.ZERO,
                "zero-distance guard");

        close(TC4BrainJarParity.fullParticleGreen(0.0F), 0.4, 1.0E-6, "particle green min");
        close(TC4BrainJarParity.fullParticleGreen(1.0F), 0.5, 1.0E-6, "particle green max");
        close(TC4BrainJarParity.fullParticleBlue(0.0F), 0.3, 1.0E-6, "particle blue min");
        close(TC4BrainJarParity.fullParticleBlue(1.0F), 0.5, 1.0E-6, "particle blue max");
        close(TC4BrainJarParity.bob(0), 0.03, 1.0E-6, "brain bob base");

        require(TC4BrainJarParity.ITEM_MAX_STACK == 64, "item stack size");
        require(TC4BrainJarParity.ITEM_WARP == 1, "item warp");
        require(TC4BrainJarParity.RESEARCH_WARP == 3, "research warp");
        require(TC4BrainJarParity.BLOCK_LIGHT_LEVEL == 9, "block light");
        require(TC4BrainJarParity.BLOCK_EXPLOSION_RESISTANCE == 0.0F, "default original resistance");
        require(TC4BrainJarParity.ENCHANT_POWER_BONUS == 2.0F, "enchant bonus");
        System.out.println("TC4BrainJarParitySelfTest: PASS");
    }
}
