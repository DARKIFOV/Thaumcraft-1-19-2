#!/usr/bin/env python3
"""Registration-level TC4 4.2.3.5 arcane recipe coverage ledger for v11.62.64."""
from __future__ import annotations
import argparse, json, pathlib, re

INFUSION_KEYS = {"InfEnchRepair", "InfEnchHaste", *{f"InfEnch{i}" for i in range(22)}}
NORMAL_KEYS = {"ArcaneStone2", "ArcaneStone3", "ArcaneStone4"}
RUNTIME_WAND_CAP_KEYS = {"WandCapGold", "WandCapCopper", "WandCapSilverInert", "WandCapThaumiumInert", "WandCapVoidInert"}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", required=True)
    ap.add_argument("--original", required=True)
    ap.add_argument("--json-out", required=True)
    ap.add_argument("--md-out", required=True)
    args = ap.parse_args()
    root = pathlib.Path(args.root)
    source = pathlib.Path(args.original).read_text(encoding="utf-8", errors="ignore")
    start = source.index("private static void initializeArcaneRecipes()")
    end = source.index("private static void initializeInfusionRecipes()")
    segment = source[start:end]
    base_keys = re.findall(r'recipes\.put\("([^"]+)', segment)
    fixed_keys = [k for k in base_keys if k not in {"Banner_", "PrimalArrow_"}]

    recipe_dir = root / "src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench"
    static = {}
    for path in sorted(recipe_dir.glob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        key = data.get("tc4_key")
        if key:
            static.setdefault(key, []).append(path.name)

    genuine_fixed = sorted(set(fixed_keys) - INFUSION_KEYS - NORMAL_KEYS - RUNTIME_WAND_CAP_KEYS)
    fixed_exact = sorted(k for k in genuine_fixed if k in static)
    fixed_missing = sorted(set(genuine_fixed) - set(fixed_exact))
    primal_expected = [f"PrimalArrow_{i}" for i in range(6)]
    primal_exact = [k for k in primal_expected if k in static]
    banner_expected = [f"Banner_{i}" for i in range(16)]
    banner_exact = [k for k in banner_expected if k in static]

    wand_index = (root / "src/main/java/com/darkifov/thaumcraft/wand/TC4ConfigRecipesWandIndex.java").read_text(encoding="utf-8")
    cap_runtime = sorted(k for k in RUNTIME_WAND_CAP_KEYS if k in wand_index)
    cap_missing = sorted(RUNTIME_WAND_CAP_KEYS - set(cap_runtime))

    total_arcane = len(genuine_fixed) + len(primal_expected) + len(banner_expected) + len(RUNTIME_WAND_CAP_KEYS)
    covered = len(fixed_exact) + len(primal_exact) + len(banner_exact) + len(cap_runtime)
    malformed_banner_adapter = static.get("'Banner_' + a", [])

    data = {
        "version": "11.62.64",
        "original_source": str(args.original),
        "method_literal_key_count": len(base_keys),
        "expanded_original_registration_count": len(fixed_keys) + 16 + 6,
        "excluded_from_arcane_workbench": {
            "normal_crafting": sorted(NORMAL_KEYS),
            "infusion_enchantments": sorted(INFUSION_KEYS),
        },
        "arcane_workbench_registration_count": total_arcane,
        "covered_registration_count": covered,
        "coverage_percent": round(covered * 100.0 / total_arcane, 2),
        "fixed_arcane": {"expected": len(genuine_fixed), "covered": len(fixed_exact), "missing": fixed_missing},
        "primal_arrow_family": {"expected": 6, "covered": len(primal_exact), "missing": sorted(set(primal_expected)-set(primal_exact))},
        "banner_family": {"expected": 16, "covered": len(banner_exact), "missing": sorted(set(banner_expected)-set(banner_exact)), "legacy_adapter_files": malformed_banner_adapter},
        "runtime_wand_caps": {"expected": 5, "covered": len(cap_runtime), "missing": cap_missing},
        "new_v11_62_64_keys": {
            k: static.get(k, []) for k in ["HungryChest", "NodeChargeRelay", "WardedGlass"]
        },
        "status": "PARTIAL" if covered < total_arcane else "STATIC_COVERAGE_COMPLETE",
        "limitations": [
            "Coverage is source/static registration coverage, not compile or runtime proof.",
            "The 16 NBT-coloured banner variants still require a modern functional banner implementation.",
            "Wand-cap coverage is generated in Java and remains uncompiled in this environment."
        ],
    }
    pathlib.Path(args.json_out).write_text(json.dumps(data, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")

    md = f'''# Покрытие оригинальных arcane-рецептов TC4 — порт 11.62.64

## Методика

Сверка выполнена по `ConfigRecipes.initializeArcaneRecipes()` из TC4 4.2.3.5. Циклы развёрнуты: 16 вариантов знамен и 6 вариантов первичных стрел считаются отдельными регистрациями. Три `ArcaneStone2..4` относятся к обычному верстаку, а 24 `InfEnch*` — к системе инфузионных зачарований, поэтому они исключены из числа рецептов Arcane Workbench.

## Итог

| Группа | Оригинал | Статически сопоставлено | Осталось |
|---|---:|---:|---:|
| Фиксированные arcane-рецепты | {len(genuine_fixed)} | {len(fixed_exact)} | {len(fixed_missing)} |
| Первичные стрелы | 6 | {len(primal_exact)} | {6-len(primal_exact)} |
| Цветные знамена | 16 | {len(banner_exact)} | {16-len(banner_exact)} |
| Наконечники жезлов, генерируемые Java | 5 | {len(cap_runtime)} | {5-len(cap_runtime)} |
| **Всего Arcane Workbench** | **{total_arcane}** | **{covered}** | **{total_arcane-covered}** |

Статическое покрытие: **{covered}/{total_arcane} ({data['coverage_percent']}%)**.

## Добавлено в 11.62.64

- `HungryChest` — точная форма, исследование `HUNGRYCHEST`, стоимость `AER 5 + ORDO 3 + PERDITIO 3`;
- `NodeChargeRelay` — точная форма, исследование `VISCHARGERELAY`, стоимость `IGNIS 16 + ORDO 16 + AER 16`;
- `WardedGlass` — выход 8, исследование `WARDEDARCANA`, стоимость `AQUA 5 + ORDO 10 + TERRA 5 + IGNIS 5`.

Эти три ключа теперь имеют отдельные data-driven рецепты и автоматически передаются в категорию Arcane Workbench JEI.

## Оставшийся пробел

Не перенесены как точные функциональные рецепты **16 вариантов `Banner_0..15`**. В старом материальном наборе остаётся один файл `tc4_banner__a.json` с буквальным ключом `"'Banner_' + a"`; он не заменяет развёрнутые оригинальные варианты и считается затычкой. Для корректного переноса требуется функциональный блок/предмет знамени с сохранением цвета в NBT или современном data component, затем 16 отдельных представлений в JEI.

## Ограничения оценки

- Это статическое сопоставление регистраций, а не доказательство компиляции или runtime-поведения.
- Пять рецептов наконечников жезлов формируются кодом `TC4ConfigRecipesWandIndex`; их наличие проверено по исходнику, но Java не скомпилирована.
- Статус всей версии остаётся **PARTIAL / STATIC PASS**.
'''
    pathlib.Path(args.md_out).write_text(md, encoding="utf-8")
    print(json.dumps(data, ensure_ascii=False, indent=2))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
