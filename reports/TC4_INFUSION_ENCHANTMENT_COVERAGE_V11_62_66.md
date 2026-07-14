# Покрытие инфузионных зачарований TC4 — порт 11.62.66

## Объём оригинала

В `ConfigRecipes.initializeArcaneRecipes()` находятся **24** отдельные регистрации `addInfusionEnchantmentRecipe`: два собственных зачарования Thaumcraft (`Repair`, `Haste`) и 22 ванильных зачарования. Они не являются рецептами Arcane Workbench и не входят в число 109 arcane-регистраций.

## Что добавлено в 11.62.66

- зарегистрированы `thaumcraft:repair` и `thaumcraft:haste`, без которых `InfEnchRepair` и `InfEnchHaste` не могли разрешить выход;
- для Repair восстановлены уровни 1–2, стоимость зачарования, несовместимость с Unbreaking, 40-тиковый цикл, перевод object aspects в primals, формула `floor(sqrt(amount * 2)) * level` и списание vis из жезлов;
- список применимых Repair-предметов ограничен тегом `thaumcraft:repairable`, соответствующим исходному `IRepairable`;
- для Haste восстановлены уровни 1–3 и импульс движения `0.015 × level` с половинным эффектом в воздухе и воде;
- JEI теперь показывает подходящий центральный предмет и его зачарованную копию, а не пустой/неопределённый результат.

## Итог

| Показатель | Значение |
|---|---:|
| Оригинальные регистрации | 24 |
| Записи индекса порта | 24 |
| Точно сопоставлено | 24 |
| Собственные ID зарегистрированы | 2/2 |
| Source-checks runtime-логики | 10/10 |
| Статус | **STATIC_MAPPING_COMPLETE** |

## Поэлементный маппинг

| TC4 key | Строка | Современный ID | Нестабильность | Аспекты | Компоненты | Статус |
|---|---:|---|---:|---|---:|---|
| `InfEnchRepair` | 1759 | `thaumcraft:repair` | 4 | PRAECANTATIO 8, FABRICO 10, ORDO 10 | 2 | MAPPED |
| `InfEnchHaste` | 1775 | `thaumcraft:haste` | 3 | PRAECANTATIO 4, ITER 8, VOLATUS 8 | 2 | MAPPED |
| `InfEnch0` | 1794 | `minecraft:protection` | 1 | PRAECANTATIO 4, TUTAMEN 8 | 2 | MAPPED |
| `InfEnch1` | 1806 | `minecraft:fire_protection` | 1 | PRAECANTATIO 4, TUTAMEN 4, IGNIS 4 | 3 | MAPPED |
| `InfEnch2` | 1820 | `minecraft:blast_protection` | 1 | PRAECANTATIO 4, TUTAMEN 4, PERDITIO 4 | 3 | MAPPED |
| `InfEnch3` | 1835 | `minecraft:projectile_protection` | 1 | PRAECANTATIO 4, TUTAMEN 4, VOLATUS 4 | 3 | MAPPED |
| `InfEnch4` | 1850 | `minecraft:feather_falling` | 1 | PRAECANTATIO 4, AER 4, VOLATUS 4 | 2 | MAPPED |
| `InfEnch5` | 1864 | `minecraft:respiration` | 2 | PRAECANTATIO 4, AER 8, AQUA 8 | 2 | MAPPED |
| `InfEnch6` | 1875 | `minecraft:aqua_affinity` | 2 | PRAECANTATIO 4, MOTUS 8, AQUA 8 | 3 | MAPPED |
| `InfEnch7` | 1889 | `minecraft:thorns` | 2 | PRAECANTATIO 4, TELUM 8, HERBA 8 | 3 | MAPPED |
| `InfEnch8` | 1903 | `minecraft:sharpness` | 2 | PRAECANTATIO 4, TELUM 8 | 2 | MAPPED |
| `InfEnch9` | 1915 | `minecraft:smite` | 2 | PRAECANTATIO 4, TELUM 4, EXANIMIS 4 | 3 | MAPPED |
| `InfEnch10` | 1930 | `minecraft:bane_of_arthropods` | 2 | PRAECANTATIO 4, TELUM 4, BESTIA 4 | 3 | MAPPED |
| `InfEnch11` | 1945 | `minecraft:knockback` | 1 | PRAECANTATIO 4, TELUM 3, MOTUS 3 | 2 | MAPPED |
| `InfEnch12` | 1959 | `minecraft:fire_aspect` | 3 | PRAECANTATIO 4, TELUM 4, IGNIS 8 | 3 | MAPPED |
| `InfEnch13` | 1973 | `minecraft:looting` | 3 | PRAECANTATIO 4, TELUM 4, LUCRUM 8 | 3 | MAPPED |
| `InfEnch14` | 1987 | `minecraft:efficiency` | 2 | PRAECANTATIO 4, INSTRUMENTUM 4, ORDO 4 | 2 | MAPPED |
| `InfEnch15` | 2000 | `minecraft:silk_touch` | 5 | PRAECANTATIO 16, INSTRUMENTUM 16, ORDO 16, METO 16, PERFODIO 16 | 3 | MAPPED |
| `InfEnch16` | 2016 | `minecraft:unbreaking` | 2 | PRAECANTATIO 4, INSTRUMENTUM 4, ORDO 8 | 3 | MAPPED |
| `InfEnch17` | 2030 | `minecraft:fortune` | 3 | PRAECANTATIO 4, INSTRUMENTUM 4, LUCRUM 8 | 3 | MAPPED |
| `InfEnch18` | 2044 | `minecraft:power` | 2 | PRAECANTATIO 4, TELUM 8 | 2 | MAPPED |
| `InfEnch19` | 2056 | `minecraft:punch` | 2 | PRAECANTATIO 4, TELUM 3, MOTUS 3 | 2 | MAPPED |
| `InfEnch20` | 2070 | `minecraft:flame` | 3 | PRAECANTATIO 4, TELUM 4, IGNIS 8 | 3 | MAPPED |
| `InfEnch21` | 2084 | `minecraft:infinity` | 5 | PRAECANTATIO 8, TELUM 16, VACUOS 16, PERMUTATIO 16 | 3 | MAPPED |

## Что ещё не подтверждено

Статическая карта не доказывает успешную компиляцию. В игре необходимо проверить принятие центрального предмета, списание XP и essentia, рост уровня зачарования, несовместимость, сохранение NBT, расход vis при Repair, скорость Haste и отображение всех 24 записей в JEI.

Общий статус версии остаётся **PARTIAL / STATIC PASS**.
