#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import struct
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/thaumcraft"
DATA = ROOT / "src/main/resources/data/thaumcraft"
JAVA_MAIN = ROOT / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"


def read_png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("invalid PNG signature")
    if data[12:16] != b"IHDR":
        raise ValueError("missing PNG IHDR")
    return struct.unpack(">II", data[16:24])


def json_audit() -> list[str]:
    errors: list[str] = []
    for path in ROOT.rglob("*.json"):
        # Build outputs are not committed and may contain partial reports locally.
        if "build" in path.relative_to(ROOT).parts:
            continue
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{path.relative_to(ROOT)}: {exc}")
    return errors


def png_audit() -> list[str]:
    errors: list[str] = []
    resources = ROOT / "src/main/resources"
    for path in resources.rglob("*.png"):
        rel = path.relative_to(ROOT).as_posix()
        try:
            width, height = read_png_size(path)
        except Exception as exc:
            errors.append(f"{rel}: {exc}")
            continue
        is_original_bank = any(token in rel for token in ["/textures/original/", "/original_tc4_1710/", "/textures/item/tc4/", "/textures/block/tc4/"])
        if width <= 0 or height <= 0:
            errors.append(f"{rel}: zero dimensions {width}x{height}")
        if not is_original_bank and (width < 8 or height < 8):
            errors.append(f"{rel}: too small {width}x{height}")
        if width > 4096 or height > 4096:
            errors.append(f"{rel}: too large {width}x{height}")
        if not is_original_bank and ("/textures/item/" in rel or "/textures/block/" in rel) and (width % 16 != 0 or height % 16 != 0):
            errors.append(f"{rel}: item/block texture size is not multiple of 16: {width}x{height}")
    return errors


def collect_registry_ids() -> tuple[set[str], set[str]]:
    text = JAVA_MAIN.read_text(encoding="utf-8")
    item_pattern = re.compile(r'\b(?:item|specialItem|extrasFocus|pechToken|ttParityItem|tceParityItem)\("([a-z0-9_]+)"')
    block_pattern = re.compile(
        r'\b(?:block|crucibleBlock|arcaneWorkbenchBlock|researchTableBlock|auraNodeBlock|'
        r'nodeStabilizerBlock|visRelayBlock|matrixAuxiliaryBlock|arcanePedestalBlock|infusionMatrixBlock|'
        r'thaumicEnergisticsDeviceBlock|thaumicTinkererDeviceBlock|transvectorInterfaceBlock|'
        r'etherealPlatformBlock|fumeDissipatorBlock|essentiaDriveBlock|pedestalBlock|'
        r'eldritchAltarBlock|eldritchPortalBlock|essentiaJarBlock|filteredEssentiaJarBlock|'
        r'voidEssentiaJarBlock|essentiaTubeBlock|essentiaValveBlock|alchemicalFurnaceBlock|alchemicalCentrifugeBlock|deconstructionTableBlock|essentiaCrystalizerBlock|'
        r'extrasElementBlock|tc4SaplingBlock|taintBlock|taintFibresBlock|fluxGooBlock|fluxGasBlock|ttParityBlock|tceParityBlock)\("([a-z0-9_]+)"'
    )
    return set(item_pattern.findall(text)), set(block_pattern.findall(text))


def registry_resource_audit() -> list[str]:
    errors: list[str] = []
    item_ids, block_ids = collect_registry_ids()
    for item_id in sorted(item_ids):
        if not (ASSETS / f"models/item/{item_id}.json").exists():
            errors.append(f"missing item model: {item_id}")
        if not (ASSETS / f"textures/item/{item_id}.png").exists():
            errors.append(f"missing item texture: {item_id}")
    for block_id in sorted(block_ids):
        if not (ASSETS / f"models/block/{block_id}.json").exists():
            errors.append(f"missing block model: {block_id}")
        if not (ASSETS / f"models/item/{block_id}.json").exists():
            errors.append(f"missing block item model: {block_id}")
        if not (ASSETS / f"blockstates/{block_id}.json").exists():
            errors.append(f"missing blockstate: {block_id}")
        if not (ASSETS / f"textures/block/{block_id}.png").exists():
            errors.append(f"missing block texture: {block_id}")
        if not (DATA / f"loot_tables/blocks/{block_id}.json").exists():
            errors.append(f"missing block loot table: {block_id}")
    print(f"Registry ids: items={len(item_ids)}, blocks={len(block_ids)}")
    return errors


