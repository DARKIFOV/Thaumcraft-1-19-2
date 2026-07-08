package com.darkifov.thaumcraft.infusion;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

/**
 * 1.19.2 adapter for TC4's IRunicArmor + EventHandlerRunic helpers.
 *
 * The original 1.7.10 implementation attached runic shielding to many item
 * classes through the IRunicArmor interface and stored augmentation levels in
 * the byte tag "RS.HARDEN".  The 1.19.2 port represents most TC4 items as
 * de-metadata'd registry entries, so this helper restores the original runtime
 * semantics without reintroducing 1.7.10 item metadata APIs.
 */
public final class TC4RunicArmorHelper {
    public static final String HARDEN_TAG = "RS.HARDEN";

    private static final Map<String, Integer> BASE_RUNIC_CHARGE = Map.ofEntries(
            Map.entry("thaumcraft:tc4_runic_amulet", 8),
            Map.entry("thaumcraft:tc4_runic_amulet_emergency", 7),
            Map.entry("thaumcraft:tc4_runic_girdle", 10),
            Map.entry("thaumcraft:tc4_runic_girdle_kinetic", 9),
            Map.entry("thaumcraft:tc4_runic_ring_lesser", 1),
            Map.entry("thaumcraft:tc4_runic_ring", 5),
            Map.entry("thaumcraft:tc4_runic_ring_charged", 4),
            Map.entry("thaumcraft:tc4_runic_ring_regen", 4)
    );

    private static final Set<String> AUGMENTABLE_ZERO_CHARGE = Set.of(
            "thaumcraft:tc4_bootstraveler",
            "thaumcraft:tc4_clothboots",
            "thaumcraft:tc4_clothchest",
            "thaumcraft:tc4_clothlegs",
            "thaumcraft:tc4_clothbootsover",
            "thaumcraft:tc4_clothchestover",
            "thaumcraft:tc4_clothlegsover",
            "thaumcraft:tc4_cultistboots",
            "thaumcraft:tc4_cultistplatechest",
            "thaumcraft:tc4_cultistplatehelm",
            "thaumcraft:tc4_cultistplatelegs",
            "thaumcraft:tc4_cultistplateleaderchest",
            "thaumcraft:tc4_cultistplateleaderhelm",
            "thaumcraft:tc4_cultistplateleaderlegs",
            "thaumcraft:tc4_cultistrobechest",
            "thaumcraft:tc4_cultistrobehelm",
            "thaumcraft:tc4_cultistrobelegs",
            "thaumcraft:tc4_gogglesrevealing",
            "thaumcraft:tc4_hoverharness",
            "thaumcraft:tc4_thaumiumboots",
            "thaumcraft:tc4_thaumiumchest",
            "thaumcraft:tc4_thaumiumhelm",
            "thaumcraft:tc4_thaumiumlegs",
            "thaumcraft:tc4_thaumiumfortresschest",
            "thaumcraft:tc4_thaumiumfortresshelm",
            "thaumcraft:tc4_thaumiumfortresslegs",
            "thaumcraft:tc4_voidboots",
            "thaumcraft:tc4_voidchest",
            "thaumcraft:tc4_voidhelm",
            "thaumcraft:tc4_voidlegs",
            "thaumcraft:tc4_voidrobechest",
            "thaumcraft:tc4_voidrobechestover",
            "thaumcraft:tc4_voidrobehelm",
            "thaumcraft:tc4_voidrobelegs",
            "thaumcraft:tc4_voidrobelegsover",
            "thaumcraft:tc4_voidrobeboots"
    );

    private TC4RunicArmorHelper() {
    }

    public static boolean isRunicArmor(ItemStack stack) {
        String id = registryId(stack);
        return id != null && (BASE_RUNIC_CHARGE.containsKey(id) || AUGMENTABLE_ZERO_CHARGE.contains(id));
    }

    public static int getBaseCharge(ItemStack stack) {
        String id = registryId(stack);
        if (id == null) {
            return 0;
        }
        return Math.max(0, BASE_RUNIC_CHARGE.getOrDefault(id, 0));
    }

    public static int getHardening(ItemStack stack) {
        if (!isRunicArmor(stack) || !stack.hasTag()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(HARDEN_TAG) ? 0 : Math.max(0, tag.getByte(HARDEN_TAG));
    }

    public static int getFinalCharge(ItemStack stack) {
        return isRunicArmor(stack) ? getBaseCharge(stack) + getHardening(stack) : 0;
    }

    public static ItemStack addHardening(ItemStack input) {
        if (!isRunicArmor(input)) {
            return ItemStack.EMPTY;
        }
        ItemStack out = input.copy();
        out.getOrCreateTag().putByte(HARDEN_TAG, (byte) Math.min(127, getHardening(input) + 1));
        return out;
    }


    public static boolean isRunicBauble(ItemStack stack) {
        String id = registryId(stack);
        return id != null && (id.contains("tc4_runic_ring") || id.contains("tc4_runic_amulet") || id.contains("tc4_runic_girdle"));
    }

    public static boolean isChargedVariant(ItemStack stack) {
        return "thaumcraft:tc4_runic_ring_charged".equals(registryId(stack));
    }

    public static boolean isHealingVariant(ItemStack stack) {
        return "thaumcraft:tc4_runic_ring_regen".equals(registryId(stack));
    }

    public static boolean isKineticVariant(ItemStack stack) {
        return "thaumcraft:tc4_runic_girdle_kinetic".equals(registryId(stack));
    }

    public static boolean isEmergencyVariant(ItemStack stack) {
        return "thaumcraft:tc4_runic_amulet_emergency".equals(registryId(stack));
    }

    public static void appendTooltip(ItemStack stack, java.util.List<Component> tooltip) {
        if (!isRunicArmor(stack)) {
            return;
        }
        int finalCharge = getFinalCharge(stack);
        tooltip.add(Component.literal("Runic shield: " + finalCharge).withStyle(ChatFormatting.AQUA));
        int hardening = getHardening(stack);
        if (hardening > 0) {
            tooltip.add(Component.literal(HARDEN_TAG + ": +" + hardening).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static String registryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? null : id.toString();
    }
}
