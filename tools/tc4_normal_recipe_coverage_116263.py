#!/usr/bin/env python3
"""Build an exact registration-level ledger for TC4 initializeNormalRecipes."""
from __future__ import annotations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECIPES = ROOT / "src/main/resources/data/thaumcraft/recipes"
REPORTS = ROOT / "reports"

rows: list[tuple[str, str, str]] = [
    ("3207 Iron nuggets", "tc4_iron_nuggets_from_ingot.json", "iron ingot -> 9 nuggets"),
    ("3210 Thaumium nuggets", "tc4_thaumium_nuggets_from_ingot.json", "thaumium ingot -> 9 nuggets"),
    ("3213 Void nuggets", "tc4_void_nuggets_from_ingot.json", "void ingot -> 9 nuggets"),
    ("3216 Iron ingot", "tc4_iron_ingot_from_nuggets.json", "9 iron nuggets -> ingot"),
    ("3219 Thaumium ingot", "tc4_thaumium_ingot_from_nuggets.json", "9 thaumium nuggets -> ingot"),
    ("3222 Quicksilver drop", "tc4_quicksilver_drop_from_nuggets.json", "9 quicksilver nuggets -> drop"),
    ("3225 Quicksilver nuggets", "tc4_quicksilver_nuggets_from_drop.json", "drop -> 9 quicksilver nuggets"),
    ("3228 Void ingot", "tc4_void_ingot_from_nuggets.json", "9 void nuggets -> ingot"),
    ("3234 MundaneAmulet", "tc4_mundane_amulet.json", "blank amulet"),
    ("3243 MundaneRing", "tc4_mundane_ring.json", "blank ring"),
    ("3250 MundaneBelt", "tc4_mundane_belt.json", "blank belt"),
    ("3261 TripleMeatTreat A", "tc4_triple_meat_treat_beef_chicken_pork.json", "beef/chicken/pork"),
    ("3263 TripleMeatTreat B", "tc4_triple_meat_treat_beef_chicken_fish.json", "beef/chicken/fish"),
    ("3265 TripleMeatTreat C", "tc4_triple_meat_treat_beef_fish_pork.json", "beef/fish/pork"),
    ("3267 TripleMeatTreat D", "tc4_triple_meat_treat_fish_chicken_pork.json", "fish/chicken/pork"),
    ("3271 Shimmerleaf", "tc4_quicksilver_from_shimmerleaf.json", "shimmerleaf -> quicksilver"),
    ("3275 Cinderpearl", "tc4_blaze_powder_from_cinderpearl.json", "cinderpearl -> blaze powder"),
    ("3281 JarLabel", "jar_label.json", "4 blank labels"),
    ("3312 WandBasic", "basic_wand_original_tc4.json", "iron-capped wooden wand"),
    ("3320 WandCapIron", "iron_wand_cap_original_tc4.json", "iron wand cap"),
    ("3327 KnowFrag", "knowledge_fragments_to_unknown_note_original_tc4.json", "9 fragments -> unknown note"),
    ("3335 PlankGreatwood", "tc4_greatwood_planks.json", "greatwood log -> 4 planks"),
    ("3339 PlankSilverwood", "tc4_silverwood_planks.json", "silverwood log -> 4 planks"),
    ("3345 Greatwood stairs", "greatwood_stairs_original_tc4.json", "4 stairs"),
    ("3347 Silverwood stairs", "silverwood_stairs_original_tc4.json", "4 stairs"),
    ("3351 Greatwood slab", "greatwood_slab_original_tc4.json", "6 slabs"),
    ("3353 Silverwood slab", "silverwood_slab_original_tc4.json", "6 slabs"),
    ("3358 BlockFlesh", "tc4_flesh_block.json", "9 rotten flesh -> flesh block"),
    ("3363 BlockThaumium", "tc4_thaumium_block.json", "9 thaumium ingots -> block"),
    ("3369 Thaumium unpack", "tc4_thaumium_from_block.json", "block -> 9 ingots"),
    ("3373 BlockTallow", "tc4_tallow_block.json", "9 tallow -> block"),
    ("3377 Tallow unpack", "tc4_tallow_from_block.json", "block -> 9 tallow"),
    ("3383 Clusters Aer", "aer_crystal.json", "6 aer shards"),
    ("3383 Clusters Terra", "terra_crystal.json", "6 terra shards"),
    ("3383 Clusters Ignis", "ignis_crystal.json", "6 ignis shards"),
    ("3383 Clusters Aqua", "aqua_crystal.json", "6 aqua shards"),
    ("3383 Clusters Ordo", "ordo_crystal.json", "6 ordo shards"),
    ("3383 Clusters Perditio", "perditio_crystal.json", "6 perditio shards"),
    ("3394 Clusters6", "balanced_crystal_original_tc4.json", "six primal shards"),
    ("3406 Amber block", "amber_block_original_tc4.json", "4 amber -> block"),
    ("3409 Amber bricks", "amber_bricks_original_tc4.json", "4 amber blocks -> bricks"),
    ("3412 Obsidian tile", "obsidian_tile.json", "4 obsidian -> 4 tiles"),
    ("3415 Amber unpack block", "amber_from_block_original_tc4.json", "block -> 4 amber"),
    ("3422 Amber unpack bricks", "amber_from_bricks_original_tc4.json", "bricks -> 4 amber"),
    ("3431 Grate", "item_grate_original_tc4.json", "iron bars + trapdoor"),
    ("3440 Phial", "essentia_phial_original_style.json", "clay + glass -> 8 phials"),
    ("3447 Table", "table_original_tc4_style.json", "wood slabs/planks"),
    ("3456 Scribe1", "scribing_tools_from_phial_original_tc4.json", "phial + feather + black dye"),
    ("3462 Scribe2", "scribing_tools_original_tc4_style.json", "glass bottle + feather + black dye"),
    ("3467 Scribe3", "scribing_tools_refill_original_tc4_style.json", "refill depleted tools"),
    ("3476 Thaumometer", "thaumometer.json", "primal shards + gold + glass"),
]
for name in ["helm", "chest", "legs", "boots", "shovel", "pick", "axe", "hoe", "sword"]:
    rows.append((f"Thaumium {name}", f"tc4_thaumium_{name}.json", "original thaumium equipment pattern"))
