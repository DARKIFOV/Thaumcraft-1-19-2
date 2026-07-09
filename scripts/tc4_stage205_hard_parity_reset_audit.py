#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
root=Path(__file__).resolve().parents[1]
errors=[]
def require(path, text=None):
    p=root/path
    if not p.exists(): errors.append(f"missing {path}"); return ''
    s=p.read_text(errors='ignore')
    if text and text not in s: errors.append(f"{path} missing {text!r}")
    return s
build=require('build.gradle', "version = '2.05.0'")
require('docs/TC4_HARD_PARITY_RULES_STAGE205.md','Thaumcraft 4 for Minecraft 1.7.10 is the only source of truth')
require('STAGE205_HARD_PARITY_RESET_REPORT.json')
require('src/main/resources/assets/thaumcraft/textures/gui/gui_researchbook.png')
require('src/main/resources/assets/thaumcraft/textures/gui/thaumonomicon.png')
require('src/main/resources/assets/thaumcraft/textures/gui/guiresearchtable2.png')
require('src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png')
require('src/main/resources/assets/thaumcraft/textures/original/thaumcraft4/models/scanner.png')
rl=require('src/main/java/com/darkifov/thaumcraft/client/screen/OriginalResearchLayout.java','return unlocked(unlockedResearch, entry) || available(unlockedResearch, entry);')
ts=require('src/main/java/com/darkifov/thaumcraft/client/screen/ThaumonomiconScreen.java','do not inject fake client-side completions')
if 'synced.add("ASPECTS")' in ts or 'midX = x1' in ts: errors.append('Thaumonomicon still has fake unlocks or L-stick line code')
rt=require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java','has no rebuild')
if 'Component.literal("New")' in rt or 'Component.literal("Open")' in rt or 'Component.literal("Ink "' in rt: errors.append('Research table still renders fake buttons/debug labels')
rn=require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java','OriginalGuiTextures.RESEARCH_TABLE_TC4_ORIGINAL')
if 'Component.literal("Solve")' in rn or 'Path hint' in rn or 'Gold hexes' in rn: errors.append('Research note still exposes fake solver/debug UI')
grid=require('src/main/java/com/darkifov/thaumcraft/research/ResearchNoteGrid.java','return 169 +')
require('src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java','single trunk')
wg=require('src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java','for (int i = 0; i < 8; i++)')
mod=require('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java','legacy duplicate hidden from creative')
model=require('src/main/resources/assets/thaumcraft/models/item/thaumometer.json','original/thaumcraft4/models/scanner')
if errors:
    print('Stage205 hard parity reset audit failed:')
    for e in errors: print(' -',e)
    sys.exit(1)
print('Stage205 hard parity reset audit OK')
