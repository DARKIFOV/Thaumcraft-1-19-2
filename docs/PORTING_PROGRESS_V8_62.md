# Porting progress — v8.62

Estimated TC4 parity completion: **75%**.

Estimated remaining: **25%**.

## What changed from v8.42

v8.62 is a compact runtime parity batch. It moves the estimate from **74%** to **75%** because it closes target lifecycle, component remainder, failure telemetry, book visibility, and visible tree pop-in edge cases. It does not add new TC4 systems or content.

Completed in this batch:

1. Thaumometer entity scans now store the target UUID at use start and complete only if the same entity is still under the ray at the end of the 25 tick hold.
2. Thaumometer early release/cancel clears pending block/entity target NBT instead of allowing stale completion later.
3. Infusion pedestal component consumption now preserves container remainders instead of replacing every consumed component with empty.
4. If a pedestal stack still has items after consuming one component, container remainders are dropped above the pedestal rather than overwriting the remaining stack.
5. Infusion terminal failure telemetry is preserved after `failInfusion`, including travelling component id/source/snapshot debug NBT.
6. Thaumonomicon visibility uses central original flag policy for hidden/lost entries.
7. Unlocked entries without original page payload no longer open a synthetic blank book page.
8. Greatwood/Silverwood generation was removed from the player-area tick fallback so trees no longer appear while a player walks into an already visible chunk.
9. Greatwood/Silverwood vegetation is now reachable only through the new-chunk load/worldgen entrypoint, preserving the TC4 IWorldGenerator-style lifecycle instead of runtime decorative spawning.
10. v8.42 audit was made forward-compatible with the UUID-based entity scan signature.

## Remaining high-risk areas

- Essentia jars/tubes and thaumatorium suction still need deeper TC4 corner-case parity.
- Golems/seals remain a large runtime parity area.
- Research pages still need broader original text/recipe payload verification against ConfigResearch.
- Infusion failure weighted events can still be refined around exact TC4 ejection/goo/gas side effects.
- Client rendering/FX needs more pixel-level parity passes.
- Surface worldgen should eventually be moved from the compatibility chunk-load hook into fully registered 1.19.2 configured/placed features; v8.62 only removes the player-walk pop-in bug for trees.
