import com.darkifov.thaumcraft.arcane.TC4ArcaneWorkbenchVisCostParity;

public final class TC4ArcaneWorkbenchParitySelfTest {
    private static void req(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        req("11.64.31".equals(TC4ArcaneWorkbenchVisCostParity.CONTRACT_VERSION), "version");
        req(TC4ArcaneWorkbenchVisCostParity.inventoryLayoutMatchesOriginal(), "11-slot layout");
        req(TC4ArcaneWorkbenchVisCostParity.zeroAspectListIsFree(), "empty AspectList must be free");
        req(TC4ArcaneWorkbenchVisCostParity.centivisScalingMatchesOriginal(), "centivis scaling");
        req(TC4ArcaneWorkbenchVisCostParity.OUTPUT_SLOT == 9, "output slot");
        req(TC4ArcaneWorkbenchVisCostParity.WAND_SLOT == 10, "wand slot");
        req(TC4ArcaneWorkbenchVisCostParity.AUTOMATION_SLOT == 10, "automation slot");
        System.out.println("TC4ArcaneWorkbenchParitySelfTest: PASS");
    }
}
