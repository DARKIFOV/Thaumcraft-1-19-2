# Thaumcraft Legacy Rebuild — Stage144 Clean GitHub Package

Forge 1.19.2 port checkpoint for the strict Thaumcraft 4 1.7.10 rebuild.

This package is cleaned for GitHub usage: the old generated Stage reports, historical audit scripts, and porting-doc clutter were removed from the repository root. The source code, resources, Gradle wrapper, workflow, and the current Stage144 audit remain.

## What was fixed

- Fixed GitHub compile error caused by duplicate `GOLEM_WIRELESS_BACKPACK` registry field.
- Fixed Crimson Cultist/Knight/Cleric/Praetor entity registration generics so Java does not infer `EntityType<Entity>`.
- Cleaned `.github/workflows/main.yml` so CI runs only the required current checks.
- Cleaned `scripts/` so only active GitHub checks remain.
- Removed old root `STAGE*.json`, `*_REPORT.json`, and historical `docs/porting` files from the upload package.

## Current active systems

Stage144 keeps the already ported systems and adds the Eldritch/Warp/Taint/Cultist pass:

- split Warp data: permanent, sticky, temporary, and warp counter;
- TC4-style Eldritch progression thresholds;
- Crimson cultist variants and Eldritch Guardian entity registration;
- Eldritch portal/altar flow using custom entities, not vanilla Enderman/Vex;
- shared taint runtime hooks;
- preserved golemancy, research, wands, nodes, Crucible, and Infusion code.

## GitHub upload

Upload the contents of this archive directly into the repository root. These files/folders must be at the top level:

- `build.gradle`
- `settings.gradle`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`
- `gradle/`
- `.github/`
- `scripts/`
- `src/`

Do not upload the extracted folder as a folder inside the repository.

## Local checks included

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_stage144_eldritch_warp_taint_audit.py
```

The full Gradle build should be run by GitHub Actions or another online environment because the local sandbox may not be able to download Gradle/Forge dependencies.
