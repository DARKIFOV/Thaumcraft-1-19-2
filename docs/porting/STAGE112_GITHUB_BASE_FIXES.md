# Stage 112 — GitHub base fixes for strict TC4 port

Goal: make the Forge 1.19.2 project safe to push to GitHub and build through GitHub Actions while keeping Stage111 TC4 source mapping intact.

## Changed

- Switched CI build command from system `gradle` to checked-in `./gradlew`.
- Added `chmod +x ./gradlew` in CI so ZIP/GitHub upload permission loss does not break Linux runners.
- Added `gradle/actions/setup-gradle@v4` for Gradle cache and wrapper validation.
- Kept Java pinned to 17 for Minecraft Forge 1.19.2.
- Pinned Forge dependency to `net.minecraftforge:forge:1.19.2-43.5.2`.
- Pinned ForgeGradle to `5.1.76` instead of dynamic `5.1.+`.
- Added NeoForged Maven releases fallback for ForgeGradle artifact resolution.
- Added `gradle.properties` for stable CI JVM/build behavior.
- Added `.gitattributes` so Gradle scripts use LF and binary files stay binary.
- Added `scripts/github_ci_guard.py` to verify GitHub-critical files and workflow snippets.
- Updated README and added `GITHUB_UPLOAD.md`.

## Validation run in sandbox

Passed:

```text
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
```

Not fully runnable in sandbox:

```text
./gradlew --no-daemon --version
./gradlew --no-daemon clean build --stacktrace
```

Reason: the sandbox cannot resolve `services.gradle.org`, so the Gradle wrapper cannot download `gradle-7.5.1-bin.zip`. GitHub Actions runners have network access, so this should proceed there.

## Next strict-port stage

After GitHub build confirms, continue with TC4 source-mapped porting:

1. Registry/class map for TC4 blocks/items/tile entities.
2. Original research JSON/page transfer.
3. Original recipes/aspect tags transfer.
4. GUI behavior parity for Thaumonomicon/research table/arcane workbench.
