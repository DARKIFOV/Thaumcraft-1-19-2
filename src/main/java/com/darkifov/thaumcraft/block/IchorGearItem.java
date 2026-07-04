package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public class IchorGearItem extends Item {
    public enum Mode {
        PICKAXE,
        SWORD,
        HOOD,
        ROBE,
        LEGGINGS,
        BOOTS
    }

    private final Mode mode;

    public IchorGearItem(Properties properties, Mode mode) {
        super(properties.durability(durability(mode)));
        this.mode = mode;
    }

    private static int durability(Mode mode) {
        return switch (mode) {
            case PICKAXE -> 4096;
            case SWORD -> 3072;
            case HOOD, BOOTS -> 880;
            case ROBE, LEGGINGS -> 1280;
        };
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 28;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (mode == Mode.SWORD) {
            target.setSecondsOnFire(3);
            stack.hurtAndBreak(1, attacker, entity -> {});
            return true;
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (mode == Mode.PICKAXE) {
                player.displayClientMessage(Component.literal("Ichor Pickaxe mode: efficient magical mining base. Stage 60 gives durability/enchantability; true mining tiers need a later ToolItem rewrite.").withStyle(ChatFormatting.GOLD), false);
            } else if (mode == Mode.SWORD) {
                player.displayClientMessage(Component.literal("Ichor Sword base: ignites enemies and has high enchantability.").withStyle(ChatFormatting.GOLD), false);
            } else {
                player.displayClientMessage(Component.literal("Ichorcloth gear base: high durability, enchantability and KAMI branch marker.").withStyle(ChatFormatting.GOLD), false);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("KAMI / Ichor gear rewrite base.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Durability: " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Enchantability: 28").withStyle(ChatFormatting.LIGHT_PURPLE));

        if (mode == Mode.SWORD) {
            tooltip.add(Component.literal("Hit effect: ignites target.").withStyle(ChatFormatting.RED));
        }

        int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack);
        if (unbreaking > 0) {
            tooltip.add(Component.literal("Unbreaking synergy: " + unbreaking).withStyle(ChatFormatting.AQUA));
        }
    }
}
