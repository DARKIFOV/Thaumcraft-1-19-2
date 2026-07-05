# Stage117 — TC4 Thaumonomicon Browser Port

Goal: stop moving to recipes while research is only a data map. This stage ports the visible Thaumonomicon browser behavior closer to original TC4 on Forge 1.19.2.

## Ported from TC4 GuiResearchBrowser

- 256x230 research browser frame using original `textures/gui/gui_research.png`.
- Original six TC4 research categories, not rebuild heuristic categories.
- Original category icons and category backgrounds from `ConfigResearch.initCategories()`.
- Research node positions now use original `displayColumn` / `displayRow` from TC4 `ConfigResearch.java`.
- Draggable map with TC4-style panning bounds and mouse wheel vertical panning.
- Parent and sibling research lines drawn before nodes.
- Node visibility uses parents and hidden parents. Hidden/concealed/lost entries no longer show as normal public entries.
- Node frames use the original TC4 sprite regions from `gui_research.png`: normal, round, hidden/secondary and special overlays.
- Completed/available/locked brightness behavior is closer to TC4; available nodes pulse.
- Clicking a completed research opens a TC4-style page screen using original `gui_researchbook` texture.
- The page screen renders extracted TC4 page keys and aspect/warp metadata.

## Still not perfect TC4 parity

- Exact item icons from old MCP ItemStack fields are not all mapped yet; aspect icons are used as runtime-safe placeholders until item/meta mapping is finished.
- Primary research should create a research note with paper + scribing tools; current 1.19.2 bridge still uses the existing Research Point unlock packet.
- Recipe page rendering is text/key based until Stage118 recipe port.

This stage intentionally focuses on making the research browser look and behave much closer to TC4 before moving to recipes.
