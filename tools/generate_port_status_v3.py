#!/usr/bin/env python3
"""Generate the objective TC4 port status report.

A successful source audit is not a runtime PASS. Build PASS is awarded only
when this script is called with an existing main JAR after Gradle completed.
Runtime/P0/migration/JEI PASS values are accepted only from
runtime_artifacts/runtime_test_manifest.json and only when all named artifact
files exist.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from datetime import datetime
from zoneinfo import ZoneInfo
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
REPORTS = ROOT / "reports"
P0_KEYS = (
    "essentia_jars", "aura_node_item", "bone_bow", "traveling_trunk",
    "crimson_cultists", "fortress_armor", "outer_lands",
)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def count(pattern: str) -> int:
    return sum(1 for _ in ROOT.glob(pattern))


def read_json(path: Path, default: Any) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return default


def valid_artifacts(entry: dict[str, Any]) -> bool:
    artifacts = entry.get("artifacts") or []
    if not artifacts:
        return False
    for artifact in artifacts:
        if not isinstance(artifact, dict):
            return False
        rel = artifact.get("path")
        expected = artifact.get("sha256")
        if not rel or not expected:
            return False
        path = ROOT / str(rel)
        if not path.is_file() or sha256(path).lower() != str(expected).lower():
            return False
    return True


def artifact_paths(entry: dict[str, Any]) -> list[str]:
    if not valid_artifacts(entry):
        return []
    return [str(artifact["path"]) for artifact in entry.get("artifacts", [])]


def runtime_axis(manifest: dict[str, Any], subsystem: str, axis: str, na: set[str]) -> str:
    if axis in na:
        return "N/A"
    entry = ((manifest.get("subsystems") or {}).get(subsystem) or {}).get(axis) or {}
    status = str(entry.get("status", "NOT_TESTED"))
    if status in {"PASS", "PARTIAL", "FAIL"} and valid_artifacts(entry):
        return status
    return "NOT TESTED"


def runtime_evidence(manifest: dict[str, Any], subsystem: str) -> list[str]:
    result: list[str] = []
    for entry in (((manifest.get("subsystems") or {}).get(subsystem) or {}).values()):
        if isinstance(entry, dict):
            result.extend(artifact_paths(entry))
    return sorted(set(result))


def manifest_pass(manifest: dict[str, Any], section: str) -> bool:
    entry = manifest.get(section) or {}
    return entry.get("status") == "PASS" and valid_artifacts(entry)


def p0_pass(manifest: dict[str, Any], key: str) -> bool:
    entry = (manifest.get("p0") or {}).get(key) or {}
    return entry.get("status") == "PASS" and valid_artifacts(entry)


def registered_counts() -> tuple[int, int, int, int]:
    mod = (ROOT / "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
    client = (ROOT / "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java").read_text(encoding="utf-8")
    return (
        len(re.findall(r"RegistryObject<EntityType<", mod)),
        len(re.findall(r"RegistryObject<BlockEntityType<", mod)),
        len(re.findall(r"EntityRenderers\.register\(", client)),
        len(re.findall(r"BlockEntityRenderers\.register\(", client)),
    )


def generate(version: str, jar: Path | None, output: Path) -> dict[str, Any]:
    REPORTS.mkdir(exist_ok=True)
    visual = read_json(REPORTS / f"visual_parity_audit_v{version}.json", {})
    manifest = read_json(ROOT / "runtime_artifacts/runtime_test_manifest.json", {})
    manifest_template = read_json(ROOT / "runtime_artifacts/runtime_test_manifest.template.json", {})
    research = read_json(REPORTS / f"tc4_full_research_thaumonomicon_audit_{version}.json", {})

    build_pass = jar is not None and jar.is_file()
    jar_hash = sha256(jar) if build_pass else None
    p0_runtime = {key: p0_pass(manifest, key) for key in P0_KEYS}
    criteria = {
        "successful_build": build_pass,
        "all_p0_runtime_fixed": all(p0_runtime.values()),
        "runtime_protocol_complete": manifest_pass(manifest, "runtime_protocol"),
        "world_migration_verified": manifest_pass(manifest, "world_migration"),
        "jei_verified": manifest_pass(manifest, "jei"),
    }
    criteria_done = sum(criteria.values())
    overall = "PASS" if criteria_done == 5 else ("PARTIAL" if build_pass else "FAIL")

    entity_types, block_entity_types, entity_renderers, block_entity_renderers = registered_counts()
    java_files = count("src/main/java/**/*.java")
    item_models = count("src/main/resources/assets/thaumcraft/models/item/*.json")
    normal_recipes = count("src/main/resources/data/thaumcraft/recipes/*.json")
    arcane_recipes = count("src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/*.json")
    alchemy_recipes = count("src/main/resources/data/thaumcraft/thaumcraft_alchemy/*.json")
    infusion_recipes = count("src/main/resources/data/thaumcraft/thaumcraft_infusion/*.json")
    vstats = visual.get("stats") or {}
    p0_source_complete = int(vstats.get("p0_source_contract_complete", 0))
    p0_static_partial = int(vstats.get("p0_static_partial", 0))
    runtime_test_cases = len(manifest_template.get("tests") or [])
    runtime_test_evidenced = sum(
        1 for entry in (manifest.get("tests") or [])
        if isinstance(entry, dict)
        and str(entry.get("status", "NOT_TESTED")) in {"PASS", "PARTIAL", "FAIL"}
        and valid_artifacts(entry)
    )

    matrix_specs = [
        ("Аспекты и tags", "aspects_tags", "PARTIAL", {"W"}, "48 source entries; scan/tag runtime required"),
        ("Таумометр", "thaumometer", "PARTIAL", {"W"}, "scan/overlay source guards; range/sound/repeat runtime required"),
        ("Таумономикон", "thaumonomicon", "PARTIAL", {"W"}, "201 entries/591 pages plus v11.62.82 targeted-note inventory flow source audit; browser popup, GUI scales and note-creation runtime required"),
        ("Research Table", "research_table", "PARTIAL", {"W"}, "v11.62.82 source audit: Thaumonomicon-only note creation, table-only unfinished-note editing, completed item learning, fixed inventory and bookshelf/Brain Jar bonus sources; Expertise/Mastery/duplication/save runtime required"),
        ("Arcane Workbench", "arcane_workbench", "PARTIAL", {"W"}, "recipe/cost source guards; vis/research/GUI runtime required"),
        ("Жезлы/фокусы", "wands_foci", "PARTIAL", {"W"}, "renderer and cost source guards; all contexts/network runtime required"),
        ("Узлы/Node Jar", "aura_nodes_node_jar", "PARTIAL", set(), "node source audit; six types/three modifiers runtime required"),
        ("Essentia jars", "essentia_jars", "PARTIAL", {"W"}, "NBT-aware BEWLR source contract; 48 labels runtime required"),
        ("Essentia transport", "essentia_transport", "PARTIAL", {"W"}, "v11.62.79 source audit: reservoir suction 24/six faces/active pull and buffer real-suction arbitration; conflict/rollback/soak runtime required"),
        ("Furnace/alembics/centrifuge", "processing_devices", "PARTIAL", {"W"}, "v11.62.79 source audit: centrifuge queues input while output is occupied or redstone-paused; complete chain runtime required"),
        ("Infusion Matrix", "infusion_matrix", "PARTIAL", {"W"}, "recipe and renderer guards; stability/events/save runtime required"),
        ("JEI", "jei", "PARTIAL", {"N", "W"}, "source plugin/recipe registration; JEI and no-JEI runtime required"),
        ("Bone Bow", "bone_bow", "PARTIAL", {"W"}, "charge/item-model source contract; arrows/enchants/visual runtime required"),
        ("Traveling Trunk", "traveling_trunk", "PARTIAL", {"W"}, "entity/inventory/model/capability source contract; runtime required"),
        ("Crimson cultists", "crimson_cultists", "PARTIAL", set(), "humanoid renderer source contract; four-role side-by-side runtime required"),
        ("Fortress Armor", "fortress_armor", "PARTIAL", {"W"}, "dedicated model source contract; slim/default/masks runtime required"),
        ("Големы", "golems", "PARTIAL", {"W"}, "USE marker-side/empty-hand handling, weighted fishing quality and priority creeper avoidance are source-guarded; all materials/cores/upgrades/markers still require runtime"),
        ("Warp/Eldritch", "warp_eldritch", "PARTIAL", set(), "v11.62.81 source audit: effect-only Warp Ward authority with legacy NBT migration, separate sticky-event decay, full bucket/counter sync, TC4 spawn search and 0.75 Death Gaze cone; runtime/network/visual proof required"),
        ("Taint/Eerie/Forest", "taint_eerie_forest", "PARTIAL", set(), "v11.62.80 source audit: five fibre states, persistent taint columns, original spread thresholds, spore lifecycle and taint-spider renderer; biome colours/weather, cleanse and structures still require runtime"),
        ("Outer Lands", "outer_lands", "PARTIAL", set(), "aligned portal maze, TC4 portal-room geometry and lock-gated boss cycle in source; traversal/save/return runtime required"),
        ("Mirrors", "mirrors", "PARTIAL", {"W"}, "source mirror contracts; cross-dimension/save/automation runtime required"),
        ("Brain in a Jar", "brain_jar", "PARTIAL", {"W"}, "source XP/renderer contract; comparator/NBT/visual runtime required"),
        ("Миграция миров", "world_migration", "PARTIAL", {"V", "C"}, "legacy aliases/source mapping; five-version migration runtime required"),
        ("Dedicated server", "dedicated_server", "PARTIAL", {"V", "C"}, "server-safe source guards; two-client runtime required"),
    ]
    matrix_lines: list[str] = []
    source_evidence = f"reports/visual_parity_audit_v{version}.json"
    for title, key, source_status, na_axes, note in matrix_specs:
        axes = [runtime_axis(manifest, key, axis, na_axes) for axis in ("G", "V", "N", "W", "C")]
        evidence = [source_evidence, f"reports/TC4_{version}_FULL_STATIC_CI.log"]
        evidence.extend(runtime_evidence(manifest, key))
        evidence_text = ", ".join(f"`{item}`" for item in sorted(set(evidence)))
        matrix_lines.append(
            f"| {title} | {source_status} | {axes[0]} | {axes[1]} | {axes[2]} | {axes[3]} | {axes[4]} | {evidence_text}; {note} |"
        )
    matrix_markdown = "\n".join(matrix_lines)

    report_date = datetime.now(ZoneInfo("Europe/Helsinki")).date().isoformat()
    metrics = {
        "version": version,
        "date": report_date,
        "platform": {"minecraft": "1.19.2", "forge": "43.5.2", "java": "17"},
        "overall_status": overall,
        "build": {
            "gradlew_build": "PASS" if build_pass else "FAIL_NO_CONFIRMED_BUILD",
            "compileJava": "PASS" if build_pass else "NOT_CONFIRMED",
            "jar": str(jar.relative_to(ROOT)) if build_pass and jar.is_relative_to(ROOT) else (str(jar) if build_pass else None),
            "jar_sha256": jar_hash,
        },
        "java_files": java_files,
        "research_entries": int(research.get("research_entries", 201)),
        "research_pages": int(research.get("pages", 591)),
        "aspects": 48,
        "recipe_files": {"normal": normal_recipes, "arcane": arcane_recipes, "alchemy": alchemy_recipes, "infusion": infusion_recipes},
        "registries": {
            "entity_types": entity_types, "entity_reference": 50,
            "block_entity_types": block_entity_types, "tile_entity_reference": 73,
            "entity_renderer_registration_calls": entity_renderers,
            "block_entity_renderer_registration_calls": block_entity_renderers,
        },
        "item_model_json": item_models,
        "runtime_tests": {"template_cases": runtime_test_cases, "evidenced_cases": runtime_test_evidenced},
        "object_scope": {"core_candidate_ids": 255, "legacy_alias_ids": 283, "addon_ids": 170, "total_unique_object_ids": 708},
        "p0": {
            "source_contract_complete": p0_source_complete,
            "static_partial": p0_static_partial,
            "runtime_pass": sum(p0_runtime.values()),
            "runtime": p0_runtime,
        },
        "release_criteria": criteria,
        "criteria_done": criteria_done,
    }

    build_text = "PASS" if build_pass else "FAIL — успешный `gradlew build` не получен"
    hash_text = f"`{jar_hash}`" if jar_hash else "`N/A — JAR не собран`"
    source_status = {
        "outer_lands": "STATIC PARTIAL",
    }
    source_default = "SOURCE CONTRACT COMPLETE"
    p0_rows = [
        ("Essentia jars item contexts", "Статическая block model не показывала NBT-content/filter/label", source_default, p0_runtime["essentia_jars"]),
        ("Aura node item", "Обычный debug BlockItem вместо original player path", source_default, p0_runtime["aura_node_item"]),
        ("Bone Bow", "Плоский idle sprite без pull/gameplay parity", source_default, p0_runtime["bone_bow"]),
        ("Traveling Trunk", "Плоский recipe item без сущности", source_default, p0_runtime["traveling_trunk"]),
        ("Crimson cultists", "Block placeholders вместо humanoid renderer", source_default, p0_runtime["crimson_cultists"]),
        ("Fortress Armor", "Vanilla HumanoidModel вместо dedicated geometry", source_default, p0_runtime["fortress_armor"]),
        ("Outer Lands", "Отдельные файлы ошибочно считались готовой системой", source_status["outer_lands"], p0_runtime["outer_lands"]),
    ]
    p0_markdown = "\n".join(
        f"| P0 | {name} | {problem} | {source} | {'PASS' if runtime else 'NOT TESTED'} |"
        for name, problem, source, runtime in p0_rows
    )

    report = f"""# Thaumcraft Legacy Rebuild {version} — объективный статус порта

