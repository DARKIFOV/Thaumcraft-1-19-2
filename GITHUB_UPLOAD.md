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


## Stage194 GitHub compile hotfix 2

Fixed the second GitHub compile failure from `logs_78043955500(1).zip`: dummy `quickMoveStack`, `ProjectileUtil.getHitResult` adapter, `this.onGround` field access, and `BonemealableBlock` `BlockGetter` signature. Added audit hardening for these API risks.
