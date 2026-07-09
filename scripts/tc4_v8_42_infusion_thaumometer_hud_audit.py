#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def require(rel, token, message):
    text = read(rel)
    if token not in text:
        errors.append(f"{rel}: missing {message}: {token}")

matrix = 'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java'
recipe = 'src/main/java/com/darkifov/thaumcraft/infusion/InfusionRecipe.java'
runtime = 'src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionRuntime.java'
helper = 'src/main/java/com/darkifov/thaumcraft/infusion/InfusionProcessHelper.java'
thaumometer = 'src/main/java/com/darkifov/thaumcraft/block/ThaumometerItem.java'
hud = 'src/main/java/com/darkifov/thaumcraft/client/TC4RevealerHudAdapter.java'
workflow = '.github/workflows/main.yml'

for token in [
    'pendingComponentSpecs',
    'orderedComponentSpecList(recipe, catalystPedestal.stored())',
    'TravellingComponentSnapshot',
    'PendingComponentSpecList',
    'pendingComponentSpecAt(travellingComponentIndex, travellingComponent)',
]:
    require(matrix, token, 'v8.42 infusion component spec ledger')

for token in [
    'public List<ComponentSpec> componentSpecsFor(ItemStack catalyst)',
    'public boolean componentMatches(ItemStack stack, ComponentSpec spec)',
    'TC4InfusionItemMatcher.matches(stack, spec.itemId(), spec.damage(), spec.tag(), true)',
]:
    require(recipe, token, 'component spec-aware matching')

for token in [
    'orderedComponentSpecList',
    'serializeComponentSpecs',
    'same item ids with different damage/NBT cannot be coalesced',
]:
    require(runtime, token, 'runtime component spec support')

for token in [
    'for (InfusionRecipe.ComponentSpec componentSpec : recipe.componentSpecsFor(catalyst))',
    'findComponentPedestal(List<ArcanePedestalBlockEntity> pedestals, InfusionRecipe.ComponentSpec componentSpec, InfusionRecipe recipe)',
]:
    require(helper, token, 'helper component spec matching')

for token in [
    'player.startUsingItem(hand)',
    'finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity)',
    'TAG_PENDING_BLOCK_SCAN',
    'performBlockScan(level, player, stack, pendingBlock)',
    'performEntityScan(player, stack',
    'TC4AuraNodeScanParity.THAUMOMETER_USE_DURATION_TICKS',
]:
    require(thaumometer, token, 'hold-to-scan thaumometer lifecycle')

for token in [
    'cachedTargetTick',
    'resolveTargetedNode(minecraft)',
    'look.distanceToSqr(cachedTargetLook)',
    'NODE_REVEAL_RANGE = TC4AuraNodeScanParity.THAUMOMETER_SCAN_RANGE',
]:
    require(hud, token, 'revealer HUD target cache')

require(workflow, 'tc4_v8_42_infusion_thaumometer_hud_audit.py', 'CI audit registration')

# Public no-new-content guard: this batch must not register new items/blocks or add new recipe JSON files.
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
if 'v8.42' in mod:
    errors.append('ThaumcraftMod.java should not contain v8.42 registration/content changes')

if errors:
    for e in errors:
        print('::error::' + e)
    raise SystemExit(1)
print('v8.42 infusion thaumometer hud audit: OK')
