#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/thaumcraft"
DATA = ROOT / "src/main/resources/data/thaumcraft"
JAVA_MAIN = ROOT / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"


def fail(message: str) -> None:
    print(f"::error::{message}")
    raise SystemExit(1)


def warn(message: str) -> None:
    print(f"::warning::{message}")


def read_png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("invalid PNG signature")
    if data[12:16] != b"IHDR":
        raise ValueError("missing PNG IHDR")
    width, height = struct.unpack(">II", data[16:24])
    return width, height


def json_audit() -> list[str]:
    errors: list[str] = []
    for path in ROOT.rglob("*.json"):
        try:
            json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{path.relative_to(ROOT)}: {exc}")
    return errors


def png_audit() -> list[str]:
    errors: list[str] = []
    for path in (ROOT / "src/main/resources").rglob("*.png"):
        rel = path.relative_to(ROOT).as_posix()
        try:
            width, height = read_png_size(path)
        except Exception as exc:
            errors.append(f"{rel}: {exc}")
            continue

        if width <= 0 or height <= 0:
            errors.append(f"{rel}: zero dimensions {width}x{height}")
        if width < 8 or height < 8:
            errors.append(f"{rel}: too small {width}x{height}")
        if width > 4096 or height > 4096:
            errors.append(f"{rel}: too large {width}x{height}")

        if ("/textures/item/" in rel or "/textures/block/" in rel) and (width % 16 != 0 or height % 16 != 0):
            errors.append(f"{rel}: item/block texture size is not multiple of 16: {width}x{height}")

    return errors


def collect_registry_ids() -> tuple[set[str], set[str]]:
    text = JAVA_MAIN.read_text(encoding="utf-8")

    item_pattern = re.compile(
        r'\b(?:item|specialItem|extrasFocus|pechToken|ttParityItem|tceParityItem)\("([a-z0-9_]+)"'
    )
    block_pattern = re.compile(
        r'\b(?:block|crucibleBlock|arcaneWorkbenchBlock|researchTableBlock|auraNodeBlock|'
        r'nodeStabilizerBlock|matrixAuxiliaryBlock|arcanePedestalBlock|infusionMatrixBlock|'
        r'thaumicEnergisticsDeviceBlock|thaumicTinkererDeviceBlock|transvectorInterfaceBlock|'
        r'etherealPlatformBlock|fumeDissipatorBlock|essentiaDriveBlock|pedestalBlock|'
        r'eldritchAltarBlock|eldritchPortalBlock|essentiaJarBlock|filteredEssentiaJarBlock|'
        r'voidEssentiaJarBlock|essentiaTubeBlock|essentiaValveBlock|alchemicalFurnaceBlock|'
        r'extrasElementBlock|ttParityBlock|tceParityBlock)\("([a-z0-9_]+)"'
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
                if ":" in value:
                    namespace, texture_path = value.split(":", 1)
                    if namespace != "thaumcraft":
                        continue
                else:
                    texture_path = value
                if not (ASSETS / "textures" / f"{texture_path}.png").exists():
                    errors.append(f"{rel}: missing texture ref {key}={value}")

    return errors


def gui_audit() -> list[str]:
    errors: list[str] = []

    gui_dir = ASSETS / "textures/gui"
    if not gui_dir.exists():
        errors.append("missing textures/gui directory")
        return errors

    required_gui = [
        "thaumonomicon.png",
        "arcane_workbench.png",
        "pech_trade.png",
        "essentia_terminal.png",
        "essentia_drive.png",
        "osmotic_enchanter.png",
        "transvector_interface.png",
        "bottomless_pouch.png",
    ]

    for name in required_gui:
        path = gui_dir / name
        if not path.exists():
            errors.append(f"missing GUI texture: {name}")
            continue
        try:
            read_png_size(path)
        except Exception as exc:
            errors.append(f"bad GUI texture {name}: {exc}")

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



def compile_helper_presence_audit() -> list[str]:
    errors: list[str] = []
    text = JAVA_MAIN.read_text(encoding="utf-8")
    required_helpers = [
        "private static RegistryObject<Item> ttParityItem",
        "private static RegistryObject<Item> tceParityItem",
        "private static RegistryObject<Block> ttParityBlock",
        "private static RegistryObject<Block> tceParityBlock",
    ]
    for helper in required_helpers:
        if helper not in text:
            errors.append(f"missing compile helper: {helper}")
    return errors



def compile_api_risk_audit() -> list[str]:
    errors: list[str] = []
    forbidden_patterns = {
        "Container.stillValidBlockEntity(": "1.19.2 has no Container.stillValidBlockEntity helper here",
        ".serverLevel()": "ServerPlayer.serverLevel() was unavailable in the GitHub compile environment",
        ".parents()": "ResearchEntry exposes requirements(), not parents()",
        ".setHint(": "EditBox#setHint was unavailable in the GitHub compile environment",
    }

    for path in (ROOT / "src/main/java").rglob("*.java"):
        text = path.read_text(encoding="utf-8", errors="ignore")
        rel = path.relative_to(ROOT).as_posix()
        for pattern, reason in forbidden_patterns.items():
            if pattern in text:
                errors.append(f"{rel}: forbidden compile-risk pattern {pattern} — {reason}")

    return errors


def main() -> None:
    checks = {
        "JSON": json_audit(),
        "PNG": png_audit(),
        "Registry resources": registry_resource_audit(),
        "Model references": model_reference_audit(),
        "GUI": gui_audit(),
        "Java braces": java_brace_audit(),
        "Compile helpers": compile_helper_presence_audit(),
        "Compile API risks": compile_api_risk_audit(),
    }

    total_errors = 0
    for name, errors in checks.items():
        if errors:
            total_errors += len(errors)
            print(f"\n{name}: FAILED ({len(errors)} errors)")
            for error in errors[:100]:
                print(f"  - {error}")
        else:
            print(f"{name}: OK")

    if total_errors:
        fail(f"Static audit failed with {total_errors} errors")

    print("\nStatic audit passed successfully.")


if __name__ == "__main__":
    main()
