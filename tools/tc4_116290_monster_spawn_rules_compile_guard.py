from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
WISP = ROOT / "src/main/java/com/darkifov/thaumcraft/entity/TC4WispEntity.java"
FORGE_GUARD = ROOT / "tools/forge_1192_compile_api_guard.py"
errors: list[str] = []

wisp = WISP.read_text(encoding="utf-8")
forge_guard = FORGE_GUARD.read_text(encoding="utf-8")

wrong = "Monster.checkMonsterSpawnRules(level, pos, random)"
incompatible = "Monster.checkMonsterSpawnRules(type, level, reason, pos, random)"

if wrong in wisp:
    errors.append("TC4WispEntity.java: unavailable three-argument Monster.checkMonsterSpawnRules call remains")
if incompatible in wisp:
    errors.append("TC4WispEntity.java: FlyingMob EntityType cannot be passed to Monster.checkMonsterSpawnRules")
for token in (
    "EntityType<TC4WispEntity> type",
    "ServerLevelAccessor level",
    "MobSpawnType reason",
    "BlockPos pos",
    "RandomSource random",
):
    if token not in wisp:
        errors.append(f"TC4WispEntity.java: spawn predicate lost parameter {token}")
if "Mob.checkMobSpawnRules(type, level, reason, pos, random)" not in wisp:
    errors.append("TC4WispEntity.java: retained generic mob spawn check is missing")
if "new AABB(pos).inflate(16.0D)" not in wisp or "return nearby < 8;" not in wisp:
    errors.append("TC4WispEntity.java: retained TC4 nearby-Wisp density cap is missing")
if wrong not in forge_guard:
    errors.append("forge_1192_compile_api_guard.py: early regression check for the unavailable overload is missing")

if errors:
    for error in errors:
        print("::error::" + error)
    sys.exit(1)

print("TC4 11.63.10 retained Monster spawn-rules compile hotfix guard: 10/10 PASS")
