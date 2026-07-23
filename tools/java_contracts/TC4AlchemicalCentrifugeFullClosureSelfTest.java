import com.darkifov.thaumcraft.alchemy.TC4AlchemicalCentrifugeParity;

public final class TC4AlchemicalCentrifugeFullClosureSelfTest {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        require(TC4AlchemicalCentrifugeParity.INPUT_SUCTION == 64, "input suction");
        require(TC4AlchemicalCentrifugeParity.PROCESS_START == 39, "process start");
        require(TC4AlchemicalCentrifugeParity.DRAW_INTERVAL_TICKS == 5, "draw interval");
        require(TC4AlchemicalCentrifugeParity.tickProcess(39, false) == 38, "process decrement");
        require(TC4AlchemicalCentrifugeParity.tickProcess(39, true) == 39, "redstone pause");
        require(TC4AlchemicalCentrifugeParity.shouldProcess(0, false, true, false), "process completion");
        require(!TC4AlchemicalCentrifugeParity.shouldProcess(0, true, true, false), "powered completion pause");
        require(TC4AlchemicalCentrifugeParity.nextRotationSpeed(0.0F, true) == 2.0F, "acceleration");
        require(TC4AlchemicalCentrifugeParity.nextRotationSpeed(20.0F, true) == 20.0F, "speed cap");
        require(TC4AlchemicalCentrifugeParity.nextRotationSpeed(1.0F, false) == 0.5F, "deceleration");
        require(TC4AlchemicalCentrifugeParity.componentIndex(-1) == 1, "component modulo");
        require(TC4AlchemicalCentrifugeParity.shouldPlayPump(160.0F, 180.0F, 20.0F), "pump crossing");
        System.out.println("TC4AlchemicalCentrifugeFullClosureSelfTest: PASS");
    }
}
