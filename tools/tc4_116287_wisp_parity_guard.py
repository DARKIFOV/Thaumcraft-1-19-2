#!/usr/bin/env python3
"""Static regression guard for the TC4 4.2.3.5 Wisp port retained in v11.62.87+."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
checks = 0

def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        errors.append(f"missing {rel}")
        return ""
    return path.read_text(encoding="utf-8", errors="ignore")

def need(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token not in read(rel):
        errors.append(f"{rel}: missing {token!r}")

entity = "src/main/java/com/darkifov/thaumcraft/entity/TC4WispEntity.java"
renderer = "src/main/java/com/darkifov/thaumcraft/client/render/TC4WispRenderer.java"
item = "src/main/java/com/darkifov/thaumcraft/item/WispEssenceItem.java"
registry = "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"
aspects = "src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java"

for token in (
    ".add(Attributes.MAX_HEALTH, 22.0D)",
    ".add(Attributes.ATTACK_DAMAGE, 3.0D)",
    "xpReward = 5;",
    "tag.putString(\"Type\", aspect.id())",
    "random.nextInt(10) != 0",
    "* 16.0D",
    "courseChangeCooldown += random.nextInt(5) + 2",
    "float damage = random.nextInt(4) == 0 ? 4.0F : 3.0F",
    "getMaxSpawnClusterSize() { return 1; }",
    "nearby < 8",
):
    need(entity, token)
need(renderer, '"textures/misc/wisp.png"')
need(renderer, "RenderType.entityTranslucentEmissive")
need(item, 'putString(TAG_ASPECT, aspect.id())')
need(registry, 'ENTITY_TYPES.register("wisp"')
need(registry, ".sized(0.9F, 0.9F)")
need(aspects, "result.add(wisp.getAspect(), 2);")
need("src/main/resources/data/thaumcraft/forge/biome_modifier/add_wisps.json", '"weight": 5')
need(".github/workflows/build.yml", "Validate retained v11.62.87 Wisp parity")
need(".github/workflows/release.yml", "Validate retained v11.62.87 Wisp parity")

if errors:
    print(f"TC4 11.62.87 Wisp parity guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.87 Wisp parity guard: PASS ({checks}/{checks} checks)")
