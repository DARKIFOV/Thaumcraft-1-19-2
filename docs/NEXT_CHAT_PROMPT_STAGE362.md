Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2. Продолжай строго с архива Stage343–362 и не придумывай новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение, пока не будет полностью перенесён оригинальный TC4.

Главное правило: это должен быть строгий original TC4 parity-port. Если код 1.7.10 нельзя перенести напрямую, делай Forge 1.19.2 adapter, но сохраняй оригинальные данные, research keys, страницы, иконки, аспекты, рецепты, координаты, флаги, родителей, hidden parents, siblings, warp, GUI-логику, звуки, текстуры и поведение.

Стартовая точка: Stage343–362 уже сделан поверх Stage323–342 HOTFIX3. В нём:
1. Infusion Matrix теперь хранит locked catalyst id и legacy-compatible `recipeobject` NBT, чтобы craftCycle не дрейфовал к другому рецепту с тем же catalyst.
2. Перед первым consumed pedestal component matrix проверяет recipe lock по catalyst + component pedestals; catalyst-only fallback запрещён.
3. Порядок component pull проходит через `TC4InfusionRuntime.orderedComponentPullList` и сохраняет original ConfigRecipes order без сортировки/дедупа/зеркальных id.
4. Infusion overlay показывает live pending essentia, pending components и instability из BlockEntity.
5. Добавлен `TC4RevealerHudAdapter`: client-only Forge 1.19.2 adapter для оригинального TC4 IRevealer/IGoggles Aura Node HUD.
6. Aura Node HUD использует реальные данные AuraNodeBlockEntity и original aspect icon paths.
7. Aura Node scan interaction больше не использует `itemId.contains`; принимаются только реальные зарегистрированные Thaumometer, Goggles of Revealing или Helmet of Revealing.
8. Thaumonomicon research page убрал modern Button widgets внутри книги и использует manual original-style page/back hotzones поверх original book texture.
9. Удалён unused duplicate source class `com.darkifov.thaumcraft.item.ThaumometerItem`; активный зарегистрированный Thaumometer остался `com.darkifov.thaumcraft.block.ThaumometerItem`.
10. Добавлен audit script: `scripts/tc4_stage343_362_super_mega_infusion_node_thaumonomicon_audit.py`.

В следующем stage начни с Stage363–382 и делай не косметику, а строгие оригинальные системы:
- Infusion Matrix deep parity: exact pedestal item travel, instability event table, stabilizer math, beam FX, sound timings, item/essentia drain cycle and renderer parity from original TileInfusionMatrix/TileRunicMatrixRenderer.
- Goggles/Thaumometer overlay parity: block aspect HUD, entity aspect HUD, node HUD, scan knowledge display, no fake research unlocks.
- Thaumonomicon full audit: every original research key, icon, page order, page type, parent, hidden parent, sibling, coordinates, warp, recipe gates and materialized recipe output. Fix drift instead of adding placeholders.
- Research Table deep parity: original container click regions, copy behavior, aspect pool rendering, bonus aspect sync, note grid/completion, research note copy and ink/paper rules.
- Wand foci: use original `ItemFocusBasic`, `FocusUpgradeType`, `ItemFocusFire/Frost/Shock/Excavation/Trade/PortableHole/Warding/Primal` and projectile entity classes as source of truth. Preserve potency/frugal/enlarge/extend/treasure/silktouch only where original focus classes use them; no fake effects.
- Continue recipe cleanup from original `ConfigRecipes`: if an active recipe still resolves to a mirror id or duplicate id, fix the resolver or explicitly mark the remaining case as a temporary Forge 1.19.2 adapter.

Do not break golems, wands, aura/nodes, crucible, infusion, infusion enchantment index, taint, eldritch, worldgen, Thaumonomicon pages/icons/research progression/recipe gates/materialized recipes/object aspects/entity scan runtime/player scan knowledge/aspect decomposition/research note grid/completion/research table container/copy behavior/bonus aspects/wand focus base behavior/focus upgrade NBT/projectile entities/output textures.

At the end, output a new ZIP, write exactly what changed, what is still temporary adapter/drift, what to do in the next stage, how many approximate large parity stages remain, and update this universal continuation prompt again.
