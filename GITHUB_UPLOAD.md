# GitHub upload notes — Stage204

Stage204 version: `2.04.0`.

Expected GitHub artifact name:

`thaumcraft_legacy_rebuild_1.19.2-2.04.0-github.jar`

Run before upload:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage203_golem_ghost_slot_audit.py
python scripts/tc4_stage204_jar_tube_edge_cases_audit.py
```

Stage204 keeps the Stage198/Stage200 resource-pack fix. `pack.mcmeta` must remain present with `pack_format: 9` for Minecraft 1.19.2.

## Stage204 GitHub audit hotfix

Fixed GitHub Actions `Static source/resource audit` failure from the stale Stage161 version guard. The Stage161 research-note audit now accepts current Stage204 version `2.04.0` via semantic version parsing instead of old hard-coded version literals.

### Stage204 GitHub Compile Hotfix 2

- Fixed GitHub `compileJava` failure in `JarTubeInteractionRuntime.java`: lambda captured reassigned local `aspect`; now snapshots `final Aspect finalAspect` before `withStyle(...)`.
- No TC4 mechanics changed; Stage204 parity/resource pack fixes remain intact.
