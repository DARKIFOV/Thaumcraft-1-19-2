#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]

def read(p):
    return (ROOT / p).read_text(encoding='utf-8', errors='ignore')

checks = {
    'version_build_232': "version = '2.32.0'" in read('build.gradle'),
    'version_mods_232': 'version="2.32.0"' in read('src/main/resources/META-INF/mods.toml'),
    'maze_cell_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeCell.java').exists(),
    'maze_cellloc_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeCellLoc.java').exists(),
    'maze_generator_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeGenerator.java').exists(),
    'maze_handler_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java').exists(),
    'packed_cell_bits': all(token in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeCell.java') for token in ['north', 'south', 'east', 'west', 'above', 'below', 'feature', '<< 8']),
    'labyrinth_map': 'labyrinth' in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java') and 'ConcurrentHashMap' in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java'),
    'feature_dispatch': all(token in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsMazeHandler.java') for token in ['FEATURE_PORTAL', 'FEATURE_KEY_ROOM', 'FEATURE_NEST_ROOM', 'FEATURE_LIBRARY_ROOM', 'generatePassage']),
    'room_selector_uses_maze': 'TC4OuterLandsMazeHandler.generateAround' in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsBossRoomPlacer.java'),
    'worldgen_tick_uses_maze': 'TC4OuterLandsMazeHandler.tickPlayerArea' in read('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java'),
    'decor_adapter_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsDecorationAdapter.java').exists(),
    'loot_post_adapter_exists': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4LootPotionEnchantAdapter.java').exists(),
    'loot_post_called': 'TC4LootPotionEnchantAdapter.postProcessGeneratedLoot' in read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java'),
    'next_prompt_stage232': (ROOT/'docs/NEXT_CHAT_PROMPT_STAGE232.md').exists(),
    'report_stage232': json.loads(read('STAGE223_232_TC4_OUTER_LANDS_MAZE_LOOT_BATCH_REPORT.json')).get('version') == '2.32.0',
}
errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print('::error::' + error)
    sys.exit(1)
print(json.dumps({'stage_batch': '223-232', 'checks': checks}, indent=2, sort_keys=True))
