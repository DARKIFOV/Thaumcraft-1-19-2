#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]

def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.96 Research efficiency parity guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=text('build.gradle')
mods=text('src/main/resources/META-INF/mods.toml')
m=re.search(r"(?m)^version = '([0-9.]+)'",build)
req(m is not None,'build version')
current=tuple(int(x) for x in m.group(1).split('.'))
req(current >= (11,63,96),'project must be v11.63.96 or newer')
req(f'version="{m.group(1)}"' in mods,'mods.toml version')

parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchEfficiencyParity.java')
for token in (
 'CONTRACT_VERSION = "11.63.96"',
 'MASTERY_FREE_PLACEMENT_CHANCE = 0.10F',
 'EXPERTISE_CLEAR_REFUND_CHANCE = 0.25F',
 'MASTERY_CLEAR_REFUND_CHANCE = 0.50F',
 'BONUS_RECALCULATE_THRESHOLD_TICKS = 600',
 'BONUS_SCAN_RADIUS = 8',
 'roll < MASTERY_FREE_PLACEMENT_CHANCE',
 'roll < EXPERTISE_CLEAR_REFUND_CHANCE',
 'roll < MASTERY_CLEAR_REFUND_CHANCE',
 'placementNeedsAspectSource'):
    req(token in parity,'missing parity token '+token)

solver=text('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteSolver.java')
roll_at=solver.index('boolean freePlacement = shouldPreservePlacedAspect(player);')
stock_at=solver.index('ResearchTableInventoryRuntime.hasPoolOrTableBonus(player, aspect)')
req(roll_at < stock_at,'Mastery roll must happen before stock rejection')
for token in (
 'TC4ResearchEfficiencyParity.placementNeedsAspectSource(freePlacement)',
 'TC4ResearchEfficiencyParity.masteryFreePlacement('):
    req(token in solver,'production solver is not linked: '+token)
clear_parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchNoteClearParity.java')
req('TC4ResearchNoteClearParity.shouldRefundClearedAspect(' in solver,
    'production solver is not linked to clear refund parity')
req('TC4ResearchEfficiencyParity.clearedAspectRefund(' in clear_parity,
    'clear refund parity no longer delegates to research efficiency boundaries')
req('player.getRandom().nextFloat() < 0.10F' not in solver,'stale Mastery literal')
req('return roll < 0.50F' not in solver and 'return roll < 0.25F' not in solver,'stale refund literals')

bonus=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableBonusRuntime.java')
req('TC4ResearchEfficiencyParity.BONUS_RECALCULATE_THRESHOLD_TICKS' in bonus,'bonus cadence not linked')
req('TC4ResearchEfficiencyParity.BONUS_SCAN_RADIUS' in bonus,'bonus radius not linked')

# Methods are static source contracts; runtime server execution remains a separate gate.
gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=104,f'expected at least 104 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names must be unique')
for name in (
 'researchMasteryFreePlacementOrderingMatchesTc4',
 'researchExpertiseMasteryRefundThresholdsMatchTc4',
 'researchTableBonusCadenceAndRadiusMatchTc4'):
    req(name in methods,'missing GameTest '+name)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(manifest['version']==m.group(1),'manifest version')
req(len(ids)>=432,f'expected at least 432 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
for id_ in (
 'gametest.research_mastery_free_placement_ordering_contract',
 'gametest.research_expertise_mastery_refund_threshold_contract',
 'gametest.research_table_bonus_cadence_radius_contract'):
    req(id_ in ids,'missing manifest id '+id_)
entry={x['id']:x for x in manifest['tests']}
for id_ in (
 'gametest.research_mastery_free_placement_ordering_contract',
 'gametest.research_expertise_mastery_refund_threshold_contract',
 'gametest.research_table_bonus_cadence_radius_contract'):
    req(entry[id_].get('subsystem')=='research_table','v96 manifest subsystem')
    req(entry[id_].get('category')=='gametest','v96 manifest category')
    req(entry[id_].get('required') is True,'v96 manifest required flag')

print(f'TC4 v11.63.96 Research efficiency parity guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; Mastery ordering production-linked)')
