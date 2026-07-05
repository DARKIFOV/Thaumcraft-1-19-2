from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []

for path in (ROOT / "src/main/java").rglob("*.java"):
    text = path.read_text(encoding="utf-8", errors="ignore")
    rel = path.relative_to(ROOT)

    if '\\"' in text:
        errors.append(f"{rel}: contains literal escaped quote sequence \\\\\" in Java source")

    # The project should not contain source-level backslash-n sequences used as physical newlines.
    # Allow this only if future code explicitly needs it inside a string by annotating the file.
    if "\\n        " in text or "\\n    " in text:
        errors.append(f"{rel}: contains literal backslash-n indentation sequence in Java source")

if errors:
    for error in errors:
        print("::error::" + error)
    sys.exit(1)

print("Java syntax guard: OK")
