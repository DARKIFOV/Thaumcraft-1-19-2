package com.darkifov.thaumcraft.entity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Forge 1.19.2 port of TC4's EntityBrainyZombie.
 *
 * The original variant is a tougher zombie with +3 armour, no reinforcement
 * chance, a 50% brain drop (increased by Looting) and three independent
 * rotten-flesh rolls.
 */
public class BrainyZombieEntity extends Zombie {
    public BrainyZombieEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0D);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        for (int roll = 0; roll < 3; roll++) {
            if (random.nextBoolean()) {
                spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH));
            }
        }
        if (random.nextInt(10) - looting <= 4) {
            var brain = ThaumcraftMod.TC4_RESEARCH_ITEMS.get("tc4_brain");
            if (brain != null) {
                spawnAtLocation(new ItemStack(brain.get()));
            }
        }
    }
}
