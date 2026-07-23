#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"
RES = ROOT / "src/main/resources/assets/thaumcraft"
checks = []


def check(name, condition):
    checks.append((name, bool(condition)))


def text(path):
    return path.read_text(encoding="utf-8")


build = text(ROOT / "build.gradle")
mod = text(JAVA / "ThaumcraftMod.java")
client = text(JAVA / "client/ClientModEvents.java")
spread = text(JAVA / "taint/TaintSpreadRuntime.java")
spore = text(JAVA / "entity/TaintSporeEntity.java")
swarmer = text(JAVA / "entity/TaintSporeSwarmerEntity.java")
swarm = text(JAVA / "entity/TaintSwarmEntity.java")
swarmer_model = text(JAVA / "client/render/model/TC4TaintSporeSwarmerModel.java")
swarmer_renderer = text(JAVA / "client/render/TaintSporeSwarmerRenderer.java")
swarm_renderer = text(JAVA / "client/render/TaintSwarmRenderer.java")

check("version_116297", "version = '11.63.23'" in build)
for token in [
    'ENTITY_TYPES.register("taint_swarmer"',
    'ENTITY_TYPES.register("taint_swarm"',
    "TaintSporeSwarmerEntity.createAttributes().build()",
    "TaintSwarmEntity.createAttributes().build()",
]:
    check("registry_" + token.split("(")[0].replace('"', "").replace(".", "_"), token in mod)

for token in [
    "TaintSporeSwarmerRenderer::new",
    "TaintSwarmRenderer::new",
    "TC4TaintSporeSwarmerModel.LAYER",
]:
    check("client_" + token.replace("::", "_").replace(".", "_"), token in client)

for token in [
    "random.nextInt(200) == 0",
    "getEntitiesOfClass(TaintSporeSwarmerEntity.class",
    "new AABB(pos).inflate(16.0D)",
    "level.removeBlock(pos, false)",
    "ThaumcraftMod.TAINT_SWARMER.get().create(level)",
    'TC4Sounds.event("roots")',
]:
    check("ecology_" + token[:32].replace(" ", "_"), token in spread)

for token in [
    "protected void clientSporeTick()",
    "protected void serverSporeTick()",
    "protected void onLethalDamage()",
]:
    check("spore_extension_" + token.split("(")[0].replace(" ", "_"), token in spore)

for token in [
    ".add(Attributes.MAX_HEALTH, 75.0D)",
    "private int spawnCounter = 500",
    "level.getNearestPlayer(this, 16.0D)",
    "releaseSwarms(1)",
    "ThaumcraftMod.TAINT_SWARM.get().create(level)",
    "TC4_RESEARCH_ITEMS.get(\"tc4_taint_tendril\")",
]:
    check("swarmer_" + token[:32].replace(" ", "_"), token in swarmer)

for token in [
    "extends FlyingMob implements Enemy",
    ".add(Attributes.MAX_HEALTH, 30.0D)",
    ".add(Attributes.ATTACK_DAMAGE, 2.0D)",
    "getNearestPlayer(this, 12.0D)",
    "new MobEffectInstance(ThaumcraftMod.TAINT_POISON.get(), 100, 0)",
    'TC4Sounds.event("swarmattack")',
    "TaintSpreadRuntime.isColumnTainted(server, target)",
    "hurt(DamageSource.STARVE, 5.0F)",
]:
    check("swarm_" + token[:32].replace(" ", "_"), token in swarm)

for token in [
    'texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F)',
    'texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)',
    "0.025F * Mth.sin(ageInTicks * 0.075F)",
    "core.render(poseStack, consumer, 0xF000F0",
]:
    check("model_" + token[:28].replace(" ", "_"), token in swarmer_model)

check("swarmer_original_texture", "textures/models/taint_spore.png" in swarmer_renderer)
check("swarmer_fullbright", "return 15;" in swarmer_renderer)
check("swarm_no_body_geometry", "Intentionally no body geometry" in swarm_renderer)
check("swarm_zero_shadow", "shadowRadius = 0.0F" in swarm_renderer)

for locale, expected in [
    ("en_us", ("Taint Spore Swarmer", "Taint Swarm")),
    ("ru_ru", ("Заражённый роевик", "Заражённый рой")),
]:
    data = json.loads(text(RES / f"lang/{locale}.json"))
    check(locale + "_swarmer", data.get("entity.thaumcraft.taint_swarmer") == expected[0])
    check(locale + "_swarm", data.get("entity.thaumcraft.taint_swarm") == expected[1])


manifest = json.loads(text(ROOT / "runtime_artifacts/runtime_test_manifest.template.json"))
manifest_ids = {entry.get("id") for entry in manifest.get("tests", [])}
for test_id in [
    "taint.swarmer_crust_spawn_and_16_block_exclusion",
    "taint.swarmer_500_tick_release_and_death_burst",
    "taint.swarm_flight_poison_velocity_and_summoned_decay",
    "taint.swarmer_swarm_visual_sound_drop_parity",
]:
    check("runtime_case_" + test_id, test_id in manifest_ids)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
print(f"SUMMARY: {len(checks) - len(failed)}/{len(checks)} PASS")
sys.exit(1 if failed else 0)
