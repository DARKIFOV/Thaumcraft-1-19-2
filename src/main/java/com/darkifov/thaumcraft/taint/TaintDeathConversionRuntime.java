package com.darkifov.thaumcraft.taint;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.entity.TC4ThaumicSlimeEntity;
import com.darkifov.thaumcraft.entity.TaintedMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** Recreates TC4's taint-poison death replacement table. */
public final class TaintDeathConversionRuntime {
    private TaintDeathConversionRuntime() {}

    public static void handle(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level instanceof ServerLevel level)
                || dead instanceof TaintedMob
                || !dead.hasEffect(ThaumcraftMod.TAINT_POISON.get())) {
            return;
        }

        Entity replacement;
        if (dead instanceof Creeper) replacement = ThaumcraftMod.TAINT_CREEPER.get().create(level);
        else if (dead instanceof Sheep) replacement = ThaumcraftMod.TAINT_SHEEP.get().create(level);
        else if (dead instanceof Cow) replacement = ThaumcraftMod.TAINT_COW.get().create(level);
        else if (dead instanceof Pig) replacement = ThaumcraftMod.TAINT_PIG.get().create(level);
        else if (dead instanceof Chicken) replacement = ThaumcraftMod.TAINT_CHICKEN.get().create(level);
        else if (dead instanceof Villager) replacement = ThaumcraftMod.TAINT_VILLAGER.get().create(level);
        else {
            TC4ThaumicSlimeEntity slime = ThaumcraftMod.THAUMIC_SLIME.get().create(level);
            if (slime != null) {
                int size = (int) (1.0F + Math.min(dead.getMaxHealth() / 10.0F, 6.0F));
                slime.setTc4Size(Math.max(1, size), true);
            }
            replacement = slime;
        }

        if (replacement == null) return;
        replacement.moveTo(dead.getX(), dead.getY(), dead.getZ(), dead.getYRot(), 0.0F);
        level.addFreshEntity(replacement);
    }
}
