#!/usr/bin/env python3
"""Static source/resource guard for the v11.62.64 arcane functional batch."""
from __future__ import annotations
import argparse, json, pathlib, re, sys

EXPECTED_RECIPES = {
    "tc4_hungrychest.json": {
        "tc4_key": "HungryChest", "research": "HUNGRYCHEST",
        "pattern": ["WTW", "W W", "WWW"],
        "key": {"W": "#minecraft:planks", "T": "minecraft:oak_trapdoor"},
        "aspects": {"AER": 5, "ORDO": 3, "PERDITIO": 3},
        "result": {"item": "thaumcraft:hungry_chest", "count": 1},
    },
    "tc4_wardedglass.json": {
        "tc4_key": "WardedGlass", "research": "WARDEDARCANA",
        "pattern": ["GGG", "WBW", "GGG"],
        "key": {"B": "thaumcraft:tc4_brain", "G": "minecraft:glass", "W": "thaumcraft:greatwood_planks"},
        "aspects": {"AQUA": 5, "ORDO": 10, "TERRA": 5, "IGNIS": 5},
        "result": {"item": "thaumcraft:warded_glass", "count": 8},
    },
    "tc4_nodechargerelay.json": {
        "tc4_key": "NodeChargeRelay", "research": "VISCHARGERELAY",
        "pattern": [" R ", "W W", "I I"],
        "key": {"I": "minecraft:iron_ingot", "R": "thaumcraft:vis_relay", "W": "thaumcraft:tc4_wand_rod_greatwood"},
        "aspects": {"IGNIS": 16, "ORDO": 16, "AER": 16},
        "result": {"item": "thaumcraft:vis_charge_relay", "count": 1},
    },
}

