# Thaumcraft Legacy Rebuild — v11.64.37

Последний strict full-closure раунд закрывает обычную Alchemical Furnace, Alembic и полный distillation lifecycle: 50/32 capacity, пять алембиков, 40/20-tick transfer, bellows-scaled cook time, sided automation, canonical NBT, GUI/light/particles, label/jar interaction и conditional renderer.

Проверки: focused static CI 61/61 PASS (сегментированный запуск), Java 17 self-test PASS, targeted parse 9/9, 281 unique GameTests, 835 runtime scenarios, JSON 2189/2189, recipes 258/258 STATICALLY MAPPED, final recheck 141/141 PASS. BUILD/RUNTIME/JAR не подтверждены: Wrapper не разрешил `services.gradle.org`, активна Java 21 вместо JDK 17.

Подробности: `TC4_11.64.37_ALCHEMICAL_FURNACE_ALEMBIC_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md`.

---

# Thaumcraft Legacy Rebuild — v11.64.35

Последний strict full-closure раунд закрывает Essentia Jar family: обычную и Void Jar, labels, suction, phial-transfer, отдельные tile/item NBT-форматы, placement/drop/migration и точный renderer. Неоригинальный `filtered_essentia_jar` оставлен только как скрытый migration alias.

Проверки: focused static CI 57/57 PASS, Java 17 self-test PASS, targeted parse 15/15, 274 unique GameTests, 807 runtime scenarios, JSON 2189/2189, recipes 258/258 STATICALLY MAPPED. BUILD/RUNTIME/JAR не подтверждены: Wrapper не разрешил `services.gradle.org`, активна Java 21 вместо JDK 17.

Подробности: `TC4_11.64.35_ESSENTIA_JAR_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md`.

---

# Thaumcraft Legacy Rebuild — v11.64.34

Последний strict full-closure раунд закрывает Infusion Matrix, Arcane Pedestal, converted pillars и полный infusion lifecycle: exact wand formation, six-primal vis cost, structure/symmetry, recipe lock, instability table, XP/essentia/components, finish и save/reload.

Проверки: focused static CI 44/44 PASS, Java 17 self-test PASS, targeted parse 16/16, 267 unique GameTests, 793 runtime scenarios, JSON 2191/2191, recipes 258/258 STATICALLY MAPPED. BUILD/RUNTIME/JAR не подтверждены без успешного Gradle/runtime.

Подробности: `TC4_11.64.33_WAND_FOCI_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md`.

---

# Thaumcraft Legacy Rebuild — v11.64.32

Последний строгий full-closure раунд закрывает базовую систему жезлов: rods, staff rods и caps. Восстановлены четыре исходных creative-жезла, stack 1, uncommon rarity, root NBT `rod`/`cap`, staff +6 damage, локализованные имена, functional component items, исходные capacity/discount/regeneration contracts и 26 оригинальных component textures. Wand foci не входят в этот релиз.

Проверки: focused static CI 42/42 PASS, Java 17 language-target self-test PASS, targeted parse 11/11, 253 уникальных GameTest-метода, 761 runtime-сценарий, JSON 2190/2190, рецепты 258/258 STATICALLY MAPPED. BUILD/RUNTIME/JAR не подтверждены: Gradle Wrapper не разрешил `services.gradle.org`, активна Java 21 вместо JDK 17.

Подробности: `TC4_11.64.32_WAND_RODS_CAPS_FULL_CLOSURE_PARITY_PORT_REPORT_RU.md`.

---

# v11.64.31 — Arcane Workbench: полное закрытие

- Единственная цель раунда — Arcane Workbench / Mystical Workbench.
- Возвращены ровно 11 слотов: grid 0–8, output 9, wand 10; скрытый runtime slot 11 удалён и оставлен только как одноразовая миграция старого сохранения.
- Пустой AspectList снова стоит 0 vis; удалён выдуманный общий расход Ordo 2.
- Thaumcraft Table превращается без vis: обычный жезл переходит в slot 10 и удаляется из руки, staff остаётся у игрока.
- Sided automation с любой стороны видит только slot 10; восстановлены sneak-pass, `Inventory` NBT, sync packet и installed-wand renderer.
- Greatwood Wand Rod исправлен на 3 Perditio; GUI/worktable textures совпадают с оригиналом по SHA-256.
- Итог: **248** GameTest / **748** сценариев / **2190** JSON / **258/258** рецептов / static CI **38/38 PASS** / parse **10/10 PASS**.
- Реестр и план: `TC4_11.64.31_FULL_CLOSURE_STATUS_AND_PLAN_RU.md`.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в полном архиве.

# v11.64.30 — Lamp of Fertility: полное закрытие

- Единственная цель раунда — Lamp of Fertility.
- Восстановлены 4 Victus charges, draw каждые 5 тиков, suction `128-charges*10`, direct top-side jar/tube input.
- Поиск пары: radius 7, first scan immediately, затем каждые 300 charged ticks, exact Java class, максимум 7 существ одного класса.
- Только взрослые age 0 и not-in-love; успешная пара расходует 2 charges.
- NBT только `orientation/charges`; light/textures 8/15, six-face Bore nozzle, second Bore Base connector, common rarity.
- Итог: **247** GameTest / **736** сценариев / **2190** JSON / **258/258** рецептов / static CI **36/36 PASS** / parse **8/8 PASS**.
- Реестр закрытых/незакрытых систем и план: `TC4_11.64.30_FULL_CLOSURE_STATUS_AND_PLAN_RU.md`.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в полном архиве.

# v11.64.29 — Lamp of Growth: полное закрытие

- Единственная цель раунда — Lamp of Growth; Lamp of Fertility не объявляется закрытой.
- Восстановлены Herba reserve/charges, 5-тиковый essentia draw, suction 128 и точные NBT `orientation/reserve/charges`.
- Сканирование снова использует 169 перемешанных колонн 13×13, одну колонну за тик и строгую сферу `distanceSquared < 36`.
- Удалён гарантированный bonemeal; выбранная культура получает один естественный random tick.
- Возвращены CropUtils-grown правила, datapack blacklist, sparkle color 4259648/range 32 и динамический свет 8/15.
- Шесть ориентаций Bore nozzle, второй connector на Arcane Bore Base, on/off textures, рецепт и `LAMPGROWTH` research сверены с TC4.
- Итог: **240** GameTest / **720** сценариев / **2189** JSON / **258/258** рецептов / static CI **34/34 PASS** / parse **8/8 PASS**.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED из-за недоступности `services.gradle.org` и Java 21 вместо Java 17.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне полного архива.

# v11.64.27 — система исследований: полное закрытие

- Единственная цель раунда — Research Table, аспекты/очки, исследовательские записки и изучение исследований.
- Убраны бесплатные стартовые очки: первичные аспекты известны с пулом 0.
- Два старых кошелька объединены; миграция выбирает максимум и не удваивает очки.
- Обычный прогресс ограничен 201 оригинальным исследованием TC4; secondary cost использует только исходные теги.
- Записка создаётся из Таумономикона; существующая записка предотвращает повторный расход; paper+ink обязательны даже creative.
- Одноаспектные исследования сохраняют один якорь; радиус записки 1+min(3, complexity).
- Стол расходует ink и aspect/bonus при редактировании, отключает неоригинальные create/complete buttons и не пишет выдуманный чат.
- RESEARCHDUPE расходует paper + ink sac + exact tags+copies и может увеличить stack выше maxStackSize 1.
- 50 aspect icons, guiresearchtable2 и обе research-note textures побайтово совпадают с TC4.
- Итог: **226** GameTest / **688** сценариев / **2054** JSON / **258/258** рецептов / static CI **42/42 PASS** / parse **19/19 PASS**.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED из-за недоступности `services.gradle.org` и Java 21 вместо Java 17.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне полного архива.

# v11.64.26 — Thaumonomicon: полное закрытие core-механики

- Единственный объект раунда — обычный Thaumonomicon, Cheat Sheet, браузер и страницы исследований, research-action packets и bookshelf transformation.
- Возвращены sibling repair, sync research+aspects, exact 50 только новым аспектам Cheat Sheet и отсутствие выдуманного чата.
- GUI использует 256×230, карту 224×196, одну статическую категорию/общий pan, even spreads, static reference history и оригинальные звуки 1.0/0.66/0.4.
- Первичные исследования требуют paper + inked scribing tools + отсутствие открытой записки; secondary purchase проходит тихо и серверно.
- Книжный шкаф превращается unfocused wand без vis в zero-velocity SpecialItemEntity с particles/sound; compound recipe — wand + bookshelf, 1×2×1.
- Текстуры книги/Cheat Sheet и два основных GUI побайтово совпадают с TC4; аспекты Cognitio 10, Praecantatio 2, Arbor 1.
- Exact rare-loot contracts: 7 ChestGenHooks pools weight 1 и wizard tower weight 20; runtime integration честно заблокирован отсутствующими внешними системами.
- Итог: **218** GameTest / **672** сценария / **2054** JSON / **258/258** рецептов / focused static CI **40/40 PASS**.
- Core SOURCE/RESOURCE CLOSED; external acquisition blocked; BUILD/RUNTIME NOT VERIFIED из-за недоступности `services.gradle.org` и Java 21 вместо Java 17.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне полного архива.

# v11.64.24 — Arcane Bore: полное закрытие механики

- В одном раунде закрывались только Arcane Bore и его неразрывное Arcane Bore Base.
- Квадратный обход заменён исходной 2° спиралью с радиусом `2 + Enlarge` и глубиной 64.
- Возвращены Perditio aura-vis/essentia acceleration, unpowered machine repair, fake-player tick, Native Clusters, nearby-item collection и nozzle-first output.
- Восстановлены вертикальная установка на base, шесть направлений жезлом, extended shape, redstone на голове или основании и common rarity.
- NBT снова использует `orientation`, `baseOrientation`, list `Inventory/Slot` и `SpeedyTime`; старый Forge compound мигрируется, `SpiralIndex` не сохраняется.
- GUI, повреждённый overlay, half-scale labels, Bore/base models, три vortex, jar core, dual beam и исходные текстуры восстановлены.
- Исправлен рецепт основания `WIW/IDI/WIW` с greatwood/iron/oak log.
- Итог: **204** GameTest / **638** сценариев / **2054** JSON / **258/258** рецептов / historical guard **26/26 PASS** / focused static CI **35/35 PASS** / final recheck **25/25 PASS**.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED: Gradle Wrapper не разрешает `services.gradle.org`, активна Java 21 вместо Java 17.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне полного архива.

# v11.64.23 — Arcane Spa: полное закрытие купели

- В одном раунде закрывалась только `Arcane Spa`; внутренняя Bath Salts/Warp Ward-механика остаётся ранее закрытой зависимостью.
- Восстановлен filled-container-first путь: частичное последнее заполнение расходует всю наполненную ёмкость и возвращает пустую; полный/несовместимый tank не открывает GUI.
- Новые сохранения используют точные `mix`, корневой FluidStack и `Items`; старые `Mix/Tank/Salts` мигрируются.
- Выход проверяет только top-solid support, replaceable и same-source; ингредиенты списываются до установки блока, как в `TileSpa`.
- Возвращены 40-тиковая cadence, redstone pause, x-outer/z-inner 5×5 expansion, six-side adjacency, ultra-warm water rejection и side automation без UP.
- Блок снова каменный: hardness 3, resistance 25, stone sound, common rarity и exact spa/pedestal textures.
- GUI: исходные slot/toggle/gauge координаты, шесть 8×8 fluid tiles, top mask, `mb` и cameraclack.
- Итог: **197** GameTest / **622** сценария / **2054** JSON / **258/258** рецептов / static CI **33/33 PASS** / final recheck **23/23 PASS**.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED: Wrapper не разрешает `services.gradle.org`, активна Java 21 вместо Java 17.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне полного архива.

# v11.64.21 — Arcane Pressure Plate: полное закрытие механики

- В одном раунде сверялась только `Arcane Pressure Plate` и её неразрывный путь железных/золотых ключей; сама Arcane Door не объявляется закрытой.
- Восстановлены точные три режима, AABB, высоты 1/16 и 1/32, отсутствие коллизии и требования опоры, 20-тиковая перепроверка, weak 15 и strong-up 15.
- Warded-путь использует hardness -1, resistance 999, explosion immunity, no loot table и owner-only снятие непрожатой плиты жезлом с одним ручным drop.
- NBT снова хранит исходные `owner`, `access/name`, префиксы 0/1 и `setting`, сохраняя UUID migration storage уже созданных миров порта.
- Ключи возвращены к `location/type`, coordinate-only matching, исходному порядку выдачи связанной копии и расхода blank stack, any-NBT bound branch и трёхстрочному tooltip x,z,y.
- Исправлены T/D в рецепте плиты и результат Gold Key; исходные applate1/2/3 и keyiron/keygold побайтово совпадают.
- Итог: **183** GameTest / **594** сценария / **2054** JSON / **258/258** рецептов / focused static CI **31/31 PASS**.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME NOT VERIFIED: Wrapper не разрешает `services.gradle.org`, а активная среда использует JDK 21 вместо требуемого JDK 17.
- Универсальный промт остаётся обязательным в корне полного архива.

# v11.64.18 — Arcane Bellows: полное закрытие основной механики

- В одном раунде сверялись только `Arcane Bellows / Магические меха`.
- Восстановлены точные шесть направлений установки, фиксированный collision/selection box 0.1..0.9 × 0..1, hardness 2.5 и resistance 10.
- Анимация использует исходные 0.35+rand×0.55, deflate 0.075, inflate 0.025, верхний overshoot и ghast-fireball sound 0.01 с исходным pitch. Красный сигнал полностью замораживает цикл и отключает generic machine boost.
- Восстановлены оригинальные различия потребителей: обычная печь каждые 2 тика и её вертикальный X/Z-only quirk; Crucible считает любые 4 горизонтальные меха; Alchemical Furnace и Essentia Buffer используют до 6 ориентированных непитаемых мехов.
- Закреплён внешний точный контракт Infernal Furnace (cap 3, 140/80−20×bellows, 44% bonus each), но он не активирован до полного порта самой Infernal Furnace.
- Восстановлены исходные NBT-ключи, единый inventory render, ModelBellows/UV, побайтовая текстура, Arcane recipe и BELLOWS research.
- Добавлены 5 GameTest-методов и 15 runtime-сценариев. Итог: 167 GameTest / 549 сценариев / 2052 JSON / рецепты 258/258 / focused static CI 25/25 PASS. Универсальный промт обязателен в корне каждого полного архива.

