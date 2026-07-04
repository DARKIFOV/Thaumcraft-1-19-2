package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class IchorArmorItem extends ArmorItem {
    public IchorArmorItem(EquipmentSlot slot, Properties properties) {
        super(IchorArmorMaterial.INSTANCE, slot, properties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && player.tickCount % 200 == 0) {
            if (PlayerThaumData.hasResearch(player, "KAMI_COMPLETION_GATE")) {
                player.getFoodData().eat(1, 0.1F);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("KAMI-tier Ichorcloth armor.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Bonus: minor sustain after KAMI gate unlock.").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
