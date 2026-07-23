#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]
def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.64.00 Research Note clear guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'version parse')
    return tuple(map(int,m.groups()))

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')
req(version_tuple(build)>=(11,64,0),'build version')
req(version_tuple(mods)>=(11,64,0),'mods.toml version')

parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteClearParity.java')
for token in (
 'CONTRACT_VERSION = "11.64.00"',
 'ORIGINAL_CLEARABLE_HEX_TYPE = ResearchNoteGrid.TYPE_PLACED',
 'INK_PER_ACCEPTED_CLEAR = 1',
 'canClearHex',
 'shouldRefundClearedAspect',
 'creativeHasImplicitRefund()',
 'return false;',
 'boundariesMatchTc4'):
    req(token in parity,'missing clear parity token '+token)
req('creative' in parity,'creative parameter must remain explicit')
req('TC4ResearchEfficiencyParity.clearedAspectRefund(' in parity,'refund boundaries not linked to research efficiency contract')

state=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteState.java')
req('public static Optional<Aspect> clearableAspect(ItemStack stack, int index)' in state,'non-mutating clear preflight missing')
req('TC4ResearchNoteClearParity.canClearHex(active, type, existing != null)' in state,'state not linked to clear parity')
req('Optional<Aspect> clearable = clearableAspect(stack, index);' in state,'clear mutation bypasses preflight')

runtime=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
for token in (
 'public record ResearchNoteClearDebit',
 'debitResearchNoteClearAtomically(',
 'rollbackResearchNoteClearDebit(',
 'openNote.get() != note',
 'CompoundTag noteTagBefore = note.getTag() == null ? null : note.getTag().copy()',
 'int poolBefore = PlayerAspectKnowledge.pool(player).get(clearable.get())',
 'if (!consumeInkForEdit(player))',
 'Optional<Aspect> removed = ResearchNoteState.clearSlot(note, slot)',
 'debit.note().setTag(',
 'restoreOpenTableInkDamage(player, debit.inkDamageBefore())',
 'PlayerAspectKnowledge.setPoolAmountForTransaction('):
    req(token in runtime,'clear transaction missing '+token)
req(runtime.index('consumeInkForEdit(player)') < runtime.index('ResearchNoteState.clearSlot(note, slot)'), 'clear transaction must debit ink before mutable write with rollback')

solver=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
for token in (
 'public static boolean clearSlotWithRoll(',
 'ResearchNoteState.clearableAspect(note, slot)',
 'TC4ResearchNoteClearParity.shouldRefundClearedAspect(',
 'debitResearchNoteClearAtomically(',
 'PlayerAspectKnowledge.addPool(player, removed, 1)'):
    req(token in solver,'solver missing '+token)
req('player.getAbilities().instabuild) {\n            return true;' not in solver,'port-only creative refund bypass remains')
req(solver.index('boolean refund = shouldRefundClearedAspect(player, refundRoll)') < solver.index('debitResearchNoteClearAtomically('), 'refund roll ordering drift')

packet=text('src/main/java/com/darkifov/thaumcraft/network/RequestClearResearchNoteSlotPacket.java')
req('if (ResearchNoteSolver.clearSlot(player, note, packet.slot)) {' in packet,'clear packet sync is not success-gated')
req(packet.index('if (ResearchNoteSolver.clearSlot') < packet.index('ThaumcraftNetwork.syncResearchNote'), 'sync ordering')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=122,f'expected at least 122 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in (
 'researchNoteClearConsumesInkWithoutImplicitRefund',
 'researchNoteExpertiseClearRefundBoundary',
 'researchNoteMasteryClearRefundBoundary',
 'researchNoteCreativeHasNoImplicitClearRefund',
 'researchNoteClearRollbackRestoresNbtInkAndPool',
 'researchNoteForgedClearRejectsEmptyAndAnchorHexes'):
    req(name in methods,'missing clear GameTest '+name)
for token in (
 'ResearchNoteSolver.clearSlotWithRoll(',
 'PlayerThaumData.unlockResearch(player, "RESEARCHER1")',
 'PlayerThaumData.unlockResearch(player, "RESEARCHER2")',
 'player.getAbilities().instabuild = true',
 'ResearchTableInventoryRuntime.debitResearchNoteClearAtomically(',
 'ResearchTableInventoryRuntime.rollbackResearchNoteClearDebit(player, debit.get())'):
    req(token in gt,'real clear behavior path missing '+token)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,64,0),'manifest version')
req(len(ids)>=450,f'expected at least 450 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in (
 'gametest.research_note_clear_consumes_ink_without_implicit_refund',
 'gametest.research_note_expertise_clear_refund_boundary',
 'gametest.research_note_mastery_clear_refund_boundary',
 'gametest.research_note_creative_no_implicit_clear_refund',
 'gametest.research_note_clear_full_transaction_rollback',
 'gametest.research_note_forged_clear_empty_anchor_rejection'):
    req(id_ in ids,'missing manifest '+id_)

evidence=json.loads(text('tools/data/tc4_research_note_clear_source_evidence_v11.64.00.json'))
for key in (
 'only_type_2_hex_is_clearable','accepted_clear_consumes_one_ink',
 'expertise_refund_is_strictly_below_0_25','mastery_refund_is_strictly_below_0_50',
 'creative_has_no_implicit_refund','port_clear_is_atomic_with_full_rollback',
 'rejected_packet_does_not_sync'):
    req(evidence['claims'].get(key) is True,'source evidence '+key)
req(evidence['runtime_status']=='NOT_VERIFIED','runtime status must stay honest')
print('TC4 v11.64.00 Research Note clear guard: PASS (122 GameTests; 450 manifest scenarios; atomic clear/refund parity)')
