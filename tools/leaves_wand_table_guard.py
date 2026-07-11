#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / 'src/main/java/com/darkifov/thaumcraft'
assets = root / 'src/main/resources/assets/thaumcraft'
problems = []

leaves = (java / 'block/TC4MagicalLeavesBlock.java').read_text()
for token in ['extends LeavesBlock', 'ParticleTypes.DRIPPING_WATER',
              'kind == Kind.SILVERWOOD', 'random.nextInt(500)']:
    if token not in leaves:
        problems.append(f'magical leaves lost TC4/LeavesBlock behavior: {token}')

mod = (java / 'ThaumcraftMod.java').read_text()
for token in ['magicalLeavesBlock("greatwood_leaves"',
              'magicalLeavesBlock("silverwood_leaves"',
              'lightLevel(state -> 7)']:
    if token not in mod:
        problems.append(f'magical leaves registration drift: {token}')

for name in ['greatwood_leaves', 'silverwood_leaves']:
    state = json.loads((assets / f'blockstates/{name}.json').read_text())
    if 'multipart' not in state:
        problems.append(f'{name} blockstate does not cover LeavesBlock distance/persistent/waterlogged states')

wand = (java / 'client/render/WandItemRenderer.java').read_text()
for token in ['RenderType.entityTranslucent(rodTexture)',
              'renderModelBoxColor',
              '0, 8, 2, 18, 2',
              'poseStack.translate(0.0D, 0.50D, 0.0D)',
              'poseStack.translate(0.0D, 0.20D, 0.0D)']:
    if token not in wand:
        problems.append(f'wand renderer lost original ModelWand adapter: {token}')

block = (java / 'block/ResearchTableBlock.java').read_text()
table = (java / 'client/render/ResearchTableRenderer.java').read_text()
formation = (java / 'block/TableBlock.java').read_text()
client = (java / 'client/ClientModEvents.java').read_text()
for token in ['public static final DirectionProperty FACING', 'public static final BooleanProperty PRIMARY', 'RenderShape.INVISIBLE']:
    if token not in block:
        problems.append(f'research table block drift: {token}')
for token in ['textures/original/thaumcraft4/models/restable.png',
              'textures/original/thaumcraft4/models/restable2.png',
              'renderParchmentStack', 'renderQuill', 'renderResearchScroll',
              'case NORTH -> 270.0F', 'case SOUTH -> 90.0F']:
    if token not in table:
        problems.append(f'research table renderer drift: {token}')
for token in ['setValue(ResearchTableBlock.FACING, direction)',
              'setValue(ResearchTableBlock.PRIMARY, false)',
              'level.setBlock(other, partnerState, 3)',
              'SLOT_SCRIBING_TOOLS', 'installedTools']:
    if token not in formation:
        problems.append(f'research table formation drift: {token}')
if 'RESEARCH_TABLE_BLOCK_ENTITY.get(), ResearchTableRenderer::new' not in client:
    problems.append('research table BER is not registered on the Forge client bus')

if problems:
    print('Leaves/Wand/Research Table guard: FAILED')
    for problem in problems:
        print(' -', problem)
    raise SystemExit(1)
print('Leaves/Wand/Research Table guard: OK (LeavesBlock states, ModelWand UVs, original two-block BER)')