# v11.64.15 — Brain in a Jar: полное закрытие основной механики

- В одном раунде сверялся только `Brain in a Jar / Мозг в банке`.
- Восстановлены точные XP capacity/attraction/absorption/release/break/comparator/enchant contracts, включая временное переполнение >2000 до следующего tick-start clamp.
- NBT снова хранит только `XP`; transient `EatDelay` не сохраняется и старый портовый тег игнорируется.
- Исправлены block/item свойства: hardness 0.3, default resistance, light 9, stack 64, обычная редкость, item warp 1.
- Восстановлены exact ModelBrain, ModelJar.Brine, исходные brain2/jarbrine/jar textures, двухкубовая банка, bob/rotation и full-jar spell particle.
- Закреплены exact infusion recipe, hidden JARBRAIN research, triggers, research warp 3, creative/JEI и локализации.
- Добавлены 3 новых GameTest-метода поверх существующего Brain Jar теста; итог: **152** уникальных GameTest / **503** manifest-сценария / **2052** JSON / рецепты **258/258**.
- Focused static CI: **21/21 PASS** при split execution. Gradle/Forge runtime остаётся **NOT VERIFIED**: Wrapper не может разрешить `services.gradle.org`.
- `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md` обязателен в корне этого и каждого следующего полного исходного архива; guard запрещает упаковку без него.

# v11.64.14 — Purifying Bath Salts: полное закрытие основной механики

- В одном раунде сверена только цепочка `Purifying Bath Salts → water source → Purifying Fluid → Warp Ward`, включая ведро, Arcane Spa, рецепт, исследование, ресурсы, частицы и звуки.
- `BathSaltsItem` сохраняет точный срок жизни выброшенного предмета **200 тиков**. Production-путь истечения преобразует только точный источник ванильной воды в серебристую очищающую жидкость и не принимает текущую воду или другую жидкость.
- Purifying Fluid имеет luminosity 10, viscosity 1000, редкость жидкости `RARE`; сами соли и наполненное ведро имеют обычную item rarity, как оригинал.
- Источник жидкости одноразово выдаёт Warp Ward amplifier 0 по формуле `min(32000, 200000 / max(1, floor(sqrt(permanentWarp))))` и удаляется. Уже защищённый игрок источник не расходует.
- Временная ванильная bubble-частица заменена на отдельную `TC4PurifyingBubbleParticle`: точные кадры 16–18 исходного `particles.png`, alpha 0.25, full-bright, исходные масштаб, lifetime, ускорение, дрейф и damping 0.85. Восстановлены порядок RNG, отдельные координаты звука и параметры lava-pop.
- Проверены и закреплены: чистое ведро, dispenser, тигельный рецепт BATHSALTS (6 Cognitio/Auram/Ordo/Sano), скрытое исследование (-4,-4, complexity 2, по 3 аспекта), зависимости SANESOAP/ARCANESPA и цикл Arcane Spa.
- Оригинальная цена у мага закреплена как **5–7 изумрудов за одну соль**, но production-активация этого предложения честно остаётся частью отдельной отсутствующей системы custom wizard villager/village tower. Предложение не подменено торговлей ванильного жителя.
- Добавлены 3 GameTest-метода, 11 runtime-сценариев, exact resource guards, pure Java 17 self-test и универсальный промт `UNIVERSAL_PROMPT_TC4_FULL_CLOSURE_RU.md`.
- Текущая статическая инвентаризация: **149** уникальных GameTest-методов / **489** уникальных manifest-сценариев / **2051** JSON / рецепты **258/258**.
- Focused static CI: **20/20 PASS** при split execution. Gradle/Forge runtime остаётся **NOT VERIFIED**: Wrapper не может разрешить `services.gradle.org`.

# v11.64.13 — Unnatural Hunger full source/resource closure

- Механика `Unnatural Hunger` разобрана как единый объект по всем связанным исходникам и ресурсам TC4 4.2.3.5: регистрация эффекта, тики, два warp-пути, завершение еды, curative metadata, сообщения, иконка и полноэкранный post-effect.
- Серверная логика приведена к оригиналу: цвет `0x446633`, иконка `(7,1)`, истощение `0.025 × (amplifier + 1)` каждый тик, длительности 5000/6000, amplifier `min(3, warp/15)`, начальные curatives rotten flesh + brain и уменьшение на 600 тиков/один amplifier.
- Удалена временная зелёная HUD-заливка. Добавлен отдельный post chain `unnatural_hunger.json` с точными параметрами TC4 `ColorScale=[1.0,0.8,0.8]` и `Saturation=1.1`; он обрабатывается до HUD, корректно resize/reload/close и не занимает единый глобальный shader-slot Minecraft.
- Иконка эффекта подтверждена как точный 18×18 crop исходного `potions.png`; runtime-лист `potions.png` побайтово совпадает с встроенным оригинальным ресурсом.
- Добавлены pure-Java contract/self-test, GameTest-контракт, пять новых manifest-сценариев, source evidence и полный guard. Текущая инвентаризация: **146** уникальных GameTest-методов / **478** уникальных сценариев / **2050** JSON / рецепты **258/258**.
- Focused static CI: **19/19 PASS** при split execution; `javac --release 17` self-test и полный Java syntax guard — PASS.
- Граница доказательства: source/resource/static closure завершено. Полный Gradle/Forge build, клиентский кадр, multiplayer и запуск 146 GameTest остаются **NOT VERIFIED**, потому что Wrapper не может скачать Gradle 7.5.1 (`UnknownHostException: services.gradle.org`).

# v11.64.12 — Unnatural Hunger food-finish parity

- Восстановлен оригинальный `PlayerUseItemEvent.Finish` контракт через Forge 1.19.2 `LivingEntityUseItemEvent.Finish`.
- При активном `Unnatural Hunger` гнилая плоть или `tc4_brain` снимают ровно 600 тиков и один amplifier; результат сохраняется только при `duration > 0 && amplifier >= 0`.
- После уменьшения выживший эффект повторно получает только гнилую плоть как явный curative item, как в TC4; мозг по-прежнему обрабатывается production event-path.
- Обычная съедобная еда выводит `warp.text.hunger.1` тёмно-красным курсивом, но её питание не отменяется; особая еда выводит `warp.text.hunger.2` тёмно-зелёным курсивом.
- Добавлен GameTest-контракт: **145** уникальных методов / **473** manifest-сценария.
- Focused static CI: **18/18 PASS** при split execution; pure helper проходит `javac --release 17` self-test.
- Полный Gradle/Forge build и runtime остаются **NOT VERIFIED**: Wrapper не может скачать Gradle 7.5.1 (`UnknownHostException`).

# v11.64.11 — Sun Scorned brightness-table parity

- Исправлен runtime-эффект `Sun Scorned`: вместо линейного `getMaxLocalRawBrightness(pos) / 15.0F` production теперь использует современный эквивалент оригинального `EntityLivingBase#getBrightness(1.0F)` — `getLightLevelDependentMagicValue()`.
- Сохранены строгие границы `brightness > 0.5F` для горения и `brightness < 0.25F` для лечения, точные формулы случайного броска, требование видимости неба, 4 секунды огня и лечение на 1 HP.
- RNG вызывается только внутри яркой/тёмной ветки; нейтральная зона 0.25–0.5 не расходует случайное число, как в TC4.
- Добавлен GameTest-контракт: **144** уникальных метода / **472** manifest-сценария.
- Focused static CI: **17/17 PASS** при split execution; pure helper проходит `javac --release 17` self-test.
- Полный Gradle/Forge build и runtime остаются **NOT VERIFIED**: Wrapper не может скачать Gradle 7.5.1 (`UnknownHostException`).

# v11.64.10 — Death Gaze cone geometry parity

- Исправлена геометрия `Death Gaze`: значение `0.75` снова трактуется как полная апертура конуса в радианах, а не как прямой порог dot product.
- Production использует точное сравнение `cos(0.75 / 2)`, центр тела цели и строгую осевую границу `< range`, как `EntityUtils.isVisibleTo`/`Utils.isLyingInCone` оригинального TC4.
- Удалён неоригинальный сферический фильтр `distance <= range`; восстановлен исходный admission gate `Entity.canBeCollidedWith()` через современный `isPickable()`.
- Добавлен GameTest-контракт: **143** уникальных метода / **471** manifest-сценарий.
- Накопительные статические проверки: **16/16 PASS** при раздельном запуске; pure helper проходит `javac --release 17` self-test.
- Полный Gradle/Forge build и runtime остаются **NOT VERIFIED**: Wrapper не может скачать Gradle 7.5.1 (`UnknownHostException`).

# v11.64.09 — Warp grantResearch client aspect-pool sync parity

- Исправлен сетевой дефект обоих production-путей `grantResearch`: серверный aspect pool больше не остаётся несинхронизированным с клиентом.
- Добавлен общий `TC4WarpResearchGrant`: точная формула `1 + rand(times)`, шесть примархов в оригинальном порядке, `+1` за выбор и один агрегированный `syncAspectKnowledge` после серии.
- `WarpEvents` и `TC4EldritchProgression` делегируют в один source-linked путь, исключая дальнейший дрейф.
- Добавлен GameTest-контракт и manifest-сценарий: **142** уникальных метода / **470** сценариев.
- Focused static CI: **13/13 PASS**; exact helper проходит `javac --release 17` self-test.
- Полный Gradle/Forge build и runtime остаются **NOT VERIFIED**: Wrapper не может скачать Gradle 7.5.1 (`UnknownHostException`).

# v11.64.08 — Eldritch milestone grantResearch parity

- ELDRITCHMINOR (actual warp > 25) and ELDRITCHMAJOR (actual warp > 50) now match TC4 checkWarpEvent: they unlock silently (no chat line) and call grantResearch(10)/grantResearch(20).
- Removed the port's fabricated milestone chat literals ("Something alien becomes visible...", "The Eldritch tab opens fully.").
- grantResearch is a faithful port of WarpEvents.grantResearch: 1 + rand(times) points spread over random primal aspects via PlayerAspectKnowledge.addPool.
- TC4EldritchParity.eldritchMilestoneGrantsMatchTc4() links the 10/20 magnitudes to production; one required GameTest added (141 methods, 469 manifest scenarios).
- Focused static CI is 12/12 PASS; Gradle build and Forge runtime remain NOT VERIFIED.

# Thaumcraft Legacy Rebuild Forge 1.19.2

## 11.64.05 — Warp spawn entity-aware collision parity

- Removes the non-original fixed two-air-block gate from Guardian/Mind Spider warp spawning.
- Candidate admission now follows the concrete entity bounding box: solid top support, no collision and no intersecting liquid.
- Preserves the corrected original tri-state `-1/0/+1` axis offsets from v11.64.04.
- Adds two required GameTests, raising the source inventory to 139 methods and the runtime manifest to 467 scenarios.
- Focused static CI is 12/12 PASS; Gradle build and Forge runtime remain NOT VERIFIED.

# Файл для продолжения в новом чате

Готовый промт находится в `PROMPT_FOR_FUTURE_CHAT_RU.md`. Краткая инструкция — в `00_START_HERE_RU.txt`.

# v11.64.04 — Warp spawn tri-state offset parity (исправленная версия)

- Возвращает точную формулу TC4: модуль 7–24 умножается на включительный случайный знак `-1`, `0` или `1`.
- Нулевая ось является штатным исходом оригинала; boolean-sign вариант отклонён как регрессия.
- Production вызывает `TC4WarpRuntimeParity.signedSpawnOffset(magnitude, signRoll)` с `Mth.nextInt(random, -1, 1)`.
- GameTest и manifest проверяют отрицательный, нулевой и положительный исходы.
- Текущие показатели: 137 уникальных GameTest, 465 manifest-сценариев, focused static CI 11/11 PASS. Gradle/JAR/Forge runtime остаются NOT VERIFIED.

# v11.64.03 — Warp runtime propagation and cleansing parity

- Restores TC4 curative asymmetry: the initial warp-event Infectious Vis Exhaust remains uncurable, while propagated instances retain the default milk cure.
- Restores amplifier downgrade, 40-tick cadence, four-block radius and 6000-tick propagated duration through one source-linked contract.
- Restores unconditional Sanity Soap stack consumption, including creative mode.
- Source-links the 0.33 + 0.25 + 0.25 sticky-cleansing chance and the purifying-fluid Warp Ward duration formula.
- Adds five GameTests: 136 unique methods and 464 manifest scenarios. Focused static CI is 15/15 PASS; runtime remains NOT VERIFIED.

# v11.64.02 — Research completion warp parity

- Restores TC4 `ResearchManager.completeResearch` warp buckets: one-point grants are permanent; larger grants split `ceil(warp/2)` permanent and `floor(warp/2)` sticky.
- Fixes the old port behavior that converted all research warp to permanent warp.
- Covers 20 original research entries with warp, including 12 entries whose sticky component was previously lost.
- Adds three GameTests, a pure-Java self-test and a source-linked production guard.
- Current static counters: 131 unique GameTests, 459 manifest scenarios, 22/22 focused checks. Runtime remains NOT VERIFIED.

# Current repair release: v11.64.01

`v11.64.01` continues from v11.64.00 and restores authoritative Research Note completion plus completed-discovery conversion. Completion now requires the same live Research Table note, an exact NBT snapshot and a graph that is solved for the requesting player; stale or repeated solve commits are rejected without extra ink or packet sync. A solved discovery unlocks the target, then eligible siblings, and consumes exactly one note even in creative mode, matching TC4. The source contains 128 unique GameTest methods and 456 unique runtime-manifest scenarios. Recipe mapping remains 258/258; 18 smelting bonuses are separate. Twenty focused static checks pass in a split execution, but Gradle/Forge runtime execution is **NOT VERIFIED**. See `TC4_11.64.01_RESEARCH_NOTE_COMPLETION_TRANSACTION_PARITY_PORT_REPORT_RU.md`.

# Thaumcraft Legacy Rebuild Forge 1.19.2

## 11.64.01 — Research Note completion and discovery transaction parity

- Requires a live, still-valid Research Table menu and the exact note instance before accepting a solve request.
- Revalidates exact note NBT, stable target and the player-known connected graph at commit time; stale or repeated completion is rejected.
- Preserves TC4's completion cost: no additional ink is consumed after the edit that completed the graph.
- Converts solved discoveries atomically: target unlock must succeed before the note is consumed.
- Restores unconditional one-note consumption in creative mode and unlocks eligible sibling research after the target.
- Suppresses table/note synchronization for rejected solve packets.
- Adds six real Research Table/player GameTests, raising the source suite to 128 methods and the runtime manifest to 456 scenarios.
- Runtime Forge execution remains NOT VERIFIED.

