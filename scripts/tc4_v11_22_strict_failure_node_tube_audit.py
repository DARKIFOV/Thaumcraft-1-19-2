#!/usr/bin/env python3
from pathlib import Path
import sys
ROOT = Path(__file__).resolve().parents[1]

def read(path):
    return (ROOT / path).read_text(encoding='utf-8')

def require(cond, msg):
    if not cond:
        print(f"FAIL: {msg}", file=sys.stderr)
        sys.exit(1)

failure = read('src/main/java/com/darkifov/thaumcraft/infusion/TC4InfusionFailureParity.java')
instability = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java')
node = read('src/main/java/com/darkifov/thaumcraft/aura/AuraNodeWorldRuntime.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
readme = read('README.md')
ci = read('.github/workflows/main.yml')

require('TERMINAL_FAILURE_EVENT_PASSES = 1' in failure, 'terminal failure must run one TC4 weighted event pass')
require('TERMINAL_FAILURE_MAX_EVENT_PASSES = 1' in failure, 'terminal failure max passes must not over-scale')
require('TC4 line 366-387 does not repeat the switch' in failure, 'failure comment must document strict original behavior')
require('triggerWeightedEvent(level, matrixPos, owner, recipe, report, 0)' in failure, 'terminal failure must not feed severity into weighted event effects')
require('float strength = 1.5F + level.random.nextFloat();' in instability, 'case 9 explosion strength must match TC4 1.5F + random')
require('Math.min(0.75F' not in instability, 'severity-scaled explosion drift must be removed')

require('isTaintedBiomeLikeTC4' in node, 'aura node tainted biome gate missing')
require('type = AuraNodeType.TAINTED' in node, 'tainted biome random type conversion missing')
require('baura = Math.max(1, (int) (baura * 1.5F));' in node, 'tainted biome aura multiplier missing')
require(node.count('int stone = 0;') == 1, 'duplicate stone counter in node scan must not return')
require('createRandomWorldgenProfile(level, pos, random, false, false, false)' in node, 'natural nodes must still use random TC4 profile')

require('!tube.allowsInputFrom(direction.getOpposite())' in tube, 'venting must ignore non-connectable neighbour input faces')
require('false different-aspect venting conflict' in tube, 'tube venting fix must be documented')

require('v11.22' in readme and '88% complete / 12% remaining' in readme, 'README version/progress markers missing')
require('tc4_v11_22_strict_failure_node_tube_audit.py' in ci, 'CI must run v11.22 audit')
require('No new items, blocks, recipes, progression' in readme, 'no-new-content statement missing')
print('tc4_v11_22_strict_failure_node_tube_audit: OK')
