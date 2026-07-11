# GitHub upload — v11.62.43

1. Распаковать архив в чистую рабочую ветку. Не накладывать его поверх старого `main.zip`.
2. Убедиться, что в `.github/workflows` находятся только актуальные `build.yml` и `release.yml`.
3. Закоммитить весь пакет, включая два новых guard-а и отчёты.
4. Открыть Pull Request и дождаться зелёного `Build and audit`.
5. Скачать JAR из Artifacts и проверить в отдельной копии мира.
6. После теста создать тег `v11.62.43`; workflow соберёт Release ZIP.

Команда CI должна оставаться:

```bash
./gradlew build --stacktrace --no-daemon
```

Не возвращать `clean build`: в прошлой конфигурации `clean` удалял подготовленный ForgeGradle cache перед `compileJava`.

Перед тестом удалить из `mods` все другие JAR этого порта. Одновременно должен лежать только один `modId=thaumcraft`.
