# Stage126 — TC4 Crucible thrown item + flux world parity

This stage continues the TC4 1.7.10 crucible port after Stage125.

## Done

- Added real `thaumcraft:flux_goo` block using original TC4 `fluxgoo.png`.
- Added real `thaumcraft:flux_gas` block using original TC4 `fluxgas.png`.
- Added thrown item processing for boiling crucibles:
  - item entities above/inside the crucible are scanned every 5 ticks around players;
  - valid catalysts craft automatically if the crucible has the required essentia;
  - non-catalyst items dissolve into their original TC4 aspect mapping;
  - unresolved/empty-aspect items are ignored instead of being destroyed;
  - catalysts with missing essentia are preserved.
- Added water consumption:
  - dissolved item: 25 water units;
  - successful alchemy craft: 50 water units;
  - slow boiling evaporation: 1 water unit every 400 boiling ticks.
- Flux now spills into the world as flux goo/gas instead of existing only as an internal integer.
- Flux goo/gas have runtime behavior: particles, poison/confusion/weakness, taint spread and unstable evaporation/condensation.

## Still not final TC4

- Exact TESR water surface and original crucible liquid color renderer still need a dedicated 1.19.2 block entity renderer.
- Exact original flux block spread math needs deeper source-by-source parity with TC4 block classes.
- Player/world balancing needs in-game tuning once GitHub/JAR testing resumes around Stage140–159.
