#!/usr/bin/env python3
"""Exact 24-entry TC4 infusion-enchantment coverage ledger for v11.62.66."""
from __future__ import annotations

import argparse
import json
import pathlib
import re

ASPECT_MAP = {
    "MAGIC": "PRAECANTATIO", "CRAFT": "FABRICO", "ORDER": "ORDO",
    "MOTION": "MOTUS", "FLIGHT": "VOLATUS", "TRAVEL": "ITER", "ARMOR": "TUTAMEN",
    "FIRE": "IGNIS", "ENTROPY": "PERDITIO", "WEAPON": "TELUM",
    "PLANT": "HERBA", "AIR": "AER", "WATER": "AQUA",
    "BEAST": "BESTIA", "TOOL": "INSTRUMENTUM", "HARVEST": "METO",
    "MINE": "PERFODIO", "GREED": "LUCRUM", "UNDEAD": "EXANIMIS",
    "VOID": "VACUOS", "EXCHANGE": "PERMUTATIO",
}

EXPECTED_MODERN_IDS = {
    "InfEnchRepair": "thaumcraft:repair",
    "InfEnchHaste": "thaumcraft:haste",
    "InfEnch0": "minecraft:protection",
    "InfEnch1": "minecraft:fire_protection",
    "InfEnch2": "minecraft:blast_protection",
    "InfEnch3": "minecraft:projectile_protection",
    "InfEnch4": "minecraft:feather_falling",
    "InfEnch5": "minecraft:respiration",
    "InfEnch6": "minecraft:aqua_affinity",
    "InfEnch7": "minecraft:thorns",
    "InfEnch8": "minecraft:sharpness",
    "InfEnch9": "minecraft:smite",
    "InfEnch10": "minecraft:bane_of_arthropods",
    "InfEnch11": "minecraft:knockback",
    "InfEnch12": "minecraft:fire_aspect",
    "InfEnch13": "minecraft:looting",
    "InfEnch14": "minecraft:efficiency",
    "InfEnch15": "minecraft:silk_touch",
    "InfEnch16": "minecraft:unbreaking",
    "InfEnch17": "minecraft:fortune",
    "InfEnch18": "minecraft:power",
    "InfEnch19": "minecraft:punch",
    "InfEnch20": "minecraft:flame",
    "InfEnch21": "minecraft:infinity",
}


def parse_original(path: pathlib.Path) -> dict[str, dict]:
    source = path.read_text(encoding="utf-8", errors="ignore")
    start = source.index("private static void initializeArcaneRecipes()")
    end = source.index("private static void initializeInfusionRecipes()")
    segment = source[start:end]
    result: dict[str, dict] = {}
    for raw in segment.splitlines():
        if "addInfusionEnchantmentRecipe" not in raw:
            continue
        key_match = re.search(r'recipes\.put\("([^"]+)"', raw)
        line_match = re.search(r'/\*\s*(\d+)\s*\*/', raw)
        body_match = re.search(
            r'addInfusionEnchantmentRecipe\("INFUSIONENCHANTMENT",\s*(.+?),\s*(\d+),\s*new AspectList\(\)(.+?),\s*new ItemStack\[\]\s*\{(.+)\}\)\);',
            raw,
        )
        if not key_match or not body_match:
            raise ValueError(f"Unable to parse original infusion enchantment line: {raw}")
        key = key_match.group(1)
        aspect_chain = body_match.group(3)
        aspects: dict[str, int] = {}
        for legacy_name, amount in re.findall(r'\.add\(Aspect\.([A-Z_]+),\s*(\d+)\)', aspect_chain):
            modern_name = ASPECT_MAP.get(legacy_name, legacy_name)
            aspects[modern_name] = int(amount)
        components = [part.strip() for part in body_match.group(4).split("),")]
        result[key] = {
            "tc4_key": key,
            "original_line": int(line_match.group(1)) if line_match else None,
            "legacy_enchantment_expression": body_match.group(1).strip(),
            "instability": int(body_match.group(2)),
            "aspects": aspects,
            "legacy_component_count": len(components),
        }
    return result


