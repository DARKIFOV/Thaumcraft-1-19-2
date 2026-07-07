#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[1]
errors=[]
def read(p): return (ROOT/p).read_text(encoding='utf-8')
for rel in [
 'src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java',
 'src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java',
 'src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_gui_copy_stage166.json',
 'STAGE166_RESEARCH_TABLE_GUI_COPY_REPORT.json']:
    if not (ROOT/rel).exists(): errors.append(f'missing {rel}')
if not errors:
    screen=read(Path('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java'))
    packet=read(Path('src/main/java/com/darkifov/thaumcraft/network/RequestResearchTableActionPacket.java'))
    client=read(Path('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java'))
    net=read(Path('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java'))
    be=read(Path('src/main/java/com/darkifov/thaumcraft/blockentity/ResearchTableBlockEntity.java'))
    required={
      'container_screen':'extends AbstractContainerScreen<ResearchTableMenu>' in screen,
      'buttons':'requestResearchTableActionFromClient' in screen and 'Component.literal("New")' in screen and 'Component.literal("Copy")' in screen,
      'copy_gate':'RESEARCHDUPE' in be and 'copyCompletedResearchNote' in be,
      'packet_actions':'createResearchNote' in packet and 'openResearchNote' in packet and 'copyCompletedResearchNote' in packet,
      'screen_registered':'RESEARCH_TABLE_MENU' in client and 'ResearchTableContainerScreen::new' in client,
      'packet_registered':'RequestResearchTableActionPacket.class' in net,
      'version': any(f"version = '{v}'" in read(Path('build.gradle')) and f'version="{v}"' in read(Path('src/main/resources/META-INF/mods.toml')) for v in ['2.04.0', '1.98.0', '2.00.0', '2.02.0']),
    }
    for name, ok in required.items():
        if not ok: errors.append(f'failed {name}')
    data=json.loads(read(Path('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_research_table_gui_copy_stage166.json')))
    if data.get('stage')!=166: errors.append('stage166 json stage mismatch')
workflow=read(Path('.github/workflows/main.yml'))
guard=read(Path('scripts/github_ci_guard.py'))
for token in ['tc4_stage166_research_table_gui_copy_audit.py','python scripts/tc4_stage166_research_table_gui_copy_audit.py']:
    if token not in workflow and token not in guard and token not in read(Path('build.gradle')): errors.append(f'missing stage166 token {token}')
if errors:
    for e in errors: print('::error::'+e)
    sys.exit(1)
print('Stage168 research table GUI/copy audit: OK')
