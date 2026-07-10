#!/usr/bin/env python3
"""v11.62.17 Focus Shock + Focus Primal original-parity source/resource audit."""
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

require("build.gradle", "version = '11.62.17'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.17"')

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "private static final Map<String, Long> FOCUS_NEXT_CAST_TICK",
    "FOCUS_NEXT_CAST_TICK.put(cooldownKey, gameTime + cooldown)",
    "case SHOCK -> castShock(wandStack, level, player)",
    "case PRIMAL -> castPrimal(wandStack, level, player)",
    "private static boolean castShock(ItemStack wandStack, Level level, Player player)",
    "private static boolean castPrimal(ItemStack wandStack, Level level, Player player)",
    "case SHOCK -> focusHasUpgrade(wandStack, FocusUpgradeType.EARTH_SHOCK) ? 20 : focusHasUpgrade(wandStack, FocusUpgradeType.CHAIN_LIGHTNING) ? 10 : 5",
    "case PRIMAL -> 10",
    "cost(Aspect.AER, 75, Aspect.TERRA, 25)",
    "cost(Aspect.AER, 40, Aspect.AQUA, 10)",
    "cost(Aspect.AER, 25)",
    "orb.setArea(4.0F + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2.0F)",
    "orb.setDamage((int) (5 + potency * 1.33D))",
    "shootProjectile(orb, player, 1.5F, 1.0F)",
    "living.hurt(DamageSource.playerAttack(player), (chainLevel > 0 ? 6.0F : 4.0F) + potency)",
    "chainLevel * 2 + focusUpgradeLevel(wandStack, FocusUpgradeType.ENLARGE) * 2",
    "source.distanceToSqr(e) <= 64.0D",
    "closest.hurt(DamageSource.playerAttack(caster), 4.0F + potency)",
    "orb.setSeeker(focusHasUpgrade(wandStack, FocusUpgradeType.SEEKER))",
    "shootProjectile(orb, player, 0.5F, 1.0F)",
    "new java.util.Random(System.currentTimeMillis() / 200L)",
    "50 + tc4Random.nextInt(5) * 50",
)
if "MobEffects.WEAKNESS" in runtime:
    errors.append("invented Shock weakness effect still present")

upgrades = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case 1, 2 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.CHAIN_LIGHTNING, FocusUpgradeType.EARTH_SHOCK)",
    "case 4, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ENLARGE)",
    "case PRIMAL -> rank == 3 ? a(FocusUpgradeType.FRUGAL, FocusUpgradeType.SEEKER) : a(FocusUpgradeType.FRUGAL)",
    "isUpgradedWith(focusStack, FocusUpgradeType.CHAIN_LIGHTNING) || isUpgradedWith(focusStack, FocusUpgradeType.EARTH_SHOCK)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java",
    'SEEKER(16', '"SENSES,MIND"',
    'CHAIN_LIGHTNING(17', '"WEATHER"',
    'EARTH_SHOCK(18',
)

shock = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4ShockOrbEntity.java",
    "EntityDataSerializers.FLOAT",
    "entityData.define(DATA_AREA, 4.0F)",
    "entityData.define(DATA_DAMAGE, 5.0F)",
    "maxLife = 500",
    "return 0.05D",
    "DamageSource.thrown(this, getOwner())",
    "for (int i = 0; i < 20; i++)",
    "ThaumcraftMod.ELECTRIC_SHOCK.get().defaultBlockState()",
    "setDeltaMovement(look.scale(0.9D))",
    "public float getPickRadius()",
)
if "MobEffects.WEAKNESS" in shock:
    errors.append("Earth Shock entity still applies weakness")

electric = require(
    "src/main/java/com/darkifov/thaumcraft/block/ElectricShockBlock.java",
    "DamageSource.LIGHTNING_BOLT, 1.0F + level.random.nextInt(2)",
    "multiply(0.8D, 1.0D, 0.8D)",
    "public void randomTick",
    "level.removeBlock(pos, false)",
    "ParticleTypes.ELECTRIC_SPARK",
    'TC4Sounds.event("jacobs")',
    "RenderShape.INVISIBLE",
)
require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'electricShockBlock("electric_shock"',
    'ENTITY_TYPES.register("focus_shock_orb"',
    'ENTITY_TYPES.register("focus_primal_orb"',
    ".clientTrackingRange(8)",
    ".updateInterval(1)",
)
require(
    "src/main/resources/assets/thaumcraft/blockstates/electric_shock.json",
    '"minecraft:block/air"',
)

