package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.entity.projectile.PrimalArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** One of the six de-metadata'd TC4 primal arrow variants. */
public final class PrimalArrowItem extends ArrowItem {
    private final int primalType;

    public PrimalArrowItem(Properties properties, int primalType) {
        super(properties);
        this.primalType = Math.max(0, Math.min(5, primalType));
    }

    public int primalType() {
        return primalType;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new PrimalArrowEntity(level, shooter, primalType);
    }
}
