#!/usr/bin/env python3
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
def text(path): return (root / path).read_text(encoding='utf-8')
def need(path, *tokens):
    value = text(path)
    for token in tokens:
        assert token in value, f'{path}: missing {token!r}'

assert "version = '11.63.43'" in text('build.gradle')
assert 'version="11.63.43"' in text('src/main/resources/META-INF/mods.toml')

need('src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyDuplicateItemMigrator.java',
     'MAX_NESTED_DEPTH = 4', 'migrateContainer(player.getEnderChestInventory())',
     'original.save(new CompoundTag())', 'serialized.putString("id", canonicalId.toString())',
     'ItemStack.of(serialized)', 'ForgeCapabilities.ITEM_HANDLER',
     'IItemHandlerModifiable', 'migrateStackDeepWithStatus',
     'Some automation wrappers expose IItemHandlerModifiable',
     'entity instanceof LivingEntity', 'EquipmentSlot.values()',
     'entity instanceof Container', 'migrateBlockEntity')
need('src/main/java/com/darkifov/thaumcraft/porting/TC4WorldMigrationSavedData.java',
     'extends SavedData', 'CURRENT_SCHEMA = 116343',
     'ProcessedChunks', 'MigratedStacks', 'MigratedBlockEntities',
     'computeIfAbsent', 'setDirty()', 'tag.putInt("Schema", CURRENT_SCHEMA)')
need('src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyWorldMigrationRuntime.java',
     'CHUNKS_PER_TICK = 2', 'queueLoadedChunk', 'drainDeferredChunkQueue',
     'getChunkNow(pos.x, pos.z)', 'chunk.getBlockEntities().values()',
     'level.getEntities((Entity) null, bounds, Entity::isAlive)',
     'data.markProcessed(pos, changedStacks, changedBlockEntities)')
need('src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java',
     'TC4LegacyWorldMigrationRuntime.queueLoadedChunk(level, chunk)',
     'TC4LegacyWorldMigrationRuntime.drainDeferredChunkQueue(level)',
     'TC4LegacyDuplicateItemMigrator.migratePlayerInventory(event.getEntity())')
need('src/main/java/com/darkifov/thaumcraft/porting/TC4LegacyStackMigrationTarget.java',
     'int migrateLegacyStacks()')
for path in (
    'src/main/java/com/darkifov/thaumcraft/blockentity/ArcanePedestalBlockEntity.java',
    'src/main/java/com/darkifov/thaumcraft/blockentity/ThaumatoriumBlockEntity.java',
    'src/main/java/com/darkifov/thaumcraft/blockentity/InfusionMatrixBlockEntity.java'):
    need(path, 'TC4LegacyStackMigrationTarget', 'migrateLegacyStacks()',
         'TC4LegacyDuplicateItemMigrator.migrateStackDeepWithStatus')

manifest = json.loads(text('runtime_artifacts/runtime_test_manifest.template.json'))
assert tuple(map(int, manifest['version'].split('.'))) >= (11, 63, 43)
assert len(manifest['tests']) >= 307
by_id = {case['id']: case for case in manifest['tests']}
for case_id in (
    'migration.loaded_chunk_container_sweep',
    'migration.player_ender_equipment_entities',
    'migration.nested_portable_item_handlers',
    'migration.chunk_budget_no_force_load',
    'migration.schema_bump_replay'):
    assert case_id in by_id, case_id
    assert by_id[case_id]['status'] == 'NOT_TESTED'
assert manifest['world_migration']['status'] == 'NOT_TESTED'

for wf in ('build.yml', 'release.yml'):
    workflow = text(f'.github/workflows/{wf}')
    assert any(f'--version {v}' in workflow for v in ('11.63.47','11.63.48', '11.63.49', '11.63.50', '11.63.46', '11.63.45', '11.63.44', '11.63.43', '11.63.51', '11.63.52', '11.63.53', '11.63.54', '11.63.55'))
    assert 'python3 tools/tc4_116343_world_migration_guard.py' in workflow

print('TC4 v11.63.43 deferred world migration and persistent chunk-ledger guard: PASS')
