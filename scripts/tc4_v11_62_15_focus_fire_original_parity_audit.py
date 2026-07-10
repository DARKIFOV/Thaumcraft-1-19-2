#!/usr/bin/env python3
"""v11.62.15 Focus Fire / centivis original-parity source audit."""
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

build = require("build.gradle", "version = '11.62.15'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.15"')

components = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandComponentData.java",
    "isSceptre(stack) ? 150 : 100",
    "rod.baseCapacity() * multiplier",
)
wand = require(
    "src/main/java/com/darkifov/thaumcraft/block/WandItem.java",
    'TAG_VIS_FORMAT = "TC4VisCentivis"',
    "raw * 100",
    "addRealVis(stack, aspect, amount * 100)",
    "public static void addRealVis",
    "formatVis(int centivis)",
    "/ 100)",
)
runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "cost(Aspect.IGNIS, 66, Aspect.PERDITIO, 33)",
    "cost(Aspect.IGNIS, 10, Aspect.ORDO, 3)",
    "case FIRE -> focusHasUpgrade(wandStack, FocusUpgradeType.FIREBALL) ? 20 : 0",
    "float scatter = fireBeam ? 0.25F : 15.0F",
    "for (int a = 0; a < 2 + potency; a++)",
    "damage = (damage + 0.5F) * 1.5F",
    "ember.setDuration(30)",
    "ember.setFirey(firey)",
    "focusPotency(wandStack)",
    "shootProjectile(orb, player, 1.5F, 1.0F)",
)
upgrades = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java",
    'FIREBALL(9',
    '"DARKNESS"',
    'FIREBEAM(10',
    '"ENERGY,AIR"',
    'ALCHEMISTS_FIRE(4',
    '"ENERGY,SLIME"',
)
upgrade_runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FIRE)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.FIREBALL, FocusUpgradeType.FIREBEAM)",
    "&& isUpgradedWith(focusStack, FocusUpgradeType.ALCHEMISTS_FIRE)",
)
ember = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4EmberEntity.java",
    "EntityDataSerializers.INT",
    "EntityDataSerializers.FLOAT",
    "getDuration() <= 20 ? 0.95D : 0.975D",
    "living.setSecondsOnFire(3 + getFirey())",
    "0.025F * getFirey()",
    "public boolean isPickable() { return false; }",
    "public boolean hurt(DamageSource source, float amount) { return false; }",
)
orb = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ExplosiveOrbEntity.java",
    "EntityDataSerializers.FLOAT",
    "EntityDataSerializers.BOOLEAN",
    "getStrength() * 1.5F",
    "Explosion.BlockInteraction.NONE",
    "setDeltaMovement(look.scale(0.9D))",
    "maxLife = 500",
)
renderer = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java",
    "int frame = Mth.clamp((int)(8.0F * progress), 0, 8)",
    "float u0 = (7 + frame) / 16.0F",
    "0.5625F",
    "0.625F",
    "float scale = 0.25F + progress",
    "255, 255, 255, 230, 220",
)
require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'ENTITY_TYPES.register("focus_ember"',
    '.updateInterval(1)',
    'ENTITY_TYPES.register("focus_explosive_orb"',
)
require(
    "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java",
    "base * 100",
    "entry.getValue() * 100",
    "ORDO_COST * 100",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/WandVisOverlayEvents.java",
    "WandFocusRuntime.focusVisCost",
    "WandItem.formatVis(amount)",
    "WandItem.formatVis(modifiedCost)",
)

recipe_path = ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusfire.json"
try:
    recipe = json.loads(recipe_path.read_text(encoding="utf-8"))
except Exception as exc:
    errors.append(f"invalid/missing Focus Fire arcane recipe: {exc}")
else:
    expected = {
        "research": "FOCUSFIRE",
        "pattern": ["CQC", "Q#Q", "CQC"],
        "key": {"#": "minecraft:fire_charge", "Q": "minecraft:quartz", "C": "thaumcraft:ignis_shard"},
        "aspects": {"IGNIS": 20, "PERDITIO": 10},
    }
    for key, value in expected.items():
        if recipe.get(key) != value:
            errors.append(f"Focus Fire recipe {key} mismatch: {recipe.get(key)!r}")
    if recipe.get("result", {}).get("item") != "thaumcraft:focus_fire":
        errors.append("Focus Fire recipe result mismatch")

for fake in (
    "src/main/resources/data/thaumcraft/recipes/focus_fire.json",
    "src/main/resources/data/thaumcraft/recipes/focus_fire_original_style.json",
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/focus_fire_original_json_sync_test.json",
):
    if (ROOT / fake).exists():
        errors.append(f"obsolete fake Focus Fire recipe still present: {fake}")

workflow = require(
    ".github/workflows/main.yml",
    "tc4_v11_62_15_focus_fire_original_parity_audit.py",
    "build/libs/*-github.jar",
    "v11.62.15-github-jar",
)
if "build/libs/*.jar" in workflow.replace("build/libs/*-github.jar", ""):
    errors.append("workflow still uploads every jar")

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.15 Focus Fire original parity audit: OK")
