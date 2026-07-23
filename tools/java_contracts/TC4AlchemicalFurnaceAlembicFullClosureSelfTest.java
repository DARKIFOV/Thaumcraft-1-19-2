import com.darkifov.thaumcraft.alchemy.TC4AlchemicalFurnaceParity;

public final class TC4AlchemicalFurnaceAlembicFullClosureSelfTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        require(TC4AlchemicalFurnaceParity.FURNACE_CAPACITY == 50, "furnace capacity");
        require(TC4AlchemicalFurnaceParity.ALEMBIC_CAPACITY == 32, "alembic capacity");
        require(TC4AlchemicalFurnaceParity.MAX_ALEMBICS == 5, "five alembics");
        require(TC4AlchemicalFurnaceParity.distillationInterval(false) == 40, "normal interval");
        require(TC4AlchemicalFurnaceParity.distillationInterval(true) == 20, "alumentum interval");
        require(TC4AlchemicalFurnaceParity.smeltTime(8, 0) == 80, "base smelt");
        require(TC4AlchemicalFurnaceParity.smeltTime(8, 6) == 20, "six bellows");
        require(TC4AlchemicalFurnaceParity.alembicFillMessage(0) == 1, "empty message");
        require(TC4AlchemicalFurnaceParity.alembicFillMessage(12) == 2, "low message");
        require(TC4AlchemicalFurnaceParity.alembicFillMessage(13) == 3, "medium message");
        require(TC4AlchemicalFurnaceParity.alembicFillMessage(26) == 4, "high message");
        require(TC4AlchemicalFurnaceParity.alembicFillMessage(32) == 5, "full message");
        require(TC4AlchemicalFurnaceParity.comparator(0) == 0, "empty comparator");
        require(TC4AlchemicalFurnaceParity.comparator(1) == 1, "one comparator");
        require(TC4AlchemicalFurnaceParity.comparator(32) == 15, "full comparator");
        System.out.println("TC4AlchemicalFurnaceAlembicFullClosureSelfTest: PASS");
    }
}
