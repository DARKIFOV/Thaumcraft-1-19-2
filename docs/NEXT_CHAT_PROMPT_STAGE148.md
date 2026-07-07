Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с последнего архива Stage148 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий оригинальный TC4 parity-порт. Если код 1.7.10 нельзя перенести напрямую, сделай адаптер под Forge 1.19.2, но сохрани оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родители, скрытые родители, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Используй как источник правды оригинальный `Thaumcraft4-1.7.10-master`, особенно:

- `thaumcraft/common/config/ConfigResearch.java`
- `thaumcraft/common/config/ConfigRecipes.java`
- `thaumcraft/common/config/ConfigAspects.java`
- `thaumcraft/common/config/ConfigBlocks.java`
- `thaumcraft/common/config/ConfigItems.java`
- `thaumcraft/common/config/ConfigEntities.java`
- `assets/thaumcraft/**`
- оригинальные GUI/entity/tile/worldgen/wand/focus классы.

Текущий статус:

- Stage147 вернул проект к строгому оригинальному TC4 parity.
- Stage148 поднял версию до `1.48.0`.
- Stage148 завершил explicit research icon coverage: все 201 оригинальных research key теперь имеют явную TC4-иконку из `ConfigResearch.java`, без fallback для оригинальных узлов Thaumonomicon.
- Добавлен `scripts/tc4_stage148_research_icon_parity_audit.py`.
- Добавлен `docs/NEXT_CHAT_PROMPT_STAGE148.md` и обновлён `docs/ORIGINAL_TC4_PORTING_STATUS.md`.
- После Stage148 осталось примерно 44-69 stage до полного точного переноса оригинального TC4.

В следующем Stage149 сделай:

1. Продолжи точный перенос Thaumonomicon, теперь не иконки, а страницы исследований.
2. Сравни все `ResearchPage` из оригинального `ConfigResearch.java` с `TC4ResearchRuntimeBridge`.
3. Исправь порядок и типы страниц: TEXT, NORMAL_CRAFTING, ARCANE, CRUCIBLE, INFUSION, INFUSION_ENCHANTMENT, MULTIBLOCK и специальные gated pages.
4. Убери page type/count mismatches, но не трогай уже рабочие golems, wands, nodes/aura, crucible, infusion, taint, eldritch, worldgen и output textures без необходимости.
5. Добавь новый аудит `scripts/tc4_stage149_research_pages_parity_audit.py`.
6. В конце выдай новый ZIP, напиши что сделал, что будешь делать в следующем stage, сколько stage ещё осталось, и обнови универсальный prompt для продолжения.
