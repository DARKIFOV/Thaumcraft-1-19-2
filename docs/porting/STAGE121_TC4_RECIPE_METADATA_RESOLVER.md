# Stage121 — TC4 Recipe Metadata Resolver Pass

This stage continues local development after Stage120. The user requested not to upload yet and to keep building toward a more complete upload around Stage140–159.

## Goal

Stage120 generated the first real recipe data, but many original TC4 recipes still pointed at 1.7.10 metadata stacks like `ConfigBlocks.blockWoodenDevice, meta 2` or loop-generated recipes such as `BalancedShard_ + a`. Stage121 expands those old metadata references into 1.19.2 registry ids.

## Added carriers

- Added `58` new `thaumcraft:tc4_*` item/block-variant carriers for original TC4 metadata stacks.
- Generated item models and copied original TC4 textures into `textures/item/tc4` so the recipe/research objects are visible in creative tabs and Thaumonomicon pages.
- Added resolver mappings for custom plant, cosmetic solid, wooden device, metal device, stone device, tube, jar, crystal, magical log/leaves and missing item fields.

## Recipe data after pass

- Alchemy/crucible JSON: `50`
- Arcane workbench JSON: `71`
- Infusion JSON: `39`
- Vanilla smelting JSON: `5`
- Loop-expanded TC4 recipes: `12`
- Materialized/preserved records: `189`
- Still unresolved/deferred: `54`

## Important

Some `INFUSION_ENCHANTMENT` entries are preserved as source data rather than runtime item-result recipes because they need a dedicated enchantment-application runtime, not a normal item output. That will be a later stage.
