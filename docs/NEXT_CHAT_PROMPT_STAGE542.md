Продолжи строгий original Thaumcraft 4 parity-port на Forge Minecraft 1.19.2 от архива Stage523–542.

Жёсткие правила:
- Не придумывать новые механики, предметы, рецепты, GUI, прогрессию, текстуры или поведение.
- Если прямой код 1.7.10 нельзя перенести в Forge 1.19.2, делать явный adapter и сохранять оригинальные data/research keys/pages/icons/aspects/recipes/parents/hidden parents/siblings/warp/gui/textures/sounds/behavior.
- Не возвращать mirror/placeholder ids там, где уже есть runtime TC4 blocks/items.
- Если добавляется runtime replacement для оригинального TC4 блока, старый tc4_* mirror placeholder должен быть скрыт/не использоваться в рецептах.

Последний stage: Stage523–542.
Сделано:
- Добавлен runtime replacement оригинального ConfigBlocks.blockMetalDevice meta 12: thaumcraft:mnemonic_matrix.
- Arcane recipe tc4_mnemonicmatrix теперь output thaumcraft:mnemonic_matrix, старый tc4_block_mnemonic_matrix оставлен только как quarantined mirror placeholder.
- Thaumatorium получил MenuProvider, ThaumatoriumMenu и ThaumatoriumScreen на оригинальном gui_thaumatorium.png.
- Thaumatorium теперь хранит selected formula NBT и состояние mnemonic matrix; без matrix показывает/использует один formula candidate, с matrix — несколько remembered formula candidates.
- Shift+empty hand на Thaumatorium циклит formula; empty hand открывает GUI; catalyst insert/extract сохранены.
- TC4EssentiaNetworkRuntime теперь учитывает sideAllows, one-way/valve traversal, filter aspect gate и TileTubeBuffer-like storage.
- Добавлен Stage523–542 audit в GitHub Actions.

Следующий stage Stage543–562:
1. Углубить Thaumatorium parity: реальные selectable formula hotzones через packet, recipe lock по research, корректные output handling и original sounds/FX.
2. Довести Mnemonic Matrix visual/model: оригинальный brainbox/brain2 render adapter и связь с Thaumatorium как в TC4.
3. Продолжить tubes edge-cases: restrict suction exact formula, buffer suction state, valve redstone timing, filter label interaction, one-way side visuals.
4. Проверить Thaumatorium/Advanced Alchemical Furnace OBJ/renderer against original models/thaumatorium.obj and alchemyblock.mtl.
5. Не трогать golems/wands/aura/nodes/crucible/infusion/taint/eldritch/worldgen/research progression, если stage не чинит их напрямую.