def require(errors, condition, text):
    if not condition:
        errors.append(text)

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--version", default="11.62.64")
    ap.add_argument("--json-out")
    args = ap.parse_args()
    root = pathlib.Path(args.root).resolve()
    errors, checks = [], []

    build = (root / "build.gradle").read_text(encoding="utf-8")
    require(errors, f"version = '{args.version}'" in build, "build.gradle version is not 11.62.64")
    checks.append("project version")

    mod = (root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
    for token in ["HUNGRY_CHEST", "VIS_CHARGE_RELAY", "WARDED_GLASS",
                  "HUNGRY_CHEST_BLOCK_ENTITY", "VIS_CHARGE_RELAY_BLOCK_ENTITY", "WARDED_GLASS_BLOCK_ENTITY"]:
        require(errors, token in mod, f"missing registry token {token}")
    require(errors, "new HungryChestBlockItem" in mod, "Hungry Chest does not use custom rendered BlockItem")
    checks.append("block/item/block-entity registration")

    java_expect = {
        "block/HungryChestBlock.java": ["entityInside", "getAnalogOutputSignal", "RenderShape.ENTITYBLOCK_ANIMATED"],
        "blockentity/HungryChestBlockEntity.java": ["SIZE = 27", "GenericContainerMenu.threeRows", "ForgeCapabilities.ITEM_HANDLER", "blockEvent"],
        "client/render/HungryChestRenderer.java": ["chesthungry.png", "renderStandalone"],
        "client/render/HungryChestItemRenderer.java": ["BlockEntityWithoutLevelRenderer", "renderStandalone"],
        "block/VisChargeRelayBlock.java": ["VisChargeRelayBlockEntity", "animateTick"],
        "blockentity/VisChargeRelayBlockEntity.java": ["MAX_TRANSFER_CENTIVIS = 500", "SLOT_WAND", "drainMachineVis"],
        "block/WardedGlassBlock.java": ["instanceof WandItem", "canEntityDestroy", "onBlockExploded", "PushReaction.BLOCK"],
        "blockentity/WardedGlassBlockEntity.java": ["putUUID(\"Owner\"", "isOwner"],
    }
    base = root / "src/main/java/com/darkifov/thaumcraft"
    for rel, tokens in java_expect.items():
        p = base / rel
        require(errors, p.is_file(), f"missing Java source {rel}")
        if p.is_file():
            txt = p.read_text(encoding="utf-8")
            for token in tokens:
                require(errors, token in txt, f"{rel} missing contract token {token}")
    checks.append("functional Java contracts")

    network = (base / "aura/AuraVisRelayNetwork.java").read_text(encoding="utf-8")
    require(errors, "VIS_CHARGE_RELAY" in network, "charge relay not included in vis relay network")
    resolver = (base / "recipe/TC4RecipeItemResolver.java").read_text(encoding="utf-8")
    mappings = {
        'BLOCK_META.put("blockMetalDevice:2", "thaumcraft:vis_charge_relay")': "metal device meta 2",
        'BLOCK_META.put("blockMetalDevice:14", "thaumcraft:vis_relay")': "metal device meta 14",
        'BLOCK_META.put("blockCosmeticOpaque:2", "thaumcraft:warded_glass")': "warded glass meta",
        'BLOCK_META.put("blockChestHungry:*", "thaumcraft:hungry_chest")': "hungry chest block",
        'VANILLA_BLOCKS.put("field_150415_aT", "minecraft:oak_trapdoor")': "trapdoor SRG field",
    }
    for line, label in mappings.items():
        require(errors, line in resolver, f"incorrect/missing resolver mapping: {label}")
    checks.append("TC4 source resolver mappings")

    recipe_dir = root / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench"
    for name, expected in EXPECTED_RECIPES.items():
        p = recipe_dir / name
        require(errors, p.is_file(), f"missing recipe {name}")
        if not p.is_file():
            continue
        data = json.loads(p.read_text(encoding="utf-8"))
        for key, value in expected.items():
            require(errors, data.get(key) == value, f"{name}: {key} differs from original mapping")
    checks.append("three exact original arcane recipes")

    resource_files = [
        "assets/thaumcraft/blockstates/hungry_chest.json",
        "assets/thaumcraft/blockstates/vis_charge_relay.json",
        "assets/thaumcraft/blockstates/warded_glass.json",
        "assets/thaumcraft/models/block/hungry_chest.json",
        "assets/thaumcraft/models/block/vis_charge_relay.json",
        "assets/thaumcraft/models/block/warded_glass.json",
        "assets/thaumcraft/models/item/hungry_chest.json",
        "assets/thaumcraft/models/item/vis_charge_relay.json",
        "assets/thaumcraft/models/item/warded_glass.json",
        "data/thaumcraft/loot_tables/blocks/hungry_chest.json",
        "data/thaumcraft/loot_tables/blocks/vis_charge_relay.json",
        "data/thaumcraft/loot_tables/blocks/warded_glass.json",
    ]
    resources = root / "src/main/resources"
    for rel in resource_files:
        require(errors, (resources / rel).is_file(), f"missing resource {rel}")
    checks.append("blockstate/model/item/loot resources")

    for lang_name in ("en_us.json", "ru_ru.json"):
        lang = json.loads((resources / f"assets/thaumcraft/lang/{lang_name}").read_text(encoding="utf-8"))
        for key in ["block.thaumcraft.hungry_chest", "container.thaumcraft.hungry_chest",
                    "block.thaumcraft.vis_charge_relay", "block.thaumcraft.warded_glass",
                    "message.thaumcraft.warded_glass.not_owner"]:
            require(errors, bool(lang.get(key)), f"{lang_name}: missing {key}")
    checks.append("English/Russian localization")

    result = {
        "version": args.version,
        "status": "PASS" if not errors else "FAIL",
        "checks": checks,
        "errors": errors,
        "limitations": ["No Java compilation", "No Forge client/runtime test", "Warded Glass connected textures deferred"],
    }
    if args.json_out:
        pathlib.Path(args.json_out).write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not errors else 1

if __name__ == "__main__":
    raise SystemExit(main())
