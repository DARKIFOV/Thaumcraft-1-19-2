#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.99 Research Note graph guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'version parse')
    return tuple(map(int,m.groups()))
build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
req(version_tuple(build)>=(11,63,99),'build version')
req(version_tuple(mods)>=(11,63,99),'mods.toml version')

parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteGraphParity.java')
for token in (
 'CONTRACT_VERSION = "11.63.99"',
 'ORIGINAL_EMPTY_HEX_TYPE = 0',
 'ORIGINAL_MAX_HEX_SLOTS = 61',
 'canPlaceIntoHex',
 'placementRequiresCompatibleNeighbour()',
 'return false;',
 'acceptsServerTableContext'):
    req(token in parity,'missing graph parity token '+token)

state=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java')
req('public static boolean canPlaceAspect(ItemStack stack, int index, Aspect aspect)' in state,'server empty-hex predicate missing')
req('TC4ResearchNoteGraphParity.canPlaceIntoHex(' in state,'state not linked to graph parity')

solver=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
req('ResearchNoteState.canPlaceAspect(note, slot, aspect)' in solver,'solver not using empty-hex predicate')
req('ResearchNoteState.touchesCompatibleNeighbor(note, slot, aspect)' not in solver,'stale neighbour placement gate remains')
req('debitResearchNotePlacementAtomically(' in solver,'atomic placement debit missing')
req('rollbackResearchNotePlacementDebit(player, debit.get())' in solver,'final-write rollback missing')
req(solver.index('debitResearchNotePlacementAtomically') < solver.index('ResearchNoteState.place(note, slot, aspect)'), 'resources must commit before final note write')

runtime=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
for token in (
 'TC4ResearchNoteGraphParity.acceptsServerTableContext(menuOpen, stillValid)',
 'public record ResearchNotePlacementDebit',
 'debitResearchNotePlacementAtomically(',
 'rollbackResearchNotePlacementDebit(',
 'restoreOpenTableInkDamage',
 'restoreAspectSourceAmounts('):
    req(token in runtime,'runtime transaction/context missing '+token)

packet=text('src/main/java/com/darkifov/thaumcraft/network/RequestPlaceResearchNoteAspectPacket.java')
req('if (ResearchNoteSolver.placeAspect(player, note, packet.slot, aspect)) {' in packet,'packet does not gate sync on accepted edit')

for rel in ('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java','src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java'):
    screen=text(rel)
    req('TC4ResearchNoteGraphParity.canPlaceIntoHex(' in screen,rel+' not linked to original empty-hex rule')
    req('touchesCompatibleClientNeighbor(slot, aspect)' not in screen,rel+' retains neighbour admission gate')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=116,f'expected at least 116 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in (
 'researchNoteDisconnectedEmptyHexPlacementMatchesTc4',
 'researchNoteForgedUnknownAspectIsRejectedServerSide',
 'researchNoteForgedOccupiedHexIsRejectedWithoutDebit',
 'researchNotePlacementRollbackRestoresInkAndAspect',
 'researchNotePacketRequiresLiveOpenResearchTable'):
    req(name in methods,'missing behavioral GameTest '+name)
for token in (
 'ResearchNoteSolver.placeAspect(player, fixture.note(), fixture.targetSlot(), Aspect.AER)',
 'ResearchNoteInventoryRuntime' if False else 'ResearchTableInventoryRuntime.debitResearchNotePlacementAtomically(',
 'new ResearchTableMenu(116399',
 'controlledResearchNote(false)'):
    req(token in gt,'real test path missing '+token)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,63,99),'manifest version')
req(len(ids)>=444,f'expected at least 444 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in (
 'gametest.research_note_disconnected_empty_hex_placement_behavior',
 'gametest.research_note_unknown_aspect_forged_request_rejection',
 'gametest.research_note_occupied_hex_forged_request_rejection',
 'gametest.research_note_placement_resource_rollback_behavior',
 'gametest.research_note_live_open_table_packet_context'):
    req(id_ in ids,'missing manifest '+id_)

evidence=json.loads(text('tools/data/tc4_research_note_graph_source_evidence_v11.63.99.json'))
req(evidence['claims']['placement_neighbour_gate_removed'] is True,'source evidence neighbour rule')
req(evidence['claims']['ink_and_aspect_debit_atomic_with_rollback'] is True,'source evidence transaction')
print(f'TC4 v11.63.99 Research Note graph guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; original empty-hex rule + atomic server edit)')
