from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
common = (ROOT / 'src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java').read_text(encoding='utf-8')
worldgen = (ROOT / 'src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java').read_text(encoding='utf-8')
build = (ROOT / 'build.gradle').read_text(encoding='utf-8')
mods = (ROOT / 'src/main/resources/META-INF/mods.toml').read_text(encoding='utf-8')
readme = (ROOT / 'README.md').read_text(encoding='utf-8')
ci = (ROOT / '.github/workflows/main.yml').read_text(encoding='utf-8')

def require(cond, msg):
    if not cond:
        print(f'::error::{msg}')
        sys.exit(1)

require("version = '11.62.2'" in build, 'build.gradle version must be 11.62.2')
require('version="11.62.2"' in mods, 'mods.toml version must be 11.62.2')
require('v11.62.2' in readme and 'infinite loading' in readme, 'README v11.62.2 world-load hotfix marker missing')
require('tc4_v11_62_2_integrated_server_world_load_hotfix_audit.py' in ci, 'CI must run v11.62.2 world-load hotfix audit')
require('queueNewChunk(level, chunk.getPos())' in common, 'ChunkEvent.Load must queue TC4 worldgen instead of running it synchronously')
require('drainDeferredChunkQueue(level)' in common, 'level tick must drain queued TC4 worldgen after world entry')
require('DEFERRED_CHUNKS' in worldgen and 'PENDING_CHUNKS' in worldgen, 'deferred worldgen queue/duplicate guard missing')
require('MAX_DEFERRED_CHUNKS_PER_TICK = 1' in worldgen, 'world-load hotfix must throttle deferred population to one chunk per tick')
require('level.players().isEmpty()' in worldgen, 'deferred worldgen must wait until at least one player has joined')
require('generateNewChunk(level, new ChunkPos(queued.x(), queued.z()))' in worldgen, 'queued worldgen must still invoke original generateNewChunk path')
require('TC4WorldgenSavedData.get(level).isProcessed(chunk)' in worldgen, 'queue must not reprocess already marked chunks')
require('ChunkEvent.Load' in worldgen and 'integrated-server bootstrap' in worldgen, 'startup hang context comment missing')

report = {
    'version': '11.62.2',
    'checks': [
        'active version bumped to 11.62.2',
        'ChunkEvent.Load queues TC4 worldgen',
        'level tick drains queue after player joins',
        'deferred queue is duplicate guarded and throttled',
        'original generateNewChunk surface pass remains the execution path',
        'already processed chunks are skipped before queueing'
    ]
}
(ROOT / 'V11_62_2_INTEGRATED_SERVER_WORLD_LOAD_HOTFIX_REPORT.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
print('TC4 v11.62.2 integrated-server world-load hotfix audit: OK')
