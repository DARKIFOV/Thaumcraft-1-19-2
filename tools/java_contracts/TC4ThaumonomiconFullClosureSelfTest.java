import com.darkifov.thaumcraft.research.TC4ThaumonomiconLootParity;
import com.darkifov.thaumcraft.research.TC4ThaumonomiconParity;

public final class TC4ThaumonomiconFullClosureSelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.26".equals(TC4ThaumonomiconParity.CONTRACT_VERSION), "parity version");
        require("11.64.26".equals(TC4ThaumonomiconLootParity.CONTRACT_VERSION), "loot version");
        require(TC4ThaumonomiconParity.MAX_STACK_SIZE == 1, "stack size");
        require(TC4ThaumonomiconParity.CHEAT_ASPECT_POOL == 50, "cheat pool");
        require(TC4ThaumonomiconParity.BROWSER_WIDTH == 256 && TC4ThaumonomiconParity.BROWSER_HEIGHT == 230,
                "browser dimensions");
        require(TC4ThaumonomiconParity.MAP_WIDTH == 224 && TC4ThaumonomiconParity.MAP_HEIGHT == 196,
                "map dimensions");
        require(TC4ThaumonomiconParity.DEFAULT_PAN_X == -202 && TC4ThaumonomiconParity.DEFAULT_PAN_Y == -214,
                "initial shared pan");
        require(TC4ThaumonomiconParity.spreadStart(0) == 0, "spread zero");
        require(TC4ThaumonomiconParity.spreadStart(3) == 2, "odd spread");
        require(TC4ThaumonomiconParity.previousSpread(3) == 0, "previous spread");
        require(TC4ThaumonomiconParity.nextSpread(0, 5) == 2, "next spread");
        require(TC4ThaumonomiconParity.maxFirstPage(1) == 0, "single page");
        require(TC4ThaumonomiconParity.maxFirstPage(5) == 4, "odd page max");
        require(TC4ThaumonomiconParity.maxFirstPage(6) == 4, "even page max");
        require(TC4ThaumonomiconLootParity.RARE_CHEST_TABLES.size() == 7, "seven legacy chest tables");
        require(TC4ThaumonomiconLootParity.RARE_CHEST_WEIGHT == 1, "rare chest weight");
        require(TC4ThaumonomiconLootParity.WIZARD_TOWER_WEIGHT == 20, "tower weight");
        require(TC4ThaumonomiconLootParity.MIN_COUNT == 1 && TC4ThaumonomiconLootParity.MAX_COUNT == 1,
                "loot count");
        require(TC4ThaumonomiconLootParity.requiresOriginalWizardTowerIntegration(), "tower boundary");
        require(TC4ThaumonomiconLootParity.requiresLegacyGlobalChestIntegration(), "chest boundary");
        System.out.println("TC4ThaumonomiconFullClosureSelfTest: PASS");
    }
}
