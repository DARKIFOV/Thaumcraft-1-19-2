#!/usr/bin/env python3
"""Static release guard for v11.62.68 Purifying Fluid, Bath Salts and Arcane Spa."""
from __future__ import annotations

import argparse
import json
import pathlib
import sys
from typing import Any


def add_check(checks: list[dict[str, Any]], name: str, passed: bool, details: str) -> None:
    checks.append({"name": name, "passed": bool(passed), "details": details})


def read(root: pathlib.Path, relative: str) -> str:
    return (root / relative).read_text(encoding="utf-8")


def read_json(root: pathlib.Path, relative: str) -> Any:
    return json.loads(read(root, relative))


def contains_all(text: str, tokens: list[str]) -> bool:
    return all(token in text for token in tokens)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--version", default="11.62.68")
    parser.add_argument("--json-out", default="reports/tc4_purifying_fluid_spa_audit_v11.62.68.json")
    args = parser.parse_args()

    root = pathlib.Path(args.root).resolve()
    checks: list[dict[str, Any]] = []

    build = read(root, "build.gradle")
    mod = read(root, "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java")
    bath = read(root, "src/main/java/com/darkifov/thaumcraft/block/BathSaltsItem.java")
    pure = read(root, "src/main/java/com/darkifov/thaumcraft/block/PurifyingFluidBlock.java")
    spa_block = read(root, "src/main/java/com/darkifov/thaumcraft/block/ArcaneSpaBlock.java")
    spa_be = read(root, "src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneSpaBlockEntity.java")
    events = read(root, "src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java")
    client = read(root, "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java")
    soap = read(root, "src/main/java/com/darkifov/thaumcraft/block/SanitySoapItem.java")
    arcane = read_json(root, "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanespa.json")
    alchemy = read_json(root, "src/main/resources/data/thaumcraft/thaumcraft_alchemy/tc4_bathsalts.json")
    fluid_model = read_json(root, "src/main/resources/assets/thaumcraft/models/block/purifying_fluid.json")
    fluid_state = read_json(root, "src/main/resources/assets/thaumcraft/blockstates/purifying_fluid.json")
    spa_model = read_json(root, "src/main/resources/assets/thaumcraft/models/block/tc4_block_arcane_spa.json")
    water_tag = read_json(root, "src/main/resources/data/minecraft/tags/fluids/water.json")

    add_check(checks, "version", f"version = '{args.version}'" in build, args.version)
    add_check(checks, "fluid_deferred_registers", contains_all(mod, [
        "DeferredRegister<FluidType> FLUID_TYPES", "DeferredRegister<Fluid> FLUIDS",
        "FLUID_TYPES.register(modBus)", "FLUIDS.register(modBus)",
    ]), "FluidType and source/flowing Fluid registries are attached")
    add_check(checks, "purifying_fluid_registration", contains_all(mod, [
        'FLUID_TYPES.register("purifying_fluid"', 'FLUIDS.register("purifying_fluid"',
        'FLUIDS.register("flowing_purifying_fluid"', 'BLOCKS.register("purifying_fluid"',
        'ITEMS.register("tc4_bucket_pure"', ".bucket(PURIFYING_FLUID_BUCKET).block(PURIFYING_FLUID_BLOCK)",
    ]), "source, flowing, block and bucket registry objects")
    add_check(checks, "fluid_properties", contains_all(mod, [
        ".lightLevel(10)", ".density(1000)", ".viscosity(1000)", ".rarity(Rarity.RARE)",
        ".canSwim(true)", ".canDrown(true)", ".canExtinguish(true)",
    ]), "TC4 luminous water-like fluid properties")
    add_check(checks, "bucket_dispenser", "DispenseFluidContainer.getInstance()" in mod, "custom bucket dispenser behavior")
    add_check(checks, "functional_ids_pre_registered", contains_all(mod, [
        'Map.entry("tc4_bath_salts", BATH_SALTS)',
        'Map.entry("tc4_bucket_pure", PURIFYING_FLUID_BUCKET)',
        'Map.entry("tc4_block_arcane_spa", ARCANE_SPA_ITEM)',
    ]), "flat research-item mirrors are bypassed")

    add_check(checks, "bath_salts_lifespan", contains_all(bath, [
        "DISSOLVE_TICKS = 200", "getEntityLifespan", "return DISSOLVE_TICKS",
    ]), "original 200-tick entity lifespan")
    add_check(checks, "bath_salts_water_conversion", contains_all(events, [
        "onBathSaltsExpire(ItemExpireEvent event)", "BATH_SALTS.get()",
        "fluidState.isSource()", "Fluids.WATER", "PURIFYING_FLUID_BLOCK.get().defaultBlockState()",
    ]), "expired salts replace only a vanilla water source")

    add_check(checks, "warp_ward_formula", contains_all(pure, [
        "PlayerThaumData.getWarpPerm(player)", "Math.floor(Math.sqrt(permanentWarp))",
        "Math.max(1", "Math.min(32000, 200000 / divisor)",
        "ThaumcraftMod.WARP_WARD.get()", "PlayerThaumData.setWarpWardTicks(player, duration)",
    ]), "permanent-Warp divisor and capped duration")
    add_check(checks, "single_use_source", contains_all(pure, [
        "level.getFluidState(pos).isSource()", "Blocks.AIR.defaultBlockState()",
    ]), "only a source grants ward and the source is consumed")
    add_check(checks, "no_non_tc4_activation_sound", "BEACON_ACTIVATE" not in pure, "no invented collision activation sound")
    add_check(checks, "fluid_ambient_fx", contains_all(pure, [
        "ParticleTypes.BUBBLE", "random.nextInt(25) == 0", "SoundEvents.LAVA_POP",
        "0.9F + random.nextFloat() * 0.15F",
    ]), "white bubble carrier plus 1/25 lava-pop cadence")

    add_check(checks, "spa_block_entity_registration", contains_all(mod, [
        'BLOCKS.register("tc4_block_arcane_spa"', 'BLOCK_ENTITIES.register("tc4_block_arcane_spa"',
        "ArcaneSpaBlockEntity::new", "ARCANE_SPA.get()",
    ]), "functional block and block entity under saved TC4 ID")
    add_check(checks, "spa_capacity_slot", contains_all(spa_be, [
        "CAPACITY = 5000", "BUCKET = 1000", "new FluidTank(CAPACITY)",
        "new ItemStackHandler(1)", "stack.is(ThaumcraftMod.BATH_SALTS.get())",
    ]), "5000 mB tank and one Bath Salts slot")
    add_check(checks, "spa_tick_redstone", contains_all(spa_be, [
        "level.hasNeighborSignal(pos)", "level.getGameTime() % 40L != 0L", "spa.tryDispense(level, pos)",
    ]), "40-tick cadence disabled by redstone")
    add_check(checks, "spa_mix_mode", contains_all(spa_be, [
        "if (mixing)", "isVanillaWater(stored.getFluid())", "PURIFYING_FLUID.get()",
        "salts.extractItem(0, 1, false)", "tank.drain(BUCKET",
    ]), "water + salts becomes one bucket of Purifying Fluid")
    add_check(checks, "spa_dispense_mode", contains_all(spa_be, [
        "targetFluid = stored.getFluid()", "targetFluid.defaultFluidState().createLegacyBlock()",
    ]), "non-mixing mode dispenses placeable stored fluid")
    add_check(checks, "spa_expansion", contains_all(spa_be, [
        "for (int x = -2; x <= 2; x++)", "for (int z = -2; z <= 2; z++)",
        "touchesTargetSource", "Direction.Plane.HORIZONTAL", "isFaceSturdy",
    ]), "5x5 supported adjacent-source expansion")
    add_check(checks, "spa_ultrawarm_guard", "level.dimensionType().ultraWarm() && isVanillaWater(targetFluid)" in spa_be,
              "vanilla water is not placed in ultra-warm dimensions")
    add_check(checks, "spa_side_automation", spa_be.count("side != Direction.UP") >= 2,
              "item and fluid automation available on non-top faces")
    add_check(checks, "spa_nbt_sync", contains_all(spa_be, [
        'tag.putBoolean("Mix", mixing)', 'tag.put("Tank"', 'tag.put("Salts"',
        "getUpdatePacket()", "onDataPacket", "sendBlockUpdated",
    ]), "mix, tank and salts persist and synchronize")
    add_check(checks, "spa_direct_controls", contains_all(spa_block, [
        "player.isShiftKeyDown()", "spa.toggleMixing()", "FluidUtil.interactWithFluidHandler",
        "spa.insertBathSalts", "spa.removeBathSalts",
    ]), "temporary direct interaction surface until GUI port")

    expected_key = {
        "P": "minecraft:piston",
        "J": "thaumcraft:essentia_jar",
        "S": "thaumcraft:tc4_block_arcane_stone",
        "Q": "minecraft:quartz_block",
        "I": "minecraft:iron_bars",
    }
    add_check(checks, "arcane_spa_recipe_exact_pattern", arcane.get("pattern") == ["QIQ", "SJS", "SPS"] and arcane.get("key") == expected_key,
              "ConfigRecipes line 879 symbol map restored")
    add_check(checks, "arcane_spa_recipe_exact_cost", arcane.get("research") == "ARCANESPA" and arcane.get("aspects") == {"AQUA": 16, "ORDO": 8, "TERRA": 4},
              "research and vis cost")
    add_check(checks, "arcane_spa_recipe_functional_result", arcane.get("result") == {"item": "thaumcraft:tc4_block_arcane_spa", "count": 1},
              "functional block output")
    add_check(checks, "bath_salts_recipe_exact", alchemy.get("research") == "BATHSALTS" and alchemy.get("catalyst") == "thaumcraft:tc4_dust" and alchemy.get("aspects") == {"COGNITIO": 6, "AURAM": 6, "ORDO": 6, "SANO": 6},
              "ConfigRecipes line 604 crucible registration")

    add_check(checks, "client_fluid_render_layer", contains_all(client, [
        "PURIFYING_FLUID.get(), RenderType.translucent()", "FLOWING_PURIFYING_FLUID.get(), RenderType.translucent()",
    ]), "source and flowing translucent layers")
    add_check(checks, "fluid_resource_contract", fluid_model == {"textures": {"particle": "thaumcraft:block/tc4/fluidpure"}} and
              fluid_state.get("variants", {}).get("", {}).get("model") == "thaumcraft:block/purifying_fluid",
              "simple particle model used by Forge fluid renderer")
    add_check(checks, "water_fluid_tag", set(water_tag.get("values", [])) >= {"thaumcraft:purifying_fluid", "thaumcraft:flowing_purifying_fluid"},
              "water-like behavior tag")
    add_check(checks, "spa_model_textures", spa_model.get("textures") == {
        "side": "thaumcraft:block/tc4/spa_side",
        "top": "thaumcraft:block/tc4/spa_top",
        "bottom": "thaumcraft:block/tc4/pedestal_top",
    }, "original TC4 spa texture set")
    add_check(checks, "sanity_soap_hook", 'id.getPath().equals("purifying_fluid")' in soap,
              "Purifying Fluid gives the existing Sanity Soap sticky-warp bonus")

    required_paths = [
        "src/main/resources/assets/thaumcraft/textures/block/tc4/fluidpure.png",
        "src/main/resources/assets/thaumcraft/textures/block/tc4/fluidpure.png.mcmeta",
        "src/main/resources/assets/thaumcraft/textures/block/tc4/spa_side.png",
        "src/main/resources/assets/thaumcraft/textures/block/tc4/spa_top.png",
        "src/main/resources/assets/thaumcraft/textures/item/tc4/bath_salts.png",
        "src/main/resources/assets/thaumcraft/textures/item/tc4/bucket_pure.png",
        "src/main/resources/data/thaumcraft/loot_tables/blocks/tc4_block_arcane_spa.json",
    ]
    missing = [path for path in required_paths if not (root / path).is_file()]
    add_check(checks, "required_resources", not missing, f"missing: {missing}")

    failures = [row for row in checks if not row["passed"]]
    result = {
        "version": args.version,
        "status": "PASS" if not failures else "FAIL",
        "passed": len(checks) - len(failures),
        "total": len(checks),
        "checks": checks,
        "failures": failures,
        "scope": "static source/resource parity guard only; not compile, runtime, multiplayer or GUI proof",
        "known_deferred": [
            "original Arcane Spa GUI/container and tank gauge",
            "in-game fluid rendering and block-entity synchronization tests",
            "successful Forge compileJava/build",
        ],
    }

    output = pathlib.Path(args.json_out)
    if not output.is_absolute():
        output = root / output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    sys.exit(main())
