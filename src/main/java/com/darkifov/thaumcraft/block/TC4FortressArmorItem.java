package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.runic.TC4FortressMaskRuntime;
import com.darkifov.thaumcraft.runic.TC4FortressArmorRuntime;
import com.darkifov.thaumcraft.runic.TC4WarpingGearAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Stage212 1.19.2 replacement for original TC4 ItemFortressArmor.
 * Preserves the original helmet NBT upgrades:
 * - byte/int tag "goggles" from HELMGOGGLES infusion
 * - int tag "mask" from MASK* infusion recipes
 */
public class TC4FortressArmorItem extends ArmorItem {
    private final String originalSource;
    private final String legacyTexture;

    public TC4FortressArmorItem(EquipmentSlot slot, Properties properties, String originalSource, String legacyTexture) {
        super(TC4FortressArmorMaterial.INSTANCE, slot, properties);
        this.originalSource = originalSource;
        this.legacyTexture = legacyTexture;
    }

    public boolean isFortressHelm() {
        return getSlot() == EquipmentSlot.HEAD;
    }

    public static boolean hasGoggles(ItemStack stack) {
        return TC4FortressMaskRuntime.hasGoggles(stack);
    }

    public static int mask(ItemStack stack) {
        return TC4FortressMaskRuntime.mask(stack);
    }

    public static boolean isFortressPiece(ItemStack stack) {
        return TC4FortressArmorRuntime.isFortressPiece(stack);
    }

    public static double fortressSetModifier(net.minecraft.world.entity.player.Player player) {
        return TC4FortressArmorRuntime.fortressSetModifier(player);
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("TC4 ItemFortressArmor parity: " + originalSource).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Legacy texture: " + legacyTexture).withStyle(ChatFormatting.DARK_GRAY));
        if (isFortressHelm() && hasGoggles(stack)) {
            tooltip.add(Component.translatable("tc4.fortress.goggles").withStyle(ChatFormatting.AQUA));
        }
        int mask = mask(stack);
        if (isFortressHelm() && mask >= 0) {
            tooltip.add(Component.translatable("item.HelmetFortress.mask." + mask).withStyle(ChatFormatting.GOLD));
        }
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
        if (level != null) {
            TC4WarpingGearAdapter.appendTooltip(stack, null, tooltip);
        }
    }
}
