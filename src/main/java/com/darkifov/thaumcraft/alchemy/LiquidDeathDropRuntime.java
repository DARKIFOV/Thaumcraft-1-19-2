package com.darkifov.thaumcraft.alchemy;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.block.EssentiaCrystalItem;
import com.darkifov.thaumcraft.damage.TC4DamageSources;
import com.darkifov.thaumcraft.source.TC4EntityAspectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.Map;

/** Restores TC4 EventHandlerEntity's Liquid Death crystal conversion branch. */
public final class LiquidDeathDropRuntime {
    private LiquidDeathDropRuntime() {
    }

    /** Exact TC4 formula: max(1, (1 + nextInt(amount)) / 2). */
    public static int crystalCountForRoll(int amount, int roll) {
        if (amount <= 0) {
            return 0;
        }
        int boundedRoll = Math.floorMod(roll, amount);
        return Math.max(1, (1 + boundedRoll) / 2);
    }

    public static void handle(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level instanceof ServerLevel level)
                || event.getSource() != TC4DamageSources.DISSOLVE) {
            return;
        }

        AspectList aspects = TC4EntityAspectRegistry.getAspectsForEntity(victim);
        for (Map.Entry<Aspect, Integer> entry : aspects.entries().entrySet()) {
            int amount = entry.getValue();
            if (amount <= 0 || victim.getRandom().nextBoolean()) {
                continue;
            }
            int crystals = crystalCountForRoll(amount, victim.getRandom().nextInt(amount));
            ItemStack stack = EssentiaCrystalItem.create(ThaumcraftMod.ESSENTIA_CRYSTAL.get(), entry.getKey());
            stack.setCount(crystals);
            event.getDrops().add(new ItemEntity(level,
                    victim.getX(), victim.getY() + victim.getEyeHeight(), victim.getZ(), stack));
        }
    }
}
