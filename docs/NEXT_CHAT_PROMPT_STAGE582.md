Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от Stage563–582. Никаких новых механик, предметов, рецептов, GUI, текстур, прогрессии или поведения, пока не будет полностью перенесён оригинальный TC4.

Главное правило: строгий TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но явно помечай его и сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущее состояние Stage563–582:
- Стол исследования всё ещё не 100% оригинал, но исправлен важный UX drift: правый клик по оригинальному note slot 1 теперь готовит/открывает research note, shift-right-click по solved note завершает исследование, без добавления vanilla Button.
- Левый клик по слотам оставлен vanilla container behavior, чтобы не ломать перемещение предметов.
- ResearchNoteScreen теперь использует paged aspect palette как в GuiResearchTable: 5 колонок, 25 аспектов на страницу, original arrow regions 27/51,121.
- Убраны modern opaque palette squares и fake plus marker; selection frame теперь старый parchment/gold-style.
- Добавлен Stage563–582 audit и честный статус: исследования/стол не полные, дальше нужен глубокий ConfigResearch/GuiResearchTable parity.

Следующий Stage583–602:
1. Добивать Research Table deep parity: все оригинальные hotzones, note-in-table flow, completion/copy behavior, scribing tools ink, paper/ink costs, sounds.
2. Сверить ConfigResearch: keys, categories, pages, recipe keys, icons, parents, hidden parents, siblings, warp, round/special/stub/autounlock flags.
3. Убирать fake/stub research metadata только после переноса реальных оригинальных данных.
4. Продолжить Thaumonomicon page/icon coordinate audit и recipe gate audit.
5. Затем вернуться к Thaumatorium OBJ/Mnemonic Matrix/tube suction, не ломая Research Table.

Не ломать golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.

В конце выдай ZIP, отчёт, сколько реально осталось до 100%, и новый NEXT_CHAT_PROMPT.

Compatibility marker: Stage563-582
Compatibility marker: Stage583-602
