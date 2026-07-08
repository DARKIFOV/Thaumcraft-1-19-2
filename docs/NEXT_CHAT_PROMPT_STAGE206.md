Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго с архива:

thaumcraft_legacy_rebuild_STAGE206_TC4_ORIGINAL_PARITY_REPAIR_1_19_2.zip

Главное правило: сначала сверяй оригинальный TC4 1.7.10 source/assets/config, потом делай Forge 1.19.2 adapter. Никаких fake GUI, fake recipes, fake items, placeholder textures, duplicate primary items, fake research unlocks, открытой прогрессии или новых механик, пока не перенесён оригинал.

Что уже сделано в Stage206:
- версия поднята до 2.06.0;
- добавлены правила docs/TC4_ORIGINAL_PARITY_RULES_STAGE206.md;
- Goggles of Revealing больше не делают fake scan/research unlock/warp HUD; оставлены TC4 IRevealer/IGoggles semantics: showNodes/showIngamePopups + 5% vis discount;
- Helmet of Revealing очищен от fake scan/research side effects;
- над aura node добавлен reveal HUD при наведении в очках/шлеме: тип, modifier, vis/aspects, без fake Research/Warp;
- AuraNodeRenderer переведён на оригинальный textures/misc/nodes.png sprite-sheet с TC4 strip mapping вместо fake aura_node_sprite textures;
- ResearchTableScreen убран с rebuild/debug кнопок и переведён ближе к оригинальному GuiResearchTable: guiresearchtable2.png, aspect page 5x5, selected aspect slots, combine icon, page arrows;
- ResearchNoteScreen теперь рисует hex slots через оригинальные hex1.png/hex2.png, а не квадратные fake cells;
- исправлены TC4 recipes для Goggles/Thaumometer/InfusionMatrix на основе ConfigRecipes.java строк 1393/3476/997;
- неправильные vanilla fallback recipes для goggles/original_style/thaumometer_original_style отключены как quarantine;
- добавлены Forge tags для primal shards;
- primary goggles texture скопирована из оригинальной gogglesrevealing.png;
- fake aura_node_sprite_* текстуры убраны из active resources в compatibility_quarantine/stage206_fake_node_sprites/;
- fake Helmet of Revealing scan/overlay research nodes убраны из ResearchRegistry, чтобы reveal gear не открывал новую invented progression.

Следующий stage делай как Stage207: Infusion Matrix full parity start. Проверить оригинальные ConfigRecipes/TileInfusionMatrix/BlockStoneDevice/RenderInfusionMatrix и перенести multiblock, pedestal ring, activation, instability, particles, sounds, crafting state, recipe gating, failure effects. Не ломай golems, wands, aura/nodes, crucible, thaumonomicon progression, research table/note, recipes, aspects, scans, worldgen, focus upgrade NBT/projectiles/textures.

В конце выдай новый ZIP, список изменений, проверки, что делать дальше и обнови этот универсальный prompt.
