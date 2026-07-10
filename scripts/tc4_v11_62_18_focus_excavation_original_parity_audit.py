#!/usr/bin/env python3
"""v11.62.18 Focus Excavation original-parity source/resource audit."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def require(path: str, *tokens: str) -> str:
    p = ROOT / path
    if not p.is_file():
        errors.append(f"missing file: {path}")
        return ""
    text = p.read_text(encoding="utf-8")
    for token in tokens:
        if token not in text:
            errors.append(f"{path} missing {token!r}")
    return text


require("build.gradle", "version = '11.62.18'", "version = '11.62.17'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.18"', 'version="11.62.17"')

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "case EXCAVATION -> onUsingExcavationFocusTick(wandStack, level, player, count)",
    "if (getFocus(wandStack) == WandFocusType.EXCAVATION)",
    "clearExcavationUse(level, player)",
    "case EXCAVATION -> focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH)",
    "cost(Aspect.AER, 1, Aspect.IGNIS, 1, Aspect.TERRA, 16, Aspect.AQUA, 1, Aspect.ORDO, 1, Aspect.PERDITIO, 1)",
    "cost(Aspect.TERRA, 15, Aspect.IGNIS, 2, Aspect.ORDO, 2)",
    "cost(Aspect.TERRA, 15)",
    "BlockHitResult blockHit = blockRay(level, player, 10.0D)",
    "TC4ClientFocusFxBridge.beamCont(level, player, end, 2, 65382, false",
    'TC4Sounds.event("rumble")',
    "EXCAVATION_SOUND_DELAY.put(pp, now + 1200L)",
    "float speed = excavationSpeed(state, potency)",
    "if (last == null || !last.equals(target))",
    "TC4ClientFocusFxBridge.excavateFX(level, last, player, -1)",
    "if (breakCount >= hardness)",
    "consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)",
    "private static boolean excavateBlock(Level level, Player player, BlockPos target, ItemStack wandStack)",
    "for (int attempt = 0; attempt < enlarge; attempt++)",
    "BlockPos neighbour = matchingNeighbour(level, target, originalState)",
    "if (excavateSingleBlock(level, player, neighbour, wandStack))",
    "consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, neighbourCost)",
    "level.getBlockState(candidate).equals(state)",
    "ForgeHooks.onBlockBreakEvent(level, player.gameMode.getGameModeForPlayer(), player, target)",
    "BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, target, state, player)",
    "MinecraftForge.EVENT_BUS.post(event)",
    "focusUpgradeLevel(wandStack, FocusUpgradeType.TREASURE)",
    "focusHasUpgrade(wandStack, FocusUpgradeType.SILK_TOUCH)",
    "Enchantments.SILK_TOUCH, 1",
    "Enchantments.BLOCK_FORTUNE, treasure",
    "Block.getDrops(state, server, target, blockEntity, player, lootTool)",
    "ExperienceOrb.award(server, Vec3.atCenterOf(target), experience)",
    "WardedBlockRuntime.mayEdit(level, target, player)",
    "private static ItemStack applyDowsing(ItemStack drop, int treasure, RandomSource random)",
    "float chance = (0.2F + treasure * 0.075F) * specialChance",
    'clusterId = "tc4_clusteriron"',
    'clusterId = "tc4_clustergold"',
    'clusterId = "tc4_clustercopper"',
    'clusterId = "tc4_clustertin"',
    'clusterId = "tc4_clustersilver"',
    'clusterId = "tc4_clusterlead"',
    'clusterId = "tc4_clustercinnabar"',
    "specialChance = 0.9F",
    "drop.getCount()",
    "SoundEvents.EXPERIENCE_ORB_PICKUP",
    "material == net.minecraft.world.level.material.Material.STONE",
    "material == net.minecraft.world.level.material.Material.GRASS",
    "material == net.minecraft.world.level.material.Material.DIRT",
    "material == net.minecraft.world.level.material.Material.SAND",
    "speed = 0.25F + potency * 0.25F",
    "speed *= 3.0F",
)
for forbidden in (
    "hardness > 20.0F",
    "hardness >= 20.0F",
    "Material.METAL",
    "Material.GLASS",
):
    if forbidden in runtime:
        errors.append(f"Excavation runtime still contains obsolete behavior: {forbidden}")

client_fx = require(
    "src/main/java/com/darkifov/thaumcraft/client/fx/TC4ClientFocusFx.java",
    "public static void excavateFX(BlockPos pos, Player player, int progress)",
    "progress < 0 ? -1 : Math.max(0, Math.min(9, progress))",
)

require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case EXCAVATION -> switch (rank)",
    "case 1 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE)",
    "case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, FocusUpgradeType.DOWSING)",
    "case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.TREASURE, FocusUpgradeType.SILK_TOUCH)",
)

recipe_path = ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusexcavation.json"
try:
    recipe = json.loads(recipe_path.read_text(encoding="utf-8"))
except Exception as exc:
    errors.append(f"invalid/missing Focus Excavation recipe: {exc}")
else:
    expected = {
        "research": "FOCUSEXCAVATION",
        "pattern": ["CQC", "Q#Q", "CQC"],
        "key": {"#": "minecraft:emerald", "Q": "minecraft:quartz", "C": "thaumcraft:terra_shard"},
        "aspects": {"TERRA": 20, "PERDITIO": 5, "ORDO": 5},
    }
    for key, value in expected.items():
        if recipe.get(key) != value:
            errors.append(f"Focus Excavation recipe {key} mismatch: {recipe.get(key)!r}")
    if recipe.get("result", {}).get("item") != "thaumcraft:focus_excavation":
        errors.append("Focus Excavation result mismatch")

recipes_dir = ROOT / "src/main/resources/data/thaumcraft/recipes"
for fake in recipes_dir.glob("*excav*.json"):
    errors.append(f"obsolete normal crafting recipe for Excavation still present: {fake.relative_to(ROOT)}")

resolver = require(
    "src/main/java/com/darkifov/thaumcraft/recipe/TC4RecipeItemResolver.java",
    'VANILLA_ITEMS.put("field_151128_bU", "minecraft:quartz")',
)
if 'VANILLA_ITEMS.put("field_151128_bU", "minecraft:nether_wart")' in resolver:
    errors.append("field_151128_bU still resolves to nether wart")

corrected_recipes = [
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusexcavation.json",
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focustrade.json",
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_golembell.json",
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_resonator.json",
    "src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focushellbat.json",
    "src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focusportablehole.json",
    "src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focuswarding.json",
]
for path in corrected_recipes:
    text = require(path, "minecraft:quartz")
    if "minecraft:nether_wart" in text:
        errors.append(f"{path} still contains the wrong nether-wart ingredient")

require(
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json",
    '"focus.upgrade.dowsing.name": "Рудоискательство"',
    '"tc.research_name.FOCUSEXCAVATION": "Набалдашник: Копание"',
    '"tc.research_text.FOCUSEXCAVATION": "Ужас земли"',
    '"tc.research_page.FOCUSEXCAVATION.1"',
)

mapping = require(
    "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_focus_excavation_original_parity_v11_62_18.json",
    '"base_formula": "0.2 + treasure * 0.075"',
    '"recursive_cascade": false',
    '"invented_hardness_20_limit_removed": true',
    '"field_151128_bU": "minecraft:quartz"',
    '"Vanilla 1.19.2 cooking JSON stores only a result item id',
)
try:
    json.loads(mapping)
except Exception as exc:
    errors.append(f"invalid v11.62.18 source mapping JSON: {exc}")

require(
    ".github/workflows/main.yml",
    "tc4_v11_62_18_focus_excavation_original_parity_audit.py",
    "v11.62.18-github-jar",
    "v11.62.18-build-reports",
)
require("README.md", "v11.62.18", "Focus Excavation original parity")

# Release reports and NEXT_CHAT_PROMPT are intentionally excluded from subsystem regression checks; only runtime code/resources are guarded.
if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.18 Focus Excavation original parity audit: OK")
