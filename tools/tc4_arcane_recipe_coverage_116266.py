#!/usr/bin/env python3
"""Full registration-level mapping for TC4 4.2.3.5 initializeArcaneRecipes()."""
from __future__ import annotations

import argparse
import json
import pathlib
import re
from collections import Counter

RUNTIME_WAND_CAP_KEYS = {
    "WandCapGold", "WandCapCopper", "WandCapSilverInert",
    "WandCapThaumiumInert", "WandCapVoidInert",
}


def parse_original(path: pathlib.Path) -> tuple[list[dict], list[dict], list[dict]]:
    source = path.read_text(encoding="utf-8", errors="ignore")
    start = source.index("private static void initializeArcaneRecipes()")
    end = source.index("private static void initializeInfusionRecipes()")
    segment = source[start:end]

    arcane: list[dict] = []
    normal: list[dict] = []
    infusion_enchantments: list[dict] = []
    for raw in segment.splitlines():
        match = re.search(r'ConfigResearch\.recipes\.put\("([^"]+)', raw)
        if not match:
            continue
        key = match.group(1)
        line_match = re.search(r'/\*\s*(\d+)\s*\*/', raw)
        line = int(line_match.group(1)) if line_match else None
        if "addArcaneCraftingRecipe" in raw:
            family = [(key, None)]
            if key == "Banner_":
                family = [(f"Banner_{i}", i) for i in range(16)]
            elif key == "PrimalArrow_":
                family = [(f"PrimalArrow_{i}", i) for i in range(6)]
            for expanded_key, variant in family:
                arcane.append({
                    "tc4_key": expanded_key,
                    "call_type": "ARCANE_SHAPED",
                    "original_line": line,
                    "loop_family": key if variant is not None else None,
                    "loop_variant": variant,
                })
        elif "addShapelessArcaneCraftingRecipe" in raw:
            arcane.append({
                "tc4_key": key,
                "call_type": "ARCANE_SHAPELESS",
                "original_line": line,
                "loop_family": None,
                "loop_variant": None,
            })
        elif "addInfusionEnchantmentRecipe" in raw:
            infusion_enchantments.append({"tc4_key": key, "original_line": line})
        elif "GameRegistry.add" in raw:
            normal.append({"tc4_key": key, "original_line": line})
    return arcane, normal, infusion_enchantments


