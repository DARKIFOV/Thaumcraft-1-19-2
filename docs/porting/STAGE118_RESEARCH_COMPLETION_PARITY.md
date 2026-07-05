# Stage118 - Research completion parity pass

Goal: do not move on to recipes until the current TC4 research/Thaumonomicon block behaves much closer to Thaumcraft 4.

Implemented:

- Extracted original `ResearchItem` icon expressions from `ConfigResearch.java`.
- Added `TC4ResearchIconMap` with 150 mapped research icons using transferred original TC4 item/block textures where possible.
- Added `tc4_research_icon_map_stage118.json` for audit/source traceability.
- Changed available-research click behavior: it now creates a targeted Research Note from paper + scribing tools ink instead of instantly unlocking via a placeholder Research Point.
- Added `TC4ResearchNoteCreator` server-side flow.
- Research Note requirements now use the original TC4 `AspectList` from the selected research entry first, with heuristic fallback only for non-TC4 rebuild entries.
- Research pages now preserve mixed text + recipe page order from `ConfigResearch.java`; recipe pages no longer disappear from the book view.
- Solving a Research Note now marks the note solved; right-clicking the solved note converts it into completed research and consumes the note, closer to TC4 progression.

Still not complete TC4 parity:

- The visual recipe cards show recipe keys, but `ConfigRecipes` runtime recipes still need the next dedicated pass.
- Some vanilla/block icons from old MCP names still use fallback icons if no transferred texture is available.
- Exact TC4 book scaling/font quirks are approximated for Forge 1.19.2.
