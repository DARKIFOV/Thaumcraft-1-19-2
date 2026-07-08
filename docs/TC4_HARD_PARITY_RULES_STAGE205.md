# Stage205 hard TC4 parity rules

From this stage forward the port must follow these rules before any new stage work:

1. Thaumcraft 4 for Minecraft 1.7.10 is the only source of truth.
2. If a 1.7.10 class cannot be copied directly, create a named Forge 1.19.2 adapter that preserves original data, coordinates, NBT, GUI sizes, sounds, texture paths, research visibility, recipe gates and behaviour.
3. Do not add fake items, fake recipes, fake GUI buttons, fake progression shortcuts, placeholder textures or duplicate creative-tab entries.
4. If a replacement is added, remove/quarantine the previous rebuild/placeholder path in the same stage.
5. Hidden/concealed/lost research must stay hidden until the original parent/hidden-parent conditions are met.
6. Active textures must resolve to original TC4 assets whenever an original asset exists; no custom substitute may override an original path.
7. Research Table and Thaumonomicon visuals must match original TC4 coordinates first, with only explicit 1.19.2 rendering adapters.
8. Worldgen density/shape must not be amplified beyond original TC4.
