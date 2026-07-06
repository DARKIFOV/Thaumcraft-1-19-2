# Thaumcraft Legacy Rebuild — Stage143

Stage143 continues the strict TC4 1.7.10 → Forge 1.19.2 port. This checkpoint focuses on Golemancy live configuration, bell status workflow, marker radius/priority metadata, NBT persistence and a multi-part TC4-like golem renderer.

Validation: Java syntax guard, GitHub CI guard, static audit, texture audit, whole parity audit, wand audit, book/table/workbench audit, Stage137–142 focused audits all pass. Full Gradle build still needs GitHub/online runner because the local sandbox cannot resolve services.gradle.org.

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
