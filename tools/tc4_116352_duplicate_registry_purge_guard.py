#!/usr/bin/env python3
"""v11.63.52 exact duplicate item registry purge contract."""
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def read(p): return (ROOT/p).read_text(encoding='utf-8')
def req(c,m):
    if not c: raise AssertionError(m)

def main():
    version=re.search(r"^version = '([0-9.]+)'", read('build.gradle'), re.M).group(1)
    req(tuple(map(int,version.split('.'))) >= (11,63,52),'build version')
    mods_version=re.search(r'^version="([0-9.]+)"', read('src/main/resources/META-INF/mods.toml'), re.M).group(1)
    req(mods_version==version,'mods version')
    mig=read('src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyDuplicateItemMigrator.java')
    mappings=dict(re.findall(r'map\.put\("([^"]+)", "([^"]+)"\);',mig))
    req(len(mappings)==63,f'exact mapping count {len(mappings)}')
    mod=read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
    req('MissingMappingsEvent' in mod and 'mapping.remap(target);' in mod,'missing mapping remap')
    research=read('src/main/java/com/darkifov/thaumcraft/porting/TC4ResearchItems.java')
    req('isRemovedDuplicateId(entry.id())' in research,'registration skip')
    req('canonicalReplacementStack' in research,'book/source canonical fallback')
    for old,new in mappings.items():
        req(not (ROOT/f'src/main/resources/assets/thaumcraft/models/item/{old}.json').exists(),f'model remains {old}')
        for lang in ('en_us.json','ru_ru.json'):
            data=json.loads(read(f'src/main/resources/assets/thaumcraft/lang/{lang}'))
            req(f'item.thaumcraft.{old}' not in data,f'lang remains {old}')
        for p in (ROOT/'src/main/resources/data').rglob('*.json'):
            if 'tc4_source_mapping' in p.parts: continue
            req(f'thaumcraft:{old}' not in p.read_text(encoding='utf-8'),f'active datapack ref {old}: {p}')
    manifest=json.loads(read('reports/duplicate_item_purge_v11.63.52.json'))
    req(len(manifest['removed_item_aliases'])==63,'manifest mapping count')
    req(manifest['removed_model_count']==63,'manifest model count')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req(tests.count('@GameTest(')>=19,'required GameTest count')
    req('exactDuplicateLegacyItemAliasesAreNotRegistered' in tests,'duplicate absence GameTest')
    runtime=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(runtime['version']==version and len(runtime['tests'])>=347,'runtime manifest')
    print('TC4 v11.63.52 duplicate registry purge guard: PASS (63 aliases removed, 19 GameTests, 347 scenarios)')
if __name__=='__main__': main()
