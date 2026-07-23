#!/usr/bin/env python3
"""Static source/resource parity guard for v11.63.10 Liquid Death and TC4 damage sources."""
from __future__ import annotations

from pathlib import Path
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"
RES = ROOT / "src/main/resources/assets/thaumcraft"
DATA = ROOT / "src/main/resources/data/thaumcraft"
ORIGINAL = RES / "textures/original/thaumcraft4"
checks: list[tuple[str, bool]] = []


def check(name: str, condition: object) -> None:
    checks.append((name, bool(condition)))


def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def same_bytes(left: Path, right: Path) -> bool:
    return left.is_file() and right.is_file() and hashlib.sha256(left.read_bytes()).digest() == hashlib.sha256(right.read_bytes()).digest()


build = text(ROOT / "build.gradle")
mods = text(ROOT / "src/main/resources/META-INF/mods.toml")
mod = text(JAVA / "ThaumcraftMod.java")
events = text(JAVA / "event/CommonEvents.java")
client = text(JAVA / "client/ClientModEvents.java")
damage = text(JAVA / "damage/TC4DamageSources.java")
block = text(JAVA / "block/LiquidDeathBlock.java")
drops = text(JAVA / "alchemy/LiquidDeathDropRuntime.java")
taint_effect = text(JAVA / "effect/TaintPoisonMobEffect.java")
taintacle = text(JAVA / "entity/TaintacleEntity.java")
taintacle_giant = text(JAVA / "entity/TaintacleGiantEntity.java")
swarm = text(JAVA / "entity/TaintSwarmEntity.java")
known = text(ROOT / "KNOWN_DEVIATIONS.md")
readme = text(ROOT / "README.md")
status = text(ROOT / "TC4_PORT_STATUS_V3.md")

check("build_version", "version = '11.63.23'" in build)
check("mods_version", 'version="11.63.23"' in mods)
check("readme_stage", "11.63.10" in readme and "Liquid Death" in readme)
check("status_stage", "11.63.10" in status and "Liquid Death" in status)
check("finite_fluid_deviation_documented", "BlockFluidFinite" in known and "ForgeFlowingFluid" in known)

# Forge registry and fluid contract.
check("fluid_type_deferred_register", "DeferredRegister<FluidType> FLUID_TYPES" in mod)
check("liquid_death_fluid_type_registry", 'FLUID_TYPES.register("liquid_death"' in mod)
check("fluid_description_id", 'descriptionId("fluid_type.thaumcraft.liquid_death")' in mod)
check("fluid_density", ".density(1200)" in mod)
check("fluid_viscosity", ".viscosity(1500)" in mod)
check("fluid_rare", ".rarity(Rarity.RARE)" in mod)
check("fluid_no_swim", ".canSwim(false)" in mod)
check("fluid_can_drown", ".canDrown(true)" in mod)
check("fluid_no_extinguish", ".canExtinguish(false)" in mod)
check("fluid_fill_sound", "SoundActions.BUCKET_FILL" in mod)
check("fluid_empty_sound", "SoundActions.BUCKET_EMPTY" in mod)
check("fluid_client_extension", "IClientFluidTypeExtensions" in mod)
check("fluid_texture", 'new ResourceLocation(MOD_ID, "block/tc4/fluiddeath")' in mod)
check("source_fluid_registry", 'FLUIDS.register("liquid_death", () -> new ForgeFlowingFluid.Source' in mod)
check("flowing_fluid_registry", 'FLUIDS.register("flowing_liquid_death", () -> new ForgeFlowingFluid.Flowing' in mod)
check("fluid_block_registry", 'BLOCKS.register("liquid_death"' in mod and "new LiquidDeathBlock" in mod)
check("bucket_registry", 'ITEMS.register("tc4_bucket_death"' in mod and "new BucketItem(LIQUID_DEATH_FLUID" in mod)
check("bucket_remainder", ".craftRemainder(Items.BUCKET)" in mod)
check("bucket_stack_one", ".stacksTo(1)" in mod)
check("bucket_replaces_placeholder", 'Map.entry("tc4_bucket_death", LIQUID_DEATH_BUCKET)' in mod)
check("bucket_dispenser", "DispenserBlock.registerBehavior(LIQUID_DEATH_BUCKET.get(), DispenseFluidContainer.getInstance())" in mod)
check("properties_source_flowing_pair", "new ForgeFlowingFluid.Properties(LIQUID_DEATH_FLUID_TYPE, LIQUID_DEATH_FLUID, FLOWING_LIQUID_DEATH_FLUID)" in mod)
check("properties_bucket_block", ".bucket(LIQUID_DEATH_BUCKET).block(LIQUID_DEATH_BLOCK)" in mod)
check("properties_slope_four", ".slopeFindDistance(4)" in mod)
check("properties_four_quanta_adapter", ".levelDecreasePerBlock(2)" in mod)
check("properties_tick_five", ".tickRate(5)" in mod)
check("properties_resistance", ".explosionResistance(100.0F)" in mod)
check("source_translucent_render_layer", "setRenderLayer(ThaumcraftMod.LIQUID_DEATH_FLUID.get(), RenderType.translucent())" in client)
check("flowing_translucent_render_layer", "setRenderLayer(ThaumcraftMod.FLOWING_LIQUID_DEATH_FLUID.get(), RenderType.translucent())" in client)

