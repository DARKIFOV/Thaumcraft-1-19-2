# Stage125 — TC4 Crucible Heat / Boil / Flux Parity

This stage continues after the whole-port audit and focuses on the original TC4 crucible loop.

## Runtime changes

- Crucible now has water level instead of only a boolean.
- Crucible now has temperature and boiling state.
- Fire, soul fire, lava, magma blocks and lit campfires below the crucible heat it.
- Alchemy and dissolving now require boiling water, closer to TC4.
- Boiling crucible emits bubble/cloud particles.
- Overloaded aspects increase flux over time.
- High flux emits witch particles and spill sound.
- Water bucket and empty bucket now play original TC4 `spill` sound.
- Dissolving plays original TC4 `bubble` sound.
- Successful alchemy plays original TC4 `craftstart` sound.

## Still not finished

- Exact TC4 crucible renderer/water surface is still not ported.
- Exact flux goo/gas block placement rules still need full port.
- Tossing items into the crucible as entities still needs TC4-style collision handling.
