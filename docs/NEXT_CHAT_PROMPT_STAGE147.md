Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage147 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должна быть точная копия оригинального TC4 1.7.10, адаптированная под API Forge 1.19.2. Если старый код 1.7.10 нельзя вставить напрямую, сделай 1.19.2-адаптер, но сохрани оригинальные данные, названия, аспекты, рецепты, research keys, страницы Thaumonomicon, GUI-логику, звуки, текстуры, сущности, worldgen и поведение.

Используй как источник правды оригинальный `Thaumcraft4-1.7.10-master`:
- `ConfigResearch.java`
- `ConfigRecipes.java`
- `ConfigAspects.java`
- `ConfigBlocks.java`
- `ConfigItems.java`
- `ConfigEntities.java`
- `assets/thaumcraft/**`
- GUI-классы, entity-классы, tile-классы, worldgen и wand/focus-классы оригинала.

Текущий статус:
- Stage147 исправил курс на строгий оригинальный TC4 parity.
- Версия проекта: `1.47.0`.
- Исправлены неправильные иконки исследований для wand rods/staff rods и `CAP_void`.
- Добавлены `docs/ORIGINAL_TC4_PORTING_STATUS.md`, `docs/NEXT_CHAT_PROMPT_STAGE147.md` и `scripts/tc4_stage147_strict_original_parity_audit.py`.
- После Stage147 осталось примерно 45-70 stage до полного оригинального переноса.

В следующем Stage продолжай с точного оригинального переноса, лучше всего Stage148 сделать так:
1. Добить точную карту иконок исследований Thaumonomicon из `ConfigResearch.java`.
2. Убрать fallback-иконки там, где в оригинале есть конкретный ItemStack/BlockStack.
3. Проверить, что все 201 оригинальных research key имеют корректные страницы, флаги, родители, siblings, hidden/secondary/concealed/special/round/stub/auto_unlock и warp.
4. Не трогать и не ломать уже сделанные golems, research, жезлы, aura/nodes, Crucible, Infusion, taint, eldritch, worldgen и GitHub output resources.
5. В конце выдай новый ZIP, напиши что сделал, что делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
