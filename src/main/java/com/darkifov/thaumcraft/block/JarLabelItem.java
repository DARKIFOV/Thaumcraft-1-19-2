package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.ThaumcraftMod;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.jar.JarTubeInteractionRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/** TC4 itemResource metadata 13 label: AspectList root entry with amount zero. */
public class JarLabelItem extends Item {
    private static final String LEGACY_TAG_ASPECT = "Aspect";

    public JarLabelItem(Properties properties) {
        super(properties);
    }

    public static Aspect getAspect(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        if (tag.contains(EssentiaJarBlockItem.ITEM_ASPECTS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(EssentiaJarBlockItem.ITEM_ASPECTS, Tag.TAG_COMPOUND);
            if (!list.isEmpty()) {
                return Aspect.byId(list.getCompound(0).getString(EssentiaJarBlockItem.ITEM_ASPECT_KEY));
            }
        }
        return Aspect.byId(tag.getString(LEGACY_TAG_ASPECT));
    }

    public static boolean isBlank(ItemStack stack) {
        return getAspect(stack) == null;
    }

    public static void setAspect(ItemStack stack, Aspect aspect) {
        if (stack == null || stack.isEmpty()) return;
        if (aspect == null) {
            clearAspect(stack);
            return;
        }
        CompoundTag root = stack.getOrCreateTag();
        root.remove(LEGACY_TAG_ASPECT);
        ListTag list = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putString(EssentiaJarBlockItem.ITEM_ASPECT_KEY, aspect.id());
        entry.putInt(EssentiaJarBlockItem.ITEM_ASPECT_AMOUNT, 0);
        list.add(entry);
        root.put(EssentiaJarBlockItem.ITEM_ASPECTS, list);
    }

    public static void clearAspect(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null) return;
        CompoundTag tag = stack.getTag();
        tag.remove(LEGACY_TAG_ASPECT);
        tag.remove(EssentiaJarBlockItem.ITEM_ASPECTS);
        if (tag.isEmpty()) stack.setTag(null);
    }

    public static ItemStack withAspect(Aspect aspect) {
        ItemStack stack = new ItemStack(ThaumcraftMod.JAR_LABEL.get());
        setAspect(stack, aspect);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof EssentiaJarBlockEntity jar)
                || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide) {
            JarTubeInteractionRuntime.applyLabelToJar(jar, context.getPlayer(), context.getItemInHand(), context.getClickedFace());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = getAspect(stack);
        if (aspect == null) {
            tooltip.add(Component.translatable("tooltip.thaumcraft.jar_label.blank").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("aspect.thaumcraft." + aspect.id()).withStyle(aspect.color()));
        }
    }
}
