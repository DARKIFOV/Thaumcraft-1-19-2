package com.darkifov.thaumcraft.item;

import net.minecraft.world.item.Item;

/** Passive TC4 ore-cluster item. Smelting yield is data-driven by counted_smelting recipes. */
public class TC4OreClusterItem extends Item {
    private final String material;

    public TC4OreClusterItem(Properties properties, String material) {
        super(properties);
        this.material = material;
    }

    public String material() { return material; }
}
