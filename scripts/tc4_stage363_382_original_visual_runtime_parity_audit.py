#!/usr/bin/env python3
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]

def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')

def require(condition: bool, message: str):
    if not condition:
        print(f"[Stage363-382 audit] FAIL: {message}")
        sys.exit(1)

build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
require("version = '3.82.0'" in build, 'build.gradle must be bumped to 3.82.0')
require('version="3.82.0"' in mods, 'mods.toml must be bumped to 3.82.0')

original_gui = read('src/main/java/com/darkifov/thaumcraft/client/screen/OriginalGuiTextures.java')
require('thaumcraft_core_original/gui_arcaneworkbench.png' in original_gui, 'Arcane Workbench must bind original TC4 gui_arcaneworkbench texture')

node_renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/AuraNodeRenderer.java')
for token in [
    'textures/original/thaumcraft4/misc/nodes.png',
    'textures/original/thaumcraft4/misc/node_bubble.png',
    'applyCameraBillboard(poseStack)',
    'renderFullTexturePlane',
]:
    require(token in node_renderer, f'AuraNodeRenderer missing TC4 node visual token: {token}')
require('textures/misc/nodes.png"' not in node_renderer, 'AuraNodeRenderer must not fall back to non-original nodes path')

revealer = read('src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java')
for token in [
    'textures/gui/thaumcraft_core_original/hud.png',
    'GuiComponent.blit(poseStack, x - 50, y - 23',
    'drawAspectIcon(poseStack, stack',
]:
    require(token in revealer, f'Revealer HUD missing original HUD token: {token}')
require('GuiComponent.fill(poseStack' not in revealer, 'Revealer HUD should not render the old modern debug rectangle')

research_table = read('src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java')
for token in [
    'renderBonusAspectsOriginalStyle',
    'OriginalGuiTextures.HEX1',
    'OriginalGuiTextures.HEX2',
    'not opaque modern square buttons',
]:
    require(token in research_table, f'ResearchTable screen missing original-style token: {token}')
require('Component.literal("Bonus " + bonus)' not in research_table, 'ResearchTable must not show modern Bonus text label')

matrix = read('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java')
for token in [
    'private BlockPos travellingComponentSource',
    'consumeTravellingComponentFromLockedPedestal',
    'sourcePedestal',
    'Do not silently consume a duplicate component from a different pedestal',
]:
    require(token in matrix, f'Infusion Matrix missing source-pedestal lock token: {token}')

report_path = ROOT / 'STAGE363_382_TC4_ORIGINAL_VISUAL_RUNTIME_PARITY_REPORT.json'
require(report_path.exists(), 'Stage363-382 report missing')
report = json.loads(report_path.read_text(encoding='utf-8'))
require(report.get('stage') == '363-382', 'Stage report must identify stage 363-382')
require(report.get('strict_original_tc4_parity') is True, 'Stage report must keep strict parity flag')
require((ROOT / 'docs/NEXT_CHAT_PROMPT_STAGE382.md').exists(), 'Next chat prompt for Stage382 missing')

print('[Stage363-382 audit] OK')
