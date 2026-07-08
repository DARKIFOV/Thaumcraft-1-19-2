#!/usr/bin/env python3
from pathlib import Path
import json, re, sys
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
    require(read('build.gradle'), "version = '2.62.0'", 'Stage253-262 build version')
    require(read('src/main/resources/META-INF/mods.toml'), 'version="2.62.0"', 'Stage253-262 mods version')
    mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
    for needle in ['TC4RegistryGarbageGuard::isHiddenFromCreative', 'ELDRITCH_CAP', 'ELDRITCH_LOCK', 'ELDRITCH_TRAP', 'ELDRITCH_CRYSTAL']:
        require(mod, needle, needle)
    guard = read('src/main/java/com/darkifov/thaumcraft/porting/TC4RegistryGarbageGuard.java')
    for needle in ['tt_', 'tce_', 'avaritia_creative_wand', 'porting_ledger', 'digital_essentia_cell_64k', 'focus_blink']:
        require(guard, needle, f'quarantine {needle}')
    variant = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4EldritchBlockVariantAdapter.java')
    for needle in ['ELDRITCH_CAP', 'ELDRITCH_LOCK', 'ELDRITCH_TRAP', 'ELDRITCH_CRYSTAL']:
        require(variant, needle, f'variant map {needle}')
    loot = read('src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsLootAdapter.java')
    require(loot, 'TC4QuarantinedLootReplacement', 'loot quarantine replacement')
    for name in ['eldritch_cap','eldritch_lock','eldritch_trap','eldritch_crystal']:
        for rel in [f'src/main/resources/assets/thaumcraft/models/block/{name}.json', f'src/main/resources/assets/thaumcraft/models/item/{name}.json', f'src/main/resources/assets/thaumcraft/blockstates/{name}.json', f'src/main/resources/data/thaumcraft/loot_tables/blocks/{name}.json']:
            if not (ROOT/rel).exists():
                raise AssertionError(f'missing resource {rel}')
    report = json.loads(read('STAGE253_262_FULL_STAGE_AUDIT_CLEANUP_REPORT.json'))
    if report.get('stage') != '253-262':
        raise AssertionError('bad report stage')
    if report.get('quarantined_registry_ids', 0) < 100:
        raise AssertionError('quarantine count unexpectedly low')
    require(read('docs/NEXT_CHAT_PROMPT_STAGE262.md'), 'Stage253-262', 'next prompt marker')
    print('Stage253-262 full-stage cleanup/parity audit OK')

if __name__ == '__main__':
    try:
        main()
    except AssertionError as exc:
        print(f'Stage253-262 audit failed: {exc}', file=sys.stderr)
        sys.exit(1)