## 11.64.00 — Research Note clear transaction and refund parity

- Restores the original clear admission rule: only active type-2 placed hexes can be erased; empty type-0 hexes and anchors are rejected server-side.
- Restores exactly one ink cost for every accepted survival clear.
- Source-links Research Expertise refund to `roll < 0.25` and Research Mastery to `roll < 0.50`.
- Removes the old port-only creative-mode unconditional refund; creative still bypasses ink damage through the existing inventory contract.
- Adds an atomic clear debit with exact Research Note NBT, scribing-tool damage and player-pool rollback.
- Gates packet synchronization on a successful server edit.
- Adds six real Research Table/GameTest scenarios, raising the source suite to 122 methods and the runtime manifest to 450 scenarios.
- Runtime Forge execution remains NOT VERIFIED.

## 11.63.99 — Research Note graph placement transaction parity

- Restores TC4 placement admission: any existing empty type-0 hex may receive an aspect; compatible-neighbour checks belong to graph completion, not placement.
- Rejects unknown aspects, occupied/anchor hexes and stale or fabricated Research Table menus on the authoritative server path.
- Makes ink and aspect placement one rollback-capable transaction and suppresses sync for rejected packets.
- Adds five real placement/security GameTests. Runtime Forge execution remains NOT VERIFIED.

## 11.63.98 — Research Mastery atomic aspect-combination parity

- Replaces two independent component debits with one source-planned transaction.
- Preserves TC4 source priority: player aspect pool first, Research Table bonus only when the pool lacks that component.
- Restores the original rule that a resource-complete invalid pair still consumes both selected aspect points.
- Adds rollback protection so a missing/stale second component can never destroy only the first component.
- Adds four real menu/player/BlockEntity GameTests, raising the source suite to 111 methods and the runtime manifest to 439 scenarios.
- Runtime Forge execution remains NOT VERIFIED.


## 11.63.97 — Research Table behavioral NBT and cadence parity

- Fixes a persistence drift: TC4 writes one `bonusAspects` NBT record per positive aspect type, while the port wrote one record per stored point and therefore preserved duplicate counts across reloads.
- Normalizes older port saves containing duplicate bonus records back to TC4's one-point-per-type persisted state.
- Always marks/synchronizes a completed bonus-recalculation pass so the reset counter persists even when RNG adds no aspect.
- Source-links the exact post-increment boundary `nextRecalc++ > 600`: counter 600 advances to 601 without firing; counter 601 fires and resets to zero.
- Adds three real `ResearchTableBlockEntity` GameTests for NBT cardinality, consumption save/reload and server-tick counter behavior.
- Runtime Forge execution remains NOT VERIFIED.


## 11.63.96 — Research Expertise / Mastery parity

- Fixes a production ordering bug: the RESEARCHER2 10% free-placement roll is now resolved before rejecting an empty player/table aspect source, as in TC4 `TileResearchTable.placeAspect`.
- Source-links the strict Mastery placement (`roll < 0.10`), Expertise clear refund (`roll < 0.25`) and Mastery clear refund (`roll < 0.50`) boundaries.
- Source-links the Research Table bonus counter threshold (`> 600`) and 8-block scan radius.
- Adds three static GameTest contracts. Runtime Forge execution remains NOT VERIFIED.

## 11.63.95 — Handheld Thaumometer timing and target parity

- Centralizes the original 25-tick use duration, completion at remaining count five, 10 camera ticks, sound volume/pitch and 10-block/0.5 entity targeting.
- Fixes an off-by-phase sound regression that emitted 11 clicks including the completion frame.
- Restores handheld `@` behavior: already scanned blocks, dropped items, entities and aura nodes are rejected at start and during stable-target validation.
- Adds three mandatory server-only contract GameTests, raising the source suite to 101 methods and the runtime manifest to 429 scenarios.
- Runtime, client audio/overlay and network behavior remain NOT VERIFIED without a Java 17 Forge build.

## Recipe registration denominator correction (v11.63.61 source revision)

- Original runtime recipe registrations are **258/258 statically mapped**: 54 crucible, 104 Arcane shaped, 5 Arcane shapeless, 63 infusion, 24 infusion-enchantment and 8 furnace-smelting recipes.
- The denominator counts registrations actually created after loop expansion, not literal API call sites and not raw JSON files.
- `tools/data/tc4_arcane_recipe_full_mapping_v11.62.66.json` proves 84 literal shaped call sites expand to 104 shaped registrations, with 5 additional shapeless registrations: **109/109 Arcane**. In this archive, 99 shaped + 5 shapeless originals are JSON-backed, while 5 shaped wand-cap registrations are generated in Java.
- The Arcane data folder contains 114 JSON files: 104 original registrations (99 shaped + 5 shapeless) plus 10 style/compatibility additions. The remaining 5 original shaped wand-cap registrations are generated in Java, so raw file count is not the original Arcane denominator.
- The 18 `ThaumcraftApi.addSmeltingBonus` calls are a separate bonus mechanic and are not included among the 8 furnace recipes.
- Runtime loading for the exact v11.63.61 source state remains **NOT VERIFIED**: no Java 17 build/JAR and no successful GameTest-server `RecipeManager`/custom reload-listener decode pass are available for this revision.

## 11.63.61 — Infusion Matrix structure pause/resume parity

- Restores the original non-terminal structure-loss behavior: an active infusion pauses when the center pedestal or diagonal pillars become invalid, while the locked recipe, pending components, essentia, instability and owner state remain intact.
- Adds wand reactivation for a paused matrix so the same locked craft resumes after the altar structure is repaired.
- Removes the erroneous post-start recipe re-selection from the current pedestal layout; a missing component now leaves the matrix waiting instead of invalidating a recipe that was already locked.
- Adds four mandatory server-only GameTests and raises the required runtime baseline to 56 GameTests and 384 manifest scenarios.
- Canonical static CI is 127/127 PASS. Runtime remains NOT_TESTED for this exact source state because the Gradle Wrapper cannot resolve services.gradle.org and the available local JDK is 21 rather than the Forge 1.19.2 development JDK 17.

## 11.63.58 — Alchemical processing and Thaumatorium runtime contracts

- Restores the original four-alembic limit above an Alchemical Furnace and uses the furnace's own persisted 40/20-tick distillation counter instead of global world time.
- Corrects successful Thaumatorium completion to replace the entire internal essentia buffer, matching legacy behavior and preventing residual migrated aspects from leaking into the next operation.
- Adds eight mandatory server-only GameTests for furnace capacity/NBT, four-alembic scheduling, alembic filtering/sides, centrifuge redstone processing, Thaumatorium heat/suction, full-buffer completion, Mnemonic Matrix formula memory and blocked-output rollback.
- Raises the required runtime baseline to 44 GameTests and 372 manifest scenarios.
- Canonical static CI is 123/123 PASS. Runtime remains NOT_TESTED for this exact source state because the Gradle Wrapper cannot resolve services.gradle.org and the available local JDK is 21 rather than the Forge 1.19.2 development JDK 17.

## 11.63.56 — Essentia tube transport runtime contracts

- Fixes `EssentiaTubeBlockEntity.restoreBufferForNetwork`: rollback is now capped by the original eight-point buffer capacity and immediately synchronizes the mixed `AspectList` with the legacy `essentiaType` / `essentiaAmount` transport view.
- Adds four mandatory server-only GameTests for normal/restricted suction, filtered aspect locking, one-way facing and buffer capacity/choke/NBT/rollback behavior.
- Raises the required runtime baseline to 36 GameTests and 364 manifest scenarios.
- Canonical static CI is 121/121 PASS. Runtime remains `NOT_TESTED` for this exact version because the local Gradle Wrapper cannot resolve `services.gradle.org` and the available JDK is 21 rather than the Forge 1.19.2 target JDK 17.

# Thaumcraft Legacy Rebuild Forge 1.19.2

## 11.63.55 — Essentia storage and Essentia Mirror runtime contracts

- Added four required dedicated-server GameTests for filtered/void jars, mixed Essentia Reservoir storage and Essentia Mirror remote drain/range/rollback.
- Required runtime baseline: 32 GameTests and 360 manifest scenarios.
- Static CI: 119/119 PASS; runtime for this version remains NOT_TESTED until a Java 17 build is obtained.
- Recipe ledger remains 258/258 and the 63 proven duplicate registry aliases remain removed with migration remap.

## 11.63.51 — Java 17 build and required Forge GameTest closure

- Fixes four real Forge 1.19.2 compile errors hidden by the previous static-only environment: the Flux Scrubber survival signature and three `UseOnContext`/`BlockPlaceContext` replacement checks.
- Corrects the energized-node GameTest fixture to restore a valid energized cooldown together with its NBT flag, so the test measures the transient per-tick vis pool instead of immediately de-energizing the node.
- Runs the actual Forge GameTest server on Java 17: **18/18 required tests pass**.
- Confirms runtime loading of 70 alchemy, 114 Arcane Workbench and 78 infusion recipes, plus a live Magical Forest biome-source installation check.
- Client visuals, JEI interaction, multiplayer and migration fixtures remain separate runtime work; this server pass does not claim those categories.

## 11.63.50 — final exact closure of the original TC4 recipe ledger

- Closes the final ten historical recipe records and closes the final ten unresolved records; with loop-expanded Arcane counting, the ledger moves from 248/258 to **258/258**.
- Proves GolemFlesh and the remaining eight arcane/shapeless recipes against original result metadata, patterns, components and aspect costs.
- Corrects the Advanced Golem infusion component from redstone to **shears** and proves the eight registry-specific recipes as an exact de-metadata expansion of the original wildcard catalyst (`itemGolemPlacer`, damage 32767).
- Preserves the original central-stack NBT operation: each advanced golem keeps its material item and receives byte `advanced=1`.
- Adds two required server-only Forge GameTests, increasing the suite from sixteen to eighteen tests and the runtime manifest from 342 to 346 scenarios.
- Static source mapping is complete; runtime loading for the exact v11.63.61 source state remains `NOT VERIFIED` until a Java 17 Forge build and successful `runGameTestServer` log exist.

## 11.63.49 — MCP field corrections, legacy tags and NBT infusion outputs

- Promotes twenty additional TC4 recipe records with exact source/MCP evidence, raising the proven runtime ledger from 228/258 to 248/258 and reducing the unresolved set from thirty to ten.
- Corrects stale resolver guesses in `JarBrain`, `TravelTrunk`, `MirrorHand`, `MirrorEssentia` and all three Fortress faceplates; confirms ordinary ice for the Ice Wand Rod.
- Restores `plankWood`, `ingotIron`, `stone` and `dyeBlue` semantics through `#minecraft:planks`, `#forge:ingots/iron`, `#forge:stone` and `#forge:dyes/blue`.
- Proves all eight Wand Rod infusion recipes, the three Mirror-family recipes, Brain in a Jar, Traveling Trunk, Helm of Revealing upgrade and all three mask NBT outputs.
- Adds two required server-only Forge GameTests, increasing the suite from fourteen to sixteen tests and the runtime manifest from 336 to 342 scenarios.
- Canonical static CI is 113/113 PASS. Runtime/GameTest evidence remains `NOT_TESTED` because the Gradle Wrapper cannot resolve `services.gradle.org` and only OpenJDK 21 is installed instead of the required development JDK 17.

## 11.63.48 — OreDictionary tags, exact wand-cap recipes and legacy smelting

- Promotes fourteen previously unresolved TC4 recipe records with direct source evidence, bringing the exact runtime recipe ledger to 228/258 and leaving thirty unresolved records.
- Replaces hard-bound copper, silver, thaumium and void-metal nugget inputs with `forge:nuggets/*` tags; gold remains the exact vanilla gold nugget.
- Extends Arcane Workbench catalyst matching and JSON parsing to use the same tag sentinel as shaped ingredients and JEI-facing recipe data.
- Confirms the complete eight-recipe wand-cap family: five arcane base/inert caps and three charged infusion caps with original costs, instability and essentia.
- Promotes exact pure tin/silver/lead crucible inputs and the three historical smelting recipes for magical logs, cinnabar ore and amber ore.
- Extends the required server-only Forge GameTest suite from twelve to fourteen tests and the runtime manifest from 330 to 336 scenarios.
- All new runtime evidence remains `NOT_TESTED` until a Java 17 build and successful `runGameTestServer` log exist.

## 11.63.46 — Autonomous Flux Scrubber and required GameTest

- Replaces the temporary click-to-cleanse Fume Dissipator with the TC4 autonomous Flux Scrubber block entity.
- Draws up to ten Aer centivis from the energized relay network, spends five per removed Flux Goo/Flux Gas block and checks at most sixteen shuffled loaded positions per server tick inside radius sixteen.
- Converts four cleanup charges with the original one-in-four chance into Praecantatio, capped at four units, and exports only from the mounted facing side.
- Restores the original `obelisk_cap.obj` Cap/Tip geometry, `fluxscrubber.png` texture and tip-bob animation through a client-only block-entity renderer.
- Extends the required server-only Forge GameTest suite from nine to ten tests with cleanup, sided output and NBT round-trip coverage.
- Runtime manifest now contains 325 scenarios. The new GameTest remains `NOT_TESTED` until Java 17 `build` and `runGameTestServer` complete successfully.

## 11.63.45 — Energized Vis Relay Network and executable relay GameTests

- Restores the TC4 energized-node output model: permanent node aspects build a persistent primal output profile, while a transient centivis pool refills once per server tick and is the only state consumed by relay clients.
- Restores the loaded-chunk Vis Relay graph with eight-block edges, line of sight, wildcard/primary attunement channels, a 512-relay traversal limit and persistent synchronized parent/pulse state.
- Restores Vis Amulet relay recharge every five ticks, after the original up-to-five-centivis transfer from the amulet into a held wand.
- Corrects the Arcane Workbench Charger to transfer at most five centivis per primary aspect per tick through the same relay graph.
- Restores source-texture relay beams and aspect-coloured consumption pulses; fixes the prior pedestal renderer compile blocker by keeping its vertex helper local.
- Extends the required Forge GameTest suite from six to nine tests with transient-pool refill, relay LoS/attunement/persistence and exact workbench-charger transfer checks.
- Runtime manifest now contains 322 scenarios. All nine GameTests remain `NOT_TESTED` until a Java 17 Forge build and successful `runGameTestServer` log exist.

