#!/usr/bin/env python3
"""Audit every Thaumcraft item model and its texture chain.

This is intentionally broader than the registry quarantine audit: it scans all
item model JSON files, including compatibility aliases, so a broken or missing
texture cannot hide merely because the item is not in the creative tab.
"""
from __future__ import annotations
from pathlib import Path
import argparse
import hashlib
import json
import re
from collections import Counter, defaultdict

root = Path(__file__).resolve().parents[1]
assets = root / "src/main/resources/assets"
models_root = assets / "thaumcraft/models/item"
manifest_file = root / "src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_1710_asset_inventory.json"

parser = argparse.ArgumentParser()
parser.add_argument("--version", default="11.62.54")
parser.add_argument("--fail-on-missing", action="store_true")
args = parser.parse_args()

manifest = json.loads(manifest_file.read_text(encoding="utf-8"))
original_texture_sha1 = {e["sha1"] for e in manifest if e.get("path", "").startswith("textures/")}
original_by_rel = {e["path"][len("textures/"):]: e["sha1"] for e in manifest if e.get("path", "").startswith("textures/")}

all_models: dict[str, dict] = {}
parse_errors = []
for path in sorted(models_root.rglob("*.json")):
    rel = path.relative_to(models_root).with_suffix("").as_posix()
    try:
        all_models[rel] = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        parse_errors.append({"model": rel, "error": str(exc)})


def canonical_parent(parent: str | None) -> str | None:
    if not parent:
        return None
    return parent if ":" in parent else f"minecraft:{parent}"


def parent_key(parent: str | None) -> str | None:
    if not parent or canonical_parent(parent) == "minecraft:builtin/entity":
        return None
    ns, sep, path = parent.partition(":")
    if not sep:
        ns, path = "minecraft", ns
    if ns != "thaumcraft" or not path.startswith("item/"):
        return None
    return path[len("item/"):]


def inherited_textures(model: str, seen=None) -> dict[str, str]:
    seen = set() if seen is None else seen
    if model in seen or model not in all_models:
        return {}
    seen.add(model)
    data = all_models[model]
    out = {}
    pk = parent_key(data.get("parent"))
    if pk:
        out.update(inherited_textures(pk, seen))
    out.update({str(k): str(v) for k, v in (data.get("textures") or {}).items()})
    return out


def resolve_token(value: str, textures: dict[str, str]) -> str | None:
    seen = set()
    while value.startswith("#"):
        key = value[1:]
        if key in seen or key not in textures:
            return None
        seen.add(key)
        value = textures[key]
    return value


def texture_path(location: str) -> tuple[Path | None, str | None, str | None]:
    ns, sep, path = location.partition(":")
    if not sep:
        ns, path = "minecraft", ns
    if ns != "thaumcraft":
        return None, ns, path
    rel = f"{path}.png"
    return assets / ns / "textures" / rel, ns, rel

placeholder_rx = re.compile(r"(?:placeholder|debug|missing|blank|template|dummy|test)(?:[_./-]|$)", re.I)
KNOWN_PLACEHOLDER_CLASSIFICATION = {
    "item/aura_node_debug.png": "quarantined_debug_item_for_non_item_node_block",
    "item/tc4/golem_core_blank.png": "canonical_tc4_blank_golem_core_component",
}
placeholder_classification = []
records = []
missing = []
unresolved = []
all_texture_refs = []
for model, data in sorted(all_models.items()):
    textures = inherited_textures(model)
    refs = []
    for key, raw in sorted(textures.items()):
        resolved = resolve_token(raw, textures)
        if not resolved:
            unresolved.append({"model": model, "slot": key, "value": raw})
            continue
        path, ns, rel = texture_path(resolved)
        rec = {"slot": key, "location": resolved, "namespace": ns, "relative_texture": rel}
        if path is None:
            rec.update({"external": True, "exists": None, "sha1": None, "original_exact": None, "original_same_path": None})
        else:
            exists = path.is_file()
            digest = hashlib.sha1(path.read_bytes()).hexdigest() if exists else None
            placeholder_name = bool(placeholder_rx.search(rel or ""))
            classification = KNOWN_PLACEHOLDER_CLASSIFICATION.get(rel or "") if placeholder_name else None
            rec.update({
                "external": False,
                "exists": exists,
                "sha1": digest,
                "original_exact": digest in original_texture_sha1 if digest else False,
                "original_same_path": bool(digest and original_by_rel.get(rel) == digest),
                "placeholder_name": placeholder_name,
                "placeholder_classification": classification,
            })
            if placeholder_name:
                placeholder_classification.append({
                    "model": model,
                    "texture": rel,
                    "classification": classification or "unclassified_suspicious_name",
                })
            if not exists:
                missing.append({"model": model, "slot": key, "location": resolved, "expected": str(path.relative_to(root))})
            all_texture_refs.append((model, key, resolved, digest))
        refs.append(rec)
    records.append({
        "model": model,
        "parent": data.get("parent", ""),
        "builtin_entity": canonical_parent(data.get("parent")) == "minecraft:builtin/entity",
        "texture_references": refs,
    })

