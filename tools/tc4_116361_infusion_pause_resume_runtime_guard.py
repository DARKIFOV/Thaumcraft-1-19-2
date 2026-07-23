#!/usr/bin/env python3
"""v11.63.61 original Infusion Matrix pause/resume guard."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def read(r): return (ROOT/r).read_text(encoding="utf-8")

def version_tuple(text):
    import re
    found = [(11, int(a), int(b)) for a, b in re.findall(r"11\.(\d+)\.(\d+)", text)]
    return max(found, default=(0, 0, 0))
def req(c,m):
    if not c: raise SystemExit(f"TC4 v11.63.61 infusion pause/resume guard: FAIL: {m}")
def main():
    req(version_tuple(read('build.gradle')) >= (11, 63, 61), 'build version')
    req(version_tuple(read('src/main/resources/META-INF/mods.toml')) >= (11, 63, 61), 'mods version')
    matrix=read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
    parity=read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionPauseResumeParity.java')
    tests=read('src/main/java/com/darkifov/thaumcraft/gametest/TC4BlockEntityGameTests.java')
    req('pauseForInvalidStructure();' in matrix,'structure pause path')
    pause=matrix.split('private void pauseForInvalidStructure',1)[1].split('private void deactivateMatrix',1)[0]
    req('active = false;' in pause and 'clearCraftingState' not in pause,'pause preserves recipe state')
    req('reactivatePausedInfusion' in matrix and 'active = true;' in matrix.split('private boolean reactivatePausedInfusion',1)[1].split('private boolean activateMatrix',1)[0],'wand resume path')
    req('Infusion recipe lock no longer matches catalyst and component pedestals.' not in matrix,'no post-start recipe reselection failure')
    req('componentLayoutReselectsRecipeAfterStart()' in parity and 'return false;' in parity,'locked recipe parity')
    for method in ('infusionStructureLossPreservesLockedRecipe','infusionStructureLossAddsNoTerminalWarp','infusionReactivationResumesLockedCraft','infusionComponentLayoutDoesNotReselectRecipeAfterStart'):
        req(method in tests,f'missing {method}')
    req(tests.count('@GameTest(') >= 56, 'expected at least 56 cumulative GameTests')
    manifest=json.loads(read('runtime_artifacts/runtime_test_manifest.template.json'))
    req(version_tuple(str(manifest.get('version'))) >= (11, 63, 61) and len(manifest.get('tests', [])) >= 384, 'manifest version/count')
    ids={x.get('id') for x in manifest['tests']}
    expected={'gametest.infusion_structure_loss_preserves_recipe_contract','gametest.infusion_structure_loss_no_terminal_warp_contract','gametest.infusion_reactivation_resumes_locked_craft_contract','gametest.infusion_component_layout_no_reselection_contract'}
    req(expected<=ids,'manifest scenarios')
    print('TC4 v11.63.61 infusion pause/resume guard: PASS (56 GameTests, 384 scenarios)')
if __name__=='__main__': main()
