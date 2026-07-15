package com.darkifov.thaumcraft.item;

import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.client.render.TravelingTrunkItemRenderer;
import com.darkifov.thaumcraft.entity.TravelingTrunkEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

/** Spawns the actual TC4 Traveling Trunk and uses its chest model in item contexts. */
public final class TravelingTrunkItem extends Item {
    public TravelingTrunkItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return TravelingTrunkItemRenderer.instance();
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        TravelingTrunkEntity trunk = ThaumcraftMod.TRAVELING_TRUNK.get().create(serverLevel);
        if (trunk == null) return InteractionResult.FAIL;
        trunk.moveTo(context.getClickedPos().relative(context.getClickedFace()).getX() + 0.5D,
                context.getClickedPos().relative(context.getClickedFace()).getY(),
                context.getClickedPos().relative(context.getClickedFace()).getZ() + 0.5D,
                context.getRotation(), 0.0F);
        Player player = context.getPlayer();
        if (player != null) {
            trunk.tame(player);
        }
        ItemStack stack = context.getItemInHand();
        if (stack.hasCustomHoverName()) trunk.setCustomName(stack.getHoverName());
        if (stack.hasTag() && stack.getTag().contains("Upgrade")) trunk.setUpgrade(stack.getTag().getInt("Upgrade"));
        serverLevel.addFreshEntity(trunk);
        if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int upgrade = stack.hasTag() ? stack.getTag().getInt("Upgrade") : -1;
        if (upgrade >= 0) tooltip.add(Component.literal("TC4 upgrade: " + upgrade).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.thaumcraft.travel_trunk_place").withStyle(ChatFormatting.GRAY));
    }
}
