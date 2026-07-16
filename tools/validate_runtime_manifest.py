#!/usr/bin/env python3
"""Validate runtime evidence for the objective TC4 port report.

No PASS/PARTIAL/FAIL result is accepted without at least one existing artifact.
Artifact hashes are mandatory and are checked byte-for-byte. NOT_TESTED may not
claim evidence. The template may be validated with --template.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
ALLOWED = {"PASS", "PARTIAL", "FAIL", "NOT_TESTED", "N/A"}
P0_KEYS = {
    "essentia_jars", "aura_node_item", "bone_bow", "traveling_trunk",
    "crimson_cultists", "fortress_armor", "outer_lands",
}
SUBSYSTEM_KEYS = {
    "aspects_tags", "thaumometer", "thaumonomicon", "research_table",
    "arcane_workbench", "wands_foci", "aura_nodes_node_jar",
    "essentia_jars", "essentia_transport", "processing_devices",
    "infusion_matrix", "jei", "bone_bow", "traveling_trunk",
    "crimson_cultists", "fortress_armor", "golems", "warp_eldritch",
    "taint_eerie_forest", "outer_lands", "mirrors", "brain_jar",
    "world_migration", "dedicated_server",
}
AXES = {"G", "V", "N", "W", "C"}
REQUIRED_TOP_LEVEL = {
    "client", "dedicated_server", "runtime_protocol", "world_migration", "jei",
}


def digest(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def status_of(entry: Any) -> str:
    return str(entry.get("status", "NOT_TESTED")) if isinstance(entry, dict) else "NOT_TESTED"


def validate_artifacts(entry: dict[str, Any], location: str, template: bool, errors: list[str]) -> None:
    status = status_of(entry)
    if status not in ALLOWED:
        errors.append(f"{location}: invalid status {status!r}")
        return
    artifacts = entry.get("artifacts") or []
    if status in {"PASS", "PARTIAL", "FAIL"} and not artifacts:
        errors.append(f"{location}: {status} requires at least one artifact")
    if status in {"NOT_TESTED", "N/A"} and artifacts:
        errors.append(f"{location}: {status} must not carry runtime evidence")
    for index, artifact in enumerate(artifacts):
        where = f"{location}.artifacts[{index}]"
        if isinstance(artifact, str):
            errors.append(f"{where}: string artifacts are forbidden; use path/kind/sha256 object")
            continue
        if not isinstance(artifact, dict):
            errors.append(f"{where}: artifact must be an object")
            continue
        rel = artifact.get("path")
        kind = artifact.get("kind")
        expected = artifact.get("sha256")
        if not rel or not kind or not expected:
            errors.append(f"{where}: path, kind and sha256 are mandatory")
            continue
        if template and expected == "TEMPLATE":
            continue
        path = (ROOT / str(rel)).resolve()
        try:
            path.relative_to(ROOT.resolve())
        except ValueError:
            errors.append(f"{where}: path escapes repository: {rel}")
            continue
        if not path.is_file():
            errors.append(f"{where}: missing artifact {rel}")
            continue
        actual = digest(path)
        if actual.lower() != str(expected).lower():
            errors.append(f"{where}: SHA-256 mismatch for {rel}: {actual}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--version", default="11.62.92")
    parser.add_argument("--template", action="store_true")
    args = parser.parse_args()

    path = args.manifest if args.manifest.is_absolute() else ROOT / args.manifest
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"runtime manifest: FAIL: {exc}")
        return 1

    errors: list[str] = []
    if manifest.get("version") != args.version:
        errors.append(f"version must be {args.version}, got {manifest.get('version')!r}")
    environment = manifest.get("environment") or {}
    expected_env = {"minecraft": "1.19.2", "forge": "43.5.2", "java": "17"}
    for key, expected in expected_env.items():
        if str(environment.get(key)) != expected:
            errors.append(f"environment.{key} must be {expected}")

    for key in REQUIRED_TOP_LEVEL:
        entry = manifest.get(key)
        if not isinstance(entry, dict):
            errors.append(f"missing top-level result {key}")
        else:
            validate_artifacts(entry, key, args.template, errors)

    p0 = manifest.get("p0") or {}
    missing_p0 = P0_KEYS - set(p0)
    if missing_p0:
        errors.append("missing P0 entries: " + ", ".join(sorted(missing_p0)))
    for key in sorted(P0_KEYS & set(p0)):
        if not isinstance(p0[key], dict):
            errors.append(f"p0.{key}: must be an object")
        else:
            validate_artifacts(p0[key], f"p0.{key}", args.template, errors)

    subsystems = manifest.get("subsystems") or {}
    missing_subsystems = SUBSYSTEM_KEYS - set(subsystems)
    if missing_subsystems:
        errors.append("missing subsystem entries: " + ", ".join(sorted(missing_subsystems)))
    for subsystem in sorted(SUBSYSTEM_KEYS & set(subsystems)):
        axes = subsystems[subsystem]
        if not isinstance(axes, dict):
            errors.append(f"subsystems.{subsystem}: must be an object")
            continue
        unknown = set(axes) - AXES
        if unknown:
            errors.append(f"subsystems.{subsystem}: unknown axes {sorted(unknown)}")
        for axis, entry in axes.items():
            if not isinstance(entry, dict):
                errors.append(f"subsystems.{subsystem}.{axis}: must be an object")
            else:
                validate_artifacts(entry, f"subsystems.{subsystem}.{axis}", args.template, errors)

    tests = manifest.get("tests") or []
    if not isinstance(tests, list) or not tests:
        errors.append("tests must be a non-empty list")
    else:
        ids: set[str] = set()
        for index, test in enumerate(tests):
            location = f"tests[{index}]"
            if not isinstance(test, dict):
                errors.append(f"{location}: must be an object")
                continue
            test_id = str(test.get("id", ""))
            if not test_id:
                errors.append(f"{location}: id is mandatory")
            elif test_id in ids:
                errors.append(f"{location}: duplicate id {test_id}")
            ids.add(test_id)
            if test.get("subsystem") not in SUBSYSTEM_KEYS:
                errors.append(f"{location}: unknown subsystem {test.get('subsystem')!r}")
            validate_artifacts(test, location, args.template, errors)

    if errors:
        print(f"runtime manifest: FAIL ({len(errors)} problems)")
        for error in errors:
            print(" -", error)
        return 1
    print(f"runtime manifest: PASS ({len(tests)} tests; template={args.template})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
