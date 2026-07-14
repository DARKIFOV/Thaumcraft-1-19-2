#!/usr/bin/env python3
"""Static parity audit for all original TC4 4.2.3.5 research entries and book assets."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import struct
import subprocess
import sys
from collections import Counter
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_original_research_entries_stage116.json"
ASSETS = ROOT / "src/main/resources/assets/thaumcraft"
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"
REPORTS = ROOT / "reports"
CATEGORIES = ("BASICS", "THAUMATURGY", "ALCHEMY", "ARTIFICE", "GOLEMANCY", "ELDRITCH")
EXEMPT_LOCALIZATION = {"CAP_iron", "ROD_wood"}  # source auto-unlock virtual component markers
WAND_MODELS = (
    "iron_capped_wooden_wand",
    "greatwood_wand",
    "silverwood_wand",
    "avaritia_creative_wand",
)

GUI_ASSETS = (
    "textures/original/thaumcraft4/gui/gui_research.png",
    "textures/original/thaumcraft4/gui/gui_researchback.png",
    "textures/original/thaumcraft4/gui/gui_researchbackeldritch.png",
    "textures/original/thaumcraft4/gui/gui_researchbook.png",
    "textures/original/thaumcraft4/gui/gui_researchbook_overlay.png",
    "textures/original/thaumcraft4/misc/nodes.png",
)


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def read_json(path: Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8"))


def png_size(path: Path) -> tuple[int, int]:
    """Read PNG dimensions without third-party packages.

    TC4 audit assets are PNG files. Reading the signature and IHDR chunk keeps
    the CI audit self-contained and avoids a hidden Pillow dependency.
    """
    with path.open("rb") as stream:
        header = stream.read(24)
    if len(header) < 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise ValueError(f"not a valid PNG with an IHDR header: {path}")
    return struct.unpack(">II", header[16:24])


def parse_icon_map() -> dict[str, dict[str, str]]:
    text = (JAVA / "client/screen/TC4ResearchIconMap.java").read_text(encoding="utf-8")
    pattern = re.compile(
        r'map\.put\("([^"]+)"\s*,\s*new Entry\(new ResourceLocation\("([^"]+)",\s*"([^"]+)"\)',
        re.S,
    )
    return {key: {"namespace": ns, "path": path} for key, ns, path in pattern.findall(text)}


def texture_path(namespace: str, path: str) -> Path | None:
    if namespace != "thaumcraft" or not path.startswith("textures/"):
        return None
    return ASSETS / path


def parse_languages() -> dict[str, dict[str, str]]:
    return {
        locale: read_json(ASSETS / "lang" / f"{locale}.json")
        for locale in ("en_us", "ru_ru")
    }


def functionality(entry: dict[str, Any]) -> str:
    flags = set(entry.get("flags", []))
    if "auto_unlock" in flags:
        return "auto_unlock"
    if "lost" in flags:
        return "lost_knowledge"
    if "hidden" in flags:
        return "trigger_or_hidden"
    if "secondary" in flags:
        return "secondary_aspect_purchase"
    return "primary_research_note"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.54-hotfix7")
    parser.add_argument("--fail-on-problems", action="store_true")
    args = parser.parse_args()

    entries: list[dict[str, Any]] = read_json(SOURCE)
    problems: list[str] = []
    if len(entries) != 201:
        problems.append(f"expected 201 original research entries, found {len(entries)}")
    keys = [e["key"] for e in entries]
    if len(keys) != len(set(keys)):
        problems.append("duplicate research keys in source mapping")

    for entry in entries:
        for trigger_field in ("item_triggers_raw", "entity_triggers", "aspect_triggers"):
            values = entry.get(trigger_field, []) or []
            if len(values) != len(set(values)):
                problems.append(f"duplicate {trigger_field} values for {entry.get('key', '<unknown>')}")

    generation = subprocess.run(
        [sys.executable, str(ROOT / "tools/generate_tc4_research_runtime_from_source_map.py"), "--check"],
        cwd=ROOT, text=True, capture_output=True,
    )
    if generation.returncode != 0:
        problems.append("generated research Java drift: " + (generation.stdout + generation.stderr).strip())

    bridge_text = (JAVA / "research/TC4ResearchRuntimeBridge.java").read_text(encoding="utf-8")
    bridge_keys = re.findall(r'new ResearchEntry\(\s*"([^"]+)"', bridge_text)
    if len(bridge_keys) != len(keys) or set(bridge_keys) != set(keys):
        problems.append("runtime bridge key set does not exactly match the 201-entry source map")

    page_index_text = (JAVA / "research/TC4OriginalResearchPageIndex.java").read_text(encoding="utf-8")
    page_index_keys = re.findall(r'put\(map,\s*"([^"]+)"', page_index_text)
    if page_index_keys != keys:
        problems.append("exact page index key/order does not match source map")

    icons = parse_icon_map()
    missing_icon_keys = sorted(set(keys) - set(icons))
    extra_icon_keys = sorted(set(icons) - set(keys))
    if missing_icon_keys:
        problems.append(f"research icon map missing {len(missing_icon_keys)} keys")
    if extra_icon_keys:
        problems.append(f"research icon map has {len(extra_icon_keys)} non-original keys")

    languages = parse_languages()
    recipe_bridge_text = (JAVA / "recipe/TC4RecipeRuntimeBridge.java").read_text(encoding="utf-8")
    recipe_runtime_keys = set(re.findall(r'recipes\.add\(r\("([^"]+)"', recipe_bridge_text))
    source_recipe_keys = {r for e in entries for r in e.get("recipe_keys", [])}
    missing_recipe_keys = sorted(k for k in source_recipe_keys if k not in recipe_runtime_keys)
    if missing_recipe_keys:
        problems.append(f"runtime recipe bridge missing {len(missing_recipe_keys)} explicit research recipe keys")

    gui_assets: list[dict[str, Any]] = []
    for rel in GUI_ASSETS:
        path = ASSETS / rel
        row: dict[str, Any] = {"path": rel, "exists": path.is_file()}
        if path.is_file():
            row["width"], row["height"] = png_size(path)
            row["sha256"] = sha256(path)
        else:
            problems.append("missing original Thaumonomicon GUI asset: " + rel)
        gui_assets.append(row)

    category_counts = Counter(e["category"] for e in entries)
    if set(category_counts) != set(CATEGORIES):
        problems.append("category set differs from original six TC4 categories")

    rows: list[dict[str, Any]] = []
    all_page_count = 0
    concealed_page_count = 0
    missing_texture_count = 0
    missing_language_count = 0
    for e in entries:
        key = e["key"]
        all_page_count += len(e.get("pages", []))
        concealed_page_count += sum(p.get("type") == "TEXT_CONCEALED" for p in e.get("pages", []))
        icon = icons.get(key)
        icon_exists = False
        icon_size: list[int] | None = None
        icon_rel = ""
        if icon:
            path = texture_path(icon["namespace"], icon["path"])
            icon_rel = f"{icon['namespace']}:{icon['path']}"
            icon_exists = bool(path and path.is_file())
            if icon_exists and path:
                icon_size = list(png_size(path))
        if not icon_exists:
            missing_texture_count += 1
            problems.append(f"{key}: missing mapped research icon texture {icon_rel or '<unmapped>'}")

        required_lang = [e.get("name_key", ""), e.get("text_key", "")] + list(e.get("page_text_keys", []))
        lang_missing: dict[str, list[str]] = {}
        for locale, data in languages.items():
            missing = [k for k in required_lang if k and k not in data]
            if key in EXEMPT_LOCALIZATION:
                missing = [k for k in missing if not (k.endswith("." + key))]
            if missing:
                lang_missing[locale] = missing
                missing_language_count += len(missing)
                problems.append(f"{key}: missing {locale} language keys: {', '.join(missing)}")

        rows.append({
            "key": key,
            "category": e["category"],
            "title": e.get("title", ""),
            "subtitle": e.get("subtitle", ""),
            "position": [e.get("display_column", 0), e.get("display_row", 0)],
            "complexity": e.get("complexity", 1),
            "aspects": e.get("aspects", {}),
            "parents": e.get("parents", []),
            "hidden_parents": e.get("parents_hidden", []),
            "siblings": e.get("siblings", []),
            "flags": e.get("flags", []),
            "functionality": functionality(e),
            "warp": e.get("warp", 0),
            "item_triggers": e.get("item_triggers_raw", []),
            "entity_triggers": e.get("entity_triggers", []),
            "aspect_triggers": e.get("aspect_triggers", []),
            "page_count": len(e.get("pages", [])),
            "pages": e.get("pages", []),
            "recipe_keys": e.get("recipe_keys", []),
            "icon": icon_rel,
            "icon_exists": icon_exists,
            "icon_size": icon_size,
            "missing_language_keys": lang_missing,
            "status": "pass" if icon_exists and not lang_missing else "problem",
        })

    screen = (JAVA / "client/screen/ThaumonomiconScreen.java").read_text(encoding="utf-8")
    page_screen = (JAVA / "client/screen/TC4ResearchPageScreen.java").read_text(encoding="utf-8")
    wand = (JAVA / "client/render/WandItemRenderer.java").read_text(encoding="utf-8")
    required_tokens = {
        "browser_256x230": (screen, "PANE_WIDTH = 256", "PANE_HEIGHT = 230"),
        "browser_map_224x196": (screen, "TC4_BACKGROUND_DEST_WIDTH = 224", "TC4_BACKGROUND_DEST_HEIGHT = 196"),
        "eldritch_gate": (screen, 'unlocked.contains("ELDRITCHMINOR")'),
        "book_256x181": (page_screen, "BOOK_SOURCE_WIDTH = 256", "BOOK_SOURCE_HEIGHT = 181"),
        "concealed_page_removal": (page_screen, "page.unlockResearch().isBlank()", "continue;"),
        "dynamic_aspect_catalog": (page_screen, '"ASPECT_CATALOG"', "ClientAspectData.knows"),
        "wand_inventory_66deg": (wand, "TransformType.GUI", "rotationDegrees(66.0F)"),
        "wand_first_person": (wand, "transformType.firstPerson()", "scale(1.00F, 1.10F, 1.00F)"),
        "wand_mesh_centering": (wand, "MODEL_CENTER_Y", "-MODEL_CENTER_Y"),
        "original_browser_tooltip_cues": (screen, "tc.research.purchase", "tc.research.getprim", "tc.forbidden", "ClientAspectData.pool"),
    }
    contract = {}
    for name, (text, *tokens) in required_tokens.items():
        ok = all(token in text for token in tokens)
        contract[name] = ok
        if not ok:
            problems.append(f"runtime contract missing: {name}")

    wand_item = (JAVA / "block/WandItem.java").read_text(encoding="utf-8")
    wand_models: dict[str, dict[str, Any]] = {}
    for model_name in WAND_MODELS:
        model_path = ASSETS / "models/item" / f"{model_name}.json"
        parent = ""
        exists = model_path.is_file()
        if exists:
            parent = read_json(model_path).get("parent", "")
        ok = exists and parent == "minecraft:builtin/entity"
        wand_models[model_name] = {"exists": exists, "parent": parent, "bewlr_contract": ok}
        if not ok:
            problems.append(f"{model_name}: wand item model is not minecraft:builtin/entity")
    wand_client_extension = all(token in wand_item for token in (
        "initializeClient", "getCustomRenderer", "WandItemRenderer.instance()"
    ))
    if not wand_client_extension:
        problems.append("WandItem does not expose the shared BEWLR through IClientItemExtensions")

    summary = {
        "version": args.version,
        "source": {
            "repository": "KAMKEEL/Thaumcraft4-1.7.10",
            "commit": "059a869367c7bfeef97e5c1aeca78a4b9fa26554",
            "config_research_blob": "6dc7334c9c13c40fa9876ce87fa356bd9bd1c2c0",
            "gui_browser_blob": "649f4454528553f657999b0ba2a2d37d2d0f01d7",
            "gui_recipe_blob": "f05130ec84c87e3b9977886055df07cec786c31a",
            "wand_renderer_blob": "00b724e20873aae09a45324c10335eba0f7d95df",
        },
        "research_entries": len(entries),
        "categories": dict(category_counts),
        "pages": all_page_count,
        "concealed_pages": concealed_page_count,
        "explicit_recipe_keys": len(source_recipe_keys),
        "mapped_icons": len(icons),
        "missing_icon_textures": missing_texture_count,
        "missing_language_keys": missing_language_count,
        "runtime_contract": contract,
        "wand_models": wand_models,
        "wand_client_extension": wand_client_extension,
        "gui_assets": gui_assets,
        "problems": problems,
        "status": "pass" if not problems else "fail",
        "entries": rows,
    }

    REPORTS.mkdir(exist_ok=True)
    out = REPORTS / f"tc4_full_research_thaumonomicon_audit_{args.version}.json"
    out.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(out)
    print(json.dumps({k: summary[k] for k in (
        "research_entries", "categories", "pages", "concealed_pages", "explicit_recipe_keys",
        "mapped_icons", "missing_icon_textures", "missing_language_keys", "status")}, ensure_ascii=False))
    if problems:
        for problem in problems[:100]:
            print(" -", problem)
    if args.fail_on_problems and problems:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
