#!/usr/bin/env python3
"""Regression guard for CI run 79587666588 compile blockers fixed in v11.62.88."""
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]
errors=[]
checks=0
def read(rel):
    p=ROOT/rel
    if not p.is_file():
        errors.append(f"missing {rel}")
        return ""
    return p.read_text(encoding="utf-8", errors="ignore")
def need(rel, token):
    global checks
    checks += 1
    if token not in read(rel): errors.append(f"{rel}: missing {token!r}")
def forbid(rel, token):
    global checks
    checks += 1
    if token in read(rel): errors.append(f"{rel}: forbidden {token!r}")
need("build.gradle", "version = '11.62.88'")
need("src/main/resources/META-INF/mods.toml", 'version="11.62.88"')
trunk="src/main/java/com/darkifov/thaumcraft/entity/TravelingTrunkEntity.java"
need(trunk, "import net.minecraft.world.entity.TamableAnimal;")
need(trunk, "extends TamableAnimal")
need(trunk, "TamableAnimal.createMobAttributes()")
forbid(trunk, "net.minecraft.world.entity.animal.TamableAnimal")
runtime="src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java"
spore="src/main/java/com/darkifov/thaumcraft/entity/TaintSporeEntity.java"
need(runtime, "public static boolean isColumnTainted(ServerLevel level, BlockPos pos)")
need(runtime, "return isColumnTainted(level, pos);")
need(spore, "TaintSpreadRuntime.isColumnTainted(server, blockPosition())")
need("tools/forge_1192_compile_api_guard.py", "net.minecraft.world.entity.animal.TamableAnimal")
need(".github/workflows/build.yml", "Validate retained v11.62.83 Forge compiler hotfix")
need(".github/workflows/release.yml", "Validate retained v11.62.83 Forge compiler hotfix")
if errors:
    print(f"TC4 11.62.88 Forge compile hotfix guard: FAIL ({len(errors)} problems; {checks} checks)")
    for e in errors: print(" -",e)
    sys.exit(1)
print(f"TC4 11.62.88 Forge compile hotfix guard: PASS ({checks} checks)")
