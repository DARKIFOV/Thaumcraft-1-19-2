# Stage623-642 research visual and scribing-tools parity cleanup

This stage continues the original TC4 parity pass for the Research Table, Research Note and Thaumonomicon.

Strict parity intent:

1. visible Thaumonomicon pages must not expose adapter/debug internals such as raw research keys, categories, inferred recipe keys or pattern letters;
2. research-note palette/page arrows must reuse the same original coordinate ledger as `GuiResearchTable` instead of drifting through hard-coded duplicate values;
3. Scribing Tools refill is represented as the original-style crafting path with an ink sac, not a new right-click ability or a duplicate item;
4. research-note creation remains Research Table only; no normal research-note crafting recipe is reintroduced.

This is a cleanup/parity stage, not a new progression system.
