# Stage194 GitHub compile hotfix

This is a compile-only hotfix for the GitHub Actions failure from `logs_78040595578(1).zip`.

## Failure

`compileJava` failed in `TC4RecipeItemResolver.java` because Java regex string literals used invalid single backslash escapes:

- `ConfigItems\.` was present in source as `ConfigItems\.` at runtime intent but written with a single Java escape form.
- `-?\d+` was written with an invalid Java `\d` escape instead of a Java string containing regex `\d`.

## Fix

The resolver regex strings now use Java-valid doubled backslashes so the runtime regex remains unchanged:

- `ConfigItems\\.`
- `ConfigBlocks\\.`
- `Items\\.`
- `Blocks\\.`
- `-?\\d+`

`java_syntax_guard.py` now scans Java string literals and fails on illegal Java escapes such as `\d` and `\.` before the Gradle build reaches `compileJava`.

## Parity impact

No TC4 behavior, data, recipes, GUI, assets, progression, foci, wand, golem, research, infusion, taint, worldgen, or aura logic was changed.
