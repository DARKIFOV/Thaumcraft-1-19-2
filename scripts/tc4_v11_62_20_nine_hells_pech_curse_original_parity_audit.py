#!/usr/bin/env python3
"""v11.62.20 Nine Hells / Firebat and Pech's Curse parity audit."""
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


require("build.gradle", "version = '11.62.20'", "version = '11.62.19'")
require("src/main/resources/META-INF/mods.toml", 'version="11.62.20"', 'version="11.62.19"')

types = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusType.java",
    'HELLBAT("hellbat", "Wand Focus: Nine Hells", "focus_hellbat", 14431746, 20',
    'PECH_CURSE("pech_curse", "Wand Focus: Pech\'s Curse", "focus_pech", 2267460, 5',
    "cost(Aspect.IGNIS, 200, Aspect.PERDITIO, 100, Aspect.AER, 100)",
    "cost(Aspect.TERRA, 10, Aspect.PERDITIO, 10, Aspect.AQUA, 10)",
)

upgrade_types = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeType.java",
    'BAT_BOMBS(13, "batbombs"',
    'DEVIL_BATS(14, "devilbats"',
    'NIGHTSHADE(15, "nightshade"',
    'VAMPIRE_BATS(19, "vampirebats"',
    '"HUNGER,LIFE"',
)

upgrades = require(
    "src/main/java/com/darkifov/thaumcraft/wand/FocusUpgradeRuntime.java",
    "case HELLBAT -> switch (rank)",
    "case 1, 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY)",
    "case 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.BAT_BOMBS, FocusUpgradeType.DEVIL_BATS)",
    "case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.VAMPIRE_BATS)",
    "case PECH_CURSE -> switch (rank)",
    "case 1, 3 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY)",
    "case 2, 4 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.EXTEND)",
    "case 5 -> a(FocusUpgradeType.FRUGAL, FocusUpgradeType.POTENCY, FocusUpgradeType.NIGHTSHADE)",
    'PlayerThaumData.hasResearch(player, "VAMPBAT")',
)

runtime = require(
    "src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java",
    "case HELLBAT -> castHellbat(wandStack, level, player)",
    "case PECH_CURSE -> castPechCurse(wandStack, level, player)",
    "case HELLBAT -> 20",
    "case PECH_CURSE -> 5",
    "ray(level, player, 32.0D)",
    "target instanceof TC4FireBatEntity",
    "!level.getServer().isPvpAllowed()",
    "case HELLBAT -> focusHasUpgrade(wandStack, FocusUpgradeType.DEVIL_BATS)",
    "cost(Aspect.IGNIS, 100, Aspect.PERDITIO, 100, Aspect.AER, 100, Aspect.TERRA, 100)",
    "cost(Aspect.IGNIS, 100, Aspect.PERDITIO, 200, Aspect.AER, 100)",
    "cost(Aspect.IGNIS, 200, Aspect.PERDITIO, 100, Aspect.AER, 100)",
    "focusHasUpgrade(wandStack, FocusUpgradeType.VAMPIRE_BATS)",
    "new TC4PechBlastEntity(ThaumcraftMod.FOCUS_PECH_BLAST.get(), level, player)",
    "focusUpgradeLevel(wandStack, FocusUpgradeType.EXTEND)",
    "focusHasUpgrade(wandStack, FocusUpgradeType.NIGHTSHADE)",
    "shootProjectile(blast, player, 1.5F, 1.0F)",
    "cost(Aspect.AER, 10, Aspect.IGNIS, 10, Aspect.TERRA, 10, Aspect.ORDO, 10, Aspect.PERDITIO, 10, Aspect.AQUA, 10)",
    'TC4ResearchItems.registered("tc4_focus_hellbat")',
    'TC4ResearchItems.registered("tc4_focus_pech")',
)

items = require(
    "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java",
    'case "tc4_focus_hellbat"',
    'case "tc4_focus_pech"',
    "new WandFocusItem",
    "WandFocusType.HELLBAT",
    "WandFocusType.PECH_CURSE",
)

