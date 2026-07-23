#!/usr/bin/env python3
"""v11.63.65 component refund ledger + random aspect selection + event-table gate fix guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.65 component refund production guard: FAIL: {m}")
def main():
    req("version = '11.63.65'" in read('build.gradle'),'build version')
    req('version="11.63.65"' in read('src/main/resources/META-INF/mods.toml'),'mods version')
    be=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    parity=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionShortageInstabilityParity.java')
    event=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionInstabilityEventTableParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')

    # lockedRecipeAspectTypes field and usage
    for token in ('private final List<Aspect> lockedRecipeAspectTypes',
                  'lockedRecipeAspectTypes.clear()',
                  'lockedRecipeAspectTypes.addAll(requiredAspects.keySet())',
                  'lockedRecipeAspectTypes.get(level.random.nextInt(lockedRecipeAspectTypes.size()))',
                  'pendingAspects.merge(extra, 1, Integer::sum)',
                  '"LockedRecipeAspectTypes"',
                  'Aspect.valueOf(lockedAspects.getCompound(i).getString("aspect"))'):
        req(token in be,f'missing token: {token[:50]}')

    # pullNextComponent refund gate uses ledger, not pendingAspects.isEmpty
    req('!lockedRecipeAspectTypes.isEmpty() && level.random.nextInt' in be,'pullNextComponent refund gate')
    # firstPendingAspect removed from refund paths
    req('InfusionProcessHelper.firstPendingAspect(pendingAspects)' not in be,'firstPendingAspect removed from refund paths')
    # drainEnchantmentXp refund uses TC4InfusionShortageInstabilityParity constant
    req('TC4InfusionShortageInstabilityParity.XP_REFUND_GATE_BOUND' in be,'XP refund uses named constant')

    # At least 4 clear() sites: activateMatrix, startInfusion, clearCraftingState, load
    req(be.count('lockedRecipeAspectTypes.clear();') >= 4,'at least 4 clear() calls')
    # NBT save/load presence
    req('ListTag lockedAspectsTag' in be,'save lockedAspects NBT')
    req('tag.contains("LockedRecipeAspectTypes")' in be,'load lockedAspects NBT')

    # Event table: gateAllows uses strict <
    req('clampedInstability > 0 && roll0to499 < clampedInstability' in event,'strict gateAllows')
    # countAssignedRolls now checks single-category membership
    req('int categories = 0' in event,'categories counter')
    req('if (categories == 1)' in event,'single-category gate')
    req('CONTRACT_VERSION = "11.63.65"' in event,'event-table contract version')

    # GameTests: fixed assertions
    req('!TC4InfusionInstabilityEventTableParity.gateAllows(0, 0)' in tests,'fixed gateAllows(0,0)')
    req('!TC4InfusionInstabilityEventTableParity.isCosmeticSurge(0)' in tests,'fixed isCosmeticSurge(0)')
    req('!TC4InfusionInstabilityEventTableParity.isCosmeticSurge(20)' in tests,'fixed isCosmeticSurge(20)')
    req('infusionComponentShortageRefundReachesProductionPendingAspects' in tests,'behavioral refund GameTest')
    req(tests.count('@GameTest(')in (72,73),'expected exactly 72 GameTests')

    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(manifest.get('version')=='11.63.65' and len(manifest.get('tests',[]))==400,'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.infusion_component_shortage_refund_reaches_production_contract',
              'gametest.infusion_essentia_shortage_retries_every_pending_aspect_contract',
              'gametest.infusion_component_shortage_first_index_always_refunds_contract',
              'gametest.infusion_instability_event_table_gate_contract'}
    req(expected<=ids,'manifest scenarios')
    print('TC4 v11.63.65 component refund production guard: PASS (72 GameTests, 400 scenarios)')
if __name__=='__main__': main()