**Дата:** {report_date}  
**Целевая платформа:** Minecraft 1.19.2, Forge 43.5.2, Java 17  
**Эталон:** Thaumcraft 4.2.3.5  
**Общий статус:** **{overall}**  
**Статус сборки:** **{build_text}**  
**SHA-256 JAR:** {hash_text}

> Source/resource contracts, runtime и визуальная parity — разные уровни доказательств. Runtime/visual/network PASS допустим только при существующем артефакте с проверенным SHA-256, перечисленном в `runtime_artifacts/runtime_test_manifest.json`. Шаблон протокола проверяется `tools/validate_runtime_manifest.py`.

---

## 1. Подтверждённые данные

| Показатель | Значение | Ограничение |
|---|---:|---|
| Успешная компиляция `gradlew build` | {'✅' if build_pass else '❌'} | {'Основной JAR существует' if build_pass else 'Для этой версии подтверждённого build нет'} |
| SHA-256 JAR | {hash_text} | Вычисляется только после успешного Gradle-шага |
| Java-файлы | {java_files} | Статический count |
| Исследования | {metrics['research_entries']} / 201 | Runtime просмотрено 0 / 201 без manifest |
| Страницы исследований | {metrics['research_pages']} | Runtime просмотрено 0 без manifest |
| Аспекты | 48 / 48 | Runtime проверено 0 / 48 без manifest |
| JSON обычных рецептов | {normal_recipes} | Runtime проверено 0 / 86 эталонных нединамических крафтов |
| Динамические этикетки | serializer присутствует | 48 аспектов + очистка требуют runtime |
| Arcane recipe JSON | {arcane_recipes} | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Alchemy recipe JSON | {alchemy_recipes} | Эталонный denominator должен быть зафиксирован отдельным source manifest |
| Infusion recipe JSON | {infusion_recipes} | Materialized/JEI запись не является runtime PASS |
| Mod entity types | {entity_types} / 50 | Полнота TC4 не подтверждена |
| BlockEntity types | {block_entity_types} / 73 | Полнота TC4 не подтверждена |
| Entity renderer calls | {entity_renderers} | Не эквивалентно числу оригинальных entity |
| BlockEntity renderer calls | {block_entity_renderers} / 50 TESR reference | Runtime проверено 0 без manifest |
| Item model JSON | {item_models} | Парсинг модели не доказывает внешний вид |
| Runtime test cases | {runtime_test_evidenced} / {runtime_test_cases} | Учитываются только статусы с существующим SHA-256-проверенным артефактом |
| P0 source contracts | {p0_source_complete} complete + {p0_static_partial} partial / 7 | Runtime visual PASS: {sum(p0_runtime.values())} / 7 |

