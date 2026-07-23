import com.darkifov.thaumcraft.block.TC4ArcanePressurePlateParity;

public final class TC4ArcanePressurePlateParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.21".equals(TC4ArcanePressurePlateParity.CONTRACT_VERSION), "version");
        require(TC4ArcanePressurePlateParity.clampSetting(-4) == 0, "lower clamp");
        require(TC4ArcanePressurePlateParity.clampSetting(9) == 2, "upper clamp");
        require(TC4ArcanePressurePlateParity.nextSetting(0) == 1
                && TC4ArcanePressurePlateParity.nextSetting(1) == 2
                && TC4ArcanePressurePlateParity.nextSetting(2) == 0, "setting cycle");

        require(TC4ArcanePressurePlateParity.shouldTrigger(0, false, false, false), "mode0 entity");
        require(TC4ArcanePressurePlateParity.shouldTrigger(0, true, false, true), "mode0 player");
        require(!TC4ArcanePressurePlateParity.shouldTrigger(1, true, false, true), "mode1 authorized");
        require(TC4ArcanePressurePlateParity.shouldTrigger(1, true, false, false), "mode1 outsider");
        require(TC4ArcanePressurePlateParity.shouldTrigger(1, false, false, false), "mode1 non-player");
        require(TC4ArcanePressurePlateParity.shouldTrigger(2, true, false, true), "mode2 authorized");
        require(!TC4ArcanePressurePlateParity.shouldTrigger(2, true, false, false), "mode2 outsider");
        require(!TC4ArcanePressurePlateParity.shouldTrigger(2, false, false, false), "mode2 non-player");
        require(!TC4ArcanePressurePlateParity.shouldTrigger(0, false, true, false), "ignore triggers");

        require(TC4ArcanePressurePlateParity.mayBindKey(true, false, true), "owner gold");
        require(TC4ArcanePressurePlateParity.mayBindKey(true, false, false), "owner iron");
        require(TC4ArcanePressurePlateParity.mayBindKey(false, true, false), "full iron");
        require(!TC4ArcanePressurePlateParity.mayBindKey(false, true, true), "full gold denied");
        require(!TC4ArcanePressurePlateParity.mayBindKey(false, false, false), "outsider denied");

        require(TC4ArcanePressurePlateParity.weakSignal(true) == 15
                && TC4ArcanePressurePlateParity.weakSignal(false) == 0, "weak signal");
        require(TC4ArcanePressurePlateParity.strongSignal(true, true) == 15
                && TC4ArcanePressurePlateParity.strongSignal(true, false) == 0, "strong signal");

        String loc = TC4ArcanePressurePlateParity.legacyLocation(-12, 64, 7);
        require("-12,64,7".equals(loc), "legacy location");
        require(TC4ArcanePressurePlateParity.locationMatches(loc, -12, 64, 7), "location match");
        require(!TC4ArcanePressurePlateParity.locationMatches(loc, -12, 65, 7), "location mismatch");
        require("x -12, z 7, y 64".equals(TC4ArcanePressurePlateParity.tooltipLocation(loc)),
                "tooltip order");
        require("bad".equals(TC4ArcanePressurePlateParity.tooltipLocation("bad")), "malformed tooltip");

        require(TC4ArcanePressurePlateParity.CHECK_INTERVAL_TICKS == 20, "tick interval");
        require(TC4ArcanePressurePlateParity.WARDED_HARDNESS == -1.0F, "warded hardness");
        require(TC4ArcanePressurePlateParity.EXPLOSION_RESISTANCE == 999.0F, "resistance");
        require(TC4ArcanePressurePlateParity.OUTLINE_UNPRESSED_HEIGHT == 0.0625D
                && TC4ArcanePressurePlateParity.OUTLINE_PRESSED_HEIGHT == 0.03125D, "outline heights");
        System.out.println("TC4ArcanePressurePlateParitySelfTest: PASS");
    }
}