def load_static(root: pathlib.Path) -> dict[str, list[str]]:
    recipe_dir = root / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench"
    result: dict[str, list[str]] = {}
    for path in sorted(recipe_dir.glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        key = data.get("tc4_key")
        if key:
            result.setdefault(key, []).append(path.relative_to(root).as_posix())
    return result


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", required=True)
    ap.add_argument("--original", required=True)
    ap.add_argument("--json-out", required=True)
    ap.add_argument("--md-out", required=True)
    args = ap.parse_args()

    root = pathlib.Path(args.root).resolve()
    original = pathlib.Path(args.original).resolve()
    arcane, normal, infusion_enchantments = parse_original(original)
    static = load_static(root)
    wand_index_path = root / "src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java"
    wand_index = wand_index_path.read_text(encoding="utf-8")

    rows: list[dict] = []
    for ordinal, entry in enumerate(arcane, 1):
        key = entry["tc4_key"]
        files = static.get(key, [])
        if files:
            implementation = "DATAPACK_JSON"
            implementation_path = ", ".join(files)
            status = "MAPPED"
        elif key in RUNTIME_WAND_CAP_KEYS and key in wand_index:
            implementation = "GENERATED_JAVA"
            implementation_path = wand_index_path.relative_to(root).as_posix()
            status = "MAPPED"
        else:
            implementation = "MISSING"
            implementation_path = "—"
            status = "MISSING"
        rows.append({
            "registration": ordinal,
            **entry,
            "implementation": implementation,
            "implementation_path": implementation_path,
            "status": status,
        })

    shaped = [row for row in rows if row["call_type"] == "ARCANE_SHAPED"]
    shapeless = [row for row in rows if row["call_type"] == "ARCANE_SHAPELESS"]
    missing = [row["tc4_key"] for row in rows if row["status"] != "MAPPED"]
    literal_shaped_call_sites = len({(row["original_line"], row["loop_family"] or row["tc4_key"]) for row in shaped})
    literal_shapeless_call_sites = len(shapeless)
    summary = {
        "version": "11.62.66",
        "original_source": str(original),
        "method": "ConfigRecipes.initializeArcaneRecipes",
        "literal_shaped_call_sites": literal_shaped_call_sites,
        "expanded_shaped_registrations": len(shaped),
        "literal_shapeless_call_sites": literal_shapeless_call_sites,
        "expanded_shapeless_registrations": len(shapeless),
        "literal_arcane_call_sites_total": literal_shaped_call_sites + literal_shapeless_call_sites,
        "expanded_arcane_workbench_registrations_total": len(rows),
        "loop_expansion": {
            "Banner_": 16,
            "PrimalArrow_": 6,
            "extra_registrations_beyond_literal_calls": 20,
        },
        "excluded_from_arcane_workbench_count": {
            "normal_crafting": normal,
            "infusion_enchantments": infusion_enchantments,
        },
        "implementation_counts": dict(Counter(row["implementation"] for row in rows)),
        "mapped": len(rows) - len(missing),
        "missing": missing,
        "coverage_percent": round((len(rows) - len(missing)) * 100.0 / len(rows), 2),
        "status": "STATIC_MAPPING_COMPLETE" if not missing and len(rows) == 109 else "PARTIAL",
        "registrations": rows,
        "limitations": [
            "This is a source/static registration map, not proof of Java compilation or runtime behavior.",
            "The 84 shaped figure is the number of literal call sites; it is not the expanded registration count.",
            "JEI, research gating, vis payment, NBT outputs and networking still require runtime verification.",
        ],
    }
    pathlib.Path(args.json_out).write_text(
        json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    table_lines = [
        "| № | TC4 key | Тип | Строка TC4 | Вариант цикла | Реализация порта | Статус |",
        "|---:|---|---|---:|---|---|---|",
    ]
    for row in rows:
        variant = "—" if row["loop_variant"] is None else str(row["loop_variant"])
        impl = f"`{row['implementation_path']}`" if row["implementation_path"] != "—" else "—"
        table_lines.append(
            f"| {row['registration']} | `{row['tc4_key']}` | {row['call_type']} | "
            f"{row['original_line'] or '—'} | {variant} | {impl} | {row['status']} |"
        )

    normal_text = ", ".join(f"`{x['tc4_key']}` (стр. {x['original_line']})" for x in normal)
    infusion_text = ", ".join(f"`{x['tc4_key']}`" for x in infusion_enchantments)
    md = f"""# Полный маппинг arcane-рецептов TC4 — порт 11.62.66

## Разрешение расхождения 89 против 109

В оригинальном `ConfigRecipes.initializeArcaneRecipes()` действительно находятся **84 буквальных вызова** `addArcaneCraftingRecipe` и **5 буквальных вызовов** `addShapelessArcaneCraftingRecipe`. Сложение 84 + 5 даёт 89 **точек вызова в исходнике**, но не количество созданных рецептов.

Два shaped-вызова находятся внутри циклов:

- `Banner_ + a` выполняется 16 раз, поэтому вместо одной точки вызова создаёт 16 регистраций: прирост относительно буквального подсчёта **+15**;
- `PrimalArrow_ + a` выполняется 6 раз: прирост **+5**.

Следовательно, исходное количество регистраций Arcane Workbench:

- shaped: **84 + 15 + 5 = {len(shaped)}**;
- shapeless: **{len(shapeless)}**;
- всего: **{len(rows)}**.

Число **89** было корректным только как количество буквальных arcane-вызовов. Число **109** является количеством регистраций после разворачивания циклов.

## Отдельно исключённые семейства

В том же Java-методе находятся элементы, не являющиеся рецептами Arcane Workbench:

- обычный crafting: {normal_text};
- инфузионные зачарования: 24 записи от `InfEnchRepair` и `InfEnchHaste` до `InfEnch21`.

Они не входят в 109. Полный список инфузионных зачарований приведён в отдельном отчёте.

## Итог статического покрытия

| Показатель | Значение |
|---|---:|
| Буквальные shaped-вызовы | {literal_shaped_call_sites} |
| Shaped-регистрации после циклов | {len(shaped)} |
| Shapeless-регистрации | {len(shapeless)} |
| Всего Arcane Workbench | {len(rows)} |
| Datapack JSON | {summary['implementation_counts'].get('DATAPACK_JSON', 0)} |
| Генерируемые Java-рецепты наконечников | {summary['implementation_counts'].get('GENERATED_JAVA', 0)} |
| Сопоставлено | {summary['mapped']} |
| Отсутствует | {len(missing)} |

Статус карты: **{summary['status']}**. Это подтверждает структуру исходников и ресурсов, но не заменяет компиляцию и runtime-тест.

## Поэлементная таблица 109 регистраций

{chr(10).join(table_lines)}

## Ограничения

- Маппинг не подтверждает успешную загрузку JSON, списание vis, research gating, NBT-результаты и отображение JEI.
- Пять наконечников жезлов формируются `TC4ConfigRecipesWandIndex` и требуют Java-компиляции.
- Общий статус версии остаётся **PARTIAL / STATIC PASS** до сборки и проверки в клиенте/на выделенном сервере.
"""
    pathlib.Path(args.md_out).write_text(md, encoding="utf-8")
    print(json.dumps({k: v for k, v in summary.items() if k != "registrations"}, ensure_ascii=False, indent=2))
    return 0 if summary["status"] == "STATIC_MAPPING_COMPLETE" else 1


if __name__ == "__main__":
    raise SystemExit(main())