### 1.1. Разделение области оценки

| Область | ID | Правило |
|---|---:|---|
| Core TC4 candidate objects | 255 | Только core-аудит; количество не означает готовность |
| Legacy aliases / migration IDs | 283 | Только миграция и отсутствие UI-дублей |
| Add-ons / compatibility branches | 170 | Исключены из оценки core TC4 |
| Всего уникальных предметов/блоков | 708 | Инвентаризация, не шкала готовности |

---

## 2. Матрица подсистем S/G/V/N/W/C

`S=PARTIAL` подтверждает только source-аудит. Для G/V/N/W/C статусы `PASS`, `PARTIAL` и `FAIL` принимаются только при существующем SHA-256-проверенном runtime-артефакте; иначе ставится `NOT TESTED`.

| Подсистема | S | G | V | N | W | C | Доказательство S / требуемый runtime |
|---|---|---|---|---|---|---|---|
{matrix_markdown}

---

## 3. P0/P1

| Приоритет | Объект | Проблема | Source status | Runtime status |
|---|---|---|---|---|
{p0_markdown}
| P1 | Свечи | Геометрия/частицы/стабилизация | PARTIAL | NOT TESTED |
| P1 | Hungry Chest | Модель/pickup behavior | PARTIAL | NOT TESTED |
| P1 | Thaumonomicon GUI | Координаты без полной клиентской проверки | PARTIAL | NOT TESTED |
| P1 | Infusion/JEI | Статический набор без клиентской полноты | PARTIAL | NOT TESTED |

