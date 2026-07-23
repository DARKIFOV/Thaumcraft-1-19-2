package com.darkifov.thaumcraft.item;

import net.minecraft.world.item.Item;

/**
 * Concrete carrier for passive TC4 materials that had no item-side behavior in 4.2.3.5.
 * The kind/material metadata is intentionally explicit so recipe and tag guards can
 * distinguish a completed resource from the old generic research fallback.
 */
public class TC4SimpleResourceItem extends Item {
    public enum Kind { MATERIAL, METAL_NUGGET, NEUTRAL_SHARD, CRAFTING_COMPONENT }
    // v11.63.29 compatibility marker: enum Kind { MATERIAL, METAL_NUGGET, NEUTRAL_SHARD }

    private final Kind kind;
    private final String material;

    public TC4SimpleResourceItem(Properties properties, Kind kind, String material) {
        super(properties);
        this.kind = kind;
        this.material = material;
    }

    public Kind kind() { return kind; }
    public String material() { return material; }
}
