# Thaumcraft Legacy Rebuild — v11.62.74

## v11.62.74 Forge compile hotfix — Brain Jar radian wrapping

- fixes the two real `compileJava` errors reported by GitHub Actions run `79417104482`;
- removes calls to the unavailable Minecraft/Forge 1.19.2 API `Mth.wrapRadians(float)`;
- keeps Brain in a Jar rotation in radians and normalizes angular deltas to `[-π, π)` with a local Java 17 helper;
- avoids the incorrect alternative of passing radian values to `Mth.wrapDegrees`;
- extends the Brain Jar regression guard so future releases reject reintroduction of `Mth.wrapRadians`;
- preserves all v11.62.73 mirror runtime changes unchanged.

GitHub Actions reached the real Forge `compileJava` task. The uploaded log contained exactly two errors, both in `BrainJarBlockEntity`, plus 100 deprecation warnings that did not cause the build failure. A new CI run is still required to reveal any later-stage errors after these two compiler blockers.


## v11.62.73 Magic Mirror / Essentia Mirror / Hand Mirror runtime parity

- Added reciprocal, dimension-safe stationary mirror links with NBT-preserving drops.
- Ported regular mirror item transport, delayed random output queue, instability decay and Ordo stabilization.
- Ported the source-only Essentia Mirror with the original forward half-space search radius of eight blocks.
- Added the original one-slot Hand Mirror menu at slot `(80, 24)` and remote item ejection.
- Exposed the regular mirror as a Forge `IItemHandler` insertion endpoint for Forge automation and ported golems.
- Prevented client-side ghost drops when the Hand Mirror GUI closes with an item still in its slot.
- Added six-face thin block geometry, support checks, linked/unlinked pane models and tube-network integration.
- Cross-dimension lookups never force-load or generate destination chunks.

Target: **Minecraft 1.19.2 / Forge 43.5.2 / Java 17**.

This revision continues the source-driven port against the supplied TC4 4.2.3.5 reference source. It replaces the three mirror placeholders with a linked Forge 1.19.2 runtime subsystem and integrates item and essentia transport with the existing port.


## v11.62.72 Brain Jar / infusion XP / valve runtime parity

- replaces the `tc4_jar_brain` migration placeholder with a real BlockEntity-backed Brain in a Jar;
- ports the original 2000-XP capacity, six-block orb attraction, touch absorption, random click release, break release, NBT sync, comparator output and +2 enchanting power;
- restores the original animated brain geometry, brine and TC4 brain/jar sounds;
- fixes infusion-enchantment XP drain so creative players without levels no longer advance the matrix;
- keeps a powered essentia valve topologically connected while suppressing fresh suction, matching `TileTubeValve`.

## v11.62.71 GUI / infusion parity hotfix

- restores the original TC4 horizontal page offsets: text `x-15`, aspects `x-8`, recipes `x-4`;
- restores long-title Y scaling from `GuiResearchRecipe`;
- rebuilds infusion pages around the original `(56,102)` centre, renders every component on the dynamic 40 px ring, and restores original output/input/aspect positions;
- adds the five omitted original infusion registrations: `JarBrain`, `Mirror`, `MirrorHand`, `MirrorEssentia`, and `TravelTrunk`;
- corrects `itemTrunkSpawner` and `blockJar:1` legacy resolution so research pages no longer show a wooden golem or ordinary jar as the result;
- makes project, mod metadata and CI release artifacts consistently report `11.62.71`;
- adds a source-parity regression guard that rejects the previous shifted-page and eight-component-limit regressions.

Status remains **PARTIAL / STATIC PASS** until Forge compilation and in-game client/dedicated-server tests are completed. Brain in a Jar now has its block/entity runtime; the two mirrors and Traveling Trunk still require their teleportation, inventory and entity behaviour.

## v11.62.69 CI hotfix 3

- fixes the six `compileJava` errors reported by GitHub Actions run `79321665380`;
- uses the Forge 1.19.2 `ChestMenu` API for Hungry Chest instead of the unavailable `GenericContainerMenu`;
- imports `UseAnim` from `net.minecraft.world.item` for Sanity Soap;
- keeps the concrete `PurifyingFluidBlock` registry type required by `ForgeFlowingFluid.Properties#block`;
- adds an early Forge 1.19.2 compile-API guard to both build and release workflows.


## v11.62.69 focus

