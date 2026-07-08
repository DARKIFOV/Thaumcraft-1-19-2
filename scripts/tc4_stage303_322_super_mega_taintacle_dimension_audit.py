#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT = Path(__file__).resolve().parents[1]
checks = {
    'version_3_22_0': 'version="3.22.0"' in (ROOT/'src/main/resources/META-INF/mods.toml').read_text() and "version = '3.22.0'" in (ROOT/'build.gradle').read_text(),
    'taintacle_entity': (ROOT/'src/main/java/com/darkifov/thaumcraft/entity/TaintacleEntity.java').exists(),
    'taintacle_small_entity': (ROOT/'src/main/java/com/darkifov/thaumcraft/entity/TaintacleSmallEntity.java').exists(),
    'taintacle_renderer': (ROOT/'src/main/java/com/darkifov/thaumcraft/client/render/TC4TaintacleRenderer.java').exists(),
    'registry_taintacle': 'TAINTACLE =' in (ROOT/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java').read_text(),
    'registry_taintacle_small': 'TAINTACLE_SMALL =' in (ROOT/'src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java').read_text(),
    'renderer_registered': 'ThaumcraftMod.TAINTACLE.get()' in (ROOT/'src/main/java/com/darkifov/thaumcraft/client/ClientModEvents.java').read_text(),
    'giant_spawns_small': 'TAINTACLE_SMALL.get().create' in (ROOT/'src/main/java/com/darkifov/thaumcraft/entity/TaintacleGiantEntity.java').read_text(),
    'cultist_portal_render_state': 'getTc4RenderScale' in (ROOT/'src/main/java/com/darkifov/thaumcraft/entity/CultistPortalEntity.java').read_text(),
    'dimension_parity': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsDimensionParity.java').exists(),
    'chunk_provider_bridge': (ROOT/'src/main/java/com/darkifov/thaumcraft/eldritch/TC4OuterLandsChunkProviderBridge.java').exists(),
    'handoff_prompt': (ROOT/'docs/NEXT_CHAT_PROMPT_STAGE322.md').exists(),
}
print(json.dumps(checks, indent=2, sort_keys=True))
failed = [k for k, v in checks.items() if not v]
if failed:
    print('FAILED:', ', '.join(failed), file=sys.stderr)
    sys.exit(1)
