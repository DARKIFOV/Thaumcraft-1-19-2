# Stage113 — Provided 1.19.2 Reboot Base Integration

## Зачем этот stage

Пользователь правильно указал, что были даны уже существующие версии Thaumcraft под Minecraft 1.19.2.
Этот stage фиксирует правильную архитектуру порта:

- **Forge 1.19.2 reboot archives** = современная база/каркас: Gradle, Forge layout, mod id `thaumcraft`, registry style, resource layout.
- **Thaumcraft 4 1.7.10 decompiled source** = строгий источник поведения: аспекты, исследования, GUI, рецепты, таумометр, ноды, аура, инфузия, жезлы, прогрессия.
- **Active Stage112 codebase** = текущая рабочая 1.19.2 ветка, которую нельзя слепо заменить, потому что в ней уже больше перенесенных файлов.

## Что реально лежит в предоставленных 1.19.2 архивах

| Архив | Роль | Java files | Resource files | Решение |
|---|---:|---:|---:|---|
| `thaumcraftreboot-main` | primary 1.19.2 Forge base/reference | 4 | 27 | Использовать как base reference, не как полный replacement |
| `thaumcraft-reboot-fbe41a...` | secondary 1.19.2 skeleton | 2 | 2 | Использовать только для Gradle/Forge skeleton comparison |
| active Stage113 | текущая рабочая ветка | 229 | 3317 | Оставить активной и постепенно приводить к strict TC4 |

## Почему я не должен был просто заменить Stage110/112 на `thaumcraftreboot-main`

`thaumcraftreboot-main` действительно под 1.19.2, но он содержит только маленький стартовый набор:

- `thaumcraft.Thaumcraft`
- `thaumcraft.api.items.ThaumcraftCreativeModeTab`
- `thaumcraft.api.items.ThaumcraftItems`
- `thaumcraft.common.block.ThaumcraftBlocks`
- `table_wood` resources
- `brain` item model/texture/sounds

Если заменить им текущий проект полностью, мы потеряем большую часть уже существующего Stage110/112: блоки, предметы, GUI, сети, research, essentia, infusion, wand, blockentities и assets.

## Что изменено в Stage113

1. Добавлен явный source reference для двух предоставленных 1.19.2 архивов:
   - `docs/source_refs/provided_1192_reboots/thaumcraftreboot-main/`
   - `docs/source_refs/provided_1192_reboots/thaumcraft-reboot-fbe41a/`

2. Добавлен inventory всех файлов из предоставленных 1.19.2 архивов:
   - `docs/porting/provided_1192_reboot_file_inventory.csv`

3. Добавлена Java-карта базы:
   - `src/main/java/com/darkifov/thaumcraft/porting/Provided1192BaseMap.java`

4. Версия stage поднята до `1.13.0`.

## Правильное правило на следующие stage

Начиная отсюда нельзя писать “похожее на Thaumcraft”. Для каждого subsystem:

1. Берем TC4 class/source как эталон.
2. Смотрим, есть ли современный 1.19.2 аналог в provided reboot archives.
3. Если есть — переносим через него.
4. Если нет — портируем строго из TC4 в текущую Forge 1.19.2 архитектуру.
5. Любая новая механика должна быть помечена как temporary scaffold, если она не подтверждена TC4 source.

## Следующий правильный stage

**Stage114 — Strict TC4 Runtime Start**

Первым лучше переносить не визуальные “пустышки”, а базовую систему, от которой зависит весь Thaumcraft:

- native TC4 aspects runtime;
- AspectList / AspectSource mapping;
- item/block object tags;
- thaumometer scan data;
- research unlock prerequisites.

