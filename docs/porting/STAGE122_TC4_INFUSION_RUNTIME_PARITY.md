# Stage122 — TC4 Infusion Runtime Parity

Stage122 continues from Stage121 and focuses on making the infusion altar behave more like Thaumcraft 4 rather than only holding materialized recipe data.

## Source of truth

Reference file:

```text
docs/source_refs/tc4_1710_original_source/thaumcraft/common/tiles/TileInfusionMatrix.java
```

The important TC4 behaviour is in `craftCycle()`:

1. validate the central catalyst pedestal;
2. drain required essentia one point at a time;
3. after essentia is drained, pull pedestal ingredients one by one;
4. instability can fire while the process is running;
5. only after all pending essentia and ingredients are consumed does the catalyst pedestal receive the result.

## Implemented in 1.19.2

- `InfusionMatrixBlockEntity` now tracks pending essentia and pending components during an active craft.
- Active infusion state is saved to NBT: progress, delay, instability, pending aspects and pending components.
- The matrix no longer consumes all jars/components at the end.
- Essentia is drained one point per cycle.
- Components are consumed one pedestal item per cycle after a TC4-style item delay.
- Instability now has a dynamic runtime value capped to `25`, matching TC4's matrix instability cap.
- Result placement happens only after all pending requirements are empty.

## Recipe coverage currently loaded

```text
Infusion recipe JSON files: 47
Total component entries: 240
Total essentia cost across loaded infusion recipes: 3093
Max recipe instability: 10
```

## Notes

This stage is still not the complete TC4 infusion system. It does not yet fully reproduce every original instability event or every visual particle from TC4, but the core runtime order is now much closer to the original than Stage121.
