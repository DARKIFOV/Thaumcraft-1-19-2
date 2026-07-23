package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.wand.WandCapType;
import com.darkifov.thaumcraft.wand.WandRodType;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

/** De-metadata implementation of original ItemWandCap and ItemWandRod variants. */
public final class TC4WandComponentItem extends Item {
    public enum Family { ACTIVE_CAP, INERT_CAP, WAND_ROD, STAFF_ROD }

    private final Family family;
    private final WandCapType cap;
    private final WandRodType rod;

    private TC4WandComponentItem(Properties properties, Family family,
                                 @Nullable WandCapType cap, @Nullable WandRodType rod) {
        super(properties);
        this.family = family;
        this.cap = cap;
        this.rod = rod;
    }

    public static TC4WandComponentItem activeCap(Properties properties, WandCapType cap) {
        return new TC4WandComponentItem(properties, Family.ACTIVE_CAP, cap, null);
    }

    public static TC4WandComponentItem inertCap(Properties properties, WandCapType cap) {
        return new TC4WandComponentItem(properties, Family.INERT_CAP, cap, null);
    }

    public static TC4WandComponentItem rod(Properties properties, WandRodType rod) {
        return new TC4WandComponentItem(properties, rod.staff() ? Family.STAFF_ROD : Family.WAND_ROD, null, rod);
    }

    public Family family() { return family; }
    @Nullable public WandCapType cap() { return cap; }
    @Nullable public WandRodType rod() { return rod; }
    public boolean isInert() { return family == Family.INERT_CAP; }
}
