package com.darkifov.thaumcraft.client.render;

import com.darkifov.thaumcraft.client.render.model.TC4HoverHarnessArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

/** Client-only armor-model bridge installed by HoverHarnessItem.initializeClient. */
public final class TC4HoverHarnessClientExtension implements IClientItemExtensions {
    private static TC4HoverHarnessArmorModel<LivingEntity> model;

    public static void bake(EntityModelSet modelSet) {
        model = new TC4HoverHarnessArmorModel<>(modelSet.bakeLayer(TC4HoverHarnessArmorModel.LAYER));
    }

    @Override
    @NotNull
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                   EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (equipmentSlot != EquipmentSlot.CHEST || model == null) {
            return original;
        }
        model.showChestOnly();
        return model;
    }
}
