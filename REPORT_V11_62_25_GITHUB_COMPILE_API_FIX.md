# v11.62.25 — GitHub compile/API fix

Fixed the 13 Java compilation errors from GitHub Actions run `78746557509` and removed the secondary Gradle execution-graph `StackOverflowError`.

## Fixed

- Correct 1.19.2 `Window` import and restored `renderRecipeVisuals`.
- Public Mob NBT overrides in `TC4FireBatEntity`.
- Correct 1.19.2 block-use and replaceability APIs.
- Correct fluid-item capability generic (`IFluidHandlerItem`).
- `RandomSource` compatibility for golem entropy retaliation.
- Restored greatwood buttress-root and crown-cap methods.
- Safe portable-hole block-entity ticker adapter.
- Removed the `jar`/`reobfJar` finalizer cycle; build output is now connected with normal task dependencies.
- Added `tc4_v11_62_25_github_compile_api_audit.py`.
