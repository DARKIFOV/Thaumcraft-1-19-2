#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    p = ROOT / rel
    if not p.exists():
        raise AssertionError(f"missing {rel}")
    return p.read_text(encoding="utf-8", errors="ignore")

def require(text, needle, label):
    if needle not in text:
        raise AssertionError(f"missing {label}: {needle}")

def main():
    require(read('build.gradle'), "version = '2.72.0'", 'Stage263-272 build version')
    require(read('src/main/resources/META-INF/mods.toml'), 'version="2.72.0"', 'Stage263-272 mods version')
    passage = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsPassageFeatureAdapter.java')
    for needle in ['FEATURE_TRAPPED_PASSAGE = 11', 'FEATURE_FLESHY_PASSAGE = 12', 'FEATURE_TAINTED_PASSAGE = 13', 'FEATURE_SPIDER_PASSAGE = 14', 'Thaumcraft.MindSpider', 'TAINT_FIBRES', 'ELDRITCH_TRAP']:
        require(passage, needle, f'passage feature {needle}')
    room = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsRoomAdapter.java')
    require(room, 'TC4OuterLandsPassageFeatureAdapter.apply(level, origin, cell)', 'passage feature hook')
    common = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsGenCommonAdapter.java')
    for needle in ['placeLibraryPedestalsAndSlabs', 'placeLibraryColumn', 'Blocks.SMOOTH_STONE_SLAB', 'SlabType.TOP', 'ELDRITCH_CRYSTAL']:
        require(common, needle, f'library/nest parity {needle}')
    manifest = json.loads(read('docs/TC4_STAGE263_272_REMOVED_GARBAGE_RECIPES.json'))
    if manifest.get('removed_count', 0) < 100:
        raise AssertionError('garbage recipe cleanup removed too few files')
    report = json.loads(read('STAGE263_272_TC4_PASSAGE_LIBRARY_NEST_CLEANUP_REPORT.json'))
    if report.get('stage') != '263-272':
        raise AssertionError('bad stage report')
    if report.get('removed_garbage_recipe_files', 0) != manifest.get('removed_count'):
        raise AssertionError('garbage cleanup manifest/report mismatch')
    require(read('docs/NEXT_CHAT_PROMPT_STAGE272.md'), 'Stage263-272', 'next prompt marker')
    print('Stage263-272 passage/library/nest cleanup mega audit OK')

if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage263-272 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
