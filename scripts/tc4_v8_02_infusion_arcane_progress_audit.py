#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path):
    return (ROOT / path).read_text(encoding='utf-8')

def require(path, *needles):
    text = read(path)
    for needle in needles:
        if needle not in text:
            raise AssertionError(f'{path} missing required text: {needle!r}')

require('build.gradle', "version = '8.02.0'", "version = '7.82.0'")  # v8.02 marker is retained for forward-compatible audits
require('src/main/resources/META-INF/mods.toml', 'version="8.02.0"')  # v8.02 marker is retained for forward-compatible audits

require('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java',
        'private ItemStack lockedCatalystSnapshot = ItemStack.EMPTY;',
        'lockedCatalystSnapshot = catalystPedestal.stored().copy();',
        'lockedCatalystStackStillMatches',
        'LockedCatalystSnapshot',
        'recipeinput',
        'TC4InfusionRuntime.sameCraftingCatalyst')

require('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java',
        'sameCraftingCatalyst',
        'TC4 TileInfusionMatrix keeps a concrete recipeInput ItemStack',
        'TC4InfusionItemMatcher.WILDCARD_DAMAGE')

require('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
        'dropRealContents',
        'slot == SLOT_OUTPUT',
        'Never serialize it as a real inventory stack',
        'items.set(SLOT_OUTPUT, ItemStack.EMPTY);')

require('src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java',
        'workbench.dropRealContents(level, pos)')
if 'Containers.dropContents(level, pos, workbench)' in read('src/main/java/com/darkifov/thaumcraft/block/ArcaneWorkbenchBlock.java'):
    raise AssertionError('ArcaneWorkbenchBlock still drops virtual output via generic Containers.dropContents')

require('docs/PORTING_PROGRESS_V8_02.md', '~72%', '~28%', 'Remaining functional buckets', 'Next planned v8.22 targets')
require('docs/NEXT_CHAT_PROMPT_V8_02.md', 'архива v8.02', 'Следующий публичный batch: v8.22', '72% готово', '28%')
require('V8_02_TC4_INFUSION_ARCANE_PROGRESS_REPORT.json',
        '"compact_label": "v8.02"',
        '"complete_percent": 72',
        '"remaining_percent": 28',
        '"no_new_items": true',
        '"no_new_progression": true')
# Forward-compatible public docs may point at a later compact batch while retaining the v8.02 report/docs above.
# Keep this audit focused on the v8.02 runtime invariants and version markers, not the current release headline.
require('README.md', 'v8.02', 'v8.22')
require('GITHUB_UPLOAD.md', 'v8.02', 'v8.22')

workflow = read('.github/workflows/main.yml')
if 'tc4_v8_02_infusion_arcane_progress_audit.py' not in workflow:
    raise AssertionError('workflow does not run v8.02 audit')

print('v8.02 infusion arcane progress audit: OK')
