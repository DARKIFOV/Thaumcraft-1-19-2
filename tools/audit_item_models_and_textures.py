#!/usr/bin/env python3
import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/thaumcraft"
MODELS = ROOT / "models/item"
OUT = Path(__file__).resolve().parents[1] / "reports/item_texture_model_audit_hotfix4.json"

problem_parents = {
    "minecraft:builtin/entity": "bewlr_or_custom_renderer",
    "minecraft:item/generated": "flat_item_icon",
    "minecraft:item/handheld": "flat_handheld_icon"
}

def collect_textures(model_path: Path, data: dict):
    textures = []
    for v in data.get("textures", {}).values():
        if isinstance(v, str) and not v.startswith('#'):
            textures.append(v)
    return textures

def exists_tex(location: str) -> bool:
    if ':' in location:
        ns, rel = location.split(':', 1)
    else:
        ns, rel = 'thaumcraft', location
    asset_root = ROOT.parent / ns / 'textures' if ns != 'thaumcraft' else ROOT / 'textures'
    return (asset_root / (rel + '.png')).exists()

rows = []
for path in sorted(MODELS.glob('*.json')):
    try:
        data = json.loads(path.read_text())
    except Exception as exc:
        rows.append({"model": path.stem, "status": "invalid_json", "error": str(exc)})
        continue
    parent = data.get('parent', '')
    textures = collect_textures(path, data)
    missing = [tex for tex in textures if not exists_tex(tex)]
    row = {
        "model": path.stem,
        "parent": parent,
        "textures": textures,
        "missing_textures": missing,
        "status": "ok"
    }
    if parent in problem_parents:
        row["render_class"] = problem_parents[parent]
    if missing:
        row["status"] = "missing_textures"
    rows.append(row)

summary = {
    "total_models": len(rows),
    "missing_texture_models": sum(1 for r in rows if r['status'] == 'missing_textures'),
    "bewlr_models": sum(1 for r in rows if r.get('render_class') == 'bewlr_or_custom_renderer'),
    "flat_generated_models": sum(1 for r in rows if r.get('render_class') in ('flat_item_icon','flat_handheld_icon')),
    "rows": rows,
}
OUT.write_text(json.dumps(summary, ensure_ascii=False, indent=2))
print(OUT)
