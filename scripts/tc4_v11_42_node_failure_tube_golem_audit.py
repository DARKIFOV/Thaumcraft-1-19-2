#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(path):
    return (ROOT / path).read_text(encoding='utf-8')

def require(cond, msg):
    if not cond:
        raise SystemExit(msg)

aura = read('src/main/java/com/darkifov/thaumcraft/aura/AuraNodeWorldRuntime.java')
infusion = read('src/main/java/com/darkifov/thaumcraft/infusion/InfusionInstabilityEvents.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
golem = read('src/main/java/com/darkifov/thaumcraft/entity/ThaumGolemEntity.java')
readme = read('README.md')
ci = read('.github/workflows/main.yml')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')

require('baura = Math.max(2, baura / 4)' in aura, 'silverwood/small node quarter-aura safety floor missing')
require('entries.get(i).amount() + amount' in aura, 'node aspect seed weights must be retained before spread merge')
require('modifier.capacityScale()' not in aura.split('public static AuraNodeProfile createRandomWorldgenProfile',1)[1].split('private static boolean isTaintedBiomeLikeTC4',1)[0], 'worldgen node aspects must not be modifier-scaled')

require('PlayerThaumData.addWarpSticky(target, 1)' in infusion, 'infusion warp sticky branch missing')
require('PlayerThaumData.addWarpPermanent(target, 1 + level.random.nextInt(5))' in infusion, 'infusion permanent warp branch missing')
require('canBeReplaced()' in infusion and 'FLUX_GOO' in infusion and 'FLUX_GAS' in infusion, 'flux goo/gas replaceable placement missing')

require('|| !tubeAllowsOutput(tubePos, direction)' in tube, 'destination suction type must respect local output side')
require('countDirectTransportDestinations' in tube and 'if (!isSideOpen(direction)' in tube, 'destination count must skip closed sides')

require('addAdjacentSameBlockContainersLikeTC4' in golem, 'double-chest/large-inventory bridge missing for sorting golem')
require('InventoryLargeChest' in golem, 'TC4 large chest parity comment missing')

require("version = '11.42.0'" in build, 'build.gradle version missing')
require('version="11.42.0"' in mods, 'mods.toml version missing')
require('v11.42' in readme and '89% complete / 11% remaining' in readme, 'README v11.42/progress marker missing')
require('tc4_v11_42_node_failure_tube_golem_audit.py' in ci, 'CI must run v11.42 audit')
require('No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.42' in readme, 'no-new-content statement missing')

print('tc4_v11_42_node_failure_tube_golem_audit: OK')
