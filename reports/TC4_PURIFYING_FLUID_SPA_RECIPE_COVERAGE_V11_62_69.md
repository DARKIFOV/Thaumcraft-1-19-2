# Покрытие рецептов Purifying Fluid / Bath Salts / Arcane Spa — 11.62.69

## Методика

В подсчёт включены только регистрации из `ConfigRecipes.java`, относящиеся непосредственно к этой подсистеме. Превращение выброшенных Bath Salts в источнике воды и размещение жидкости Arcane Spa являются world interactions, а не crafting-рецептами, поэтому в число регистраций не входят.

## Поэлементный маппинг

| № | Оригинальная регистрация | Строка TC4 | Тип | Порт | JEI | Статус |
|---|---|---:|---|---|---|---|
| 1 | `BathSalts` | `ConfigRecipes.java:604` | Crucible / Alchemy | `data/thaumcraft/thaumcraft_alchemy/tc4_bathsalts.json` | категория Alchemy | PASS статически |
| 2 | `ArcaneSpa` | `ConfigRecipes.java:879` | Arcane shaped | `data/thaumcraft/thaumcraft_arcane_workbench/tc4_arcanespa.json` | категория Arcane Workbench | PASS статически |

## Контракты

### BathSalts

- research: `BATHSALTS`;
- catalyst: `thaumcraft:tc4_dust` — современный носитель `ConfigItems.itemResource:14`;
- output: `thaumcraft:tc4_bath_salts`;
- аспекты: `COGNITIO 6`, `AURAM 6`, `ORDO 6`, `SANO 6`.

### ArcaneSpa

- research: `ARCANESPA`;
- pattern: `QIQ / SJS / SPS`;
- `P`: piston;
- `J`: essentia jar;
- `S`: arcane stone;
- `Q`: quartz block;
- `I`: iron bars;
- стоимость: `AQUA 16`, `ORDO 8`, `TERRA 4`;
- output: `thaumcraft:tc4_block_arcane_spa`.

## Итог покрытия

- оригинальных регистраций в выбранной подсистеме: **2**;
- сопоставлено: **2**;
- статическое покрытие: **2/2 (100%)**;
- runtime-проверка крафта и JEI: **NOT TESTED**.
