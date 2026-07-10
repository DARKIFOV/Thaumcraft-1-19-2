package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectStack;
import com.darkifov.thaumcraft.wand.WandFocusType;
import com.darkifov.thaumcraft.wand.FocusUpgradeRuntime;
import com.darkifov.thaumcraft.wand.FocusUpgradeType;
import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** ItemFocusBasic parity surface for TC4 focus stacks. */
public class WandFocusItem extends Item {
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#####.##");
    private final WandFocusType focusType;

    public WandFocusItem(Properties properties, WandFocusType focusType) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.focusType = focusType;
    }

    public WandFocusType focusType() {
        return focusType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // Original ItemFocusBasic displays cost first and formats internal hundredths as decimal vis.
        tooltip.add(Component.translatable(focusType.isVisCostPerTick() ? "item.Focus.cost2" : "item.Focus.cost1")
                .withStyle(ChatFormatting.DARK_AQUA));
        AspectList displayedCost = focusType.visCost();
        if (focusType == WandFocusType.FIRE) {
            if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.FIREBALL)) {
                displayedCost = new AspectList().add(Aspect.IGNIS, 66).add(Aspect.PERDITIO, 33);
            } else if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.FIREBEAM)) {
                displayedCost = new AspectList().add(Aspect.IGNIS, 10).add(Aspect.ORDO, 3);
            }
        }
        if (focusType == WandFocusType.FROST) {
            if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.ICE_BOULDER)) {
                displayedCost = new AspectList().add(Aspect.AQUA, 20).add(Aspect.IGNIS, 2).add(Aspect.PERDITIO, 2).add(Aspect.TERRA, 5);
            } else if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.SCATTERSHOT)) {
                displayedCost = new AspectList().add(Aspect.AQUA, 20).add(Aspect.IGNIS, 2).add(Aspect.PERDITIO, 2).add(Aspect.AER, 5);
            }
        }
        if (focusType == WandFocusType.HELLBAT) {
            if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.DEVIL_BATS)) {
                displayedCost = new AspectList().add(Aspect.IGNIS, 100).add(Aspect.PERDITIO, 100).add(Aspect.AER, 100).add(Aspect.TERRA, 100);
            } else if (FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.BAT_BOMBS)) {
                displayedCost = new AspectList().add(Aspect.IGNIS, 100).add(Aspect.PERDITIO, 200).add(Aspect.AER, 100);
            }
        }
        if (focusType == WandFocusType.PECH_CURSE && FocusUpgradeRuntime.isUpgradedWith(stack, FocusUpgradeType.NIGHTSHADE)) {
            displayedCost = new AspectList().add(Aspect.AER, 10).add(Aspect.IGNIS, 10).add(Aspect.TERRA, 10)
                    .add(Aspect.ORDO, 10).add(Aspect.PERDITIO, 10).add(Aspect.AQUA, 10);
        }
        for (AspectStack aspect : displayedCost.all()) {
            String amount = VIS_FORMAT.format(aspect.amount() / 100.0F);
            tooltip.add(Component.literal(aspect.aspect().displayName() + " x " + amount)
                    .withStyle(style -> style.withColor(aspect.aspect().textColor())));
        }

        // Original ItemFocusBasic groups duplicate upgrades and shows enchantment-level numerals.
        LinkedHashMap<FocusUpgradeType, Integer> grouped = new LinkedHashMap<>();
        for (short id : FocusUpgradeRuntime.getAppliedUpgrades(stack)) {
            FocusUpgradeType type = FocusUpgradeType.byId(id);
            if (type != null) grouped.put(type, grouped.getOrDefault(type, 0) + 1);
        }
        for (Map.Entry<FocusUpgradeType, Integer> entry : grouped.entrySet()) {
            Component line = Component.translatable(entry.getKey().nameKey());
            if (entry.getValue() > 1) {
                line = line.copy().append(" ").append(Component.translatable("enchantment.level." + entry.getValue()));
            }
            tooltip.add(line.copy().withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