## 11.63.44 — Required Forge GameTest smoke suite

- Adds six required headless GameTests for Essentia Jar persistence, pedestal-to-node Vis transfer, Node Jar profile persistence, Mana Pod growth/persistence, payload-safe legacy stack migration and two-block Arcane Door access delegation.
- Registers tests through Forge `RegisterGameTestsEvent` under the `thaumcraft` namespace and ships a deterministic empty 9×5×9 structure template.
- Configures ForgeGradle 5 `gameTestServer` with `forceExit false` and adds `runGameTestServer` to both build and release workflows after a successful Java 17 build.
- Runtime manifest now contains 313 scenarios. GameTest cases remain `NOT_TESTED` in this archive until a real Java 17 Forge run produces a successful server log.

## 11.63.43 — Deferred legacy world migration

- Extends safe legacy-ID conversion beyond player login to loaded chunk inventories, block entities, item entities, living equipment, container entities and Ender Chests.
- Preserves the complete serialized ItemStack, including Forge capability payloads, while changing only allow-listed duplicate registry IDs.
- Recursively migrates writable portable `IItemHandler` inventories to a bounded depth of four; read-only or rejecting wrappers are skipped instead of breaking world load.
- Adds explicit migration hooks for Arcane Pedestals, Thaumatorium catalysts and active Infusion Matrix snapshots that are not exposed through ordinary containers.
- Processes at most two already-loaded chunks per level tick through `getChunkNow`, never force-loading neighbours from `ChunkEvent.Load`.
- Persists schema 116343, processed chunks and migration counters through per-dimension `SavedData`; a future schema bump can intentionally replay a new bounded pass.
- Runtime protocol now contains 307 scenarios. Static source/API/resource checks pass; migration remains runtime `NOT_TESTED` until a Java 17 Forge client/server build is available.

## 11.63.42 — Mana Pod cultivation and Magical Forest parity

- Restores planting Mana Beans on the underside of oak, spruce, Greatwood and Silverwood logs inside biomes tagged `thaumcraft:is_magical`.
- Adds the full age 0–7 Mana Pod block and persistent aspect block entity, including the original 1/30 random-growth attempt and age-three aspect inheritance/crossbreeding weights.
- Restores aspect-preserving harvests: no drops at ages 0–1, one attuned bean at ages 2–6 and the original mature one-or-two-bean roll at age 7.
- Restores ten natural Mana Pod generation attempts per fresh Magical Forest chunk, beginning at Y=64 and producing visible ages 3–7 without force-loading neighbouring chunks.
- Restores the original stem/pod textures, age light levels, pulsing inner model, aspect-coloured outer model and mature dynamic Thaumometer aspect exposure.
- Runtime protocol now contains 302 scenarios. Static source/API/resource regression checks pass; no Forge JAR is claimed without a successful Java 17 Gradle build.

## 11.63.36 — Simple Item Mechanics Consolidation

- Replaces 50 remaining item fallbacks with concrete TC4-compatible implementations or existing canonical systems.
- Restores eight active legacy wand foci, blank/reversal focus components, all blank and runic jewellery, both Vis Amulets, Mana Beans, Nitor, essentia phials and the Primal Charm.
- Restores cloth, cultist robe/plate/leader and Void Robe armor families with original material values, texture routes, dye support, vis discounts, source-accurate cultist-robe/Void warp, runic compatibility, Void-hood revealing and Void self-repair.
- Adds optional Curios item tags for rings, necklaces and belts without introducing a hard Curios dependency.
- Reduces the reproducible generic fallback upper bound from 100 to 50 IDs: only eight unique item mechanics and 42 block migration aliases remain.
- Adds six runtime evidence scenarios (273 total). Static source/API/resource checks pass; no Forge JAR is claimed without a successful Java 17 Gradle build.

## 11.63.35 — Research Utilities and Sanity Checker HUD

- Replaces ten generic TC4 migration fallbacks with the existing functional 1.19.2 implementations: both Thaumonomicons, Research Notes, Discovery, both Focus Pouches, Goggles of Revealing, Scribing Tools, Sanity Soap and the Sanity Checker.
- Restores the Sanity Checker main-hand HUD with the original `hud.png` frame, 48-pixel permanent/sticky/temporary warp gauge, violet tints and the 100+ cap marker.
- Preserves legacy stack limits and rarity tiers while keeping menu/screen compatibility through the same concrete item subclasses.
- Restores exact English legacy names and a localized detector tooltip.
- Reduces the reproducible generic fallback upper bound from 110 to 100 IDs: 58 item-like entries and 42 block aliases.
- Adds five explicit runtime evidence scenarios (267 total). Static guards and all 97 retained regression checks pass. A fresh Forge JAR is not claimed because this environment cannot download the Gradle wrapper/dependencies and does not provide JDK 17.

## 11.63.33 — Essentia Phial, Loot Bags and Simple Eldritch Lore

- Replaces seven generic TC4 migration fallbacks with explicit item implementations: the legacy essentia phial, all three loot-bag rarities, Crimson Rites, the Eldritch Eye and the Runed Tablet.
- The legacy essentia phial now shares the real 8-unit jar/alembic/crucible interaction contract, creative aspect variants, two-layer animated model and aspect tint.
- Loot bags preserve the original stack limit 16, rarity levels, 8–12 generated stacks and `thaumcraft:coins` sound.
- Crimson Rites and the two simple Eldritch objects own their behavior directly; the exact legacy eye/tablet IDs are accepted by the altar and lock.
- Fixes a duplicate local-variable declaration in `TC4OuterLandsLootAdapter` that would have prevented Java compilation.
- The reproducible fallback upper bound is 111 IDs. Runtime behavior remains NOT TESTED until a JDK 17 Wrapper build succeeds.

## 11.63.32 — Wand Component Families

- All 26 TC4 wand caps, inert caps, wand rods and staff cores now use the dedicated `TC4WandComponentItem` implementation instead of the generic research fallback.
- Existing `WandComponentData` and Arcane Workbench assembly continue to recognize the exact legacy registry IDs.
- Active caps expose original vis-cost modifiers; inert caps remain rejected until infusion; rods expose capacity and original primal regeneration.
- Original English/Russian names, models and byte-exact textures are retained.
- Static runtime protocol now contains 256 scenarios. No 11.63.32 JAR is claimed without a real JDK 17 Wrapper build.

## 11.63.31 — Simple Consumables and Legacy Jar Label

- Materializes four additional legacy IDs: Zombie Brain, Triple Meat Treat, Knowledge Fragment and the `tc4_label` Jar Label alias.
- Restores Zombie Brain food values, 80% Hunger risk, 10% sticky-warp branch and the original 1–3 permanent-warp branch.
- Restores Triple Meat Treat as always-edible meat with 6 nutrition, 0.8 saturation and a 66% five-second Regeneration chance.
- Restores Knowledge Fragment consumption: independently adds 1–2 points to every primal aspect and synchronizes the client pool.
- Routes `tc4_label` through the real `JarLabelItem` NBT and jar/alembic interaction path; dynamic label recipes accept both canonical and legacy labels.
- Adds family tags and six runtime evidence cases (250 total), reducing the reproducible fallback upper bound to 144 IDs: 102 item-like entries and 42 block aliases.
- No v11.63.31 JAR is claimed until the Gradle Wrapper runs with JDK 17 and completes `clean build` successfully.

## 11.63.30 — Passive Crafting Components

- Materializes six additional passive legacy IDs: Gold Coin, Salis Mundus, Vis Filter, Mirrored Glass, Void Seed and Blank Golem Upgrade.
- Uses explicit item classes instead of `TC4ResearchComponentItem`; all existing recipe IDs remain unchanged.
- Restores original English names, adds a translated blank-upgrade tooltip, uncommon rarity and the original 64-stack blank behavior.
- Adds Thaumcraft family tags for recipe/data-pack interoperability and six runtime evidence cases (244 total).
- Reduces the reproducible fallback upper bound to 148 IDs: 106 item-like entries and 42 block aliases.
- No v11.63.30 JAR is claimed until the Gradle Wrapper runs with JDK 17 and finishes `clean build` successfully.

## 11.63.27 — Arcane Pressure Plate and owner-linked keys parity

- Replaces the flat `tc4_block_arcane_pressure_plate` carrier with a real owner-aware block and persistent block entity.
- Restores the three TC4 modes: everything, everything except authorized players, and authorized players only.
- Restores the 20-tick pressure polling cadence, 1/16 vs 1/32 outline height, strength-15 redstone output and original click pitches.
- Adds owner-only configuration/removal, UUID-based standard/full access lists, piston/explosion protection and NBT/client synchronization.
- Replaces the generic iron/gold key items with functional stack-safe keys: owners bind both types, full-access holders may duplicate iron keys, iron grants trigger access and gold grants full configuration access.
- Adds six runtime evidence cases (226 total) and updates the reproducible fallback audit to 189 upper-bound IDs: 146 item-like entries and 43 block aliases.
- No new JAR is claimed until the Gradle Wrapper completes `clean build` with Java 17 and produces a freshly verified artifact.

## 11.63.26 — Arcane Ear note sensor and redstone parity

- Replaces the flat `tc4_block_arcane_ear` carrier with a real block and `ArcaneEarBlockEntity`.
- Restores the 25-note pitch cycle, five support-material instruments, radius-64 note matching and ten-tick strength-15 redstone pulse.
- Uses Forge `NoteBlockEvent.Play` for server-authoritative detection and a loaded-ear registry that is cleaned on block-entity removal.
- Restores the original blocked-top behavior, volume/pitch formula, manual sound, silent remote particle, powered textures and ten-part Arcane Ear geometry.
- Adds loot, axe mining tag, 3D item model and six runtime evidence cases (220 total).
- Updates the reproducible fallback audit to 192 upper-bound IDs: 148 item-like entries and 44 block aliases. Runtime note propagation, multiplayer sound, redstone timing and save/reload remain `NOT_TESTED` until a real JDK 17 Wrapper build runs.

## 11.63.23 — Arcane Levitator stack and motion parity

- Replaces the flat `tc4_block_levitator` research carrier with a real full-cube block and `ArcaneLevitatorBlockEntity`.
- Restores the original ten-block lift column and adds exactly ten blocks for every contiguous unpowered Levitator below the current unit.
- Stops the column at the first solid-render block and clamps it to the modern world build height.
- Restores the TC4 motion rule: eligible entities gain `+0.1` Y velocity while below `0.35`; sneaking disables lift and damps negative Y velocity by `0.9`; fall distance resets every active tick.
- Restores the original entity family filter for items, pushable entities and horses, with a modern spectator exclusion.
- Redstone power at the Levitator or the block above disables the unit and terminates lower-stack contribution; placement, removal and neighbour changes invalidate the affected stack immediately, with a 100-tick safety recomputation.
- Restores the original top/bottom/side textures, animated two-colour glow overlays and active sparkle emission.
- Adds a real loot table, pickaxe tag, block/item localisation and six runtime evidence cases (208 total). Actual multiplayer motion, collision edge cases, resource reload and save/reload remain `NOT_TESTED` until a Forge build runs.

## 11.63.21 — Hungry Chest inventory, pickup and lid parity

- Routes dropped-item absorption through Forge `ItemHandlerHelper.insertItemStacked`, so the same 27-slot handler is authoritative for world pickup and automation.
- Preserves exact partial remainders and NBT-bearing stacks; rejected items remain untouched and silent.
- Restores the TC4 eat sound at volume `0.25` with triangular pitch `(random - random) * 0.2 + 1.0`, plus block event `(2,2)` and the `0.2` lid nudge.
- Restores the original lid cadence: `0.1` per tick, cubic renderer easing, opening only from fully closed state and closing sound when the angle crosses below `0.5`.
- Adds an 80-tick server recount of players whose active three-row menu belongs to this chest, preventing a disconnected viewer from leaving the lid stuck open.
- Keeps the exact 14×14 inset shape, horizontal facing, 27-slot menu, all-side Forge capability, comparator output, break drops, original ModelChest geometry/UVs and byte-exact `chesthungry.png`.
- Adds six runtime evidence cases (196 total). Collision pickup, hoppers, two-player openers, resource reload and save/reload remain `NOT_TESTED` until a Java 17 Forge build runs.

## 11.63.20 — Tallow Candle coordinate-seeded visual parity

- Replaces the fixed JSON wax-drip geometry of all 16 tallow-candle colours with a dedicated Forge block-entity renderer.
- Restores the exact TC4 body (6/16..10/16, height 8/16) and wick (0.475..0.525, height 8/16..10/16).
- Restores `new Random(x + y * z)`, 1–5 world drips, alternating axes, side choice, two lateral positions and 1–3 pixel heights.
- Keeps inventory/held rendering three-dimensional but deliberately omits world drips, matching the original inventory renderer.
- Preserves no collision, support removal, smoke/flame particles, light level and infusion-stabilizer behavior.
- Adds six runtime evidence cases (190 total); actual atlas orientation, item transforms, multiplayer visibility and resource reload remain `NOT_TESTED` until a Java 17 Forge build runs.

## 11.63.19 — Hover Harness worn-model visual parity

- Connects `HoverHarnessItem.initializeClient` to a dedicated Forge `IClientItemExtensions` armor model instead of relying on the generic chestplate silhouette.
- Restores the original torso shell exactly: an 8×12×4 humanoid body cube using texture offset 16/16, deformation 0.6 and the retained 128×64 `hoverharness.png` atlas; head, arms and legs are suppressed.
- Converts all 124 triangles from the retained TC4 `hoverharness.obj` into immutable modern vertex data with source UVs, V-flip and normals, then renders them with `hoverharness2.png` using the original scale 0.1, −90° X rotation and `(0, 0.33, −3.7)` translation.
- Registers and bakes the armor layer through the client mod event bus and attaches an additional `TC4HoverHarnessLayer` to both default and slim player renderers.
- Restores the active dual-ring effect from the 16-frame `lightningring.png`: a 2.5 white full-bright ring and a reversed 1.5 violet ring offset by 0.03.
- Adds deterministic eight-segment blue-white local arcs that refresh every three ticks and render only while the synchronized `hover` NBT flag is active.
- Adds six runtime evidence cases (184 total). Worn geometry, animation, pose behavior, resource reload and multiplayer visibility remain `NOT_TESTED` until a Java 17 Forge client build is available.

