# Stage134 — TC4 Wands + prior systems repair pass

Goal: stop moving forward while the wand/aura/research/crucible/infusion systems still had obvious TC4 gaps. This stage repairs the systems already touched, with wands as the main target.

## Wand parity changes

- Wand vis remains primal-only, matching TC4 wand storage.
- Rod capacity now controls and clamps stored vis after component changes.
- Cap cost modifiers now apply to focus casting and Arcane Workbench recipes.
- Copper and silver use their TC4 special aspect modifiers:
  - copper: Ordo/Perditio are not penalized as harshly;
  - silver: Aer/Terra/Ignis/Aqua receive the 0.95 special modifier.
- Elemental rods now use the original `WandRodPrimalOnUpdate` behavior:
  - obsidian/staff obsidian regenerates Terra;
  - blaze/staff blaze regenerates Ignis;
  - ice/staff ice regenerates Aqua;
  - quartz/staff quartz regenerates Ordo;
  - bone/staff bone regenerates Perditio;
  - reed/staff reed regenerates Aer.
- Primal staff rod regenerates any missing primal aspect up to 10% capacity.
- Off-hand TC4 cap/rod components can be installed onto a wand and saved as NBT.
- Staff rods render longer/thicker and glowing rods use brighter render light.

## Focus and pouch repair

- Focus casting now pays post-cap-modifier vis cost.
- Focus pouch now has an 18-focus capacity like the TC4 role, instead of infinite NBT storage.
- Excavation, Equal Trade and Portable Hole respect warded-block ownership checks.

## Previous-system checks

- Arcane Workbench applies wand cap modifiers to recipe costs.
- Thaumonomicon/research data from Stage116–118 remains active.
- Crucible boil/flux/renderer changes from Stage125–127 remain active.
- Infusion runtime from Stage122–123 remains active.
- Aura nodes, stabilizer/transducer/Node Jar/Vis Relay from Stage131–133 remain active.
- Texture audit still reports zero missing model/blockstate texture refs.

## Not yet final TC4 perfection

The next remaining wand-specific work is exact focus upgrade UI/upgrade trees and exact animations/projectile entities for every focus. The runtime behavior is much closer, but the old 1.7.10 GUI/effect stack still needs further porting.
