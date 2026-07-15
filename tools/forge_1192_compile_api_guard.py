from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java"
errors: list[str] = []

# These symbols belong to other Minecraft mapping/version surfaces and caused
# real Forge 1.19.2 compileJava failures captured by CI runs 79321665380 and
# 79417104482. Keep them in the early text guard so Gradle is not the first
# place where a known cross-version API regression is discovered.
for path in SRC.rglob("*.java"):
    text = path.read_text(encoding="utf-8", errors="ignore")
    rel = path.relative_to(ROOT)
    if "net.minecraft.world.inventory.GenericContainerMenu" in text:
        errors.append(f"{rel}: GenericContainerMenu is not present in Forge 1.19.2 official mappings; use ChestMenu")
    if "net.minecraft.world.UseAnim" in text:
        errors.append(f"{rel}: UseAnim must be imported from net.minecraft.world.item in 1.19.2")
    if "Mth.wrapRadians(" in text:
        errors.append(f"{rel}: Mth.wrapRadians(float) is unavailable in Forge/Minecraft 1.19.2; use a local radian normalizer")
    if ".onGround()" in text:
        errors.append(f"{rel}: Entity#onGround() is not the Mojmap 1.19.2 accessor; use isOnGround()")
    if "net.minecraft.world.entity.animal.TamableAnimal" in text:
        errors.append(f"{rel}: TamableAnimal is in net.minecraft.world.entity in Forge/Mojmap 1.19.2")
    if "BlockPos.containing(" in text:
        errors.append(f"{rel}: BlockPos.containing(double,double,double) is unavailable in Mojmap 1.19.2; floor coordinates explicitly")
    if ".canBeReplaced()" in text:
        errors.append(f"{rel}: no-arg BlockState#canBeReplaced() is unavailable in Mojmap 1.19.2; use the 1.19.2 material/context API")

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

trunk = (SRC / "com/darkifov/thaumcraft/entity/TravelingTrunkEntity.java").read_text(encoding="utf-8")
if "import net.minecraft.world.entity.TamableAnimal;" not in trunk:
    errors.append("src/main/java/com/darkifov/thaumcraft/entity/TravelingTrunkEntity.java: correct Mojmap TamableAnimal import is required")
if "public InteractionResult mobInteract(Player player, InteractionHand hand)" not in trunk:
    errors.append("src/main/java/com/darkifov/thaumcraft/entity/TravelingTrunkEntity.java: Animal#mobInteract is public in Mojmap 1.19.2")

taint_runtime = (SRC / "com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java").read_text(encoding="utf-8")
taint_spore = (SRC / "com/darkifov/thaumcraft/entity/TaintSporeEntity.java").read_text(encoding="utf-8")
if "public static boolean isColumnTainted(ServerLevel level, BlockPos pos)" not in taint_runtime:
    errors.append("src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java: public isColumnTainted entity contract is required")
if "TaintSpreadRuntime.isColumnTainted(server, blockPosition())" not in taint_spore:
    errors.append("src/main/java/com/darkifov/thaumcraft/entity/TaintSporeEntity.java: use the stable public taint-column contract")
if "new BlockPos(Mth.floor(getX()), Mth.floor(getBoundingBox().minY - 0.05D), Mth.floor(getZ()))" not in taint_spore:
    errors.append("src/main/java/com/darkifov/thaumcraft/entity/TaintSporeEntity.java: use explicit Mth.floor coordinates instead of post-1.19.2 BlockPos.containing")
if taint_runtime.count(".getMaterial().isReplaceable()") < 3:
    errors.append("src/main/java/com/darkifov/thaumcraft/taint/TaintSpreadRuntime.java: all three no-arg replacement checks must use the 1.19.2 Material API")

if errors:
    for error in errors:
        print("::error::" + error)
    sys.exit(1)

print("Forge 1.19.2 compile API guard: OK")
