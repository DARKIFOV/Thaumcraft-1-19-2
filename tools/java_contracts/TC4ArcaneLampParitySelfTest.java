import com.darkifov.thaumcraft.block.TC4ArcaneLampParity;

public final class TC4ArcaneLampParitySelfTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check("11.64.22".equals(TC4ArcaneLampParity.CONTRACT_VERSION), "version");
        check(TC4ArcaneLampParity.triangularOffset(15, 0) == 15, "+15");
        check(TC4ArcaneLampParity.triangularOffset(0, 15) == -15, "-15");
        check(TC4ArcaneLampParity.clampSampledY(100, 20, -64) == 24, "surface+4");
        check(TC4ArcaneLampParity.clampSampledY(-100, 20, -64) == 5, "minimum 5");
        check(!TC4ArcaneLampParity.insideBuildHeight(320, -64, 320), "upper exclusive");
        check(TC4ArcaneLampParity.shouldPlaceLampLight(true, false, 8, true), "lamp brightness 8");
        check(!TC4ArcaneLampParity.shouldPlaceLampLight(true, false, 9, true), "lamp brightness 9");
        check(!TC4ArcaneLampParity.shouldPlaceLampLight(true, true, 0, true), "own marker exclusion");
        check(TC4ArcaneLampParity.boreDistance(31) == 62, "distance 62");
        check(TC4ArcaneLampParity.boreLateralOffset(0) == 3, "phase 0");
        check(TC4ArcaneLampParity.boreLateralOffset(2) == 0, "phase 1");
        check(TC4ArcaneLampParity.boreLateralOffset(4) == -3, "phase 2");
        check(TC4ArcaneLampParity.boreVerticalOffset(6, false) == -2, "phase 3 horizontal");
        check(TC4ArcaneLampParity.boreVerticalOffset(6, true) == 0, "phase 3 vertical");
        check(TC4ArcaneLampParity.shouldPlaceBoreLight(true, false, 14, true), "bore brightness 14");
        check(!TC4ArcaneLampParity.shouldPlaceBoreLight(true, false, 15, true), "bore brightness 15");
        System.out.println("TC4ArcaneLampParitySelfTest: PASS");
    }
}
