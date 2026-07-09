# v11.62.2 — Integrated Server World Load Hotfix

Upload this archive over the current repository and push to GitHub.

## Fix

This hotfix targets the infinite `Loading terrain...` / stuck world-entry screen after v11.62.1.

Root cause: TC4 surface worldgen was executed synchronously from `ChunkEvent.Load`. During integrated-server bootstrap, spawn chunks are still being prepared while the client is waiting on the terrain join future. Running Greatwood/Silverwood/Aura surface population directly inside that event can keep the load phase stuck.

## Changed files

- `src/main/java/com/darkifov/thaumcraft/event/CommonEvents.java`
  - `ChunkEvent.Load` now queues chunk population instead of running it immediately.
  - `LevelTickEvent` drains the queue safely after the level has at least one player.

- `src/main/java/com/darkifov/thaumcraft/world/TC4WorldgenRuntime.java`
  - Added duplicate-guarded deferred chunk queue.
  - Drains at most one chunk per tick.
  - Preserves `generateNewChunk(...)` as the only real surface population path.

- `build.gradle`
  - Version bumped to `11.62.2`.

- `src/main/resources/META-INF/mods.toml`
  - Mod version bumped to `11.62.2`.

- `scripts/tc4_v11_62_2_integrated_server_world_load_hotfix_audit.py`
  - Added audit coverage for this hotfix.

## Not changed

- No new items.
- No new blocks.
- No new recipes.
- No progression changes.
- v11.62 infusion source ordering is unchanged.
- v11.62.1 RecipeManager startup crash guard is unchanged.

## Local checks

- `java_syntax_guard.py` — OK
- `github_ci_guard.py` — OK
- `github_static_audit.py` — OK
- `tc4_v11_62_infusion_source_order_audit.py` — OK
- `tc4_v11_62_1_integrated_server_startup_hotfix_audit.py` — OK
- `tc4_v11_62_2_integrated_server_world_load_hotfix_audit.py` — OK

Gradle build was not run in the sandbox because Forge/Gradle dependencies require external network access.
