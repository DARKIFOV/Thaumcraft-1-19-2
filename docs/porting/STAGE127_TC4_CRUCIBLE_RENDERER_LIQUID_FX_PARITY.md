# Stage127 — TC4 Crucible Renderer / Liquid / FX Parity

Stage127 continues the Crucible work from Stages 125–126 and targets visual/runtime parity with Thaumcraft 4's `TileCrucible` and `TileCrucibleRenderer`.

## Original TC4 source referenced

- `thaumcraft/common/tiles/TileCrucible.java`
- `thaumcraft/client/renderers/tile/TileCrucibleRenderer.java`

Key original behaviors mapped:

- `TileCrucible#getFluidHeight()` raises the rendered water surface based on tank fill and stored tag amount.
- `TileCrucibleRenderer#renderFluid()` renders a vanilla water icon at that height.
- `heat > 150` is the boiling threshold.
- `heat` caps at 200.
- Every boiling cycle can degrade stored aspects: compound aspects split into components; primal aspect overflow spills flux.
- Overloaded aspect counts produce froth/downward spill FX.

## 1.19.2 port changes

### Client renderer

Added:

- `com.darkifov.thaumcraft.client.render.CrucibleRenderer`

Registered in:

- `ClientModEvents`

Renderer features:

- renders an inner liquid plane instead of showing an empty crucible model;
- uses vanilla water texture, like TC4 used the vanilla water icon;
- height follows a port of `getFluidHeight()`;
- liquid tint blends water with stored aspect colors;
- boiling surface gently pulses;
- boiling renders an additional froth overlay using the original TC4 `textures/misc/r_crucible.png` texture.

### Client sync

Added to `CrucibleBlockEntity`:

- `getUpdatePacket()`
- `getUpdateTag()`

This makes water level, heat, flux and aspects available to the renderer on the client after `setChangedAndSync()`.

### Runtime parity

Changed:

- boiling threshold from previous rebuild value to TC4-style `151`;
- max heat from previous rebuild value to `200`;
- boiling essentia degradation added.

New helper methods:

- `fluidHeight()`
- `dominantAspect()`
- `liquidColorArgb(int alpha)`
- `degradeBoilingAspect()`
- `randomStoredAspect()`

## Remaining Crucible gaps

Still not final TC4 parity:

- original custom particle classes (`crucibleBubble`, `crucibleFroth`, `crucibleFrothDown`) are approximated with vanilla/Forge particles;
- bellows acceleration is not yet fully ported;
- wand sneak-right-click `spillRemnants()` behavior is not fully connected to the modern wand item;
- the crucible 3D model itself still uses the current block model and not a dedicated TC4 TESR model pipeline.
