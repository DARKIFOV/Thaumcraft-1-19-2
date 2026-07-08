#!/usr/bin/env python3
from pathlib import Path
import json
import sys
import re

ROOT = Path(__file__).resolve().parents[1]
errors = []

def require(path, needle, label=None):
    text = (ROOT / path).read_text(encoding='utf-8')
    if needle not in text:
        errors.append(f"{path}: missing {label or needle!r}")

def forbid(path, needle, label=None):
    text = (ROOT / path).read_text(encoding='utf-8')
    if needle in text:
        errors.append(f"{path}: forbidden {label or needle!r}")

def require_file(path):
    if not (ROOT / path).exists():
        errors.append(f"missing file: {path}")

build_text = (ROOT / 'build.gradle').read_text(encoding='utf-8')
mods_text = (ROOT / 'src/main/resources/META-INF/mods.toml').read_text(encoding='utf-8')
match = re.search(r"version\s*=\s*['\"]([0-9]+)\.([0-9]+)\.([0-9]+)['\"]", build_text)
if not match or tuple(map(int, match.groups())) < (2, 6, 0):
    errors.append('build.gradle: missing Stage206+ build version')
match = re.search(r'version="([0-9]+)\.([0-9]+)\.([0-9]+)"', mods_text)
if not match or tuple(map(int, match.groups())) < (2, 6, 0):
    errors.append('src/main/resources/META-INF/mods.toml: missing Stage206+ mods.toml version')
require_file('docs/TC4_ORIGINAL_PARITY_RULES_STAGE206.md')
require_file('docs/NEXT_CHAT_PROMPT_STAGE206.md')
require_file('STAGE206_TC4_ORIGINAL_PARITY_REPAIR_REPORT.json')

require('src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java', 'VIS_DISCOUNT = 5', 'TC4 5% vis discount')
require('src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java', 'showNodes', 'IRevealer showNodes adapter')
forbid('src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java', 'unlockResearch', 'fake research unlock')
forbid('src/main/java/com/darkifov/thaumcraft/block/GogglesOfRevealingItem.java', 'scan(', 'fake scan method')
forbid('src/main/java/com/darkifov/thaumcraft/block/HelmetOfRevealingItem.java', 'unlockResearch', 'fake helmet research unlock')
forbid('src/main/java/com/darkifov/thaumcraft/block/HelmetOfRevealingItem.java', 'scanNearby', 'fake helmet scan')

require('src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java', 'AuraNodeBlockEntity', 'node HUD')
forbid('src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java', 'Research: ', 'fake research HUD')
forbid('src/main/java/com/darkifov/thaumcraft/client/HelmetRevealingOverlayEvents.java', 'Warp: ', 'fake warp HUD')

require('src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java', 'textures/misc/nodes.png', 'original node sheet')
forbid('src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java', 'aura_node_sprite_', 'fake node sprites')

require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java', 'RESEARCH_TABLE_TC4_ORIGINAL', 'original research table texture')
require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java', 'ASPECTS_PER_PAGE = 25', 'TC4 5x5 aspect page')
forbid('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java', 'TC4 aspect base', 'debug text')
forbid('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java', 'Component.literal("Clear")', 'fake clear button')
forbid('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableScreen.java', 'Combinations:', 'debug combinations text')

require('src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java', 'HEX1', 'original hex texture 1')
require('src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java', 'HEX2', 'original hex texture 2')
require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java', 'OriginalGuiTextures.HEX1', 'hex1 usage')
require('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchNoteScreen.java', 'OriginalGuiTextures.HEX2', 'hex2 usage')

# Recipe checks
arcane = json.loads((ROOT/'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_goggles.json').read_text())
if arcane['key'] != {'L': 'minecraft:leather', 'G': 'minecraft:gold_ingot', 'T': 'thaumcraft:thaumometer'}:
    errors.append('tc4_goggles.json key no longer matches TC4 ConfigRecipes.java:1393')
if arcane['result']['item'] != 'thaumcraft:goggles_of_revealing':
    errors.append('tc4_goggles.json must craft primary goggles_of_revealing item')
thaumometer = json.loads((ROOT/'src/main/resources/data/thaumcraft/recipes/thaumometer.json').read_text())
if thaumometer['pattern'] != [' 1 ', 'IGI', ' 1 '] or thaumometer['key']['G']['item'] != 'minecraft:glass':
    errors.append('thaumometer.json no longer matches TC4 ConfigRecipes.java:3476')
infusion = json.loads((ROOT/'src/main/resources/data/thaumcraft/thaumcraft_arcane_workbench/tc4_infusionmatrix.json').read_text())
if infusion['key'] != {'S':'thaumcraft:arcane_stone','E':'minecraft:ender_eye','B':'#forge:shards/primal'}:
    errors.append('tc4_infusionmatrix.json key no longer matches TC4 ConfigRecipes.java:997 adapter mapping')
require_file('src/main/resources/data/forge/tags/items/shards/primal.json')
require_file('src/main/resources/assets/thaumcraft/textures/item/goggles_of_revealing.png')
forbid('src/main/java/com/darkifov/thaumcraft/research/ResearchRegistry.java', 'add("HELMET_REVEALING_SCAN"', 'fake helmet scan research node')
forbid('src/main/java/com/darkifov/thaumcraft/research/ResearchRegistry.java', 'add("HELMET_REVEALING_OVERLAY"', 'fake helmet overlay research node')

if errors:
    print('Stage206 original parity repair audit FAILED:')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('Stage206 original parity repair audit OK')
