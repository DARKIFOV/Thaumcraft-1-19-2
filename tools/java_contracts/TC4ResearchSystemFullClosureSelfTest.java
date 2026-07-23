import com.darkifov.thaumcraft.research.TC4ResearchSystemFullClosureParity;

public final class TC4ResearchSystemFullClosureSelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.27".equals(TC4ResearchSystemFullClosureParity.CONTRACT_VERSION), "version");
        require(TC4ResearchSystemFullClosureParity.ORIGINAL_RESEARCH_COUNT == 201, "research denominator");
        require(TC4ResearchSystemFullClosureParity.primalsStartKnownButEmpty(), "zero primal pool");
        require(TC4ResearchSystemFullClosureParity.tableSlotsMatchOriginal(), "table slots");
        require(TC4ResearchSystemFullClosureParity.NOTE_PAPER_COST == 1
                && TC4ResearchSystemFullClosureParity.NOTE_INK_COST == 1, "note costs");
        require(TC4ResearchSystemFullClosureParity.DUPLICATE_PAPER_COST == 1
                && TC4ResearchSystemFullClosureParity.DUPLICATE_INK_SAC_COST == 1, "duplicate item costs");
        require(TC4ResearchSystemFullClosureParity.noteRadius(1) == 2, "complexity 1 radius");
        require(TC4ResearchSystemFullClosureParity.noteRadius(2) == 3, "complexity 2 radius");
        require(TC4ResearchSystemFullClosureParity.noteRadius(3) == 4, "complexity 3 radius");
        require(TC4ResearchSystemFullClosureParity.noteRadius(9) == 4, "radius clamp");
        require(TC4ResearchSystemFullClosureParity.duplicateAspectCost(4, 0) == 4, "first copy cost");
        require(TC4ResearchSystemFullClosureParity.duplicateAspectCost(4, 3) == 7, "later copy cost");
        require(TC4ResearchSystemFullClosureParity.BONUS_RECALCULATE_THRESHOLD == 600
                && TC4ResearchSystemFullClosureParity.BONUS_SCAN_RADIUS == 8, "bonus cadence/radius");
        require(TC4ResearchSystemFullClosureParity.noteCreationAndLearningPathsMatchOriginal(),
                "note creation/learning paths");
        System.out.println("TC4ResearchSystemFullClosureSelfTest: PASS");
    }
}
