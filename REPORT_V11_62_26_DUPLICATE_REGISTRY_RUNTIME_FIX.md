# v11.62.26 — duplicate registry runtime fix

Fixed the Minecraft 1.19.2 Forge startup crash from `crash-2026-07-10_20.48.42-fml.txt`.

## Root cause

`thaumcraft:tc4_crystalessence` was registered twice in the same `DeferredRegister<Item>`:

1. as the functional `EssentiaCrystalItem` in `ThaumcraftMod`;
2. again as a generic TC4 research-item placeholder in `TC4ResearchItems.registerAll()`.

Forge rejects duplicate registry names during static mod initialization, producing:

`IllegalArgumentException: Duplicate registration tc4_crystalessence`

## Fix

- Kept the functional `EssentiaCrystalItem` as the only owner of `tc4_crystalessence`.
- Added a `preRegistered` registry-object map to `TC4ResearchItems.registerAll()`.
- Reused the existing crystal registry object in the TC4 research lookup map instead of registering a placeholder.
- Added a generic duplicate-registry audit that parses the research entry list and main item registrations.
- Verified that the only intentional overlaps are:
  - `tc4_crystalessence`, reused through `preRegistered`;
  - `tc4_block_focal_manipulator`, skipped because the real BlockItem owns it.
