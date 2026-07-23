package com.darkifov.thaumcraft.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * TC4 4.2.3.5 damage-source identities adapted to Minecraft 1.19.2.
 *
 * <p>The original taint source ignored armour and counted as magic; dissolve
 * ignored armour; tentacle and swarm attacks retained their attacking entity
 * so vanilla death-message attribution can name the attacker.</p>
 */
public final class TC4DamageSources {
    public static final DamageSource TAINT = new NamedSource("taint").bypassArmor().setMagic();
    public static final DamageSource DISSOLVE = new NamedSource("dissolve").bypassArmor();

    private TC4DamageSources() {
    }

    public static DamageSource tentacle(LivingEntity attacker) {
        return new EntityDamageSource("tentacle", attacker);
    }

    public static DamageSource swarm(LivingEntity attacker) {
        return new EntityDamageSource("swarm", attacker);
    }

    private static final class NamedSource extends DamageSource {
        private NamedSource(String messageId) {
            super(messageId);
        }
    }
}
