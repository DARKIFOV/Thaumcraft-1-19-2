# v11.62.36 — Integrated Research Table + Aura Node Blend Parity

Target: **Minecraft 1.19.2, Forge 43.5.2, Java 17**.

This batch continues the screenshot-driven restoration of the original Thaumcraft 4.2.3.5 presentation. It does not replace Forge world generation, menus or networking with a second loader.

## 1. Research puzzle returned to the Research Table

The previous rebuild kept the research-note puzzle in a separate `ResearchNoteScreen`. That was structurally different from TC4: the original `GuiResearchTable` displays the aspect palette, combination wells, parchment, hex puzzle and player inventory in one 255×255 container.

The main `ResearchTableContainerScreen` now renders and controls all of those parts together:

- original split 255×255 Research Table background;
- original 150×150 `parchment3.png` region at the TC4 position;
- full 32×32 `hex1.png` and `hex2.png` source sampling;
- fixed anchor orbs and unknown-aspect art;
- anchor-rooted breadth-first connection traversal;
- pulsing cyan links only through valid connected aspects;
- faded placed aspects that are not connected to an anchor;
- aspect palette, table bonus quantities, paging and combination wells;
- dragging a known aspect from the palette onto a valid parchment hex;
- dragging into either combination well;
- right-click erasing of a placed research aspect;
- left-click clearing of either selected combination component, matching the old GUI;
- 200 ms combination-arrow flash;
- completed-note copy icon and no-ink warning.

The compatibility `ResearchNoteScreen` remains in source so old packets or saves do not fail, but normal Research Table gameplay no longer calls `ACTION_OPEN_NOTE`.

## 2. Live note synchronization without a second GUI

A new server action, `ACTION_SYNC_NOTE`, mirrors only the note NBT and known-aspect state to the open table.

Added or changed:

- `TC4ResearchTableParity.ACTION_SYNC_NOTE`;
- `ResearchTableBlockEntity.syncResearchNote(ServerPlayer)`;
- `RequestResearchTableActionPacket` handling for the new action;
- empty-note clearing packet in `ThaumcraftNetwork.syncResearchNote`;
- note signature tracking in `containerTick()`.

Inserting, removing or replacing a note while the table stays open now requests a fresh state. Removing the note sends an empty `ResearchNoteSyncPacket`, preventing the previous puzzle from remaining on screen as stale client data.

## 3. Aura-node blending corrected

The v11.62.35 renderer sampled the original `nodes.png` atlas but sent every layer through ordinary alpha translucency. TC4 does not do that.

The renderer now separates the original paths:

- aspect layers use additive glow unless the aspect explicitly requires alpha blending;
- `NORMAL`, `UNSTABLE`, `PURE` and `HUNGRY` type strips use the additive emissive path;
- `DARK` and `TAINTED` type strips keep normal alpha blending;
- the unrevealed 10% node silhouette remains a faint additive layer;
- the temporary drain-color layer is additive;
- the wand-to-node wispy ribbon uses an emissive additive render type instead of flat translucency.

The original 32-frame atlas, node strips, modifier alpha, rotation, average-aspect scale and full-bright lighting remain in place.

## 4. Regression protection

Added `tools/research_table_guard.py`. GitHub Actions now fails if any of these regressions return:

- parchment removed from the main Research Table;
- second research screen used by normal table flow;
- drag/place/erase packets removed;
- note insertion/removal no longer synchronized;
- anchor-rooted graph traversal removed;
- empty-note client clearing removed.

`visual_parity_guard.py` was also moved from checking only the compatibility note screen to checking the integrated main table, and now checks node additive/alpha separation and the additive drain ribbon.

## 5. Preserved fixes from earlier batches

The source still includes:

- one shared stable Thaumometer ray on client and server;
- per-player scan knowledge and dropped-item scanning;
- original Thaumometer glass readout;
- original Arcane Workbench background and aspect coordinates;
- original node-stabilizer OBJ path and animated overlay;
- corrected wand item transforms;
- hidden legacy/duplicate IDs without deleting old-world registry entries;
- Forge-only Magical Forest installation and `possibleBiomes()` validation.

## Static validation

Passed in this source tree:

```text
Forge-only guard: OK
Java source guard: OK
JSON resource validation: OK — 1697 files
TC4 visual parity guard: OK
Research Table integration guard: OK
Magical Forest worldgen guard: OK — 5.89% candidate regions before forest filtering
Item models audited: 689
Parsed item models: 689
Statically detected registered models: 383
Visible clone leaks: 0
Missing or invalid model/texture resources: 0
Unexpected audit problems: 0
```

## Build status

A local Forge JAR was **not** produced in this environment. Gradle Wrapper attempted to download Gradle 7.5.1 and stopped before project configuration with:

```text
java.net.UnknownHostException: services.gradle.org
```

GitHub Actions is configured to run all guards and then execute `./gradlew clean build`. The first green GitHub build is still required before calling this an in-game-tested release.

## Required in-game test order

1. Install only the v11.62.36 JAR.
2. Open a Research Table with scribing tools and a valid note.
3. Confirm that no second puzzle window opens.
4. Drag an aspect to the parchment, erase it with right click and clear each combination well by clicking it.
5. Confirm that removing the note clears the parchment immediately.
6. View `NORMAL`, `DARK`, `TAINTED`, `PURE`, `HUNGRY` and `UNSTABLE` nodes with goggles and the Thaumometer.
7. Drain a node with a wand and verify that the ribbon glows rather than appearing as a dark transparent strip.
8. In a new world, run `/locate biome thaumcraft:magical_forest` and inspect server log diagnostics.

## Current estimated parity

| Subsystem | Estimate |
|---|---:|
| Research Table container and layout | 97% |
| Research puzzle interaction and paths | 96% |
| Aura-node atlas, animation and blending | 92% |
| Node stabilizer | 92% |
| Thaumometer scanning and readout | 91% |
| Arcane Workbench | 94% |
| Wand rendering and core use | 87% |
| Registry/clone quarantine | 97% |
| Magical Forest source integration | 96% |
| Overall port | about 83% |
| Remaining to full parity | about 17% |

These are engineering estimates, not results from a complete gameplay test suite.

## Next batch

The next pass should focus on actual GitHub build errors first, then in-game calibration of node depth-ignore/revealer visibility, Thaumometer size, stabilizer animation timing, Magical Forest `/locate`, and a systematic runtime review of remaining placeholder block/item models and GUI screens.
