import com.darkifov.thaumcraft.alchemy.TC4ThaumatoriumParity;

public final class TC4ThaumatoriumFullClosureSelfTest {
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.39".equals(TC4ThaumatoriumParity.CONTRACT_VERSION), "contract version");
        require(TC4ThaumatoriumParity.CRAFT_INTERVAL_TICKS == 5, "craft interval");
        require(TC4ThaumatoriumParity.HEAT_REFRESH_TICKS == 40, "heat refresh");
        require(TC4ThaumatoriumParity.SUCTION == 128, "suction");
        require(TC4ThaumatoriumParity.recipeCapacity(0) == 1, "base memory");
        require(TC4ThaumatoriumParity.recipeCapacity(2) == 5, "matrix memory");
        require(TC4ThaumatoriumParity.trimmedSize(6, 3) == 3, "memory trimming");
        require(TC4ThaumatoriumParity.canRun(true, false, true, 1, 10), "craft lifecycle");
        require(!TC4ThaumatoriumParity.canRun(true, true, true, 1, 10), "redstone pause");
        require(!TC4ThaumatoriumParity.canRun(false, false, true, 1, 10), "heat gate");
        require(TC4ThaumatoriumParity.FIRE_VIS_CENTIVIS == 1500, "fire vis");
        require(TC4ThaumatoriumParity.ORDER_VIS_CENTIVIS == 3000, "order vis");
        require(TC4ThaumatoriumParity.WATER_VIS_CENTIVIS == 3000, "water vis");
        System.out.println("TC4ThaumatoriumFullClosureSelfTest: PASS");
    }
}
