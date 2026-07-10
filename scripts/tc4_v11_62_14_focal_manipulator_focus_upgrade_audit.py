from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    print(f"::error::{message}")
    raise SystemExit(1)


def text(path: str) -> str:
    target = ROOT / path
    if not target.is_file():
        fail(f"missing file: {path}")
    return target.read_text(encoding="utf-8")


def require(path: str, *tokens: str) -> str:
    body = text(path)
    missing = [token for token in tokens if token not in body]
    if missing:
        fail(f"{path} missing required parity markers: {missing}")
    return body


def forbid(path: str, *tokens: str) -> str:
    body = text(path)
    found = [token for token in tokens if token in body]
    if found:
        fail(f"{path} still contains stale/forbidden implementation markers: {found}")
    return body


def load_json(path: str):
    try:
        return json.loads(text(path))
    except json.JSONDecodeError as exc:
        fail(f"invalid JSON in {path}: {exc}")


require("build.gradle", "version = '11.62.14'")

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    'public static final String TAG_UPGRADE = "upgrade"',
    "public static final int MAX_RANK = 5",
    "nextOpenRank",
    "rank * 8",
    "200 << (rank - 1)",
    "primalVisCost",
    "reduceToPrimals",
    "canApplyUpgrade(ItemStack focusStack, WandFocusType focus, FocusUpgradeType type, int rank)",
    "originalSortingHelper",
    "sortingHelper",
    'entry.putShort("id"',
)

block_entity = require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/FocalManipulatorBlockEntity.java",
    "public static final int SIZE = 1",
    "public static final int XP_MULT = 8",
    "public static final int VIS_MULT = 200",
    "public static final int DRAIN_INTERVAL = 5",
    "public static final int MAX_DRAIN_PER_ASPECT = 100",
    "FocusUpgradeRuntime.nextOpenRank(focusStack)",
    "player.experienceLevel < xp",
    "if (!player.getAbilities().instabuild) player.giveExperienceLevels(-xp)",
    "FocusUpgradeRuntime.primalVisCost(selected, nextRank)",
    "AuraVisRelayNetwork.drainMachineVis",
    "Math.min(MAX_DRAIN_PER_ASPECT, outstanding)",
    "tile.finish(false)",
    "FocusUpgradeRuntime.applyUpgrade",
    'TC4Sounds.event("craftstart")',
    'TC4Sounds.event(success ? "wand" : "craftfail")',
    'tag.putInt("size", initialSize)',
    'tag.putInt("upgrade", selectedUpgrade)',
    'tag.putInt("rank", rank)',
    'tag.put("Aspects", remaining.save())',
)
forbid(
    "src/main/java/com/darkifov/thaumcraft/blockentity/FocalManipulatorBlockEntity.java",
    ".primalCost(",
)

require(
    "src/main/java/com/darkifov/thaumcraft/aura/AuraVisRelayNetwork.java",
    "private static final int MACHINE_NETWORK_RADIUS = 8",
    "private static final int CENTIVIS_PER_NODE_POINT = 100",
    "public static int drainMachineVis",
    "findEnergizedNodeNearWithAspect",
    "findConnectedEnergizedNodeWithAspect",
    "findNearestRelay",
    "drainToWand",
    "markWandDrain",
    "playMachineRelayFx",
)

require(
    "src/main/java/com/darkifov/thaumcraft/block/FocalManipulatorBlock.java",
    "extends BaseEntityBlock",
    "Shapes.or",
    "NetworkHooks.openScreen",
    "FocalManipulatorBlockEntity::serverTick",
    "Containers.dropContents",
    "getShape",
    "getCollisionShape",
)

menu = require(
    "src/main/java/com/darkifov/thaumcraft/menu/FocalManipulatorMenu.java",
    "FocalManipulatorBlockEntity.SLOT_FOCUS, 88, 60",
    "16 + col * 18, 151 + row * 18",
    "16 + col * 18, 209",
    "stack.getItem() instanceof WandFocusItem && !isCrafting()",
    "public boolean clickMenuButton",
    "tile.startCraft(id, player)",
    'TC4Sounds.event("craftfail")',
    "if (isCrafting() || index < 0 || index >= slots.size())",
)

screen = require(
    "src/main/java/com/darkifov/thaumcraft/client/screen/FocalManipulatorScreen.java",
    '"textures/gui/gui_wandtable.png"',
    "imageWidth = 192",
    "imageHeight = 233",
    "leftPos + 56 + i * 16",
    "leftPos + 48 + i * 16",
    "topPos + 104",
    "200, 0, 16, 16",
    "leftPos + 108, topPos + 59, 200, 16, 16, 16",
    "leftPos + 48, topPos + 88, 8, 240, 96, 8",
    "FocusUpgradeRuntime.primalVisCost(type, rank)",
    "minecraft.player.experienceLevel >= rank * FocalManipulatorBlockEntity.XP_MULT",
    'TC4Sounds.event("cameraclack")',
    "handleInventoryButtonClick(menu.containerId, selected)",
    'Component.translatable("wandtable.text1")',
    'Component.translatable("wandtable.text2")',
    'Component.translatable("wandtable.text3")',
)

