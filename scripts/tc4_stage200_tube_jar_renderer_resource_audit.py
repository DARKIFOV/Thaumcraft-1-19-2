#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]
errors=[]

def require_file(rel):
    p=ROOT/rel
    if not p.exists(): errors.append(f'missing {rel}')
    return p

pack=require_file('src/main/resources/pack.mcmeta')
if pack.exists():
    try:
        data=json.loads(pack.read_text(encoding='utf-8'))
        if data.get('pack',{}).get('pack_format') != 9:
            errors.append('pack.mcmeta pack_format must be 9 for Minecraft 1.19.2')
    except Exception as exc:
        errors.append(f'pack.mcmeta invalid json: {exc}')

for rel, snippets in {
    'src/main/java/com/darkifov/thaumcraft/client/render/EssentiaTubeRenderer.java': [
        'TileTubeFilter/Restrict/Oneway/Buffer/Valve', 'ORIGINAL_LABEL_TEXTURE', 'textures/original/thaumcraft4/models/label.png',
        'bufferAspect', 'isFlowAllowed', 'isVenting'
    ],
    'src/main/java/com/darkifov/thaumcraft/client/render/EssentiaJarRenderer.java': [
        'ORIGINAL_LABEL_TEXTURE', 'renderJarLabel', 'jar.hasFilter()', 'jar.filterAspect()'
    ],
    'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java': [
        'EssentiaTubeRenderer', 'ESSENTIA_TUBE_BLOCK_ENTITY'
    ],
    'src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java': [
        'public Aspect aspectFilter()', 'public Aspect bufferAspect()', 'public boolean isFlowAllowed()', 'public boolean isVenting()'
    ],
}.items():
    p=require_file(rel)
    if p.exists():
        text=p.read_text(encoding='utf-8')
        for snippet in snippets:
            if snippet not in text:
                errors.append(f'{rel} missing {snippet}')

asset=ROOT/'src/main/resources/assets/thaumcraft'
for name in ['essentia_tube','essentia_tube_filter','essentia_tube_restrict','essentia_tube_oneway','essentia_tube_buffer']:
    for rel in [f'blockstates/{name}.json', f'models/block/{name}.json', f'models/item/{name}.json', f'textures/block/{name}.png']:
        require_file('src/main/resources/assets/thaumcraft/'+rel)
    block_model=(asset/'models/block'/f'{name}.json')
    if block_model.exists() and f'thaumcraft:block/{name}' not in block_model.read_text(encoding='utf-8'):
        errors.append(f'{name} block model does not point to its own texture')
    item_model=(asset/'models/item'/f'{name}.json')
    if item_model.exists() and f'thaumcraft:block/{name}' not in item_model.read_text(encoding='utf-8'):
        errors.append(f'{name} item model does not point to its own parent')

# Resolve model texture references, including this stage's subtype models.
missing=[]
for model in (asset/'models').rglob('*.json'):
    try:
        data=json.loads(model.read_text(encoding='utf-8'))
    except Exception as exc:
        errors.append(f'invalid model json {model}: {exc}')
        continue
    for value in data.get('textures',{}).values() if isinstance(data.get('textures',{}), dict) else []:
        if not isinstance(value,str) or value.startswith('#'):
            continue
        ns,path=(value.split(':',1) if ':' in value else ('thaumcraft',value))
        if ns=='minecraft':
            continue
        if ns!='thaumcraft':
            missing.append(f'{model}: bad namespace {value}')
            continue
        if not (asset/'textures'/(path+'.png')).exists():
            missing.append(f'{model}: missing texture {value}')
if missing:
    errors.extend(missing[:50])

if errors:
    for e in errors: print('::error::'+e)
    sys.exit(1)
print('Stage200 tube/jar renderer/resource audit: OK')
