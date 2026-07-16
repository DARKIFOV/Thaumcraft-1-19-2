from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
WISP = ROOT / "src/main/java/com/darkifov/thaumcraft/entity/TC4WispEntity.java"
FORGE_GUARD = ROOT / "tools/forge_1192_compile_api_guard.py"
BUILD = ROOT / "build.gradle"
MODS = ROOT / "src/main/resources/META-INF/mods.toml"
errors: list[str] = []
checks = 0

def require(name: str, condition: bool) -> None:
    global checks
    checks += 1
    if not condition:
        errors.append(name)

wisp = WISP.read_text(encoding="utf-8")
forge_guard = FORGE_GUARD.read_text(encoding="utf-8")
build = BUILD.read_text(encoding="utf-8")
mods = MODS.read_text(encoding="utf-8")

require("build version 11.62.92", "version = '11.62.96'" in build)
require("mods version 11.62.92", 'version="11.62.96"' in mods)
require("Wisp remains a FlyingMob", "class TC4WispEntity extends FlyingMob implements Enemy" in wisp)
require("incompatible Monster EntityType call removed", "Monster.checkMonsterSpawnRules(type, level, reason, pos, random)" not in wisp)
require("peaceful difficulty gate retained", "level.getDifficulty() == Difficulty.PEACEFUL" in wisp)
require("hostile darkness rule retained", "Monster.isDarkEnoughToSpawn(level, pos, random)" in wisp)
require("generic mob placement rule retained", "Mob.checkMobSpawnRules(type, level, reason, pos, random)" in wisp)
require("nearby Wisp radius retained", "new AABB(pos).inflate(16.0D)" in wisp)
require("nearby Wisp cap retained", "return nearby < 8;" in wisp)
require("early API guard covers FlyingMob type bound", "TC4WispEntity extends FlyingMob" in forge_guard and "EntityType<? extends Monster>" in forge_guard)

if errors:
    for error in errors:
        print("::error::" + error)
    print(f"TC4 11.62.96 Wisp FlyingMob spawn-rules compile guard: FAIL ({checks-len(errors)}/{checks})")
    sys.exit(1)

print(f"TC4 11.62.96 Wisp FlyingMob spawn-rules compile guard: PASS ({checks}/{checks})")