def model_reference_audit() -> list[str]:
    errors: list[str] = []
    for path in (ASSETS / "models").rglob("*.json"):
        rel = path.relative_to(ROOT).as_posix()
        try:
            obj = json.loads(path.read_text(encoding="utf-8"))
        except Exception:
            continue
        parent = obj.get("parent")
        if isinstance(parent, str) and parent.startswith("thaumcraft:"):
            parent_path = ASSETS / "models" / f"{parent.split(':', 1)[1]}.json"
            if not parent_path.exists():
                errors.append(f"{rel}: missing parent model {parent}")
        textures = obj.get("textures", {})
        if isinstance(textures, dict):
            for key, value in textures.items():
                if not isinstance(value, str) or value.startswith("#"):
                    continue
                namespace, texture_path = (value.split(":", 1) if ":" in value else ("thaumcraft", value))
                if namespace == "thaumcraft" and not (ASSETS / "textures" / f"{texture_path}.png").exists():
                    errors.append(f"{rel}: missing texture ref {key}={value}")
    return errors


def gui_audit() -> list[str]:
    required = [
        "thaumonomicon.png",
        "arcane_workbench.png",
        "pech_trade.png",
        "essentia_terminal.png",
        "essentia_drive.png",
        "osmotic_enchanter.png",
        "transvector_interface.png",
        "bottomless_pouch.png",
    ]
    errors: list[str] = []
    for name in required:
        path = ASSETS / "textures/gui" / name
        if not path.exists():
            errors.append(f"missing GUI texture: {name}")
            continue
        try:
            read_png_size(path)
        except Exception as exc:
            errors.append(f"bad GUI texture {name}: {exc}")
    return errors


def pack_metadata_audit() -> list[str]:
    errors: list[str] = []
    path = ROOT / "src/main/resources/pack.mcmeta"
    if not path.exists():
        return ["missing src/main/resources/pack.mcmeta; Minecraft/Forge may warn that the mod jar failed to load correct resource pack info"]
    try:
        obj = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        return [f"invalid pack.mcmeta JSON: {exc}"]
    pack = obj.get("pack")
    if not isinstance(pack, dict):
        errors.append("pack.mcmeta missing top-level pack object")
        return errors
    if pack.get("pack_format") != 9:
        errors.append(f"pack.mcmeta pack_format must be 9 for Minecraft 1.19.2, got {pack.get('pack_format')!r}")
    description = pack.get("description")
    if not isinstance(description, str) or not description.strip():
        errors.append("pack.mcmeta missing non-empty pack.description")
    return errors


def java_brace_audit() -> list[str]:
    errors: list[str] = []
    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        balance = 0
        for ch in text:
            if ch == "{":
                balance += 1
            elif ch == "}":
                balance -= 1
            if balance < 0:
                errors.append(f"{path.relative_to(ROOT)}: negative brace balance")
                break
        if balance != 0:
            errors.append(f"{path.relative_to(ROOT)}: brace balance {balance}")
    return errors


def duplicate_registry_field_audit() -> list[str]:
    text = JAVA_MAIN.read_text(encoding="utf-8")
    names = re.findall(r'public static final RegistryObject<[^>]+>\s+(\w+)\s*=', text)
    return [f"duplicate registry field in ThaumcraftMod.java: {name}" for name, count in Counter(names).items() if count > 1]


def compile_api_risk_audit() -> list[str]:
    errors: list[str] = []
    forbidden = {
        "Container.stillValidBlockEntity(": "1.19.2 compile environment does not expose this helper here",
        ".serverLevel()": "ServerPlayer.serverLevel() was unavailable in the GitHub compile environment",
        ".parents()": "ResearchEntry exposes requirements(), not parents()",
        ".setHint(": "EditBox#setHint was unavailable in this Forge 1.19.2 environment",
        "Button.builder(": "Button.builder was unavailable in this Forge 1.19.2 environment",
        "SoundEvent.createVariableRangeEvent(": "Forge 1.19.2/MC 1.19.2 compile target uses new SoundEvent(ResourceLocation); createVariableRangeEvent is newer API",
        "ProjectileUtil.getHitResultOnMoveVector(": "GitHub Forge 1.19.2 official mappings expose ProjectileUtil.getHitResult for this projectile path",
        "onGround()": "GitHub Forge 1.19.2 official mappings expose Entity.onGround as a field in this target",
        "isValidBonemealTarget(LevelReader": "BonemealableBlock in this 1.19.2 target requires BlockGetter for isValidBonemealTarget",
    }
    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        rel = path.relative_to(ROOT).as_posix()
        for token, reason in forbidden.items():
            if token in text:
                errors.append(f"{rel}: forbidden compile-risk pattern {token} — {reason}")
    return errors


checks = {
    "JSON": json_audit(),
    "PNG": png_audit(),
    "Registry resources": registry_resource_audit(),
    "Model references": model_reference_audit(),
    "GUI": gui_audit(),
    "Pack metadata": pack_metadata_audit(),
    "Java braces": java_brace_audit(),
    "Duplicate registry fields": duplicate_registry_field_audit(),
    "Compile API risks": compile_api_risk_audit(),
}

failed = False
for name, errors in checks.items():
    if errors:
        failed = True
        for error in errors:
            print(f"::error::{name}: {error}")
    else:
        print(f"{name}: OK")

if failed:
    sys.exit(1)
print("Static source/resource audit passed.")
