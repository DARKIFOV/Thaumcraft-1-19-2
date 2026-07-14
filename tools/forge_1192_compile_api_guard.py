from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java"
errors: list[str] = []

# These symbols belong to other Minecraft mapping/version surfaces and caused
# the real Forge 1.19.2 compileJava failure captured by CI run 79321665380.
for path in SRC.rglob("*.java"):
    text = path.read_text(encoding="utf-8", errors="ignore")
    rel = path.relative_to(ROOT)
    if "net.minecraft.world.inventory.GenericContainerMenu" in text:
        errors.append(f"{rel}: GenericContainerMenu is not present in Forge 1.19.2 official mappings; use ChestMenu")
    if "net.minecraft.world.UseAnim" in text:
        errors.append(f"{rel}: UseAnim must be imported from net.minecraft.world.item in 1.19.2")

mod = (SRC / "com/darkifov/thaumcraft/ThaumcraftMod.java").read_text(encoding="utf-8")
required = "RegistryObject<PurifyingFluidBlock> PURIFYING_FLUID_BLOCK"
if required not in mod:
    errors.append(
        "src/main/java/com/darkifov/thaumcraft/ThaumcraftMod.java: "
        "Purifying Fluid block registry object must retain the concrete PurifyingFluidBlock type "
        "for ForgeFlowingFluid.Properties#block"
    )

hungry = (SRC / "com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java").read_text(encoding="utf-8")
if "import net.minecraft.world.inventory.ChestMenu;" not in hungry or "ChestMenu.threeRows(" not in hungry:
    errors.append(
        "src/main/java/com/darkifov/thaumcraft/blockentity/HungryChestBlockEntity.java: "
        "Hungry Chest must open the Forge 1.19.2 ChestMenu.threeRows menu"
    )

soap = (SRC / "com/darkifov/thaumcraft/block/SanitySoapItem.java").read_text(encoding="utf-8")
if "import net.minecraft.world.item.UseAnim;" not in soap:
    errors.append(
        "src/main/java/com/darkifov/thaumcraft/block/SanitySoapItem.java: "
        "Sanity Soap must use net.minecraft.world.item.UseAnim"
    )

if errors:
    for error in errors:
        print("::error::" + error)
    sys.exit(1)

print("Forge 1.19.2 compile API guard: OK")
