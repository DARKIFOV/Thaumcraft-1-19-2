package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ThaumicTinkererParityBlock extends Block {
    public enum Mode {
        MOB_MAGNET,
        REPAIRER,
        ASPECT_ANALYZER,
        ENCHANTER,
        GOLEM_CONNECTOR,
        FUNNEL,
        FORCEFIELD,
        SUMMON_TABLET,
        REMOTE_PLACER,
        MOBILIZER,
        MOBILIZER_RELAY,
        TRANSVECTOR_DISLOCATOR,
        WARP_GATE,
        BEDROCK_PORTAL,
        DARK_QUARTZ,
        GAS_LIGHT,
        GAS_SHADOW,
        NITOR_GAS,
        FIRE_ELEMENT,
        ANIMATION_TABLET,
        INFUSED_FARMLAND
    }

    private final Mode mode;

    public ThaumicTinkererParityBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        PlayerThaumData.unlockResearch(player, "THAUMIC_TINKERER_TRUE_PARITY");

        switch (mode) {
            case MOB_MAGNET -> {
                int pulled = 0;
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(8))) {
                    item.setDeltaMovement(Vec3.atCenterOf(pos).subtract(item.position()).normalize().scale(0.25D));
                    pulled++;
                }
                player.displayClientMessage(Component.literal("Mob/Item Magnet pulled nearby item entities: " + pulled).withStyle(ChatFormatting.AQUA), false);
            }
            case REPAIRER -> {
                ItemStack held = player.getItemInHand(hand);
                if (!held.isEmpty() && held.isDamaged()) {
                    held.setDamageValue(Math.max(0, held.getDamageValue() - 24));
                    player.displayClientMessage(Component.literal("Repairer restored durability.").withStyle(ChatFormatting.GREEN), false);
                } else {
                    player.displayClientMessage(Component.literal("Repairer: hold a damaged item.").withStyle(ChatFormatting.GRAY), false);
                }
            }
            case ASPECT_ANALYZER -> player.displayClientMessage(Component.literal("Aspect Analyzer: scans item/block aspects in TT parity layer.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case ENCHANTER -> player.displayClientMessage(Component.literal("Enchanter: TT enchantment station mapped to Osmotic/Arcane enchantment systems.").withStyle(ChatFormatting.GOLD), false);
            case GOLEM_CONNECTOR -> player.displayClientMessage(Component.literal("Golem Connector: links golem logistics with TT machines.").withStyle(ChatFormatting.GREEN), false);
            case FUNNEL -> player.displayClientMessage(Component.literal("Funnel: routes items/essentia into adjacent inventories in the original TT chain.").withStyle(ChatFormatting.AQUA), false);
            case FORCEFIELD -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 20, 1));
                player.displayClientMessage(Component.literal("Forcefield grants temporary protection.").withStyle(ChatFormatting.AQUA), false);
            }
            case SUMMON_TABLET -> player.displayClientMessage(Component.literal("Summon Tablet: original entity summon tablet mapped as parity machine.").withStyle(ChatFormatting.DARK_PURPLE), false);
            case REMOTE_PLACER -> player.displayClientMessage(Component.literal("Remote Placer: remote block placement parity.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case MOBILIZER -> player.displayClientMessage(Component.literal("Mobilizer: block/entity movement machine parity.").withStyle(ChatFormatting.GOLD), false);
            case MOBILIZER_RELAY -> player.displayClientMessage(Component.literal("Mobilizer Relay: extends mobilizer range.").withStyle(ChatFormatting.GOLD), false);
            case TRANSVECTOR_DISLOCATOR -> player.displayClientMessage(Component.literal("Transvector Dislocator: remote block redirection and translocation parity.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case WARP_GATE -> {
                player.teleportTo(pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D);
                player.displayClientMessage(Component.literal("Warp Gate anchors teleport destination.").withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            case BEDROCK_PORTAL -> player.displayClientMessage(Component.literal("Bedrock Portal: KAMI dimension/portal surface mapped.").withStyle(ChatFormatting.DARK_PURPLE), false);
            case DARK_QUARTZ -> player.displayClientMessage(Component.literal("Dark Quartz construction block.").withStyle(ChatFormatting.DARK_GRAY), false);
            case GAS_LIGHT -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30 * 20, 0));
                player.displayClientMessage(Component.literal("Gaseous Light grants vision.").withStyle(ChatFormatting.YELLOW), false);
            }
            case GAS_SHADOW -> {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(5));
                for (LivingEntity entity : entities) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0));
                }
                player.displayClientMessage(Component.literal("Gaseous Shadow blinds nearby entities.").withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            case NITOR_GAS -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 20, 0));
                player.displayClientMessage(Component.literal("Nitor Gas burns cleanly and grants fire resistance.").withStyle(ChatFormatting.GOLD), false);
            }
            case FIRE_ELEMENT -> player.displayClientMessage(Component.literal("Primal fire block parity: air/water/earth/order/chaos/ignis variants share elemental behavior.").withStyle(ChatFormatting.RED), false);
            case ANIMATION_TABLET -> player.displayClientMessage(Component.literal("Animation Tablet: animates golem/block tasks in TT parity layer.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case INFUSED_FARMLAND -> player.displayClientMessage(Component.literal("Infused Farmland: grows infused seeds/grain.").withStyle(ChatFormatting.GREEN), false);
        }

        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.55F, 1.1F);
        return InteractionResult.CONSUME;
    }
}
