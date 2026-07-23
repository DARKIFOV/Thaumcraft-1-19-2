import com.darkifov.thaumcraft.wand.TC4WandFocusContract;

public final class TC4WandFocusContractSelfTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.33".equals(TC4WandFocusContract.CONTRACT_VERSION), "version");
        require(TC4WandFocusContract.ORIGINAL_FOCUS_TYPES == 10, "focus count");
        require(TC4WandFocusContract.FOCUS_POUCH_SLOTS == 18, "pouch slots");
        require(TC4WandFocusContract.FOCUS_UPGRADE_RANKS == 5, "upgrade ranks");
        require(TC4WandFocusContract.radialRadius(10) == 41.0F, "radial radius");
        require(TC4WandFocusContract.radialSliceDegrees(10) == 36.0F, "radial slice");
        require(TC4WandFocusContract.radialAngleDegrees(0, 10, 1.0F) == -90.0F, "first angle");
        require(TC4WandFocusContract.radialAngleDegrees(9, 10, 1.0F) == 234.0F, "last angle");
        require(TC4WandFocusContract.radialSliceDegrees(0) == 0.0F, "empty radial");
        require(TC4WandFocusContract.clamp01(-2.0F) == 0.0F, "clamp low");
        require(TC4WandFocusContract.clamp01(2.0F) == 1.0F, "clamp high");
        System.out.println("TC4WandFocusContractSelfTest: PASS");
    }
}
