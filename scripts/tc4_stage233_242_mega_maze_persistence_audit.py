#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]

def read(path):
    return (ROOT / path).read_text(encoding='utf-8', errors='ignore')

maze = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeGenerator.java')
handler = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java')
gen = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
report = json.loads(read('STAGE233_242_TC4_OUTER_LANDS_MAZE_PERSISTENCE_BATCH_REPORT.json'))

checks = {
    'version_build_242': "version = '2.42.0'" in read('build.gradle'),
    'version_mods_242': 'version="2.42.0"' in read('src/main/resources/META-INF/mods.toml'),
    'next_prompt_242': (ROOT/'docs/NEXT_CHAT_PROMPT_STAGE242.md').exists(),
    'report_242': report.get('version') == '2.42.0',
    'original_dfs_bias': all(t in maze for t in ['getNextIndex', '0.45F', '0.9F', 'Collections.shuffle', 'deadEnds']),
    'boss_anchor_features': all(t in maze for t in ['FEATURE_BOSS_2 << 8', 'FEATURE_BOSS_3 << 8', 'FEATURE_BOSS_4 << 8', 'FEATURE_BOSS_5 << 8']),
    'maze_thread_copy_contract': all(t in maze for t in ['centerX - (1 + width / 2)', 'centerZ - (1 + height / 2)', 'removeIfZero']),
    'odd_size_selection': '15 + random.nextInt(8) * 2' in maze,
    'saved_data_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeSavedData.java').exists(),
    'saved_data_payload': all(t in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeSavedData.java') for t in ['SavedData', 'Data', 'thaumcraft_labyrinth']),
    'portal_generated_persist': all(t in handler for t in ['portalOrigins', 'generatedCells', 'StringTag.valueOf']),
    'feature_selector_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsFeatureSelector.java').exists(),
    'feature_selector_dispatch': all(t in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsFeatureSelector.java') for t in ['FEATURE_PORTAL', 'FEATURE_KEY_ROOM', 'FEATURE_NEST_ROOM', 'FEATURE_LIBRARY_ROOM', 'generatePassage']),
    'pat_connect_exact': 'PAT_CONNECT' in gen and '{1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1}' in gen,
    'connection_depth_loops': all(t in gen for t in ['placeConnectionSlice', 'depth == 2', 'depth == 3', 'justTheTip']),
    'eldritch_nothing_block': (ROOT/'src/main/java/com/darkifov/thaumcraft/block/EldritchNothingBlock.java').exists() and 'ELDRITCH_NOTHING' in mod and 'ThaumcraftMod.ELDRITCH_NOTHING' in gen,
    'no_legacy_api': not any(t in maze + handler + gen for t in ['NBTTag', 'func_', 'net.minecraft.world.World;']),
}
errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print('::error::' + error)
    sys.exit(1)
print(json.dumps({'stage_batch': '233-242', 'checks': checks}, indent=2, sort_keys=True))
