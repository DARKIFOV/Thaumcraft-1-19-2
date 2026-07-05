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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ThaumcraftExtrasParityBlock extends Block {
    public enum Mode {
        CHARGER,
        EXCHANGER,
        DARK_INFUSER,
        MAGIC_GENERATOR,
        MAGIC_SOLAR_PANEL,
        MAGIC_CHARGER,
        TELEPORTER,
        TESLA,
        COLOR_BLOCK,
        HIDDEN_WARDED,
        OPEN_WARDED,
        WARDED_GLASS,
        WARDED_PILLAR,
        WARDED_SLAB,
        WARDED_WALL,
        WARDED_CARPET,
        WARDED_COVER,
        CACTUS,
        DARK_SILVERWOOD,
        DARK_SILVERWOOD_PLANKS,
        IGNIS_FUEL_BLOCK,
        INFUSION_INFO,
        LAVA_BLOCK,
        CABLE,
        CLEAR_GLASS
    }

    private final Mode mode;

    public ThaumcraftExtrasParityBlock(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        PlayerThaumData.unlockResearch(player, "THAUMCRAFT_EXTRAS_TRUE_PARITY");

        switch (mode) {
            case CHARGER -> player.displayClientMessage(Component.literal("Charger: charges wand/vis items in Thaumcraft Extras parity layer.").withStyle(ChatFormatting.AQUA), false);
            case EXCHANGER -> player.displayClientMessage(Component.literal("Exchanger: exchanges configured blocks/items.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case DARK_INFUSER -> player.displayClientMessage(Component.literal("Dark Infuser: dark thaumium and dark crystal processing.").withStyle(ChatFormatting.DARK_PURPLE), false);
            case MAGIC_GENERATOR -> player.displayClientMessage(Component.literal("Magic Generator: converts magical fuel into internal power.").withStyle(ChatFormatting.GOLD), false);
            case MAGIC_SOLAR_PANEL -> player.displayClientMessage(Component.literal("Magic Solar Panel: passive magical generation in sunlight.").withStyle(ChatFormatting.YELLOW), false);
            case MAGIC_CHARGER -> player.displayClientMessage(Component.literal("Magic Charger: advanced wand and crystal charger.").withStyle(ChatFormatting.AQUA), false);
            case TELEPORTER -> {
                player.teleportTo(pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D);
                player.displayClientMessage(Component.literal("Teleporter anchor activated.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            case TESLA -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10 * 20, 1));
                player.displayClientMessage(Component.literal("Tesla block discharges magical electricity.").withStyle(ChatFormatting.AQUA), false);
            }
            case COLOR_BLOCK -> player.displayClientMessage(Component.literal("Color Block: stores configurable colour state.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            case HIDDEN_WARDED -> player.displayClientMessage(Component.literal("Hidden Warded block: camouflaged warded construction.").withStyle(ChatFormatting.GRAY), false);
            case OPEN_WARDED -> player.displayClientMessage(Component.literal("Open Warded block: pass-through warded construction.").withStyle(ChatFormatting.GRAY), false);
            case WARDED_GLASS -> player.displayClientMessage(Component.literal("Warded Glass: protected transparent construction.").withStyle(ChatFormatting.AQUA), false);
            case WARDED_PILLAR, WARDED_SLAB, WARDED_WALL, WARDED_CARPET, WARDED_COVER -> player.displayClientMessage(Component.literal("Warded decorative block parity: " + mode.name()).withStyle(ChatFormatting.GRAY), false);
            case CACTUS -> {
                player.displayClientMessage(Component.literal("Thaumcraft Extras cactus pricks nearby entities.").withStyle(ChatFormatting.GREEN), false);
            }
            case DARK_SILVERWOOD -> player.displayClientMessage(Component.literal("Dark Silverwood log: TCE dark tree block.").withStyle(ChatFormatting.DARK_PURPLE), false);
            case DARK_SILVERWOOD_PLANKS -> player.displayClientMessage(Component.literal("Dark Silverwood planks.").withStyle(ChatFormatting.DARK_PURPLE), false);
            case IGNIS_FUEL_BLOCK -> {
                player.setSecondsOnFire(2);
                player.displayClientMessage(Component.literal("Ignis Fuel Block burns as magical fuel.").withStyle(ChatFormatting.RED), false);
            }
            case INFUSION_INFO -> player.displayClientMessage(Component.literal("Infusion Info block displays TCE infusion data.").withStyle(ChatFormatting.GOLD), false);
            case LAVA_BLOCK -> {
                player.setSecondsOnFire(3);
                player.displayClientMessage(Component.literal("Lava Block emits heat.").withStyle(ChatFormatting.RED), false);
            }
            case CABLE -> player.displayClientMessage(Component.literal("TCE Cable routes machine energy/links.").withStyle(ChatFormatting.AQUA), false);
            case CLEAR_GLASS -> player.displayClientMessage(Component.literal("Clear Glass connected-texture parity block.").withStyle(ChatFormatting.AQUA), false);
        }

        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5F, 1.0F);
        return InteractionResult.CONSUME;
    }
}
