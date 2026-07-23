#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.64.01 Research Note completion guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'version parse')
    return tuple(map(int,m.groups()))
req(version_tuple(text('build.gradle')) >= (11,64,1),'build version')
req(version_tuple(text('src/main/resources/META-INF/mods.toml')) >= (11,64,1),'mods version')
parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteCompletionParity.java')
for token in ('CONTRACT_VERSION = "11.64.01"','ORIGINAL_DISCOVERY_DAMAGE = 64','COMPLETED_DISCOVERY_CONSUME_COUNT = 1','acceptsCompletionContext','canCommitCompletion','completionConsumesAdditionalInk()','completedDiscoveryConsumedInCreative()','shouldUnlockSibling','completedDiscoveryConsumeCount'):
    req(token in parity,'missing parity token '+token)
runtime=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
for token in ('public record ResearchNoteCompletionSnapshot','beginResearchNoteCompletion(','commitResearchNoteCompletion(','openNote.get() == note','note.getTag() == null ? null : note.getTag().copy()','snapshot.noteTagBefore().equals(current)','ResearchNoteState.isSolvedForPlayer(snapshot.note(), player)','ResearchNoteState.markSolved(snapshot.note())','snapshot.note().setTag(rollback == null ? null : rollback.copy())'):
    req(token in runtime,'completion transaction missing '+token)
solver=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
for token in ('beginResearchNoteCompletion(player, note)','commitResearchNoteCompletion(player, snapshot.get())','if (ResearchNoteState.solved(note))','public record SolvedNoteConversionSnapshot','beginSolvedNoteConversion(','commitSolvedNoteConversion(','if (!OriginalResearchBridge.unlock(player, target.get()))','OriginalResearchBridge.unlockEligibleSiblings(player, target.get())','snapshot.note().shrink(TC4ResearchNoteCompletionParity.completedDiscoveryConsumeCount())'):
    req(token in solver,'solver completion/conversion missing '+token)
segment=solver[solver.index('commitSolvedNoteConversion'):solver.index('private static boolean shouldPreservePlacedAspect')]
req('if (!player.getAbilities().instabuild)' not in segment,'creative-only note retention remains')
bridge=text('src/main/java/com/darkifov/thaumcraft/research/OriginalResearchBridge.java')
for token in ('unlockEligibleSiblings(','target.siblings()','TC4ResearchNoteCompletionParity.shouldUnlockSibling(','&& unlock(player, sibling.get())'):
    req(token in bridge,'sibling completion missing '+token)
packet=text('src/main/java/com/darkifov/thaumcraft/network/RequestSolveResearchNotePacket.java')
req('if (ResearchNoteSolver.solve(player, note)) {' in packet,'solve packet is not success-gated')
solve_block=packet[packet.index('if (ResearchNoteSolver.solve'):]
req('ThaumcraftNetwork.syncResearchNote(player, note)' in solve_block,'successful note sync missing')
req('ThaumcraftNetwork.syncAspectKnowledge(player)' not in solve_block,'irrelevant aspect sync remains')
req('ThaumcraftNetwork.syncResearch(player)' not in solve_block,'irrelevant research sync remains')
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=128,f'expected at least 128 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in ('researchNoteCompletionRequiresLiveOpenTable','researchNoteConnectedGraphCompletesAndPrunes','researchNoteRepeatedCompletionIsRejected','researchNoteStaleCompletionSnapshotIsRejected','completedDiscoveryConsumesCreativeAndUnlocksSibling','staleSolvedDiscoverySnapshotCannotUnlockOrConsume'):
    req(name in methods,'missing GameTest '+name)
req('return table;\n        return table;' not in gt,'duplicate unreachable return remains')
manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,1),'manifest version')
req(len(ids)>=456,f'expected at least 456 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in ('gametest.research_note_completion_live_table_context','gametest.research_note_completion_connected_graph_pruning','gametest.research_note_completion_repeat_rejection','gametest.research_note_completion_stale_snapshot_rejection','gametest.research_discovery_creative_consumption_sibling_unlock','gametest.research_discovery_stale_snapshot_rejection'):
    req(id_ in ids,'missing manifest '+id_)
evidence=json.loads(text('tools/data/tc4_research_note_completion_source_evidence_v11.64.01.json'))
for key,value in evidence['claims'].items(): req(value is True,'source evidence '+key)
req(evidence['runtime_status']=='NOT_VERIFIED','runtime status honesty')
print('TC4 v11.64.01 Research Note completion guard: PASS (128 GameTests; 456 manifest scenarios; completion/discovery transactions)')
