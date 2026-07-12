#!/usr/bin/env python3
"""Semantic audit for every Thaumcraft item model.

Validates parent chains, inherited display transforms, overrides, and the
special builtin/entity path used by Forge BEWLR renderers. This complements
the texture audit: a valid PNG reference does not prove that an item is placed
correctly in GUI, ground, fixed, or either hand.
"""
from __future__ import annotations

import argparse
import json
import math
from collections import Counter
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets"
ITEM_ROOT = ASSETS / "thaumcraft/models/item"
DISPLAY_KEYS = (
    "thirdperson_righthand", "thirdperson_lefthand",
    "firstperson_righthand", "firstperson_lefthand",
    "head", "gui", "ground", "fixed",
)
BUILTIN_ENTITY = {"builtin/entity", "minecraft:builtin/entity"}
VANILLA_TERMINALS = {
    "minecraft:item/generated", "minecraft:item/handheld", "minecraft:item/handheld_rod",
    "minecraft:builtin/generated", "minecraft:builtin/entity",
    "builtin/generated", "builtin/entity",
}


def canonical(location: str | None, default_namespace: str = "minecraft") -> str | None:
    if not location:
        return None
    return location if ":" in location else f"{default_namespace}:{location}"


def model_file(location: str) -> Path | None:
    namespace, path = canonical(location).split(":", 1)
    candidate = ASSETS / namespace / "models" / f"{path}.json"
    return candidate if candidate.is_file() else None


