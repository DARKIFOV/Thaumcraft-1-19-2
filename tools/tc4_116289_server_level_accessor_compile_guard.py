from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java"
WISP = SRC / "com/darkifov/thaumcraft/entity/TC4WispEntity.java"
SLIME = SRC / "com/darkifov/thaumcraft/entity/TC4ThaumicSlimeEntity.java"
errors: list[str] = []

wrong = "import net.minecraft.server.level.ServerLevelAccessor;"
correct = "import net.minecraft.world.level.ServerLevelAccessor;"

for path in (WISP, SLIME):
    text = path.read_text(encoding="utf-8")
    rel = path.relative_to(ROOT)
    if wrong in text:
        errors.append(f"{rel}: wrong 1.19.2 Mojmap package for ServerLevelAccessor")
    if correct not in text:
        errors.append(f"{rel}: expected net.minecraft.world.level.ServerLevelAccessor import")
    if "ServerLevelAccessor" not in text:
        errors.append(f"{rel}: spawn/finalizeSpawn signature lost ServerLevelAccessor")

all_java = "\n".join(p.read_text(encoding="utf-8", errors="ignore") for p in SRC.rglob("*.java"))
if "net.minecraft.server.level.ServerLevelAccessor" in all_java:
    errors.append("src/main/java: forbidden net.minecraft.server.level.ServerLevelAccessor import remains")

if errors:
    for error in errors:
        print("::error::" + error)
    sys.exit(1)

print("TC4 11.62.91 ServerLevelAccessor compile hotfix guard: 7/7 PASS")
