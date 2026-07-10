# GitHub upload — v11.62.24

Загрузите содержимое архива в корень репозитория без дополнительной внешней папки.

GitHub Actions выполняет:

1. общие source/resource guards;
2. 22 subsystem-аудита v11.62.2–v11.62.24;
3. установку Java 17;
4. Forge Gradle build;
5. reobfuscation;
6. проверку ресурсов итогового JAR;
7. выгрузку только `build/libs/*-github.jar`.

Игровой artifact:

```text
thaumcraft-legacy-rebuild-1.19.2-v11.62.24-github-jar
```

При ошибке дополнительно выгружается:

```text
v11.62.24-build-reports
```

Не помещайте одновременно несколько JAR этого мода в папку `mods`.