firebat = require(
    "src/main/java/com/darkifov/thaumcraft/entity/TC4FireBatEntity.java",
    "setNoGravity(true)",
    ".add(Attributes.MAX_HEALTH, 5.0D)",
    ".add(Attributes.FOLLOW_RANGE, 32.0D)",
    "devil ? 15.0D : 5.0D",
    "Math.signum(delta.x) * 0.5D",
    "Math.signum(delta.y) * 0.7D",
    "Math.signum(delta.z) * 0.5D",
    "if (isSummoned()) hurt(DamageSource.MAGIC, 2.0F)",
    "targetPlayer.getAbilities().instabuild",
    "target.setLastHurtByPlayer(owner)",
    "new MobEffectInstance(MobEffects.REGENERATION, 26, 1)",
    "heal(1.0F)",
    "random.nextInt(10) == 0",
    "1.5F + (isExplosive() ? potency() * 0.33F : 0.0F)",
    "Explosion.BlockInteraction.NONE",
    "float damage = (isDevil() ? 3.0F : 2.0F) + potency()",
    "target.setSecondsOnFire(isSummoned() ? 4 : 2)",
    "return source.isFire() || source.isExplosion()",
    "public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }",
    'tag.putUUID("tc4Owner", uuid)',
    'tag.putUUID("tc4Target", uuid)',
)
if "targetlessTicks > 200" in firebat:
    errors.append("invented 200-tick targetless lifetime returned")

pech_blast = require(
    "src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4PechBlastEntity.java",
    "maxLife = 500",
    "protected double tc4Gravity() { return 0.025D; }",
    "getBoundingBox().inflate(2.0D)",
    "living != owner && !(living instanceof PechEntity)",
    "strength() + 2.0F",
    "int duration = 100 + extend() * 40",
    "new MobEffectInstance(MobEffects.HUNGER, duration, potency)",
    "new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, potency + 1)",
    "new MobEffectInstance(MobEffects.WEAKNESS, duration, potency)",
    "switch (random.nextInt(3))",
    'tag.putBoolean("nightshade", nightshade())',
)

model = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/model/TC4FireBatModel.java",
    "LayerDefinition.create(mesh, 64, 64)",
    "addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)",
    "addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F)",
    "addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F)",
    "addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F)",
    "Mth.cos(ageInTicks * 1.3F) * Mth.PI * 0.25F",
)

renderer = require(
    "src/main/java/com/darkifov/thaumcraft/client/render/TC4FireBatRenderer.java",
    '"textures/models/firebat.png"',
    '"textures/models/vampirebat.png"',
    "entity.isDevil() || entity.isVampire() ? 0.60F : 0.35F",
    "Mth.cos(ageInTicks * 0.3F) * 0.1F",
    "protected int getBlockLightLevel",
    "return 15",
)

mod = require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'ENTITY_TYPES.register("firebat"',
    ".sized(0.5F, 0.9F)",
    ".fireImmune()",
    'ENTITY_TYPES.register("focus_pech_blast"',
    "event.put(FIREBAT.get(), TC4FireBatEntity.createAttributes().build())",
)
client = require(
    "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java",
    "EntityRenderers.register(ThaumcraftMod.FIREBAT.get(), TC4FireBatRenderer::new)",
    "EntityRenderers.register(ThaumcraftMod.FOCUS_PECH_BLAST.get()",
    "event.registerLayerDefinition(TC4FireBatModel.LAYER, TC4FireBatModel::createBodyLayer)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java",
    "entity instanceof TC4PechBlastEntity pech",
    "renderPechBlast(pech, partialTicks, poseStack, buffer)",
)
require(
    "src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java",
    "case PECH_CURSE -> originalItemTexture(\"focus_pech_depth\")",
    "case HELLBAT -> originalItemTexture(\"focus_hellbat_orn\")",
)
require(
    "src/main/java/com/darkifov/thaumcraft/block/FocusPouchItem.java",
    'case HELLBAT -> "HH"',
    'case PECH_CURSE -> "PP"',
)
require(
    "src/main/java/com/darkifov/thaumcraft/entity/PechEntity.java",
    'variant == Variant.ELDRITCH && tier >= 5 && random.nextInt(100) < 20',
    'TC4ResearchItems.registered("tc4_focus_pech")',
)

