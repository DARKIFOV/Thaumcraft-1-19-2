#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

keybinds = read('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectKeybinds.java')
events = read('src/main/java/com/darkifov/thaumcraft/client/ClientWandArchitectEvents.java')
packet = read('src/main/java/com/darkifov/thaumcraft/network/RequestWandArchitectTogglePacket.java')
network = read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
architect = read('src/main/java/com/darkifov/thaumcraft/wand/FocusArchitectRuntime.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'Stage179 client keybind adapter for original TC4 KeyHandler.miscWandToggle',
    'KEY_MISC_WAND_TOGGLE',
    'GLFW.GLFW_KEY_G',
    'key.categories.thaumcraft',
    'key.thaumcraft.misc_wand_toggle',
]:
    if token not in keybinds:
        errors.append(f'ClientWandArchitectKeybinds missing token {token}')

for token in [
    'Stage179 client adapter for original TC4 REHWandHandler/IArchitect preview',
    'InputEvent.Key',
    'requestWandArchitectToggleFromClient',
    'RenderGuiOverlayEvent.Post',
    'RenderLevelStageEvent',
    'RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS',
    'FocusArchitectRuntime.architectStatusLine',
    'FocusArchitectRuntime.pickedBlock',
    'FocusArchitectRuntime.equalTradeArchitectBlocks',
    'FocusArchitectRuntime.wardingArchitectBlocks',
    'WardedBlockRuntime.isWarded',
    'LevelRenderer.renderLineBox',
    'RenderType.lines()',
    'Preview blocks',
    'Picked:',
]:
    if token not in events:
        errors.append(f'ClientWandArchitectEvents missing overlay/key token {token}')

for token in [
    'Stage179 server packet adapter for original PacketItemKeyToServer key==1',
    'FocusArchitectRuntime.toggleMisc',
    'FocusArchitectRuntime.architectStatusLine',
    'TC4Sounds.event("wand")',
    'NetworkEvent.Context',
]:
    if token not in packet:
        errors.append(f'RequestWandArchitectTogglePacket missing token {token}')

for token in [
    'RequestWandArchitectTogglePacket.class',
    'RequestWandArchitectTogglePacket::encode',
    'RequestWandArchitectTogglePacket::decode',
    'RequestWandArchitectTogglePacket::handle',
    'requestWandArchitectToggleFromClient()',
]:
    if token not in network:
        errors.append(f'ThaumcraftNetwork missing Stage179 packet registration token {token}')

for token in ['architectStatusLine(ItemStack wandStack)', 'pickedBlock(ItemStack wandStack)', 'toggleMisc(ItemStack wandStack, Player player)']:
    if token not in architect:
        errors.append(f'FocusArchitectRuntime missing client-visible helper {token}')

for token in ['tc4_stage179_architect_client_overlay_audit.py', 'python scripts/tc4_stage179_architect_client_overlay_audit.py', 'thaumcraft-legacy-rebuild-stage204-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if not (("version = '2.04.0'" in build or "version = '1.98.0'" in build or "version = '2.00.0'" in build) and ('version="2.04.0"' in mods or 'version="1.98.0"' in mods or 'version="2.00.0"' in mods)):
    errors.append('project version must be 1.98.0 or 2.00.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage179 architect client overlay/keybind audit: OK')
