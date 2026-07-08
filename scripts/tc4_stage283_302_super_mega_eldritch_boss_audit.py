#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
checks = []

def require(path, needle, label):
    text = (ROOT / path).read_text(encoding='utf-8')
    ok = needle in text
    checks.append((label, ok, path, needle))
    if not ok:
        raise SystemExit(f"FAIL {label}: missing {needle!r} in {path}")

require('build.gradle', "version = '3.02.0'", 'project version 3.02.0')
require('src/main/resources/META-INF/mods.toml', 'version="3.02.0"', 'mods.toml version 3.02.0')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java', 'CULTIST_PORTAL', 'cultist portal registry')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java', 'TAINTACLE_GIANT', 'taintacle giant registry')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java', 'CultistPortalEntity.createAttributes()', 'cultist portal attributes')
require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java', 'TaintacleGiantEntity.createAttributes()', 'taintacle giant attributes')
require('src/main/java/com/darkifov/thaumcraft/entity/CultistPortalEntity.java', 'stageCounter = 200', 'cultist portal original stagecounter')
require('src/main/java/com/darkifov/thaumcraft/entity/CultistPortalEntity.java', 'spawnMinion', 'cultist portal staged minions')
require('src/main/java/com/darkifov/thaumcraft/entity/CultistPortalEntity.java', 'spawnBoss', 'cultist portal boss spawn')
require('src/main/java/com/darkifov/thaumcraft/entity/TaintacleGiantEntity.java', 'amount > 35.0F', 'taintacle damage cap')
require('src/main/java/com/darkifov/thaumcraft/entity/TaintacleGiantEntity.java', 'anger = 200', 'taintacle anger timer')
require('src/main/java/com/darkifov/thaumcraft/entity/TaintacleGiantEntity.java', 'TC4ChampionModifierRuntime.makeChampion', 'taintacle champion finalize')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchLockBossSpawner.java', 'CultistPortalEntity', 'lock spawns real cultist portal')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchLockBossSpawner.java', 'TaintacleGiantEntity', 'lock spawns real taintacle giant')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java', 'TC4MindSpiderRenderer::new', 'mind spider renderer registered')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java', 'TC4CultistPortalRenderer::new', 'cultist portal renderer registered')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java', 'TC4TaintacleGiantRenderer::new', 'taintacle renderer registered')
require('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java', 'TC4EldritchTileRenderer::new', 'eldritch tile renderer registered')
require('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java', 'dropBossDeathLoot(LivingEntity boss', 'non-ThaumcraftBoss loot overload')
require('docs/NEXT_CHAT_PROMPT_STAGE302.md', 'Stage303-312', 'handoff next prompt')

report = {
    'stage': '283-302',
    'name': 'TC4_SUPER_MEGA_ELDRITCH_BOSS_TILE_RENDER_BATCH',
    'checks': [{'label': label, 'ok': ok, 'path': path} for label, ok, path, needle in checks],
    'result': 'PASS'
}
print(json.dumps(report, indent=2))