for name in ["helm", "chest", "legs", "boots", "shovel", "pick", "axe", "hoe", "sword"]:
    rows.append((f"Void {name}", f"tc4_void_{name}.json", "original void equipment pattern"))
rows.append(("3602 TallowCandle", "tallow_candle_original_tc4.json", "3 white candles"))
for color in ["orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]:
    rows.append((f"3609 Candle {color}", f"tallow_candle_{color}_from_white_original_tc4.json", "white candle + dye"))
rows.append(("3614 Candle whitewash", "tallow_candle_whitewash_original_tc4.json", "any coloured candle + white dye"))

assert len(rows) == 86, len(rows)

results=[]
errors=[]
for original, filename, note in rows:
    path=RECIPES/filename
    exists=path.exists()
    recipe_type="missing"
    if exists:
        try:
            recipe_type=json.loads(path.read_text(encoding="utf-8")).get("type", "unknown")
        except Exception as exc:
            errors.append(f"{filename}: invalid JSON: {exc}")
    else:
        errors.append(f"{filename}: missing")
    standard=recipe_type in {"minecraft:crafting_shaped", "minecraft:crafting_shapeless"}
    if exists and not standard:
        errors.append(f"{filename}: expected standard crafting type, got {recipe_type}")
    results.append({"original": original, "modern_recipe": filename, "description": note,
                    "exists": exists, "recipe_type": recipe_type, "status": "MAPPED" if exists and standard else "FAIL"})

custom=RECIPES/"jar_label_aspect_original_tc4.json"
custom_data=json.loads(custom.read_text(encoding="utf-8")) if custom.exists() else {}
if custom_data.get("type") != "thaumcraft:jar_label":
    errors.append("dynamic JarLabel0..47/JarLabelNull serializer recipe missing")

payload={
    "version":"11.62.63",
    "status":"PASS" if not errors else "FAIL",
    "original_total_registrations":135,
    "original_non_dynamic_registrations":86,
    "mapped_non_dynamic_registrations":sum(r["status"]=="MAPPED" for r in results),
    "original_dynamic_label_assignments":48,
    "original_dynamic_label_reset":1,
    "modern_dynamic_label_recipe_files":1,
    "modern_dynamic_label_jei_displays":49,
    "errors":errors,
    "entries":results,
    "counting_note":"135 is the number of 1.7.10 registrations after loops. The 49 NBT label registrations are intentionally represented by one data recipe serializer and 49 JEI displays, so the number of modern JSON files must not equal 135."
}
REPORTS.mkdir(exist_ok=True)
(REPORTS/"tc4_normal_recipe_coverage_v11.62.63.json").write_text(json.dumps(payload,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")

lines=[
    "# Покрытие `ConfigRecipes.initializeNormalRecipes` — 11.62.63",
    "",
    f"**Статус:** {'STATIC PASS' if not errors else 'FAIL'}",
    "",
    "## Правильная методика подсчёта",
    "",
    "Оригинальные **135 регистраций** состоят из **86 обычных регистраций**, **48 NBT-рецептов назначения аспекта этикетке** и **1 рецепта очистки этикетки**. В современном порте 49 NBT-вариантов намеренно свёрнуты в один сериализатор `thaumcraft:jar_label`, а JEI показывает 48 назначений и 1 очистку. Поэтому сравнение `135` с числом JSON-файлов некорректно.",
    "",
    "| Показатель | Значение |",
    "|---|---:|",
    "| Оригинальные регистрации после разворачивания циклов | 135 |",
    "| Оригинальные нединамические регистрации | 86 |",
    f"| Сопоставлено нединамических регистраций | {payload['mapped_non_dynamic_registrations']} |",
    "| Динамические назначения этикеток | 48 |",
    "| Очистка этикетки | 1 |",
    "| Современные runtime-сериализаторы для семейства этикеток | 1 |",
    "| JEI-представления семейства этикеток | 49 |",
    "",
    "## Поэлементный маппинг 86 нединамических регистраций",
    "",
    "| № | Оригинальная регистрация | Рецепт порта | Статус |",
    "|---:|---|---|---|",
]
for i,row in enumerate(results,1):
    lines.append(f"| {i} | `{row['original']}` | `{row['modern_recipe']}` | {row['status']} |")
lines += ["", "## Ограничение", "", "Этот документ подтверждает наличие и тип рецептов статически. Он не заменяет компиляцию Forge, загрузку datapack-рецептов сервером и просмотр в JEI-клиенте.", ""]
(REPORTS/"TC4_NORMAL_RECIPE_COVERAGE_V11_62_63.md").write_text("\n".join(lines),encoding="utf-8")

print(f"TC4 normal recipe coverage: {'PASS' if not errors else 'FAIL'} ({payload['mapped_non_dynamic_registrations']}/86 + dynamic 49)")
for error in errors:
    print(" -",error)
raise SystemExit(1 if errors else 0)
