#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks = []

def require(path, *tokens):
    text = (ROOT / path).read_text(encoding='utf-8')
    missing = [t for t in tokens if t not in text]
    if missing:
        raise SystemExit(f"{path}: missing tokens: {missing}")
    checks.append((path, len(tokens)))

require('src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java',
        'travellingComponentSnapshot',
        'travellingComponentIndex',
        'componentStackStillMatchesLockedSource',
        'TravellingComponentSnapshot',
        'sourceStack',
        'removeTravellingComponentFromPending')
require('src/main/java/com/darkifov/thaumcraft/blockentity/ArcaneWorkbenchBlockEntity.java',
        'recipe.getRemainingItems(crafting)',
        'consumeVanillaCraftingMatrix',
        'applyCraftingRemainder',
        'ItemStack.isSameItemSameTags(stack, copy)')
require('src/main/java/com/darkifov/thaumcraft/aura/TC4AuraNodeScanParity.java',
        'THAUMOMETER_USE_DURATION_TICKS = 25',
        'THAUMOMETER_SCAN_RANGE = 10.0D',
        'comparingInt(AspectStack::amount).reversed()',
        'isWithinScanRange')
require('src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java',
        'getUseDuration',
        'UseAnim.NONE',
        'TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE',
        'TC4AuraNodeScanParity.isWithinScanRange')
require('src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java',
        'targetedNodeByOriginalScanRay',
        'NODE_REVEAL_RANGE = TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE',
        'new AABB(scan).inflate(0.35D)',
        'TC4AuraNodeScanParity.isWithinScanRange')
print('v8.22 infusion arcane node parity audit: OK')
for path, count in checks:
    print(f' - {path}: {count} tokens')
