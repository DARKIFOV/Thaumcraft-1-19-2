# TC4 normal recipes, gear and JEI audit — 11.62.61

**Status:** PASS

- Original normal-recipe entries checked: **39** (38 new JSON files plus corrected Jar Label).
- Vanilla shaped recipes: **82**.
- Vanilla shapeless recipes: **22**.
- Standard crafting recipes available to JEI's vanilla crafting category: **104**.
- Functional Thaumium/Void gear registrations checked: **18**.
- Legacy placeholder wood ids in active custom recipes: **0**.
- Legacy duplicate material ids in active recipes: **0**.

## Functional parity included

Thaumium and Void armor now use real ArmorItem implementations and the original armor textures. Thaumium tools use the TC4 material statistics. Void tools use the original material statistics, repair one durability point each second, apply Weakness on hit, and remain present in the warping-gear adapter. Active recipes now use canonical modern ingot/nugget carriers instead of save-migration aliases.

## Problems

No static parity problems detected by this guard.
