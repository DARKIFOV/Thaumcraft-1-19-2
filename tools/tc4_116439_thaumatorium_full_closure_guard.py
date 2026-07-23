from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
def text(path):
    return (ROOT / path).read_text(encoding="utf-8")
def require(ok, message):
    if not ok:
        raise SystemExit("TC4 v11.64.39 Thaumatorium guard: FAIL: " + message)

require("version = '11.64.39'" in text("build.gradle"), "build version")
require('version="11.64.39"' in text("src/main/resources/META-INF/mods.toml"), "mods version")
parity = text("src/main/java/com/darkifov/thaumcraft/alchemy/TC4ThaumatoriumParity.java")
for token in ('CONTRACT_VERSION = "11.64.39"', 'CRAFT_INTERVAL_TICKS = 5',
              'HEAT_REFRESH_TICKS = 40', 'SUCTION = 128',
              'FIRE_VIS_CENTIVIS = 1500', 'ORDER_VIS_CENTIVIS = 3000',
              'WATER_VIS_CENTIVIS = 3000', 'recipeCapacity', 'trimmedSize', 'canRun'):
    require(token in parity, "missing parity token " + token)

tile = text("src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java")
for token in ('implements MenuProvider, WorldlyContainer', 'rememberedFormulas',
              'toggleFormulaIndex', 'TC4RecipeRequirementIndex.isRuntimeRecipeUnlocked',
              'refreshRecipeCapacity', 'ForgeEventFactory.firePlayerCraftingEvent',
              'setDeltaMovement(0.075D', 'CLIENT_VENT_TICKS'):
    require(token in tile, "missing production lifecycle " + token)

multi = text("src/main/java/com/darkifov/thaumcraft/alchemy/TC4ThaumatoriumMultiblock.java")
for token in ('TC4_ALCHEMICAL_CONSTRUCT', 'THAUMATORIUM_UPPER', 'CRUCIBLE',
              'PlayerThaumData.hasResearch', 'consumeAllVis', 'setValue(ThaumatoriumBlock.FACING'):
    require(token in multi, "missing multiblock token " + token)

menu = text("src/main/java/com/darkifov/thaumcraft/menu/ThaumatoriumMenu.java")
screen = text("src/main/java/com/darkifov/thaumcraft/client/screen/ThaumatoriumScreen.java")
require('addSlot(new Slot(this.thaumatorium, 0, 48, 16))' in menu, "catalyst slot")
require('imageHeight = 166' in screen and 'topPos + 16' in screen, "original GUI geometry")
require('visibleFormulaCandidates(minecraft.player)' in screen, "research-gated candidates")

resources = [
    "src/main/resources/assets/thaumcraft/textures/gui/thaumcraft_core_original/gui_thaumatorium.png",
    "src/main/resources/assets/thaumcraft/textures/models/thaumatorium.obj",
    "src/main/resources/assets/thaumcraft/textures/models/thaumatorium.mtl",
    "src/main/resources/assets/thaumcraft/textures/models/thaumatorium.png",
    "src/main/resources/assets/thaumcraft/models/block/thaumatorium.obj",
    "src/main/resources/assets/thaumcraft/models/block/thaumatorium.mtl",
]
for resource in resources:
    require((ROOT / resource).is_file(), "missing resource " + resource)

tests = text("src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java")
required_tests = (
    "thaumatoriumRequiresHeatAndRedstoneStopsSuction",
    "thaumatoriumCraftClearsEntireLegacyEssentiaBuffer",
    "mnemonicMatricesExpandThaumatoriumFormulaMemoryByTwo",
    "blockedThaumatoriumOutputPreservesCatalystAndEssentia",
    "thaumatoriumRequiresRememberedFormulaAndUpperPartProxiesLower",
    "thaumatoriumTrimsRememberedRecipesWhenOrientedMatrixIsLost",
)
for name in required_tests:
    require(name in tests, "missing GameTest " + name)

manifest = json.loads(text("runtime_artifacts/runtime_test_manifest.template.json"))
require(manifest.get("version") == "11.64.39", "runtime manifest version")
ids = [entry.get("id") for entry in manifest.get("tests", [])]
for scenario in ("multiblock.thaumatorium_wand_creation", "alchemy.thaumatorium_formula_memory",
                 "alchemy.thaumatorium_transport_craft", "client.thaumatorium_gui_obj"):
    require(scenario in ids, "missing runtime scenario " + scenario)
require(len(ids) == len(set(ids)), "duplicate runtime scenario ids")
require((ROOT / "UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md").is_file(), "universal prompt missing")
print(f"TC4 v11.64.39 Thaumatorium guard: PASS ({len(required_tests)} GameTests, {len(ids)} runtime scenarios)")
