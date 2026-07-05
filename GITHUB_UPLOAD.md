# GitHub upload — Stage118

Upload the contents of this ZIP to GitHub. This stage includes original TC4 research runtime data extracted from `ConfigResearch.java`, plus the existing GitHub Actions build checks.

# GitHub upload — Stage115

Upload the contents of this folder to GitHub, not the folder itself.

## What Stage115 changes

- Imports original TC4 1.7.10 source into `docs/source_refs/tc4_1710_original_source/` for strict file-by-file porting.
- Imports original TC4 1.7.10 assets into `src/main/resources/assets/thaumcraft/original_tc4_1710/`.
- Mirrors original TC4 textures into current resource paths for later model binding.
- Adds generated source/data maps for TC4 blocks, items, tile entities, wand components, research, recipes, ore dictionary entries, entities, source files and asset files.
- Replaces approximate wand cap/rod values with original TC4 values from `ConfigItems.java`.

## GitHub Actions

The workflow still runs Java 17 + Gradle Wrapper + static audits. The original TC4 Java source is under `docs/`, so it is not compiled directly.


## Stage115 note

This zip contains generated runtime bridges from original TC4 `ConfigAspects.java`: `TC4ObjectAspectRegistry` and `TC4EntityAspectRegistry`. Upload the whole extracted folder to GitHub, not only `src/`.


## Stage118

TC4 Thaumonomicon browser port: original 256x230 research browser, original category tabs/backgrounds, original display coordinates, draggable map, TC4 node sprites, hidden parent visibility and TC4-style research page viewer.


## Stage118 research parity

This build continues the strict TC4 research port: original research icons, targeted research note creation from paper + scribing tools, original AspectList note requirements, and mixed text/recipe page rendering in the Thaumonomicon.