def read_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def validate_vector(model: str, transform: str, field: str, value: Any,
                    problems: list[dict[str, str]], warnings: list[dict[str, str]]) -> None:
    prefix = f"{transform}.{field}"
    if not isinstance(value, list) or len(value) != 3:
        problems.append({"model": model, "kind": "invalid_display_vector", "detail": f"{prefix} must contain 3 numbers"})
        return
    if not all(isinstance(v, (int, float)) and math.isfinite(float(v)) for v in value):
        problems.append({"model": model, "kind": "non_finite_display_value", "detail": prefix})
        return
    values = [float(v) for v in value]
    if field == "scale":
        if any(v <= 0.0 for v in values):
            problems.append({"model": model, "kind": "non_positive_scale", "detail": f"{prefix}={values}"})
        elif any(v > 4.0 for v in values):
            warnings.append({"model": model, "kind": "large_scale", "detail": f"{prefix}={values}"})
    elif field == "translation" and any(abs(v) > 80.0 for v in values):
        warnings.append({"model": model, "kind": "large_translation", "detail": f"{prefix}={values}"})


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.49")
    parser.add_argument("--fail-on-problems", action="store_true")
    args = parser.parse_args()

    models: dict[str, dict[str, Any]] = {}
    paths: dict[str, Path] = {}
    parse_errors: list[dict[str, str]] = []
    for path in sorted(ITEM_ROOT.rglob("*.json")):
        key = f"thaumcraft:item/{path.relative_to(ITEM_ROOT).with_suffix('').as_posix()}"
        try:
            models[key] = read_json(path)
            paths[key] = path
        except Exception as exc:  # noqa: BLE001
            parse_errors.append({"model": key, "error": str(exc)})

    # Include local block/item parents outside models/item when resolving chains.
    local_cache = dict(models)
    problems: list[dict[str, str]] = []
    warnings: list[dict[str, str]] = []
    records: list[dict[str, Any]] = []
    depth_counts: Counter[int] = Counter()
    effective_transform_counts: Counter[str] = Counter()

    def load_location(location: str) -> dict[str, Any] | None:
        location = canonical(location)
        if location in local_cache:
            return local_cache[location]
        path = model_file(location)
        if path is None:
            return None
        try:
            data = read_json(path)
        except Exception:
            return None
        local_cache[location] = data
        return data

    for model_key, data in sorted(models.items()):
        chain: list[str] = []
        seen: set[str] = set()
        current_key = model_key
        current = data
        effective_display: dict[str, Any] = {}
        terminal = "none"
        while True:
            if current_key in seen:
                problems.append({"model": model_key, "kind": "parent_cycle", "detail": " -> ".join(chain + [current_key])})
                terminal = "cycle"
                break
            seen.add(current_key)
            chain.append(current_key)
            display = current.get("display")
            if display is not None:
                if not isinstance(display, dict):
                    problems.append({"model": current_key, "kind": "invalid_display_object", "detail": type(display).__name__})
                else:
                    for transform, spec in display.items():
                        if transform not in DISPLAY_KEYS:
                            warnings.append({"model": current_key, "kind": "unknown_display_context", "detail": transform})
                            continue
                        if transform not in effective_display:
                            effective_display[transform] = spec
            parent_raw = current.get("parent")
            if not parent_raw:
                terminal = "root"
                break
            if not isinstance(parent_raw, str):
                problems.append({"model": current_key, "kind": "invalid_parent", "detail": repr(parent_raw)})
                terminal = "invalid"
                break
            parent = canonical(parent_raw)
            if parent in VANILLA_TERMINALS or parent_raw in BUILTIN_ENTITY:
                terminal = parent
                chain.append(parent)
                break
            parent_data = load_location(parent)
            if parent_data is None:
                # External non-Thaumcraft parents are accepted; missing Thaumcraft parents are not.
                if parent.startswith("thaumcraft:"):
                    problems.append({"model": model_key, "kind": "missing_parent", "detail": parent})
                    terminal = "missing"
                else:
                    terminal = f"external:{parent}"
                chain.append(parent)
                break
            current_key = parent
            current = parent_data

        # Child display entries override inherited entries. Validate effective entries.
        for transform, spec in effective_display.items():
            effective_transform_counts[transform] += 1
            if not isinstance(spec, dict):
                problems.append({"model": model_key, "kind": "invalid_transform", "detail": transform})
                continue
            for field in ("rotation", "translation", "scale"):
                if field in spec:
                    validate_vector(model_key, transform, field, spec[field], problems, warnings)

        overrides = data.get("overrides", [])
        if overrides is not None and not isinstance(overrides, list):
            problems.append({"model": model_key, "kind": "invalid_overrides", "detail": type(overrides).__name__})
        elif isinstance(overrides, list):
            for index, override in enumerate(overrides):
                if not isinstance(override, dict) or not isinstance(override.get("model"), str):
                    problems.append({"model": model_key, "kind": "invalid_override", "detail": str(index)})
                    continue
                predicate = override.get("predicate", {})
                if not isinstance(predicate, dict) or not all(isinstance(v, (int, float)) and math.isfinite(float(v)) for v in predicate.values()):
                    problems.append({"model": model_key, "kind": "invalid_override_predicate", "detail": str(index)})

        parent_raw = data.get("parent", "")
        builtin = isinstance(parent_raw, str) and canonical(parent_raw) == "minecraft:builtin/entity"
        if builtin and isinstance(data.get("display"), dict) and data["display"]:
            problems.append({
                "model": model_key,
                "kind": "duplicate_dynamic_display",
                "detail": "builtin/entity model declares JSON display while this port's BEWLR applies TransformType-specific placement",
            })
        depth_counts[max(0, len(chain) - 1)] += 1
        records.append({
            "model": model_key.removeprefix("thaumcraft:item/"),
            "parent": parent_raw,
            "terminal_parent": terminal,
            "chain": chain,
            "chain_depth": max(0, len(chain) - 1),
            "builtin_entity": builtin,
            "explicit_display": sorted((data.get("display") or {}).keys()) if isinstance(data.get("display"), dict) else [],
            "effective_display": sorted(effective_display),
            "override_count": len(overrides) if isinstance(overrides, list) else 0,
        })

    stats = {
        "version": args.version,
        "item_models": len(models),
        "parse_errors": len(parse_errors),
        "parent_cycles": sum(1 for p in problems if p["kind"] == "parent_cycle"),
        "missing_parents": sum(1 for p in problems if p["kind"] == "missing_parent"),
        "builtin_entity_models": sum(1 for r in records if r["builtin_entity"]),
        "models_with_explicit_display": sum(1 for r in records if r["explicit_display"]),
        "models_with_effective_display": sum(1 for r in records if r["effective_display"]),
        "models_with_overrides": sum(1 for r in records if r["override_count"]),
        "problem_count": len(parse_errors) + len(problems),
        "warning_count": len(warnings),
        "max_parent_depth": max((r["chain_depth"] for r in records), default=0),
        "effective_transform_counts": dict(sorted(effective_transform_counts.items())),
        "parent_depth_counts": {str(k): v for k, v in sorted(depth_counts.items())},
    }

    reports = ROOT / "reports"
    reports.mkdir(exist_ok=True)
    json_path = reports / f"model_transform_audit_v{args.version}.json"
    md_path = reports / f"MODEL_TRANSFORM_AUDIT_V{args.version.replace('.', '_')}.md"
    json_path.write_text(json.dumps({
        "stats": stats,
        "parse_errors": parse_errors,
        "problems": problems,
        "warnings": warnings,
        "models": records,
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    lines = [
        f"# Semantic item-model audit v{args.version}", "",
        "Проверены цепочки `parent`, наследуемые `display`-трансформации, overrides и динамические `builtin/entity` модели.", "",
        "## Итоги", "",
        f"- Моделей предметов: **{stats['item_models']}**",
        f"- Динамических `builtin/entity`: **{stats['builtin_entity_models']}**",
        f"- Моделей с собственным `display`: **{stats['models_with_explicit_display']}**",
        f"- Моделей с эффективным `display` после наследования: **{stats['models_with_effective_display']}**",
        f"- Моделей с overrides: **{stats['models_with_overrides']}**",
        f"- Максимальная глубина parent-цепочки: **{stats['max_parent_depth']}**",
        f"- Ошибок: **{stats['problem_count']}**",
        f"- Предупреждений: **{stats['warning_count']}**", "",
        "## Покрытие контекстов", "",
    ]
    for key in DISPLAY_KEYS:
        lines.append(f"- `{key}`: **{effective_transform_counts.get(key, 0)}** моделей")
    lines += ["", "## Динамические модели", ""]
    for record in records:
        if record["builtin_entity"]:
            lines.append(f"- `{record['model']}` → `{record['parent']}`")
    lines += ["", "## Ошибки", ""]
    if problems or parse_errors:
        for item in parse_errors:
            lines.append(f"- **parse_error** `{item['model']}` — {item['error']}")
        for item in problems:
            lines.append(f"- **{item['kind']}** `{item['model']}` — {item['detail']}")
    else:
        lines.append("Нет: parent-цепочки разрешаются, циклов и некорректных display-векторов не найдено.")
    lines += ["", "## Интерпретация", "",
              "Отсутствие собственного `display` не является ошибкой: обычные generated/handheld модели используют стандартные трансформации Minecraft. В этом порте все восемь `builtin/entity` рендереров сами обрабатывают `TransformType`, поэтому собственный JSON `display` для них считается ошибкой двойного преобразования. Отдельный BEWLR-аудит проверяет Java-контракт."]
    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(f"Semantic model audit: {len(models)} models, {stats['problem_count']} problems, {len(warnings)} warnings")
    print(json_path.relative_to(ROOT))
    print(md_path.relative_to(ROOT))
    return 1 if args.fail_on_problems and stats["problem_count"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
