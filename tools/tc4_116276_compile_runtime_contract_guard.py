#!/usr/bin/env python3
"""Regression guard for v11.62.83 compile-risk and runtime evidence contracts."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
checks = 0


def text(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        errors.append(f"missing {rel}")
        return ""
    return path.read_text(encoding="utf-8", errors="ignore")


def need(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token not in text(rel):
        errors.append(f"{rel}: missing {token!r}")


def forbid(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token in text(rel):
        errors.append(f"{rel}: forbidden {token!r}")

need("build.gradle", "version = '11.62.83'")
need("src/main/resources/META-INF/mods.toml", 'version="11.62.83"')

trunk = "src/main/java/com/darkifov/thaumcraft/entity/TravelingTrunkEntity.java"
for token in (
    "isOnGround()", "ForgeCapabilities.ITEM_HANDLER", "new InvWrapper(this)",
    "public <T> LazyOptional<T> getCapability", "itemHandler.invalidate()",
    "entityData.set(OPEN, tag.getBoolean(\"Open\"))",
    "for (int slot = 0; slot < items.size(); slot++)",
):
    need(trunk, token)
forbid(trunk, "onGround()")
need("src/main/java/com/darkifov/thaumcraft/client/render/TravelingTrunkRenderer.java", "entity.isOnGround()")
forbid("src/main/java/com/darkifov/thaumcraft/client/render/TravelingTrunkRenderer.java", "entity.onGround()")

outer = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLivePopulateAdapter.java"
for token in ("level.hasChunkAt(chunkProbe)", "data.isChunkPopulated(chunkX, chunkZ)", "populateLikeTC4", "data.markChunkPopulated"):
    need(outer, token)
out = text(outer)
checks += 1
if out.find("populateLikeTC4") > out.find("data.markChunkPopulated"):
    errors.append("Outer Lands must mark a chunk only after populateLikeTC4 returns")

for rel, tokens in {
    "tools/validate_runtime_manifest.py": ("SHA-256 mismatch", "PASS", "PARTIAL", "FAIL", "NOT_TESTED"),
    "tools/compare_visual_artifacts.py": ("global_ssim_luma", "Diagnostic only", "human side-by-side review"),
    "runtime_artifacts/runtime_test_manifest.template.json": ('"version": "11.62.83"', '"outer_lands"', '"tests"'),
    ".github/workflows/build.yml": ("Validate runtime evidence manifest template", "11.62.83", "gradle-build.log", "continue-on-error: true", "Fail job after preserving compiler log"),
    ".github/workflows/release.yml": ("Validate runtime evidence manifest template", "11.62.83", "gradle-build.log", "continue-on-error: true", "Fail job after preserving compiler log"),
}.items():
    for token in tokens:
        need(rel, token)

if errors:
    print(f"TC4 11.62.83 compile/runtime contract guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.83 compile/runtime contract guard: PASS ({checks} checks)")
