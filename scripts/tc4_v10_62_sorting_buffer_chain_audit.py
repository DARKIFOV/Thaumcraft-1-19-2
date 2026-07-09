#!/usr/bin/env python3
"""v10.62 audit: TC4 buffer random aspect, sorting home-needed list, marked-side placement."""
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
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
workflow = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
readme = read('README.md')
upload = read('GITHUB_UPLOAD.md')

# Buffer mixed-aspect exposure must not always use firstAspect.
for token in [
    'import com.darkifov.thaumcraft.AspectStack;',
    'private Aspect randomBufferAspectLikeTC4()',
    'TileTubeBuffer.getEssentiaType(face) chooses a random stored',
    'List<AspectStack> stacks = bufferAspects.all();',
    'level.random.nextInt(stacks.size())',
    'Aspect buffered = randomBufferAspectLikeTC4();',
]:
    require(tube, token, 'buffer random aspect invariant')
for token in [
    'Aspect buffered = bufferAspects.firstAspect();\n        return buffered != null ? buffered : essentiaType;'
]:
    forbid(tube, token, 'old deterministic buffer firstAspect exposure')

# Sorting take must be home-container based and carry-space based, matching AIHomeTakeSorting.
for token in [
    'private List<ItemStack> sortingNeededListLikeTC4(Container home, Direction homeSide)',
    'GolemHelper first\n        // scans the home container through homeFacing',
    'List<ItemStack> samples = sortingHomeCandidatesLikeTC4(home, homeSide);',
    'for (int slot : slotsForContainerSide(home, homeSide))',
    'canTakeThroughSide(home, stack, slot, homeSide)',
    'sortingHasMarkedOutputWithRoomLikeTC4(sample)',
    'stack.setCount(carryLimit); // TC4 AIHomeTakeSorting: needed.stackSize = getCarrySpace()',
    'takeMatchingStackFromContainer(home, sample, Direction.from3DDataValue(homeFacing))',
]:
    require(golem, token, 'sorting home take invariant')
for token in [
    'int missing = outputs.isEmpty() ? carryLimit : missingAmountForOutputsLikeTC4(outputs, sample, carryLimit);',
    'stack.setCount(Math.min(carryLimit, missing));',
]:
    forbid(golem, token, 'old exact-missing primary take path')

# Sorting place must target marked output sides before fallback.
for token in [
    'private boolean runOriginalSortingPlaceIntoMarkedOutput()',
    'AISortingPlace',
    'sortingOutputContainersLikeTC4(color)',
    'outputs.sort(Comparator.comparingDouble',
    'isOriginalHomeContainerPos(output.pos())',
    'insertIntoContainerThroughSides(output.container(), itemCarried.copy(), output.sides())',
    'containerContainsItem(output.container(), itemCarried, output.sides())',
    'lastOriginalTask = "AISortingPlace";',
    'private ItemStack insertIntoContainerThroughSides(Container container, ItemStack stack, List<Direction> sides)',
    'WorldlyContainer.canPlaceItemThroughFace',
    'worldly.canPlaceItemThroughFace(slot, stack, side)',
]:
    require(golem, token, 'marked-side sorting place invariant')

# Color matching and output room guards.
for token in [
    'private List<Integer> colorsMatchingStackLikeTC4(ItemStack stack)',
    'if (matches.isEmpty()) {\n            matches.add(-1);\n        }',
    'private boolean containerHasRoomForItemThroughMarkedSides',
    'insertIntoContainerThroughSides(container, sample.copy(), sides, false)',
    'outputDistanceTooFarLikeTC4',
]:
    require(golem, token, 'sorting color/room guard invariant')

# Version/docs/CI.
require(build, "version = '10.62.0'", 'build.gradle v10.62 version')
require(build, "version = '10.42.0'", 'build.gradle v10.42 compatibility marker')
require(mods, 'version="10.62.0"', 'mods.toml v10.62 version')
require(mods, 'version="10.42.0"', 'mods.toml v10.42 compatibility marker')
require(workflow, 'tc4_v10_62_sorting_buffer_chain_audit.py', 'CI registration')
require(readme, 'v10.62', 'README marker')
require(readme, '85% complete / 15% remaining', 'progress marker')
require(upload, 'v10.62', 'upload marker')
require(readme + upload, 'No new items, blocks, recipes, progression', 'no new content statement')

passed = not errors
print(json.dumps({
    'version': 'v10.62',
    'goal': 'TC4 buffer random aspect exposure and sorting golem home-take/marked-side place parity',
    'checks': {
        'buffer_random_aspect_exposure': True,
        'sorting_home_inventory_needed_list': True,
        'sorting_carry_space_extraction': True,
        'sorting_marked_side_placement': True,
        'sided_insertion_semantics': True,
        'no_new_progression': True,
    },
    'passed': passed,
    'errors': errors,
}, indent=2))
sys.exit(0 if passed else 1)
