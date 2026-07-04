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

public class ThaumicTinkererUtilityItem extends Item {
    public enum Mode {
        TOME_KNOWLEDGE,
        INFUSED_SCRIBING_TOOLS,
        BOTTOMLESS_POUCH,
        HELMET_REVEALING,
        KAMI_CORE,
        ICHOR_GEAR
    }

    private final Mode mode;

    public ThaumicTinkererUtilityItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (mode == Mode.TOME_KNOWLEDGE) {
                boolean unlocked = PlayerThaumData.unlockResearch(player, "TT_SHARED_KNOWLEDGE");
                player.displayClientMessage(Component.literal(unlocked
                        ? "Tome of Knowledge Sharing unlocked TT_SHARED_KNOWLEDGE."
                        : "Tome of Knowledge Sharing: knowledge already shared.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);
            } else if (mode == Mode.INFUSED_SCRIBING_TOOLS) {
                PlayerThaumData.unlockResearch(player, "TT_INFUSED_SCRIBING");
                player.displayClientMessage(Component.literal("Infused Scribing Tools are reusable research tools for Thaumic Tinkerer research.").withStyle(ChatFormatting.AQUA), false);
            } else if (mode == Mode.BOTTOMLESS_POUCH) {
                player.displayClientMessage(Component.literal("Bottomless Pouch is handled by its dedicated pouch item class in Stage 65.").withStyle(ChatFormatting.GOLD), false);
            } else if (mode == Mode.HELMET_REVEALING) {
                player.displayClientMessage(Component.literal("Helmet of Revealing is handled by its dedicated helmet item class in Stage 65.").withStyle(ChatFormatting.AQUA), false);
            } else if (mode == Mode.KAMI_CORE) {
                player.displayClientMessage(Component.literal("KAMI Core is handled by its dedicated KAMI item class in Stage 65.").withStyle(ChatFormatting.DARK_PURPLE), false);
            } else {
                player.displayClientMessage(Component.literal("Ichor gear is handled by real ArmorItem/SwordItem/PickaxeItem classes in Stage 65.").withStyle(ChatFormatting.GOLD), false);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(description().withStyle(ChatFormatting.GRAY));
    }

    private Component description() {
        return switch (mode) {
            case TOME_KNOWLEDGE -> Component.literal("Shares a Thaumic Tinkerer research unlock.");
            case INFUSED_SCRIBING_TOOLS -> Component.literal("Reusable magical scribing tools; research branch marker.");
            case BOTTOMLESS_POUCH -> Component.literal("Deprecated placeholder; dedicated item handles pouch logic.");
            case HELMET_REVEALING -> Component.literal("Deprecated placeholder; dedicated item handles revealing logic.");
            case KAMI_CORE -> Component.literal("Deprecated placeholder; dedicated item handles KAMI logic.");
            case ICHOR_GEAR -> Component.literal("Deprecated placeholder; dedicated item handles Ichor gear logic.");
        };
    }
}
