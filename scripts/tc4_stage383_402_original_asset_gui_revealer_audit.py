#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(path, label):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing {label}: {path}")
    elif p.is_file() and p.stat().st_size <= 0:
        errors.append(f"empty {label}: {path}")
    return p

def text(path):
    return require(path, path).read_text(encoding='utf-8')

build = text('build.gradle')
if "version = '4.02.0'" not in build:
    errors.append('Stage383-402 must set project version to 4.02.0')

# Original TC4 key visual assets must be present in the active 1.19.2 pack, not only archived.
for path in [
    'src/main/resources/assets/thaumcraft/textures/gui/gui_arcaneworkbench.png',
    'src/main/resources/assets/thaumcraft/textures/gui/guiresearchtable2.png',
    'src/main/resources/assets/thaumcraft/textures/gui/hud.png',
    'src/main/resources/assets/thaumcraft/textures/gui/hex1.png',
    'src/main/resources/assets/thaumcraft/textures/gui/hex2.png',
    'src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png',
    'src/main/resources/assets/thaumcraft/textures/item/thaumometer.png',
    'src/main/resources/assets/thaumcraft/textures/item/scribing_tools.png',
    'src/main/resources/assets/thaumcraft/textures/item/research_note.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_fire.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_frost.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_shock.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_equal_trade.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_portable_hole.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_warding.png',
    'src/main/resources/assets/thaumcraft/textures/item/focus_primal.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_rod_wood.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_rod_greatwood.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_rod_silverwood.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_cap_iron.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_cap_gold.png',
    'src/main/resources/assets/thaumcraft/textures/entity/wand/wand_cap_thaumium.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/nodes.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/misc/node_bubble.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanner.png',
    'src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/goggles.png',
]:
    require(path, 'original visual parity asset')

thaumometer_model = json.loads(text('src/main/resources/assets/thaumcraft/models/item/thaumometer.json'))
if thaumometer_model.get('parent') != 'minecraft:builtin/entity':
    errors.append('Thaumometer item model must use minecraft:builtin/entity so the original scanner renderer is active')

thaumometer_item = text('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java')
if 'ThaumometerItemRenderer.instance()' not in thaumometer_item:
    errors.append('ThaumometerItem must install the original scanner custom renderer')

goggles_layer = text('src/main/java/com/darkifov/thaumcraft/client/render/TC4GogglesLayer.java')
if 'textures/original/thaumcraft4/models/goggles.png' not in goggles_layer:
    errors.append('Goggles layer must bind original TC4 models/goggles.png')
client_events = text('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
if 'new TC4GogglesLayer(renderer)' not in client_events:
    errors.append('ClientModEvents must register TC4GogglesLayer on player renderers')

workbench = text('src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java')
if 'textures/aspects/' not in workbench or 'original GuiArcaneWorkbench displays primal aspect icons' not in workbench:
    errors.append('Arcane Workbench must render original aspect icons instead of modern colored boxes')
if 'fill(poseStack, x, y, x + 16, y + 16, color)' in workbench:
    errors.append('Arcane Workbench still draws the old modern flat color aspect boxes')

wand_renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/WandItemRenderer.java')
if 'textures/entity/wand/' not in wand_renderer:
    errors.append('Wand renderer must still use the active wand texture path now populated with original TC4 textures')

aura_renderer = text('src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java')
for token in ['textures/original/thaumcraft4/misc/nodes.png', 'textures/original/thaumcraft4/misc/node_bubble.png']:
    if token not in aura_renderer:
        errors.append(f'AuraNodeRenderer lost original TC4 node texture binding: {token}')

if errors:
    print('Stage383-402 original asset/gui/revealer audit failed:')
    for err in errors:
        print(' -', err)
    raise SystemExit(1)
print('Stage383-402 original asset/gui/revealer audit: OK')