## 11.63.18 — Thaumostatic Harness and Hover Girdle gameplay parity

- Replaces the flat `tc4_hoverharness` and `tc4_hovergirdle` research items with dedicated functional equipment.
- Restores the Harness as an epic 400-durability chest item with three armor points, enchantability 25, gold repair, zero-base runic hardening, 2% general vis discount and 5% Aer discount.
- Restores the original one-slot Harness GUI from `guihoverharness.png`; only a filled essentia jar containing Potentia is accepted, and the exact jar stack persists in the Harness `jar` NBT key.
- Restores the H-key server packet and persistent `hover` state. Flight permission is granted only while the Harness is active and fueled, fall distance is reset, removal or shutdown revokes only flight granted by the Harness, and creative/spectator abilities are preserved.
- Restores the original Potentia charge counter: one unit is consumed after the 360-tick threshold, reduced to 288 with the Hover Girdle. The last unit shuts the Harness down.
- Restores Haste-dependent horizontal damping `min(1, 0.7 + 0.075 * Haste + 0.21 * girdle)`, `hhon`/`hhoff` toggle sounds, periodic `jacobs`, inactive Harness fall-distance damping and the Girdle's `0.33` fall-distance relief.
- Uses the existing reflective Curios/legacy-Baubles bridge; installations without either API retain the project's offhand/inventory compatibility mirror.
- Adds six runtime evidence cases (178 total). Actual flight prediction, ability synchronization, jar GUI interaction, Curios slot behavior, custom Harness armor rendering, two-client play and save/reload remain `NOT TESTED` until a Java 17 Forge build runs.

## 11.63.17 — Boots of the Traveller gameplay parity

- Replaces the flat `tc4_bootstraveler` research item with a dedicated `BootsOfTravellerItem` and the original 350 durability, one armor point, enchantability 25, rare rarity and byte-exact TC4 item/armor textures.
- Restores forward ground acceleration `0.055`, reduced to one quarter in water, plus the original `+0.275` jump impulse and `0.25` per-tick fall-distance relief.
- Restores the one-block step behavior with Forge's synchronized `STEP_HEIGHT_ADDITION` attribute: a transient `+0.4` modifier is applied only while moving forward, not flying and not sneaking, and is removed immediately when the condition or equipment changes.
- Adapts the legacy `jumpMovementFactor` target of `0.05` as an extra `0.03` airborne movement contribution because the old mutable field is not a stable public 1.19.2 contract.
- Keeps the item compatible with the existing Runic Augment and Repair infusion-enchantment systems.
- Adds six runtime evidence cases. Movement prediction, step collision, Hover equipment interaction, multiplayer synchronization and save/reload remain `NOT TESTED` until a Java 17 Forge client/server build runs.

## 11.63.16 — Primal Crusher gameplay parity

- Replaces `tc4_primal_crusher` with a dedicated combined pickaxe/shovel using the original PRIMALVOID material values, 3x3 face-plane mining, fresh-drop cluster conversion, passive repair and Warping 2.
- Routes every expanded break through `ServerPlayerGameMode.destroyBlock`, keeps sneaking as single-block mining and exposes only the original pickaxe/shovel tool actions.
- Corrects the historical warping registry key and retains the original Primal Charm repair path.
- Static guards passed; real client/server mining, protection hooks, cluster drops and save/reload remain `NOT TESTED` without a built JAR.

## 11.63.15 — Sword of the Zephyr and reconstructed 11.63.14 baseline

This stage completes the TC4 elemental tool family on top of the reconstructed 11.63.14 source state. `tc4_elementalsword` is now a real `ElementalSwordItem`: the wide-arc strike attacks nearby hostile living targets through the standard player attack path, while held use creates the original 2.5-block wind sphere, pushes non-player entities and projectiles, softens falling, lifts the wielder, plays the wind effect and costs one durability every 20 ticks.

The source tree also contains the previously completed 11.63.12–11.63.14 gameplay ports: Hoe of Growth, Pickaxe of the Core and Shovel of the Earthmover. A separate Russian cumulative report documents versions 11.63.11 through 11.63.14.

# Thaumcraft Legacy Rebuild

## 11.63.11 — Axe of the Stream gameplay parity

- Replaces the flat `tc4_elementalaxe` research component with a real tier-3 axe using the original elemental material values: 1500 durability, speed 10, +3 material damage and enchantability 18.
- Restores the rare rarity, thaumium repair ingredient, 72000-tick bow-style use action and ten-block loose-item attraction with the original 0.3 acceleration and ±0.35 velocity cap.
- Restores non-sneaking log harvest through the original greedy farthest-connected-log search, including same-block filtering and the 24/48/24 search bounds.
- Uses `ServerPlayerGameMode.destroyBlock` under an internal recursion guard so Forge break/protection hooks and ordinary tool durability remain authoritative.
- Converts only newborn block drops into bubble-type `FollowingItemEntity` instances aimed at the player, preserving their launch motion and applying the original pickup delay.
- Restores the quiet bubble cue, blue stream particles and delayed neighbour ticks used to wake leaf decay; sneaking keeps normal axe behaviour.
- Adds six runtime evidence cases; connected-tree selection, protection-event compatibility, multiplayer particles and save/reload remain `NOT TESTED` until a Java 17 Forge client/server build runs.

## 11.63.10 — Special, permanent and following item entity parity

- Registers the three remaining TC4 item-entity classes: `special_item`, `permanent_item` and `following_item`, bringing the planned original entity registry coverage to 50/50 types.
- Restores the 0.25-block item body, upward-only damping, +0.04 anti-gravity compensation and explosion immunity of `EntitySpecialItem`.
- Routes the first Thaumonomicon, Crucible recipe products and Outer Lands boss rewards through the protected item path instead of ordinary vanilla drops.
- Restores `EntityPermanentItem` lifetime immunity for the Outer Lands key-room object and persists the permanent contract across save/reload.
- Restores target/entity-coordinate following, 20-step homing, no-clip travel, particle type synchronization and NBT state for `EntityFollowingItem`; the current port exercises it through Equal Trade overflow pickup.
- Adds six runtime evidence cases; exact TC4 lightning/sparkle rendering, the original Elemental Axe/harvest call sites and multiplayer save/reload remain `NOT TESTED` until a Java 17 Forge client/server build runs.

- `FollowingItemEntity` sends its initial UUID/coordinate target, homing duration and gravity through `IEntityAdditionalSpawnData`; changing target id and particle type remain in `SynchedEntityData`.
- Crucibles reject `SpecialItemEntity` inputs, matching TC4 and preventing freshly crafted output from being reprocessed.

## 11.63.09 — Falling Taint and crust-collapse parity

- Replaces the taint crust's vanilla `FallingBlockEntity` shortcut with a registered `falling_taint` entity based on TC4 `EntityFallingTaint`.
- Preserves the original source coordinate until the first server tick, so direct and lateral crust collapse remove the source atomically instead of duplicating or prematurely moving it.
- Restores TC4 gravity/drag, landing damping, log-supported fall blocking, replaceable/fibres/goo landing rules, gore sound, ten-particle landing burst and the 600-tick safety timeout.
- Synchronizes the carried block state, persists the source coordinate and fall time in NBT, and renders the moving block from the block atlas at the original full-bright lightmap.
- Adds six runtime evidence cases; chunk-boundary behaviour, Flux Goo landing, save/reload and multiplayer rendering remain `NOT TESTED` until a Java 17 Forge client/server build runs.

## 11.63.09 — Golem fishing bobber lifecycle parity

- Replaces the fishing core's timer-only instant catch with a registered `golem_bobber` entity owned and synchronized by the golem.
- Restores the original launch vector, 300–499 tick active attempt window, quality/strength bite probability, 4000-tick safety lifetime and water-only collision handling.
- Restores five-slice buoyancy, original drag constants, ambient/catch splashes, Air extra-catch and Fire cooking behavior, plus the 1.7.10 fishing loot weights adapted to 1.19.2 items.
- Adds a billboard bobber and the original sixteen-segment black line attached to the golem body-yaw hand position.
- Persists the active bobber UUID, target, quality and remaining attempt time; waiting, GUI pause or core replacement cleans the projectile up server-side.
- Adds six runtime evidence cases; cast/reel animation, multiplayer line synchronization and long-running fishing behavior remain `NOT TESTED` until a Java 17 Forge client/server build runs.

## 11.63.07 — Primal Arrow and golem dart projectile parity

- Replaces the six inert primal-arrow research items with real `ArrowItem` implementations usable by vanilla bows and the Bone Bow.
- Registers a synchronized primal-arrow projectile with the original air/fire/water/earth/order/entropy type order, 2.1 base damage, TC4 damage multipliers and status effects.
- Restores elemental pickup identity, NBT type persistence, coloured flight particles and the original 64-block/20-tick tracking contract adapted to the existing projectile registry pattern.
- Replaces the golem dart launcher's vanilla arrow with a dedicated non-pickup dart entity and the original five-particle launch-smoke cue.
- Adds six runtime evidence cases; projectile damage, multiplayer synchronization and final renderer parity remain `NOT TESTED` until a Java 17 Forge build runs.

## 11.63.05 — Brainy Zombie family parity

- Registers TC4's Brainy Zombie and Giant Brainy Zombie as real Forge entity types with original 25/60 health, 5/7 base damage, armour and reinforcement rules.
- Restores the normal variant's Overworld spawn weight 10 and the original spawn-egg colours; the giant remains spawn-egg accessible until the Eerie biome is completed.
- Restores three and twelve independent rotten-flesh rolls, the brain drop formula, and the giant's thaumium/carrot/potato/amber rare-drop pool.
- Synchronizes the giant's 1.0–2.0 anger value, 0.1 growth on damage, 0.002/tick decay, 7–12 attack curve, collision scaling and renderer scaling.
- Uses the byte-exact original `bzombie.png` texture and reuses the existing TC4 aspect/entity scan IDs so either form can satisfy the hidden JARBRAIN scan trigger.
- Adds six runtime evidence cases; entity AI, spawn distribution, drops, rendering and multiplayer synchronization remain `NOT TESTED` until a Java 17 Forge build runs.

## 11.62.99 — Taint Poison death conversion

- restores the original death replacement table for creepers, sheep, cows, pigs, chickens and villagers;
- converts every other non-tainted living victim into a Thaumic Slime sized from maximum health;
- adds six dedicated tainted entity types with original attributes, hostile targeting and resource drops;
- restores tainted sheep fur/shearing and tainted creeper's 30-tick, non-block-breaking infection burst;
- registers original TC4 textures and vanilla-silhouette renderers with synchronized creeper fuse animation;
- adds six runtime evidence cases; client/server runtime status remains `NOT TESTED` until a Java 17 Forge build can run.

## 11.62.98 — dedicated Taint Poison and infected resources

- registers a dedicated Forge 1.19.2 Taint Poison effect with the original colour, tick cadence, tainted-mob healing and living-target damage;
- routes taint blocks, fibres, swarms and infusion instability through the dedicated effect;
- restores inventory infection and consumption for tainted slime/goo and taint tendrils;
- restores the taint spider's original 1-in-6, two-resource drop branch;
- exports the original potion-atlas icon and adds four runtime evidence cases;
- source/static validation is included; a real Java 17 Forge client/server build is still required for runtime PASS.

## 11.62.94 — alembic and vis-relay model/UV hotfix

- replaces the cuboid Alembic block model with the original TC4 `alembic.obj` geometry and `alembic.png` UV unwrap in world and item contexts;
- replaces the invented cube used by `vis_relay` with the original `vis_relay.obj` renderer;
- fixes OBJ triangle submission to Minecraft's QUADS render mode by emitting a degenerate fourth vertex per source triangle;
- preserves the existing essentia fill overlay and relay gameplay/network behavior;
- runtime screenshots remain required before visual PASS.

## 11.62.93 — wand variants, vis capacity, JEI and textures

The canonical wand item now exposes every craftable TC4 rod/cap combination in the Thaumcraft creative tab, registers its NBT variants with JEI, and supplies NBT-aware outputs for generated Arcane Wand/Sceptre recipes. This prevents non-wood recipes from collapsing to the 25-vis wood/iron fallback and lets the existing BEWLR select the correct byte-exact original rod/cap textures.


## v11.62.92 full texture/UV audit and confirmed visual hotfixes

- audits all 921 original TC4 PNG/MCMETA resources, 1,653 active port texture resources, 988 JSON models, 70 custom renderer files, 25 GUI screens and 202 blockstate render-layer entries;
- restores Wisp 4x4 frame UV selection, original particles.png halo row, additive full-bright layers, original scale/pulse and hurt tint;
- restores the original Alchemical Furnace GUI texture, four gauge/overlay UV regions and original input/fuel slot positions;
- includes machine-readable CSV reports, proof images and an explicit runtime evidence checklist;
- static audit does not award runtime visual PASS without side-by-side client screenshots.

## 11.62.92 — Wisp FlyingMob spawn-rules compile hotfix

Fixes GitHub Actions run 79798875408: `TC4WispEntity` extends `FlyingMob`, so its `EntityType<TC4WispEntity>` cannot be passed to `Monster.checkMonsterSpawnRules`, whose type bound is `EntityType<? extends Monster>`. The predicate now preserves the same hostile-spawn contract by checking peaceful difficulty, `Monster.isDarkEnoughToSpawn`, `Mob.checkMobSpawnRules`, and the original nearby-Wisp cap separately.

## v11.62.92 Forge compile hotfix — Monster spawn rule signature

GitHub Actions run `79795527703` reached `:compileJava` and exposed one independent blocker in `TC4WispEntity`: the Forge/Mojmap 1.19.2 `Monster.checkMonsterSpawnRules` overload requires the complete five-argument spawn context (`EntityType`, `ServerLevelAccessor`, `MobSpawnType`, `BlockPos`, `RandomSource`). The old three-argument call was replaced without changing Wisp density or biome-spawn behavior.

A dedicated guard now rejects the unavailable three-argument call before Gradle starts.

## v11.62.84 Forge compile hotfix — Traveling Trunk and Taint Spore