primal = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4PrimalOrbEntity.java",
    "EntityDataSerializers.BOOLEAN",
    "maxLife = 5000",
    "if (isInWaterOrBubble())",
    "new java.util.Random(getId() + nextCount)",
    "getBoundingBox().inflate(16.0D)",
    "distanceToSqr(e) <= 256.0D",
    "double distanceSq = Math.max(1.0D, distanceToSqr(target))",
    "dx * 0.2D, dy * 0.2D, dz * 0.2D",
    "Mth.clamp((float) next.x, -0.2F, 0.2F)",
    "return 0.001D",
    "return 1.0D",
    "float specialChance = waterImpact ? 10.0F : 1.0F",
    "float strength = waterImpact ? 4.0F : 2.0F",
    "if (!isSeeker() && random.nextInt(100) <= specialChance)",
    "AuraNodeWorldRuntime.createRandomWorldgenProfile",
    "ThaumcraftMod.TAINT_FIBRES.get().defaultBlockState()",
    "public float getPickRadius()",
)
if "DATA_ASPECT" in primal or "setAspect(" in primal:
    errors.append("invented synchronized primal aspect/color still present")

require(
    "src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java",
    "float ramp = Mth.clamp(age / 10.0F, 0.0F, 1.0F)",
    "for (int i = 0; i < 12; i++)",
    "255, 255, 255, 204, 220",
)

for path, expected in {
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusshock.json": {
        "research": "FOCUSSHOCK",
        "pattern": ["CQC", "Q#Q", "CQC"],
        "key": {"#": "minecraft:cauldron", "Q": "minecraft:quartz", "C": "thaumcraft:aer_shard"},
        "aspects": {"AER": 10, "ORDO": 10, "PERDITIO": 10},
        "result_item": "thaumcraft:focus_shock",
    },
    "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusprimal.json": {
        "research": "FOCUSPRIMAL",
        "pattern": ["CQC", "Q#Q", "CQC"],
        "key": {"#": "thaumcraft:primal_charm", "Q": "minecraft:quartz", "C": "minecraft:diamond"},
        "aspects": {"TERRA": 25, "PERDITIO": 25, "ORDO": 25, "AER": 25, "IGNIS": 25, "AQUA": 25},
        "result_item": "thaumcraft:focus_primal",
    },
}.items():
    try:
        data = json.loads((ROOT / path).read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"invalid/missing recipe {path}: {exc}")
        continue
    for key in ("research", "pattern", "key", "aspects"):
        if data.get(key) != expected[key]:
            errors.append(f"{path} {key} mismatch: {data.get(key)!r}")
    if data.get("result", {}).get("item") != expected["result_item"]:
        errors.append(f"{path} result mismatch")

for fake in (
    "src/main/resources/data/thaumcraft/recipes/focus_shock.json",
    "src/main/resources/data/thaumcraft/recipes/focus_shock_original_style.json",
):
    if (ROOT / fake).exists():
        errors.append(f"obsolete fake Shock recipe still present: {fake}")

require(
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json",
    '"focus.upgrade.chainlightning.name": "Цепная молния"',
    '"focus.upgrade.earthshock.name": "Рокот гор"',
    '"focus.upgrade.seeker.name": "Искатель"',
    '"tc.research_name.FOCUSSHOCK": "Набалдашник: Шок"',
    '"tc.research_name.FOCUSPRIMAL": "Набалдашник: Сингулярность"',
)
require(
    "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_focus_shock_primal_original_parity_v11_62_17.json",
    '"normal_special_probability": "2% (nextInt(100) <= 1)"',
    '"water_special_probability": "11% (nextInt(100) <= 10)"',
    '"Taint fibres are placed, but live biome mutation is not yet implemented on the modern biome API."',
)
require(
    ".github/workflows/main.yml",
    "tc4_v11_62_17_focus_shock_primal_original_parity_audit.py",
    "v11.62.17-github-jar",
    "v11.62.17-build-reports",
)

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.17 Focus Shock + Primal original parity audit: OK")
