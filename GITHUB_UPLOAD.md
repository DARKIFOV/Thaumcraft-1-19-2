# GitHub upload instructions — Stage144 Clean Package

This is the cleaned Stage144 GitHub package for Forge 1.19.2.

## Fixed from the latest GitHub log

The failing GitHub build log showed:

- duplicate `GOLEM_WIRELESS_BACKPACK` in `ThaumcraftMod.java`;
- Crimson Cultist entity registrations inferred as `EntityType<Entity>` instead of `EntityType<CrimsonCultistEntity>`.

Both are fixed in this package.

## Removed as unnecessary

The upload package no longer includes old generated Stage JSON reports, obsolete Stage137–143 audit scripts, or historical `docs/porting` files. This keeps the GitHub repo clean and prevents stale stage checks from breaking the current build.

## Correct upload layout

Put the archive contents directly into the repository root:

```text
.github/
gradle/
scripts/
src/
build.gradle
settings.gradle
gradle.properties
gradlew
gradlew.bat
README.md
GITHUB_UPLOAD.md
```

Do not upload the extracted folder itself into GitHub.

## Checks run by GitHub Actions

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_stage144_eldritch_warp_taint_audit.py
./gradlew --no-daemon clean build --stacktrace
```

## Stage144 hotfix: GitHub compile SoundEvent fix

Fixed Forge 1.19.2 GitHub compile failure in `TC4Sounds`: replaced newer `SoundEvent.createVariableRangeEvent(id)` with the 1.19.2-compatible `new SoundEvent(id)`. Also added a static audit rule so this API mismatch does not return in later stages.
