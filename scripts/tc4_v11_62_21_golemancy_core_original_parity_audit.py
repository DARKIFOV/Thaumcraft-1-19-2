#!/usr/bin/env python3
"""v11.62.21 Golemancy core / bell / marker / ghost-filter / renderer audit."""
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


def load_json(path: str):
    p = ROOT / path
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid/missing JSON {path}: {exc}")
        return {}


require("build.gradle", "version = '11.62.21'", "version = '11.62.20'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.21"', 'version="11.62.20"')

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/golem/GolemOriginalRuntime.java",
    "public static final int MAX_SAME_UPGRADE = 2",
    "byte[] defaultUpgrades",
    "normalizeUpgradeSlots",
    "upgradeAmount(slots, GolemUpgradeType.AIR) * 0.15F",
    'decoration.contains("H") ? 5 : 0',
    'decoration.contains("F")',
    'decoration.contains("G")',
    'decoration.contains("P")',
    'decoration.contains("M")',
    "fluidCarryLimit",
)

entity = require(
    "src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java",
    "private byte[] originalUpgradeSlots",
    "DATA_MATERIAL",
    "DATA_CORE",
    "DATA_DECORATIONS",
    "DATA_UPGRADES",
    "DATA_COLORS",
    "DATA_TOGGLES",
    "DATA_CARRIED",
    "GolemOriginalRuntime.installUpgrade(originalUpgradeSlots, upgrade)",
    "GolemOriginalRuntime.upgradeAmount(originalUpgradeSlots, upgrade)",
    "GolemOriginalRuntime.normalizeUpgradeSlots",
    "originalMarkerListSnapshot()",
    "createGolemPlacerStack()",
    "createBareGolemBodyStack()",
    "dropCarriedStackAfterDismantle()",
    "findUseMarkerTargetLikeTC4()",
    "performUseActionLikeTC4",
    "FakePlayerFactory.get(serverLevel, profile)",
    "fake.gameMode.destroyBlock(pos)",
    "new UseOnContext(fake, InteractionHand.MAIN_HAND, hit)",
    "itemCarried = fake.getMainHandItem().copy()",
    'lastOriginalTask = originalToggleEnabled(1) ? "AIUseItem:left-click" : "AIUseItem:right-click"',
    "findFurthestConnectedLogLikeTC4",
    "getUpgradeAmount(GolemUpgradeType.ORDER) > 0",
    "carriedFluidAmount",
    "carriedEssentiaAmount",
    "dart.pickup = AbstractArrow.Pickup.DISALLOWED",
    "float inaccuracy = 7.0F - getUpgradeAmount(GolemUpgradeType.WATER) * 1.75F",
    'TC4Sounds.event("golemironshoot")',
)
if "itemCarried = inventory.get(" in entity or ".shrink(1); // ghost" in entity:
    errors.append("Use core appears to consume ghost filter inventory")

container = require(
    "src/main/java/com/darkifov/thaumcraft/menu/GolemInventoryContainer.java",
    "Original ContainerGolem exposes SlotGhost / SlotGhostFluid",
    "public ItemStack removeItem(int slot, int amount)",
    "return ItemStack.EMPTY",
)
menu = require(
    "src/main/java/com/darkifov/thaumcraft/menu/GolemMenu.java",
    "GolemVisibleGhostSlot",
    "SlotGhost never removes a real item",
    "copyToFirstGhostSlot",
    "Original TC4 uses SlotGhost",
)

core_types = require(
    "src/main/java/com/darkifov/thaumcraft/golem/GolemCoreType.java",
    'FILL("fill", 0', 'EMPTY("empty", 1', 'GATHER("gather", 2',
    'HARVEST("harvest", 3', 'GUARD("guard", 4', 'LIQUID("liquid", 5',
    'ESSENTIA("essentia", 6', 'LUMBER("lumber", 7', 'USE("use", 8',
    'BUTCHER("butcher", 9', 'SORTING("sorting", 10', 'FISH("fish", 11',
)

