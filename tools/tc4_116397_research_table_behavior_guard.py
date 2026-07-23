#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]

def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.97 Research Table behavior guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*[\"\'](\d+)\.(\d+)\.(\d+)[\"\']', raw)
    req(m is not None, 'project version parse')
    return tuple(map(int,m.groups()))
req(version_tuple(build) >= (11,63,97),'build version must be 11.63.97 or newer')
req(version_tuple(mods) >= (11,63,97),'mods.toml version must be 11.63.97 or newer')

parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchTableBehaviorParity.java')
for token in (
 'CONTRACT_VERSION = "11.63.97"',
 'NEXT_RECALC_TAG = "nextRecalc"',
 'BONUS_ASPECTS_TAG = "bonusAspects"',
 'BONUS_ASPECT_TAG = "tag"',
 'counterBeforeTick > TC4ResearchEfficiencyParity.BONUS_RECALCULATE_THRESHOLD_TICKS',
 'return shouldRecalculate(counterBeforeTick) ? 0 : counterBeforeTick + 1',
 'return amount > 0 ? 1 : 0',
 'return !alreadyLoaded'):
    req(token in parity,'missing parity token '+token)

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
for token in (
 'TC4ResearchTableBehaviorParity.counterAfterTick(counterBeforeTick)',
 'TC4ResearchTableBehaviorParity.shouldRecalculate(counterBeforeTick)',
 'ResearchTableBonusRuntime.recalculateInto(level, pos, table.bonusAspects);',
 'table.setChanged();',
 'table.syncToClient();',
 'TC4ResearchTableBehaviorParity.NEXT_RECALC_TAG',
 'TC4ResearchTableBehaviorParity.BONUS_ASPECTS_TAG',
 'TC4ResearchTableBehaviorParity.serializedCopiesForAmount(entry.getValue())',
 'TC4ResearchTableBehaviorParity.shouldLoadSerializedType(bonusAspects.get(aspect) > 0)'):
    req(token in be,'production block entity not linked: '+token)
req('for (int i = 0; i < entry.getValue(); i++)' not in be,'stale per-point bonus serialization loop')
recalc=be.index('ResearchTableBonusRuntime.recalculateInto(level, pos, table.bonusAspects);')
changed=be.index('table.setChanged();', recalc)
sync=be.index('table.syncToClient();', changed)
req(recalc < changed < sync,'recalc tick must always persist/sync counter reset')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=107,f'expected at least 107 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in (
 'researchTableBonusNbtCollapsesDuplicateTypesLikeTc4',
 'researchTableBonusConsumptionPersistsAcrossSaveReload',
 'researchTableRecalcCounterUsesOriginalPostIncrementBoundary'):
    req(name in methods,'missing behavioral GameTest '+name)
for token in (
 'ResearchTableBlockEntity table = requireBlockEntity',
 'table.saveWithoutMetadata()',
 'restored.load(saved)',
 'serialized.add(duplicateAer)',
 'ResearchTableBlockEntity.serverTick(level, pos, state, table)'):
    req(token in gt,'behavioral tests do not exercise real block entity: '+token)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,63,97),'manifest version must be 11.63.97 or newer')
req(len(ids)>=435,f'expected at least 435 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
entries={x['id']:x for x in manifest['tests']}
for id_ in (
 'gametest.research_table_bonus_nbt_cardinality_behavior',
 'gametest.research_table_bonus_consumption_save_reload_behavior',
 'gametest.research_table_recalc_postincrement_boundary_behavior'):
    req(id_ in entries,'missing manifest '+id_)
    req(entries[id_].get('subsystem')=='research_table','manifest subsystem '+id_)
    req(entries[id_].get('category')=='gametest','manifest category '+id_)
    req(entries[id_].get('required') is True,'manifest required '+id_)

print(f'TC4 v11.63.97 Research Table behavior guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; real BE NBT/cadence coverage retained)')
