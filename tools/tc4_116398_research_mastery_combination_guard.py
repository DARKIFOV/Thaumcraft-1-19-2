#!/usr/bin/env python3
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1]

def req(cond,msg):
    if not cond: raise SystemExit('TC4 v11.63.98 Research Mastery combination guard: FAIL: '+msg)
def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=text('build.gradle'); mods=text('src/main/resources/META-INF/mods.toml')

def version_tuple(raw):
    m=re.search(r'(?m)^version\s*=\s*["\'](\d+)\.(\d+)\.(\d+)["\']',raw)
    req(m is not None,'project version parse')
    return tuple(map(int,m.groups()))
req(version_tuple(build) >= (11,63,98),'build version must be 11.63.98 or newer')
req(version_tuple(mods) >= (11,63,98),'mods.toml version must be 11.63.98 or newer')

parity=text('src/main/java/com/darkifov/thaumcraft/research/TC4ResearchMasteryCombinationParity.java')
for token in (
 'CONTRACT_VERSION = "11.63.98"',
 'COMPONENTS_PER_COMBINATION = 2',
 'record SourceDebit', 'record PairDebitPlan',
 'if (playerPool > 0)', 'if (tableBonus > 0)',
 'plan(1, 0, 0, 0, false).isEmpty()',
 'invalidPairsStillConsumeComponents()'):
    req(token in parity,'missing parity token '+token)

runtime=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java')
for token in (
 'consumeAspectPairAtomically(Player player, Aspect first, Aspect second)',
 'TC4ResearchMasteryCombinationParity.plan(',
 'consumePlanned(player, first,',
 'restoreAspectSourceAmounts(player, first,',
 'PlayerAspectKnowledge.setPoolAmountForTransaction',
 'menu.setBonusAmountForTransaction'):
    req(token in runtime,'transaction not production-linked: '+token)

foundation=text('src/main/java/com/darkifov/thaumcraft/research/ResearchTableFoundation.java')
req('ResearchTableInventoryRuntime.consumeAspectPairAtomically(player, first, second)' in foundation,
    'foundation does not use atomic pair debit')
req('consumePoolOrTableBonus(player, first)' not in foundation,
    'stale independent first-component debit remains')
req('Optional<Aspect> result = AspectCombinationRegistry.combine(first, second);' in foundation,
    'result resolution missing after debit')
req(foundation.index('consumeAspectPairAtomically') < foundation.index('AspectCombinationRegistry.combine(first, second)'),
    'invalid pairs must consume resource-complete components before result validation')

be=text('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java')
menu=text('src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java')
knowledge=text('src/main/java/com/darkifov/thaumcraft/research/PlayerAspectKnowledge.java')
for token in ('consumeBonusAspect(Aspect aspect, int amount)','setBonusAmountForTransaction'):
    req(token in be,'block entity transaction hook missing '+token)
    req(token in menu,'menu transaction hook missing '+token)
req('setPoolAmountForTransaction' in knowledge,'pool rollback hook missing')

gt=text('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
methods=re.findall(r'@GameTest\([^)]*\)\s*public static void\s+(\w+)\s*\(',gt,re.S)
req(len(methods)>=111,f'expected at least 111 annotated GameTests, got {len(methods)}')
req(len(methods)==len(set(methods)),'GameTest names unique')
for name in (
 'researchMasteryCombinationConsumesPoolAndBonusAtomically',
 'researchCombinationMissingSecondComponentRollsBackFirst',
 'researchInvalidCombinationStillConsumesBothComponents',
 'researchSameAspectShortageCannotPartiallyDebit'):
    req(name in methods,'missing behavioral GameTest '+name)
for token in (
 'player.containerMenu = new ResearchTableMenu',
 'ResearchTableFoundation.combine(player, Aspect.AER, Aspect.PERDITIO)',
 'table.bonusAspects().add(Aspect.AER, 1)',
 'ResearchTableFoundation.combine(player, Aspect.AER, Aspect.TERRA)',
 'ResearchTableFoundation.combine(player, Aspect.AER, Aspect.AER)'):
    req(token in gt,'tests do not exercise real player/menu/table path: '+token)

manifest=json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
ids=[x['id'] for x in manifest['tests']]
req(tuple(map(int,manifest['version'].split('.'))) >= (11,63,98),'manifest version')
req(len(ids)>=439,f'expected at least 439 manifest scenarios, got {len(ids)}')
req(len(ids)==len(set(ids)),'manifest ids unique')
entries={x['id']:x for x in manifest['tests']}
for id_ in (
 'gametest.research_mastery_combination_pool_bonus_atomic_behavior',
 'gametest.research_combination_missing_second_component_rollback_behavior',
 'gametest.research_invalid_combination_consumes_both_behavior',
 'gametest.research_same_aspect_shortage_atomic_behavior'):
    req(id_ in entries,'missing manifest '+id_)
    req(entries[id_].get('subsystem')=='research_table','manifest subsystem '+id_)
    req(entries[id_].get('required') is True,'manifest required '+id_)

print(f'TC4 v11.63.98 Research Mastery combination guard: PASS ({len(methods)} GameTests; {len(ids)} manifest scenarios; atomic production debit retained)')
