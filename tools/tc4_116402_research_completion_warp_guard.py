#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.64.02 research completion warp guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'version parse'); return tuple(map(int,m.groups()))
req(version_tuple(text('build.gradle')) >= (11,64,2),'build version')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11,64,2),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchCompletionWarpParity.java')
for token in ('CONTRACT_VERSION = "11.64.02"','record WarpSplit','splitResearchWarp','int sticky = warp / 2','return new WarpSplit(warp - sticky, sticky)'):
    req(token in parity,'missing parity token '+token)
progress=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchProgression.java')
for token in ('applyResearchWarp(player, entry.warp())','TC4ResearchCompletionWarpParity.splitResearchWarp(rawWarp)','PlayerThaumData.addWarpPermanent(player, split.permanent())','PlayerThaumData.addWarpSticky(player, split.sticky())'):
    req(token in progress,'production wiring missing '+token)
req('PlayerThaumData.addWarpPermanent(player, warp)' not in progress,'legacy all-permanent research warp remains')
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=131,f'expected at least 131 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in ('researchCompletionWarpSplitMatchesOriginal','researchCompletionWarpUpdatesPermanentStickyAndCounter','repeatedResearchUnlockDoesNotDuplicateWarp'):
    req(name in methods,'missing GameTest '+name)
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,2),'manifest version')
req(len(ids)>=459,f'expected at least 459 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in ('gametest.research_completion_warp_split_table','gametest.research_completion_warp_buckets_counter','gametest.research_completion_warp_repeat_idempotence'):
    req(id_ in ids,'missing manifest '+id_)
evidence=json.loads(text('tools/data/tc4_research_completion_warp_source_evidence_v11.64.02.json'))
for key,value in evidence['claims'].items(): req(value is True,'source evidence '+key)
req(evidence['runtime_status']=='NOT_VERIFIED','runtime status honesty')
print('TC4 v11.64.02 research completion warp guard: PASS (131 GameTests; 459 scenarios; permanent/sticky split source-linked)')
