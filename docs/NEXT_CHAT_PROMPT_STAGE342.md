Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с архива Stage323–342 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий original TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Стартовая точка: Stage323–342 уже сделан поверх Stage303–322. В нём:
1. Taintacle renderer переведён с плоского billboard-пути на численный segmented adapter по формулам оригинального `ModelTaintacle`.
2. Outer Lands chunk-provider bridge подключён к live server tick через безопасный Forge 1.19.2 adapter.
3. Research-note линии заменены с прямых палок на deterministic sagging/thread-like adapter, чтобы визуально ближе соответствовать оригинальным ниточкам TC4.
4. Research Table Container Screen снова использует original `guiresearchtable2.png`, координаты оригинала, aspect palette, selected aspect slots, combine region, preview aspect и page arrows.
5. Research table block model заменён с куба на table top + legs и использует original `tablequill` texture copy.
6. Активные texture paths для Thaumonomicon, Goggles of Revealing, Research Notes, Scribing Tools, Thaumometer и tablequill заменены на копии оригинальных TC4 текстур.
7. Основные focus recipes и resolver mappings очищены от старых `tc4_focus_*` / `tc4_shard_*` mirror ids и теперь указывают на реальные runtime ids: `focus_fire`, `focus_frost`, `focus_shock`, `focus_excavation`, `focus_equal_trade`, `focus_portable_hole`, `focus_warding`, `focus_primal`, primal shards, quicksilver drop и warp charm.
8. Старые duplicate mirror ids для стандартных foci/goggles добавлены в quarantine guard, чтобы не лезли в creative/loot/report как мусор.
9. Добавлен audit script: `scripts/tc4_stage323_342_super_mega_original_parity_cleanup_audit.py`.

В следующем stage начни с Stage343–362 и делай не косметику, а строгие оригинальные системы:
- Infusion Matrix: GUI/renderer/tick/instability/recipe lock/essentia/item pedestal behaviour, без fake effects.
- Aura Node GUI/overlay and node interaction parity; не добавлять новые node-механики.
- Goggles of Revealing overlay/render parity.
- Thaumonomicon full page/icon/recipe-gate audit: research keys, icons, parents, hidden parents, siblings, warp, pages, coordinates, recipe materialization and original texture alignment.
- Research Table deep parity: container, copy behaviour, bonus aspects, aspect pool display, note grid/completion and original clickable regions.
- Wand focus base behaviour: use original `ItemFocusBasic`, `FocusUpgradeType`, `ItemFocusFire/Frost/Shock/Excavation/Trade/PortableHole/Warding/Primal` and projectile entity classes as the source of truth. Preserve potency/frugal/enlarge/extend/treasure/silktouch exactly where the original focus classes use them; no fake effects.
- Continue recipe cleanup from original `ConfigRecipes`: if any active recipe still resolves to a mirror id or duplicate id, fix the resolver or explicitly mark the remaining case as a temporary Forge 1.19.2 adapter.

Do not break golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.

At the end, output a new ZIP, write exactly what changed, what is still temporary adapter/drift, what to do in the next stage, how many approximate large parity stages remain, and update this universal continuation prompt again.