hash_groups = defaultdict(list)
for model, key, loc, digest in all_texture_refs:
    if digest:
        hash_groups[digest].append({"model": model, "slot": key, "location": loc})
duplicate_hash_groups = [v for v in hash_groups.values() if len({x['location'] for x in v}) > 1]

stats = {
    "version": args.version,
    "item_models": len(all_models),
    "parse_errors": len(parse_errors),
    "builtin_entity_models": sum(r["builtin_entity"] for r in records),
    "texture_references_total": sum(len(r["texture_references"]) for r in records),
    "thaumcraft_texture_references": sum(1 for r in records for t in r["texture_references"] if not t.get("external")),
    "external_texture_references": sum(1 for r in records for t in r["texture_references"] if t.get("external")),
    "missing_texture_references": len(missing),
    "unresolved_texture_tokens": len(unresolved),
    "exact_original_texture_references": sum(1 for r in records for t in r["texture_references"] if t.get("original_exact") is True),
    "same_path_original_texture_references": sum(1 for r in records for t in r["texture_references"] if t.get("original_same_path") is True),
    "custom_or_adapted_texture_references": sum(1 for r in records for t in r["texture_references"] if not t.get("external") and t.get("exists") and not t.get("original_exact")),
    "placeholder_named_references": sum(1 for r in records for t in r["texture_references"] if t.get("placeholder_name")),
    "classified_placeholder_named_references": sum(1 for x in placeholder_classification if x["classification"] != "unclassified_suspicious_name"),
    "suspicious_placeholder_named_references": sum(1 for x in placeholder_classification if x["classification"] == "unclassified_suspicious_name"),
    "duplicate_texture_hash_groups": len(duplicate_hash_groups),
    "canonical_original_texture_files": len(original_by_rel),
}

payload = {
    "stats": stats,
    "parse_errors": parse_errors,
    "missing": missing,
    "unresolved": unresolved,
    "models": records,
    "duplicate_hash_groups": duplicate_hash_groups,
    "placeholder_classification": placeholder_classification,
}
reports = root / "reports"
reports.mkdir(exist_ok=True)
json_path = reports / f"item_visual_audit_v{args.version}.json"
md_path = reports / f"ITEM_VISUAL_AUDIT_V{args.version.replace('.', '_')}.md"
json_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

lines = [
    f"# Item visual audit v{args.version}", "",
    "Проверены все JSON-модели предметов в проекте, включая скрытые legacy-ID и совместимые алиасы.", "",
    "## Итоги", "",
]
labels = [
    ("Моделей предметов", "item_models"),
    ("Динамических `builtin/entity` моделей", "builtin_entity_models"),
    ("Всего ссылок на текстуры", "texture_references_total"),
    ("Ссылок на текстуры Thaumcraft", "thaumcraft_texture_references"),
    ("Ссылок на внешние Minecraft/моды", "external_texture_references"),
    ("Точных ссылок на оригинальные байты TC4", "exact_original_texture_references"),
    ("Оригинальных по тому же пути", "same_path_original_texture_references"),
    ("Адаптированных/новых текстур", "custom_or_adapted_texture_references"),
    ("Отсутствующих текстур", "missing_texture_references"),
    ("Неразрешённых `#texture` ссылок", "unresolved_texture_tokens"),
    ("Ссылок с placeholder/debug именами (всего)", "placeholder_named_references"),
    ("Из них классифицировано как допустимые", "classified_placeholder_named_references"),
    ("Неклассифицированных подозрительных имён", "suspicious_placeholder_named_references"),
    ("Групп одинаковых файлов под разными путями", "duplicate_texture_hash_groups"),
    ("Файлов в каноническом банке TC4", "canonical_original_texture_files"),
]
lines += [f"- **{label}:** {stats[key]}" for label, key in labels]
if missing:
    lines += ["", "## Отсутствующие текстуры", ""] + [f"- `{x['model']}` → `{x['location']}`" for x in missing[:100]]
if unresolved:
    lines += ["", "## Неразрешённые ссылки", ""] + [f"- `{x['model']}` `{x['slot']}` → `{x['value']}`" for x in unresolved[:100]]
if placeholder_classification:
    lines += ["", "## Placeholder/debug классификация", ""]
    for item in placeholder_classification:
        lines.append(f"- `{item['model']}` → `{item['texture']}` — `{item['classification']}`")
lines += ["", "## Интерпретация", "",
          "Точное совпадение PNG подтверждает только ресурс. Оно не доказывает, что модель, UV, масштаб, blending или механика предмета уже перенесены правильно.",
          "`builtin/entity` предметы проверяются дополнительно отдельными runtime-guards, поскольку их внешний вид задаётся Java-рендерером, а не JSON-моделью."]
md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"Item visual audit: {len(all_models)} models, {len(missing)} missing, {len(unresolved)} unresolved, {stats['exact_original_texture_references']} exact-original refs")
print(json_path.relative_to(root))
print(md_path.relative_to(root))
if args.fail_on_missing and (parse_errors or missing or unresolved):
    raise SystemExit(1)
