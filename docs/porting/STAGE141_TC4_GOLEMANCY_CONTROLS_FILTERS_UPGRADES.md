# Stage141 — TC4 Golemancy Controls / Filters / Upgrades Parity

Stage141 continues from the Stage140 Golemancy checkpoint and focuses on the systems that make TC4 golems usable as a gameplay branch rather than simple mobs.

## Added

- TC4-style golem upgrade runtime:
  - Air
  - Fire
  - Water
  - Earth
  - Order
  - Entropy
- New upgrade items:
  - `thaumcraft:golem_upgrade_air`
  - `thaumcraft:golem_upgrade_fire`
  - `thaumcraft:golem_upgrade_water`
  - `thaumcraft:golem_upgrade_earth`
  - `thaumcraft:golem_upgrade_order`
  - `thaumcraft:golem_upgrade_entropy`
- New filter item:
  - `thaumcraft:golem_filter`
- New task marker item:
  - `thaumcraft:golem_task_marker`
- Expanded Golem Bell modes:
  - Home
  - Marker
  - Recall
  - Wait
  - Retask

## Expanded TC4 core coverage

Stage140 covered gather/fill/empty/guard/harvest/lumber/use/sorting. Stage141 adds runtime support for additional original-style cores:

- Bodyguard
- Butcher
- Fish
- Liquid
- Essentia
- Patrol

Liquid and essentia are still mapped to item/logistics behavior for now, but the explicit core modes exist and are saved, displayed and retaskable so the exact essentia/liquid network behavior can be tightened in later stages without changing saved data.

## Runtime behavior changes

- Golem cores can now store attached upgrades, marker positions and a filter stack in NBT.
- Spawned golems copy the complete core configuration into entity NBT.
- Air upgrade increases movement/work range.
- Earth upgrade increases durability, armor and carrying capacity.
- Fire upgrade improves combat/destructive work.
- Water upgrade improves fishing/harvesting timing.
- Order upgrade improves filtered/sorting behavior and carrying capacity.
- Entropy upgrade increases aggression at defensive cost.
- Golems now track separate home/input/output/guard/work positions.
- Empty core can pull from input containers.
- Fill/sorting can deliver to output/home containers.
- Patrol core moves between home and work marker.
- Fish core produces fish near its work marker over time.
- Butcher core attacks nearby adult animals.
- Bodyguard core keeps guard behavior centered on owner/guard marker.

## Validation

`tc4_stage141_golemancy_controls_audit.py` checks that the new control, filter, marker and upgrade systems are present and registered.
