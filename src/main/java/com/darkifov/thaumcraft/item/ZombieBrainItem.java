package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** TC4 ItemZombieBrain parity: food, hunger risk and warp on consumption. */
public class ZombieBrainItem extends Item {
    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.2F)
            .meat()
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 30 * 20, 0), 0.8F)
            .build();

    public ZombieBrainItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.tc4_brain.desc"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!level.isClientSide && living instanceof ServerPlayer player) {
            if (level.random.nextFloat() < 0.1F) {
                PlayerThaumData.addWarpSticky(player, 1);
            } else {
                PlayerThaumData.addWarpPermanent(player, 1 + level.random.nextInt(3));
            }
        }
        return super.finishUsingItem(stack, level, living);
    }
}
