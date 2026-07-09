Продолжай строго от архива Stage443–462. Это parity-port оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Нельзя придумывать новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: строгий TC4 parity. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, parents, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Текущее состояние Stage443–462:
- Stage443 усилил визуальный/runtime cleanup без новых механик.
- IRevealer parity исправлен: Goggles/Helmet of Revealing работают как revealer из head slot; Thaumometer работает из руки; держание goggles в руке больше не даёт fake reveal.
- Aura Node HUD закреплён на оригинальных hud.png, node_bubble.png, nodes.png с full 256/64/2048 atlas constants.
- Research Table получил Stage443 coordinate ledger по оригинальному GuiResearchTable: aspect grid 10,40 step 18, combine slots 13,139 и 71,139, arrows 27,121 и 51,121, copy 37,5.
- Arcane Workbench aspect-cost GUI теперь берёт recipe по видимой сетке до output fallback, чтобы стоимость не пропадала до preview output.
- Добавлен audit scripts/tc4_stage443_462_original_gui_revealer_wand_audit.py и подключён в GitHub Actions.

Следующий stage должен быть Stage463–482. Главные цели:
1. Research Table deep parity: координаты, hitboxes, copy behavior, note grid visual, аспектные ниточки/линии, страницы, ink/paper, без modern tooltips где оригинал их не имел.
2. Arcane Workbench deep parity: слоты, vis cost display, wand slot behavior, server craft path, original GUI texture only.
3. Infusion Matrix: не косметика — довести craft parity по оригинальному ConfigRecipes: catalyst + ordered components + essentia + research gate + instability/failure + FX. Нельзя выбирать recipe только по catalyst.
4. Aura Node: renderer/HUD точнее по original nodes.png strip/frame/type/modifier/alpha, без fake orbitals и debug visuals.
5. Thaumometer/Goggles/Wands: продолжать сверку geometry/UV/transform с оригинальными ModelScanner/ModelGoggles/ModelWand.
6. Сохранять правило no duplicate items: если обнаружен mirror/placeholder item, либо удалить/карантинить, либо явно пометить как Forge 1.19.2 adapter с source mapping.

В конце следующего stage выдать новый ZIP, написать что сделано, что дальше, честную оценку готовности до 100%, и обновить NEXT_CHAT_PROMPT.
