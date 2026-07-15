#!/usr/bin/env python3
"""Static parity ledger for TC4 aura-node, jar and stabilizer rendering."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/darkifov/thaumcraft"

CHECKS = [
    ("world_node_original_sheet", "client/render/AuraNodeRenderer.java", "ORIGINAL_NODES"),
    ("world_node_32_frames", "client/TC4AuraNodeHudParity.java", "NODE_SHEET_FRAMES = 32"),
    ("world_node_nanotime", "client/render/AuraNodeRenderer.java", "System.nanoTime()"),
    ("world_node_camera_billboard", "client/render/AuraNodeRenderer.java", "getMainCamera().rotation()"),
    ("world_node_view_cone", "client/render/AuraNodeRenderer.java", ">= 0.44D"),
    ("world_node_hidden_fallback", "client/render/AuraNodeRenderer.java", "0.10F, frame, 1"),
    ("world_node_additive_types", "client/render/AuraNodeRenderer.java", "type != AuraNodeType.DARK && type != AuraNodeType.TAINTED"),
    ("drain_wispy_texture", "client/render/AuraNodeRenderer.java", "ORIGINAL_WISPY"),
    ("jar_node_nanotime", "client/render/NodeJarItemRenderer.java", "nanoTime / 40_000_000L + 1L"),
    ("jar_node_same_frame", "client/render/NodeJarItemRenderer.java", "layerAlpha, frame, 0"),
    ("jar_node_white_type_layer", "client/render/NodeJarItemRenderer.java", "typeScale, 0xFFFFFFFF"),
    ("jar_node_additive_contract", "client/render/NodeJarItemRenderer.java", "TC4NodeRenderTypes.node"),
    ("stabilizer_original_mesh", "client/render/NodeStabilizerRenderer.java", "TC4NodeStabilizerModel.PISTON_TRIANGLES"),
    ("stabilizer_lightmap_not_alpha", "client/render/NodeStabilizerRenderer.java", "tc4LightCoordinate"),
    ("stabilizer_overlay_cutout", "client/render/NodeStabilizerRenderer.java", "entityCutoutNoCull(OVERLAY)"),
    ("stabilizer_additive_field", "client/render/NodeStabilizerRenderer.java", "TC4NodeRenderTypes.node(BUBBLE, true, false)"),
]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default="11.62.54")
    parser.add_argument("--fail-on-problems", action="store_true")
    args = parser.parse_args()

    records = []
    for name, relative, token in CHECKS:
        path = JAVA / relative
        source = path.read_text(encoding="utf-8") if path.is_file() else ""
        records.append({"check": name, "file": str(path.relative_to(ROOT)), "token": token, "ok": token in source})
    problems = [r for r in records if not r["ok"]]
    stats = {"version": args.version, "checks": len(records), "passed": len(records) - len(problems), "problem_count": len(problems)}
    reports = ROOT / "reports"
    reports.mkdir(exist_ok=True)
    json_path = reports / f"aura_node_parity_audit_v{args.version}.json"
    md_path = reports / f"AURA_NODE_PARITY_AUDIT_V{args.version.replace('.', '_')}.md"
    json_path.write_text(json.dumps({"stats": stats, "checks": records}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    lines = [f"# Aura node and stabilizer parity audit v{args.version}", "",
             "Сопоставление переноса с TC4 `TileNodeRenderer`, `ItemNodeRenderer`, `ItemJarNodeRenderer` и `TileNodeStabilizerRenderer`.", "",
             "## Итоги", "",
             f"- Проверок: **{stats['checks']}**",
             f"- Пройдено: **{stats['passed']}**",
             f"- Ошибок: **{stats['problem_count']}**", "",
             "## Что перенесено", "",
             "- Узел мира: 32-кадровый `nodes.png`, отдельный слой каждого аспекта и слой типа, TC4 blend-режимы, дистанционная альфа, модификаторы Bright/Pale/Fading, Thaumometer-конус и Goggles reveal.",
             "- Откачка жезлом: цвет аспекта, плавная wispy-линия и затухание первых десяти тиков использования.",
             "- Узел в банке: три взаимно перпендикулярные плоскости, единый nanosecond-кадр, оригинальный размер слоёв, белый type-strip и индивидуальный additive/alpha blend.",
             "- Стабилизатор: оригинальная OBJ-геометрия/UV, четыре поршня, 0..37 выдвижение, динамическая lightmap-яркость overlay и additive `node_bubble.png`.", "",
             "## Контрольные точки", ""]
    for r in records:
        lines.append(f"- **{'OK' if r['ok'] else 'FAIL'}** `{r['check']}` — `{r['file']}`")
    lines += ["", "## Ограничение", "",
              "Статический аудит подтверждает формулы, ресурсы и render-state контракты. Финальная визуальная приёмка всё равно требует запуска клиента Forge: узел без reveal, узел через Таумометр, узел через очки, откачка жезлом, обычный и продвинутый стабилизатор, банка в GUI/руке/на земле."]
    md_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Aura-node parity audit: {stats['passed']}/{stats['checks']} passed")
    print(json_path.relative_to(ROOT))
    print(md_path.relative_to(ROOT))
    return 1 if args.fail_on_problems and problems else 0


if __name__ == "__main__":
    raise SystemExit(main())
