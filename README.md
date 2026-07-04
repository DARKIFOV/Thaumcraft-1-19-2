# Thaumcraft Legacy Rebuild 1.19.2 — Clean GitHub Build Package

## Что это

Чистая версия проекта для загрузки на GitHub.

В архиве оставлено только нужное для сборки:

- `.github/workflows/main.yml`
- `scripts/github_static_audit.py`
- `src/`
- `build.gradle`
- `settings.gradle`
- `.gitignore`
- `README.md`

Лишние папки и файлы удалены:

- `docs`
- `preview`
- `tools`
- `addon_compat_examples`
- старые инструкции
- старые changelog-файлы

## Как проверить

Открой файл:

`.github/workflows/main.yml`

Первая строка должна быть:

```yml
name: Forge 1.19.2 Build
```

Файл `.gitignore` должен лежать в корне проекта.

## Как загрузить на GitHub

1. Распакуй архив.
2. Открой распакованную папку.
3. Выдели всё содержимое папки.
4. На GitHub нажми `Add file → Upload files`.
5. Перетащи файлы.
6. Нажми `Commit changes`.
7. Открой `Actions → Forge 1.19.2 Build`.

## Если сборка упала

Скопируй ошибку из красного шага `Build Forge mod` и пришли её сюда.
