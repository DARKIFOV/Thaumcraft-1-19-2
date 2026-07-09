# GitHub upload notes — v11.42.1 HOTFIX

Upload/replace the repository contents with this archive and push to `main`. This hotfix is intended to clear the current GitHub Actions failure.

Current failing annotation fixed:

```text
ResearchTableContainerScreen must display table bonus aspects
```

Main files changed:

- `src/main/java/com/darkifov/thaumcraft/client/screen/ResearchTableContainerScreen.java`
- `src/main/resources/assets/thaumcraft/blockstates/greatwood_log.json`
- `src/main/resources/assets/thaumcraft/blockstates/silverwood_log.json`
- `src/main/resources/assets/thaumcraft/models/block/greatwood_log_horizontal.json`
- `src/main/resources/assets/thaumcraft/models/block/silverwood_log_horizontal.json`
- `.github/workflows/main.yml`
- `scripts/tc4_v11_42_1_hotfix_axis_bonus_audit.py`
- `build.gradle`
- `src/main/resources/META-INF/mods.toml`

Required checks run locally:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage168_research_dupe_copy_audit.py
python3 scripts/tc4_stage170_research_table_bonus_sync_audit.py
python3 scripts/tc4_v11_42_node_failure_tube_golem_audit.py
python3 scripts/tc4_v11_42_1_hotfix_axis_bonus_audit.py
```

Progress remains **89% complete / 11% remaining**.

---

# GitHub upload notes — v11.42

Upload the contents of this folder to GitHub as the next compact batch after `v11.22`.

No new items, blocks, recipes, progression, GUI, or invented mechanics were added in v11.42.

## What changed

1. **Aura node value parity**
   - Silverwood/small worldgen nodes quarter biome aura like TC4.
   - Random node AspectList keeps initial seed weights before spread merge.
   - Node modifier no longer scales generated aspects; it remains stored node metadata.

2. **Infusion instability side effects**
   - `inEvWarp` now uses sticky warp +1 on the 25% branch and permanent warp 1..5 otherwise.
   - Flux goo/gas event side effects can replace replaceable space instead of only air.

3. **Tube connectability guards**
   - Destination suction type and destination counts respect closed/output-blocked sides.

4. **Sorting golem marked inventory parity**
   - Marked outputs include adjacent same-block container halves for double-chest style inventories.

## Required local checks

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_static_audit.py
python3 scripts/github_ci_guard.py
python3 scripts/tc4_v11_42_node_failure_tube_golem_audit.py
```

For a broader confidence pass, run the compact chain from v7.62 through v11.42.


## GitHub Actions hotfix 2

Fixed the Stage168 CI audit failure:

```text
RequestResearchTableActionPacket does not accept original container copy action id 5
```

`RequestResearchTableActionPacket` now explicitly accepts both copy action ids `3` and `5` before routing to `copyCompletedResearchNote(...)`.

Verified:

```bash
python3 scripts/java_syntax_guard.py
python3 scripts/github_ci_guard.py
python3 scripts/github_static_audit.py
python3 scripts/tc4_stage168_research_dupe_copy_audit.py
python3 scripts/tc4_v11_42_node_failure_tube_golem_audit.py
```

## Expected jar build

Use GitHub Actions or a local machine with network access for Gradle wrapper downloads. The sandbox may fail to resolve `services.gradle.org`.

## Compatibility markers retained

v11.42 v11.22 v11.02 v10.82 v10.62 v10.42 v10.22 v10.02 v9.82 v9.62 v9.42 v9.22 v9.02 v8.82 v8.62 v8.42 v8.22 v8.02 v7.82 v7.62
