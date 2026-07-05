# Stage 96 — Real Minecraft / TC4 Parity Audit + Core Fix Pass

## Почему мы поменяли систему

Раньше мод двигался слишком массово: много предметов и блоков добавлялись сразу, из-за чего появлялись костыли:

- aura node как полноценный куб;
- жезлы как 2D item/generated или layered handheld;
- Thaumic Energistics import/export buses как cube_all;
- банки как полный куб;
- часть TT/TCE/TE блоков с generic/fallback моделями.

Новая система: **маленький пакет объектов → доведение до оригинального намерения → только потом следующий пакет**.

## Что исправлено в Stage 96

### 1. Aura Node

Было неправильно:

```text
aura_node = обычный block model / cube-like object
```

Теперь:

```text
AuraNodeBlock:
- RenderShape.INVISIBLE
- no collision
- selection shape only
- skylight propagation
- BlockEntity сохраняется

AuraNodeRenderer:
- зарегистрирован через BlockEntityRenderers
- рисует translucent aura_node_sprite
- цвет зависит от NORMAL / PURE / TAINTED / HUNGRY
- размер зависит от total aspects
```

Ограничение: это renderer bridge, ещё не точный TC4 TESR.

### 2. Wands

Было неправильно:

```text
wand = 2D layered item model
```

Теперь:

```text
iron_capped_wooden_wand
greatwood_wand
silverwood_wand

- 3D JSON cuboid model
- отдельный rod texture
- отдельный cap texture
- не item/generated
```

Ограничение: для полного 1-в-1 нужен custom item renderer.

### 3. Thaumic Energistics buses / terminals

Было неправильно:

```text
essentia_import_bus / export_bus / storage_bus = cube_all
```

Теперь:

```text
- part-like plate model
- reduced shape
- no full cube visual
```

Исправлены модели:

- essentia_import_bus
- essentia_export_bus
- essentia_storage_bus
- essentia_storage_monitor
- essentia_terminal
- arcane_crafting_terminal
- essentia_conversion_monitor
- essentia_level_emitter

Ограничение: нужна directional/orientation state и cable attachment logic.

### 4. Essentia Jars

Было неправильно:

```text
essentia_jar = full cube
```

Теперь:

```text
- jar-shaped JSON elements
- reduced jar collision shape
- item model points to jar model
```

Ограничение: dynamic liquid/aspect fill renderer ещё нужен отдельно.

## Что всё ещё надо чинить

- AuraNodeRenderer is a 1.19.2 renderer bridge, not exact TC4 TESR byte-for-byte.
- Wands still need a dedicated custom item renderer for fully original TC4 hand animation/cap/rod composition.
- TE parts still need orientation/directional state and cable attachment logic.
- Essentia jars still need dynamic liquid/aspect renderer, not just static jar shape.
- Many TT/TCE parity blocks remain generic and must be audited in small batches.

## Следующие правильные этапы

- Stage 97: finish Aura Node renderer + node types + thaumometer scan visuals.
- Stage 98: true Wand item renderer and rod/cap data model.
- Stage 99: TE import/export/storage bus orientation and exact original texture faces.
- Stage 100: Essentia jar dynamic aspect fill renderer.
- Stage 101: GUI exact coordinate audit for Research Table / Arcane Workbench / Thaumonomicon.

## Главное правило дальше

```text
Никаких выдуманных fallback-текстур.
Никаких cube_all, если в оригинале не куб.
Никаких 2D item/generated, если в оригинале custom/3D render.
Сначала аудит объекта → потом перенос → потом тест.
```
