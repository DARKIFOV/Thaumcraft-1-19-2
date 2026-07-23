import com.darkifov.thaumcraft.block.TC4ArcaneEarParity;

public final class TC4ArcaneEarParitySelfTest {
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static boolean close(float a, float b) { return Math.abs(a - b) < 1.0e-6F; }
    public static void main(String[] args) {
        require("11.64.19".equals(TC4ArcaneEarParity.CONTRACT_VERSION), "version");
        require(TC4ArcaneEarParity.nextNote(0) == 1, "next 0");
        require(TC4ArcaneEarParity.nextNote(24) == 0, "wrap 24");
        require(TC4ArcaneEarParity.clampNote(-1) == 0, "clamp low");
        require(TC4ArcaneEarParity.clampNote(25) == 24, "clamp high");
        require(close(TC4ArcaneEarParity.notePitch(0), 0.5F), "pitch low");
        require(close(TC4ArcaneEarParity.notePitch(12), 1.0F), "pitch center");
        require(close(TC4ArcaneEarParity.notePitch(24), 2.0F), "pitch high");
        require(TC4ArcaneEarParity.matchesPlayedNote(4, 24, 4, 24, 4096.0D), "range inclusive");
        require(!TC4ArcaneEarParity.matchesPlayedNote(4, 24, 4, 24, 4096.0001D), "range reject");
        require(!TC4ArcaneEarParity.matchesPlayedNote(4, 24, 0, 24, 0.0D), "tone reject");
        require(TC4ArcaneEarParity.pulseAfterTick(10) == 9, "pulse 10");
        require(TC4ArcaneEarParity.pulseAfterTick(1) == 0, "pulse 1");
        require(TC4ArcaneEarParity.pulseAfterTick(0) == 0, "pulse 0");
        System.out.println("TC4ArcaneEarParitySelfTest: PASS");
    }
}
