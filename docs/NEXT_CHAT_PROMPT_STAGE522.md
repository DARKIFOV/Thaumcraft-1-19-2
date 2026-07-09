Продолжай перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго от архива Stage503–522. Главное правило: это parity-порт оригинального TC4, без выдуманных механик, предметов, рецептов, GUI, прогрессии, текстур или поведения, пока оригинальный TC4 не будет перенесён полностью.

Текущий stage: Stage503–522. Реально добавлено/исправлено:
- добавлен runtime block `essentia_reservoir` как перенос `BlockEssentiaReservoir`/`TileEssentiaReservoir`: 256 essentia, mixed-aspect storage, NBT `Aspects` + `facing`, доступ только с выбранной стороны;
- добавлен renderer adapter для reservoir, активная текстура взята из оригинального `essentiareservoir.png`, модельные assets `reservoir.obj/png` оставлены активными;
- tube network теперь видит все subtype blocks через `EssentiaTubeBlockEntity`, а не только normal tube/valve;
- tubes теперь могут использовать reservoir как destination/source с original-side guard;
- добавлен `TC4EssentiaNetworkRuntime` для потребителей, которые тянут essentia через tube network;
- добавлен runtime `thaumatorium` как Forge 1.19.2 adapter под оригинальные `TileThaumatorium`/`TileThaumatoriumTop`: catalyst + original alchemy recipes + drain essentia from tube-connected jars/reservoirs/alembics;
- добавлен active block `advanced_alchemical_furnace` и переведён recipe result с mirror item на runtime block;
- рецепты Essentia Reservoir/Advanced Alchemical Furnace больше не выводят старые `tc4_*` mirror result ids.

Следующий stage делай как Stage523–542. Приоритеты:
1. Довести Thaumatorium: GUI `gui_thaumatorium.png`, formula selection, catalyst slots, mnemonic matrix capacity, exact original ContainerThaumatorium rules.
2. Довести reservoir renderer: OBJ `reservoir.obj`/UV, side connector, exact orientation, break/filled behavior.
3. Довести advanced alchemical furnace: original model `adv_alch_furnace.obj`, real speed/throughput if present in original source, no fake effects.
4. Довести tubes: exact suction, filter/restrict/oneway/buffer/valve edge-cases, no self-loop no-op transfers.
5. Проверять, что recipe outputs use runtime ids, not old `tc4_*` mirror ids, except source-only placeholders clearly quarantined.
6. Не ломать Infusion Matrix, Research Table, Arcane Workbench, Thaumonomicon, wands/foci, aura/nodes, golems, taint, eldritch, worldgen.

В конце нового stage выдай ZIP, отчёт, audit, что сделано, что осталось, и честный процент готовности.