- ports the original `ContainerSpa` as `ArcaneSpaMenu` with one Bath Salts slot at `(65,31)`, the original player-inventory coordinates and exact shift-click filtering;
- ports `GuiSpa` using the original `gui_spa.png`, `176×166` viewport, mix/dispense button, tank gauge, fluid and mode tooltips;
- synchronizes mix mode, tank amount and fluid registry id through three menu data values;
- renders the stored fluid through `IClientFluidTypeExtensions`, including the fluid texture and tint;
- restores button id `1` and the original `cameraclack` sound;
- replaces temporary Shift-right-click/salt/direct-FluidUtil controls with the original GUI workflow;
- removes Purifying Fluid from `#minecraft:water` and creates the dedicated `#thaumcraft:purifying_fluid` tag;
- documents two original registrations in this subsystem: `BathSalts` and `ArcaneSpa`, with static coverage **2/2**.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.68 focus

- registers Purifying Fluid as a real Forge source/flowing fluid, luminous block and bucket using the existing original animated texture;
- restores Bath Salts' 200-tick dissolution in a vanilla water source and conversion of that source into Purifying Fluid;
- restores the single-use Purifying Fluid source collision formula that grants Warp Ward according to permanent Warp and consumes the source;
- restores the original white bubble and lava-pop ambient fluid effects;
- ports the Arcane Spa as a 5000 mB block entity with one Bath Salts slot, 40-tick cadence, redstone disable, mixing/dispensing modes and 5x5 source expansion;
- exposes side fluid/item automation while keeping the top closed, and synchronizes tank, salts and mix state through block-entity NBT;
- connects the already-ported `BathSalts` alchemy and `ArcaneSpa` Arcane Workbench recipes to functional registered outputs and therefore to their existing JEI categories;
- corrects the previously broken Arcane Spa symbol map to the original `QIQ/SJS/SPS` piston/jar/arcane-stone/quartz/iron-bars recipe;
- completes the Purifying Fluid hook already used by Sanity Soap's permanent-Warp cleansing bonus.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.67 focus

- restores the original 2000-tick Warp check, square-root counter roll, counter reduction and temporary-warp decay;
- includes held, armor and optional Baubles/Curios warping gear in event severity;
- registers all eight TC4 Warp effects with original colors, icons, durations, tick behavior and curative rules;
- restores Flux Flu wand-cost penalties, Flux Goo/Gas effects, Death Gaze, Thaumarhia, Sun Scorned and infectious spread;
- restores harmless viewer-only Mind Spiders and the original event-range gaps;
- ports the 200-tick Sanity Soap use sequence, particles/sounds, complete temporary-warp removal and sticky-warp chance;
- adds the TC4 `wuss` equivalent config switch for random Warp events.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.66 focus

- provides a complete 109-row registration ledger for `initializeArcaneRecipes()`: 84 literal shaped call sites expand to 104 shaped registrations after the 16-banner and 6-arrow loops, plus 5 shapeless registrations;
- explicitly separates the three normal `ArcaneStone2..4` recipes and all 24 infusion-enchantment recipes from the Arcane Workbench count;
- registers the original `thaumcraft:repair` and `thaumcraft:haste` enchantments with TC4 level caps, enchanting costs and applicability rules;
- restores the 40-tick vis-powered Repair effect, original primal-aspect cost formula and the carried-hover-harness exception;
- restores the Haste movement impulse and airborne/water reductions;
- replaces the broad repair namespace approximation with the data-driven `thaumcraft:repairable` equipment tag;
- improves JEI infusion-enchantment entries with representative central items and visibly enchanted outputs;
- adds a full 24-row infusion-enchantment parity ledger.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.65 focus

- replaces the flat `tc4_block_banner` mirror with a placeable floor/wall banner block, persistent colour/aspect NBT, cloth sway, original model geometry and custom item rendering;
- ports all 16 exact `Banner_0..15` arcane recipes with wool metadata parity, AQUA/TERRA costs, research gating and NBT-coloured outputs in JEI;
- adds result-NBT support to the Arcane Workbench recipe adapter so crafted and JEI outputs retain TC4 metadata;
- restores the exact 256-mask/47-texture owner-aware connected-texture selection for Warded Glass;
- replaces the Vis Charge Relay cube with triangle data converted from the original `vis_relay.obj`, original support transforms and a synced five-tick primal-aspect crystal pulse;
- adds BEWLR rendering for banner and relay item forms.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.64 focus

- functional 27-slot Hungry Chest with item absorption, comparator output, lid/eating animation, original chest texture and custom item rendering;
- functional Vis Charge Relay / Arcane Workbench Charger that joins the relay network and charges the wand slot below it;
- owner-bound, explosion-proof Warded Glass removable by its owner with a wand;
- exact original `HungryChest`, `NodeChargeRelay` and `WardedGlass` arcane recipes, including aspect costs and research keys;
- corrected TC4 metadata resolver mappings for metal-device relay metas, warded glass, hungry chest and the vanilla trapdoor field.

Status remains **PARTIAL / STATIC PASS** until Gradle compilation and in-game runtime tests are completed.

## v11.62.63 focus

