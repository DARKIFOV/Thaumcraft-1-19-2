# Как запускать через GitHub

1. Распакуй архив Stage112.
2. Загрузи содержимое папки в корень GitHub-репозитория. Важно: не загружай саму папку внутрь другой папки — файлы `build.gradle`, `settings.gradle`, `gradlew` и `.github/workflows/main.yml` должны лежать в корне репозитория.
3. Сделай commit/push в ветку `main` или `master`.
4. Открой вкладку **Actions** в GitHub.
5. Дождись workflow **Forge 1.19.2 Build**.
6. Если сборка прошла, скачай jar во вкладке **Artifacts**: `thaumcraft-legacy-rebuild-stage112-jars`.

## Что проверяется автоматически

- наличие Gradle wrapper;
- Java 17;
- ForgeGradle pinned version;
- Forge 1.19.2 dependency;
- базовая Java syntax guard проверка;
- ресурсный audit: JSON, PNG, модели, blockstates, loot tables, GUI textures;
- сборка jar через `./gradlew clean build`.

## Если GitHub упадёт на первом запуске

Самая частая причина — репозиторий загружен не из корня. Проверь, что `.github/workflows/main.yml`, `build.gradle`, `settings.gradle`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar` находятся прямо в корне репозитория.
