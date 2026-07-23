#!/usr/bin/env python3
"""Focused static CI for restored v11.63.71 and new v11.63.72 rounds.

Historical exact-version guards are intentionally not included: many encode
older package versions and are archival checks rather than forward-compatible
validators. Their exclusion is reported, not hidden.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
LOG = ROOT / "TC4_11.63.72_FOCUSED_STATIC_CI_FINAL.log"

CHECKS = [
    [sys.executable, "-m", "py_compile", "tools/tc4_116371_wand_parity_guard.py", "tools/tc4_116372_essentia_parity_guard.py"],
    [sys.executable, "tools/tc4_116371_wand_parity_guard.py"],
    [sys.executable, "tools/tc4_116372_essentia_parity_guard.py"],
    [sys.executable, "tools/validate_runtime_manifest.py", "--manifest", "runtime_artifacts/runtime_test_manifest.template.json", "--version", "11.63.72", "--template"],
    [sys.executable, "tools/validate_json_resources.py"],
    [sys.executable, "tools/tc4_recipe_registration_denominator_guard.py"],
]

def main() -> int:
    lines = [
        "TC4 v11.63.72 focused static CI",
        "Scope: restored Wand parity + new Essentia parity + stable global validators",
        "Historical exact-version guards: NOT RUN (archival version drift)",
        "",
    ]
    passed = 0
    for index, command in enumerate(CHECKS, 1):
        proc = subprocess.run(command, cwd=ROOT, text=True, capture_output=True)
        status = "PASS" if proc.returncode == 0 else "FAIL"
        lines.append(f"[{index}/{len(CHECKS)}] {status}: {' '.join(command)}")
        if proc.stdout.strip(): lines.append(proc.stdout.rstrip())
        if proc.stderr.strip(): lines.append(proc.stderr.rstrip())
        lines.append("")
        if proc.returncode == 0:
            passed += 1
    lines.append(f"RESULT: {passed}/{len(CHECKS)} PASS")
    lines.append("Gradle compile/GameTest runtime: NOT VERIFIED (wrapper download unavailable in sandbox)")
    LOG.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    return 0 if passed == len(CHECKS) else 1

if __name__ == "__main__":
    raise SystemExit(main())
