#!/usr/bin/env python3
"""v11.62.16 Focus Frost original-parity source/resource audit."""
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

require("build.gradle", "version = '11.62.16'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.16"')

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "case FROST -> castFrost(wandStack, level, player)",
    "? 10 : 4",
    "private static boolean castFrost(ItemStack wandStack, Level level, Player player)",
    "int potency = focusPotency(wandStack)",
    "int count = 5 + potency * 2",
    "shard.setDamage(1.0F)",
    "shard.setFragile(true)",
    "shard.setDamage(4.0F + potency * 2.0F)",
    "shard.setBounce(0.8D, 6)",
    "shard.setDamage((float) (3.0D + potency * 1.5D))",
    "shootProjectile(shard, player, 1.5F, 8.0F)",
    "shootProjectile(shard, player, 1.5F, 1.0F)",
    "player.swing(hand, true)",
)
for expected in (
    "cost(Aspect.AQUA, 5, Aspect.IGNIS, 2, Aspect.PERDITIO, 2)",
    "cost(Aspect.AQUA, 20, Aspect.IGNIS, 2, Aspect.PERDITIO, 2, Aspect.AER, 5)",
    "cost(Aspect.AQUA, 20, Aspect.IGNIS, 2, Aspect.PERDITIO, 2, Aspect.TERRA, 5)",
):
    if expected not in runtime:
        errors.append(f"Focus Frost cost mismatch: missing {expected!r}")

upgrade_runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case 1, 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.ALCHEMISTS_FROST)",
    "case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.SCATTERSHOT, FocusUpgradeType.ICE_BOULDER, FocusUpgradeType.ALCHEMISTS_FROST)",
)
if "focus == WandFocusType.FROST && type == FocusUpgradeType.ALCHEMISTS_FROST" in upgrade_runtime:
    errors.append("invented Alchemist's Frost upgrade gate still present")

require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java",
    'ALCHEMISTS_FROST(5', '"COLD,TRAP"',
    'SCATTERSHOT(11', '"COLD,WEAPON"',
    'ICE_BOULDER(12', '"COLD,CRYSTAL"',
)

entity = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4FrostShardEntity.java",
    "EntityDataSerializers.FLOAT",
    "EntityDataSerializers.INT",
    "EntityDataSerializers.BOOLEAN",
    "entityData.define(DATA_BOUNCE, 0.5F)",
    "entityData.define(DATA_BOUNCE_LIMIT, 3)",
    "0.15F + getDamage() * 0.15F",
    "DamageSource.thrown(this, owner)",
    "MobEffects.MOVEMENT_SLOWDOWN, 200, getFrosty() - 1",
    "maxLife = Integer.MAX_VALUE",
    "motion.scale(0.66D * getBounce())",
    "motion.scale(getBounce())",
    "Vec3 correction = motion.normalize().scale(0.05D)",
    "int previousLimit = getBounceLimit()",
    "if (previousLimit <= 0)",
    "living.invulnerableTime = 0",
    "changed.subtract(oldMotion).scale(0.1D)",
    "new BlockParticleOption(ParticleTypes.BLOCK, state)",
    "return isFragile() ? 0.015D : 0.05D",
)

require(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "FOCUS_FROST_SHARD.get(), TC4FrostShardRenderer::new",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/render/TC4FrostShardRenderer.java",
    "RandomSource.create(shard.getId())",
    "float base = shard.getVisualDamage() * 0.1F",
    "TC4FrostShardModel.TRIANGLES",
    "RenderType.entityTranslucent(TEXTURE)",
    "Matrix3f normal = poseStack.last().normal()",
    ".normal(normal, data[index + 5], data[index + 6], data[index + 7])",
)
mesh = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/model/TC4FrostShardModel.java",
    "original TC4 textures/models/orb.obj",
    "public static final int STRIDE = 8",
)
if mesh.count("F,") < 1500:
    errors.append("Frost orb mesh appears truncated")
if (ROOT / "src/main/java/com/darkifov/thaumcraft/client/render/TC4OrbMesh.java").exists():
    errors.append("temporary duplicate Frost orb mesh still present")

require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'ENTITY_TYPES.register("focus_frost_shard"',
    '.updateInterval(1)',
)

require(
    "src/main/java/com/darkifov/thaumcraft/block/WandFocusItem.java",
    "FocusUpgradeType.ICE_BOULDER",
    "new AspectList().add(Aspect.AQUA, 20).add(Aspect.IGNIS, 2).add(Aspect.PERDITIO, 2).add(Aspect.TERRA, 5)",
    "FocusUpgradeType.SCATTERSHOT",
    "new AspectList().add(Aspect.AQUA, 20).add(Aspect.IGNIS, 2).add(Aspect.PERDITIO, 2).add(Aspect.AER, 5)",
)
require(
    "src/main/resources/assets/thaumcraft/lang/ru_ru.json",
    '"focus.upgrade.alchemistsfrost.name": "Лёд алхимика"',
    '"focus.upgrade.scattershot.name": "Ледяной шторм"',
    '"focus.upgrade.iceboulder.name": "Ледяной булыжник"',
    '"tc.research_name.FOCUSFROST": "Набалдашник: Заморозка"',
)

recipe_path = ROOT / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_focusfrost.json"
try:
    recipe = json.loads(recipe_path.read_text(encoding="utf-8"))
except Exception as exc:
    errors.append(f"invalid/missing Focus Frost arcane recipe: {exc}")
else:
    expected = {
        "research": "FOCUSFROST",
        "pattern": ["CQC", "Q#Q", "CQC"],
        "key": {"#": "minecraft:diamond", "Q": "minecraft:quartz", "C": "thaumcraft:aqua_shard"},
        "aspects": {"AQUA": 10, "ORDO": 10, "PERDITIO": 10},
    }
    for key, value in expected.items():
        if recipe.get(key) != value:
            errors.append(f"Focus Frost recipe {key} mismatch: {recipe.get(key)!r}")
    if recipe.get("result", {}).get("item") != "thaumcraft:focus_frost":
        errors.append("Focus Frost recipe result mismatch")

for fake in (
    "src/main/resources/data/thaumcraft/recipes/focus_frost.json",
    "src/main/resources/data/thaumcraft/recipes/focus_frost_original_style.json",
):
    if (ROOT / fake).exists():
        errors.append(f"obsolete fake Focus Frost recipe still present: {fake}")

require(
    ".github/workflows/main.yml",
    "tc4_v11_62_16_focus_frost_original_parity_audit.py",
    "v11.62.16-github-jar",
    "v11.62.16-build-reports",
)

if errors:
    for error in errors:
        print(f"::error::{error}")
    sys.exit(1)
print("TC4 v11.62.16 Focus Frost original parity audit: OK")
