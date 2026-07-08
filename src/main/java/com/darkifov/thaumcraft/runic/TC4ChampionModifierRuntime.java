package com.darkifov.thaumcraft.runic;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.entity.CrimsonCultistEntity;
import com.darkifov.thaumcraft.entity.EldritchGuardianEntity;
import com.darkifov.thaumcraft.entity.EldritchWardenEntity;
import com.darkifov.thaumcraft.entity.EldritchGolemEntity;
import com.darkifov.thaumcraft.entity.TaintCrawlerEntity;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;

/**
 * Stage215 1.19.2 adapter for TC4 ChampionModifier (keeps Stage214 1.19.2 adapter behavior), showFX and generation parity.
 *
 * The original champion table had ids 0..12 and three runtime types:
 * type 0 = periodic self effect, type 1 = offensive attack effect, type 2 =
 * defensive effect when the champion is hurt.  This class preserves those ids
 * through persistent data tag TC4ChampionMod so existing/migrated mobs can keep
 * the same behavior without importing TC4 1.7.10 APIs.
 */
public final class TC4ChampionModifierRuntime {
    public static final String TC4_CHAMPION_MOD_TAG = "TC4ChampionMod";
    public static final String TC4_CHAMPION_PERSIST_TAG = "TC4ChampionPersist";
    public static final String TC4_CHAMPION_NAME_TAG = "TC4ChampionName";
    public static final String TC4_CHAMPION_GENERATION_CHECKED_TAG = "TC4ChampionGenerationChecked";

    private static final UUID CHAMPION_HEALTH_UUID = UUID.fromString("a62bef38-48cc-42a6-ac5e-ef913841c4fd");
    private static final UUID CHAMPION_DAMAGE_UUID = UUID.fromString("a340d2db-d881-4c25-ac62-f0ad14cd63b0");
    private static final UUID BOLD_SPEED_UUID = UUID.fromString("4b1edd33-caa9-47ae-a702-d86c05701037");
    private static final UUID MIGHTY_DAMAGE_UUID = UUID.fromString("7163897f-07f5-49b3-9ce4-b74beb83d2d3");
    public static final double CHAMPION_MOD_SENTINEL = -2.0D;

    /** TC4 ConfigEntities.championModWhitelist mirror using 1.19.2 registry ids. */
    private static final Map<String, Integer> CHAMPION_MOD_WHITELIST = Map.ofEntries(
            Map.entry("minecraft:zombie", 0),
            Map.entry("minecraft:spider", 0),
            Map.entry("minecraft:blaze", 0),
            Map.entry("minecraft:enderman", 0),
            Map.entry("minecraft:skeleton", 0),
            Map.entry("minecraft:witch", 1),
            Map.entry("thaumcraft:eldritch_guardian", 0),
            Map.entry("thaumcraft:eldritch_warden", 200),
            Map.entry("thaumcraft:eldritch_golem", 200),
            Map.entry("thaumcraft:taint_crawler", 1),
            Map.entry("thaumcraft:crimson_cultist", 1),
            Map.entry("thaumcraft:crimson_knight", 1),
            Map.entry("thaumcraft:crimson_cleric", 1),
            Map.entry("thaumcraft:crimson_praetor", 200)
    );

    public static final ChampionData[] MODS = new ChampionData[] {
            new ChampionData(0, "bold", -1),
            new ChampionData(1, "spine", 2),
            new ChampionData(2, "armor", 2),
            new ChampionData(3, "mighty", -1),
            new ChampionData(4, "grim", 1),
            new ChampionData(5, "warded", 0),
            new ChampionData(6, "warp", 1),
            new ChampionData(7, "undying", 0),
            new ChampionData(8, "fiery", 1),
            new ChampionData(9, "sickly", 1),
            new ChampionData(10, "venomous", 1),
            new ChampionData(11, "vampiric", 1),
            new ChampionData(12, "infested", 2)
    };

    private TC4ChampionModifierRuntime() {
    }

    public static int championMod(LivingEntity entity) {
        if (entity == null) {
            return -1;
        }
        CompoundTag tag = entity.getPersistentData();
        int mod = tag.contains(TC4_CHAMPION_MOD_TAG) ? tag.getInt(TC4_CHAMPION_MOD_TAG) : -1;
        return mod >= 0 && mod < MODS.length ? mod : -1;
    }

    public static void setChampion(Mob mob, int mod, boolean persist) {
        if (mob == null || mod < 0 || mod >= MODS.length) {
            return;
        }
        CompoundTag tag = mob.getPersistentData();
        tag.putInt(TC4_CHAMPION_MOD_TAG, mod);
        tag.putBoolean(TC4_CHAMPION_PERSIST_TAG, persist);
        tag.putString(TC4_CHAMPION_NAME_TAG, MODS[mod].name());
        tag.putBoolean(TC4_CHAMPION_GENERATION_CHECKED_TAG, true);
        applyBaseChampionAttributes(mob, mod);
        refreshChampionDisplayName(mob);
        if (persist) {
            mob.setPersistenceRequired();
        }
    }