- Fixes the root `compileJava` blocker from GitHub Actions run `79587666588`: `TamableAnimal` now uses the Forge/Mojmap 1.19.2 package `net.minecraft.world.entity`.
- Removes the cascade of entity/type errors caused by Traveling Trunk failing to extend a valid Minecraft entity class.
- Adds a stable public `TaintSpreadRuntime.isColumnTainted(ServerLevel, BlockPos)` contract and updates `TaintSporeEntity` to use it.
- Retains `isTaintedColumn` as a compatibility wrapper for existing spread code and older patches.
- Extends the early Forge compile-API guard and adds `tc4_116283_forge_compile_hotfix_guard.py`.
- All 49 pre-build/static checks pass; a new networked CI run is still required to confirm the next `compileJava` stage and produce a JAR.

## v11.62.82 Research note and Research Table workflow parity

- Restored the original TC4 research start path: available primary research creates a targeted note in the player inventory, but does not open a freehand puzzle.
- Unfinished research notes now require placement in a Research Table; item use only resolves unknown notes or consumes a completed discovery.
- Removed the rebuild-only empty-slot note creation and shift-click learning actions from the table GUI while preserving the physical two-slot container.
- Restored the original `thaumcraft:learn` sound when a note is created from the Thaumonomicon.
- Restored Brain in a Jar as a 1/200 random research-table bonus-aspect source alongside the bookshelf 1/300 source.
- Fixed an unreachable duplicate `return false` compile blocker and hardened the fixed-size two-slot inventory reset/load path.
- Added three runtime evidence cases and `tc4_116282_research_workflow_parity_guard.py`; runtime status remains `NOT TESTED`.



## v11.62.81 Warp event, ward migration and Death Gaze parity hardening

- Restored Warp Ward as a real synced effect instead of a duplicated persistent timer; legacy timers migrate once and are cleared so milk/commands work like TC4.
- Preserved TC4's rare sticky-warp cleansing event without incorrectly resetting the warp event counter.
- Restored the original 50-attempt signed three-axis spawn search for Eldritch Guardians and Mind Spiders; invalid locations no longer force a fallback spawn.
- Restored Death Gaze's forward visibility cone (`dot >= 0.75`), line-of-sight/PvP checks, attacker attribution and hostile aggro.
- Expanded research synchronization to permanent, sticky and temporary warp plus the event counter instead of sending total warp only.
- Added three runtime evidence cases and `tc4_116281_warp_eldritch_parity_guard.py` (50 source/network checks).
- The original client post-processing packets/shaders and full event-distribution multiplayer proof remain `NOT TESTED`.


## v11.62.80 Taint ecology, fibre geometry and spore lifecycle parity

- Restored TC4 fibre metadata states as distinct geometry/textures: face-attached film, two grass stages and two spore-stalk stages.
- Restored crust/soil conversion thresholds (2/3 adjacent taint), persistent tainted-column spread and original out-of-taint decay.
- Restored crust falling/log support rules, original hardness/drop behavior and TC4 poison probabilities.
- Replaced the block-rendered crawler placeholder with the original small taint-spider model/texture/eyes and 5-health/2-damage attributes.
- Added stationary, growing taint spores anchored to mature stalks; broken or detached spores burst into taint spiders and reduce the stalk to age 3.
- Added three evidence-backed runtime cases and `tc4_116280_taint_ecology_parity_guard.py`.
- The modern biome-colour/weather layer and flying Spore Swarmer remain known deviations; runtime status remains `NOT TESTED`.

## v11.62.79 Essentia transport pressure and processing parity hardening

- Restored the TC4 reservoir suction value of 24, six-direction access face, DOWN default and five-tick active one-unit pull.
- Reservoir active pull now accepts every supported adjacent essentia source, not only a regular tube, and rolls a unit back when the destination rejects it.
- Buffer filling compares its per-side bellows/choke suction against the neighbour's real suction and minimum suction instead of an artificial source-priority score.
- Buffer extraction preserves the requested face, so choke and competing-output arbitration are not bypassed.
- Reservoir suction remains untyped, while jar/reservoir source resistance uses their original minimum-suction contracts.
- Alchemical Centrifuge can queue the next compound aspect while output is occupied or redstone pauses processing, matching TC4 input semantics.
- Added three dedicated runtime evidence cases and `tc4_116279_essentia_transport_parity_guard.py`.
- Source status remains `PARTIAL`; suction conflicts, rollback, redstone queueing and long-running network behavior remain `NOT TESTED`.


## v11.62.78 Golem core runtime parity hardening

- Corrected USE-core marker side resolution to match TC4 `AIUseItem`: the free side is checked in `marker.side`, while empty-space markers act on the block behind the marked air cell.
- Restored empty-hand USE-core operation when the home has no inventory, and preserved the first FakePlayer by-product as the golem's carried stack.
- Restored priority-zero avoidance of swelling or ignited creepers for every functional golem core.
- Corrected fishing-core probability order (junk before treasure), water-quality modifiers, sky/depth bonuses and weighted junk selection.
- Added three dedicated runtime evidence cases and the `tc4_116278_golem_core_parity_guard.py` CI regression guard.
- Source status remains `PARTIAL`; runtime gameplay/network/visual parity is `NOT TESTED` until a successful Forge build and evidence-backed client/server tests exist.


## v11.62.77 Outer Lands generation and boss-lock cycle hardening

- Outer Lands entry coordinates are now aligned to TC4 maze cells; the player arrives beside the portal generated at local `(8, 3, 8)` instead of a duplicate corner portal.
- `GenPortal` stepped floor/ceiling, corner shafts and lower/upper stair triplets are restored.
- Chunk population no longer creates a new maze around every loaded player chunk. Maze creation is restricted to the entry portal path.
- Boss-room generation now places an Eldritch Lock (original palette code 16) and does not spawn a boss during world generation.
- The lock accepts only the flattened `Eldritch Eye` equivalent of `ItemEldritchObject:2`, preserves the 100-tick pump sequence and resolves the actual 2×2 boss-room center before spawning.
- These are source-level corrections only. Outer Lands remains `PARTIAL / NOT TESTED` until Forge build, portal traversal, boss cycle, save/reload and return are demonstrated with runtime artifacts.


## v11.62.76 Compile-risk hardening and evidence protocol

- Traveling Trunk uses Forge `ITEM_HANDLER`, Mojmap `isOnGround()`, fixed-size inventory clearing and silent NBT restoration.
- Outer Lands live population no longer requests unloaded chunks and records completion only after generation succeeds.
- Runtime evidence validation requires SHA-256-verified files for every PASS, PARTIAL or FAIL result.
- Added deterministic TC4-vs-port screenshot diff output (MAE, RMSE, exact pixels and global SSIM); human review remains mandatory.
- Build/runtime status remains FAIL/NOT TESTED until a networked Forge CI build and game artifacts exist.

## v11.62.75 Objective P0 implementation pass

- adds NBT-aware Forge BEWLR rendering and pickup/place persistence for essentia jars;
- makes raw aura nodes migration-only and keeps Node in a Jar as the player-facing dynamic item;
- restores the TC4 Bone Bow charge curve and pull-stage models;
- replaces crimson-cultist block placeholders with humanoid robe/plate/leader render layers;
- replaces the vanilla fortress-armor approximation with a dedicated ModelPart geometry and NBT mask/goggles selection;
- restores Traveling Trunk entity, inventory, AI, lid/leg model and item BEWLR;
- connects Outer Lands live population to the Forge server tick and persists populated chunks;
- introduces `audit_visual_parity.py` and `TC4_PORT_STATUS_V3.md`.

**Evidence rule:** these changes are STATIC IMPLEMENTED until a successful Forge build and named runtime artifacts exist.

## v11.62.74 Forge compile hotfix — Brain Jar radian wrapping

- fixes the two real `compileJava` errors reported by GitHub Actions run `79417104482`;
- removes calls to the unavailable Minecraft/Forge 1.19.2 API `Mth.wrapRadians(float)`;
- keeps Brain in a Jar rotation in radians and normalizes angular deltas to `[-π, π)` with a local Java 17 helper;
- avoids the incorrect alternative of passing radian values to `Mth.wrapDegrees`;
- extends the Brain Jar regression guard so future releases reject reintroduction of `Mth.wrapRadians`;
- preserves all v11.62.73 mirror runtime changes unchanged.

GitHub Actions reached the real Forge `compileJava` task. The uploaded log contained exactly two errors, both in `BrainJarBlockEntity`, plus 100 deprecation warnings that did not cause the build failure. A new CI run is still required to reveal any later-stage errors after these two compiler blockers.


## v11.62.73 Magic Mirror / Essentia Mirror / Hand Mirror runtime parity

- Added reciprocal, dimension-safe stationary mirror links with NBT-preserving drops.
- Ported regular mirror item transport, delayed random output queue, instability decay and Ordo stabilization.
- Ported the source-only Essentia Mirror with the original forward half-space search radius of eight blocks.
- Added the original one-slot Hand Mirror menu at slot `(80, 24)` and remote item ejection.
- Exposed the regular mirror as a Forge `IItemHandler` insertion endpoint for Forge automation and ported golems.
- Prevented client-side ghost drops when the Hand Mirror GUI closes with an item still in its slot.
- Added six-face thin block geometry, support checks, linked/unlinked pane models and tube-network integration.
- Cross-dimension lookups never force-load or generate destination chunks.

Target: **Minecraft 1.19.2 / Forge 43.5.2 / Java 17**.

This revision continues the source-driven port against the supplied TC4 4.2.3.5 reference source. It replaces the three mirror placeholders with a linked Forge 1.19.2 runtime subsystem and integrates item and essentia transport with the existing port.


## v11.62.72 Brain Jar / infusion XP / valve runtime parity

- replaces the `tc4_jar_brain` migration placeholder with a real BlockEntity-backed Brain in a Jar;
- ports the original 2000-XP capacity, six-block orb attraction, touch absorption, random click release, break release, NBT sync, comparator output and +2 enchanting power;
- restores the original animated brain geometry, brine and TC4 brain/jar sounds;
- fixes infusion-enchantment XP drain so creative players without levels no longer advance the matrix;
- keeps a powered essentia valve topologically connected while suppressing fresh suction, matching `TileTubeValve`.

## v11.62.71 GUI / infusion parity hotfix

- restores the original TC4 horizontal page offsets: text `x-15`, aspects `x-8`, recipes `x-4`;
- restores long-title Y scaling from `GuiResearchRecipe`;
- rebuilds infusion pages around the original `(56,102)` centre, renders every component on the dynamic 40 px ring, and restores original output/input/aspect positions;
- adds the five omitted original infusion registrations: `JarBrain`, `Mirror`, `MirrorHand`, `MirrorEssentia`, and `TravelTrunk`;
- corrects `itemTrunkSpawner` and `blockJar:1` legacy resolution so research pages no longer show a wooden golem or ordinary jar as the result;
- makes project, mod metadata and CI release artifacts consistently report `11.62.71`;
- adds a source-parity regression guard that rejects the previous shifted-page and eight-component-limit regressions.

Status remains **PARTIAL / STATIC PASS** until Forge compilation and in-game client/dedicated-server tests are completed. Brain in a Jar now has its block/entity runtime; the two mirrors and Traveling Trunk still require their teleportation, inventory and entity behaviour.

## v11.62.69 CI hotfix 3

- fixes the six `compileJava` errors reported by GitHub Actions run `79321665380`;
- uses the Forge 1.19.2 `ChestMenu` API for Hungry Chest instead of the unavailable `GenericContainerMenu`;
- imports `UseAnim` from `net.minecraft.world.item` for Sanity Soap;
- keeps the concrete `PurifyingFluidBlock` registry type required by `ForgeFlowingFluid.Properties#block`;
- adds an early Forge 1.19.2 compile-API guard to both build and release workflows.


## v11.62.69 focus

- ports the original `ContainerSpa` as `ArcaneSpaMenu` with one Bath Salts slot at `(65,31)`, the original player-inventory coordinates and exact shift-click filtering;
- ports `GuiSpa` using the original `gui_spa.png`, `176×166` viewport, mix/dispense button, tank gauge, fluid and mode tooltips;
- synchronizes mix mode, tank amount and fluid registry id through three menu data values;
- renders the stored fluid through `IClientFluidTypeExtensions`, including the fluid texture and tint;
- restores button id `1` and the original `cameraclack` sound;
- replaces temporary Shift-right-click/salt/direct-FluidUtil controls with the original GUI workflow;
- removes Purifying Fluid from `#minecraft:water` and creates the dedicated `#thaumcraft:purifying_fluid` tag;
- documents two original registrations in this subsystem: `BathSalts` and `ArcaneSpa`, with static coverage **2/2**.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.68 focus

- registers Purifying Fluid as a real Forge source/flowing fluid, luminous block and bucket using the existing original animated texture;
- restores Bath Salts' 200-tick dissolution in a vanilla water source and conversion of that source into Purifying Fluid;
- restores the single-use Purifying Fluid source collision formula that grants Warp Ward according to permanent Warp and consumes the source;
- restores the original white bubble and lava-pop ambient fluid effects;
- ports the Arcane Spa as a 5000 mB block entity with one Bath Salts slot, 40-tick cadence, redstone disable, mixing/dispensing modes and 5x5 source expansion;
- exposes side fluid/item automation while keeping the top closed, and synchronizes tank, salts and mix state through block-entity NBT;
- connects the already-ported `BathSalts` alchemy and `ArcaneSpa` Arcane Workbench recipes to functional registered outputs and therefore to their existing JEI categories;
- corrects the previously broken Arcane Spa symbol map to the original `QIQ/SJS/SPS` piston/jar/arcane-stone/quartz/iron-bars recipe;
- completes the Purifying Fluid hook already used by Sanity Soap's permanent-Warp cleansing bonus.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.67 focus

- restores the original 2000-tick Warp check, square-root counter roll, counter reduction and temporary-warp decay;
- includes held, armor and optional Baubles/Curios warping gear in event severity;
- registers all eight TC4 Warp effects with original colors, icons, durations, tick behavior and curative rules;
- restores Flux Flu wand-cost penalties, Flux Goo/Gas effects, Death Gaze, Thaumarhia, Sun Scorned and infectious spread;
- restores harmless viewer-only Mind Spiders and the original event-range gaps;
- ports the 200-tick Sanity Soap use sequence, particles/sounds, complete temporary-warp removal and sticky-warp chance;
- adds the TC4 `wuss` equivalent config switch for random Warp events.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.66 focus

