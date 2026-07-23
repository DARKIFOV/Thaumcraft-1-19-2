import com.darkifov.thaumcraft.blockentity.TC4ArcaneBoreParity;

public final class TC4ArcaneBoreParitySelfTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.000001F, message + ": " + actual);
    }

    public static void main(String[] args) {
        check("11.64.24".equals(TC4ArcaneBoreParity.CONTRACT_VERSION), "contract version");
        check(TC4ArcaneBoreParity.BASE_RADIUS == 2 && TC4ArcaneBoreParity.MAX_DEPTH == 64, "geometry constants");
        check(TC4ArcaneBoreParity.SPIRAL_STEP_DEGREES == 2, "spiral step");
        check(TC4ArcaneBoreParity.width(0) == 5 && TC4ArcaneBoreParity.width(3) == 11, "width");
        check(TC4ArcaneBoreParity.pickaxeIsNearBroken(99, 100), "near-broken true boundary");
        check(!TC4ArcaneBoreParity.pickaxeIsNearBroken(98, 100), "near-broken false boundary");
        check(TC4ArcaneBoreParity.miningDelay(5.0F, 2, true) == 8, "accelerated delay");
        check(TC4ArcaneBoreParity.miningDelay(5.0F, 2, false) == 32, "normal delay x4");
        close(TC4ArcaneBoreParity.addVisCredit(0.0F, 100), 20.0F, "vis credit");
        close(TC4ArcaneBoreParity.addEssentiaCredit(3.0F), 23.0F, "essentia credit");
        close(TC4ArcaneBoreParity.consumeAcceleratedBlock(2.0F), 1.0F, "credit consumption");
        close(TC4ArcaneBoreParity.initialRadiusIncrement(0), 2.0F / 360.0F, "radius increment");
        TC4ArcaneBoreParity.SpiralLane lane = TC4ArcaneBoreParity.nextLane(
                10, 20, 30, 1, 0, 0, 0, 0, 0.0F, 0.0F, 0, 0, 0);
        check(lane.spiral() % 2 == 0, "spiral parity");
        close(lane.radiusIncrement(), 2.0F / 360.0F, "lane radius increment");
        TC4ArcaneBoreParity.SpiralLane vertical = TC4ArcaneBoreParity.nextLane(
                10, 20, 30, 0, 1, 0, 2, 0, 0.0F, 0.0F, 0, 0, 0);
        check(vertical.spiral() % 2 == 0, "vertical lane");
        System.out.println("TC4ArcaneBoreParitySelfTest: PASS");
    }
}