recipe = load_json("src/main/resources/data/thaumcraft/thaumcraft_infusion/tc4_focushellbat.json")
expected = {
    "research": "FOCUSHELLBAT",
    "catalyst": "minecraft:carrot",
    "components": [
        "minecraft:quartz",
        "thaumcraft:ignis_shard",
        "minecraft:quartz",
        "thaumcraft:aer_shard",
        "minecraft:quartz",
        "thaumcraft:perditio_shard",
    ],
    "aspects": {"IGNIS": 25, "AER": 15, "BESTIA": 15, "PERDITIO": 25},
    "instability": 3,
    "result": {"item": "thaumcraft:tc4_focus_hellbat", "count": 1},
}
for key, value in expected.items():
    if recipe.get(key) != value:
        errors.append(f"Nine Hells infusion {key} mismatch: {recipe.get(key)!r}")

# Pech's Curse is trade-only in original TC4. A fabricated crafting recipe must not appear.
for p in (ROOT / "src/main/resources/data").rglob("*.json"):
    rel = p.relative_to(ROOT).as_posix()
    if "tc4_source_mapping" in rel or "lang" in rel:
        continue
    try:
        data = json.loads(p.read_text(encoding="utf-8"))
    except Exception:
        continue
    if data.get("result") == "thaumcraft:tc4_focus_pech" or data.get("result") == {"item": "thaumcraft:tc4_focus_pech", "count": 1}:
        errors.append(f"fake Pech's Curse crafting recipe found: {rel}")

for texture in (
    "src/main/resources/assets/thaumcraft/textures/models/firebat.png",
    "src/main/resources/assets/thaumcraft/textures/models/vampirebat.png",
    "src/main/resources/assets/thaumcraft/textures/item/tc4/focus_hellbat.png",
    "src/main/resources/assets/thaumcraft/textures/item/tc4/focus_hellbat_orn.png",
    "src/main/resources/assets/thaumcraft/textures/item/tc4/focus_pech.png",
    "src/main/resources/assets/thaumcraft/textures/item/tc4/focus_pech_depth.png",
    "src/main/resources/assets/thaumcraft/textures/item/tc4/foci/vampirebats.png",
):
    if not (ROOT / texture).is_file():
        errors.append(f"missing original texture: {texture}")

for lang_file in ("en_us.json", "ru_ru.json"):
    lang = load_json(f"src/main/resources/assets/thaumcraft/lang/{lang_file}")
    for key in (
        "item.thaumcraft.tc4_focus_hellbat",
        "item.thaumcraft.tc4_focus_pech",
        "entity.thaumcraft.firebat",
        "entity.thaumcraft.focus_pech_blast",
        "focus.upgrade.batbombs.name",
        "focus.upgrade.devilbats.name",
        "focus.upgrade.vampirebats.name",
        "focus.upgrade.nightshade.name",
        "tc.research_name.FOCUSHELLBAT",
        "tc.research_name.VAMPBAT",
    ):
        if not lang.get(key):
            errors.append(f"{lang_file} missing language key {key}")

mapping = load_json("src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_v11_62_20_nine_hells_pech_curse_original_parity.json")
if mapping.get("version") != "11.62.20" or not mapping.get("strict_original"):
    errors.append("v11.62.20 source mapping metadata mismatch")

workflow = require(
    ".github/workflows/main.yml",
    "tc4_v11_62_20_nine_hells_pech_curse_original_parity_audit.py",
    "thaumcraft-legacy-rebuild-1.19.2-v11.62.20-github-jar",
    "v11.62.20-build-reports",
)

if errors:
    print("TC4 v11.62.20 Nine Hells + Pech's Curse parity audit FAILED:")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print("TC4 v11.62.20 Nine Hells + Pech's Curse original parity audit: OK")