- provides a complete 109-row registration ledger for `initializeArcaneRecipes()`: 84 literal shaped call sites expand to 104 shaped registrations after the 16-banner and 6-arrow loops, plus 5 shapeless registrations;
- explicitly separates the three normal `ArcaneStone2..4` recipes and all 24 infusion-enchantment recipes from the Arcane Workbench count;
- registers the original `thaumcraft:repair` and `thaumcraft:haste` enchantments with TC4 level caps, enchanting costs and applicability rules;
- restores the 40-tick vis-powered Repair effect, original primal-aspect cost formula and the carried-hover-harness exception;
- restores the Haste movement impulse and airborne/water reductions;
- replaces the broad repair namespace approximation with the data-driven `thaumcraft:repairable` equipment tag;
- improves JEI infusion-enchantment entries with representative central items and visibly enchanted outputs;
- adds a full 24-row infusion-enchantment parity ledger.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.65 focus

- replaces the flat `tc4_block_banner` mirror with a placeable floor/wall banner block, persistent colour/aspect NBT, cloth sway, original model geometry and custom item rendering;
- ports all 16 exact `Banner_0..15` arcane recipes with wool metadata parity, AQUA/TERRA costs, research gating and NBT-coloured outputs in JEI;
- adds result-NBT support to the Arcane Workbench recipe adapter so crafted and JEI outputs retain TC4 metadata;
- restores the exact 256-mask/47-texture owner-aware connected-texture selection for Warded Glass;
- replaces the Vis Charge Relay cube with triangle data converted from the original `vis_relay.obj`, original support transforms and a synced five-tick primal-aspect crystal pulse;
- adds BEWLR rendering for banner and relay item forms.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.64 focus

- functional 27-slot Hungry Chest with item absorption, comparator output, lid/eating animation, original chest texture and custom item rendering;
- functional Vis Charge Relay / Arcane Workbench Charger that joins the relay network and charges the wand slot below it;
- owner-bound, explosion-proof Warded Glass removable by its owner with a wand;
- exact original `HungryChest`, `NodeChargeRelay` and `WardedGlass` arcane recipes, including aspect costs and research keys;
- corrected TC4 metadata resolver mappings for metal-device relay metas, warded glass, hungry chest and the vanilla trapdoor field.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.63 focus

- corrects all 16 tallow candles to light level 14, restores smoke/flame particles and makes them effective infusion stabilizers;
- replaces the item-grate state stub with a real block entity exposing an insertion-only upper item handler while open, immediate downward item ejection and redstone open/close parity;
- ports `KnowFrag`: nine knowledge fragments craft an unknown note whose first use finds eligible hidden triggered research or returns 7–9 fragments;
- ports the 48 aspect-coded Jar Label recipes and label reset as one NBT-aware custom serializer;
- adds a dedicated JEI category with 48 aspect assignments plus one reset display;
- provides a registration-level normal-recipe ledger: 86/86 non-dynamic original registrations mapped, while the 49 NBT label registrations are represented by one runtime serializer and 49 JEI views;
- retains the release status as **PARTIAL / STATIC PASS** until Forge compilation and runtime tests succeed.


## v11.62.62 focus

- ports or corrects 40 original construction and utility recipe entries;
- adds functional Greatwood/Silverwood stairs and slabs;
- replaces the legacy Thaumium storage block, Tallow block and balanced crystal-cluster item mirrors with real placeable blocks under their exact saved registry ids;
- adds Amber Block, Amber Bricks and the original item grate with an open/closed state;
- ports all 16 animated tallow-candle colours as separate modern blocks, including dye conversion and whitewash recipes;
- corrects Essentia Phial to the original clay/glass pattern and output count of eight;
- corrects Table inputs to accept the vanilla wooden slab/plank tags;
- removes six non-original reverse crystal recipes and restores original six-shard cluster assembly;
- exposes 125 standard shaped/shapeless recipes through JEI's normal crafting category;
- adds `tools/tc4_116262_construction_recipe_jei_guard.py` and machine-readable audit output.


## v11.62.61 focus

- ports 39 original normal-crafting entries: metal compaction, mundane baubles, four Triple Meat Treat alternatives, plant conversions, Greatwood/Silverwood planks, flesh block, Jar Label, and the complete Thaumium/Void armor and tool sets;
- exposes all normal recipes through vanilla shaped/shapeless recipe types, allowing JEI to list them in its normal crafting category without duplicate custom registrations;
- replaces the 18 inert Thaumium/Void equipment mirrors with real `ArmorItem`, `SwordItem`, `PickaxeItem`, `AxeItem`, `ShovelItem` and `HoeItem` implementations;
- restores original TC4 material statistics and armor textures;
- restores Void gear passive repair, Weakness-on-hit behavior and Warping 1 integration;
- replaces active duplicate ingot/nugget recipe ids with canonical modern carriers and adds safe old-stack migration mappings;
- removes legacy Greatwood/Silverwood placeholder ids from active Arcane and Infusion recipes;
- adds `tools/tc4_116261_normal_recipe_jei_guard.py` and machine-readable audit output.


## v11.62.60 focus

- ports the original conditional Pure Tin, Pure Silver and Pure Lead crucible recipes through modern `forge:ores/*` item tags;
- teaches the crucible runtime and JEI category to consume tag-backed catalysts instead of reducing legacy ore-dictionary recipes to one hard-coded item;
- ports the exact Node Relay and Void Essentia Jar arcane recipes;
- repairs the original Primal Charm result and symbol mapping, Focus Primal catalyst, all eight staff-rod shapes/costs, Essentia Crystalizer balanced shard, and Sinister Stone catalyst/result;
- corrects `ConfigItems.itemResource:15` and `itemCompassStone` resolver mappings;
- de-duplicates generated wand recipes by original TC4 key so JEI no longer shows stale materialized copies beside the authoritative runtime recipe;
- adds `tools/tc4_116260_recipe_jei_guard.py` and a machine-readable audit report.

## v11.62.59 focus

- removes the non-original world-space nameplate rendered above aura nodes;
- restores the original wand equipped origin and removes the accidental half-scale first-person transform;
- fixes the Thaumometer first-person near-plane offset and keeps node data on its glass;
- requires the same exact ray target for the full scan instead of allowing look-away completion;
- removes physical Research Point rewards and the non-original scan chat dump;
- restores missing `.png.mcmeta` files for animated item strips;
- removes disabled duplicate recipe stubs and non-original Research Point/Cache recipes;
- materializes the first three missing original smelting recipes (magical logs, cinnabar, amber).

## v11.62.54 focus

- original top-left wand dial (`wandDialBottom=false`), with optional bottom-left placement;
- original six primal reservoirs, cost/change markers and sneaking values;
- focus icon or Equal Trade picked block in the dial centre;
- focus cooldown seconds in the original half-scale position;
- client HUD state reset on disconnect;
- original `TreeMap` focus sort/overwrite behaviour;
- transactional focus swaps: a full pouch/inventory cannot eject or lose the installed focus;
- exact rollback to the source inventory/pouch slot if a swap cannot complete.

## Validation

The committed CI audits use only the Python standard library; no third-party Python packages are required.

```bash
python3 tools/java_syntax_guard.py
python3 tools/validate_json_resources.py
python3 tools/tc4_116269_arcane_spa_gui_guard.py --version 11.62.69
python3 tools/tc4_116266_arcane_enchantment_guard.py --version 11.62.66
python3 tools/tc4_arcane_recipe_coverage_116266.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_arcane_recipe_full_mapping_v11.62.66.json \
  --md-out reports/TC4_ARCANE_RECIPE_FULL_MAPPING_V11_62_66.md
python3 tools/tc4_infusion_enchantment_coverage_116266.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_infusion_enchantment_coverage_v11.62.66.json \
  --md-out reports/TC4_INFUSION_ENCHANTMENT_COVERAGE_V11_62_66.md
python3 tools/tc4_116265_banner_ctm_relay_guard.py --version 11.62.65
python3 tools/tc4_arcane_recipe_coverage_116265.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_arcane_recipe_coverage_v11.62.65.json \
  --md-out reports/TC4_ARCANE_RECIPE_COVERAGE_V11_62_65.md
python3 tools/tc4_item_visual_audit.py --version 11.62.65 --fail-on-missing
python3 tools/model_transform_audit.py --version 11.62.65 --fail-on-problems
python3 tools/bewlr_contract_audit.py --version 11.62.65 --fail-on-problems
python3 tools/audit_registry.py --version 11.62.65 --fail-on-unexpected
```

## Full build

```bash
chmod +x gradlew
./gradlew build --stacktrace --no-daemon
```

Expected output:

```text
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.69.jar
```

Audit reports are generated on demand into `reports/` and are not stored in the source archive.

## v11.62.54-hotfix1

This packaging hotfix repairs an invalid standalone prose line in `META-INF/mods.toml` that caused Forge to abort mod discovery and the launcher to report exit code 1. Gameplay classes are unchanged from v11.62.54. The known focus-transaction ordering defect D-001 remains open.


## v11.62.54-hotfix2

This source hotfix addresses the startup crash reported as `AbstractMethodError` in `BlockEntityRendererProvider`. Direct renderer, entity-renderer, menu-screen and colour-handler lambdas no longer implement obfuscated Minecraft interfaces themselves. They now target stable JDK/project interfaces and are forwarded through explicit adapter overrides that ForgeGradle can reobfuscate safely.

The crash was in the mod bootstrap bytecode, not in Oculus, Rubidium or the world save. Build this source through the included GitHub Actions workflow and use the produced reobfuscated JAR. The known focus transaction ordering issue D-001 is still tracked separately.

## v11.62.54-hotfix3

This CI/release hotfix repairs a stale source guard that rejected the required SRG-safe Research Table renderer adapter. The guard now verifies the wrapped registration and the explicit `BlockEntityRendererProvider` bridge instead of demanding the forbidden direct Minecraft SAM constructor reference. Gameplay code and assets are unchanged from hotfix2.


## v11.62.54-hotfix4

Adds an Alchemical Furnace menu/screen and revises the Crucible, Arcane Pedestal and Arcane Workbench block models after runtime screenshot review. Adds a complete item-model/texture-reference audit.


## v11.62.54-hotfix5

Fixes research puzzle parity: identical aspects no longer connect, new placements must touch a compatible occupied hex, Research Expertise reveals compound components and keeps its 25% removal refund, while Research Mastery keeps the 50% refund, adds the original 10% free placement chance and Shift-click automatic combination shortcut.

## v11.62.54-hotfix6

- Generates the exact 201-entry TC4 4.2.3.5 research graph, metadata and all 591 ordered ResearchPage declarations from the committed original source map.
- Audits every research icon, both language sets and the original Thaumonomicon GUI textures.
- Restores original concealed-page removal, dynamic known-aspect pages and research tooltip cues.
- Re-centres all WandItem BEWLR meshes so iron, greatwood, silverwood and creative wands render in inventory and first person.

## v11.62.54-hotfix7

- Removes the duplicated `PRIMPEARL` item trigger from both committed source maps and generated runtime metadata.
- Regenerates the machine-readable full research audit with duplicate-trigger detection.
- Normalizes the consolidated report revision history, numbering and hotfix appendices.
- Clarifies that `gui_research.png` supplies frames/tabs while research icon content may come from an `ItemStack` or a standalone resource texture.


## 11.62.96 — P0 item geometry / UV hotfix

- Bellows item: original five-part ModelBellows geometry and per-part UV normalization via BEWLR.
- Alchemical Centrifuge: exact six-part ModelCentrifuge UV layout; full 3D animated item preview.
- Infusion Matrix: real eight-piece ModelCube geometry in all item contexts.
- Brain in a Jar: real jar + brine + original ModelBrain instead of a flat icon.
- Essentia Reservoir: original reservoir OBJ/UV for world and item.
- Advanced Alchemical Furnace: compact centered original OBJ for item contexts.
- Runtime visual parity remains NOT TESTED until client screenshots are captured.

## 11.62.97 — Taint Spore Swarmer / Taint Swarm ecology

- Ports TC4's crust-grown Taint Spore Swarmer as a separate full-bright entity with the original two-cube geometry and `taint_spore.png` texture.
- Restores the 1-in-200 taint-crust ecology branch: one Swarmer may replace crust when no other Swarmer is within 16 blocks.
- Ports the invisible particle-driven Taint Swarm, including flying pursuit, poison melee, summoned decay and original swarm sounds.
- Restores the Swarmer's 500-tick release cycle, death release, and two-item taint material drop.
- Source/static validation is included; gameplay timing, renderer blending and multiplayer synchronization remain NOT TESTED until a Forge 1.19.2 client JAR is built.


## 11.63.03 — runtime visual and Outer Lands loading hotfix

- Rebased the wand/sceptre first- and third-person BEWLR transforms into bounded 1.19.2 item-local space; the legacy `+1.5Y` equipped matrix is no longer applied twice.
- Node Transducer world and item rendering now share the original TC4 OBJ/UV path. Its coloured overlay again uses the legacy lightmap curve instead of forced full-bright translucent slabs.
- Alembic inspection text is sent to the action bar, preventing repeated permanent chat lines.
- Outer Lands entry synchronously creates only the portal room. The previous five-by-five maze generation before teleport was removed, and nearby rooms now populate one successful chunk per 40-tick pass to avoid the indefinite **Loading terrain** stall.
- Runtime verification remains required on Java 17 / Forge 43.5.2 for all supplied screenshot cases.

## 11.63.02 — Magical Forest biome parity

- Restores the original TC4 default biome weight (5) in both WARM and COOL
  Forge pools instead of the rebuild-only WARM/8 registration.
- Restores TC4 4.2.3.5 temperature/downfall and exact grass, foliage and water
  colours; removes the invented purple fog.
- Keeps Flower Forest available by splitting compatible 1.19.2 multi-noise
  climate points instead of replacing every Flower Forest point.
- Restores the Magical Forest additions to the spawn table: wolves, horses,
  witches, endermen, Pech and Wisps with the original weights/group sizes.
- Adds regression checks for locate visibility, climate-table preservation,
  original colours, pool registration and biome-spawn data.

## 11.63.01 — complete wand catalogue and legacy UV geometry

- Every craftable rod/cap pair is exposed in creative and JEI in both normal
  and sceptre form; subtype identity excludes stored vis and focus state.
- Crafted results keep the original root `rod`, `cap` and `sceptre` NBT, so
  capacity follows the rod (25/50/75/100 for wands, staff capacities, and the
  original 1.5x sceptre multiplier) instead of falling back to 25.
- The BEWLR now reproduces the mirrored 1.7.10 `ModelBox` vertex/UV ordering
  and supplies per-face transformed normals, fixing stretched/mirrored caps
  and flat lighting on rods in GUI, hand, ground and item-frame contexts.
