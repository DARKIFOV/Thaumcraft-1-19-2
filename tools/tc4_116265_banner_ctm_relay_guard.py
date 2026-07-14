#!/usr/bin/env python3
"""Static parity guard for v11.62.65 banners, warded-glass CTM and vis-relay render."""
from __future__ import annotations

import argparse
import json
import pathlib
import re

DYE_NAMES = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]


def require(errors: list[str], condition: bool, text: str) -> None:
    if not condition:
        errors.append(text)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--version", default="11.62.65")
    ap.add_argument("--json-out")
    args = ap.parse_args()

    root = pathlib.Path(args.root).resolve()
    resources = root / "src/main/resources"
    java = root / "src/main/java/com/darkifov/thaumcraft"
    errors: list[str] = []
    checks: list[dict[str, object]] = []

    def checkpoint(name: str, before: int) -> None:
        checks.append({"name": name, "status": "PASS" if len(errors) == before else "FAIL",
                       "new_errors": errors[before:]})

    before = len(errors)
    build = (root / "build.gradle").read_text(encoding="utf-8")
    require(errors, f"version = '{args.version}'" in build, "build.gradle version mismatch")
    checkpoint("project version", before)

    before = len(errors)
    recipe_dir = resources / "data/thaumcraft/thaumcraft_arcane_workbench"
    require(errors, not (recipe_dir / "tc4_banner__a.json").exists(), "legacy malformed banner adapter remains")
    for color, dye in enumerate(DYE_NAMES):
        path = recipe_dir / f"tc4_banner_{color}.json"
        require(errors, path.is_file(), f"missing banner recipe {path.name}")
        if not path.is_file():
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        expected = {
            "tc4_key": f"Banner_{color}",
            "research": "BANNERS",
            "pattern": ["WS", "WS", "WB"],
            "key": {"W": f"minecraft:{dye}_wool", "S": "minecraft:stick", "B": "#minecraft:wooden_slabs"},
            "aspects": {"AQUA": 5, "TERRA": 5},
            "result": {"item": "thaumcraft:tc4_block_banner", "count": 1, "nbt": f"{{color:{color}b}}"},
        }
        for key, value in expected.items():
            require(errors, data.get(key) == value, f"{path.name}: {key} differs from original")
        require(errors, data.get("v11_62_65_strict_original") is True,
                f"{path.name}: missing strict-original marker")
    checkpoint("16 exact Banner_0..15 arcane recipes", before)

    before = len(errors)
    arcane = (java / "arcane/ArcaneWorkbenchRecipe.java").read_text(encoding="utf-8")
    for token in ["private final String resultNbt", 'resultObject.has("nbt")',
                  "TagParser.parseTag(resultNbt)", "stack.setTag", "CommandSyntaxException"]:
        require(errors, token in arcane, f"arcane result-NBT contract missing: {token}")
    checkpoint("arcane output NBT and JEI result stack", before)

    before = len(errors)
    mod = (java / "ThaumcraftMod.java").read_text(encoding="utf-8")
    client = (java / "client/ClientModEvents.java").read_text(encoding="utf-8")
    for token in ["TC4_BANNER", "TC4_BANNER_ITEM", "TC4_BANNER_BLOCK_ENTITY",
                  '"tc4_block_banner"', "new TC4BannerBlockItem"]:
        require(errors, token in mod, f"banner registry contract missing: {token}")
    for rel, tokens in {
        "block/TC4BannerBlock.java": ["RenderShape.ENTITYBLOCK_ANIMATED", "setPlacedBy", "getCloneItemStack", "getDrops"],
        "block/TC4BannerBlockEntity.java": ["TAG_COLOR", "TAG_ASPECT", "getUpdatePacket", "saveAdditional"],
        "block/TC4BannerBlockItem.java": ["fillItemCategory", "initializeClient", "TC4BannerItemRenderer"],
        "client/render/TC4BannerRenderer.java": ["banner_blank.png", "banner_cultist.png", "COLORS", "renderAspect", "sway"],
        "client/render/TC4BannerItemRenderer.java": ["BlockEntityWithoutLevelRenderer", "renderStandalone"],
    }.items():
        path = java / rel
        require(errors, path.is_file(), f"missing banner Java source {rel}")
        if path.is_file():
            text = path.read_text(encoding="utf-8")
            for token in tokens:
                require(errors, token in text, f"{rel}: missing {token}")
    require(errors, "TC4BannerRenderer" in client and "TC4_BANNER_BLOCK_ENTITY" in client,
            "banner BER is not registered")
    for rel in [
        "assets/thaumcraft/blockstates/tc4_block_banner.json",
        "assets/thaumcraft/models/block/tc4_block_banner.json",
        "assets/thaumcraft/models/item/tc4_block_banner.json",
        "assets/thaumcraft/textures/models/banner_blank.png",
        "assets/thaumcraft/textures/models/banner_cultist.png",
        "data/thaumcraft/loot_tables/blocks/tc4_block_banner.json",
    ]:
        require(errors, (resources / rel).is_file(), f"missing banner resource {rel}")
    checkpoint("functional persistent banner block/item/render", before)

    before = len(errors)
    ctm = (java / "client/render/WardedGlassRenderer.java").read_text(encoding="utf-8")
    table_match = re.search(r"CONNECTED_TEXTURE_REF\s*=\s*\{(.*?)\};", ctm, re.S)
    refs = [int(v) for v in re.findall(r"\b\d+\b", table_match.group(1))] if table_match else []
    require(errors, len(refs) == 256, f"connected texture lookup has {len(refs)} entries, expected 256")
    require(errors, min(refs, default=-1) == 0 and max(refs, default=-1) == 46,
            "connected texture lookup must address source tiles 0..46")
    for tile in range(1, 48):
        require(errors, (resources / f"assets/thaumcraft/textures/block/tc4/warded_glass_{tile}.png").is_file(),
                f"missing warded-glass CTM tile {tile}")
    for token in ["ownerId().equals(origin.ownerId())", "textureFor", "Direction.values()", "+ texture + \".png\""]:
        require(errors, token in ctm, f"warded-glass renderer missing {token}")
    glass_block = (java / "block/WardedGlassBlock.java").read_text(encoding="utf-8")
    require(errors, "RenderShape.ENTITYBLOCK_ANIMATED" in glass_block,
            "warded glass does not delegate rendering to exact CTM BER")
    require(errors, "WardedGlassRenderer" in client and "WARDED_GLASS_BLOCK_ENTITY" in client,
            "warded-glass renderer is not registered")
    checkpoint("47-tile owner-aware warded-glass CTM", before)

    before = len(errors)
    relay_model = (java / "client/render/model/TC4VisRelayModel.java")
    relay_renderer = (java / "client/render/VisChargeRelayRenderer.java")
    relay_item = (java / "client/render/VisChargeRelayItemRenderer.java")
    for path in [relay_model, relay_renderer, relay_item]:
        require(errors, path.is_file(), f"missing relay renderer source {path.name}")
    if relay_model.is_file():
        text = relay_model.read_text(encoding="utf-8")
        for token in ["CRYSTAL", "RINGFLOAT", "SUPPORT", "renderCrystal", "renderRingFloat", "renderSupport"]:
            require(errors, token in text, f"relay OBJ data missing {token}")
        arrays = {}
        for name in ("CRYSTAL", "RINGFLOAT", "SUPPORT"):
            match = re.search(rf"{name}\s*=\s*\{{(.*?)\}};", text, re.S)
            arrays[name] = len(re.findall(r"[-+]?\d+(?:\.\d+)?F", match.group(1))) if match else 0
        require(errors, arrays["CRYSTAL"] == 16 * 3 * 8,
                f"Crystal OBJ payload has {arrays['CRYSTAL']} floats, expected 384")
        require(errors, arrays["RINGFLOAT"] == 32 * 3 * 8,
                f"RingFloat OBJ payload has {arrays['RINGFLOAT']} floats, expected 768")
        require(errors, arrays["SUPPORT"] == 36 * 3 * 8,
                f"Support OBJ payload has {arrays['SUPPORT']} floats, expected 864")
    if relay_renderer.is_file():
        text = relay_renderer.read_text(encoding="utf-8")
        for token in ["textures/models/vis_relay.png", "renderRingFloat", "renderSupport", "renderCrystal",
                      "LightTexture.FULL_BRIGHT", "pulseStrength"]:
            require(errors, token in text, f"relay renderer missing {token}")
    relay_be = (java / "blockentity/VisChargeRelayBlockEntity.java").read_text(encoding="utf-8")
    for token in ["PULSE_TICKS = 5", "triggerPulse(aspect)", "pulseStrength", "sendBlockUpdated"]:
        require(errors, token in relay_be, f"relay pulse sync missing {token}")
    relay_block = (java / "block/VisChargeRelayBlock.java").read_text(encoding="utf-8")
    require(errors, "RenderShape.ENTITYBLOCK_ANIMATED" in relay_block,
            "vis charge relay does not use original OBJ renderer")
    require(errors, "VisChargeRelayRenderer" in client and "VIS_CHARGE_RELAY_BLOCK_ENTITY" in client,
            "vis charge relay BER is not registered")
    require(errors, "VisChargeRelayBlockItem" in mod and "VIS_CHARGE_RELAY_ITEM" in mod,
            "vis charge relay item BEWLR is not registered")
    checkpoint("original vis_relay.obj groups and five-tick pulse", before)

    before = len(errors)
    for lang_name in ("en_us.json", "ru_ru.json"):
        lang = json.loads((resources / f"assets/thaumcraft/lang/{lang_name}").read_text(encoding="utf-8"))
        for key in ["block.thaumcraft.tc4_block_banner", "item.thaumcraft.tc4_block_banner.cultist",
                    "tooltip.thaumcraft.banner.aspect", "tooltip.thaumcraft.banner.apply_aspect"]:
            require(errors, bool(lang.get(key)), f"{lang_name}: missing {key}")
        for color in range(16):
            require(errors, bool(lang.get(f"item.thaumcraft.tc4_block_banner.{color}")),
                    f"{lang_name}: missing banner color {color}")
    checkpoint("English/Russian banner localization", before)

    result = {
        "version": args.version,
        "status": "PASS" if not errors else "FAIL",
        "checks": checks,
        "errors": errors,
        "facts": {
            "banner_recipe_variants": 16,
            "warded_glass_texture_tiles": 47,
            "warded_glass_lookup_entries": len(refs),
            "vis_relay_obj_groups": ["Crystal", "RingFloat", "Support"],
        },
        "limitations": [
            "Static source/resource parity only; Java compilation is checked separately.",
            "No Minecraft client, dedicated-server, save/reload or JEI runtime test was performed.",
        ],
    }
    if args.json_out:
        pathlib.Path(args.json_out).write_text(
            json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
