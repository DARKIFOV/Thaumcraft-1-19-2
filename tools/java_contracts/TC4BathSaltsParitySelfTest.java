import com.darkifov.thaumcraft.warp.TC4BathSaltsParity;

public final class TC4BathSaltsParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void close(double expected, double actual, double epsilon, String message) {
        require(Math.abs(expected - actual) <= epsilon,
                message + ": expected=" + expected + " actual=" + actual);
    }

    public static void main(String[] args) {
        require("11.64.14".equals(TC4BathSaltsParity.CONTRACT_VERSION), "contract version");
        require(TC4BathSaltsParity.ITEM_ENTITY_LIFESPAN_TICKS == 200, "item lifespan");
        require(TC4BathSaltsParity.convertsExpiredItem(true, 0), "source water conversion");
        require(!TC4BathSaltsParity.convertsExpiredItem(true, 1), "flowing water rejection");
        require(!TC4BathSaltsParity.convertsExpiredItem(false, 0), "non-vanilla-fluid rejection");

        require(TC4BathSaltsParity.wardDurationTicks(0) == 32000, "zero warp duration");
        require(TC4BathSaltsParity.wardDurationTicks(36) == 32000, "capped duration");
        require(TC4BathSaltsParity.wardDurationTicks(49) == 28571, "sqrt duration");
        require(TC4BathSaltsParity.wardDurationTicks(100) == 20000, "100 warp duration");
        require(TC4BathSaltsParity.wardDurationTicks(10000) == 2000, "high warp duration");

        close(1.0D, TC4BathSaltsParity.bubbleYOffset(0), 1.0E-12, "source bubble height");
        close(0.125D, TC4BathSaltsParity.bubbleYOffset(7), 1.0E-12, "level-seven bubble height");
        close(0.0D, TC4BathSaltsParity.bubbleYOffset(8), 1.0E-12, "falling bubble height");
        require(TC4BathSaltsParity.bubbleLifetime(0.0D) == 42, "maximum bubble lifetime");
        require(TC4BathSaltsParity.bubbleLifetime(0.999999D) == 10, "minimum bubble lifetime");
        require(TC4BathSaltsParity.playsPopSound(0), "pop sound roll zero");
        require(!TC4BathSaltsParity.playsPopSound(1), "pop sound nonzero");
        close(0.10D, TC4BathSaltsParity.popVolume(0.0F), 1.0E-6, "minimum volume");
        close(0.20D, TC4BathSaltsParity.popVolume(1.0F), 1.0E-6, "maximum volume");
        close(0.90D, TC4BathSaltsParity.popPitch(0.0F), 1.0E-6, "minimum pitch");
        close(1.05D, TC4BathSaltsParity.popPitch(1.0F), 1.0E-6, "maximum pitch");

        require(TC4BathSaltsParity.RECIPE_COGNITIO == 6
                && TC4BathSaltsParity.RECIPE_AURAM == 6
                && TC4BathSaltsParity.RECIPE_ORDO == 6
                && TC4BathSaltsParity.RECIPE_SANO == 6, "crucible aspects");
        require(TC4BathSaltsParity.RESEARCH_COGNITIO == 3
                && TC4BathSaltsParity.RESEARCH_AURAM == 3
                && TC4BathSaltsParity.RESEARCH_ORDO == 3
                && TC4BathSaltsParity.RESEARCH_SANO == 3, "research aspects");
        require(TC4BathSaltsParity.RESEARCH_X == -4
                && TC4BathSaltsParity.RESEARCH_Y == -4
                && TC4BathSaltsParity.RESEARCH_COMPLEXITY == 2, "research placement");
        require(TC4BathSaltsParity.wizardEmeraldCost(0) == 5
                && TC4BathSaltsParity.wizardEmeraldCost(1) == 6
                && TC4BathSaltsParity.wizardEmeraldCost(2) == 7, "wizard price range");

        boolean rejected = false;
        try {
            TC4BathSaltsParity.wizardEmeraldCost(3);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        require(rejected, "invalid wizard roll rejection");
        System.out.println("TC4BathSaltsParitySelfTest: PASS");
    }
}
