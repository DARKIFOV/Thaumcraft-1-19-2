package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.AspectDatabase;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.AuraNodeBlockEntity;
import com.darkifov.thaumcraft.data.NodeScanData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class ThaumometerItem extends Item {
    private static final String TAG_SCANNED = "ScannedBlocks";

    public ThaumometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            BlockPos pos = context.getClickedPos();
            BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);

            if (blockEntity instanceof AuraNodeBlockEntity node) {
                if (!node.initialized()) {
                    node.initializeFromPosition();
                }

                boolean firstNodeScan = NodeScanData.markScanned(context.getPlayer(), pos);

                context.getPlayer().displayClientMessage(
                        Component.literal(firstNodeScan ? "New aura node scan: " : "Aura node already scanned: ")
                                .append(Component.literal(node.nodeType()).withStyle(ChatFormatting.LIGHT_PURPLE))
                                .append(Component.literal(" | Aspects: "))
                                .append(node.aspects().toComponent())
                                .append(Component.literal(node.isStabilized() ? " | Stabilized" : "")),
                        false
                );

                if (firstNodeScan) {
                    ItemStack reward = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

                    if (!context.getPlayer().getInventory().add(reward)) {
                        Containers.dropItemStack(context.getLevel(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
                    }

                    context.getPlayer().displayClientMessage(Component.literal("Aura insight gained! You received a Research Point.").withStyle(ChatFormatting.GOLD), false);
                }

                return InteractionResult.CONSUME;
            }

            BlockState state = context.getLevel().getBlockState(pos);
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            String key = id == null ? "unknown" : id.toString();

            boolean firstScan = addScannedBlock(stack, key);
            int scanCount = getScanCount(stack);

            AspectList aspects = AspectDatabase.getAspectsForBlock(state);

            context.getPlayer().displayClientMessage(
                    Component.literal(firstScan ? "New scan: " : "Already scanned: ")
                            .append(Component.literal(state.getBlock().getName().getString()).withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" | Aspects: "))
                            .append(aspects.toComponent())
                            .append(Component.literal(" | Total scans: " + scanCount).withStyle(ChatFormatting.GOLD)),
                    false
            );

            if (firstScan && scanCount > 0 && scanCount % 5 == 0) {
                ItemStack reward = new ItemStack(ThaumcraftMod.RESEARCH_POINT.get());

                if (!context.getPlayer().getInventory().add(reward)) {
                    Containers.dropItemStack(context.getLevel(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward);
                }

                context.getPlayer().displayClientMessage(
                        Component.literal("Research insight gained! You received a Research Point.").withStyle(ChatFormatting.LIGHT_PURPLE),
                        false
                );
            }
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    private boolean addScannedBlock(ItemStack stack, String key) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = tag.getList(TAG_SCANNED, 8);

        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(key)) {
                return false;
            }
        }

        list.add(StringTag.valueOf(key));
        tag.put(TAG_SCANNED, list);
        return true;
    }

    private int getScanCount(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getList(TAG_SCANNED, 8).size();
    }
}
