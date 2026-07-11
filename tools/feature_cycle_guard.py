#!/usr/bin/env python3
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
biomes = root / 'src/main/java/com/darkifov/thaumcraft/world/TC4Biomes.java'
text = biomes.read_text(encoding='utf-8')
errors = []

# The runtime bridge owns TC4 surface population. Reintroducing shared vanilla
# placed features here can create a cross-biome ordering cycle on Forge 1.19.2.
generation_block = text.split('BiomeGenerationSettings.Builder generation', 1)[1].split('BiomeSpecialEffects effects', 1)[0]
if 'BiomeDefaultFeatures.add' in generation_block:
    errors.append('TC4Biomes reintroduced shared vanilla placed features into Magical Forest')
if 'FeatureCycleException' not in text:
    errors.append('feature-cycle rationale marker is missing from TC4Biomes')
if 'TC4WorldgenRuntime' not in text:
    errors.append('TC4 runtime worldgen ownership marker is missing')

if errors:
    print('Feature cycle guard: FAIL')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print('Feature cycle guard: OK')
