#!/usr/bin/env python3
"""v11.63.64 original Infusion Matrix essentia/component shortage instability-creep guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.64 infusion shortage instability guard: FAIL: {m}")
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 64), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 64), 'mods version')
    parity=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionShortageInstabilityParity.java')
    runtime=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java')
    blockentity=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')

    for token in ('essentiaShortageCancelsCraft','componentShortageCancelsCraft','essentiaShortageAttemptsEveryPendingAspectPerCycle','essentiaShortageForcesSurroundingsResurvey','componentShortageForcesSurroundingsResurvey','componentShortageFirstUnmatchedIngredientAlwaysRefunds','componentShortageRefundRollBoundForIndex','essentiaShortageInstabilityRollBound','componentShortageInstabilityRollBound'):
        req(token in parity,f'missing parity token {token}')

    req('componentShortageEssentiaRefundRollBound' in runtime,'missing runtime helper componentShortageEssentiaRefundRollBound')
    req('Math.max(1, 1 + ingredientIndex)' in runtime,'component shortage refund formula must be nextInt(1 + a)')
    req('ESSENTIA_FAILURE_BASE_ROLL = 100' in runtime,'essentia shortage instability base must stay 100')
    req('COMPONENT_FAILURE_BASE_ROLL = 50' in runtime,'component/xp shortage instability base must stay 50')

    # Essentia shortage must loop every pending aspect per cycle, not just the first one.
    req('for (Aspect aspect : new ArrayList<>(pendingAspects.keySet()))' in blockentity,
        'drainNextEssentia must iterate every pending aspect per cycle')
    req('checkSurroundings = true;' in blockentity,
        'essentia shortage must force checkSurroundings = true after all pending aspects fail')

    # Component shortage refund gate must be rolled per unmatched ingredient index inside the pedestal loop.
    req('TC4InfusionRuntime.componentShortageEssentiaRefundRollBound(i)' in blockentity,
        'pullNextComponent must roll the essentia-refund gate per unmatched ingredient index i')

    for method in ('infusionEssentiaAndComponentShortageNeverCancelTheCraft','infusionEssentiaShortageAttemptsEveryPendingAspectPerCycle','infusionEssentiaShortageForcesResurveyButComponentShortageDoesNot','infusionComponentShortageFirstUnmatchedIngredientAlwaysRefundsEssentia','infusionComponentShortageRefundOddsShrinkPerUnmatchedIndex'):
        req(method in tests,f'missing {method}')
    req('TC4InfusionShortageInstabilityParity' in tests,'missing import usage in gametest file')
    req(tests.count('@GameTest(') >= 71, 'expected at least 71 cumulative GameTests')

    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 64) and len(manifest.get('tests', [])) >= 399, 'manifest version/count')
    expected={'gametest.infusion_shortage_never_cancels_craft_contract','gametest.infusion_essentia_shortage_retries_every_pending_aspect_contract','gametest.infusion_essentia_shortage_forces_resurvey_contract','gametest.infusion_component_shortage_first_index_always_refunds_contract','gametest.infusion_component_shortage_refund_odds_shrink_contract'}
    req(expected <= {x.get('id') for x in manifest['tests']},'manifest scenarios')

    print('TC4 v11.63.64 infusion shortage instability guard: PASS (71 GameTests, 399 scenarios)')
if __name__=='__main__': main()
