#!/usr/bin/env python3
"""Regression guard for v11.62.77 Outer Lands generation and lock/boss cycle."""
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


need("build.gradle", "version = '11.62.82'")
need("src/main/resources/META-INF/mods.toml", 'version="11.62.82"')

teleporter = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsTeleporter.java"
for token in (
    "Math.floorDiv(sourcePortal.getX(), TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE)",
    "BlockPos mazeOrigin = new BlockPos(originX, TC4OuterLandsDimensionAdapter.ORIGINAL_ROOM_Y, originZ)",
    "BlockPos generatedPortal = mazeOrigin.offset(8, 3, 8)",
    "return generatedPortal",
):
    need(teleporter, token)
forbid(teleporter, "for (int x = -2; x <= 2; x++)")
forbid(teleporter, "level.setBlock(portalPos, ThaumcraftMod.ELDRITCH_PORTAL")

gen = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java"
for token in (
    "public static final int CODE_ELDRITCH_LOCK = 16",
    "case CODE_ELDRITCH_LOCK -> ThaumcraftMod.ELDRITCH_LOCK.get().defaultBlockState()",
    "int q = Math.min(Math.abs(8 - a), Math.abs(8 - b))",
    "placePortalCornerStairs(level, origin, cell)",
    "origin.offset(8, 3, 8), ThaumcraftMod.ELDRITCH_PORTAL",
):
    need(gen, token)
forbid(gen, "CODE_CRAB_SPAWNER_B")

room = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java"
for token in (
    "TC4OuterLandsGenCommonAdapter.CODE_ELDRITCH_LOCK",
    "GenBossRoom only builds the 2x2 chamber",
):
    need(room, token)
# The cell-generation method must not instantiate a boss before lock activation.
segment = text(room).split("public static void placeBossRoomCell", 1)[-1].split("public static void placeKeyRoomCell", 1)[0]
checks += 2
if "spawnWarden(" in segment:
    errors.append("placeBossRoomCell still spawns a Warden during world generation")
if "spawnGolem(" in segment:
    errors.append("placeBossRoomCell still spawns a Golem during world generation")

bridge = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsChunkProviderBridge.java"
for token in (
    "public static boolean populateLikeTC4",
    "TC4OuterLandsMazeHandler.hasMazeCell(chunkX, chunkZ)",
    "TC4OuterLandsMazeHandler.generateForNewChunk",
):
    need(bridge, token)
forbid(bridge, "ensurePortalMaze")
forbid(bridge, "generateAround(level")

live = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLivePopulateAdapter.java"
need(live, "if (!TC4OuterLandsChunkProviderBridge.populateLikeTC4(level, chunkX, chunkZ))")

lock = "src/main/java/com/darkifov/thaumcraft/block/EldritchLockBlock.java"
need(lock, "boolean key = held.is(ThaumcraftMod.ELDRITCH_EYE.get())")
need(lock, 'TC4Sounds.event("runicShieldCharge")')
forbid(lock, "AWAKENED_CRIMSON_KEY")
forbid(lock, "CRIMSON_KEY")

spawner = "src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchLockBossSpawner.java"
for token in (
    "BossRoomContext room = locateBossRoom(lockPos)",
    "for (int dx = -2; dx <= 2; dx++)",
    "TC4OuterLandsMazeGenerator.FEATURE_BOSS_2",
    "centerX * TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE + TC4OuterLandsDimensionAdapter.ORIGINAL_CELL_SIZE",
    "bindHome(boss, home, 32)",
):
    need(spawner, token)

for workflow in (".github/workflows/build.yml", ".github/workflows/release.yml"):
    need(workflow, "Validate v11.62.77 Outer Lands generation and boss-lock cycle")
    need(workflow, "python3 tools/tc4_116277_outer_lands_cycle_guard.py")

if errors:
    print(f"TC4 11.62.77 Outer Lands cycle guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.77 Outer Lands cycle guard: PASS ({checks} checks)")
