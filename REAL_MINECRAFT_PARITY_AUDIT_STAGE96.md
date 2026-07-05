# Stage 96 Real Minecraft Parity Audit

Base: Stage 95

Goal: document the Stage96 parity pass that moved renderer-driven or non-cubic objects away from generic cube placeholders.

Validated targets:

- `aura_node`: invisible technical block plus renderer bridge instead of visible cube model.
- `iron_capped_wooden_wand`, `greatwood_wand`, `silverwood_wand`: 3D JSON wand bodies using original rod and cap texture channels.
- Thaumic Energistics buses: part-like geometry instead of full cube blocks.
- `essentia_jar`: jar-shaped model and reduced shape, with dynamic fill handled by later renderer work.

Known limits:

- The aura node renderer is a Forge 1.19.2 bridge, not a byte-for-byte TC4 TESR port.
- Wands still need deeper custom item renderer work for exact TC4 hand animation.
- Remaining cube-like addon models should continue to be audited in small batches.
