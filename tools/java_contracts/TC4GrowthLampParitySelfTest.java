import com.darkifov.thaumcraft.block.TC4GrowthLampParity;

public final class TC4GrowthLampParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.29".equals(TC4GrowthLampParity.CONTRACT_VERSION), "contract version");
        require(TC4GrowthLampParity.RADIUS == 6, "radius");
        require(TC4GrowthLampParity.DIAMETER == 13, "diameter");
        require(TC4GrowthLampParity.columnCount() == 169, "column count");
        require(TC4GrowthLampParity.CHARGES_PER_ESSENTIA == 100, "charge refill");
        require(TC4GrowthLampParity.DRAW_INTERVAL_TICKS == 5, "draw interval");
        require(TC4GrowthLampParity.suction(false, -1, true) == 128, "empty suction");
        require(TC4GrowthLampParity.suction(true, 100, true) == 0, "full reserve suction");
        require(TC4GrowthLampParity.suction(true, 0, true) == 128, "depleted suction");
        require(TC4GrowthLampParity.suction(false, -1, false) == 0, "wrong face suction");
        require(TC4GrowthLampParity.insideSphere(5, 0, 0), "inside sphere");
        require(!TC4GrowthLampParity.insideSphere(6, 0, 0), "strict sphere boundary");
        require(TC4GrowthLampParity.insideSphere(3, 3, 3), "diagonal sphere point");
        require(TC4GrowthLampParity.isActive(1) && !TC4GrowthLampParity.isActive(0), "active state");
        require(TC4GrowthLampParity.USES_SINGLE_NATURAL_TICK, "natural tick adaptation");
        require(TC4GrowthLampParity.ACTIVE_LIGHT == 15 && TC4GrowthLampParity.INACTIVE_LIGHT == 8,
                "dynamic light");
        System.out.println("TC4GrowthLampParitySelfTest: PASS");
    }
}
