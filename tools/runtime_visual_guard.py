#!/usr/bin/env python3
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
resources = root / 'src/main/resources/assets/thaumcraft'
problems = []

for relative in ['models/block/infusion_matrix.json', 'models/block/node_transducer.json']:
    data = json.loads((resources / relative).read_text())
    if data.get('elements') != []:
        problems.append(f'{relative} still renders a placeholder cube behind its BER')

research = json.loads((resources / 'models/block/research_table.json').read_text())
research_item = json.loads((resources / 'models/block/research_table_item.json').read_text())
renderer = (root / 'src/main/java/com/darkifov/thaumcraft/client/render/ResearchTableRenderer.java')
if research.get('elements'):
    problems.append('research table still renders a duplicate JSON model behind its BER')
if not renderer.exists():
    problems.append('research table BER is missing')
else:
    renderer_text = renderer.read_text()
    for token in ['textures/original/thaumcraft4/models/restable.png',
                  'renderBaseModel', 'renderParchmentStack', 'renderResearchScroll']:
        if token not in renderer_text:
            problems.append(f'research table BER lost original runtime component: {token}')
elements = research_item.get('elements', [])
if not elements:
    problems.append('research table inventory model is empty')
else:
    low = min(float((element.get('from') or [0])[0]) for element in elements)
    high = max(float((element.get('to') or [0])[0]) for element in elements)
    if low > -8 or high < 24:
        problems.append('research table inventory model no longer spans the original two-block 32px top')

overlay = (root / 'src/main/java/com/darkifov/thaumcraft/client/EssentiaOverlayEvents.java').read_text()
if 'import net.minecraftforge.client.event.RenderGuiOverlayEvent' in overlay or '@SubscribeEvent' in overlay:
    problems.append('essentia containers still create a screen-space fake Thaumometer popup')
jar = (root / 'src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java').read_text()
if 'RevealerAspectTagRenderer.renderSingle' not in jar:
    problems.append('jar does not render original-style world aspect tags')

empty = resources / 'textures/models/armor/tc4_empty_layer_1.png'
if not empty.exists():
    problems.append('transparent vanilla armor suppression texture missing')
else:
    digest = hashlib.sha256(empty.read_bytes()).hexdigest()
    if digest != '050ab82bd34987f71468d5851d3707e15c6c6de862d98384259834d92deefd57':
        problems.append('armor suppression texture is not the known fully transparent 64x32 PNG')
for name in ['GogglesOfRevealingItem.java', 'HelmetOfRevealingItem.java']:
    text = (root / 'src/main/java/com/darkifov/thaumcraft/block' / name).read_text()
    if 'textures/models/armor/tc4_empty_layer_1.png' not in text:
        problems.append(f'{name} can render the vanilla gold helmet cube')

transducer = (root / 'src/main/java/com/darkifov/thaumcraft/blockentity/NodeTransducerBlockEntity.java').read_text()
if 'pos.below()' not in transducer:
    problems.append('node transducer searches above itself instead of the original node-below stack')

if problems:
    print('Runtime visual guard: FAILED')
    for problem in problems:
        print(' -', problem)
    raise SystemExit(1)
print('Runtime visual guard: OK (BER-only matrix/transducer/table, two-block item, world tags, transparent armor)')
