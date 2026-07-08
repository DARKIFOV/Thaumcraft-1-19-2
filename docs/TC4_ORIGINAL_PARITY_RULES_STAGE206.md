# Stage206 TC4 Original Parity Rules

This project is a strict Thaumcraft 4 for Minecraft 1.7.10 parity port to Forge 1.19.2.

## Mandatory rule

Original TC4 1.7.10 source/assets/config are the source of truth first. Forge 1.19.2 code is only an adapter layer after the original behavior has been checked.

## Forbidden drift

Do not add or keep fake GUI, fake buttons, fake recipes, fake items, placeholder textures, duplicate primary items, fake progression, fake unlocks, fake research visibility, fake scan side effects, or newly invented visuals/mechanics while the original TC4 behavior is still missing.

## Replacement rule

When a correct TC4 implementation is added, the previous temporary implementation must be removed from the main path. If removing it would break save compatibility, it must be documented and quarantined as a legacy compatibility adapter, not exposed as the primary implementation.

## GUI rule

Research Table, Research Note, Thaumonomicon, Arcane Workbench, Infusion, Focus, Wand and other screens must use original TC4 coordinates, textures and user flow first. Debug labels and rebuild-only buttons are not allowed in player-facing screens.

## Recipe rule

Recipes must be materialized from `ConfigRecipes.java` and gated with the same original research keys. Vanilla fallback recipes may only exist when the original used a normal ore-dictionary recipe; wrong duplicate recipes must be disabled or moved to compatibility quarantine.

## Texture rule

Use original TC4 assets for item/block/gui/entity/node/focus/particle textures whenever they exist. Generated/placeholder textures are only allowed as temporary documented 1.19.2 adapters for content whose original asset has not yet been extracted.

## Progression rule

Research visibility must match original TC4: hidden/concealed/lost or unavailable research must not be drawn as open grey nodes, and no item/equipment tick may grant research unless TC4 did that explicitly.
