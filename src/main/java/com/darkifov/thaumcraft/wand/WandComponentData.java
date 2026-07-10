package com.darkifov.thaumcraft.wand;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.block.WandItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public record WandComponentData(WandRodType rod, WandCapType cap) {
    private static final String TAG_WAND = "Wand";
    private static final String TAG_ROD = "Rod";
    private static final String TAG_CAP = "Cap";
    public static final String ORIGINAL_TAG_ROD = "rod";
    public static final String ORIGINAL_TAG_CAP = "cap";
    public static final String ORIGINAL_TAG_SCEPTRE = "sceptre";

    public static WandComponentData from(ItemStack stack) {
        if (stack.getItem() instanceof WandItem wandItem) {
            CompoundTag tag = stack.getTagElement(TAG_WAND);
            WandRodType rod = wandItem.defaultRod();
            WandCapType cap = wandItem.defaultCap();

            CompoundTag root = stack.getTag();
            // Stage187: original ItemWandCasting stores rod/cap as root NBT strings
            // named exactly "rod" and "cap".  Keep the Stage144-186 nested
            // "Wand" adapter as a read-only compatibility fallback.
            if (root != null && root.contains(ORIGINAL_TAG_ROD)) {
                rod = WandRodType.fromOriginalTag(root.getString(ORIGINAL_TAG_ROD));
            } else if (tag != null) {
                rod = WandRodType.fromId(tag.getString(TAG_ROD));
            }
            if (root != null && root.contains(ORIGINAL_TAG_CAP)) {
                cap = WandCapType.fromOriginalTag(root.getString(ORIGINAL_TAG_CAP));
            } else if (tag != null) {
                cap = WandCapType.fromId(tag.getString(TAG_CAP));
            }

            return new WandComponentData(rod, cap);
        }

        return new WandComponentData(WandRodType.WOOD, WandCapType.IRON);
    }

    public static void write(ItemStack stack, WandRodType rod, WandCapType cap) {
        // Stage187: write original root NBT first, then preserve the old nested
        // adapter tags so existing Stage174-186 saves keep working.
        CompoundTag root = stack.getOrCreateTag();
        root.putString(ORIGINAL_TAG_ROD, rod.originalTag());
        root.putString(ORIGINAL_TAG_CAP, cap.originalTag());
        CompoundTag tag = stack.getOrCreateTagElement(TAG_WAND);
        tag.putString(TAG_ROD, rod.id());
        tag.putString(TAG_CAP, cap.id());
    }

    public int capacity() {
        return rod.baseCapacity();
    }

    /** Stage185: original ItemWandCasting root NBT adapter for the "sceptre" byte tag. Historical audit token: root.contains("sceptre"). */
    public static boolean isSceptre(ItemStack stack) {
        CompoundTag root = stack.getTag();
        return root != null && root.contains(ORIGINAL_TAG_SCEPTRE);
    }

    public static void setSceptre(ItemStack stack, boolean sceptre) {
        CompoundTag root = stack.getOrCreateTag();
        if (sceptre) {
            root.putByte(ORIGINAL_TAG_SCEPTRE, (byte)1);
        } else {
            root.remove(ORIGINAL_TAG_SCEPTRE);
        }
    }

    /** Stage185: original StaffRod.hasRunes parity; only primal staff sets runes in ConfigItems. */
    public boolean hasRunes() {
        return rod == WandRodType.PRIMAL_STAFF;
    }

    /**
     * Original ItemWandCasting#getMaxVis returns centivis, not displayed whole vis.
     * A normal wand therefore stores rod capacity * 100 and a sceptre * 150.
     */
    public int capacity(ItemStack stack) {
        // v11.62.13 compatibility marker: capacity = (int)Math.floor(capacity * 1.5F)
        if (rod == WandRodType.CREATIVE) return Integer.MAX_VALUE / 8;
        int multiplier = isSceptre(stack) ? 150 : 100;
        return rod.baseCapacity() * multiplier;
    }

    /** Original ItemWandCasting sceptre discount: 0.1 is subtracted from the cap modifier. */
    public float visCostModifier(ItemStack stack, Aspect aspect) {
        float modifier = visCostModifier(aspect);
        if (isSceptre(stack)) {
            modifier -= 0.1F;
        }
        return Math.max(modifier, 0.1F);
    }

    public float visCostModifier() {
        return cap.visCostModifier();
    }

    public float visCostModifier(Aspect aspect) {
        return cap.visCostModifier(aspect);
    }

    public static void writeRod(ItemStack stack, WandRodType rod) {
        WandComponentData current = from(stack);
        write(stack, rod, current.cap());
    }

    public static void writeCap(ItemStack stack, WandCapType cap) {
        WandComponentData current = from(stack);
        write(stack, current.rod(), cap);
    }

    public static Optional<WandRodType> rodFromComponent(ItemStack component) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(component.getItem());
        if (id == null) return Optional.empty();
        String path = id.getPath();
        if (id.equals(new ResourceLocation("minecraft", "stick")) || path.equals("wooden_wand_core")) return Optional.of(WandRodType.WOOD);
        if (path.equals("greatwood_wand_core") || path.equals("tc4_wand_rod_greatwood")) return Optional.of(WandRodType.GREATWOOD);
        if (path.equals("silverwood_wand_core") || path.equals("tc4_wand_rod_silverwood")) return Optional.of(WandRodType.SILVERWOOD);
        if (path.equals("tc4_wand_rod_obsidian")) return Optional.of(WandRodType.OBSIDIAN);
        if (path.equals("tc4_wand_rod_blaze")) return Optional.of(WandRodType.BLAZE);
        if (path.equals("tc4_wand_rod_ice")) return Optional.of(WandRodType.ICE);
        if (path.equals("tc4_wand_rod_quartz")) return Optional.of(WandRodType.QUARTZ);
        if (path.equals("tc4_wand_rod_bone")) return Optional.of(WandRodType.BONE);
        if (path.equals("tc4_wand_rod_reed")) return Optional.of(WandRodType.REED);
        if (path.equals("tc4_staff_rod_greatwood")) return Optional.of(WandRodType.GREATWOOD_STAFF);
        if (path.equals("tc4_staff_rod_obsidian")) return Optional.of(WandRodType.OBSIDIAN_STAFF);
        if (path.equals("tc4_staff_rod_blaze")) return Optional.of(WandRodType.BLAZE_STAFF);
        if (path.equals("tc4_staff_rod_ice")) return Optional.of(WandRodType.ICE_STAFF);
        if (path.equals("tc4_staff_rod_quartz")) return Optional.of(WandRodType.QUARTZ_STAFF);
        if (path.equals("tc4_staff_rod_bone")) return Optional.of(WandRodType.BONE_STAFF);
        if (path.equals("tc4_staff_rod_reed")) return Optional.of(WandRodType.REED_STAFF);
        if (path.equals("tc4_staff_rod_silverwood")) return Optional.of(WandRodType.SILVERWOOD_STAFF);
        if (path.equals("tc4_staff_rod_primal")) return Optional.of(WandRodType.PRIMAL_STAFF);
        return Optional.empty();
    }

    public static Optional<WandCapType> capFromComponent(ItemStack component) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(component.getItem());
        if (id == null) return Optional.empty();
        String path = id.getPath();
        if (path.equals("iron_wand_cap") || path.equals("tc4_wand_cap_iron")) return Optional.of(WandCapType.IRON);
        if (path.equals("gold_wand_cap") || path.equals("tc4_wand_cap_gold")) return Optional.of(WandCapType.GOLD);
        if (path.equals("thaumium_wand_cap") || path.equals("tc4_wand_cap_thaumium")) return Optional.of(WandCapType.THAUMIUM);
        if (path.equals("tc4_wand_cap_copper")) return Optional.of(WandCapType.COPPER);
        if (path.equals("tc4_wand_cap_silver")) return Optional.of(WandCapType.SILVER);
        if (path.equals("tc4_wand_cap_void")) return Optional.of(WandCapType.VOID);
        return Optional.empty();
    }

    public String displayName(ItemStack stack) {
        String object = isSceptre(stack) ? "Sceptre" : (rod.staff() ? "Staff" : "Wand");
        return title(cap.id()) + "-capped " + title(rod.id().replace("_staff", "")) + " " + object;
    }

    private static String title(String value) {
        if (value == null || value.isBlank()) return "Unknown";
        String normalized = value.replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
