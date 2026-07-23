package com.darkifov.thaumcraft.item.gear;

import com.darkifov.thaumcraft.Aspect;
import com.darkifov.thaumcraft.AspectList;
import com.darkifov.thaumcraft.block.EssentiaJarBlockItem;
import com.darkifov.thaumcraft.infusion.TC4RunicArmorHelper;
import com.darkifov.thaumcraft.menu.HoverHarnessMenu;
import com.darkifov.thaumcraft.wand.TC4VisDiscountGear;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/** Functional Forge 1.19.2 port of TC4 ItemHoverHarness. */
public final class HoverHarnessItem extends ArmorItem implements TC4VisDiscountGear {
    public static final String TAG_JAR = "jar";
    public static final String TAG_HOVER = "hover";
    public static final String TAG_CHARGE = "charge";
    public static final int EFFICIENCY = 360;

    public HoverHarnessItem(Properties properties) {
        super(TC4HoverHarnessArmorMaterial.INSTANCE, EquipmentSlot.CHEST, properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new com.darkifov.thaumcraft.client.render.TC4HoverHarnessClientExtension());
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "thaumcraft:textures/models/hoverharness.png";
    }

    @Override
    public int getVisDiscount(ItemStack stack, LivingEntity wearer, Aspect aspect) {
        return aspect == Aspect.AER ? 5 : 2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack harness = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean mainHand = hand == InteractionHand.MAIN_HAND;
            NetworkHooks.openScreen(
                    serverPlayer,
                    new SimpleMenuProvider(
                            (int id, Inventory inventory, Player menuPlayer) -> new HoverHarnessMenu(id, inventory, hand),
                            Component.translatable("container.thaumcraft.hover_harness")
                    ),
                    buffer -> buffer.writeBoolean(mainHand)
            );
        }
        return InteractionResultHolder.sidedSuccess(harness, level.isClientSide);
    }

    public static boolean isValidFuelJar(ItemStack stack) {
        return stack.getItem() instanceof EssentiaJarBlockItem && getFuel(stack) > 0;
    }

    public static ItemStack getJar(ItemStack harness) {
        CompoundTag root = harness.getTag();
        if (root == null || !root.contains(TAG_JAR, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(root.getCompound(TAG_JAR));
    }

    public static void setJar(ItemStack harness, ItemStack jar) {
        CompoundTag root = harness.getOrCreateTag();
        if (jar == null || jar.isEmpty()) {
            root.remove(TAG_JAR);
            root.putShort(TAG_CHARGE, (short) 0);
            return;
        }
        ItemStack copy = jar.copy();
        copy.setCount(1);
        root.put(TAG_JAR, copy.save(new CompoundTag()));
    }

    public static int getFuel(ItemStack jar) {
        if (!(jar.getItem() instanceof EssentiaJarBlockItem)) {
            return 0;
        }
        return Math.max(0, EssentiaJarBlockItem.itemAspects(jar).get(Aspect.POTENTIA));
    }

    public static int getStoredFuel(ItemStack harness) {
        return getFuel(getJar(harness));
    }

    public static boolean isHoverEnabled(ItemStack harness) {
        CompoundTag tag = harness.getTag();
        return tag != null && tag.getByte(TAG_HOVER) == 1;
    }

    public static void setHoverEnabled(ItemStack harness, boolean enabled) {
        harness.getOrCreateTag().putByte(TAG_HOVER, (byte) (enabled ? 1 : 0));
    }

    public static int getCharge(ItemStack harness) {
        CompoundTag tag = harness.getTag();
        return tag == null ? 0 : Math.max(0, tag.getShort(TAG_CHARGE));
    }

    public static void setCharge(ItemStack harness, int charge) {
        harness.getOrCreateTag().putShort(TAG_CHARGE, (short) Math.max(0, Math.min(Short.MAX_VALUE, charge)));
    }

    /** TC4 Hover.expendCharge: 360 ticks per Potentia, or 288 while the hover girdle is worn. */
    public static boolean expendCharge(ItemStack harness, boolean girdle) {
        ItemStack jar = getJar(harness);
        int fuel = getFuel(jar);
        if (fuel <= 0) {
            return false;
        }
        int threshold = Math.round(EFFICIENCY * (girdle ? 0.8F : 1.0F));
        int charge = getCharge(harness);
        if (charge < threshold) {
            setCharge(harness, charge + 1);
            return true;
        }

        setCharge(harness, 0);
        fuel--;
        writePotentia(jar, fuel);
        setJar(harness, jar);
        return fuel > 0;
    }

    private static void writePotentia(ItemStack jar, int amount) {
        Aspect filter = EssentiaJarBlockItem.itemFilter(jar);
        EssentiaJarBlockItem.writeItemData(jar, amount > 0 ? Aspect.POTENTIA : null, amount, filter);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int fuel = getStoredFuel(stack);
        tooltip.add(Component.translatable("tooltip.thaumcraft.hover_harness.fuel", fuel)
                .withStyle(fuel > 0 ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
        tooltip.add(Component.translatable(isHoverEnabled(stack)
                        ? "tooltip.thaumcraft.hover_harness.active"
                        : "tooltip.thaumcraft.hover_harness.inactive")
                .withStyle(isHoverEnabled(stack) ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.thaumcraft.hover_harness.key").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thaumcraft.vis_discount", 2).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.thaumcraft.vis_discount_aer", 5).withStyle(ChatFormatting.DARK_PURPLE));
        TC4RunicArmorHelper.appendTooltip(stack, tooltip);
    }
}
