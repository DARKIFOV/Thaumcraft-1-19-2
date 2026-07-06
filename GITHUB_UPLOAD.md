# Thaumcraft Legacy Rebuild — Stage144 GitHub Upload

Stage144 continues the strict TC4 1.7.10 → Forge 1.19.2 port. This checkpoint adds Eldritch progression, split Warp buckets, Crimson cultists, Eldritch Guardian, portal/altar flow and taint runtime while preserving the Stage143 Golemancy work.

GitHub loading fix: `github_ci_guard.py`, `mods.toml`, and `.github/workflows/main.yml` are now aligned to Stage144 / version `1.44.0`. The workflow also runs the new `tc4_stage144_eldritch_warp_taint_audit.py`, and artifact names no longer say Stage142.

Validation in this package: Java syntax guard, GitHub CI guard, static resource audit, Stage140–144 focused audits, Stage143 whole golemancy audit, and book/table/workbench audit pass locally. Full Gradle build still needs GitHub/online runner because the local sandbox cannot resolve services.gradle.org.

# Stage141 GitHub upload notes

Stage141 is still part of the local development chain toward the larger Stage140–159 checkpoint. It extends Stage140 Golemancy with controls, filters, marker assignment and upgrades.

Recommended checks before upload later:

- `python scripts/java_syntax_guard.py`
- `python scripts/github_ci_guard.py`
- `python scripts/github_static_audit.py`
- `python scripts/tc4_texture_audit.py`
- `python scripts/tc4_full_parity_audit.py`
- `python scripts/tc4_stage140_golemancy_checkpoint_audit.py`
- `python scripts/tc4_stage141_golemancy_controls_audit.py`

Do not call this a final release. It is a broad Golemancy parity step.
