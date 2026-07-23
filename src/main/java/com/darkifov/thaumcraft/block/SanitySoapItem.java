package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.data.PlayerThaumData;
import com.darkifov.thaumcraft.network.ThaumcraftNetwork;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import com.darkifov.thaumcraft.warp.TC4WarpRuntimeParity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Runtime port of TC4 {@code ItemSanitySoap}.
 *
 * <p>The soap must be used for ten seconds. Completion removes every point of
 * temporary warp and has a 33% chance to remove one sticky warp. Warp Ward adds
 * the original 25 percentage-point bonus. The purifying-fluid bonus is also
 * recognized by registry id so it starts working automatically once that block
 * is materialized by a later porting stage.</p>
 */
public class SanitySoapItem extends Item {
    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return TC4WarpRuntimeParity.SANITY_SOAP_USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        int usedTicks = getUseDuration(stack) - remainingUseDuration;
        if (!level.isClientSide && livingEntity instanceof ServerPlayer serverPlayer && usedTicks > TC4WarpRuntimeParity.SANITY_SOAP_COMPLETION_THRESHOLD) {
            cleanse(serverPlayer, stack);
            serverPlayer.stopUsingItem();
            return;
        }
        if (!level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }

        if (level.random.nextFloat() < 0.2F) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TC4Sounds.event("roots"),
                    SoundSource.PLAYERS, 0.1F, 1.5F + level.random.nextFloat() * 0.2F, false);
        }

        for (int i = 0; i < 5; i++) {
            double x = player.getX() - 0.5D + level.random.nextDouble();
            double y = player.getBoundingBox().minY + level.random.nextDouble() * player.getBbHeight();
            double z = player.getZ() - 0.5D + level.random.nextDouble();
            level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof ServerPlayer player) {
            cleanse(player, stack);
        }
        return stack;
    }

    private static void cleanse(ServerPlayer player, ItemStack stack) {
        int temporaryBefore = PlayerThaumData.getWarpTemporary(player);
        int stickyBefore = PlayerThaumData.getWarpSticky(player);

        float stickyChance = TC4WarpRuntimeParity.sanitySoapStickyChance(
                player.hasEffect(ThaumcraftMod.WARP_WARD.get()),
                isPurifyingFluid(player)
        );

        if (stickyBefore > 0 && player.getRandom().nextFloat() < stickyChance) {
            PlayerThaumData.addWarpSticky(player, -1);
        }
        if (temporaryBefore > 0) {
            PlayerThaumData.addWarpTemporary(player, -temporaryBefore);
        }

        stack.shrink(TC4WarpRuntimeParity.sanitySoapConsumption(
                player.getAbilities().instabuild
        ));

        ServerLevel level = player.getLevel();
        level.playSound(null, player.blockPosition(), TC4Sounds.event("craftstart"),
                SoundSource.PLAYERS, 0.25F, 1.0F);
        level.sendParticles(ParticleTypes.BUBBLE_POP, player.getX(), player.getY() + 0.8D, player.getZ(),
                20, 0.75D, 0.9D, 0.75D, 0.02D);
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY() + 0.8D, player.getZ(),
                12, 0.65D, 0.8D, 0.65D, 0.02D);

        int temporaryRemoved = temporaryBefore - PlayerThaumData.getWarpTemporary(player);
        int stickyRemoved = stickyBefore - PlayerThaumData.getWarpSticky(player);
        player.displayClientMessage(Component.translatable(
                "message.thaumcraft.sanity_soap.cleaned", temporaryRemoved, stickyRemoved
        ).withStyle(ChatFormatting.AQUA), false);
        ThaumcraftNetwork.syncResearch(player);
    }

    private static boolean isPurifyingFluid(ServerPlayer player) {
        BlockPos feet = player.blockPosition();
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(player.level.getBlockState(feet).getBlock());
        if (id == null || !ThaumcraftMod.MOD_ID.equals(id.getNamespace())) {
            return false;
        }
        return id.getPath().equals("purifying_fluid")
                || id.getPath().equals("fluid_pure")
                || id.getPath().equals("tc4_block_fluid_pure");
    }
}
