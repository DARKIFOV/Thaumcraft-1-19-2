#!/usr/bin/env python3
"""Verify every Forge builtin/entity item has a complete Java renderer contract."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODEL_ROOT = ROOT / "src/main/resources/assets/thaumcraft/models/item"
JAVA_ROOT = ROOT / "src/main/java"

CONTRACTS = {
    "node_stabilizer": ("NodeStabilizerItem.java", "NodeStabilizerItemRenderer"),
    "advanced_node_stabilizer": ("NodeStabilizerItem.java", "NodeStabilizerItemRenderer"),
    "iron_capped_wooden_wand": ("WandItem.java", "WandItemRenderer"),
    "greatwood_wand": ("WandItem.java", "WandItemRenderer"),
    "silverwood_wand": ("WandItem.java", "WandItemRenderer"),
    "avaritia_creative_wand": ("WandItem.java", "WandItemRenderer"),
    "thaumometer": ("ThaumometerItem.java", "ThaumometerItemRenderer"),
    "node_jar": ("NodeJarItem.java", "NodeJarItemRenderer"),
}


def java_file(name: str) -> Path | None:
    matches = list(JAVA_ROOT.rglob(name))
    return matches[0] if len(matches) == 1 else None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.49")
    parser.add_argument("--fail-on-problems", action="store_true")
    args = parser.parse_args()

    dynamic: dict[str, str] = {}
    problems: list[dict[str, str]] = []
    for path in sorted(MODEL_ROOT.rglob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        parent = data.get("parent")
        canonical = parent if isinstance(parent, str) and ":" in parent else f"minecraft:{parent}" if parent else ""
        if canonical == "minecraft:builtin/entity":
            item_id = path.relative_to(MODEL_ROOT).with_suffix("").as_posix()
            dynamic[item_id] = str(parent)
            if isinstance(data.get("display"), dict) and data["display"]:
                problems.append({
                    "item": item_id,
                    "kind": "duplicate_dynamic_display",
                    "detail": "BEWLR handles TransformType placement; JSON display would be applied first by Forge",
                })

    unexpected = sorted(set(dynamic) - set(CONTRACTS))
    missing = sorted(set(CONTRACTS) - set(dynamic))
    for item in unexpected:
        problems.append({"item": item, "kind": "unmapped_dynamic_model", "detail": dynamic[item]})
    for item in missing:
        problems.append({"item": item, "kind": "missing_dynamic_model", "detail": CONTRACTS[item][0]})

    records = []
    mod_source = (JAVA_ROOT / "com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
    for item, (item_file_name, renderer_name) in CONTRACTS.items():
        item_file = java_file(item_file_name)
        renderer_file = java_file(renderer_name + ".java")
        checks = {
            "model_builtin_entity": item in dynamic,
            "item_class_found": item_file is not None,
            "renderer_class_found": renderer_file is not None,
            "initialize_client": False,
            "custom_renderer_return": False,
            "registered_id": f'"{item}"' in mod_source,
        }
        if item_file:
            source = item_file.read_text(encoding="utf-8")
            checks["initialize_client"] = "initializeClient" in source and "IClientItemExtensions" in source
            checks["custom_renderer_return"] = renderer_name in source and "getCustomRenderer" in source
        for check, ok in checks.items():
            if not ok:
                problems.append({"item": item, "kind": "contract_failure", "detail": check})
        records.append({"item": item, "item_class": item_file_name, "renderer": renderer_name, "checks": checks})

    stats = {
        "version": args.version,
        "dynamic_models": len(dynamic),
        "expected_contracts": len(CONTRACTS),
        "complete_contracts": sum(all(r["checks"].values()) for r in records),
        "problem_count": len(problems),
    }
    reports = ROOT / "reports"
    reports.mkdir(exist_ok=True)
    json_path = reports / f"bewlr_contract_audit_v{args.version}.json"
    md_path = reports / f"BEWLR_CONTRACT_AUDIT_V{args.version.replace('.', '_')}.md"
    json_path.write_text(json.dumps({"stats": stats, "dynamic_models": dynamic, "contracts": records, "problems": problems}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    lines = [f"# Forge BEWLR contract audit v{args.version}", "",
             "Каждая модель `builtin/entity` должна иметь зарегистрированный Item-класс, `initializeClient`, `IClientItemExtensions` и конкретный `BlockEntityWithoutLevelRenderer`. Поскольку рендереры этого порта сами обрабатывают `TransformType`, динамическая JSON-модель не должна дополнительно объявлять `display`.", "",
             "## Итоги", "",
             f"- Динамических моделей: **{stats['dynamic_models']}**",
             f"- Ожидаемых контрактов: **{stats['expected_contracts']}**",
             f"- Полных контрактов: **{stats['complete_contracts']}**",
             f"- Ошибок: **{stats['problem_count']}**", "",
             "## Карта рендереров", ""]
    for record in records:
        status = "OK" if all(record["checks"].values()) else "FAIL"
        lines.append(f"- **{status}** `{record['item']}` → `{record['item_class']}` → `{record['renderer']}`")
    lines += ["", "## Ошибки", ""]
    if problems:
        lines.extend(f"- **{p['kind']}** `{p['item']}` — {p['detail']}" for p in problems)
    else:
        lines.append("Нет. Все восемь Forge runtime-контрактов замкнуты.")
    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"BEWLR audit: {stats['complete_contracts']}/{stats['expected_contracts']} complete, {len(problems)} problems")
    print(json_path.relative_to(ROOT))
    print(md_path.relative_to(ROOT))
    return 1 if args.fail_on_problems and problems else 0


if __name__ == "__main__":
    raise SystemExit(main())
