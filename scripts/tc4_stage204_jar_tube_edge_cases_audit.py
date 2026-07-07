#!/usr/bin/env python3
from __future__ import annotations
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel: str) -> str:
    p = ROOT / rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

jar = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaJarBlockEntity.java')
jar_block = read('src/main/java/com/darkifov/thaumcraft/block/EssentiaJarBlock.java')
tube = read('src/main/java/com/darkifov/thaumcraft/blockentity/EssentiaTubeBlockEntity.java')
resolver = read('src/main/java/com/darkifov/thaumcraft/essentia/EssentiaSuctionResolver.java')
interaction = read('src/main/java/com/darkifov/thaumcraft/jar/JarTubeInteractionRuntime.java')
build = read('build.gradle')
mods = read('src/main/resources/META-INF/mods.toml')
pack = read('src/main/resources/pack.mcmeta')
report = json.loads(read('STAGE204_JAR_TUBE_EDGE_CASES_REPORT.json') or '{}')

checks = {
    'version_stage204': "version = '2.04.0'" in build and 'version="2.04.0"' in mods,
    'report_stage_204': report.get('stage') == 204 and report.get('version') == '2.04.0',
    'pack_metadata_preserved': 'pack_format' in pack and '9' in pack,
    'original_add_to_container': 'addToContainerOriginal' in jar and 'Returns the remainder' in jar,
    'original_take_from_container': 'takeFromContainerOriginal' in jar,
    'void_jar_consumes_overflow': 'voidJar ? 0 : amount - stored' in jar,
    'jar_original_suction_amount': 'originalSuctionAmount' in jar and '48' in jar and '64' in jar and '32' in jar,
    'phial_uses_original_add_take': 'addToContainerOriginal' in jar_block and 'takeFromContainerOriginal' in jar_block,
    'oneway_direction_methods': 'allowsInputFrom' in tube and 'allowsOutputTo' in tube and 'allowsNetworkTraversal' in tube,
    'tube_original_destination_suction': 'originalDestinationSuction' in tube and 'jar.originalSuctionAmount' in tube,
    'source_dest_direction_gates': 'tubeAllowsInput' in tube and 'tubeAllowsOutput' in tube,
    'resolver_uses_original_suction': 'jar.originalSuctionAmount(voidJar)' in resolver,
    'label_phial_filter_interaction_preserved': 'AspectFilter' in interaction and 'aspectFromPhial' in interaction,
}
errors = [name for name, ok in checks.items() if not ok]
if errors:
    for name in errors:
        print(f'::error::Stage204 audit failed: {name}')
    sys.exit(1)
print('Stage204 jar/tube edge-case parity audit: OK')
