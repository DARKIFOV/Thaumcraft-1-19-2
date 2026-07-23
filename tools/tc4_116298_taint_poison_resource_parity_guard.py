#!/usr/bin/env python3
"""Static parity guard for v11.63.10 dedicated Taint Poison and infected resources."""
from pathlib import Path
import json
import sys
import hashlib
import struct

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"
RES = ROOT / "src/main/resources/assets/thaumcraft"
checks = []


def check(name, condition):
    checks.append((name, bool(condition)))


def text(path):
    return path.read_text(encoding="utf-8")


build = text(ROOT / "build.gradle")
mods = text(ROOT / "src/main/resources/META-INF/mods.toml")
mod = text(JAVA / "ThaumcraftMod.java")
effect = text(JAVA / "effect/TaintPoisonMobEffect.java")
resource = text(JAVA / "item/TaintedResourceItem.java")
research_items = text(JAVA / "porting/TC4ResearchItems.java")
spider = text(JAVA / "entity/TaintCrawlerEntity.java")
block = text(JAVA / "block/TaintBlock.java")
fibres = text(JAVA / "block/TaintFibresBlock.java")
swarm = text(JAVA / "entity/TaintSwarmEntity.java")
infusion = text(JAVA / "infusion/InfusionInstabilityEvents.java")

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("effect_registry", 'MOB_EFFECTS.register("taint_poison", TaintPoisonMobEffect::new)' in mod)
check("active_goo_item", 'new TaintedResourceItem' in mod and '"ConfigItems.itemResource meta 11"' in mod)
check("legacy_tendril_item", '"tc4_taint_tendril"' in research_items and 'new TaintedResourceItem' in research_items)

for name, token in [
    ("effect_harmful", "MobEffectCategory.HARMFUL"),
    ("effect_colour", "0x663377"),
    ("tainted_marker_heal", "target instanceof TaintedMob"),
    ("heals_one", "target.heal(1.0F)"),
    ("undead_immune", "!target.isInvertedHealAndHarm()"),
    ("magic_damage_one", "target.hurt(TC4DamageSources.TAINT, 1.0F)"),
    ("cadence_40_shift", "40 >> Math.max(0, amplifier)"),
]:
    check(name, token in effect)

for name, token in [
    ("server_only_inventory_tick", "level.isClientSide"),
    ("living_only", "entity instanceof LivingEntity living"),
    ("undead_inventory_immune", "living.isInvertedHealAndHarm()"),
    ("no_refresh_while_active", "living.hasEffect(ThaumcraftMod.TAINT_POISON.get())"),
    ("original_roll_bound", "level.random.nextInt(4321) > stack.getCount()"),
    ("effect_120_ticks", "ThaumcraftMod.TAINT_POISON.get(), 120, 0"),
    ("translated_warning", 'Component.translatable("tc.taint_item_poison", itemName)' in resource),
    ("creative_safe_consumption", "!player.getAbilities().instabuild"),
    ("consume_one", "stack.shrink(1)"),
]:
    check(name, token if isinstance(token, bool) else token in resource)

for entity in [
    "TaintCrawlerEntity", "TaintSporeEntity", "TaintSwarmEntity",
    "TaintacleEntity", "TaintacleGiantEntity", "TC4ThaumicSlimeEntity",
]:
    source = text(JAVA / f"entity/{entity}.java")
    check(f"tainted_marker_{entity}", "TaintedMob" in source.split("{", 1)[0])

for name, source in [
    ("taint_block", block), ("taint_fibres", fibres),
    ("taint_swarm", swarm), ("infusion_instability", infusion),
]:
    check(name + "_uses_custom_effect", "ThaumcraftMod.TAINT_POISON.get()" in source)

check("spider_one_in_six", "random.nextInt(6) == 0" in spider)
check("spider_fifty_fifty", "random.nextBoolean()" in spider)
check("spider_goo_drop", "ThaumcraftMod.TAINTED_SLIME.get()" in spider)
check("spider_tendril_drop", 'TC4_RESEARCH_ITEMS.get("tc4_taint_tendril")' in spider)

check("flux_scrubber_not_fake_manual_cleanser", "ThaumcraftMod.TAINT_POISON.get()" not in text(JAVA / "block/FumeDissipatorBlock.java"))

for rel in [
    "block/ThaumicTinkererParityItem.java",
    "block/ThaumicTinkererDeviceBlock.java",
    "blockentity/AuraNodeBlockEntity.java",
]:
    check("cleanse_" + Path(rel).stem, "ThaumcraftMod.TAINT_POISON.get()" in text(JAVA / rel))

for locale, effect_name, goo_name, tendril_name in [
    ("en_us", "Taint Poison", "Tainted Goo", "Taint Tendril"),
    ("ru_ru", "Магический яд", "Заражённая слизь", "Заражённое щупальце"),
]:
    data = json.loads(text(RES / f"lang/{locale}.json"))
    check(locale + "_effect_name", data.get("effect.thaumcraft.taint_poison") == effect_name)
    check(locale + "_goo_name", data.get("item.thaumcraft.tainted_slime") == goo_name)
    check(locale + "_tendril_name", data.get("item.thaumcraft.tc4_taint_tendril") == tendril_name)
    check(locale + "_warning", "%s" in data.get("tc.taint_item_poison", ""))

icon_path = RES / "textures/mob_effect/taint_poison.png"
check("icon_exists", icon_path.is_file())
if icon_path.is_file():
    payload = icon_path.read_bytes()
    png_ok = payload.startswith(b"\x89PNG\r\n\x1a\n") and len(payload) >= 24
    check("icon_png", png_ok)
    width, height = struct.unpack(">II", payload[16:24]) if png_ok else (0, 0)
    check("icon_size_18", (width, height) == (18, 18))
    check("icon_exact_original_cell_3_1", hashlib.sha256(payload).hexdigest() == "ac11cb4f90ca145fb316a861efffce3296ecf94daea4459f0d6feac0d3c64121")
else:
    check("icon_png", False)
    check("icon_size_18", False)
    check("icon_exact_original_cell_3_1", False)

manifest = json.loads(text(ROOT / "runtime_artifacts/runtime_test_manifest.template.json"))
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
ids = {entry.get("id") for entry in manifest.get("tests", [])}
for test_id in [
    "taint.poison_tick_heals_tainted_harms_living",
    "taint.resource_inventory_infection_consumption",
    "taint.spider_dual_resource_drop_distribution",
    "taint.poison_sources_icon_cleanse_multiplayer_sync",
]:
    check("runtime_" + test_id, test_id in ids)
check("manifest_70_cases", len(manifest.get("tests", [])) >= 70)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
print(f"Taint Poison/resource parity guard: {len(checks)-len(failed)}/{len(checks)}")
sys.exit(1 if failed else 0)
