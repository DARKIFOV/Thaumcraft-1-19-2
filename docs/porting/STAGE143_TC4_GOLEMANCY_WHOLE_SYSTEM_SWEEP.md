# Stage143 — TC4 Golemancy Whole System Sweep

This stage broadens Golemancy beyond the Stage140-142 baseline. It keeps the existing Forge 1.19.2 implementation working while adding a larger TC4-style layer:

- TC4 metadata-split golem decorations as real 1.19.2 registry items.
- Golem decorations can be attached to a Golem Core or directly to a live golem.
- Decorations are persisted on the entity and affect status/render/runtime: armor, hats, visor/glasses, bowtie, mace, wireless backpack.
- Renderer draws simple live decoration overlays instead of a single undecorated body.
- Use core now attempts block placement from golem inventory at the work marker.
- Liquid core can bucket water/lava from the work marker and dump filled buckets at output.
- Essentia core can fill empty phials from a source jar and empty filled phials into an output jar.
- Sorting/container delivery respects filter and Order upgrade more strictly.
- Status summary now reports upgrade and decoration counts.

This is not claimed as perfect TC4 source parity yet; it is a broad system sweep that connects the remaining golem-control, inventory, decoration, liquid and essentia pieces into one playable Golemancy loop.
