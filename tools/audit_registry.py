#!/usr/bin/env python3
"""Audit Thaumcraft item models, texture references, and visible clone groups.

The port intentionally keeps legacy registry ids so old worlds do not lose
items. Those ids must remain quarantined from normal player-facing lists. This
tool verifies that legacy aliases do not leak as visible clones and that model
JSON files do not reference missing Thaumcraft textures/models.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

MOD_ID = "thaumcraft"


@dataclass(frozen=True)
class Problem:
    kind: str
    path: str
    detail: str


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def canonical_parent(parent: str | None) -> str | None:
    if parent is None:
        return None
    return parent if ":" in parent else f"minecraft:{parent}"


def resource_path(resources: Path, location: str, category: str, suffix: str) -> Path | None:
    if location.startswith("#"):
        return None
    namespace, sep, path = location.partition(":")
    if not sep:
        namespace, path = "minecraft", namespace
    if namespace != MOD_ID:
        return None
    return resources / "assets" / namespace / category / f"{path}{suffix}"


def parse_java_string_set(source: str, field: str) -> set[str]:
    match = re.search(rf"\b{re.escape(field)}\s*=\s*Set\.of\((.*?)\n\s*\);", source, re.S)
    if not match:
        raise ValueError(f"Could not parse {field} from TC4RegistryGarbageGuard.java")
    return set(re.findall(r'"([^"]+)"', match.group(1)))




def extract_registered_ids(root: Path, model_ids: set[str]) -> set[str]:
    """Best-effort static extraction of item/block-item registry names.

    The mod keeps many non-registry submodels (predicate targets and source
    reference assets). Those files cannot appear in creative tabs and must not
    be treated as leaked registry clones.
    """
    registered: set[str] = set()
    for java in (root / "src/main/java").rglob("*.java"):
        source = java.read_text(encoding="utf-8", errors="ignore")
        for match in re.finditer(r"RegistryObject<(?:Item|Block)>\s+\w+\s*=\s*", source):
            statement = source[match.end():match.end() + 900]
            literal = re.search(r"\(\s*\"([^\"]+)\"", statement)
            if literal and literal.group(1) in model_ids:
                registered.add(literal.group(1))
        for match in re.finditer(r"ITEMS\.register\(\s*\"([^\"]+)\"", source):
            if match.group(1) in model_ids:
                registered.add(match.group(1))
    return registered

def load_quarantine(root: Path, model_ids: set[str]) -> tuple[set[str], set[str]]:
    guard = root / "src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java"
    source = guard.read_text(encoding="utf-8")
    exact = parse_java_string_set(source, "EXACT")
    prefixes = parse_java_string_set(source, "PREFIXES")
    return exact, prefixes


def is_quarantined(item_id: str, exact: set[str], prefixes: set[str], all_ids: set[str]) -> bool:
    if item_id in exact:
        return True
    if item_id.startswith("tc4_") and item_id[4:] in all_ids:
        return True
    return any(item_id.startswith(prefix) for prefix in prefixes)


def normalized_model(model: dict[str, Any]) -> str:
    return json.dumps(model, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def visual_signature(resources: Path, model: dict[str, Any]) -> str:
    """Signature based on rendering-relevant JSON and actual texture bytes.

    Texture paths are replaced with hashes, so legacy aliases pointing to the
    same image are detected even when their JSON strings differ.
    """
    copy = json.loads(json.dumps(model))
    copy["parent"] = canonical_parent(copy.get("parent"))
    textures = copy.get("textures", {})
    hashed: dict[str, str] = {}
    for layer, location in sorted(textures.items()):
        if not isinstance(location, str) or location.startswith("#"):
            hashed[layer] = str(location)
            continue
        texture = resource_path(resources, location, "textures", ".png")
        if texture is not None and texture.is_file():
            hashed[layer] = "sha256:" + sha256_bytes(texture.read_bytes())
        else:
            hashed[layer] = location
    copy["textures"] = hashed
    return sha256_bytes(normalized_model(copy).encode("utf-8"))


def markdown_list(values: Iterable[str], limit: int = 30) -> str:
    values = list(values)
    shown = values[:limit]
    text = ", ".join(f"`{value}`" for value in shown)
    if len(values) > limit:
        text += f" … (+{len(values) - limit})"
    return text


def run(root: Path, fail_on_unexpected: bool) -> int:
    resources = root / "src/main/resources"
    item_models = resources / "assets/thaumcraft/models/item"
    report_dir = root / "reports"
    report_dir.mkdir(parents=True, exist_ok=True)

    model_files = sorted(item_models.glob("*.json"))
    model_ids = {path.stem for path in model_files}
    exact, prefixes = load_quarantine(root, model_ids)
    registered_ids = extract_registered_ids(root, model_ids)
    intentional_shared_visuals = {
        frozenset({"warp_charm", "warp_ward_talisman"}),
        # Both stabilizer JSONs intentionally select builtin/entity. Their
        # NodeStabilizerItem instances pass the normal/advanced state to the
        # custom OBJ renderer, so the runtime visuals are not clones.
        frozenset({"node_stabilizer", "advanced_node_stabilizer"}),
        # The three assembled wands all use minecraft:builtin/entity. Their
        # WandItem instances provide different default rod/cap/capacity data to
        # the custom renderer, so identical JSON is intentional and functional.
        frozenset({"iron_capped_wooden_wand", "greatwood_wand", "silverwood_wand"}),
    }

    problems: list[Problem] = []
    models: dict[str, dict[str, Any]] = {}
    exact_groups: dict[str, list[str]] = defaultdict(list)
    visual_groups: dict[str, list[str]] = defaultdict(list)
    texture_usage: dict[str, list[str]] = defaultdict(list)

    for path in model_files:
        relative = path.relative_to(root).as_posix()
        try:
            model = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:  # noqa: BLE001 - report the exact malformed file
            problems.append(Problem("invalid_json", relative, str(exc)))
            continue
        models[path.stem] = model
        exact_groups[sha256_bytes(normalized_model(model).encode("utf-8"))].append(path.stem)
        visual_groups[visual_signature(resources, model)].append(path.stem)

        parent = model.get("parent")
        if isinstance(parent, str):
            parent_file = resource_path(resources, parent, "models", ".json")
            if parent_file is not None and not parent_file.is_file():
                problems.append(Problem("missing_parent_model", relative, parent))

        textures = model.get("textures", {})
        if isinstance(textures, dict):
            for layer, location in textures.items():
                if not isinstance(location, str) or location.startswith("#"):
                    continue
                texture_file = resource_path(resources, location, "textures", ".png")
                if texture_file is not None:
                    texture_usage[location].append(path.stem)
                    if not texture_file.is_file():
                        problems.append(Problem("missing_texture", relative, f"{layer} -> {location}"))

    exact_clone_groups = [sorted(ids) for ids in exact_groups.values() if len(ids) > 1]
    visual_clone_groups = [sorted(ids) for ids in visual_groups.values() if len(ids) > 1]

    leaking_groups: list[list[str]] = []
    for ids in visual_clone_groups:
        visible = [
            item_id for item_id in ids
            if item_id in registered_ids and not is_quarantined(item_id, exact, prefixes, model_ids)
        ]
        if len(visible) > 1 and frozenset(visible) not in intentional_shared_visuals:
            leaking_groups.append(visible)

    # Also find identical item texture PNGs; this catches placeholder copies
    # even when model JSON contains unrelated display/override data.
    texture_dir = resources / "assets/thaumcraft/textures/item"
    texture_hashes: dict[str, list[str]] = defaultdict(list)
    for texture in sorted(texture_dir.rglob("*.png")):
        texture_hashes[sha256_bytes(texture.read_bytes())].append(texture.relative_to(texture_dir).as_posix())
    duplicate_texture_groups = [sorted(paths) for paths in texture_hashes.values() if len(paths) > 1]

    unexpected = [problem for problem in problems if problem.kind in {
        "invalid_json", "missing_texture", "missing_parent_model"
    }]
    if leaking_groups:
        for group in leaking_groups:
            unexpected.append(Problem("visible_clone_group", "models/item", ", ".join(group)))

    result = {
        "version": "11.62.43",
        "item_model_count": len(model_files),
        "parsed_model_count": len(models),
        "statically_detected_registered_model_count": len(registered_ids),
        "quarantine_exact_count": len(exact),
        "quarantine_prefix_count": len(prefixes),
        "exact_duplicate_model_groups": exact_clone_groups,
        "visual_duplicate_groups": visual_clone_groups,
        "visible_clone_leaks": leaking_groups,
        "duplicate_item_texture_groups": duplicate_texture_groups,
        "problems": [problem.__dict__ for problem in problems],
        "unexpected_problem_count": len(unexpected),
    }
    (report_dir / "registry_audit_v11.62.43.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    lines = [
        "# Registry and resource audit — v11.62.43",
        "",
        f"- Item model JSON files: **{len(model_files)}**",
        f"- Parsed successfully: **{len(models)}**",
        f"- Statically detected registered item/block-item models: **{len(registered_ids)}**",
        f"- Quarantined exact IDs: **{len(exact)}**",
        f"- Quarantined prefixes: **{len(prefixes)}**",
        f"- Exact duplicate model groups: **{len(exact_clone_groups)}**",
        f"- Visual duplicate groups: **{len(visual_clone_groups)}**",
        f"- Visible clone leaks: **{len(leaking_groups)}**",
        f"- Missing/invalid resource problems: **{len(problems)}**",
        "",
        "## Visible clone leaks",
        "",
    ]
    if leaking_groups:
        lines.extend(f"- {markdown_list(group)}" for group in leaking_groups)
    else:
        lines.append("None. Duplicate compatibility IDs are quarantined from the player-facing creative list.")

    lines += ["", "## Exact duplicate model groups", ""]
    if exact_clone_groups:
        lines.extend(f"- {markdown_list(group)}" for group in exact_clone_groups)
    else:
        lines.append("None.")

    lines += ["", "## Resource problems", ""]
    if problems:
        lines.extend(f"- **{problem.kind}** `{problem.path}` — {problem.detail}" for problem in problems)
    else:
        lines.append("None: every item model JSON parsed and every Thaumcraft texture/model reference resolved.")

    lines += [
        "",
        "## Policy",
        "",
        "Legacy registry IDs are retained only for old-world compatibility. They must not appear as normal craftable/player-facing duplicates until a separate migration removes them safely.",
        "",
    ]
    (report_dir / "REGISTRY_AUDIT_V11_62_43.md").write_text("\n".join(lines), encoding="utf-8")

    print(f"Audited {len(model_files)} item models")
    print(f"Visible clone leaks: {len(leaking_groups)}")
    print(f"Resource problems: {len(problems)}")
    print(f"Unexpected problems: {len(unexpected)}")
    if fail_on_unexpected and unexpected:
        for problem in unexpected:
            print(f"ERROR {problem.kind}: {problem.path}: {problem.detail}", file=sys.stderr)
        return 1
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--fail-on-unexpected", action="store_true")
    args = parser.parse_args()
    return run(args.root.resolve(), args.fail_on_unexpected)


if __name__ == "__main__":
    raise SystemExit(main())
