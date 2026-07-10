# GitHub upload — v11.62.23

1. Распаковать архив в пустой репозиторий или полностью заменить содержимое текущей ветки.
2. Не загружать второй старый JAR мода в тот же `mods` каталог.
3. Запушить проект и открыть вкладку **Actions**.
4. Дождаться workflow сборки Java 17.
5. Скачать artifact:

```text
thaumcraft-legacy-rebuild-1.19.2-v11.62.23-github-jar
```

Внутри должен находиться только:

```text
build/libs/*-github.jar
```

До Forge-сборки workflow запускает:

- `github_static_audit.py`;
- `java_syntax_guard.py`;
- `github_ci_guard.py`;
- все 21 subsystem-аудит v11.62.2–v11.62.23;
- новый аудит Essentia Transport / Thaumatorium / Alchemical Infrastructure.

При ошибке сборки скачать:

```text
v11.62.23-build-reports
```

Локальная среда не смогла начать `compileJava`, поскольку Gradle Wrapper не получил distribution с `services.gradle.org`. Поэтому GitHub Actions остаётся обязательной компиляционной проверкой v11.62.23.
