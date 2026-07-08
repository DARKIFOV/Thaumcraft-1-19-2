Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго с архива:

thaumcraft_legacy_rebuild_STAGE207_TC4_INFUSION_MATRIX_PARITY_START_1_19_2.zip

Главное правило: сначала сверяй оригинальный TC4 1.7.10 source/assets/config, потом делай Forge 1.19.2 adapter. Никаких fake GUI, fake recipes, fake items, placeholder textures, duplicate primary items, fake research unlocks, открытой прогрессии или новых механик, пока не перенесён оригинал.

Что уже сделано в Stage207:
- версия поднята до 2.07.0;
- Infusion Matrix переведена на оригинальную двухфазную wand-семантику TC4: первый клик активирует `active`, второй запускает `crafting`;
- activation теперь требует оригинальный `validLocation`: center pedestal на `matrixY - 2` и 4 diagonal infusion pillars;
- runtime хранит отдельные `active`, `crafting`, `count`, `craftCount`, `symmetry`, `recipeInstability`, `TravellingComponent`;
- сохранены original NBT aliases `active`, `crafting`, `instability`, `recipeinst`, `recipetype`, `recipeplayer`;
- структура отдаёт `originalSymmetryPenalty`, и стартовая instability считается от TC4 symmetry + recipeInstability;
- essentia drain range исправлен на TC4 radius 12, а cycle pacing отделён от range через `CRAFT_CYCLE_DELAY = 10`;
- component pull теперь делает source FX first + five tick travel delay перед consumed pedestal item, ближе к original `itemCount = 5`;
- overlay показывается только во время `crafting`, а не просто при активированной матрице;
- оригинальный `textures/models/infuser.png` скопирован в active resources;
- добавлен аудит `scripts/tc4_stage207_infusion_matrix_parity_audit.py` и заметки `docs/TC4_INFUSION_MATRIX_PARITY_STAGE207.md`.

Следующий stage делай как Stage208: Infusion Matrix renderer + output/enchantment parity. Проверить оригинальные `TileRunicMatrixRenderer`, `ModelCube`, `TileInfusionMatrix.craftingFinish`, `InfusionEnchantmentRecipe`, `InfusionRunicAugmentRecipe`, `PacketFXInfusionSource` и перенести:
1. renderer: 8 cube pieces, startup rotation, instability wobble, overlay glow, crafting halo по `infuser.png` без fake cube_all;
2. output parity: ItemStack/NBTBase/Enchantment outputs, runic augment NBT, enchantment XP drain и level damage loop;
3. source FX parity: pedestal->matrix particles, block zap, item/essentia source packets/adapters;
4. failure effects: exact event weights 0..20, flux goo/gas adapter, pedestal zap/sounds, warp/sticky warp split;
5. recipe matching: перейти от item-id-only component matching к original ItemStack matching где нужен damage/NBT.

Не ломай golems, wands, aura/nodes, crucible, thaumonomicon progression, research table/note, recipes, aspects, scans, worldgen, focus upgrade NBT/projectiles/textures. Прогони Stage205, Stage206, Stage207 audits, static resource audit и java syntax guard. В конце выдай новый ZIP, список изменений, проверки, что делать дальше и обнови этот prompt.
