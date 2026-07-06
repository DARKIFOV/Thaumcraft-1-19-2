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

    public static WandComponentData from(ItemStack stack) {
        if (stack.getItem() instanceof WandItem wandItem) {
            CompoundTag tag = stack.getTagElement(TAG_WAND);
            WandRodType rod = wandItem.defaultRod();
            WandCapType cap = wandItem.defaultCap();

            if (tag != null) {
                rod = WandRodType.fromId(tag.getString(TAG_ROD));
                cap = WandCapType.fromId(tag.getString(TAG_CAP));
            }

            return new WandComponentData(rod, cap);
        }

        return new WandComponentData(WandRodType.WOOD, WandCapType.IRON);
    }

    public static void write(ItemStack stack, WandRodType rod, WandCapType cap) {
        CompoundTag tag = stack.getOrCreateTagElement(TAG_WAND);
        tag.putString(TAG_ROD, rod.id());
        tag.putString(TAG_CAP, cap.id());
    }

    public int capacity() {
        return rod.baseCapacity();
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
        if (path.equals("wooden_wand_core")) return Optional.of(WandRodType.WOOD);
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

    public String displayName() {
        return cap.id() + "-capped " + rod.id() + " wand";
    }
}
