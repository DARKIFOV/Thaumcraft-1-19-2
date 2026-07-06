# Stage129 — TC4 Wand Focus Runtime + Aura Node Visual Parity

Stage129 continues from Stage128 and focuses on the weak area the user called out: **wands and aura nodes**.

## What changed

### Wand focus attachment

Wands now support a TC4-style focus attachment stored in NBT under `Focus`.

- Put a focus in the off-hand.
- Right-click with a wand in the main hand to equip it.
- Sneak-right-click with the wand to remove the focus and return the item.
- Right-click with an equipped wand to cast the focus.

### Original TC4 focus costs

The new focus cost table comes from original TC4 focus source classes and maps legacy aspects to 1.19.2 enum names:

- `AIR` → `AER`
- `EARTH` → `TERRA`
- `FIRE` → `IGNIS`
- `WATER` → `AQUA`
- `ORDER` → `ORDO`
- `ENTROPY` → `PERDITIO`

Ported focus types:

- Fire
- Frost
- Shock
- Excavation
- Portable Hole
- Equal Trade
- Warding
- Primal

### Real focus items

Existing basic foci were converted from plain placeholder items to `WandFocusItem` runtime items:

- `thaumcraft:focus_fire`
- `thaumcraft:focus_frost`
- `thaumcraft:focus_shock`

New TC4 foci added:

- `thaumcraft:focus_excavation`
- `thaumcraft:focus_portable_hole`
- `thaumcraft:focus_equal_trade`
- `thaumcraft:focus_warding`
- `thaumcraft:focus_primal`

All use original TC4 focus textures under `textures/item/tc4/`.

### Casting behaviour

The old TC4 classes cannot be dropped into Forge 1.19.2 directly, but Stage129 ports their runtime intent:

- Fire: consumes Ignis, raycasts, burns entities or places fire.
- Frost: consumes Aqua/Ignis/Perditio, slows/damages entities or places snow.
- Shock: consumes Aer, damages target and does a small chain effect.
- Excavation: consumes Terra and breaks a targeted block.
- Portable Hole: consumes Perditio/Aer and opens a short tunnel path.
- Equal Trade: consumes Perditio/Terra/Ordo and replaces target block with off-hand block.
- Warding: consumes Terra/Ordo/Aqua and provides target/feedback path for the future protected-block system.
- Primal: consumes all six primal aspects and applies a heavy mixed effect.

### Aura node visual improvement

`AuraNodeRenderer` now renders orbiting aspect wisps around the node using the node's actual aspect list and TC4 aspect colours.

## Source of truth

Original TC4 files used:

- `thaumcraft/common/items/wands/foci/ItemFocusFire.java`
- `thaumcraft/common/items/wands/foci/ItemFocusFrost.java`
- `thaumcraft/common/items/wands/foci/ItemFocusShock.java`
- `thaumcraft/common/items/wands/foci/ItemFocusExcavation.java`
- `thaumcraft/common/items/wands/foci/ItemFocusPortableHole.java`
- `thaumcraft/common/items/wands/foci/ItemFocusTrade.java`
- `thaumcraft/common/items/wands/foci/ItemFocusWarding.java`
- `thaumcraft/common/items/wands/foci/ItemFocusPrimal.java`
- `thaumcraft/api/wands/IWandFocus.java`
- `thaumcraft/api/wands/ItemFocusBasic.java`

## Still not final TC4 parity

Remaining work for later stages:

- exact focus upgrade tree and focus pouch;
- packet-based focus cycling like TC4;
- true temporary Portable Hole restoration;
- true warded block ownership/protection;
- projectile/entity classes for original fire/frost/shock/primal effects.

## Validation

Passed:

```bash
python scripts/java_syntax_guard.py
python scripts/github_ci_guard.py
python scripts/github_static_audit.py
python scripts/tc4_texture_audit.py
python scripts/tc4_full_parity_audit.py
```
