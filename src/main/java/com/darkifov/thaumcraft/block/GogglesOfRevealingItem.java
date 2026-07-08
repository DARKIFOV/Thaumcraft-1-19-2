package com.darkifov.thaumcraft.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Forge 1.19.2 adapter for TC4 1.7.10 ItemGoggles.
 * Source of truth: thaumcraft.common.items.armor.ItemGoggles.
 *
 * Original behavior kept here:
 * - durability 350;
 * - 5% vis discount;
 * - acts as IRevealer/IGoggles for node and thaumic popup visibility;
 * - no fake scan, no research unlock side effect, no debug HUD data.
 */
public class GogglesOfRevealingItem extends ArmorItem {
    public static final int TC4_DURABILITY = 350;
    public static final int VIS_DISCOUNT = 5;

    public GogglesOfRevealingItem(Properties properties) {
        super(ArmorMaterials.GOLD, EquipmentSlot.HEAD, properties.durability(TC4_DURABILITY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) {
            ItemStack equipped = stack.copy();
            equipped.setCount(1);
            player.setItemSlot(EquipmentSlot.HEAD, equipped);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(player, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        // TC4 goggles reveal information; they do not grant research or perform scans by ticking.
    }

    public static boolean showNodes(ItemStack stack, LivingEntity wearer) {
        return !stack.isEmpty() && wearer != null;
    }

    public static boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return showNodes(stack, wearer);
    }

    public static int visDiscount(ItemStack stack, LivingEntity wearer) {
        return showNodes(stack, wearer) ? VIS_DISCOUNT : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tc.visdiscount", VIS_DISCOUNT).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("Reveals aura nodes and thaumic popups like TC4 IRevealer/IGoggles.").withStyle(ChatFormatting.GRAY));
    }
}
