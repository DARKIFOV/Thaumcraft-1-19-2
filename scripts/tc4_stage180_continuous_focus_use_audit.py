#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

wand = read('src/main/java/com/darkifov/thaumcraft/block/WandItem.java')
focus = read('src/main/java/com/darkifov/thaumcraft/wand/WandFocusRuntime.java')
ember = read('src/main/java/com/darkifov/thaumcraft/entity/projectile/TC4EmberEntity.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
client_events = read('src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java')
renderer = read('src/main/java/com/darkifov/thaumcraft/client/render/TC4FocusProjectileRenderer.java')
workflow = read('.github/workflows/main.yml')
guard = read('scripts/github_ci_guard.py')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

for token in [
    'getUseDuration(ItemStack stack)',
    'getUseAnimation(ItemStack stack)',
    'onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration)',
    'releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int remainingUseDuration)',
    'player.startUsingItem(hand)',
    'player.startUsingItem(context.getHand())',
    'WandFocusRuntime.beginContinuousUse',
    'WandFocusRuntime.onUsingFocusTick',
    'WandFocusRuntime.onPlayerStoppedUsingFocus',
]:
    if token not in wand:
        errors.append(f'WandItem missing held-use hook {token}')

for token in [
    'shouldUseContinuously(ItemStack wandStack)',
    'type == WandFocusType.FIRE',
    'type == WandFocusType.SHOCK',
    'return type == WandFocusType.EXCAVATION',
    'onUsingFireFocusTick',
    'onUsingShockFocusTick',
    'onUsingExcavationFocusTick',
    'FIRE_SOUND_DELAY',
    'EXCAVATION_SOUND_DELAY',
    'EXCAVATION_BREAKCOUNT',
    'EXCAVATION_LAST_BLOCK',
    'TC4EmberEntity ember',
    'float scatter = fireBeam ? 0.25F : 15.0F',
    '2 + potency',
    'ember.setDuration(30)',
    'ember.setFirey(firey)',
    'TC4Sounds.event("fireloop")',
    'TC4Sounds.event("shock")',
    'TC4Sounds.event("rumble")',
    'chainShock(level, living',
    'float speed = excavationSpeed(state, potency)',
    'consumeFocusVis(wandStack, player, WandFocusType.EXCAVATION, cost)',
]:
    if token not in focus:
        errors.append(f'WandFocusRuntime missing continuous focus token {token}')

for token in [
    'Stage180 strict port shell for original EntityEmber used by ItemFocusFire',
    'private int duration = 20',
    'private int firey = 0',
    'setNoGravity(true)',
    'duration <= 20 ? 0.95D : 0.975D',
    'onGround() ? 0.66D : drag',
    'return 0.0D',
    'DamageSource.ON_FIRE',
    'living.setSecondsOnFire(3 + firey)',
    '0.025F * firey',
    'Blocks.FIRE.defaultBlockState()',
    'tag.putFloat("damage", damage)',
    'tag.putInt("firey", firey)',
    'tag.putInt("duration", duration)',
]:
    if token not in ember:
        errors.append(f'TC4EmberEntity missing original ember token {token}')

for token in ['FOCUS_EMBER', 'focus_ember', 'EntityType.Builder.<TC4EmberEntity>']:
    if token not in mod:
        errors.append(f'ThaumcraftMod missing ember registration token {token}')
if 'EntityRenderers.register(ThaumcraftMod.FOCUS_EMBER.get()' not in client_events:
    errors.append('ClientModEvents missing FOCUS_EMBER renderer registration')
for token in ['TC4EmberEntity', 'renderEmber', 'PARTICLES2', 'entity.tickCount % 8']:
    if token not in renderer:
        errors.append(f'projectile renderer missing ember render token {token}')

for forbidden in ['HEAVY_METAL']:
    if forbidden in focus:
        errors.append(f'WandFocusRuntime still contains incompatible 1.7.10 material token {forbidden}')

for token in ['tc4_stage180_continuous_focus_use_audit.py', 'python scripts/tc4_stage180_continuous_focus_use_audit.py', 'thaumcraft-legacy-rebuild-stage194-jars']:
    if token not in workflow or token not in guard:
        errors.append(f'workflow/guard missing {token}')

if "version = '1.94.0'" not in build or 'version="1.94.0"' not in mods:
    errors.append('project version must be 1.94.0')

if errors:
    for e in errors:
        print('::error::' + e)
    sys.exit(1)
print('Stage180 continuous focus use audit: OK')