- Fixed the Forge 1.19.2 `Heightmap` import that prevented v11.63.00 sources
  from compiling.

## 11.63.00 — Liquid Death and TC4 damage-source identities

- Ports Liquid Death as a registered Forge source/flowing fluid with the original animated texture and bucket sprite.
- Restores the four-step 4/3/2/1 dissolve-damage ladder, purple bubble/pop feedback and dispenser-compatible bucket.
- Restores aspect crystal drops for entities dissolved by the fluid.
- Replaces generic magic/melee attribution with TC4's named `taint`, `tentacle`, `swarm` and `dissolve` sources.
- Source/static validation is included; exact finite-fluid conservation and runtime rendering remain NOT TESTED.


### 11.63.05
Restores TC4's dedicated Eldritch biome and Inhabited Zombie lifecycle for Outer Lands, including original spawn weights, cultist armour and helmeted Eldritch Crab release on death.

## 11.63.22 — Arcane Bellows six-direction runtime parity

- Replaces the static world-only Bellows shell with a real `BellowsBlockEntity` and original five-part animated renderer.
- Restores floor, ceiling and four wall orientations from the clicked face.
- Restores TC4 inflation timing, quiet ghast-fireball sound and redstone pause.
- Restores placement-time detection and +1 cook progress every two ticks for the normal vanilla furnace.
- Powered Bellows no longer contribute to the Alchemical Furnace or essentia buffer tube.
- Runtime collision, furnace timing, renderer transforms, multiplayer and save/reload remain `NOT_TESTED` until a compiled Forge client/server artifact exists.


## 11.63.24 — Arcane Spa fluid and bath parity

Arcane Spa now uses an independent 40-tick block-entity cadence, six-sided source adjacency, safe replaceable output positions, preserved 5000 mB/1000 mB fluid accounting, Bath Salts expiry conversion and the original one-slot GUI/capability contract. The runtime template contains 214 scenarios.

## 11.63.28 — Arcane Lamp

- Replaces the `tc4_block_arcane_lamp` research fallback with a real six-face attached block and two server block entities.
- Restores TC4's one-attempt-per-tick triangular coordinate selection, surface+4 / Y=5 clamp, darkness threshold 9 and invisible level-15 light markers.
- Each marker stores its source coordinates and validates the source on a randomized 100-tick phase; removing the lamp clears the original 31×31×31 marker cube.
- Restores the original animated side/top textures and the block body plus two-part Bore nozzle in a rotatable three-part JSON model.
- Adds the self-drop loot table, pickaxe tag, English/Russian names and six runtime scenarios. Runtime lighting, chunk-boundary behavior and multiplayer visuals remain `NOT_TESTED` until a real Forge JAR is built.
- Restores six worldgen Java files accidentally omitted from the v11.63.27 source ZIP and restores the byte-identical Gradle Wrapper JAR from v11.63.25.


## 11.63.29 — Simple Resource Families

- Replaces 33 generic research-component fallbacks with concrete passive resource, edible nugget, shard and ore-cluster items.
- Restores TC4 meat-nugget food values and 10-tick eating duration.
- Adds Forge compatibility tags for nuggets, clusters, shards, thaumium/void ingots, amber, quicksilver, tallow and enchanted fabric.
- Stops join-time conversion of the newly materialized TC4 IDs into parallel rebuild IDs.
- Corrects the remaining-object auditor so grouped Java switch cases are counted completely; the reproducible upper bound is now 154 IDs.
- Adds six runtime scenarios; actual food, recipe/tag interoperability, save/reload and multiplayer remain `NOT_TESTED` without a successful JDK 17 Wrapper build.

## 11.63.37 — Unique Item Systems and Item Fallback Closure

- Closes the remaining eight item-like legacy IDs. The reproducible generic-fallback upper bound falls from 50 to 42, with **zero item-like fallbacks**; every remaining entry is a block migration alias.
- Ports the two-block Arcane Door with UUID owner/access persistence, iron/gold Arcane Key integration, protected breaking and operation from an adjacent authorized Arcane Pressure Plate. Ordinary redstone does not open the door.
- Restores the Crimson Blade's TC4 tier values, 200 durability, one-point-per-second self repair, primal-charm repair, Wither/Weakness hit effects and warp value 2.
- Routes `tc4_ironbell` through the existing Golemancer's Bell runtime and implements the Essentia Resonator readout for jars, reservoirs, alembics and tube faces.
- Restores the creative Eldritch Obelisk Placer and validates every block position that the reconstructed structure actually replaces.
- Restores the Sinister Lodestone active model state for visible DARK nodes within 256 blocks. The old `tc4_sinister_stone_active` sprite and `tc4_lightningring` renderer sprite remain hidden save-migration aliases instead of receiving invented gameplay.
- Runtime evidence remains `NOT_TESTED`; the template now contains 280 scenarios.


## 11.63.38 — Block Alias Normalization, Plants and Paving

- Normalized 38 of the 42 remaining legacy block migration aliases into real BlockItems or exact registered blocks; generic fallback is now four block IDs and zero item IDs.
- Routed existing device aliases to the functional 1.19.2 implementations for advanced alchemy, essentia transport, infusion infrastructure, mnemonic matrix and node devices.
- Added exact Greatwood/Silverwood placement aliases, Arcane Stone slab, Metal Base, Obsidian Totem variants and complete blockstate/model/loot/tag resources.
- Ported TC4 Shimmerleaf, Cinderpearl, Vishroom and Ethereal Bloom behavior. Ethereal Bloom uses conservative explicit-taint cleanup instead of unsafe biome mutation.
- Ported Paving Stone of Travel effects and the redstone-disableable Paving Stone of Warding push field.
- Remaining fallback: Growth Lamp, Fertility Lamp, Wand Pedestal and Focused Wand Pedestal.

## 11.63.39 — Final TC4 Fallback Closure: Essentia Lamps and Wand Pedestals

The final four generic migration aliases are now functional blocks: Herba-powered Growth Lamp, Victus-powered Fertility Lamp, the one-slot Wand Recharge Pedestal and its focused compound-aspect matrix. The reproducible TC4ResearchItems fallback audit is now zero.

## 11.63.41 — Exact Node in a Jar ritual and transport

- replaces the simplified direct node capture with TC4's 3×4×3 structure: nine wooden slabs, 26 glass blocks and a live node;
- consumes the cap-discounted equivalent of 70 centivis of all six primal aspects atomically;
- preserves current/base aspects, type and a persistent NodeId; only the original 75% one-step modifier degradation remains;
- adds a real world `node_jar` BlockEntity, dynamic drop/place NBT and wand release back to an unjarred live node;
- restores the one-second 3→1 capture-collapse animation and reuses the NBT-aware item renderer;
- adds four NOT_TESTED runtime scenarios and `tools/tc4_116341_node_jar_ritual_guard.py`.

Registry fallback remains zero. Runtime status remains NOT TESTED until a Java 17 Forge client/dedicated-server run provides SHA-256-backed evidence.


## 11.63.52 — Canonical registry purge

- Removed 63 proven exact duplicate item registry aliases and their duplicate models/localization.
- Active recipes and runtime references now use canonical IDs only.
- Old save IDs are remapped through Forge `MissingMappingsEvent`; deep stack migration preserves serialized NBT/capabilities.
- Added the 19th server-only GameTest for alias absence/canonical presence.
- Static CI: 117/117 PASS. v11.63.52 runtime remains NOT_TESTED; v11.63.51 is the last verified Java 17 baseline at 18/18 GameTests.

## 11.63.53 — P0 runtime contract expansion

- Added five dedicated-server GameTests for legacy aura-node conversion, Bone Bow timing, Traveling Trunk persistence, Crimson Cult roles and Fortress Armor NBT/set math.
- `AuraNodeLegacyItem.convertLegacyStack` is now a deterministic, testable migration function that preserves custom names and rejects empty payloads.
- Required runtime package: **24 server-only GameTests** and **352 manifest scenarios**.
- Runtime verdict remains `NOT_TESTED` until a fresh Java 17 build and GameTest run is captured for this exact source state.


## 11.63.54 — P1 block-entity runtime contracts

- Added four dedicated-server GameTests for Tallow Candle, Hungry Chest, Brain in a Jar and linked Magic Mirrors.
- Runtime manifest now contains 356 scenarios and requires 28 GameTests.
- Recipe ledger remains 258/258 and duplicate registry purge remains 63 aliases.
- This source version still requires a fresh Java 17 build and 28/28 GameTest run before runtime PASS can be claimed.

## 11.63.60 — Original Infusion Stability and Lifecycle Runtime Contracts

- Сохранены исправления стабильности v11.63.59: один проход `symmetry + recipeInstability`, monotonic running instability и исходный whitelist stabilizer-блоков.
- Изменённый locked catalyst запускает ровно одну weighted instability-ветку и затем отменяет процесс.
- Отмена из-за изменённого катализатора не получает дополнительный generic Warp.
- Успешная инфузия не начисляет Warp автоматически.
- Отсутствующий компонент оставляет активный процесс в режиме ожидания.
- Добавлены 4 обязательных GameTest; общий baseline 52 теста / 380 сценариев.

## 11.64.04 — Warp spawn tri-state offset parity

- Подтверждено исходником TC4: каждая ось использует `random(7,24) * random(-1,1)`, поэтому возможны отрицательное, нулевое и положительное смещения.
- Отклонена ошибочная boolean-sign адаптация, которая удаляла нулевые оси примерно в 70,4% трёхосевых выборок хотя бы по одной координате.
- Production-код, GameTest, manifest, source evidence, guards и self-test приведены к одному tri-state контракту.
- Runtime остаётся `NOT VERIFIED` до Java 17 Gradle build и запуска GameTest server.


## 11.64.07 — Warp text.8 BATHSALTS milestone parity (исправление 11.64.06)

- Получен полный оригинальный исходник TC4 4.2.3.5; вложен как оракул в `reference/`.
- ИСПРАВЛЕНО: `warp.text.8` — веха BATHSALTS (actual warp > 10), а не сообщение blurred-vision.
  Blurred-vision (effectRoll<=36) в оригинале не выводит чат.
- WarpEvents: убрана строка из ветки blurred-vision и из DISPLAYED_WARP_MESSAGES.
- TC4EldritchProgression: BATHSALTS теперь выводит warp.text.8 один раз (тёмно-фиолетовый курсив).
- Константа BLURRED_VISION_MESSAGE_KEY -> BATHSALTS_MILESTONE_MESSAGE_KEY; CONTRACT_VERSION=11.64.07.
- GameTest warpText8IsBathSaltsMilestoneNotBlurredVision; guard tc4_116407 (заменил 116406).
- Метрики: 140 GameTests; 468 сценариев; 2049 JSON; 258 рецептов; focused CI 11/11.
- NOT VERIFIED: нет javac, JAR не собран, GameTest-сервер не запускался.

## 11.64.06 — Warp blurred-vision headache message parity

- Подтверждено оригиналом: warp-событие blurred vision (эффект замешательства) выводит строку `warp.text.8` («Surely there must be a way to stop these headaches?»).
- В порте v11.64.05 ветка `effectRoll <= 36` накладывала эффект, но не показывала сообщение; `warp.text.8` была единственной оригинальной warp-строкой, которую порт никогда не выводил.
- Исправление: `WarpEvents.applyOriginalEvent` теперь вызывает `message(player, TC4WarpRuntimeParity.BLURRED_VISION_MESSAGE_KEY)`; добавлен аксессор `usesWarpMessage(...)` и множество всех выводимых строк 1..15.
- Добавлены GameTest `warpBlurredVisionUsesOriginalHeadacheMessage` и guard `tc4_116406_warp_message_parity_guard.py`. Baseline: 140 GameTests / 468 сценариев.
- Ограничения: `javac` отсутствует (Java parse и self-test NOT VERIFIED), Gradle build недоступен (нет сети). Java 17 JAR и Forge GameTest runtime остаются NOT VERIFIED.

## 11.64.16 — Hungry Chest full closure

- One-object release: Hungry Chest only.
- Restores separate 14/16 outline and 15/16 collision, collision-gated item ingestion, exact partial/rejection behavior, original block events/lid timing, tile-identity-only menu validity and 10..30 NBT-preserving break chunks.
- Removes the invented 80-tick opener recount, eight-block menu distance gate, vanilla bulk drop shortcut and approximate per-context item transforms.
- Exact ModelChest geometry/UV, byte-identical chest texture, arcane recipe and HUNGRYCHEST research graph are guarded.
- Static baseline: 157 GameTests, 518 scenarios, 2052 JSON, 258/258 recipes, focused CI 23/23. Build/runtime remain NOT VERIFIED because Gradle could not reach services.gradle.org.
- The universal one-object full-closure prompt remains mandatory in every full archive.

## 11.64.20 — Arcane Levitator full closure

- One-object release: Arcane Levitator only.
- Restores transient TileLifter state, exact 100-tick refresh, 10+10 stacked range, lower-position-only power sampling, opaque obstruction clipping, item/pushable/horse filtering and exact vertical velocity thresholds.
- Restores horizontal-only support/redstone, ordinary rarity, active/powered glow lighting, separate world/inventory glow geometry and exact world/inventory colors.
- Replaces the vanilla enchant approximation with the original four-frame type-3 FXSparkle, exact gravity/damping/lifetime and 4/6, 2/6, 0/6 particle-density gate.
- Corrects the Arcane recipe to WEW/BNB/WAW and retains the exact LEVITATOR research entry.
- Static proof: 177 unique GameTests, 579 scenarios, 2055 JSON, 258/258 recipes, focused CI 29/29 PASS.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME remain NOT VERIFIED because Gradle could not reach services.gradle.org.
- The universal one-object full-closure prompt remains mandatory in every full archive.


## 11.64.36 — Essentia tubes full closure

- Strictly closes normal, filtered, restricted and one-way tubes, valves and buffers as one transport system.
- Restores 2/5/20-tick cadence, minimum suction 0, neighbour-1 / neighbour÷2 propagation, mixed buffer capacity 8 and three-state side choking.
- Restores original subtype NBT, manual/redstone valve behavior, comparator output, exact connection geometry and valve/buffer rendering.
- Static proof: 278 GameTests, 821 scenarios, 2189 JSON resources, 258/258 recipe mapping and 59/59 focused CI.
- SOURCE/RESOURCE CLOSED; BUILD/RUNTIME remain NOT VERIFIED because Gradle could not reach services.gradle.org.
