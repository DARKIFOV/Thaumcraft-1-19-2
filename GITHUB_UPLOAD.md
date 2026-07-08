# GitHub upload notes — Stage209

Stage209 version: `2.09.0`.

Expected GitHub artifact name:

`thaumcraft_legacy_rebuild_1.19.2-2.09.0-github.jar`

Run before upload:

```bash
python scripts/java_syntax_guard.py
python scripts/github_static_audit.py
python scripts/github_ci_guard.py
python scripts/tc4_stage205_hard_parity_reset_audit.py
python scripts/tc4_stage206_original_parity_repair_audit.py
python scripts/tc4_stage207_infusion_matrix_parity_audit.py
python scripts/tc4_stage208_infusion_renderer_enchantment_audit.py
```

Stage209 keeps the Stage205/206 hard original-parity rules and continues Stage207 Infusion Matrix work with an animated matrix renderer, original infusion enchantment runtime adapter, XP-drain cycle, enchantment/NBT output handling and recipe-aware component pulling.

Important install note: remove old jars from the Minecraft `mods` folder, especially 1.94.0, 2.04.0, 2.05.0, 2.06.0 and 2.07.0 jars, before testing the new jar. If old jars remain, Minecraft can keep loading the old broken version.


## Stage342 GitHub compile/static hotfix 2

Upload this hotfix ZIP after the Stage342 failed GitHub Actions run. It fixes the compile errors reported in `logs_78314132650.zip` and keeps the Stage153 infusion selection guard strict: no catalyst-only fallback after component-pedestal matching fails.

Local sandbox checks passed: Java syntax guard, GitHub CI guard, static source/resource audit, Stage153 audit, all Python audit steps listed in `.github/workflows/main.yml` through Stage215, and Stage323–342 cleanup audit. Gradle build could not be completed locally because the wrapper needs the Gradle 7.5.1 distribution and sandbox DNS/network is unavailable.
