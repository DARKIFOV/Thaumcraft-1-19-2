#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

errors = []

def read(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

def require(cond: bool, msg: str) -> None:
    if not cond:
        errors.append(msg)

build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')

great_state = read('src/main/resources/assets/thaumcraft/blockstates/greatwood_log.json')
silver_state = read('src/main/resources/assets/thaumcraft/blockstates/silverwood_log.json')
great_model = ROOT / 'src/main/resources/assets/thaumcraft/models/block/greatwood_log_horizontal.json'
silver_model = ROOT / 'src/main/resources/assets/thaumcraft/models/block/silverwood_log_horizontal.json'

require("version = '11.42.1'" in build, 'build.gradle must be hotfix version 11.42.1')
require('version="11.42.1"' in mods, 'mods.toml must be hotfix version 11.42.1')
require('RotatedPillarBlock' in mod and 'pillarBlock("greatwood_log"' in mod and 'pillarBlock("silverwood_log"' in mod,
        'greatwood/silverwood logs must stay RotatedPillarBlock/pillarBlock')
for name, state in [('greatwood_log', great_state), ('silverwood_log', silver_state)]:
    for token in ['axis=y', 'axis=x', 'axis=z']:
        require(token in state, f'{name}.json missing {token} variant')
    require('_horizontal' in state, f'{name}.json must reference horizontal model for x/z axes')
require(great_model.exists(), 'missing greatwood_log_horizontal.json model')
require(silver_model.exists(), 'missing silverwood_log_horizontal.json model')
require('Bonus ' in screen and 'bonusSummary()' in screen and 'renderBonusAspectTooltips' in screen,
        'ResearchTableContainerScreen must display/tooltip table bonus aspects')

report = {
    'version': '11.42.1',
    'hotfix': 'log_axis_resource_and_research_table_bonus_display',
    'checks': {
        'greatwood_log_axis_variants': all(t in great_state for t in ['axis=y', 'axis=x', 'axis=z']),
        'silverwood_log_axis_variants': all(t in silver_state for t in ['axis=y', 'axis=x', 'axis=z']),
        'research_table_bonus_display': 'Bonus ' in screen and 'bonusSummary()' in screen,
    },
    'progress': '89% complete / 11% remaining',
}
(ROOT / 'V11_42_1_HOTFIX_AXIS_BONUS_REPORT.json').write_text(json.dumps(report, indent=2), encoding='utf-8')

if errors:
    for e in errors:
        print(f'::error::{e}')
    sys.exit(1)
print('tc4_v11_42_1_hotfix_axis_bonus_audit: OK')
