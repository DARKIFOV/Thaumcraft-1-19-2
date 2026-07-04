package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.data.PlayerThaumData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ThaumicEnergisticsUtilityItem extends Item {
    public enum Mode {
        AE_WRENCH,
        GOLEM_WIRELESS_BACKPACK,
        KNOWLEDGE_CORE,
        COALESCENCE_CORE,
        DIFFUSION_CORE,
        IRON_GEAR,
        ESSENTIA_CELL_CASING,
        CRAFTING_ASPECT,
        STORAGE_COMPONENT_64K
    }

    private final Mode mode;

    public ThaumicEnergisticsUtilityItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerThaumData.unlockResearch(player, "THAUMIC_ENERGISTICS_TRUE_PARITY");

            switch (mode) {
                case AE_WRENCH -> player.displayClientMessage(Component.literal("AE Wrench: diagnostic wrench for Thaumic Energistics machines and parts.").withStyle(ChatFormatting.AQUA), false);
                case GOLEM_WIRELESS_BACKPACK -> player.displayClientMessage(Component.literal("Golem Wireless Backpack: golem-side wireless essentia link placeholder/diagnostic item.").withStyle(ChatFormatting.GREEN), false);
                case KNOWLEDGE_CORE -> player.displayClientMessage(Component.literal("Knowledge Core: used by Knowledge Inscriber and advanced TE devices.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                case COALESCENCE_CORE -> player.displayClientMessage(Component.literal("Coalescence Core: condenses essentia/item crafting logic.").withStyle(ChatFormatting.GOLD), false);
                case DIFFUSION_CORE -> player.displayClientMessage(Component.literal("Diffusion Core: distributes essentia through the digital network.").withStyle(ChatFormatting.AQUA), false);
                case IRON_GEAR -> player.displayClientMessage(Component.literal("Iron Gear: mechanical component for gear boxes and vibration chamber.").withStyle(ChatFormatting.GRAY), false);
                case ESSENTIA_CELL_CASING -> player.displayClientMessage(Component.literal("Essentia Cell Casing: shell for high-tier digital essentia cells.").withStyle(ChatFormatting.DARK_AQUA), false);
                case CRAFTING_ASPECT -> player.displayClientMessage(Component.literal("Crafting Aspect: encoded aspect ingredient for autocrafting patterns.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                case STORAGE_COMPONENT_64K -> player.displayClientMessage(Component.literal("64k Storage Component: final high-tier digital essentia storage component.").withStyle(ChatFormatting.GOLD), false);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return mode == Mode.KNOWLEDGE_CORE || mode == Mode.STORAGE_COMPONENT_64K;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Thaumic Energistics parity item: " + mode.name()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Stage 80 true-parity surface item from the original 1.7.10 class/resource map.").withStyle(ChatFormatting.GRAY));
    }
}
