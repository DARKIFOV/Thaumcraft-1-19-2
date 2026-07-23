package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import com.darkifov.thaumcraft.client.render.NodeJarItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/** Filled portable form of TC4 BlockJar metadata 2 / ItemJarNode. */
public class NodeJarItem extends BlockItem {
    public NodeJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return NodeJarItemRenderer.instance();
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        CompoundTag root = context.getItemInHand().getTag();
        if (root == null || !TC4NodeJarRuntime.hasNode(root)) {
            if (context.getPlayer() != null && !context.getLevel().isClientSide) {
                context.getPlayer().displayClientMessage(Component.translatable("thaumcraft.nodejar.ritual_hint")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !TC4NodeJarRuntime.hasNode(tag)) {
            tooltip.add(Component.translatable("thaumcraft.nodejar.migration_empty").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("thaumcraft.nodejar.ritual_hint").withStyle(ChatFormatting.GRAY));
            return;
        }

        CompoundTag nodeTag = tag.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR);
        AspectList aspects = new AspectList();
        aspects.load(nodeTag.getCompound("Aspects"));
        tooltip.add(Component.translatable("thaumcraft.nodejar.contains",
                Component.translatable("thaumcraft.node.type." + nodeTag.getString("NodeType").toLowerCase(java.util.Locale.ROOT)),
                Component.translatable("thaumcraft.node.modifier." + nodeTag.getString("NodeModifier").toLowerCase(java.util.Locale.ROOT)))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(aspects.toComponent().withStyle(ChatFormatting.DARK_GRAY));
    }
}
