package com.darkifov.thaumcraft.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/** TC4 ItemTripleMeatTreat parity: strong meat food with a regeneration chance. */
public class TripleMeatTreatItem extends Item {
    private static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.8F)
            .meat()
            .alwaysEat()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 0), 0.66F)
            .build();

    public TripleMeatTreatItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumcraft.tc4_tripletreat.desc"));
    }
}