# Fluid collision and visual behavior.
check("block_extends_liquid", "extends LiquidBlock" in block)
check("server_only_damage", "level.isClientSide" in block)
check("living_only_damage", "entity instanceof LivingEntity living" in block)
check("four_quanta_depth_clamp", "Math.min(3, state.getValue(LEVEL))" in block)
check("damage_ladder", "living.hurt(TC4DamageSources.DISSOLVE, 4.0F - flowDepth)" in block)
check("bubble_particle", "ParticleTypes.BUBBLE" in block)
check("purple_particle", "ParticleTypes.WITCH" in block)
check("particle_height_metadata", "0.12D * flowDepth" in block)
check("lava_pop_one_in_fifty", "random.nextInt(50) == 0" in block and "SoundEvents.LAVA_POP" in block)
check("lava_pop_volume", "0.1F + random.nextFloat() * 0.1F" in block)
check("lava_pop_pitch", "0.9F + random.nextFloat() * 0.15F" in block)

# Named TC4 damage sources.
check("named_source_subclass", "class NamedSource extends DamageSource" in damage)
check("taint_identity", 'new NamedSource("taint")' in damage)
check("taint_bypass_armor", 'TAINT = new NamedSource("taint").bypassArmor()' in damage)
check("taint_magic", ".setMagic()" in damage)
check("dissolve_identity", 'DISSOLVE = new NamedSource("dissolve")' in damage)
check("dissolve_bypass_armor", 'DISSOLVE = new NamedSource("dissolve").bypassArmor()' in damage)
check("tentacle_entity_source", 'new EntityDamageSource("tentacle", attacker)' in damage)
check("swarm_entity_source", 'new EntityDamageSource("swarm", attacker)' in damage)
check("taint_effect_uses_named_source", "TC4DamageSources.TAINT" in taint_effect)
check("taintacle_uses_named_source", "TC4DamageSources.tentacle(this)" in taintacle)
check("giant_taintacle_uses_named_source", "TC4DamageSources.tentacle(this)" in taintacle_giant)
check("swarm_uses_named_source", "TC4DamageSources.swarm(this)" in swarm)
check("tentacle_attack_attribute", "Attributes.ATTACK_DAMAGE" in taintacle)
check("swarm_attack_attribute", "Attributes.ATTACK_DAMAGE" in swarm)

# Dissolve-death aspect crystal conversion.
check("living_drops_import", "LivingDropsEvent" in events)
check("living_drops_hook", "LiquidDeathDropRuntime.handle(event)" in events)
check("drop_runtime_server_only", "victim.level instanceof ServerLevel level" in drops)
check("drop_runtime_source_identity", "event.getSource() != TC4DamageSources.DISSOLVE" in drops)
check("drop_runtime_entity_aspects", "TC4EntityAspectRegistry.getAspectsForEntity(victim)" in drops)
check("drop_runtime_each_aspect", "for (Map.Entry<Aspect, Integer> entry" in drops)
check("drop_runtime_fifty_percent", "victim.getRandom().nextBoolean()" in drops)
check("drop_runtime_exact_random_amount", "1 + victim.getRandom().nextInt(amount)" in drops)
check("drop_runtime_half_amount", "Math.max(1, crystals / 2)" in drops)
check("drop_runtime_attuned_crystal", "EssentiaCrystalItem.create" in drops and "entry.getKey()" in drops)
check("drop_runtime_stack_count", "stack.setCount(crystals)" in drops)
check("drop_runtime_eye_height", "victim.getY() + victim.getEyeHeight()" in drops)
check("drop_runtime_adds_to_event", "event.getDrops().add(new ItemEntity" in drops)

