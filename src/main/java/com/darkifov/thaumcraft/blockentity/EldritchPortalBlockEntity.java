package com.darkifov.thaumcraft.blockentity;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.config.ThaumcraftConfig;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class EldritchPortalBlockEntity extends BlockEntity {
    private int cooldown = 0;
    private int stability = 100;
    private boolean encounterActive = false;
    private int encounterTicks = 0;
    private int wave = 0;
    private UUID ownerId = null;
    private String ownerName = "";

    public EldritchPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumcraftMod.ELDRITCH_PORTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public int cooldown() {
        return cooldown;
    }

    public int stability() {
        return stability;
    }

    public boolean encounterActive() {
        return encounterActive;
    }

    public int encounterTicks() {
        return encounterTicks;
    }

    public int wave() {
        return wave;
    }

    public Component status() {
        if (encounterActive) {
            return Component.literal("Eldritch Portal | ACTIVE | Wave " + wave + " | Stability " + stability + " | Owner " + ownerName)
                    .withStyle(ChatFormatting.DARK_PURPLE);
        }

        return Component.literal("Eldritch Portal | Cooldown " + cooldown + " | Stability " + stability)
                .withStyle(ChatFormatting.DARK_PURPLE);
    }

    public boolean startEncounter(Player player, boolean awakenedKey) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (encounterActive) {
            player.displayClientMessage(status(), false);
            return false;
        }

        if (cooldown > 0) {
            player.displayClientMessage(Component.literal("The portal is unstable. Cooldown: " + cooldown + " ticks.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        int warp = PlayerThaumData.getWarp(player);
        int attunement = PlayerThaumData.getEldritchAttunement(player);

        if (warp < ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_WARP.get() || attunement < ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_ATTUNEMENT.get()) {
            player.displayClientMessage(Component.literal("The portal rejects you. Need " + ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_WARP.get() + "+ Warp and " + ThaumcraftConfig.ELDRITCH_PORTAL_REQUIRED_ATTUNEMENT.get() + "+ Eldritch Attunement.").withStyle(ChatFormatting.RED), false);
            return false;
        }

        buildArena();
        encounterActive = true;
        encounterTicks = 0;
        wave = 0;
        stability = awakenedKey ? ThaumcraftConfig.ELDRITCH_PORTAL_AWAKENED_STABILITY.get() : ThaumcraftConfig.ELDRITCH_PORTAL_BASE_STABILITY.get();
        ownerId = player.getUUID();
        ownerName = player.getName().getString();

        PlayerThaumData.addWarp(player, awakenedKey ? 1 : 2);
        PlayerThaumData.addEldritchAttunement(player, awakenedKey ? 10 : 5);
        PlayerThaumData.unlockResearch(player, "ELDRITCH_ARENA");

        if (player instanceof ServerPlayer serverPlayer) {
            ThaumcraftNetwork.syncResearch(serverPlayer);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.75D, worldPosition.getZ() + 0.5D, 100, 1.2D, 0.8D, 1.2D, 0.09D);
            serverLevel.playSound(null, worldPosition, SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.9F, 0.6F);
        }

        player.displayClientMessage(Component.literal("The Eldritch arena awakens. Survive the guardian waves.").withStyle(ChatFormatting.DARK_PURPLE), false);
        setChangedAndSync();
        return true;
    }

    private void tickServer() {
        if (level == null) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }

        if (!encounterActive) {
            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 20L == 0L) {
                serverLevel.sendParticles(ParticleTypes.PORTAL, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.8D, worldPosition.getZ() + 0.5D, 8, 0.6D, 0.4D, 0.6D, 0.03D);
            }

            setChangedAndSync();
            return;
        }

        encounterTicks++;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.85D, worldPosition.getZ() + 0.5D, 12, 0.8D, 0.5D, 0.8D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.6D, worldPosition.getZ() + 0.5D, 4, 0.6D, 0.35D, 0.6D, 0.03D);
        }

        if (encounterTicks == 40 || encounterTicks == 180 || encounterTicks == 340) {
            spawnWave();
        }

        if (encounterTicks % 80 == 0) {
            stability = Math.max(0, stability - ThaumcraftConfig.ELDRITCH_PORTAL_STABILITY_DRAIN.get());
            Player owner = getOwner();

            if (owner != null) {
                owner.displayClientMessage(Component.literal("Eldritch portal stability: " + stability).withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }

        if (stability <= 0) {
            failEncounter("Portal stability collapsed.");
            return;
        }

        if (encounterTicks >= ThaumcraftConfig.ELDRITCH_ENCOUNTER_DURATION_TICKS.get()) {
            completeEncounter();
        }

        setChangedAndSync();
    }

    private void spawnWave() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        wave++;
        Player owner = getOwner();

        if (owner != null) {
            owner.displayClientMessage(Component.literal("Eldritch wave " + wave + " begins.").withStyle(ChatFormatting.DARK_PURPLE), false);
        }

        for (int i = 0; i < wave + 1; i++) {
            EnderMan guardian = EntityType.ENDERMAN.create(serverLevel);

            if (guardian != null) {
                BlockPos spawn = worldPosition.offset(level.random.nextInt(9) - 4, 1, level.random.nextInt(9) - 4);
                guardian.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                guardian.setCustomName(Component.literal(wave >= 3 ? "Eldritch Guardian" : "Eldritch Sentinel").withStyle(ChatFormatting.DARK_PURPLE));
                guardian.setCustomNameVisible(true);
                guardian.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, Math.min(2, wave)));
                guardian.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60, 0));

                if (owner != null) {
                    guardian.setTarget(owner);
                }

                serverLevel.addFreshEntity(guardian);
            }
        }

        if (wave >= 2) {
            Vex vex = EntityType.VEX.create(serverLevel);

            if (vex != null) {
                vex.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 2.2D, worldPosition.getZ() + 0.5D, 0.0F, 0.0F);
                vex.setCustomName(Component.literal("Eldritch Wisp").withStyle(ChatFormatting.LIGHT_PURPLE));
                vex.setCustomNameVisible(true);

                if (owner != null) {
                    vex.setTarget(owner);
                }

                serverLevel.addFreshEntity(vex);
            }
        }

        serverLevel.playSound(null, worldPosition, SoundEvents.AMBIENT_CAVE, SoundSource.HOSTILE, 0.8F, 0.65F);
    }

    private void completeEncounter() {
        if (level == null) {
            return;
        }

        Player owner = getOwner();

        if (owner != null) {
            PlayerThaumData.unlockResearch(owner, "ELDRITCH_GUARDIAN");
            PlayerThaumData.unlockResearch(owner, "AWAKENED_CRIMSON_KEY");
            PlayerThaumData.addEldritchAttunement(owner, 15);

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }

            owner.displayClientMessage(Component.literal("The guardian wave collapses. Eldritch loot falls from the portal.").withStyle(ChatFormatting.GOLD), false);
        }

        ItemEntity relic = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.1D, worldPosition.getZ() + 0.5D, new ItemStack(ThaumcraftMod.ELDRITCH_RELIC.get()));
        level.addFreshEntity(relic);

        if (level.random.nextBoolean()) {
            ItemEntity core = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.3D, worldPosition.getZ() + 0.5D, new ItemStack(ThaumcraftMod.ELDRITCH_GUARDIAN_CORE.get()));
            level.addFreshEntity(core);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.85D, worldPosition.getZ() + 0.5D, 80, 1.1D, 0.6D, 1.1D, 0.08D);
            serverLevel.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 0.7F);
        }

        encounterActive = false;
        encounterTicks = 0;
        wave = 0;
        cooldown = ThaumcraftConfig.ELDRITCH_PORTAL_COOLDOWN_SECONDS.get() * 20;
        stability = 100;
        setChangedAndSync();
    }

    private void failEncounter(String reason) {
        Player owner = getOwner();

        if (owner != null) {
            PlayerThaumData.addWarp(owner, 3);
            owner.displayClientMessage(Component.literal("Eldritch encounter failed: " + reason).withStyle(ChatFormatting.RED), false);

            if (owner instanceof ServerPlayer serverPlayer) {
                ThaumcraftNetwork.syncResearch(serverPlayer);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.85D, worldPosition.getZ() + 0.5D, 90, 1.3D, 0.6D, 1.3D, 0.08D);
            serverLevel.playSound(null, worldPosition, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.8F, 0.55F);
        }

        encounterActive = false;
        encounterTicks = 0;
        wave = 0;
        cooldown = ThaumcraftConfig.ELDRITCH_PORTAL_FAIL_COOLDOWN_SECONDS.get() * 20;
        stability = 60;
        setChangedAndSync();
    }

    private void buildArena() {
        if (level == null) {
            return;
        }

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockPos floor = worldPosition.offset(x, -1, z);

                if (Math.abs(x) == 5 || Math.abs(z) == 5 || (x * x + z * z <= 25)) {
                    level.setBlock(floor, ThaumcraftMod.ELDRITCH_STONE.get().defaultBlockState(), 3);
                }
            }
        }

        level.setBlock(worldPosition.offset(5, 0, 5), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(worldPosition.offset(-5, 0, 5), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(worldPosition.offset(5, 0, -5), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
        level.setBlock(worldPosition.offset(-5, 0, -5), ThaumcraftMod.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
    }

    private Player getOwner() {
        if (!(level instanceof ServerLevel serverLevel) || ownerId == null) {
            return null;
        }

        return serverLevel.getServer().getPlayerList().getPlayer(ownerId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchPortalBlockEntity portal) {
        portal.tickServer();
    }

    public void setChangedAndSync() {
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Cooldown", cooldown);
        tag.putInt("Stability", stability);
        tag.putBoolean("EncounterActive", encounterActive);
        tag.putInt("EncounterTicks", encounterTicks);
        tag.putInt("Wave", wave);

        if (ownerId != null) {
            tag.putUUID("OwnerId", ownerId);
        }

        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt("Cooldown");
        stability = tag.contains("Stability") ? tag.getInt("Stability") : 100;
        encounterActive = tag.getBoolean("EncounterActive");
        encounterTicks = tag.getInt("EncounterTicks");
        wave = tag.getInt("Wave");
        ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        ownerName = tag.getString("OwnerName");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }
}
