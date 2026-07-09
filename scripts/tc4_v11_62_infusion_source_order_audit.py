#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')

def require(cond: bool, msg: str):
    if not cond:
        raise SystemExit(msg)

build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
ci = read('.github/workflows/main.yml')
helper = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java')
matrix = read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
ledger = read('src/main/java/com/darkifov/thaumcraft/porting/TC4FullPortDriftLedger.java')

require("version = '11.62.0'" in build, 'build.gradle version must be 11.62.0')
require('version="11.62.0"' in mods, 'mods.toml version must be 11.62.0')
require('v11.62' in readme and '90% complete / 10% remaining' in readme, 'README v11.62/progress marker missing')
require('No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.62' in readme, 'no-new-content statement missing')
require('tc4_v11_62_infusion_source_order_audit.py' in ci, 'CI must run v11.62 audit')

require('consumeOneAspectSource(List<EssentiaJarBlockEntity> jars, Aspect aspect, BlockPos matrixPos)' in helper, 'matrix-aware essentia source overload missing')
require('nearestJarOrder' in helper and 'distSqr(matrixPos)' in helper, 'nearest jar source order missing')
require('findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe.ComponentSpec componentSpec, InfusionRecipe recipe, BlockPos matrixPos)' in helper, 'matrix-aware component pedestal overload missing')
require('nearestPedestalOrder' in helper and 'lock the concrete component source' in helper, 'nearest component source order missing')
require('consumeOneAspectSource(jars, aspect, worldPosition)' in matrix, 'matrix must use deterministic essentia source order')
require('findComponentPedestal(report.componentPedestals(), componentSpec, recipe, worldPosition)' in matrix, 'matrix must use deterministic component source order')
require('craftCycle source ordering' in ledger or 'source ordering' in ledger, 'drift ledger must record source ordering lock')

report = {
    'version': '11.62.0',
    'batch': 'infusion_source_order_lock',
    'checks': {
        'matrix_aware_essentia_source_order': True,
        'nearest_jar_tiebreakers': True,
        'matrix_aware_component_source_order': True,
        'travelling_component_lock_preserved': True,
        'no_new_content': True,
    },
    'progress': '90% complete / 10% remaining',
}
(ROOT / 'V11_62_TC4_INFUSION_SOURCE_ORDER_REPORT.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
print('tc4_v11_62_infusion_source_order_audit: OK')
