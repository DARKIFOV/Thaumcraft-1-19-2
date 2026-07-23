import com.darkifov.thaumcraft.block.TC4ArcaneLevitatorParity;

public final class TC4ArcaneLevitatorParitySelfTest {
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static boolean close(double a, double b) { return Math.abs(a - b) < 1.0e-12D; }
    private static boolean close(float a, float b) { return Math.abs(a - b) < 1.0e-6F; }

    public static void main(String[] args) {
        require("11.64.20".equals(TC4ArcaneLevitatorParity.CONTRACT_VERSION), "version");
        require(TC4ArcaneLevitatorParity.stackedMaximumRange(0) == 10, "single range");
        require(TC4ArcaneLevitatorParity.stackedMaximumRange(3) == 40, "stack range");
        require(TC4ArcaneLevitatorParity.stackedMaximumRange(-2) == 10, "negative clamp");
        require(close(TC4ArcaneLevitatorParity.nextVerticalVelocity(0.0D, false),
                TC4ArcaneLevitatorParity.LIFT_INCREMENT), "lift zero");
        require(close(TC4ArcaneLevitatorParity.nextVerticalVelocity(
                TC4ArcaneLevitatorParity.MAX_UPWARD_VELOCITY, false),
                TC4ArcaneLevitatorParity.MAX_UPWARD_VELOCITY), "strict cap");
        require(close(TC4ArcaneLevitatorParity.nextVerticalVelocity(-0.5D, true),
                -0.5D * TC4ArcaneLevitatorParity.SNEAK_DESCENT_MULTIPLIER), "sneak descent");
        require(close(TC4ArcaneLevitatorParity.nextVerticalVelocity(0.1D, true), 0.1D), "sneak upward unchanged");
        require(TC4ArcaneLevitatorParity.lowerSegmentContributes(false), "unpowered lower contributes");
        require(!TC4ArcaneLevitatorParity.lowerSegmentContributes(true), "powered lower stops");
        require(TC4ArcaneLevitatorParity.admitsEntity(true, false, false), "item");
        require(TC4ArcaneLevitatorParity.admitsEntity(false, true, false), "pushable");
        require(TC4ArcaneLevitatorParity.admitsEntity(false, false, true), "horse");
        require(!TC4ArcaneLevitatorParity.admitsEntity(false, false, false), "reject");
        require(!TC4ArcaneLevitatorParity.connectsRedstoneOnLegacySide(0), "down no connect");
        require(!TC4ArcaneLevitatorParity.connectsRedstoneOnLegacySide(1), "up no connect");
        require(TC4ArcaneLevitatorParity.connectsRedstoneOnLegacySide(2), "horizontal connect");
        require(TC4ArcaneLevitatorParity.connectsRedstoneOnLegacySide(5), "horizontal connect 5");
        require(!TC4ArcaneLevitatorParity.connectsRedstoneOnLegacySide(6), "invalid side");
        require(TC4ArcaneLevitatorParity.ACTIVE_GLOW_LEGACY_BRIGHTNESS == 180, "legacy brightness");
        require(TC4ArcaneLevitatorParity.ACTIVE_GLOW_BLOCK_LIGHT == 11, "modern block light");
        require(TC4ArcaneLevitatorParity.WORLD_SIDE_GLOW == 0xDD11FF, "world side color");
        require(TC4ArcaneLevitatorParity.INVENTORY_SIDE_GLOW == 0xEECCFF, "inventory side color");
        require(close(TC4ArcaneLevitatorParity.particleRed(), 0.2F), "particle red");
        require(close(TC4ArcaneLevitatorParity.particleGreen(0.0F), 0.7F), "particle green low");
        require(close(TC4ArcaneLevitatorParity.particleGreen(1.0F), 1.0F), "particle green high");
        require(close(TC4ArcaneLevitatorParity.particleBlue(), 0.2F), "particle blue");
        require(TC4ArcaneLevitatorParity.PARTICLE_LIFETIME == 18, "particle lifetime");
        require(TC4ArcaneLevitatorParity.PARTICLE_GATE_DENOMINATOR == 6, "particle denominator");
        require(TC4ArcaneLevitatorParity.PARTICLE_ALL_THRESHOLD == 4, "all particles threshold");
        require(TC4ArcaneLevitatorParity.PARTICLE_DECREASED_THRESHOLD == 2, "decreased threshold");
        require(TC4ArcaneLevitatorParity.PARTICLE_MINIMAL_THRESHOLD == 0, "minimal threshold");
        System.out.println("TC4ArcaneLevitatorParitySelfTest: PASS");
    }
}
