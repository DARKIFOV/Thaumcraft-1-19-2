package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Stage221:
 * Original anchor includes ConfigItems.itemChestCultistPlate for crab helm break drops.
 * TC4 crimson plate armor item bridge, preserving original texture ids. */
public class TC4CrimsonPlateArmorItem extends ArmorItem {
    private final String originalField;
    private final String legacyTexture;
    private final boolean leader;

    public TC4CrimsonPlateArmorItem(EquipmentSlot slot, Properties properties, String originalField, String legacyTexture, boolean leader) {
        super(TC4CrimsonPlateArmorMaterial.INSTANCE, slot, properties);
        this.originalField = originalField;
        this.legacyTexture = legacyTexture;
        this.leader = leader;
    }

    public String originalField() {
        return originalField;
    }

    public String legacyTexture() {
        return legacyTexture;
    }

    public boolean isLeaderVariant() {
        return leader;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("TC4 crimson/cultist plate parity: " + originalField).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Legacy texture: textures/items/" + legacyTexture + ".png").withStyle(ChatFormatting.DARK_GRAY));
    }
}
