#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
read = lambda p: (ROOT / p).read_text(encoding='utf-8') if (ROOT / p).exists() else ''
exists = lambda p: (ROOT / p).exists()

def all_java_text() -> str:
    parts = []
    for p in (ROOT / 'src/main/java').rglob('*.java'):
        parts.append(f'// {p.relative_to(ROOT)}\n' + p.read_text(encoding='utf-8'))
    return '\n'.join(parts)

java = all_java_text()
network = read('src/main/java/com/darkifov/thaumcraft/network/ThaumcraftNetwork.java')
block = read('src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java')
client_events = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
container_screen = read('src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchContainerScreen.java')
client_hooks = read('src/main/java/com/darkifov/thaumcraft/client/ClientHooks.java')
mapping = read('src/main/resources/data/thaumcraft/tc4_source_mapping/tc4_to_1192_class_map.json')
be = read('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')

forbidden_runtime_tokens = [
    'RequestArcaneCraftPacket',
    'RequestArcaneMenuCraftPacket',
    'OpenArcaneWorkbenchPacket',
    'requestArcaneCraftFromClient',
    'requestArcaneMenuCraftFromClient',
    'ArcaneWorkbenchScreen',
]

checks = {
    'version_194': "version = '1.94.0'" in build and 'version="1.94.0"' in mods,
    'legacy_arcane_packet_files_removed': all(not exists(path) for path in [
        'src/main/java/com/darkifov/thaumcraft/network/RequestArcaneCraftPacket.java',
        'src/main/java/com/darkifov/thaumcraft/network/RequestArcaneMenuCraftPacket.java',
        'src/main/java/com/darkifov/thaumcraft/network/OpenArcaneWorkbenchPacket.java',
        'src/main/java/com/darkifov/thaumcraft/client/screen/ArcaneWorkbenchScreen.java',
    ]),
    'legacy_arcane_runtime_tokens_absent': all(token not in java for token in forbidden_runtime_tokens),
    'network_keeps_sync_only_no_browser_craft_packets': 'ArcaneRecipeSyncPacket' in network and all(token not in network for token in forbidden_runtime_tokens[:5]),
    'arcane_opens_container_menu_not_packet_screen': 'NetworkHooks.openScreen(serverPlayer, workbench' in block and 'syncArcaneRecipes(serverPlayer)' in block,
    'menu_screen_registered': 'MenuScreens.register(ThaumcraftMod.ARCANE_WORKBENCH_MENU.get(), ArcaneWorkbenchContainerScreen::new)' in client_events,
    'client_hooks_no_arcane_standalone_screen': 'openArcaneWorkbench' not in client_hooks and 'ArcaneWorkbenchScreen' not in client_hooks,
    'mapping_points_to_container_screen': 'ArcaneWorkbenchContainerScreen.java' in mapping and 'ArcaneWorkbenchScreen.java' not in mapping,
    'container_screen_documents_no_browser_button': 'There is no recipe browser, search field or client-side Craft button in TC4' in container_screen and 'new Button' not in container_screen and 'EditBox' not in container_screen,
    'hidden_legacy_catalyst_marked_migration_only': 'SLOT_LEGACY_CATALYST = 11' in be and 'migration adapter only' in be and 'older Stage135-188 saves' in be,
    'workflow_and_guard': 'tc4_stage193_arcane_cleanup_audit.py' in workflow and 'tc4_stage193_arcane_cleanup_audit.py' in guard,
}

errors = [name for name, ok in checks.items() if not ok]
if errors:
    for error in errors:
        print(f'::error::Stage193 Arcane Workbench cleanup audit failed: {error}')
    sys.exit(1)
print('Stage193 Arcane Workbench cleanup audit: OK')
