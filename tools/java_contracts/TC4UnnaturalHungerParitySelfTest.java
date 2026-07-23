import com.darkifov.thaumcraft.warp.TC4UnnaturalHungerParity;

public final class TC4UnnaturalHungerParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require(Math.abs(TC4UnnaturalHungerParity.exhaustionPerTick(0) - 0.025F) < 1.0E-6F,
                "level I exhaustion");
        require(Math.abs(TC4UnnaturalHungerParity.exhaustionPerTick(3) - 0.100F) < 1.0E-6F,
                "level IV exhaustion");
        require(TC4UnnaturalHungerParity.warpAmplifier(14) == 0, "warp 14");
        require(TC4UnnaturalHungerParity.warpAmplifier(15) == 1, "warp 15");
        require(TC4UnnaturalHungerParity.warpAmplifier(60) == 3, "warp cap");

        TC4UnnaturalHungerParity.Reduction reduced =
                TC4UnnaturalHungerParity.afterCurative(5000, 3);
        require(reduced.remainsActive() && reduced.duration() == 4400 && reduced.amplifier() == 2,
                "curative reduction");
        require(!TC4UnnaturalHungerParity.afterCurative(5000, 0).remainsActive(),
                "level zero removal");
        require(!TC4UnnaturalHungerParity.afterCurative(600, 3).remainsActive(),
                "strict duration gate");

        TC4UnnaturalHungerParity.Rgb white = TC4UnnaturalHungerParity.transform(1.0F, 1.0F, 1.0F);
        require(Math.abs(white.red() - 1.014F) < 1.0E-5F, "shader red");
        require(Math.abs(white.green() - 0.794F) < 1.0E-5F, "shader green");
        require(Math.abs(white.blue() - 0.794F) < 1.0E-5F, "shader blue");
        System.out.println("TC4 Unnatural Hunger parity self-test: PASS");
    }
}
