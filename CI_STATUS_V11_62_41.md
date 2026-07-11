# v11.62.41 CI status — resolved

## What was actually wrong

The duplicate `ResearchTableScreen.java` and obsolete `.github/workflows/main.yml` were already removed.

The later GitHub Actions failure was caused by running ForgeGradle as:

```bash
./gradlew clean build --stacktrace --no-daemon
```

During project configuration ForgeGradle 5.1.76 creates the mapped Forge/Minecraft dependency under `build/fg_cache`. The `clean` task then deletes `build/`, including that freshly generated mapped JAR. `compileJava` subsequently loses the complete `net.minecraft` / `net.minecraftforge` classpath and reports hundreds of false `package ... does not exist` errors.

Both workflows now use:

```bash
./gradlew build --stacktrace --no-daemon
```

GitHub runners start from a clean checkout, so a separate `clean` is unnecessary. If a clean build is ever required locally, run two separate invocations:

```bash
./gradlew clean
./gradlew build --stacktrace --no-daemon
```

## Confirmed

- `.github/workflows/main.yml` is absent.
- `ResearchTableScreen.java` is absent.
- `build.yml` and `release.yml` no longer run `clean build` in one invocation.
- The supplied v11.62.41 JAR is a valid ZIP/JAR, targets Java 17, declares Forge 43+, and does not contain the obsolete `ResearchTableScreen.class`.
