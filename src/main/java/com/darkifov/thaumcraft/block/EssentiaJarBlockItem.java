package com.darkifov.thaumcraft.block;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.blockentity.EssentiaJarBlockEntity;
import com.darkifov.thaumcraft.client.render.EssentiaJarItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/** Exact TC4 ItemJarFilled carrier: root Aspects list plus optional AspectFilter. */
public final class EssentiaJarBlockItem extends BlockItem {
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag"; // migration-only pre-11.64.35 wrapper
    public static final String ITEM_ASPECTS = "Aspects";
    public static final String ITEM_ASPECT_KEY = "key";
    public static final String ITEM_ASPECT_AMOUNT = "amount";

    public EssentiaJarBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return EssentiaJarItemRenderer.instance();
            }
        });
    }

    /** Converts exact item NBT (or old port NBT) into TileJarFillable placement NBT. */
    public static CompoundTag readJarData(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null) return new CompoundTag();
        CompoundTag source = root.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(BLOCK_ENTITY_TAG) : root;
        return canonicalBlockEntityData(source);
    }

    public static void writeJarData(ItemStack stack, EssentiaJarBlockEntity jar) {
        writeItemData(stack, jar.storedAspect(), jar.amount(), jar.filterAspect());
    }

    /** Writes the original ItemJarFilled root NBT and strips all world-only/migration keys. */
    public static void writeItemData(ItemStack stack, @Nullable Aspect aspect, int amount, @Nullable Aspect filter) {
        CompoundTag root = stack.getOrCreateTag();
        removeLegacyJarKeys(root);
        if (aspect != null && amount > 0) {
            ListTag list = new ListTag();
            CompoundTag entry = new CompoundTag();
            entry.putString(ITEM_ASPECT_KEY, aspect.id());
            entry.putInt(ITEM_ASPECT_AMOUNT, amount);
            list.add(entry);
            root.put(ITEM_ASPECTS, list);
        }
        if (filter != null) root.putString("AspectFilter", filter.id());
        if (root.isEmpty()) stack.setTag(null);
    }

    public static AspectList itemAspects(ItemStack stack) {
        CompoundTag data = readJarData(stack);
        AspectList result = new AspectList();
        Aspect aspect = Aspect.byId(data.getString("Aspect"));
        int amount = Math.max(0, data.getShort("Amount"));
        if (aspect != null && amount > 0) result.add(aspect, amount);
        return result;
    }

    @Nullable
    public static Aspect itemFilter(ItemStack stack) {
        return Aspect.byId(readJarData(stack).getString("AspectFilter"));
    }

    private static CompoundTag canonicalBlockEntityData(CompoundTag source) {
        CompoundTag result = new CompoundTag();
        Aspect aspect = Aspect.byId(source.getString("Aspect"));
        int amount = Math.max(0, source.getShort("Amount"));

        if (source.contains(ITEM_ASPECTS, Tag.TAG_LIST)) {
            ListTag list = source.getList(ITEM_ASPECTS, Tag.TAG_COMPOUND);
            if (!list.isEmpty()) {
                CompoundTag entry = list.getCompound(0);
                aspect = Aspect.byId(entry.getString(ITEM_ASPECT_KEY));
                amount = Math.max(0, entry.getInt(ITEM_ASPECT_AMOUNT));
            }
        } else if (aspect == null && source.contains(ITEM_ASPECTS, Tag.TAG_COMPOUND)) {
            // One-time migration from the port's old AspectList compound format.
            AspectList migrated = new AspectList();
            migrated.load(source.getCompound(ITEM_ASPECTS));
            aspect = migrated.firstAspect();
            amount = aspect == null ? 0 : migrated.get(aspect);
        }

        Aspect filter = Aspect.byId(source.getString("AspectFilter"));
        if (filter == null) filter = Aspect.byId(source.getString("FilterAspect"));
        if (aspect != null && amount > 0) {
            result.putString("Aspect", aspect.id());
            result.putShort("Amount", (short) amount);
        }
        if (filter != null) result.putString("AspectFilter", filter.id());
        return result;
    }

    private static void removeLegacyJarKeys(CompoundTag root) {
        root.remove(BLOCK_ENTITY_TAG);
        root.remove(ITEM_ASPECTS);
        root.remove("FilterAspect");
        root.remove("Aspect");
        root.remove("Amount");
        root.remove("AspectFilter");
        root.remove("facing");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        AspectList aspects = itemAspects(stack);
        Aspect stored = aspects.firstAspect();
        if (stored != null) {
            tooltip.add(Component.literal(stored.displayName() + " x " + aspects.get(stored))
                    .withStyle(stored.color()));
        }
        Aspect filter = itemFilter(stack);
        if (filter != null) {
            tooltip.add(Component.literal(filter.displayName()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
