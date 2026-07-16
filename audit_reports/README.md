# Texture and UV audit artifacts

The files directly in this directory are the exhaustive **11.62.91 baseline** requested for comparison with TC4 4.2.3.5:

- `texture_audit.csv`
- `model_audit.csv`
- `custom_renderer_audit.csv`
- `gui_audit.csv`
- `render_type_audit.csv`
- `item_context_audit.csv`
- `summary.md`
- `diffs/`

`baseline_11.62.91/` preserves the same immutable baseline set. `postfix_11.62.92/` is the rerun after fixing the two source-confirmed discrepancies. Static closure does not constitute runtime visual PASS.