items = require(
    "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java",
    'case "tc4_golem_straw" -> new TC4GolemPlacerItem',
    'case "tc4_golem_thaumium" -> new TC4GolemPlacerItem',
    'case "tc4_golem_core_fill" -> new TC4GolemCoreComponentItem',
    'case "tc4_golem_core_fish" -> new TC4GolemCoreComponentItem',
    'case "tc4_golem_upgrade_air" -> new GolemUpgradeItem',
    'case "tc4_golem_upgrade_entropy" -> new GolemUpgradeItem',
    'case "tc4_golemdecodart" -> new GolemDecorationItem',
    'case "tc4_golemdecomace" -> new GolemDecorationItem',
)

require(
    "src/main/java/com/darkifov/thaumcraft/item/TC4GolemPlacerItem.java",
    "level.noCollision(golem)",
    "level.addFreshEntity(golem)",
    "if (!player.getAbilities().instabuild)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/item/TC4GolemCoreComponentItem.java",
    "Install into a coreless golem body",
    "setCoreType(coreType)",
)

bell = require(
    "src/main/java/com/darkifov/thaumcraft/block/GolemBellItem.java",
    "GolemBellMarkerRuntime.bindGolem",
    "GolemBellMarkerRuntime.changeMarkers",
    "golem.createGolemPlacerStack()",
    "golem.createBareGolemBodyStack()",
    "dropOriginalCore(golem)",
    "dropRecoveredUpgrades(golem)",
    "getRandom().nextBoolean()",
)

model = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/model/TC4ThaumGolemModel.java",
    "Exact modern ModelPart translation of TC4 4.2.3.5 ModelGolem",
    "LayerDefinition.create(mesh, 128, 128)",
    "triangleWave(limbSwing, 13.0F)",
    "entity.getCoreType() == GolemCoreType.ESSENTIA",
)
accessory_model = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/model/TC4GolemAccessoriesModel.java",
    "Exact 128x128 geometry translation of TC4 ModelGolemAccessories",
    'root.getChild("dartgun")',
    'root.getChild("mace")',
    'root.getChild("visor")',
    'root.getChild("plate")',
    "entity.hasDecoration(GolemDecorationType.TOP_HAT)",
    "entity.hasDecoration(GolemDecorationType.DART_LAUNCHER)",
)
renderer = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/ThaumGolemRenderer.java",
    'texture("golem_straw")', 'texture("golem_wood")', 'texture("golem_tallow")',
    'texture("golem_clay")', 'texture("golem_flesh")', 'texture("golem_stone")',
    'texture("golem_iron")', 'texture("golem_thaumium")',
    "poseStack.scale(0.4F, 0.4F, 0.4F)",
    "new TC4GolemAccessoriesLayer",
    "new TC4GolemDamageLayer",
    "new TC4GolemCarriedItemLayer",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "EntityRenderers.register(ThaumcraftMod.THAUM_GOLEM.get(), ThaumGolemRenderer::new)",
    "event.registerLayerDefinition(TC4ThaumGolemModel.LAYER, TC4ThaumGolemModel::createBodyLayer)",
    "event.registerLayerDefinition(TC4GolemAccessoriesModel.LAYER, TC4GolemAccessoriesModel::createBodyLayer)",
)

for texture in (
    "golem_straw.png", "golem_wood.png", "golem_tallow.png", "golem_clay.png",
    "golem_flesh.png", "golem_stone.png", "golem_iron.png", "golem_thaumium.png",
    "golem_decoration.png", "golem_damage.png",
):
    path = ROOT / "src/main/resources/assets/thaumcraft/textures/models" / texture
    if not path.is_file():
        errors.append(f"missing original golem texture: {path.relative_to(ROOT)}")

mapping = load_json("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_v11_62_21_golemancy_core_original_parity.json")
if mapping.get("version") != "11.62.21" or not mapping.get("strict_original"):
    errors.append("v11.62.21 source mapping metadata mismatch")

require(
    ".github/workflows/main.yml",
    "tc4_v11_62_21_golemancy_core_original_parity_audit.py",
    "thaumcraft-legacy-rebuild-1.19.2-v11.62.21-github-jar",
    "v11.62.21-build-reports",
)

if errors:
    print("TC4 v11.62.21 Golemancy core parity audit FAILED:")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print("TC4 v11.62.21 Golemancy core original parity audit: OK")
