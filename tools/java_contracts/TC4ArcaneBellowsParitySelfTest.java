import com.darkifov.thaumcraft.block.TC4ArcaneBellowsParity;

public final class TC4ArcaneBellowsParitySelfTest {
    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static boolean close(float a, float b) { return Math.abs(a-b) < 1.0e-6F; }
    public static void main(String[] args) {
        require("11.64.18".equals(TC4ArcaneBellowsParity.CONTRACT_VERSION), "version");
        require(close(TC4ArcaneBellowsParity.initialInflation(0.0F), 0.35F), "initial min");
        require(close(TC4ArcaneBellowsParity.initialInflation(1.0F), 0.90F), "initial max");
        var down = TC4ArcaneBellowsParity.animationStep(1.0F, false);
        require(close(down.inflation(), 0.925F) && !down.expanding() && !down.playSound(), "deflate");
        var low = TC4ArcaneBellowsParity.animationStep(0.35F, false);
        require(close(low.inflation(), 0.375F) && low.expanding() && !low.playSound(), "low switch");
        var high = TC4ArcaneBellowsParity.animationStep(0.99F, true);
        require(close(high.inflation(), 1.015F) && !high.expanding() && high.playSound(), "high overshoot");
        require(close(TC4ArcaneBellowsParity.soundPitch(1.0F,0.0F),0.7F), "pitch");
        require(close(TC4ArcaneBellowsParity.inventoryInflation(0),0.7F), "inventory");
        require(!TC4ArcaneBellowsParity.canAdvanceVanillaFurnace(0), "cook 0");
        require(TC4ArcaneBellowsParity.canAdvanceVanillaFurnace(1), "cook 1");
        require(TC4ArcaneBellowsParity.canAdvanceVanillaFurnace(198), "cook 198");
        require(!TC4ArcaneBellowsParity.canAdvanceVanillaFurnace(199), "cook 199");
        require(TC4ArcaneBellowsParity.legacyVanillaFurnaceOffset(0).equals(new TC4ArcaneBellowsParity.Offset(0,0,0)), "vertical quirk");
        require(TC4ArcaneBellowsParity.legacyVanillaFurnaceOffset(5).equals(new TC4ArcaneBellowsParity.Offset(1,0,0)), "east");
        require(TC4ArcaneBellowsParity.crucibleHeatGain(4)==9, "crucible");
        require(TC4ArcaneBellowsParity.alchemicalFurnaceSmeltTime(10,6)==25, "alchemical");
        require(TC4ArcaneBellowsParity.bufferSuction(6,0)==192, "buffer");
        require(TC4ArcaneBellowsParity.bufferSuction(6,1)==1, "choke1");
        require(TC4ArcaneBellowsParity.bufferSuction(6,2)==0, "choke2");
        require(TC4ArcaneBellowsParity.infernalFurnaceCookTime(false,3)==80, "infernal normal");
        require(TC4ArcaneBellowsParity.infernalFurnaceCookTime(true,3)==20, "infernal speedy");
        System.out.println("TC4ArcaneBellowsParitySelfTest: PASS");
    }
}
