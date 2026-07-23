#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert any(f"version = '{v}'" in text('build.gradle') for v in ('11.63.40','11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))
assert any(f'version="{v}"' in text('src/main/resources/META-INF/mods.toml') for v in ('11.63.40','11.63.41', '11.63.42','11.63.43', '11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50'))

need('src/main/java/com/darkifov/thaumcraft/blockentity/TC4WandPedestalBlockEntity.java',
     'DRAIN_VISUAL_TICKS = CHARGE_INTERVAL + 2',
     '@Nullable private BlockPos drainSource',
     'recordDrain(nodePos, primal)',
     'recordDrain(nodePos, compound)',
     'drainedAspect.nativeColor()',
     'tag.putLong("DrainSource", drainSource.asLong())',
     'tag.putInt("DrainColor", drainColor)',
     'tag.putLong("DrainStartedAt", drainStartedAt)',
     'age <= DRAIN_VISUAL_TICKS',
     'new AABB(worldPosition).inflate(2.0D)')

need('src/main/java/com/darkifov/thaumcraft/client/render/TC4WandPedestalRenderer.java',
     'TC4_LINK_QUALITY = 16',
     'TC4_BEAM_WIDTH = 0.15F',
     'TC4_BEAM_SPEED = -0.02F',
     'Mth.sin((ticks % 32767.0F) / 16.0F) * 0.05F',
     '1.15D + bob',
     'rotationDegrees(ticks % 360.0F)',
     '1.65D - bob * 2.0F',
     'TC4AuraNodeHudParity.ORIGINAL_WISPY',
     'renderOriginalFloatyLine',
     'LightTexture.FULL_BRIGHT',
     'VertexConsumerHelper.beamQuad')

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert manifest['version'] in ('11.63.40','11.63.41', '11.63.42','11.63.43','11.63.44','11.63.45','11.63.46','11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.52', '11.63.53', '11.63.54', '11.63.55', '11.63.56', '11.63.58', '11.63.59', '11.63.60', '11.63.61')
assert len(manifest['tests']) >= 292
case = next(x for x in manifest['tests'] if x['id'] == 'visuals.tc4_wand_pedestal_wispy_drain')
assert 'wispy.png' in case['expected']
assert 'compound aspect color' in case['expected']

for wf in ('build.yml', 'release.yml'):
    assert 'python3 tools/tc4_116340_wand_pedestal_visual_guard.py' in text(f'.github/workflows/{wf}')
for artifact in ('TC4_11.63.40_WAND_PEDESTAL_VISUAL_PARITY_PORT_REPORT_RU.md',
                 'TC4_11.63.40_REMAINING_OBJECTS_AUDIT_RU.md',
                 'reports/remaining_objects_v11.63.40.json'):
    assert (root / artifact).is_file(), artifact
print('TC4 v11.63.40 wand pedestal visual parity guard: PASS')
