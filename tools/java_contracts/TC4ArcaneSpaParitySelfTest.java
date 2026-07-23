import com.darkifov.thaumcraft.block.TC4ArcaneSpaParity;

public final class TC4ArcaneSpaParitySelfTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check("11.64.23".equals(TC4ArcaneSpaParity.CONTRACT_VERSION), "contract version");
        check(TC4ArcaneSpaParity.CAPACITY_MB == 5000, "capacity");
        check(TC4ArcaneSpaParity.BUCKET_MB == 1000, "bucket");
        check(TC4ArcaneSpaParity.shouldRunCycle(0), "new spa must run immediately");
        check(!TC4ArcaneSpaParity.shouldRunCycle(39), "tick 39 must not run");
        check(TC4ArcaneSpaParity.shouldRunCycle(40), "tick 40 must run");
        check(!TC4ArcaneSpaParity.exposesAutomationSide(1), "top must be closed");
        check(TC4ArcaneSpaParity.exposesAutomationSide(0)
                && TC4ArcaneSpaParity.exposesAutomationSide(-1), "other/unknown sides must be open");
        check(TC4ArcaneSpaParity.canAcceptFilledContainer(4999, true), "partial final fill");
        check(!TC4ArcaneSpaParity.canAcceptFilledContainer(5000, true), "full tank");
        check(!TC4ArcaneSpaParity.canAcceptFilledContainer(0, false), "different fluid");
        check(TC4ArcaneSpaParity.acceptedFluidAmount(4500, 1000) == 500, "partial amount");
        check(TC4ArcaneSpaParity.CONSUME_BEFORE_PLACEMENT, "consume-before-placement order");
        check(TC4ArcaneSpaParity.emptyFluidMaskHeight(500, 48) == 43, "GUI mask rounding");
        check(TC4ArcaneSpaParity.emptyFluidMaskHeight(5000, 48) == 0, "full GUI mask");
        check(TC4ArcaneSpaParity.canPlaceOutput(true, true, false), "valid output");
        check(!TC4ArcaneSpaParity.canPlaceOutput(true, true, true), "same source");
        check(TC4ArcaneSpaParity.candidateOffsetX(0) == -2
                && TC4ArcaneSpaParity.candidateOffsetZ(0) == -2
                && TC4ArcaneSpaParity.candidateOffsetX(24) == 2
                && TC4ArcaneSpaParity.candidateOffsetZ(24) == 2, "candidate loop order");
        System.out.println("TC4ArcaneSpaParitySelfTest: PASS");
    }
}
