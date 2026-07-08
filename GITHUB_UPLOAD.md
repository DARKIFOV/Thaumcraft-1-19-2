# GitHub upload note — Stage323–342 GITHUB_COMPILE_HOTFIX3

This package continues from Stage323–342 and fixes the GitHub Actions compile failure from logs_78317349221.zip.

## Fixed
- `MindSpiderEntity#getExperienceReward()` is now `public int getExperienceReward()`.
- This matches Forge/Minecraft 1.19.2 `Mob#getExperienceReward()` access and avoids the weaker-access override compile error.
- No TC4 gameplay mechanics, recipes, GUI behaviour, research data, aspects, textures, or progression were changed by this hotfix.

## Verified locally
- `python3 scripts/java_syntax_guard.py` — OK
- `python3 scripts/github_ci_guard.py` — OK
- `python3 scripts/github_static_audit.py` — OK
- `python3 scripts/tc4_stage153_recipe_materialization_parity_audit.py` — OK
- `python3 scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py` — OK

## Note
Gradle full build may still require GitHub Actions/online Gradle cache because the sandbox cannot download Gradle dependencies.
