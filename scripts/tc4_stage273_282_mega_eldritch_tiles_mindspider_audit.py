#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"missing {rel}")
    return p.read_text(encoding='utf-8', errors='ignore')

def require(text, needle, label):
    if needle not in text:
        raise AssertionError(f"missing {label}: {needle}")

def main():
    require(read('build.gradle'), "version = '2.82.0'", 'Stage273-282 build version')
    require(read('src/main/resources/META-INF/mods.toml'), 'version="2.82.0"', 'Stage273-282 mods version')
    mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
    for needle in [
        'MIND_SPIDER', 'MindSpiderEntity.createAttributes()',
        'ELDRITCH_CAP_BLOCK_ENTITY', 'ELDRITCH_LOCK_BLOCK_ENTITY',
        'ELDRITCH_TRAP_BLOCK_ENTITY', 'ELDRITCH_CRYSTAL_BLOCK_ENTITY',
        'ELDRITCH_CRUST', 'ELDRITCH_DECORATIVE', 'ELDRITCH_DOOR'
    ]:
        require(mod, needle, needle)
    mind = read('src/main/java/com/darkifov/thaumcraft/entity/MindSpiderEntity.java')
    for needle in ['EntityMindSpider', 'harmless', 'viewer', 'lifeSpan = 1200', 'FOLLOW_RANGE, 12.0D', 'MAX_HEALTH, 1.0D', 'ATTACK_DAMAGE, 1.0D', 'onClimbable']:
        require(mind, needle, f'mind spider {needle}')
    passage = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsPassageFeatureAdapter.java')
    require(passage, 'ThaumcraftMod.MIND_SPIDER.get()', 'feature 14 uses dedicated mind spider')
    for rel, needles in {
        'src/main/java/com/darkifov/thaumcraft/blockentity/EldritchTrapBlockEntity.java': ['count = 20', '10 + level.random.nextInt(25)', 'PlayerThaumData.addWarpTemporary', 'sendBlockZap'],
        'src/main/java/com/darkifov/thaumcraft/blockentity/EldritchLockBlockEntity.java': ['count = -1', 'count % 5 == 0', 'count > 100', 'TC4EldritchLockBossSpawner.spawnFromLock'],
        'src/main/java/com/darkifov/thaumcraft/blockentity/EldritchCrystalBlockEntity.java': ['TileEldritchCrystal', 'ParticleTypes.PORTAL'],
        'src/main/java/com/darkifov/thaumcraft/blockentity/EldritchCapBlockEntity.java': ['TileEldritchCap'],
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchLockBossSpawner.java': ['spawnWarden', 'spawnGolem', 'EntityCultistPortal-equivalent', 'EntityTaintacleGiant-equivalent-pending'],
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossCycleData.java': ['bossCount', '0.25F', 'thaumcraft_boss_cycle'],
        'src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchBlockVariantAdapter.java': ['ELDRITCH_CRUST', 'ELDRITCH_DECORATIVE', 'ELDRITCH_DOOR', 'ELDRITCH_CRAB_SPAWNER'],
        'src/main/java/com/darkifov/thaumcraft/client/render/model/TC4EldritchBossLayerDefinitions.java': ['MbeltL', 'ArmR2', 'SidepanelR1', 'Cloak1', 'CollarF', 'Frontcloth2']
    }.items():
        text = read(rel)
        for needle in needles:
            require(text, needle, f'{rel} {needle}')
    for name in ['eldritch_crust','eldritch_decorative','eldritch_door']:
        for rel in [
            f'src/main/resources/assets/thaumcraft/blockstates/{name}.json',
            f'src/main/resources/assets/thaumcraft/models/block/{name}.json',
            f'src/main/resources/assets/thaumcraft/models/item/{name}.json',
            f'src/main/resources/assets/thaumcraft/textures/block/{name}.png',
            f'src/main/resources/data/thaumcraft/loot_tables/blocks/{name}.json'
        ]:
            if not (ROOT / rel).exists():
                raise AssertionError(f'missing resource {rel}')
    require(read('docs/NEXT_CHAT_PROMPT_STAGE282.md'), 'Stage273-282', 'next prompt marker')
    report = json.loads(read('STAGE273_282_TC4_ELDRITCH_TILES_MINDSPIDER_BATCH_REPORT.json'))
    if report.get('stage_batch') != '273-282':
        raise AssertionError('bad report stage')
    print('Stage273-282 eldritch tiles/mindspider mega audit OK')

if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage273-282 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
