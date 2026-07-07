# GitHub upload notes — Stage194

Stage194 version: `1.94.0`.

Run before upload:

- `python scripts/java_syntax_guard.py`
- `python scripts/github_static_audit.py`
- `python scripts/tc4_stage193_arcane_cleanup_audit.py`
- `python scripts/tc4_stage194_full_port_drift_ledger_audit.py`
- `python scripts/github_ci_guard.py`

Expected GitHub artifact names:

- `thaumcraft-legacy-rebuild-stage194-jars`
- `stage194-build-reports` on failure

Gradle remains pinned to Forge 1.19.2 / ForgeGradle 5.1.76 / Gradle 7.5.1.
