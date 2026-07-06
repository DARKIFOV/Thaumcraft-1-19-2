# Stage130 — TC4 Focus Pouch / Portable Hole / Warding Parity

Stage130 continues the strict TC4 1.7.10 port after Stage129's wand-focus runtime.

## Source-of-truth classes checked

- `thaumcraft/common/items/wands/ItemFocusPouch.java`
- `thaumcraft/common/container/InventoryFocusPouch.java`
- `thaumcraft/client/gui/GuiFocusPouch.java`
- `thaumcraft/common/items/wands/foci/ItemFocusPortableHole.java`
- `thaumcraft/common/items/wands/foci/ItemFocusWarding.java`

## Runtime ported in this stage

### Focus Pouch

A new `FocusPouchItem` was added. It stores TC4 wand foci in NBT and provides a 1.19.2-safe runtime path for the old TC4 focus pouch behaviour:

- focus in off-hand + right-click pouch = store focus;
- pouch in off-hand + right-click wand = equip/cycle focus from pouch;
- shift + pouch use = remove selected focus to inventory;
- tooltip lists stored foci and selected focus.

The original TC4 texture `focuspouch.png` is copied as `textures/item/focus_pouch.png`.

### Portable Hole

Stage129's placeholder permanently broke blocks. Stage130 replaces that with a dedicated `TemporaryHoleBlock`:

- no collision;
- invisible renderer;
- short scheduled lifetime;
- collapses automatically after the timer;
- does not edit warded blocks owned by another player.

This is closer to the original Focus: Portable Hole behaviour than direct `destroyBlock`.

### Warding

Stage129 only displayed a warding message. Stage130 adds `WardedBlockRuntime` and a Forge block-break guard:

- Focus: Warding marks a block as warded by the caster;
- other players cannot break that block;
- owner or creative players can edit;
- shift-cast with the warding focus removes the ward.

Persistence and the full old TC4 warded block-state wrapper are still scheduled for a later deeper pass.
