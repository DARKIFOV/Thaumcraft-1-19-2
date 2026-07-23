import com.darkifov.thaumcraft.jar.TC4EssentiaJarParity;

public final class TC4EssentiaJarFullClosureSelfTest {
    public static void main(String[] args) {
        require(TC4EssentiaJarParity.CAPACITY == 64, "capacity");
        require(TC4EssentiaJarParity.FILL_INTERVAL_TICKS == 5, "cadence");
        require(TC4EssentiaJarParity.PHIAL_TRANSFER == 8, "phial transfer");
        require(TC4EssentiaJarParity.suctionAmount(false, false, 0) == 32, "normal suction");
        require(TC4EssentiaJarParity.suctionAmount(false, true, 63) == 64, "labelled suction");
        require(TC4EssentiaJarParity.suctionAmount(false, true, 64) == 0, "full normal jar");
        require(TC4EssentiaJarParity.suctionAmount(true, true, 63) == 48, "labelled void suction");
        require(TC4EssentiaJarParity.suctionAmount(true, true, 64) == 32, "full labelled void suction");
        require(TC4EssentiaJarParity.suctionAmount(true, false, 64) == 32, "full void suction");
        require(TC4EssentiaJarParity.remainderAfterInsert(false, true, 60, 8) == 4, "normal overflow remainder");
        require(TC4EssentiaJarParity.remainderAfterInsert(true, true, 64, 8) == 0, "void overflow consumed");
        require(TC4EssentiaJarParity.remainderAfterInsert(true, false, 0, 8) == 8, "wrong aspect rejected");
        require(TC4EssentiaJarParity.labelFacingDataValue(0.0F) == 2, "north facing");
        require(TC4EssentiaJarParity.labelFacingDataValue(90.0F) == 5, "east facing");
        require(TC4EssentiaJarParity.labelFacingDataValue(180.0F) == 3, "south facing");
        require(TC4EssentiaJarParity.labelFacingDataValue(270.0F) == 4, "west facing");
        require(TC4EssentiaJarParity.canFillEmptyPhial(8), "fill phial");
        require(!TC4EssentiaJarParity.canFillEmptyPhial(7), "partial fill forbidden");
        require(TC4EssentiaJarParity.canEmptyFilledPhial(56, true), "empty phial at 56");
        require(!TC4EssentiaJarParity.canEmptyFilledPhial(57, true), "partial empty forbidden");
        System.out.println("TC4 essentia jar full-closure self-test: PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
