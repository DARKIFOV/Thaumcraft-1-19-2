#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def read(p): return (ROOT/p).read_text(encoding='utf-8')
def exists(p):
    if not (ROOT/p).exists(): errors.append(f'missing {p}')
for rel in [
 'src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java',
 'src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java',
 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_block_entity_stage165.json',
 'STAGE165_RESEARCH_TABLE_BLOCK_ENTITY_REPORT.json']:
    exists(rel)
if not errors:
    be=read(Path('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java'))
    menu=read(Path('src/main/java/com/darkifov/thaumcraft/menu/ResearchTableMenu.java'))
    block=read(Path('src/main/java/com/darkifov/thaumcraft/block/ResearchTableBlock.java'))
    mod=read(Path('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java'))
    runtime=read(Path('src/main/java/com/darkifov/thaumcraft/research/ResearchTableInventoryRuntime.java'))
    required={
      'be_slot0':'SLOT_SCRIBING_TOOLS' in be,
      'be_slot1':'SLOT_RESEARCH_NOTE' in be,
      'be_persistent':'saveAdditional' in be and 'ContainerHelper.saveAllItems' in be and 'ContainerHelper.loadAllItems' in be,
      'be_create_note':'createResearchNote' in be and 'ResearchNoteState.initialize' in be,
      'block_base_entity':'extends BaseEntityBlock' in block and 'newBlockEntity' in block and 'NetworkHooks.openScreen' in block,
      'menu_slots':'ResearchTableBlockEntity.SLOT_SCRIBING_TOOLS' in menu and 'ResearchTableBlockEntity.SLOT_RESEARCH_NOTE' in menu,
      'registry_be':'RESEARCH_TABLE_BLOCK_ENTITY' in mod and 'ResearchTableBlockEntity::new' in mod,
      'registry_menu':'RESEARCH_TABLE_MENU' in mod and 'new ResearchTableMenu' in mod,
      'fallback_marked':'compatibility fallback' in runtime,
    }
    for name, ok in required.items():
        if not ok: errors.append(f'failed {name}')
    data=json.loads(read(Path('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_block_entity_stage165.json')))
    if data.get('stage')!=165: errors.append('stage165 json stage mismatch')
    if data.get('parity_contract',{}).get('slot_0') is None: errors.append('stage165 json missing slot contract')
workflow=read(Path('.github/workflows/main.yml'))
guard=read(Path('scripts/github_ci_guard.py'))
for token in ['tc4_stage165_research_table_block_entity_audit.py','python scripts/tc4_stage165_research_table_block_entity_audit.py','thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow and token not in guard: errors.append(f'missing workflow/guard token {token}')
if errors:
    for e in errors: print('::error::'+e)
    sys.exit(1)
print('Stage165 research table block entity audit: OK')