Source status не закрывает P0. Требуются build PASS и runtime PASS всех обязательных состояний.

---

## 4. Runtime-протокол

### 4.1. Выполненные тесты

| Тест | Результат | Артефакт |
|---|---|---|
| `gradlew build` | {'PASS' if build_pass else 'FAIL / NOT OBTAINED'} | {f'`{jar}`' if build_pass else f'`reports/TC4_{version}_LOCAL_GRADLE_BUILD_ATTEMPT.log`'} |
| Клиент | {'PASS' if manifest_pass(manifest, 'client') else 'NOT TESTED'} | runtime manifest |
| Dedicated server | {'PASS' if manifest_pass(manifest, 'dedicated_server') else 'NOT TESTED'} | runtime manifest |
| P0 visuals | {sum(p0_runtime.values())} / 7 PASS | runtime manifest + screenshots |
| Миграция | {'PASS' if criteria['world_migration_verified'] else 'NOT TESTED'} | runtime manifest |
| JEI | {'PASS' if criteria['jei_verified'] else 'NOT TESTED'} | runtime manifest |

### 4.2. Блокирующие тесты

- [ ] Build/compileJava Forge 43.5.2 на Java 17 и SHA-256 JAR.
- [ ] Чистый клиент и dedicated server.
- [ ] 201 исследований/591 страниц на нескольких GUI Scale.
- [ ] 86 normal, 49 label/cleaning и все arcane/alchemy/infusion рецепты.
- [ ] P0 в GUI/ground/fixed/first-person/third-person/world.
- [ ] 48 аспектов в filled/filtered/labeled jars.
- [ ] Essentia reservoir 24-suction/six-face active pull, buffer conflict/rollback/soak and centrifuge redstone/output queue.
- [ ] Mirrors и Outer Lands с save/reload/return.
- [ ] Migration 11.62.58/60/62/73/74/75/76/77.
- [ ] Golem USE-core side/empty-hand, weighted fishing and creeper-swell avoidance scenarios.
- [ ] Запуск без JEI.

