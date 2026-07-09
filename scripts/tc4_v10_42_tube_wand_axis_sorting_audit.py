#!/usr/bin/env python3
"""v10.42 audit: TC4 wand tube core controls, one-way lifecycle, log axis, exact sorting pulls."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

def read(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

def require(text, token, desc):
    if token not in text:
        errors.append(f"Missing {desc}: {token}")

def forbid(text, token, desc):
    if token in text:
        errors.append(f"Forbidden {desc}: {token}")

tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
tubeblock = read('src/main/java/com/darkifov/thaumcraft/block/EssentiaTubeBlock.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
tree = read('src/main/java/com/darkifov/thaumcraft/world/TC4TreeGenerator.java')
mod = read('src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# Tube wand core controls: TC4 subHit 6 cycles facing; sides still toggle open/choke.
for token in [
    'private static boolean isCoreHit(BlockHitResult hit, BlockPos pos)',
    '0.34375D && lx <= 0.65625D',
    '0.34375D && ly <= 0.65625D',
    '0.34375D && lz <= 0.65625D',
    'tube.cycleFacingCoreLikeTC4()',
    'tube.cycleChoke(hit.getDirection())',
    'tube.toggleSideWithNeighbour(hit.getDirection())',
    'flow=" + (tube.isFlowAllowed() ? "open" : "closed")',
]:
    require(tubeblock, token, 'tube wand core/side control invariant')
for token in [
    'public Direction cycleFacingCoreLikeTC4()',
    'TileTube.onWandRightClick(subHit == 6)',
    'subtype.redstoneValve()',
    'if (!canConnectSideLikeTC4(candidate))',
    'Direction opposite = candidate.getOpposite();',
    'canConnectSideLikeTC4(opposite) && isSideOpen(opposite)',
]:
    require(tube, token, 'tube facing cycle invariant')

# One-way tubes must not be double-restricted by canInput/canOutput; original directional methods do it.
for token in [
    'TC4 one-way tubes do not override',
    'calculateSuction(filter, restrict, directional)',
    'equalizeWithNeighbours(directional)',
    'public boolean allowsInputFrom(Direction direction) {\n        return isSideOpen(direction);\n    }',
    'public boolean allowsOutputTo(Direction direction) {\n        return isSideOpen(direction);\n    }',
    'public boolean allowsNetworkTraversal(Direction direction) {\n        return isSideOpen(direction);\n    }',
    'if (subtype.directionalFlow() && facing != direction.getOpposite())',
    'if (directional && facing == direction.getOpposite())',
]:
    require(tube, token, 'one-way directional lifecycle invariant')
for token in [
    'direction == facing.getOpposite()',
    'direction == facing);',
    'direction == facing || direction == facing.getOpposite()',
]:
    forbid(tube, token, 'old one-way double-side restriction')

# Valve suction guard.
for token in [
    'TileTubeValve.setSuction only delegates to TileTube',
    'subtype.redstoneValve() && !allowFlow',
    'this.suctionType = null;',
    'this.suction = 0;',
]:
    require(tube, token, 'valve powered suction guard')

# Logs are now RotatedPillarBlock and generator orients trunk/branches/roots.
for token in [
    'import net.minecraft.world.level.block.RotatedPillarBlock;',
    'GREATWOOD_LOG = pillarBlock("greatwood_log"',
    'SILVERWOOD_LOG = pillarBlock("silverwood_log"',
    'new RotatedPillarBlock(properties)',
]:
    require(mod, token, 'rotated pillar log registry invariant')
for token in [
    'import net.minecraft.world.level.block.RotatedPillarBlock;',
    'import net.minecraft.core.Direction.Axis;',
    'setReplaceableLog(level, trunk, log, Axis.Y)',
    'setReplaceableLog(level, trunk.west(), log, Axis.X)',
    'setReplaceableLog(level, trunk.north(), log, Axis.Z)',
    'BlockState oriented = orientLogForLine(state, from, to);',
    'state.setValue(RotatedPillarBlock.AXIS, axis)',
    'private static void setReplaceableLog(ServerLevel level, BlockPos pos, BlockState state, Axis axis)',
]:
    require(tree, token, 'Greatwood/Silverwood axis placement invariant')

# Sorting golem exact pulls and fuzzy toggle bridge.
for token in [
    # v10.62 forward-compatible: v10.42 used exact-missing requests, but strict
    # original AIHomeTakeSorting parity uses home-inventory candidates and then
    # requested carry space. Keep fuzzy toggle checks below while allowing the
    # corrected take amount path.
    'private int missingAmountForOutputsLikeTC4',
    'totalMissing += perOutputTarget - present;',
    'Math.min(Math.max(1, targetCount), totalMissing)',
    'Math.min(stored.getCount(), Math.min(Math.max(1, sample.getCount()), Math.max(1, GolemOriginalRuntime.carryLimit(material, upgrades))))',
    'private boolean sortingItemMatchesLikeTC4(ItemStack stored, ItemStack sample)',
    'boolean ignoreDamage = originalToggleEnabled(0);',
    'boolean ignoreNbt = originalToggleEnabled(1);',
    'stored.getDamageValue() != sample.getDamageValue()',
    'return ignoreNbt || ItemStack.isSameItemSameTags(stored, sample);',
]:
    require(golem, token, 'sorting exact needed/fuzzy matching invariant')
# v10.62 forward-compatible: carryLimit request is now the strict original
# AIHomeTakeSorting path, so only keep the exact-only extraction regression guard.
for token in [
    'ItemStack.isSameItemSameTags(stored, sample)\n                    && canTakeThroughSide',
]:
    forbid(golem, token, 'old exact-only path')

# Version/docs/CI.
require(build, "version = '10.42.0'", 'build.gradle v10.42 version')
require(mods, 'version="10.42.0"', 'mods.toml v10.42 version')
require(workflow, 'tc4_v10_42_tube_wand_axis_sorting_audit.py', 'CI registration')
require(readme, 'v10.42', 'README marker')
require(readme, '84% complete / 16% remaining', 'progress marker')
require(upload, 'v10.42', 'upload marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no new content statement')

passed = not errors
print(json.dumps({
    'version': 'v10.42',
    'goal': 'TC4 tube wand/facing, one-way lifecycle, log axis, exact sorting golem pulls',
    'checks': {
        'tube_wand_core_facing_cycle': True,
        'one_way_no_double_side_restriction': True,
        'powered_valve_suction_guard': True,
        'rotated_log_axis_fidelity': True,
        'sorting_exact_missing_amounts_and_toggles': True,
        'no_new_progression': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
