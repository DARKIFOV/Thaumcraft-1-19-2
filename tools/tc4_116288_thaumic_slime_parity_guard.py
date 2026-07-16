#!/usr/bin/env python3
"""Static parity guard for the TC4 4.2.3.5 Thaumic Slime port in v11.62.91."""
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

def forbid(rel: str, token: str) -> None:
    global checks
    checks += 1
    if token in read(rel):
        errors.append(f"{rel}: forbidden {token!r}")

entity = "src/main/java/com/darkifov/thaumcraft/entity/TC4ThaumicSlimeEntity.java"
renderer = "src/main/java/com/darkifov/thaumcraft/client/render/TC4ThaumicSlimeRenderer.java"
registry = "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java"
client = "src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java"
aspects = "src/main/java/com/darkifov/thaumcraft/source/TC4EntityAspectRegistry.java"

need("build.gradle", "version = '11.62.91'")
need("src/main/resources/META-INF/mods.toml", 'version="11.62.91"')
for token in (
    "private static final int MAX_TC4_SIZE = 100;",
    "setTc4Size(1 << random.nextInt(3), true);",
    "public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,",
    "distanceTo(player) > 4.0F && spitCounter <= 0 && getTc4Size() > 3",
    "spitCounter = 101;",
    "setTc4Size(getTc4Size() - 1, true);",
    "slime.tickCount > 100 && slime.getTc4Size() < MAX_TC4_SIZE",
    "Math.min(MAX_TC4_SIZE, target.getTc4Size() + getTc4Size())",
    "int children = (int) Math.sqrt(oldSize);",
    "children > 1 && isDeadOrDying()",
    "child.setTc4Size(1, true);",
    "double reach = 0.8D * reachScale * 0.8D * reachScale;",
    "distanceTo(player) < reach",
    "player.hurt(DamageSource.mobAttack(this), getTc4Size())",
    'tag.putInt("Size", getTc4Size() - 1);',
    "getTc4Size() < 3 && random.nextInt(3) == 0",
    "getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.20D);",
    "return false;",
):
    need(entity, token)
need(registry, 'ENTITY_TYPES.register("thaumic_slime"')
need(registry, 'ITEMS.register("thaumic_slime_spawn_egg"')
need(registry, '.clientTrackingRange(64)')
need(registry, 'ForgeSpawnEggItem(THAUMIC_SLIME, 0xFFC0FF, 0xFF80FF')
need(client, "new TC4ThaumicSlimeRenderer(context)")
need(renderer, '"textures/models/tslime.png"')
need(renderer, "0.6F * (float) Math.sqrt")
need(aspects, 'exact("thaumcraft:thaumic_slime", aspects(Aspect.LIMUS, 2, Aspect.AQUA, 2, Aspect.PRAECANTATIO, 1))')
need("src/main/resources/assets/thaumcraft/models/item/thaumic_slime_spawn_egg.json", '"parent": "minecraft:item/template_spawn_egg"')
need(".github/workflows/build.yml", "Validate v11.62.91 Thaumic Slime parity")
need(".github/workflows/release.yml", "Validate v11.62.91 Thaumic Slime parity")
forbid(entity, "Math.min(16")

if errors:
    print(f"TC4 11.62.91 Thaumic Slime parity guard: FAIL ({len(errors)} problems; {checks} checks)")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"TC4 11.62.91 Thaumic Slime parity guard: PASS ({checks}/{checks} checks)")