# Resources, data and localizations.
blockstate = json.loads(text(RES / "blockstates/liquid_death.json"))
blockmodel = json.loads(text(RES / "models/block/liquid_death.json"))
bucketmodel = json.loads(text(RES / "models/item/tc4_bucket_death.json"))
check("blockstate_model", blockstate.get("variants", {}).get("", {}).get("model") == "thaumcraft:block/liquid_death")
check("block_particle_texture", blockmodel.get("textures", {}).get("particle") == "thaumcraft:block/tc4/fluiddeath")
check("bucket_generated_model", bucketmodel.get("parent") == "item/generated")
check("bucket_original_texture_model", bucketmodel.get("textures", {}).get("layer0") == "thaumcraft:item/tc4/bucket_death")
check("fluid_texture_exact_original", same_bytes(RES / "textures/block/tc4/fluiddeath.png", ORIGINAL / "blocks/fluiddeath.png"))
check("fluid_mcmeta_exact_original", same_bytes(RES / "textures/block/tc4/fluiddeath.png.mcmeta", ORIGINAL / "blocks/fluiddeath.png.mcmeta"))
check("bucket_texture_exact_original", same_bytes(RES / "textures/item/tc4/bucket_death.png", ORIGINAL / "items/bucket_death.png"))
recipe = json.loads(text(DATA / "thaumcraft_alchemy/tc4_liquiddeath.json"))
check("alchemy_recipe_output_bucket", recipe.get("result", {}).get("item") == "thaumcraft:tc4_bucket_death")

for locale in ("en_us", "ru_ru"):
    lang = json.loads(text(RES / "lang" / f"{locale}.json"))
    for key in (
        "block.thaumcraft.liquid_death",
        "fluid_type.thaumcraft.liquid_death",
        "item.thaumcraft.tc4_bucket_death",
        "death.attack.dissolve",
        "death.attack.dissolve.player",
        "death.attack.taint",
        "death.attack.taint.player",
        "death.attack.tentacle",
        "death.attack.tentacle.item",
        "death.attack.swarm",
        "death.attack.swarm.item",
    ):
        check(f"{locale}_{key}", bool(lang.get(key)))

manifest = json.loads(text(ROOT / "runtime_artifacts/runtime_test_manifest.template.json"))
check("manifest_version", manifest.get("version") in ("11.63.23", "11.63.24", "11.63.26", "11.63.27", "11.63.27", "11.63.28", "11.63.29", "11.63.30", "11.63.31", "11.63.32", "11.63.33", "11.63.36", "11.63.37", "11.63.38", "11.63.39", "11.63.40", "11.63.41", "11.63.42", "11.63.43", "11.63.44", "11.63.45", "11.63.46", "11.63.47", "11.63.48", "11.63.49", "11.63.50", "11.63.52", "11.63.53", "11.63.54", "11.63.55", "11.63.56", "11.63.58", "11.63.59", "11.63.60", "11.63.61"))
ids = {case.get("id") for case in manifest.get("tests", [])}
expected_ids = (
    "alchemy.liquid_death_bucket_place_pickup_and_dispenser",
    "alchemy.liquid_death_four_quanta_damage_ladder",
    "alchemy.liquid_death_dissolve_death_messages",
    "alchemy.liquid_death_entity_aspect_crystal_drops",
    "taint.named_taint_damage_bypasses_armor_magic",
    "taint.tentacle_swarm_named_attacker_death_messages",
)
for case_id in expected_ids:
    check("runtime_" + case_id, case_id in ids)
check("manifest_82_cases", len(manifest.get("tests", [])) >= 82)

for workflow_name in ("build.yml", "release.yml"):
    workflow = text(ROOT / ".github/workflows" / workflow_name)
    check(workflow_name + "_poison_guard", "tc4_116298_taint_poison_resource_parity_guard.py" in workflow)
    check(workflow_name + "_conversion_guard", "tc4_116299_taint_death_conversion_parity_guard.py" in workflow)
    check(workflow_name + "_liquid_death_guard", "tc4_116300_liquid_death_damage_source_parity_guard.py" in workflow)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
print(f"Liquid Death / damage-source parity guard: {len(checks) - len(failed)}/{len(checks)}")
sys.exit(1 if failed else 0)
