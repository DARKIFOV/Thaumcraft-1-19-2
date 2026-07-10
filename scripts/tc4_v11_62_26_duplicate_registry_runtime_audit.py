#!/usr/bin/env python3
"""Regression guard for the v11.62.26 Forge duplicate-registry runtime hotfix."""
from __future__ import annotations

from collections import Counter, defaultdict
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def read(rel: str) -> str:
    path = ROOT / rel
    if not path.exists():
        errors.append(f"missing {rel}")
        return ""
    return path.read_text(encoding="utf-8", errors="replace")


def require(rel: str, *tokens: str) -> str:
    text = read(rel)
    for token in tokens:
        if token not in text:
            errors.append(f"{rel}: missing {token}")
    return text


build = require("build.gradle", "version = '11.62.26'")
mods = require("src/main/resources/META-INF/mods.toml", 'version="11.62.26"')
workflow = require(
    ".github/workflows/main.yml",
    "tc4_v11_62_26_duplicate_registry_runtime_audit.py",
    "v11.62.26-github-jar",
    "v11.62.26-build-reports",
)
main = require(
    "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java",
    'specialItem("tc4_crystalessence"',
    'Map.of("tc4_crystalessence", ESSENTIA_CRYSTAL)',
)
research = require(
    "src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java",
    "Map<String, RegistryObject<Item>> preRegistered",
    "if (preRegistered != null) out.putAll(preRegistered);",
    "if (out.containsKey(entry.id())) continue;",
    'if (entry.id().equals("tc4_block_focal_manipulator")) continue;',
)

# Parse only the declared legacy entry array, not unrelated e(...) text later.
entries_match = re.search(
    r"private static final Entry\[\] ENTRIES\s*=\s*new Entry\[\]\s*\{(.*?)\n\s*\};",
    research,
    re.S,
)
entry_ids: list[str] = []
if not entries_match:
    errors.append("TC4ResearchItems.java: could not parse ENTRIES")
else:
    entry_ids = re.findall(r'\be\(\s*"([^"]+)"\s*,', entries_match.group(1))
    duplicates = sorted(k for k, n in Counter(entry_ids).items() if n > 1)
    if duplicates:
        errors.append("duplicate ids inside ENTRIES: " + ", ".join(duplicates))

# Discover helper methods that register items, including helpers that delegate to
# another item-registering helper (for example crystalBlock -> block).
method_pattern = re.compile(r"private\s+static\s+[^;={]+?\s+(\w+)\s*\([^)]*\)\s*\{")
method_bodies: dict[str, list[str]] = defaultdict(list)
for match in method_pattern.finditer(main):
    name = match.group(1)
    start = main.find("{", match.start())
    depth = 0
    end = None
    for index in range(start, len(main)):
        char = main[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                end = index + 1
                break
    if end is not None:
        method_bodies[name].append(main[start:end])

item_helpers = {
    name
    for name, bodies in method_bodies.items()
    if any("ITEMS.register" in body for body in bodies)
}
changed = True
while changed:
    changed = False
    for name, bodies in method_bodies.items():
        if name in item_helpers:
            continue
        if any(
            re.search(r"\b" + re.escape(helper) + r"\s*\(", body)
            for helper in item_helpers
            for body in bodies
        ):
            item_helpers.add(name)
            changed = True
            break

registered_ids: dict[str, list[str]] = defaultdict(list)
for helper in sorted(item_helpers):
    for match in re.finditer(r"\b" + re.escape(helper) + r'\s*\(\s*"([^"]+)"', main):
        line = main.count("\n", 0, match.start()) + 1
        registered_ids[match.group(1)].append(f"{helper}@{line}")
for match in re.finditer(r'ITEMS\.register\s*\(\s*"([^"]+)"', main):
    line = main.count("\n", 0, match.start()) + 1
    registered_ids[match.group(1)].append(f"ITEMS.register@{line}")

actual_overlaps = sorted(set(entry_ids) & set(registered_ids))
expected_overlaps = ["tc4_block_focal_manipulator", "tc4_crystalessence"]
if actual_overlaps != expected_overlaps:
    errors.append(
        "unexpected TC4ResearchItems/main item registry overlap set: "
        f"expected {expected_overlaps}, got {actual_overlaps}"
    )

# The crash was exactly Forge rejecting this duplicate during static init.
if 'items.register("tc4_crystalessence"' in research:
    errors.append("TC4ResearchItems directly registers tc4_crystalessence")
if main.count('specialItem("tc4_crystalessence"') != 1:
    errors.append("tc4_crystalessence functional item must be registered exactly once")

if errors:
    print("TC4 v11.62.26 duplicate registry runtime audit failed:")
    for error in errors:
        print(" -", error)
    sys.exit(1)

print("TC4 v11.62.26 duplicate registry runtime audit: OK")
print("Handled item registry overlaps:")
for registry_id in actual_overlaps:
    print(f" - {registry_id}: {', '.join(registered_ids[registry_id])}")
