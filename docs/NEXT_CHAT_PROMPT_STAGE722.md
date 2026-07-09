Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива Stage703–722. Не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но явно помечай его и сохраняй оригинальные TC4 данные/ключи/иконки/аспекты/рецепты/координаты/NBT/звуки/текстуры/поведение.

Текущий статус Stage703–722:
- версия 7.22.0;
- Arcane Workbench получил общий hitbox/coordinate ledger для output/wand/aspect hover и оригинальный craftstart sound при server-side output take;
- Infusion Matrix получила TC4InfusionCraftCycleParity ledger для craftCycle delays/sound/debug policy;
- обычные debug/waiting чат-сообщения матрицы скрыты по умолчанию, внутренние NBT/state сохранены;
- essentia drain теперь отправляет FX от реальной банки-источника к матрице, а не generic matrix-only puff;
- удалён non-TC4 HAPPY_VILLAGER progress marker;
- Aura Node HUD переведён на общие pixel constants и убран debug type/total/E text overlay;
- исправлен stale `% FRAMES` compile-risk в AuraNodeRenderer.

Следующий stage Stage723–742:
1. Продолжай Infusion Matrix craftCycle parity: точнее failure paths, стабилизаторы, свечи/черепа/симметрия, source FX, catalyst/result edge-cases, enchantment/runic edge-cases.
2. Продолжай Arcane Workbench parity: container item handling, shift-click edge-cases, wand/staff restrictions, sounds, preview/output, все ConfigRecipes arcane recipes.
3. Продолжай Aura Nodes: HUD draw order/alpha/pixel coords, Thaumometer scan GUI/HUD, node type/modifier frame mapping, energized node display, без debug текста.
4. Не ломай Research Table/Thaumonomicon/research gates, jars/tubes/thaumatorium, foci/wands, golems, aura/nodes, taint, eldritch, worldgen, recipes/materialized recipes/object aspects/entity scan/player scan/aspect decomposition.
5. В конце выдай новый ZIP, отчёт, audit script, NEXT_CHAT_PROMPT_STAGE742.md и честную оценку готовности.
