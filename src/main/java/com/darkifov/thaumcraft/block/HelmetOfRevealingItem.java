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
 * TC4 adapter for the reveal-capable helmet path.
 * This stays reveal-only: no scan command, no fake research/warp unlocks.
 */
public class HelmetOfRevealingItem extends ArmorItem {
    private static final String INVISIBLE_ARMOR_TEXTURE = "thaumcraft:textures/models/armor/tc4_empty_layer_1.png";
    public HelmetOfRevealingItem(Properties properties) {
        super(ArmorMaterials.GOLD, EquipmentSlot.HEAD, properties.durability(880));
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
            level.playSound(player, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.PLAYERS, 0.8F, 1.1F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
                                  EquipmentSlot slot, String type) {
        // The visible shape is provided by TC4GogglesLayer. Returning the blank
        // texture prevents Forge's vanilla gold helmet cube from rendering first.
        return INVISIBLE_ARMOR_TEXTURE;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        // Reveal helmets are passive IRevealer-style gear. They must not unlock research by ticking.
    }

    public static boolean showNodes(ItemStack stack, LivingEntity wearer) {
        return !stack.isEmpty() && wearer != null;
    }

    public static boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return showNodes(stack, wearer);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Reveals aura nodes and thaumic popups while worn.").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("No fake scan/research/warp side effects.").withStyle(ChatFormatting.GRAY));
    }
}