require(
    "src/main/java/com/darkifov/thaumcraft/client/render/FocalManipulatorRenderer.java",
    "pose.translate(0.5D, 1.0D, 0.5D)",
    "ticks % 360.0F",
    "Mth.sin(ticks / 14.0F) * 0.2F + 0.2F",
    "shouldRenderOffScreen",
)

require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'focalManipulatorBlock("tc4_block_focal_manipulator"',
    "FOCAL_MANIPULATOR_BLOCK_ENTITY",
    "FocalManipulatorBlockEntity::new, FOCAL_MANIPULATOR.get()",
    "FOCAL_MANIPULATOR_MENU",
    "new FocalManipulatorMenu",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "BlockEntityRenderers.register(ThaumcraftMod.FOCAL_MANIPULATOR_BLOCK_ENTITY.get(), FocalManipulatorRenderer::new)",
    "MenuScreens.register(ThaumcraftMod.FOCAL_MANIPULATOR_MENU.get(), FocalManipulatorScreen::new)",
)

focus_item = require(
    "src/main/java/com/darkifov/thaumcraft/block/WandFocusItem.java",
    "Rarity.RARE",
    '"item.Focus.cost2"',
    '"item.Focus.cost1"',
    "/ 100.0F",
    "LinkedHashMap<FocusUpgradeType, Integer>",
    'Component.translatable("enchantment.level." + entry.getValue())',
)
if "Next focal rank" in focus_item:
    fail("WandFocusItem still exposes the non-original debug 'Next focal rank' tooltip")

pouch = require(
    "src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java",
    'case FIRE -> "AF"',
    'case EXCAVATION -> "BE"',
    'case FROST -> "BF"',
    'case SHOCK -> "BL"',
    'case EQUAL_TRADE -> "BT"',
    'case PORTABLE_HOLE -> "BPH"',
    'case WARDING -> "BWA"',
    'case PRIMAL -> "FP"',
    "FocusUpgradeRuntime.sortingHelper(stack)",
)

blockstate = load_json("src/main/resources/assets/thaumcraft/blockstates/tc4_block_focal_manipulator.json")
variants = blockstate.get("variants", {})
if variants.get("", {}).get("model") != "thaumcraft:block/tc4_block_focal_manipulator":
    fail("Focal Manipulator blockstate does not point at its real block model")

model = load_json("src/main/resources/assets/thaumcraft/models/block/tc4_block_focal_manipulator.json")
if model.get("textures", {}).get("table") != "thaumcraft:models/wandtable":
    fail("Focal Manipulator model is not using the original TC4 wandtable texture")
if len(model.get("elements", [])) != 6:
    fail("Focal Manipulator model must contain the original six cuboids (top, base, four legs)")

item_model = load_json("src/main/resources/assets/thaumcraft/models/item/tc4_block_focal_manipulator.json")
if item_model.get("parent") != "thaumcraft:block/tc4_block_focal_manipulator":
    fail("Focal Manipulator item model is not backed by the real block model")

loot = load_json("src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_focal_manipulator.json")
loot_text = json.dumps(loot, sort_keys=True)
if "thaumcraft:tc4_block_focal_manipulator" not in loot_text:
    fail("Focal Manipulator loot table does not return the block")

for asset in (
    "src/main/resources/assets/thaumcraft/textures/gui/gui_wandtable.png",
    "src/main/resources/assets/thaumcraft/textures/models/wandtable.png",
):
    if not (ROOT / asset).is_file():
        fail(f"missing original TC4 asset: {asset}")

recipe = load_json("src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focalmanipulator.json")
expected_key = {
    "I": "minecraft:iron_ingot",
    "Q": "thaumcraft:tc4_block_arcane_stone_slab",
    "S": "thaumcraft:tc4_block_arcane_stone",
    "P": "thaumcraft:tc4_charm",
    "G": "minecraft:gold_ingot",
    "T": "thaumcraft:table",
}
if recipe.get("research") != "FOCALMANIPULATION":
    fail("Focal Manipulator recipe has the wrong research gate")
if recipe.get("pattern") != ["IQI", "SPS", "GTG"]:
    fail("Focal Manipulator recipe pattern differs from ConfigRecipes.java")
if recipe.get("key") != expected_key:
    fail(f"Focal Manipulator recipe key mapping differs from original: {recipe.get('key')}")
expected_primals = {name: 32 for name in ("IGNIS", "AER", "PERDITIO", "TERRA", "AQUA", "ORDO")}
if recipe.get("aspects") != expected_primals:
    fail("Focal Manipulator recipe must cost 32 of every primal aspect")
if recipe.get("result") != {"item": "thaumcraft:tc4_block_focal_manipulator", "count": 1}:
    fail("Focal Manipulator recipe result is not the real registered block")

workflow = require(
    ".github/workflows/main.yml",
    "tc4_v11_62_14_focal_manipulator_focus_upgrade_audit.py",
    "v11.62.14-github-jar",
    "v11.62.14-build-reports",
    "build/libs/*-github.jar",
)
if "build/libs/*.jar" in workflow:
    fail("GitHub Actions still uploads every jar instead of the one playable *-github.jar")

for dotfile in (".gitattributes", ".gitignore", ".github/workflows/main.yml"):
    if not (ROOT / dotfile).is_file():
        fail(f"required GitHub/dotfile missing: {dotfile}")

print("TC4 v11.62.14 Focal Manipulator/focus upgrade parity audit: OK")
