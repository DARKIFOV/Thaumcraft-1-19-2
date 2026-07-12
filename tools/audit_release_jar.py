#!/usr/bin/env python3
"""Verify that a patched production JAR contains the complete current resource tree.

This does not replace ForgeGradle/reobfJar, but it prevents the principal risk of
incremental JAR patching: omitted or stale assets/data/metadata.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_ROOT = ROOT / "src/main/resources"
SOURCE_ROOT = ROOT / "src/main/java"


def normalized_equal(name: str, expected: bytes, actual: bytes) -> bool:
    if expected == actual:
        return True
    if name.endswith((".json", ".mcmeta")):
        try:
            return json.loads(expected) == json.loads(actual)
        except (UnicodeDecodeError, json.JSONDecodeError):
            return False
    if name == "META-INF/accesstransformer.cfg":
        return expected.replace(b"\r\n", b"\n") == actual.replace(b"\r\n", b"\n")
    return False


def primary_class_entries() -> list[str]:
    entries: list[str] = []
    for source in SOURCE_ROOT.rglob("*.java"):
        text = source.read_text(encoding="utf-8")
        package_match = re.search(r"^package\s+([\w.]+)\s*;", text, re.MULTILINE)
        type_match = re.search(r"\bpublic\s+(?:final\s+|abstract\s+|sealed\s+)?(?:class|interface|enum|record)\s+(\w+)", text)
        if not package_match or not type_match:
            continue
        entries.append(package_match.group(1).replace(".", "/") + "/" + type_match.group(1) + ".class")
    return sorted(set(entries))


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", type=Path, required=True)
    ap.add_argument("--version", required=True)
    ap.add_argument("--report", type=Path)
    args = ap.parse_args()

    errors: list[str] = []
    resource_files = [p for p in RESOURCE_ROOT.rglob("*") if p.is_file()]
    with zipfile.ZipFile(args.jar) as jar:
        names = set(jar.namelist())
        mismatched: list[str] = []
        for path in resource_files:
            name = path.relative_to(RESOURCE_ROOT).as_posix()
            if name not in names:
                errors.append(f"missing resource: {name}")
                continue
            if not normalized_equal(name, path.read_bytes(), jar.read(name)):
                mismatched.append(name)
        if mismatched:
            errors.extend(f"resource differs: {name}" for name in mismatched)

        for entry in primary_class_entries():
            if entry not in names:
                errors.append(f"missing primary class: {entry}")

        try:
            mods = jar.read("META-INF/mods.toml").decode("utf-8")
            manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
        except KeyError as exc:
            errors.append(f"missing metadata: {exc}")
            mods = manifest = ""
        if f'version="{args.version}"' not in mods:
            errors.append("mods.toml version mismatch")
        if f"Implementation-Version: {args.version}" not in manifest:
            errors.append("manifest version mismatch")

        result = {
            "version": args.version,
            "jar": args.jar.name,
            "jar_sha256": hashlib.sha256(args.jar.read_bytes()).hexdigest(),
            "resource_files_checked": len(resource_files),
            "primary_classes_checked": len(primary_class_entries()),
            "missing_or_mismatched": len(errors),
            "status": "OK" if not errors else "FAIL",
            "errors": errors,
        }

    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if errors:
        print("Release JAR audit: FAIL")
        for error in errors:
            print(" -", error)
        return 1
    print(f"Release JAR audit: OK ({result['resource_files_checked']} resources, {result['primary_classes_checked']} primary classes, SHA-256 {result['jar_sha256'][:16]}…)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
