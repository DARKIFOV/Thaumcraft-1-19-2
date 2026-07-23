import com.darkifov.thaumcraft.wand.TC4WandComponentMath;

public final class TC4WandComponentMathSelfTest {
    private static void req(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static boolean close(float a, float b) { return Math.abs(a-b) < 0.0001F; }

    public static void main(String[] args) {
        req("11.64.32".equals(TC4WandComponentMath.CONTRACT_VERSION), "version");
        req(TC4WandComponentMath.capacityCentivis(25, false) == 2500, "wood wand capacity");
        req(TC4WandComponentMath.capacityCentivis(100, true) == 15000, "silverwood sceptre capacity");
        req(TC4WandComponentMath.capacityCentivis(250, false) == 25000, "staff capacity");
        req(close(TC4WandComponentMath.consumptionModifier(1.1F, false), 1.1F), "iron cap");
        req(close(TC4WandComponentMath.consumptionModifier(0.8F, true), 0.7F), "void sceptre");
        req(close(TC4WandComponentMath.consumptionModifier(0.1F, true), 0.1F), "modifier floor");
        req(TC4WandComponentMath.regenerationThresholdCentivis(7500) == 750, "10 percent threshold");
        req(TC4WandComponentMath.regenerationDeltaCentivis(1) == 100, "one displayed vis");
        System.out.println("TC4WandComponentMathSelfTest: PASS");
    }
}