- corrects all 16 tallow candles to light level 14, restores smoke/flame particles and makes them effective infusion stabilizers;
- replaces the item-grate state stub with a real block entity exposing an insertion-only upper item handler while open, immediate downward item ejection and redstone open/close parity;
- ports `KnowFrag`: nine knowledge fragments craft an unknown note whose first use finds eligible hidden triggered research or returns 7–9 fragments;
- ports the 48 aspect-coded Jar Label recipes and label reset as one NBT-aware custom serializer;
- adds a dedicated JEI category with 48 aspect assignments plus one reset display;
- provides a registration-level normal-recipe ledger: 86/86 non-dynamic original registrations mapped, while the 49 NBT label registrations are represented by one runtime serializer and 49 JEI views;
- retains the release status as **PARTIAL / STATIC PASS** until Forge compilation and runtime tests succeed.


## v11.62.62 focus

- ports or corrects 40 original construction and utility recipe entries;
- adds functional Greatwood/Silverwood stairs and slabs;
- replaces the legacy Thaumium storage block, Tallow block and balanced crystal-cluster item mirrors with real placeable blocks under their exact saved registry ids;
- adds Amber Block, Amber Bricks and the original item grate with an open/closed state;
- ports all 16 animated tallow-candle colours as separate modern blocks, including dye conversion and whitewash recipes;
- corrects Essentia Phial to the original clay/glass pattern and output count of eight;
- corrects Table inputs to accept the vanilla wooden slab/plank tags;
- removes six non-original reverse crystal recipes and restores original six-shard cluster assembly;
- exposes 125 standard shaped/shapeless recipes through JEI's normal crafting category;
- adds `tools/tc4_116262_construction_recipe_jei_guard.py` and machine-readable audit output.


## v11.62.61 focus

- ports 39 original normal-crafting entries: metal compaction, mundane baubles, four Triple Meat Treat alternatives, plant conversions, Greatwood/Silverwood planks, flesh block, Jar Label, and the complete Thaumium/Void armor and tool sets;
- exposes all normal recipes through vanilla shaped/shapeless recipe types, allowing JEI to list them in its normal crafting category without duplicate custom registrations;
- replaces the 18 inert Thaumium/Void equipment mirrors with real `ArmorItem`, `SwordItem`, `PickaxeItem`, `AxeItem`, `ShovelItem` and `HoeItem` implementations;
- restores original TC4 material statistics and armor textures;
- restores Void gear passive repair, Weakness-on-hit behavior and Warping 1 integration;
- replaces active duplicate ingot/nugget recipe ids with canonical modern carriers and adds safe old-stack migration mappings;
- removes legacy Greatwood/Silverwood placeholder ids from active Arcane and Infusion recipes;
- adds `tools/tc4_116261_normal_recipe_jei_guard.py` and machine-readable audit output.


## v11.62.60 focus

- ports the original conditional Pure Tin, Pure Silver and Pure Lead crucible recipes through modern `forge:ores/*` item tags;
- teaches the crucible runtime and JEI category to consume tag-backed catalysts instead of reducing legacy ore-dictionary recipes to one hard-coded item;
- ports the exact Node Relay and Void Essentia Jar arcane recipes;
- repairs the original Primal Charm result and symbol mapping, Focus Primal catalyst, all eight staff-rod shapes/costs, Essentia Crystalizer balanced shard, and Sinister Stone catalyst/result;
- corrects `ConfigItems.itemResource:15` and `itemCompassStone` resolver mappings;
- de-duplicates generated wand recipes by original TC4 key so JEI no longer shows stale materialized copies beside the authoritative runtime recipe;
- adds `tools/tc4_116260_recipe_jei_guard.py` and a machine-readable audit report.

## v11.62.59 focus

- removes the non-original world-space nameplate rendered above aura nodes;
- restores the original wand equipped origin and removes the accidental half-scale first-person transform;
- fixes the Thaumometer first-person near-plane offset and keeps node data on its glass;
- requires the same exact ray target for the full scan instead of allowing look-away completion;
- removes physical Research Point rewards and the non-original scan chat dump;
- restores missing `.png.mcmeta` files for animated item strips;
- removes disabled duplicate recipe stubs and non-original Research Point/Cache recipes;
- materializes the first three missing original smelting recipes (magical logs, cinnabar, amber).

## v11.62.54 focus

- original top-left wand dial (`wandDialBottom=false`), with optional bottom-left placement;
- original six primal reservoirs, cost/change markers and sneaking values;
- focus icon or Equal Trade picked block in the dial centre;
- focus cooldown seconds in the original half-scale position;
- client HUD state reset on disconnect;
- original `TreeMap` focus sort/overwrite behaviour;
- transactional focus swaps: a full pouch/inventory cannot eject or lose the installed focus;
- exact rollback to the source inventory/pouch slot if a swap cannot complete.

