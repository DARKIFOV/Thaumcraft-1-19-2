import com.darkifov.thaumcraft.block.InfernalFurnaceLayer;
import com.darkifov.thaumcraft.block.TC4InfernalFurnaceParity;
import net.minecraft.core.Direction;

public final class TC4InfernalFurnaceParitySelfTest {
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        require(TC4InfernalFurnaceParity.INVENTORY_SIZE == 32, "inventory");
        require(TC4InfernalFurnaceParity.cookTime(false,0)==140, "normal");
        require(TC4InfernalFurnaceParity.cookTime(true,0)==80, "speedy");
        require(TC4InfernalFurnaceParity.cookTime(false,3)==80, "bellows normal");
        require(TC4InfernalFurnaceParity.cookTime(true,3)==20, "bellows speedy");
        require(TC4InfernalFurnaceParity.nozzlePart(Direction.NORTH)==2, "north nozzle");
        require(TC4InfernalFurnaceParity.nozzlePart(Direction.EAST)==6, "east nozzle");
        require(TC4InfernalFurnaceParity.originalPartRestoresObsidian(2), "obsidian even");
        require(TC4InfernalFurnaceParity.originalPartRestoresObsidian(5), "obsidian center");
        require(!TC4InfernalFurnaceParity.originalPartRestoresObsidian(1), "nether brick odd");
        require(TC4InfernalFurnaceParity.textureLevelCode(1, InfernalFurnaceLayer.MIDDLE, Direction.NORTH)==9, "middle texture");
        require(TC4InfernalFurnaceParity.FORMATION_IGNIS_CENTIVIS==5000, "50 Ignis");
        require(TC4InfernalFurnaceParity.FORMATION_TERRA_CENTIVIS==5000, "50 Terra");
        System.out.println("TC4InfernalFurnaceParitySelfTest: PASS");
    }
}