    public static void makeChampion(Mob mob, boolean persist) {
        if (mob == null) {
            return;
        }
        int type = mob instanceof Creeper ? 0 : mob.getRandom().nextInt(MODS.length);
        setChampion(mob, type, persist);
    }

    public static void tick(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || entity.level.isClientSide) {
            return;
        }
        int mod = championMod(mob);
        if (mod < 0) {
            return;
        }
        refreshChampionDisplayName(mob);
        if (mod == 5 && mob.invulnerableTime <= 0 && mob.tickCount % 25 == 0) {
            float cap = mob.getMaxHealth() / 2.0F;
            if (mob.getAbsorptionAmount() < cap) {
                mob.setAbsorptionAmount(Math.min(cap, mob.getAbsorptionAmount() + 1.0F));
            }
        } else if (mod == 7 && mob.tickCount % 20 == 0) {
            mob.heal(1.0F);
        }
        if (mob.level instanceof ServerLevel level && mob.tickCount % 4 == 0) {
            // TC4 RenderEventHandler called ChampionModifier.mods[t].effect.showFX(mob) on the client each tick.
            // The 1.19.2 port mirrors that with a compact nearby packet and a client-side particle adapter.
            ThaumcraftNetwork.sendChampionFx(level, mob, mod, 32.0D);
        }
    }

    public static void handleHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F || event.isCanceled()) {
            return;
        }
        applyDefensiveChampionEffect(event);
        applyOffensiveChampionEffect(event);
    }

    public static void maybeMakeSpawnChampion(Entity entity) {
        if (!(entity instanceof Mob mob) || mob.level.isClientSide) {
            return;
        }
        if (championMod(mob) >= 0) {
            refreshChampionDisplayName(mob);
            return;
        }
        CompoundTag tag = mob.getPersistentData();
        if (tag.getBoolean(TC4_CHAMPION_GENERATION_CHECKED_TAG)) {
            return;
        }
        tag.putBoolean(TC4_CHAMPION_GENERATION_CHECKED_TAG, true);
        if (!(mob.level instanceof ServerLevel level) || mob.getMaxHealth() < 10.0F) {
            return;
        }
        int whitelistLevel = championWhitelistLevel(mob);
        if (whitelistLevel < 0) {
            return;
        }

        boolean championMobs = ThaumcraftConfig.CHAMPION_MOBS.get();
        int c = mob.getRandom().nextInt(100);
        if (level.getDifficulty() == Difficulty.EASY || !championMobs) {
            c += 2;
        }
        if (level.getDifficulty() == Difficulty.HARD) {
            c -= championMobs ? 2 : 0;
        }
        if (isNetherOrEnd(level)) {
            c -= championMobs ? 2 : 1;
        }
        if (isDangerousLocation(level, mob.blockPosition())) {
            c -= championMobs ? 10 : 3;
        }

        boolean bossLike = isBossLikeChampion(mob);
        if (championMobs || bossLike) {
            c -= Math.max(0, whitelistLevel - 1);
        }
        if ((championMobs || bossLike || c <= -3) && c <= 0) {
            makeChampion(mob, bossLike);
        }
    }

    private static void applyDefensiveChampionEffect(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Mob champion)) {
            return;
        }
        int mod = championMod(champion);
        if (mod < 0 || MODS[mod].type() != 2) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        float amount = event.getAmount();
        if (mod == 1) {
            if (!"thorns".equalsIgnoreCase(event.getSource().getMsgId())) {
                livingAttacker.hurt(DamageSource.thorns(champion), 1.0F + champion.getRandom().nextInt(3));
                champion.level.playSound(null, champion.blockPosition(), TC4Sounds.event("zap"), SoundSource.HOSTILE, 0.5F, 1.0F);
            }
        } else if (mod == 2 && !event.getSource().isBypassArmor()) {
            event.setAmount(amount * 19.0F / 25.0F);
        } else if (mod == 12 && champion.level instanceof ServerLevel level && champion.getRandom().nextFloat() < 0.4F) {
            TaintCrawlerEntity crawler = ThaumcraftMod.TAINT_CRAWLER.get().create(level);
            if (crawler != null) {
                crawler.moveTo(champion.getX(), champion.getY() + champion.getBbHeight() * 0.5D, champion.getZ(), champion.getRandom().nextFloat() * 360.0F, 0.0F);
                crawler.setTarget(livingAttacker);
                level.addFreshEntity(crawler);
                level.playSound(null, champion.blockPosition(), TC4Sounds.event("gore"), SoundSource.HOSTILE, 0.5F, 1.0F);
            }
        }
    }

    private static void applyOffensiveChampionEffect(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Mob champion) || !(event.getEntity() instanceof LivingEntity target)) {
            return;
        }
        int mod = championMod(champion);
        if (mod < 0 || MODS[mod].type() != 1) {
            return;
        }
        if (mod == 4 && champion.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
        } else if (mod == 6 && champion.getRandom().nextFloat() < 0.33F && target instanceof Player player) {
            PlayerThaumData.addWarpTemporary(player, 1 + champion.getRandom().nextInt(3));
        } else if (mod == 8 && champion.getRandom().nextFloat() < 0.4F) {
            target.setSecondsOnFire(4);
        } else if (mod == 9 && champion.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500));
        } else if (mod == 10 && champion.getRandom().nextFloat() < 0.4F) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        } else if (mod == 11) {
            champion.heal(Math.max(2.0F, event.getAmount() / 2.0F));
        }
    }

    private static void applyBaseChampionAttributes(Mob mob, int mod) {
        mob.getPersistentData().putDouble("tc.mobmod", mod);
        if (!isBossLikeChampion(mob)) {
            AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getModifier(CHAMPION_HEALTH_UUID) == null) {
                maxHealth.addPermanentModifier(new AttributeModifier(CHAMPION_HEALTH_UUID, "TC4 Champion health buff", 30.0D, AttributeModifier.Operation.ADDITION));
                mob.heal(25.0F);
            }
            AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null && damage.getModifier(CHAMPION_DAMAGE_UUID) == null) {
                damage.addPermanentModifier(new AttributeModifier(CHAMPION_DAMAGE_UUID, "TC4 Champion damage buff", 2.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (mod == 0) {
            AttributeInstance speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getModifier(BOLD_SPEED_UUID) == null) {
                speed.addPermanentModifier(new AttributeModifier(BOLD_SPEED_UUID, "Bold speed boost", 0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else if (mod == 3) {
            AttributeInstance damage = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null && damage.getModifier(MIGHTY_DAMAGE_UUID) == null) {
                damage.addPermanentModifier(new AttributeModifier(MIGHTY_DAMAGE_UUID, "Mighty damage boost", 3.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else if (mod == 5) {
            mob.setAbsorptionAmount(mob.getAbsorptionAmount() + mob.getMaxHealth() / 2.0F);
        }
    }

    public static void refreshChampionDisplayName(Mob mob) {
        int mod = championMod(mob);
        if (mod < 0) {
            return;
        }
        Component modifier = Component.translatable("champion.mod." + MODS[mod].name());
        Component name;
        if (mob instanceof CrimsonCultistEntity cultist && cultist.role() == CrimsonCultistEntity.Role.LEADER) {
            name = Component.translatable("thaumcraft.champion.crimson_praetor", modifier);
        } else if (mob instanceof CrimsonCultistEntity cultist) {
            name = Component.translatable("thaumcraft.champion.crimson_cultist", modifier, cultist.role().name().toLowerCase());
        } else if (mob instanceof EldritchWardenEntity warden) {
            name = Component.translatable("entity.thaumcraft.eldritch_warden.champion", Component.literal(warden.getTitle()), modifier);
        } else if (mob instanceof EldritchGolemEntity) {
            name = Component.translatable("entity.thaumcraft.eldritch_golem.champion", modifier);
        } else if (mob instanceof EldritchGuardianEntity) {
            name = Component.translatable("thaumcraft.champion.eldritch_guardian", modifier);
        } else {
            name = Component.translatable("thaumcraft.champion.generic", modifier, mob.getType().getDescription());
        }
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
    }

    private static int championWhitelistLevel(Mob mob) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (id == null) {
            return -1;
        }
        return CHAMPION_MOD_WHITELIST.getOrDefault(id.toString(), -1);
    }

    private static boolean isBossLikeChampion(Mob mob) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        String key = id == null ? "" : id.toString();
        return key.endsWith("crimson_praetor") || key.endsWith("eldritch_warden") || key.endsWith("eldritch_golem") || key.endsWith("taintacle_giant");
    }

    private static boolean isNetherOrEnd(ServerLevel level) {
        String path = level.dimension().location().getPath();
        return "the_nether".equals(path) || "the_end".equals(path) || path.contains("outer") || path.contains("eldritch");
    }

    private static boolean isDangerousLocation(ServerLevel level, BlockPos center) {
        if (isNetherOrEnd(level)) {
            return true;
        }
        BlockPos min = center.offset(-6, -3, -6);
        BlockPos max = center.offset(6, 3, 6);
        for (BlockPos scan : BlockPos.betweenClosed(min, max)) {
            ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(level.getBlockState(scan).getBlock());
            if (blockId == null) {
                continue;
            }
            String path = blockId.getPath();
            if (path.contains("taint") || path.contains("eldritch") || path.contains("flux")) {
                return true;
            }
        }
        return false;
    }

    public static ResourceLocation particleTexture(int mod) {
        String name = mod >= 0 && mod < MODS.length ? MODS[mod].name() : "normal";
        return new ResourceLocation("thaumcraft", "textures/original/thaumcraft4/misc/champion_" + name + ".png");
    }

    public record ChampionData(int id, String name, int type) {
    }
}