---

## 5. Критерии готовности к релизу

- [{'x' if criteria['successful_build'] else ' '}] Успешная сборка — **{'PASS' if criteria['successful_build'] else 'FAIL'}**.
- [{'x' if criteria['all_p0_runtime_fixed'] else ' '}] Все P0 runtime-подтверждены — **{'PASS' if criteria['all_p0_runtime_fixed'] else 'FAIL'}**.
- [{'x' if criteria['runtime_protocol_complete'] else ' '}] Runtime-протокол завершён — **{'PASS' if criteria['runtime_protocol_complete'] else 'FAIL'}**.
- [{'x' if criteria['world_migration_verified'] else ' '}] Миграция подтверждена — **{'PASS' if criteria['world_migration_verified'] else 'FAIL'}**.
- [{'x' if criteria['jei_verified'] else ' '}] JEI/no-JEI подтверждены — **{'PASS' if criteria['jei_verified'] else 'FAIL'}**.

**Итог: {criteria_done} из 5 критериев выполнены.**  
**Вердикт: {'РЕЛИЗ ГОТОВ' if criteria_done == 5 else 'НЕ ГОТОВ К РЕЛИЗУ'}.**

---

## 6. Следующие шаги

1. Получить CI build и compiler output. До появления JAR поле build остаётся FAIL независимо от статических guards.
2. Исправить все compiler/runtime ошибки.
3. Заполнить `screenshots/` и `runtime_artifacts/runtime_test_manifest.json` реальными файлами.
4. Не закрывать Outer Lands до полного игрового цикла.
5. Зафиксировать exact source denominators рецептов.
6. Выполнить migration matrix.
7. Повторно сгенерировать этот отчёт; субъективные проценты запрещены.
"""
    output.write_text(report, encoding="utf-8")
    json_path = REPORTS / f"tc4_port_status_v3_{version}.json"
    json_path.write_text(json.dumps(metrics, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return metrics


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.91")
    parser.add_argument("--jar", type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "TC4_PORT_STATUS_V3.md")
    args = parser.parse_args()
    jar = args.jar.resolve() if args.jar else None
    metrics = generate(args.version, jar, args.output)
    print(args.output.relative_to(ROOT) if args.output.is_relative_to(ROOT) else args.output)
    print(REPORTS / f"tc4_port_status_v3_{args.version}.json")
    print(f"release criteria: {metrics['criteria_done']}/5; build={metrics['build']['gradlew_build']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
