#!/usr/bin/env python3
"""v11.63.62 original Infusion Matrix save/reload guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.62 infusion save/reload guard: FAIL: {m}")
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 62), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 62), 'mods version')
    matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    parity=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionSaveReloadParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('tag.putBoolean("active", active);' in matrix and 'tag.putBoolean("crafting", crafting);' in matrix,'active/crafting saved')
    req('active = tag.contains("Active") ? tag.getBoolean("Active") : tag.getBoolean("active");' in matrix,'active loaded')
    req('crafting = tag.contains("Crafting") ? tag.getBoolean("Crafting") : tag.getBoolean("crafting");' in matrix,'crafting loaded')
    for token in ('Aspects','recipein','recipeinput','recipeout','recipeinst','recipetype','recipexp','recipeplayer'):
        req(token in matrix,f'missing canonical NBT token {token}')
    for forbidden in ('TravellingComponentSourceX','TravellingComponentSnapshot','CountDelay','ItemCount'):
        req(f'tag.put{forbidden}' not in matrix, f'transient field must not be serialized: {forbidden}')
    req('CONTRACT_VERSION = "11.63.62"' in parity,'parity version')
    for method in ('saveReloadKeepsCraftingFlag','saveReloadKeepsLockedRecipe','saveReloadKeepsPendingEssentiaAndComponents','saveReloadRestartsComponentTravelCounter'):
        req(method+'()' in parity and 'return true;' in parity,f'parity true {method}')
    req('saveReloadKeepsTravellingComponentSource()' in parity and 'return false;' in parity.split('saveReloadKeepsTravellingComponentSource()',1)[1].split('}',1)[0], 'travelling source is transient')
    req('chunkUnloadCancelsInfusion()' in parity and 'return false;' in parity,'chunk unload does not cancel')
    for method in ('infusionSaveReloadKeepsCraftingFlag','infusionSaveReloadKeepsLockedRecipe','infusionSaveReloadKeepsPendingCosts','infusionSaveReloadRestartsTransientComponentTravel','infusionChunkUnloadDoesNotCancelCraft'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(') >= 61, 'expected at least 61 cumulative GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 62) and len(manifest.get('tests', [])) >= 389, 'manifest version/count')
    ids = {x.get('id') for x in manifest['tests']}
    expected={'gametest.infusion_save_reload_keeps_crafting_flag_contract','gametest.infusion_save_reload_keeps_locked_recipe_contract','gametest.infusion_save_reload_keeps_pending_costs_contract','gametest.infusion_chunk_unload_does_not_cancel_contract'}
    req(expected <= ids and ({'gametest.infusion_save_reload_restarts_transient_component_travel_contract','gametest.infusion_save_reload_keeps_travelling_source_contract'} & ids), 'manifest scenarios')
    print('TC4 v11.63.62 infusion save/reload guard: PASS (61 GameTests, 389 scenarios)')
if __name__=='__main__': main()
