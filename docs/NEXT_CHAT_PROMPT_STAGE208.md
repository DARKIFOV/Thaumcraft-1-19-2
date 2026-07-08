Продолжи перенос оригинального Thaumcraft 4 с Minecraft 1.7.10 на Forge Minecraft 1.19.2 строго с архива:

thaumcraft_legacy_rebuild_STAGE208_TC4_INFUSION_RENDERER_ENCHANTMENT_PARITY_1_19_2.zip

Source tree marker: `Thaumcraft4-1.7.10`.

Главное правило: сначала сверяй оригинальный TC4 1.7.10 source/assets/config, потом делай Forge 1.19.2 adapter. Никаких fake GUI, fake recipes, fake items, placeholder textures, duplicate primary items, fake research unlocks, открытой прогрессии или новых механик, пока не перенесён оригинал.

Что уже сделано в Stage208:
- версия поднята до 2.08.0;
- Infusion Matrix получил block-entity renderer вместо fake/static cube_all model;
- renderer использует original `textures/models/infuser.png`, 8 cubelets, scale 0.45, offsets ±0.25, startup yaw/pitch/roll, active instability wobble, glow pass и crafting halo adapter;
- `InfusionRecipe` хранит `recipeType`, `enchantmentId`, `outputNbtLabel/outputNbt`, catalyst wildcard;
- добавлен `TC4InfusionEnchantmentAdapter` для original `InfusionEnchantmentRecipe` runtime materialization;
- enchantment recipes теперь проверяют central item applicability, max level и compatibility;
- enchantment instability/essentia/XP scaling перенесены из original TC4 formulas;
- `craftCycle` теперь до essentia drain обрабатывает `recipeXP`: one level per cycle, radius 10 player search, magic damage 0..1, creative bypass, source beam FX;
- `craftingFinish` поддерживает normal ItemStack output, recipeType==1 Enchantment output и NBTBase-style `addTagElement` output adapter;
- component validation/pull теперь идёт через `recipe.componentMatches`, container item сохраняется через `getCraftingRemainingItem`;
- сохранены TC4 NBT aliases `recipetype` и `recipexp`;
- добавлены `docs/TC4_INFUSION_RENDERER_ENCHANTMENT_PARITY_STAGE208.md`, `STAGE208_TC4_INFUSION_RENDERER_ENCHANTMENT_PARITY_REPORT.json` и `scripts/tc4_stage208_infusion_renderer_enchantment_audit.py`.

Следующий stage делай как Stage209: exact Infusion failure/source-FX/runic augment parity. Проверить оригинальные `TileInfusionMatrix.craftCycle`, `InfusionRunicAugmentRecipe`, `PacketFXInfusionSource`, `PacketFXBlockZap`, `FXInfusionSource`, flux/goo/gas classes и перенести:
1. exact weighted instability event table cases 0..20, включая component loss, essentia increase, source zap, item damage, block effects;
2. full PacketFXInfusionSource/PacketFXBlockZap adapter вместо локальных particle approximations;
3. runic augment dynamic components/output NBT (`RS.HARDEN`) и exact aspect/instability calculation;
4. full ItemStack matching: damage wildcard 32767, crafting NBT equality, OreDictionary/fuzzy matching where original requires;
5. flux goo/gas/block zap parity or safe adapter tied to original blocks only;
6. warp/sticky warp split and failure sounds/particles without fake mechanics.

Не ломай golems, wands, aura/nodes, crucible, thaumonomicon progression, research table/note, recipes, aspects, scans, worldgen, focus upgrade NBT/projectiles/textures. Прогони Stage205, Stage206, Stage207, Stage208 audits, static resource audit и java syntax guard. В конце выдай новый ZIP, список изменений, проверки, что делать дальше и обнови этот prompt.
