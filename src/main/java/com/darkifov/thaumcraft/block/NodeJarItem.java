package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.aura.TC4NodeJarRuntime;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.client.render.NodeJarItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import com.darkifov.thaumcraft.porting.TC4Sounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

/**
 * Stage132: TC4 Node in a Jar port.
 *
 * One item represents both the empty and filled jar. Filled jars keep the old
 * TC4 node data in NBT so the node can be placed again later.
 */
public class NodeJarItem extends Item {
    public NodeJarItem(Properties properties) {
        super(properties);
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
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        CompoundTag root = stack.getOrCreateTag();
        if (level.getBlockEntity(clicked) instanceof AuraNodeBlockEntity node && !TC4NodeJarRuntime.hasNode(root)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            CompoundTag nodeTag = TC4NodeJarRuntime.capture(level, node);
            root.put(TC4NodeJarRuntime.TAG_NODE_JAR, nodeTag);
            level.removeBlock(clicked, false);
            level.playSound(null, clicked, TC4Sounds.event("jar"), SoundSource.BLOCKS, 0.85F, 0.9F);
            player.displayClientMessage(Component.translatable("thaumcraft.nodejar.captured",
                    Component.translatable("thaumcraft.node.type." + nodeTag.getString("NodeType").toLowerCase(java.util.Locale.ROOT)),
                    Component.translatable("thaumcraft.node.modifier." + nodeTag.getString("NodeModifier").toLowerCase(java.util.Locale.ROOT)))
                    .withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.CONSUME;
        }

        if (TC4NodeJarRuntime.hasNode(root)) {
            BlockPos placePos = clicked.relative(context.getClickedFace());
            if (!level.getBlockState(placePos).getMaterial().isReplaceable()) {
                return InteractionResult.FAIL;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            BlockState nodeState = ThaumcraftMod.AURA_NODE.get().defaultBlockState();
            level.setBlock(placePos, nodeState, 3);
            if (level.getBlockEntity(placePos) instanceof AuraNodeBlockEntity node) {
                node.initializeFromJarTag(root.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR));
            }
            level.playSound(null, placePos, TC4Sounds.event("jar"), SoundSource.BLOCKS, 0.9F, 1.15F);
            player.displayClientMessage(Component.translatable("thaumcraft.nodejar.released").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            if (!player.getAbilities().instabuild) {
                root.remove(TC4NodeJarRuntime.TAG_NODE_JAR);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TC4NodeJarRuntime.TAG_NODE_JAR)) {
            tooltip.add(Component.translatable("thaumcraft.nodejar.empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("thaumcraft.nodejar.empty_hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        CompoundTag nodeTag = tag.getCompound(TC4NodeJarRuntime.TAG_NODE_JAR);
        AspectList aspects = new AspectList();
        aspects.load(nodeTag.getCompound("Aspects"));
        tooltip.add(Component.translatable("thaumcraft.nodejar.contains",
                Component.translatable("thaumcraft.node.type." + nodeTag.getString("NodeType").toLowerCase(java.util.Locale.ROOT)),
                Component.translatable("thaumcraft.node.modifier." + nodeTag.getString("NodeModifier").toLowerCase(java.util.Locale.ROOT))).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("thaumcraft.nodejar.stability", nodeTag.getInt("Stability"), nodeTag.getInt("PreservationPercent")).withStyle(ChatFormatting.GRAY));
        tooltip.add(aspects.toComponent().withStyle(ChatFormatting.DARK_GRAY));
    }
}
