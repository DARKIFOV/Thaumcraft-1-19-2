package com.darkifov.thaumcraft.block;

import net.minecraft.world.item.Item;

/**
 * Legacy compatibility shell for saves created by early rebuild versions.
 * TC4 research points are aspect-pool values, not a consumable item, so this
 * registry object intentionally has no gameplay action and is not obtainable.
 */
public class ResearchPointItem extends Item {
    public ResearchPointItem(Properties properties) {
        super(properties);
    }
}