def parse_index(path: pathlib.Path) -> dict[str, dict]:
    source = path.read_text(encoding="utf-8")
    pattern = re.compile(
        r'add\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*(\d+),\s*(true|false),'
        r'\s*new String\[\]\s*\{([^}]*)\},\s*([^;]+?)\);',
        re.S,
    )
    result: dict[str, dict] = {}
    for match in pattern.finditer(source):
        key, legacy, modern, instability, custom, components_raw, aspects_raw = match.groups()
        components = re.findall(r'"([^"]+)"', components_raw)
        aspects = {}
        for token in re.findall(r'"([A-Z_]+:\d+)"', aspects_raw):
            name, amount = token.split(":", 1)
            aspects[name] = int(amount)
        result[key] = {
            "tc4_key": key,
            "legacy_enchantment_expression": legacy,
            "modern_enchantment_id": modern,
            "instability": int(instability),
            "custom_thaumcraft_enchantment": custom == "true",
            "aspects": aspects,
            "legacy_components": components,
            "legacy_component_count": len(components),
        }
    return result


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", required=True)
    ap.add_argument("--original", required=True)
    ap.add_argument("--json-out", required=True)
    ap.add_argument("--md-out", required=True)
    args = ap.parse_args()

    root = pathlib.Path(args.root).resolve()
    original_path = pathlib.Path(args.original).resolve()
    index_path = root / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentIndex.java"
    original = parse_original(original_path)
    index = parse_index(index_path)
    mod_source = (root / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
    event_source = (root / "src/main/java/com/darkifov/thaumcraft/enchantment/TC4EnchantmentEvents.java").read_text(encoding="utf-8")
    jei_source = (root / "src/main/java/com/darkifov/thaumcraft/compat/jei/TC4JeiPlugin.java").read_text(encoding="utf-8")
    adapter_source = (root / "src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionEnchantmentAdapter.java").read_text(encoding="utf-8")

    rows = []
    for key, original_entry in original.items():
        port = index.get(key)
        issues = []
        if port is None:
            issues.append("missing_index_entry")
            port = {}
        else:
            if port.get("legacy_enchantment_expression") != original_entry["legacy_enchantment_expression"]:
                issues.append("legacy_enchantment_expression")
            if port.get("instability") != original_entry["instability"]:
                issues.append("instability")
            if port.get("aspects") != original_entry["aspects"]:
                issues.append("aspects")
            if port.get("legacy_component_count") != original_entry["legacy_component_count"]:
                issues.append("component_count")
            if port.get("modern_enchantment_id") != EXPECTED_MODERN_IDS[key]:
                issues.append("modern_enchantment_id")
        rows.append({
            **original_entry,
            "modern_enchantment_id": port.get("modern_enchantment_id", ""),
            "custom_thaumcraft_enchantment": port.get("custom_thaumcraft_enchantment", False),
            "port_component_count": port.get("legacy_component_count", 0),
            "issues": issues,
            "status": "MAPPED" if not issues else "MISMATCH",
        })

    extra = sorted(set(index) - set(original))
    missing = sorted(set(original) - set(index))
    custom_registry = {
        "thaumcraft:repair": all(token in mod_source for token in ["REPAIR_ENCHANTMENT", 'register("repair"']),
        "thaumcraft:haste": all(token in mod_source for token in ["HASTE_ENCHANTMENT", 'register("haste"']),
    }
    runtime_checks = {
        "repair_40_tick_cadence": "% 40 == 0" in event_source,
        "repair_original_cost_formula": "Math.sqrt(entry.getValue() * 2.0D)" in event_source,
        "repair_vis_consumption": "consumeInventoryVis" in event_source,
        "repair_single_wand_source": "getContainerSize() - 1; i >= 0; i--" in event_source and "boolean enough = true" in event_source,
        "repair_hover_harness_inventory_exception": "if (!isHoverHarness(stack))" in event_source,
        "haste_impulse_0_015": "level * 0.015D" in event_source,
        "haste_air_reduction": "!player.isOnGround()" in event_source,
        "haste_water_reduction": "player.isInWater()" in event_source,
        "infusion_custom_target_enchantability": "central.getItem().isEnchantable(central)" in adapter_source and "central.getItem().canApplyAtEnchantingTable(" not in adapter_source,
        "jei_enchanted_outputs": "infusionEnchantmentOutputs" in jei_source and "EnchantmentHelper.setEnchantments" in jei_source,
    }
    all_mapped = len(rows) == 24 and not missing and not extra and all(not row["issues"] for row in rows)
    all_custom = all(custom_registry.values())
    all_runtime_source = all(runtime_checks.values())
    status = "STATIC_MAPPING_COMPLETE" if all_mapped and all_custom and all_runtime_source else "PARTIAL"

    report = {
        "version": "11.62.66",
        "original_source": str(original_path),
        "original_registration_count": len(original),
        "port_index_count": len(index),
        "mapped_count": sum(row["status"] == "MAPPED" for row in rows),
        "missing": missing,
        "extra": extra,
        "custom_enchantment_registry": custom_registry,
        "runtime_source_checks": runtime_checks,
        "status": status,
        "entries": rows,
        "limitations": [
            "Static source matching does not prove Forge registration, compilation or in-game execution.",
            "The infusion matrix, XP drain, essentia scaling, compatibility and saved enchantment output require runtime tests.",
            "Repair and Haste behavior is source-ported but has not been verified in a built client or dedicated server.",
        ],
    }
    pathlib.Path(args.json_out).write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    table = [
        "| TC4 key | Строка | Современный ID | Нестабильность | Аспекты | Компоненты | Статус |",
        "|---|---:|---|---:|---|---:|---|",
    ]
    for row in rows:
        aspects = ", ".join(f"{name} {amount}" for name, amount in row["aspects"].items())
        table.append(
            f"| `{row['tc4_key']}` | {row['original_line']} | `{row['modern_enchantment_id']}` | "
            f"{row['instability']} | {aspects} | {row['legacy_component_count']} | {row['status']} |"
        )

    md = f"""# Покрытие инфузионных зачарований TC4 — порт 11.62.66

## Объём оригинала

В `ConfigRecipes.initializeArcaneRecipes()` находятся **24** отдельные регистрации `addInfusionEnchantmentRecipe`: два собственных зачарования Thaumcraft (`Repair`, `Haste`) и 22 ванильных зачарования. Они не являются рецептами Arcane Workbench и не входят в число 109 arcane-регистраций.

## Что добавлено в 11.62.66

- зарегистрированы `thaumcraft:repair` и `thaumcraft:haste`, без которых `InfEnchRepair` и `InfEnchHaste` не могли разрешить выход;
- для Repair восстановлены уровни 1–2, стоимость зачарования, несовместимость с Unbreaking, 40-тиковый цикл, перевод object aspects в primals, формула `floor(sqrt(amount * 2)) * level` и списание vis из жезлов;
- список применимых Repair-предметов ограничен тегом `thaumcraft:repairable`, соответствующим исходному `IRepairable`;
- для Haste восстановлены уровни 1–3 и импульс движения `0.015 × level` с половинным эффектом в воздухе и воде;
- JEI теперь показывает подходящий центральный предмет и его зачарованную копию, а не пустой/неопределённый результат.

## Итог

| Показатель | Значение |
|---|---:|
| Оригинальные регистрации | {len(original)} |
| Записи индекса порта | {len(index)} |
| Точно сопоставлено | {sum(row['status'] == 'MAPPED' for row in rows)} |
| Собственные ID зарегистрированы | {sum(custom_registry.values())}/2 |
| Source-checks runtime-логики | {sum(runtime_checks.values())}/{len(runtime_checks)} |
| Статус | **{status}** |

## Поэлементный маппинг

{chr(10).join(table)}

## Что ещё не подтверждено

Статическая карта не доказывает успешную компиляцию. В игре необходимо проверить принятие центрального предмета, списание XP и essentia, рост уровня зачарования, несовместимость, сохранение NBT, расход vis при Repair, скорость Haste и отображение всех 24 записей в JEI.

Общий статус версии остаётся **PARTIAL / STATIC PASS**.
"""
    pathlib.Path(args.md_out).write_text(md, encoding="utf-8")
    print(json.dumps({k: v for k, v in report.items() if k != "entries"}, ensure_ascii=False, indent=2))
    return 0 if status == "STATIC_MAPPING_COMPLETE" else 1


if __name__ == "__main__":
    raise SystemExit(main())
