import com.darkifov.thaumcraft.infusion.TC4InfusionAltarFullClosureParity;

public final class TC4InfusionAltarFullClosureSelfTest {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.34".equals(TC4InfusionAltarFullClosureParity.CONTRACT_VERSION), "version");
        require(TC4InfusionAltarFullClosureParity.PRIMAL_ASPECT_COUNT == 6, "six primal aspects");
        require(TC4InfusionAltarFullClosureParity.ALTAR_VIS_COST_PER_PRIMAL == 25, "25 vis");
        require(TC4InfusionAltarFullClosureParity.ALTAR_VIS_COST_PER_PRIMAL_CENTIVIS == 2500, "2500 centivis");
        require(TC4InfusionAltarFullClosureParity.altarBlueprintVolume() == 27, "3x3x3 volume");
        require(TC4InfusionAltarFullClosureParity.altarBlueprintAccountedBlocks() == 27, "blueprint accounting");
        for (int i = 2; i <= 5; i++) {
            require(TC4InfusionAltarFullClosureParity.isOriginalPillarOrientation(i), "orientation " + i);
        }
        require(!TC4InfusionAltarFullClosureParity.isOriginalPillarOrientation(1), "orientation lower bound");
        require(!TC4InfusionAltarFullClosureParity.isOriginalPillarOrientation(6), "orientation upper bound");
        require(TC4InfusionAltarFullClosureParity.pedestalComparator(false) == 0, "empty comparator");
        require(TC4InfusionAltarFullClosureParity.pedestalComparator(true) == 15, "occupied comparator");
        require(TC4InfusionAltarFullClosureParity.isPedestalScanOffset(8, -10, 8), "pedestal far bound");
        require(!TC4InfusionAltarFullClosureParity.isPedestalScanOffset(9, -10, 8), "pedestal horizontal bound");
        require(!TC4InfusionAltarFullClosureParity.isPedestalScanOffset(1, 0, 0), "pedestal vertical top");
        require(TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(10, 10), "normal cycle");
        require(!TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(11, 10), "normal non-cycle");
        require(TC4InfusionAltarFullClosureParity.shouldRunCraftCycle(20, 20), "xp cycle");
        require(TC4InfusionAltarFullClosureParity.COMPONENT_TRAVEL_CYCLES == 5, "component cycles");
        require(TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_IDLE_INTERVAL == 100, "idle validity");
        require(TC4InfusionAltarFullClosureParity.MATRIX_VALIDITY_CRAFTING_INTERVAL == 20, "craft validity");
        require(TC4InfusionAltarFullClosureParity.CRAFTING_BREAK_EXPLOSION_STRENGTH == 2.0F, "break explosion");
        require("CustomName".equals(TC4InfusionAltarFullClosureParity.PEDESTAL_CUSTOM_NAME_NBT), "pedestal custom name");
        require("recipein".equals(TC4InfusionAltarFullClosureParity.MATRIX_RECIPE_INPUTS_NBT), "matrix ingredients nbt");
        require("rotype".equals(TC4InfusionAltarFullClosureParity.MATRIX_RECIPE_OUTPUT_TYPE_NBT), "matrix output type nbt");
        require("recipeout".equals(TC4InfusionAltarFullClosureParity.MATRIX_RECIPE_OUTPUT_NBT), "matrix output nbt");
        System.out.println("TC4InfusionAltarFullClosureSelfTest: PASS");
    }
}