## Validation

The committed CI audits use only the Python standard library; no third-party Python packages are required.

```bash
python3 tools/java_syntax_guard.py
python3 tools/validate_json_resources.py
python3 tools/tc4_116269_arcane_spa_gui_guard.py --version 11.62.69
python3 tools/tc4_116266_arcane_enchantment_guard.py --version 11.62.66
python3 tools/tc4_arcane_recipe_coverage_116266.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_arcane_recipe_full_mapping_v11.62.66.json \
  --md-out reports/TC4_ARCANE_RECIPE_FULL_MAPPING_V11_62_66.md
python3 tools/tc4_infusion_enchantment_coverage_116266.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_infusion_enchantment_coverage_v11.62.66.json \
  --md-out reports/TC4_INFUSION_ENCHANTMENT_COVERAGE_V11_62_66.md
python3 tools/tc4_116265_banner_ctm_relay_guard.py --version 11.62.65
python3 tools/tc4_arcane_recipe_coverage_116265.py \
  --root . \
  --original /path/to/TC4/thaumcraft/common/config/ConfigRecipes.java \
  --json-out reports/tc4_arcane_recipe_coverage_v11.62.65.json \
  --md-out reports/TC4_ARCANE_RECIPE_COVERAGE_V11_62_65.md
python3 tools/tc4_item_visual_audit.py --version 11.62.65 --fail-on-missing
python3 tools/model_transform_audit.py --version 11.62.65 --fail-on-problems
python3 tools/bewlr_contract_audit.py --version 11.62.65 --fail-on-problems
python3 tools/audit_registry.py --version 11.62.65 --fail-on-unexpected
```

## Full build

```bash
chmod +x gradlew
./gradlew build --stacktrace --no-daemon
```

Expected output:

```text
build/libs/thaumcraft_legacy_rebuild_1.19.2-11.62.69.jar
```

Audit reports are generated on demand into `reports/` and are not stored in the source archive.

## v11.62.54-hotfix1

This packaging hotfix repairs an invalid standalone prose line in `META-INF/mods.toml` that caused Forge to abort mod discovery and the launcher to report exit code 1. Gameplay classes are unchanged from v11.62.54. The known focus-transaction ordering defect D-001 remains open.


## v11.62.54-hotfix2

This source hotfix addresses the startup crash reported as `AbstractMethodError` in `BlockEntityRendererProvider`. Direct renderer, entity-renderer, menu-screen and colour-handler lambdas no longer implement obfuscated Minecraft interfaces themselves. They now target stable JDK/project interfaces and are forwarded through explicit adapter overrides that ForgeGradle can reobfuscate safely.

The crash was in the mod bootstrap bytecode, not in Oculus, Rubidium or the world save. Build this source through the included GitHub Actions workflow and use the produced reobfuscated JAR. The known focus transaction ordering issue D-001 is still tracked separately.

## v11.62.54-hotfix3

This CI/release hotfix repairs a stale source guard that rejected the required SRG-safe Research Table renderer adapter. The guard now verifies the wrapped registration and the explicit `BlockEntityRendererProvider` bridge instead of demanding the forbidden direct Minecraft SAM constructor reference. Gameplay code and assets are unchanged from hotfix2.


## v11.62.54-hotfix4

Adds an Alchemical Furnace menu/screen and revises the Crucible, Arcane Pedestal and Arcane Workbench block models after runtime screenshot review. Adds a complete item-model/texture-reference audit.


## v11.62.54-hotfix5

Fixes research puzzle parity: identical aspects no longer connect, new placements must touch a compatible occupied hex, Research Expertise reveals compound components and keeps its 25% removal refund, while Research Mastery keeps the 50% refund, adds the original 10% free placement chance and Shift-click automatic combination shortcut.

## v11.62.54-hotfix6

- Generates the exact 201-entry TC4 4.2.3.5 research graph, metadata and all 591 ordered ResearchPage declarations from the committed original source map.
- Audits every research icon, both language sets and the original Thaumonomicon GUI textures.
- Restores original concealed-page removal, dynamic known-aspect pages and research tooltip cues.
- Re-centres all WandItem BEWLR meshes so iron, greatwood, silverwood and creative wands render in inventory and first person.

## v11.62.54-hotfix7

- Removes the duplicated `PRIMPEARL` item trigger from both committed source maps and generated runtime metadata.
- Regenerates the machine-readable full research audit with duplicate-trigger detection.
- Normalizes the consolidated report revision history, numbering and hotfix appendices.
- Clarifies that `gui_research.png` supplies frames/tabs while research icon content may come from an `ItemStack` or a standalone resource texture.
